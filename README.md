# Assembleia &mdash; API de Sessões de Votação

API REST para gerenciar pautas e sessões de votação em assembleias de cooperativas,
onde cada associado tem direito a um voto (`Sim`/`Não`) por pauta.

---

## Stack

| Item | Escolha | Motivo |
|------|---------|--------|
| Linguagem | Java 17 (LTS) | Requisito do desafio |
| Framework | Spring Boot 3.4 | Requisito do desafio |
| Persistência | Spring Data JPA + Hibernate | Padrão de mercado, mapeamento objeto-relacional |
| Banco (default) | H2 em **arquivo** | Persiste entre restarts (requisito) sem dependência externa |
| Banco (opcional) | PostgreSQL via `docker-compose` | Demonstra uso com banco relacional real |
| Documentação | springdoc-openapi (Swagger UI) | Documentação da API viva, gerada do código |
| Validação | Bean Validation (`jakarta.validation`) | Validação declarativa nos DTOs de request |
| Observabilidade | Spring Boot Actuator | Health check para execução em nuvem |
| Testes | JUnit 5, Spring Boot Test, Mockito | Unitários e de integração |
| Cobertura | JaCoCo (`mvn verify`) | Ferramenta de qualidade, relatório em `target/site/jacoco` |

O racional de cada escolha de arquitetura é registrado nas **mensagens de commit** e em
comentários no código, acompanhando a etapa em que a decisão é aplicada.

---

## Como executar

### Pré-requisitos
- JDK 17
- (Opcional) Docker, apenas para o profile `postgres`

### Perfil padrão (H2 em arquivo &mdash; recomendado para avaliação)

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`. O banco é gravado em `./data/assembleia.mv.db`
e **sobrevive ao restart** da aplicação.

### Perfil PostgreSQL

```bash
docker compose up -d
SPRING_PROFILES_ACTIVE=postgres ./mvnw spring-boot:run
```

### Testes

```bash
./mvnw test        # testes
./mvnw verify      # testes + relatório de cobertura (JaCoCo)
```

---

## Endpoints principais

| Recurso | Descrição |
|---------|-----------|
| Swagger UI | `http://localhost:8080/swagger-ui.html` |
| OpenAPI JSON | `http://localhost:8080/api-docs` |
| Health check | `http://localhost:8080/actuator/health` |
| Console H2 | `http://localhost:8080/h2-console` (profile default) |

A referência completa da API é gerada pelo springdoc e fica disponível no Swagger UI.

---

## Configuração

Chaves relevantes (`src/main/resources/application.yml`), todas sobrescrevíveis por variável de ambiente:

| Chave | Default | Descrição |
|-------|---------|-----------|
| `assembleia.sessao.duracao-padrao` | `1m` | Duração da sessão quando não informada na abertura |
| `assembleia.integracao.user-info.base-url` | `https://user-info.herokuapp.com` | Serviço externo de elegibilidade por CPF (bônus 1) |
| `assembleia.integracao.user-info.fake` | `true` | Usa stub local quando o serviço externo está indisponível |

---

