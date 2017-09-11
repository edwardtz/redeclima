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

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SeriePrevisao;

/**
 * @author edwardtz
 *
 */
@Component
public class SeriePrevisaoDao {

	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void save(SeriePrevisao seriePrevisao) {
		entityManager.persist(seriePrevisao);
	}
	
	@Transactional
	public void saveOrUpdate(SeriePrevisao seriePrevisao) {
		entityManager.merge(seriePrevisao);
	}
	
	@Transactional
	public void remove(SeriePrevisao seriePrevisao){
		SeriePrevisao previsao = entityManager.merge(seriePrevisao);
		entityManager.remove(previsao);
	}
	
	public SeriePrevisao findById(Long id){
		return entityManager.find(SeriePrevisao.class, id);
	}
	
	public SeriePrevisao findByAttributes(Date dataRodada, Date dataInicio, Date dataFim, Grade grade){
		SeriePrevisao valorPrevisao;
		try{
			valorPrevisao = (SeriePrevisao) entityManager.createQuery("select s from SeriePrevisao s where s.dataRodada=:dataRodada and s.dataInicio=:dataInicio and s.dataFim=:dataFim and s.grade.id=:gradeId  ")
					.setParameter("dataRodada", dataRodada)
					.setParameter("dataInicio", dataInicio)
					.setParameter("dataFim", dataFim)
					.setParameter("gradeId", grade.getId())
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return valorPrevisao;
	}
	
	@SuppressWarnings("unchecked")
	public List<SeriePrevisao> list(Grade grade){
			return entityManager.createQuery("select s from SeriePrevisao s where s.grade.id=:gradeId  ")
					.setParameter("gradeId", grade.getId())
					.getResultList();
	}
	
	/**
	 * Retorna o conjunto de series que não foram simuladas para um usuario com bacia e serie
	 * @return conjunto de series que podem pertecer a diferentes grades
	 * */
	@SuppressWarnings("unchecked")
	public List<SeriePrevisao> listNaoSimuladas(SimDto simDto){
			return entityManager.createQuery("select s from SeriePrevisao s " +
											" where s.id not in (select sim.serie.id from SeriePrevisao s2, SimulacaoSerie sim " +
																 " where sim.serie.id = s2.id and sim.bacia.id =:baciaId and sim.usuario.id =:usuarioId )  ")
					.setParameter("usuarioId", simDto.getUsuario().getId())
					.setParameter("baciaId", simDto.getBacia().getId())
					.getResultList();
	}
	
	/**
	 * Busca o cria a uma serie de dados
	 * */
	@Transactional
	public SeriePrevisao findOrCreate(Date dataRodada, Date dataInicio, Date dataFim, Grade grade){
		SeriePrevisao serie = findByAttributes(dataRodada, dataInicio, dataFim, grade);
		if (serie==null){
			serie = new SeriePrevisao();
			serie.setDataRodada(dataRodada);
			serie.setDataInicio(dataInicio);
			serie.setDataFim(dataFim);
			serie.setGrade(grade);
			save(serie);
		}
		return serie;
	}
	
}
