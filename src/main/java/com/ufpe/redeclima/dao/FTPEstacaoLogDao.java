/**
    * Copyright (c) 2013, Universidade Federal de Pernambuco e/ou seus parceiros. Todos os direitos reservados.
    * NÃO ALTERAR OU REMOVER O AVISO DE COPYRIGHT OU ESTE CABEÇALHO.
    *
    * Este código é software livre; o usuário pode redistribui-lo e/ou modifica-lo
    * de acordo com os termos da licença GNU General Public License version 2, como foi
    * publicado pela Free Software Foundation. 
    *
    * Este código é distribuído com a intenção de ajudar aos usuários, pesquisadores e cientistas, mas sem nenhuma
    * garantia; sem mesmo a garantia implícita de COMERCIALIZAÇÃO ou
    * ADEQUAÇÃO A UM DETERMINADO FIM. Veja a GNU General Public License
    * Versão 2 para mais detalhes (uma cópia está incluído no arquivo de licença que
    * acompanha esse código).
    *
    * Por favor, entrar em contato com a Universidade Federal de Pernambuco (UFPE),
    * Av. Prof. Moraes Rego, 1235 - Cidade Universitária, Recife - PE - CEP: 50670-901BR101, Brasil
    * ou visite o site www.ufpe.br se necessitar informação adicional ou alguma consulta.
 */
package com.ufpe.redeclima.dao;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.FTPEstacaoLog;

/**
 * @author edwardtz
 *
 */
@Component
public class FTPEstacaoLogDao {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(FTPEstacaoLog ftpEstacaoLog) {
		entityManager.persist(ftpEstacaoLog);
	}
	
	public void registrarComoProcessado(String nomeArquivo, String codigoEstacao){
		FTPEstacaoLog registro = new FTPEstacaoLog();
		registro.setId(nomeArquivo);
		registro.setCodigoEstacao(codigoEstacao);
		save(registro);
	}
	
	public FTPEstacaoLog findById(String nomeArquivo){
		return entityManager.find(FTPEstacaoLog.class, nomeArquivo);
	}

}
