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

import com.ufpe.redeclima.model.Trecho;

/**
 * @author edwardtz
 *
 */
@Component
public class TrechoDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(Trecho trecho) {
		entityManager.persist(trecho);
	}
	
	@Transactional
	public void saveOrUpdate(Trecho trecho) {
		entityManager.merge(trecho);
		entityManager.flush();
	}
	
	public Trecho findById(Long id){
		return entityManager.find(Trecho.class, id);
	}
	
	public Trecho findByNome(String nome){
		Trecho trecho;
		try{
			trecho = (Trecho) entityManager.createQuery("select t from Trecho t where t.nome=:nome")
					.setParameter("nome", nome)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return trecho;
	}

	@SuppressWarnings("unchecked")
	public List<Trecho> list() {
		return entityManager.createQuery("select t from Trecho t").getResultList();
	}
}
