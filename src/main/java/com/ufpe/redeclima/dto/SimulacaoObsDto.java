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
package com.ufpe.redeclima.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.ResultadoObsRasDao;
import com.ufpe.redeclima.dao.SimulacaoObsDao;
import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.batch.EstadoSimulacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
@Component
@Scope("prototype")
public class SimulacaoObsDto implements InitializingBean, Serializable, SimDto {

	private static final long serialVersionUID = 1L;

	/* Bacia de referencia para a simulação */ 
	private Bacia bacia;
	
	private Grade grade;
	
	/* Data de incio da simulação */
	private Date dataInicial;
	
	/* Data final da simulação */
	private Date dataFinal;
	
	/* Unidade de entrada de dados da simulação, pode ser dia, hora, etc */
	private EnumUnidadeTempo unidade;
	
	/* Usuario que esta executando a simulaçao */
	private Usuario usuario;
	
	/* Indicador de si se deverá executar RAS apois HMS ou não */
	private boolean continua;
	
	/* Estado da simulacao */
	private EstadoSimulacao estadoSimulacao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private SimulacaoObsDao simulacaoObsDao;
	
	@Autowired
	private ResultadoObsRasDao resultadoObsRasDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private Workspace workspace;
	
	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getHash()
	 */
	public String getHash() {
		return bacia.getId() + "#" + usuario.getId();
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getBacia()
	 */
	public Bacia getBacia() {
		return bacia;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getDataInicial()
	 */
	public Date getDataInicial() {
		return dataInicial;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getDataFinal()
	 */
	public Date getDataFinal() {
		return dataFinal;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getUnidade()
	 */
	public EnumUnidadeTempo getUnidade() {
		return unidade;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getUsuario()
	 */
	public Usuario getUsuario() {
		return usuario;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#list(com.ufpe.redeclima.model.PontoGrade, com.ufpe.redeclima.interfaces.SimDto)
	 */
	public List<JSONObject> list(PontoDado estacao, SimDto simDto) {
		return dadosEstacaoTelemetricaDao.listSerie((Estacao) estacao, simDto);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarArquivosTemporais()
	 */
	public void salvarArquivosTemporais() {

		SimulacaoObs simulacao = simulacaoObsDao.findByAttributes(this);
		
		workspace.setUsuario(usuario);
		StringBuilder sb = new StringBuilder();
		sb.append(workspace.getHMSLog(bacia));
		sb.append(System.getProperty("line.separator"));
		sb.append(workspace.getRASLog(bacia));
		
		if(simulacao==null){
			simulacao = new SimulacaoObs();
			simulacao.setBacia(bacia);
			simulacao.setUsuario(usuario);
			simulacao.setDataInicio(dataInicial);
			simulacao.setDataFim(dataFinal);
			simulacao.setLog(sb.toString());
			simulacaoObsDao.save(simulacao);
		}else {
			simulacao.setLog(sb.toString());
			simulacaoObsDao.saveOrUpdate(simulacao);
		}
		
		resultadoObsRasDao.salvarArquivosTemporais(simulacao);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarResultados()
	 */
	public void salvarResultados() {

		SimulacaoObs simulacao = simulacaoObsDao.findByAttributes(this);
		
		//Remover resultados anteriores da simulação
		resultadoObsRasDao.removerResultados(simulacao);
		//Adicionar novos resultados
		resultadoObsRasDao.salvarResultados(simulacao);
		
	}

	public boolean isContinua() {
		return continua;
	}

	public void setContinua(boolean continua) {
		this.continua = continua;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public void setUnidade(EnumUnidadeTempo unidade) {
		this.unidade = unidade;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public EstadoSimulacao getEstadoSimulacao() {
		return estadoSimulacao;
	}

	public void setEstadoSimulacao(EstadoSimulacao estadoSimulacao) {
		this.estadoSimulacao = estadoSimulacao;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#getPontosDados()
	 */
	public List<PontoDado> getPontosDados() {
		List<Estacao> estacoes = estacaoDao.listEstacoesANA(bacia);
		List<PontoDado> pontosDados = new ArrayList<PontoDado>();
		for(Estacao estacao: estacoes){
			pontosDados.add(estacao);
		}
		return pontosDados;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		estadoSimulacao = new EstadoSimulacao();
		grade = new Grade();
		grade.setNome("Grade de Estações");
		grade.setQuantidadeTempoPeriodo(1);
		grade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
		
	}
	
}
