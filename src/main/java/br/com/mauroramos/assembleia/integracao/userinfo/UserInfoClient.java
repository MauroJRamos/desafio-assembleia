package br.com.mauroramos.assembleia.integracao.userinfo;

public interface UserInfoClient {

    StatusElegibilidade consultar(String cpf);
}
