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

import com.ufpe.redeclima.model.AreaRecorte;

/**
 * @author edwardtz
 *
 */
@Component
public class AreaRecorteDao {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void save(AreaRecorte areaRecorte) {
		entityManager.persist(areaRecorte);
	}
	
	@Transactional
	public void saveOrUpdate(AreaRecorte areaRecorte) {
		entityManager.merge(areaRecorte);
	}
	
	@Transactional
	public void remove(AreaRecorte areaRecorte) {
		AreaRecorte areaRecorteDeletar = entityManager.find(AreaRecorte.class, areaRecorte.getId());
		entityManager.remove(areaRecorteDeletar);
	}
	
	@Transactional
	public void update(AreaRecorte areaRecorte) {
		entityManager.merge(areaRecorte);
	}
	
	public AreaRecorte findById(Long id){
		return entityManager.find(AreaRecorte.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<AreaRecorte> list() {
		return entityManager.createQuery("from AreaRecorte a").getResultList();
	}
	
	public AreaRecorte findByNome(String nomeAreaRecorte) {
		AreaRecorte areaRecorte;
		try{
			areaRecorte = (AreaRecorte) entityManager.createQuery("from AreaRecorte a where a.nome=:nome")
					.setParameter("nome", nomeAreaRecorte)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return areaRecorte;
	}
	
}
