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

import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumPerfil;


@Component
public class UsuarioDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private SimulacaoDao simulacaoDao;
	
	public Usuario findById(long usuarioId) {
		return entityManager.find(Usuario.class, usuarioId);
	}
	
	@Transactional
	public void save(Usuario usuario) {
		entityManager.persist(usuario);
	}
	
	@Transactional
	public void saveOrUpdate(Usuario usuario) {
		entityManager.merge(usuario);
	}
	
	@Transactional
	public void remove(Usuario usuario) {
		
		Usuario us = entityManager.find(Usuario.class, usuario.getId());
		simulacaoDao.apagarHistorico(usuario);
		entityManager.remove(us);
		
	}

	public Usuario findByLogin(String login) {
		try{
			return (Usuario) entityManager.createQuery("select u from Usuario u where u.login=:login")
										  .setParameter("login", login)
										  .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Usuario> list() {
		return entityManager.createQuery("select u from Usuario u").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Usuario> listByPerfil(EnumPerfil perfil) {
		return entityManager.createQuery("select u from Usuario u where u.perfil.id =:perfilId and u.ativo = true ")
										.setParameter("perfilId", perfil.getId())
										.getResultList();
	}
	
}
