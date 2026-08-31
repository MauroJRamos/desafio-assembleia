# Decisões de arquitetura

Registro curto das principais decisões de arquitetura do projeto, no formato ADR
(Architecture Decision Record) enxuto: contexto, decisão e consequências.

---

## Versionamento da API

**Decisão**: versionamento por URI (`/api/v1/...`).

**Alternativas consideradas**:

| Alternativa | Por que não agora |
|---|---|
| Header (`Accept: application/vnd.assembleia.v1+json`) | Mais "purista", mas difícil de testar manualmente (browser, cURL simples) e de documentar no Swagger UI |
| Query param (`?version=1`) | Polui cache HTTP e logs de acesso |

**Motivo da escolha**: simples, explícito, cacheável, trivial de testar e documentar, fácil de
rotear no Spring MVC (`@RequestMapping("/api/v1/...")`).

**Política de evolução**:
- `v1` é estável.
- Mudança **incompatível** (remover campo, mudar tipo, mudar semântica de um status HTTP) exige
  `v2`, coexistindo com `v1` — nunca quebra um cliente já integrado.
- Campo novo opcional na resposta é compatível e **não** exige nova versão.
- Quando `v2` existir, os controllers são versionados por pacote
  (`pauta.v1.PautaController`, `pauta.v2.PautaController`), não por sufixo de classe.

---

## ADR-001 &mdash; Request/Response DTO, nunca a entidade JPA na camada web

**Contexto**: expor a entidade JPA diretamente acopla o contrato HTTP ao esquema do banco e
arrisca vazar dados internos (relações `@ManyToOne`, `LazyInitializationException`).

**Decisão**: todo endpoint recebe um Request DTO e devolve um Response DTO (`record` Java).
Mapeamento entidade ↔ DTO em mappers estáticos manuais (`PautaMapper`, `VotoMapper`).

**Consequências**: mudança de coluna no banco não quebra o contrato da API; a resposta pode
ser desenhada para o cliente, não ser um espelho da tabela. Custo: uma classe a mais por
operação (aceitável para o tamanho do domínio).

---

## ADR-002 &mdash; Estado da sessão derivado de datas, nunca persistido

**Contexto**: persistir um campo `status` que precisa ser atualizado por um job é fonte de
inconsistência (status desatualizado, necessidade de scheduler).

**Decisão**: `Pauta` guarda só `sessaoAbertaEm`/`sessaoFechaEm`; o status
(`SEM_SESSAO`/`ABERTA`/`FECHADA`) é calculado comparando essas datas com o relógio no momento
da leitura.

**Consequências**: não precisa de job para "fechar" sessão — ela fecha sozinha quando o tempo
passa. `Clock` é injetado como bean, permitindo controlar o tempo nos testes sem `Thread.sleep`.

---

## ADR-003 &mdash; Unicidade de voto garantida no banco, não só na aplicação

**Contexto**: checar duplicidade só na aplicação (`SELECT` antes do `INSERT`) tem uma janela de
corrida sob concorrência real.

**Decisão**: constraint `UNIQUE (pauta_id, associado_id)` no banco. A aplicação ainda faz uma
checagem antecipada (`exists`) só para devolver uma mensagem de erro melhor no caminho feliz.

**Consequências**: corretude garantida mesmo com dois votos simultâneos do mesmo associado.
A violação da constraint é tratada globalmente (`DataIntegrityViolationException` → `409`).

---

## ADR-004 &mdash; Tratamento de erros padronizado com RFC 7807 (`ProblemDetail`)

**Contexto**: a v1 mapeava toda regra de negócio violada para `404`, independente da causa real
(sessão fechada, voto duplicado, etc.), e não tinha handler genérico nem para erros de
integridade de dados.

**Decisão**: catálogo de exceções de domínio (`RecursoNaoEncontradoException` → 404,
`ConflitoDeEstadoException` → 409, `RegraDeNegocioException` → 422,
`AssociadoNaoHabilitadoException` → 403, `IntegracaoIndisponivelException` → 503) mapeado
centralmente num `@RestControllerAdvice`, com corpo padronizado em `ProblemDetail`.

**Consequências**: toda resposta de erro segue o mesmo formato, facilitando o consumo pelo
cliente. Fallback genérico (`Exception` → 500) loga a causa real no servidor sem expor detalhes
internos na resposta.

---

## ADR-005 &mdash; Apuração de resultado via agregação no banco

**Contexto**: contar votos carregando todos os registros para a memória (`stream().count()`)
não escala para centenas de milhares de votos.

**Decisão**: `SELECT opcao, COUNT(*) FROM voto WHERE pauta_id = ? GROUP BY opcao` — a aplicação
nunca materializa a lista de votos.

**Consequências**: latência da apuração praticamente independente do volume de votos.
Índice `(pauta_id, opcao)` criado desde a etapa de modelagem para suportar essa query.

---

## ADR-006 &mdash; Integração de elegibilidade com fallback local

**Contexto**: o serviço externo de elegibilidade por CPF (`user-info.herokuapp.com`) foi
descontinuado (Heroku removeu o free tier).

**Decisão**: `UserInfoClient` como interface; duas implementações (`UserInfoClientReal`,
`UserInfoClientFake`) selecionadas por `@ConditionalOnProperty`
(`assembleia.integracao.user-info.fake`, `true` por padrão). Indisponibilidade do serviço real
bloqueia o voto (`503`), não libera por degradação graciosa.

**Consequências**: a aplicação funciona de ponta a ponta sem dependência externa viva; trocar
para o serviço real (se algum dia voltar) é uma mudança de configuração, não de código.
