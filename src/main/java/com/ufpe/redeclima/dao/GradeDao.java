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
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Grade;

@Component
public class GradeDao {

	@PersistenceContext
	private EntityManager entityManager;

	private String obterPrefixNomeArquivo(String nomeArquivo){
		String[] partes = nomeArquivo.split("\\+");
		String prefixo = partes[0].substring(0, partes[0].length() - 10);
		return prefixo;
	}
	
	@Transactional
	public void save(Grade grade) {
		entityManager.persist(grade);
	}
	
	@Transactional
	public void saveOrUpdate(Grade grade) {
		entityManager.merge(grade);
	}
	
	@Transactional
	public void remove(Grade grade) {
		Grade gradeDeletar = entityManager.find(Grade.class, grade.getId());
		entityManager.remove(gradeDeletar);
	}
	
	@Transactional
	public void update(Grade grade) {
		entityManager.merge(grade);
	}
	
	public Grade findById(Long id){
		return entityManager.find(Grade.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public List<Grade> list() {
		return entityManager.createQuery("from Grade g").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Grade> listAtivas() {
		return entityManager.createQuery("from Grade g where g.buscaAtiva = true ").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public Grade findByNome(String nomeGrade) {
		List<Grade> listaGrade = entityManager.createQuery("from Grade g where g.nome =:nome ")
				.setParameter("nome", nomeGrade)
				.getResultList();
		 if (listaGrade !=null && listaGrade.size()>0){
			 return listaGrade.get(0);
		 }
		 return null;
	}
	
}
