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

import com.ufpe.redeclima.dto.SimulacaoSerieDto;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.SimulacaoSerie;
import com.ufpe.redeclima.model.Usuario;

/**
 * @author edwardtz
 *
 */

@Component
public class SimulacaoSerieDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private ResultadoSerieRasDao resultadoSerieRasDao;
	
	@Transactional
	public void save(SimulacaoSerie simulacao) {
		entityManager.persist(simulacao);
	}
	
	@Transactional
	public void saveOrUpdate(SimulacaoSerie simulacao) {
		entityManager.merge(simulacao);
	}
	
	@Transactional
	public void remove(SimulacaoSerie simulacao){
		SimulacaoSerie sim = entityManager.merge(simulacao);
		entityManager.remove(sim);
	}
	
	/**
	 * Apaga o historico de simulações do usuario
	 * */
	@Transactional
	public void apagarHistorico(Usuario usuario){
		List<SimulacaoSerie> rodadas = listByUser(usuario.getId());
		if (rodadas != null){
			for (SimulacaoSerie sim: rodadas){
				resultadoSerieRasDao.removerResultados(sim);
				entityManager.remove(sim);
			}
		}
	}
	
	public SimulacaoSerie findById(Long id){
		return entityManager.find(SimulacaoSerie.class, id);
	}
	
	public SimulacaoSerie findByAttributes(SimDto simDto){
		return findByAttributes(simDto.getUsuario().getId(), simDto.getBacia().getId(), ((SimulacaoSerieDto)simDto).getSeriePrevisao().getId());
	}
	
	public SimulacaoSerie findByAttributes(SimulacaoSerieDto simDto){
		return findByAttributes(simDto.getUsuario().getId(), simDto.getBacia().getId(), simDto.getSeriePrevisao().getId());
	}
	
	public SimulacaoSerie findByAttributes(Long usuarioId, Long baciaId, Long serieId){
		SimulacaoSerie simulacao;
		try{
			simulacao = (SimulacaoSerie) entityManager.createQuery("from SimulacaoSerie s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.serie.id=:serieId ")
					.setParameter("usuarioId", usuarioId)
					.setParameter("baciaId", baciaId)
					.setParameter("serieId", serieId)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return simulacao;
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> listByUser(Long usuarioId){
		return entityManager.createQuery("from SimulacaoSerie s where s.usuario.id=:usuarioId")
				.setParameter("usuarioId", usuarioId)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> list() {
		return entityManager.createQuery("from SimulacaoSerie s").getResultList();
	}
	
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> list(Usuario usuario, Bacia bacia) {
		return list(usuario.getId(), bacia.getId());
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> list(long usuarioId, long baciaId) {
		return entityManager.createQuery("from SimulacaoSerie s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId order by s.serie.dataRodada")
				.setParameter("usuarioId", usuarioId)
				.setParameter("baciaId", baciaId)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> listFrom(Usuario usuario, Bacia bacia, Date datafinal) {
		return listFrom(usuario.getId(), bacia.getId(), datafinal);
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> listFrom(long usuarioId, long baciaId, Date dataFinal) {
		return entityManager.createQuery("from SimulacaoSerie s where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.serie.dataFim >=:dataFinal order by d.serie.dataRodada")
				.setParameter("usuarioId", usuarioId)
				.setParameter("baciaId", baciaId)
				.setParameter("dataFinal", dataFinal)
				.getResultList();
	}
	
	/**
	 * Retorna todas as series cuja data final esteja dentro do periodo
	 * */
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> list(Usuario usuario, Bacia bacia, Grade grade) {
		return list(usuario.getId(), bacia.getId(), grade.getId());
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> list(long usuarioId, long baciaId, long gradeId) {
		return entityManager.createQuery("from SimulacaoSerie s " +
										 "where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.serie.grade.id =:gradeId " +
										 "order by s.serie.dataRodada")
				.setParameter("usuarioId", usuarioId)
				.setParameter("baciaId", baciaId)
				.setParameter("gradeId", gradeId)
				.getResultList();
	}
	
	/**
	 * Retorna todas as series cuja data final esteja dentro do periodo
	 * */
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> listByDataFinal(Usuario usuario, Bacia bacia, Grade grade, Date dataInicial, Date dataFinal) {
		return listByDataFinal(usuario.getId(), bacia.getId(), grade.getId(), dataInicial, dataFinal);
	}
	
	@SuppressWarnings("unchecked")
	public List<SimulacaoSerie> listByDataFinal(long usuarioId, long baciaId, long gradeId, Date dataInicial, Date dataFinal) {
		return entityManager.createQuery("from SimulacaoSerie s " +
										 "where s.usuario.id=:usuarioId and s.bacia.id=:baciaId and s.serie.grade.id =:gradeId and " +
										 "s.serie.dataFim >=:dataInicial and s.serie.dataFim <=:dataFinal " +
										 "order by s.serie.dataRodada")
				.setParameter("usuarioId", usuarioId)
				.setParameter("baciaId", baciaId)
				.setParameter("gradeId", gradeId)
				.setParameter("dataInicial", dataInicial)
				.setParameter("dataFinal", dataFinal)
				.getResultList();
	}
	
}
