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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Simulacao;
import com.ufpe.redeclima.model.Usuario;

/**
 * @author edwardtz
 *
 */

@Component
public class SimulacaoDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ResultadoRasDao resultadoRasDao;
	
	@Transactional
	public void save(Simulacao simulacao) {
		entityManager.persist(simulacao);
	}
	
	@Transactional
	public void saveOrUpdate(Simulacao simulacao) {
		entityManager.merge(simulacao);
		entityManager.flush();
	}
	
	@Transactional
	public void remove(Simulacao simulacao){
		Simulacao sim = entityManager.merge(simulacao);
		entityManager.remove(sim);
	}
	
	/**
	 * Apaga o historico de simulações do usuario
	 * */
	@Transactional
	public void apagarHistorico(Usuario usuario){
		List<Simulacao> rodadas = listByUser(usuario.getId());
		if (rodadas != null){
			for (Simulacao sim: rodadas){
				resultadoRasDao.removerResultados(sim);
				entityManager.remove(sim);
			}
		}
	}
	
	public Simulacao findById(Long id){
		return entityManager.find(Simulacao.class, id);
	}
	
	public Simulacao findByAttributes(SimDto simDto){
		return findByAttributes(simDto.getUsuario().getId(), simDto.getBacia().getId(), simDto.getGrade().getId(), simDto.getDataInicial(), simDto.getDataFinal());
	}
	
	public Simulacao findByAttributes(Long usuarioId, Long baciaId, Long gradeId, Date dataInicio, Date dataFim){
		Simulacao simulacao;
		try{
			simulacao = (Simulacao) entityManager.createQuery("select s from Simulacao s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.grade.id=:gradeId and s.dataInicio =:dataInicio and s.dataFim=:dataFim  ")
					.setParameter("usuarioId", usuarioId)
					.setParameter("baciaId", baciaId)
					.setParameter("gradeId", gradeId)
					.setParameter("dataInicio", dataInicio)
					.setParameter("dataFim", dataFim)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return simulacao;
	}
	
	@SuppressWarnings("unchecked")
	public List<Simulacao> listByUser(Long usuarioId){
		return entityManager.createQuery("select s from Simulacao s where s.usuario.id=:usuarioId")
				.setParameter("usuarioId", usuarioId).
				getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<Simulacao> list() {
		return entityManager.createQuery("select s from Simulacao s").getResultList();
	}
	
}
