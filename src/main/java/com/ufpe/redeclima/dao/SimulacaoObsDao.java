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
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Usuario;

/**
 * @author edwardtz
 *
 */

@Component
public class SimulacaoObsDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ResultadoObsRasDao resultadoObsRasDao;
	
	@Transactional
	public void save(SimulacaoObs simulacao) {
		entityManager.persist(simulacao);
	}
	
	@Transactional
	public void saveOrUpdate(SimulacaoObs simulacao) {
		entityManager.merge(simulacao);
	}
	
	@Transactional
	public void remove(SimulacaoObs simulacao){
		SimulacaoObs sim = entityManager.merge(simulacao);
		entityManager.remove(sim);
	}
	
	/**
	 * Apaga o historico de simulações do usuario
	 * */
	@Transactional
	public void apagarHistorico(Usuario usuario){
		List<SimulacaoObs> rodadas = listByUser(usuario.getId());
		if (rodadas != null){
			for (SimulacaoObs sim: rodadas){
				resultadoObsRasDao.removerResultados(sim);
				entityManager.remove(sim);
			}
		}
	}
	
	public SimulacaoObs findById(Long id){
		return entityManager.find(SimulacaoObs.class, id);
	}
	
	public SimulacaoObs findByAttributes(SimDto simDto){
		return findByAttributes(simDto.getUsuario().getId(), simDto.getBacia().getId(), simDto.getDataInicial(), simDto.getDataFinal());
	}
	
	public SimulacaoObs findByAttributes(Long usuarioId, Long baciaId, Date dataInicio, Date dataFim){
		SimulacaoObs simulacao;
		try{
			simulacao = (SimulacaoObs) entityManager.createQuery("select s from SimulacaoObs s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.dataInicio =:dataInicio and s.dataFim=:dataFim  ")
					.setParameter("usuarioId", usuarioId)
					.setParameter("baciaId", baciaId)
					.setParameter("dataInicio", dataInicio)
					.setParameter("dataFim", dataFim)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return simulacao;
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoObs> listByUser(Long usuarioId){
		return entityManager.createQuery("select s from SimulacaoObs s where s.usuario.id=:usuarioId")
				.setParameter("usuarioId", usuarioId).
				getResultList();
	}
	
	public SimulacaoObs getUltima(Usuario usuario, Bacia bacia){
		return getUltima(usuario.getId(), bacia.getId());
	}
	
	public SimulacaoObs getUltima(Long usuarioId, Long baciaId){
		SimulacaoObs simulacao;
		try{
			simulacao = (SimulacaoObs) entityManager.createQuery("from SimulacaoObs s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.dataFim=(select max(s2.dataFim) from SimulacaoObs s2 where s2.usuario.id=:usuarioId and s2.bacia.id=:baciaId)  ")
					.setParameter("usuarioId", usuarioId)
					.setParameter("baciaId", baciaId)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return simulacao;
	}
	

	@SuppressWarnings("unchecked")
	public List<SimulacaoObs> list() {
		return entityManager.createQuery("select s from SimulacaoObs s").getResultList();
	}
	
}
