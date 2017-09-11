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

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.TipoEstacao;
import com.ufpe.redeclima.model.TipoNotificacao;

/**
 * @author edwardtz
 *
 */
@Component
public class TipoNotificacaoDao {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(TipoNotificacao tipoNotificacao) {
		entityManager.persist(tipoNotificacao);
	}

	public TipoNotificacao findById(Long id){
		return entityManager.find(TipoNotificacao.class, id);
	}
	
	public TipoNotificacao findByCodigo(String codigo) {
		try{
			return (TipoNotificacao) entityManager.createQuery("select t from TipoNotificacao t where t.codigo=:codigo")
										      .setParameter("codigo", codigo)
										      .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<TipoNotificacao> list() {
		return entityManager.createQuery("from TipoNotificacao t").getResultList();
	}

}
