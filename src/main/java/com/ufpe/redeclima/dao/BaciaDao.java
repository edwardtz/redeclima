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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Bacia;

@Component
public class BaciaDao {

	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(Bacia bacia) {
		entityManager.persist(bacia);
	}
	
	@Transactional
	public void saveOrUpdate(Bacia bacia) {
		entityManager.merge(bacia);
	}
	
	@Transactional
	public void remove(Bacia bacia) {
		Bacia baciaDelete = findById(bacia.getId());
		entityManager.remove(baciaDelete);
	}
	
	@Transactional
	public void update(Bacia bacia) {
		entityManager.merge(bacia);
	}
	
	public Bacia findById(Long id){
		return entityManager.find(Bacia.class, id);
	}
	
	public Bacia findByNome(String nome){
		Bacia bacia;
		try{
			bacia = (Bacia) entityManager.createQuery("from Bacia b where b.nome=:nome")
					.setParameter("nome", nome)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return bacia;
	}
	
	
	@SuppressWarnings("unchecked")
	public List<Bacia> list() {
		return entityManager.createQuery("from Bacia b").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Bacia> listOperativas() {
		return entityManager.createQuery("from Bacia b where b.configRios = true and b.configSecoes = true and b.configHms = true and b.configRas = true").getResultList();
	}
	
	
}
