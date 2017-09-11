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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15Min;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.util.EnumEstadoEstacao;
import com.ufpe.redeclima.util.EnumTipoResponsavel;

@Component
public class EstacaoDao {

	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(Estacao estacao) {
		entityManager.persist(estacao);
		entityManager.flush();
	}
	
	@Transactional
	public void saveOrUpdate(Estacao estacao) {
		entityManager.merge(estacao);
		entityManager.flush();
	}
	
	@Transactional
	public void remove(Estacao estacao) {
		Estacao estacaoDelete = entityManager.find(Estacao.class, estacao.getId());
		entityManager.remove(estacaoDelete);;
	}
	
	@Transactional
	public void atualizarEstado(Estacao estacao, DadosEstacaoTelemetrica15Min ultimoRegistro){
		
		if (ultimoRegistro==null){
			estacao.setEstadoEstacao(EnumEstadoEstacao.FDS);
			estacao.setDataAtualizacaoEstado(new Date());
		}else{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.HOUR_OF_DAY, -1);
			calendar.add(Calendar.MINUTE, -30);
			Date unaHoraAtras = calendar.getTime();
			if (ultimoRegistro.getId().getData().before(unaHoraAtras)){
				estacao.setEstadoEstacao(EnumEstadoEstacao.FDS);
			}else if(ultimoRegistro.getChuva()==-1 || ultimoRegistro.getNivel()==-1 || ultimoRegistro.getVazao()==-1){
				estacao.setEstadoEstacao(EnumEstadoEstacao.FED);
			}else{
				estacao.setEstadoEstacao(EnumEstadoEstacao.NRM);
			}
			
			estacao.setDataAtualizacaoEstado(ultimoRegistro.getId().getData());
		}
		
		entityManager.merge(estacao);
	}
	
	public Estacao findById(Long id) {
		return entityManager.find(Estacao.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public Estacao findByCodigo(int codigo) {
		List<Estacao> estacoes = entityManager.createQuery(
		    "from Estacao e where e.codigo = :codigo")
		    .setParameter("codigo", codigo)
		    .getResultList(); //TODO trocar para getSingleResult e testar
		
		if (estacoes!=null)
			return estacoes.get(0);
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Estacao> list() {
		return entityManager.createQuery("from Estacao e").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<Estacao> listEstacoesANA(Bacia bacia) {
		if(bacia!=null){
			return entityManager.createQuery("from Estacao e where e.bacia.id =:baciaId and e.responsavel =:codigoResponsavel ")
					.setParameter("baciaId", bacia.getId())
					.setParameter("codigoResponsavel", EnumTipoResponsavel.ANA)
					.getResultList();	
		}else{
			return new ArrayList<Estacao>();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<Estacao> listEstacoes(EnumTipoResponsavel responsavel) {
		if(responsavel!=null){
			return entityManager.createQuery("from Estacao e where e.responsavel =:codigoResponsavel ")
					.setParameter("codigoResponsavel", responsavel)
					.getResultList();	
		}else{
			return new ArrayList<Estacao>();
		}
	}
	
}
