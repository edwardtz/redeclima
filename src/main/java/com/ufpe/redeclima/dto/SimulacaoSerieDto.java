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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import net.sf.json.JSONObject;

import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.dao.DadoSeriePrevisaoDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.ResultadoSerieRasDao;
import com.ufpe.redeclima.dao.SimulacaoSerieDao;
import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.model.SimulacaoSerie;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.batch.EstadoSimulacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 * Contem os parâmetros de entrada do simulador quando a entrada de dados é uma serie
 */

@Component
@Scope("prototype")
public class SimulacaoSerieDto implements Serializable, SimDto {

	private static final long serialVersionUID = 1L;

	private Bacia bacia;
	
	private SeriePrevisao seriePrevisao;
	
	private Usuario usuario;
	
	private EnumUnidadeTempo unidade;
	
	/* Estado da simulacao */
	private EstadoSimulacao estadoSimulacao;
	
	@Autowired
	private DadoSeriePrevisaoDao dadoSeriePrevisaoDao;
	
	@Autowired
	private SimulacaoSerieDao simulacaoSerieDao;
	
	@Autowired
	private ResultadoSerieRasDao resultadoSerieRasDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private Workspace workspace;
	
	public SimulacaoSerieDto(){
		estadoSimulacao = new EstadoSimulacao();
	}
	
	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public SeriePrevisao getSeriePrevisao() {
		return seriePrevisao;
	}

	public void setSeriePrevisao(SeriePrevisao seriePrevisao) {
		this.seriePrevisao = seriePrevisao;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public EnumUnidadeTempo getUnidade() {
		return unidade;
	}

	public void setUnidade(EnumUnidadeTempo unidade) {
		this.unidade = unidade;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.dto.SimDto#getHash()
	 */
	public String getHash() {
		return bacia.getId() + "#" + seriePrevisao.getId() + "#" + usuario.getId();
		
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.dto.SimDto#getGrade()
	 */
	public Grade getGrade() {
		return seriePrevisao.getGrade();
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.dto.SimDto#getDataInicial()
	 */
	public Date getDataInicial() {
		return seriePrevisao.getDataInicio();
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.dto.SimDto#getDataFinal()
	 */
	public Date getDataFinal() {
		return seriePrevisao.getDataFim();
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#list(com.ufpe.redeclima.model.PontoGrade, com.ufpe.redeclima.interfaces.SimDto)
	 */
	public List<JSONObject> list(PontoDado pontoGrade, SimDto simDto) {
		return dadoSeriePrevisaoDao.listBy((PontoGrade)pontoGrade, simDto);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarArquivosTemporais(com.ufpe.redeclima.interfaces.SimDto)
	 */
	public void salvarArquivosTemporais() {

		
		SimulacaoSerie simulacao = simulacaoSerieDao.findByAttributes(this);
		
		workspace.setUsuario(usuario);
		StringBuilder sb = new StringBuilder();
		sb.append(workspace.getHMSLog(bacia));
		sb.append(System.getProperty("line.separator"));
		sb.append(workspace.getRASLog(bacia));
		
		if(simulacao==null){
			simulacao = new SimulacaoSerie();
			simulacao.setBacia(bacia);
			simulacao.setSerie(seriePrevisao); 
			simulacao.setUsuario(usuario);
			simulacao.setLog(sb.toString());
			simulacaoSerieDao.save(simulacao);
		} else {
			simulacao.setLog(sb.toString());
			simulacaoSerieDao.saveOrUpdate(simulacao);
		}
		
		resultadoSerieRasDao.salvarArquivosTemporais(simulacao);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarResultados()
	 */
	public void salvarResultados() {

		SimulacaoSerie simulacao = simulacaoSerieDao.findByAttributes(this);
		
		//Remover resultados anteriores da simulação
		resultadoSerieRasDao.removerResultados(simulacao);
		//Adicionar novos resultados
		resultadoSerieRasDao.salvarResultados(simulacao);
				
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#setGrade(com.ufpe.redeclima.model.Grade)
	 */
	public void setGrade(Grade grade) {
		seriePrevisao.setGrade(grade);
		
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#setDataInicial(java.util.Date)
	 */
	public void setDataInicial(Date dataInicial) {
		seriePrevisao.setDataInicio(dataInicial);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#setDataFinal(java.util.Date)
	 */
	public void setDataFinal(Date dataFinal) {
		seriePrevisao.setDataFim(dataFinal);
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
		List<PontoGrade> pontosGrade =  pontoGradeDao.getPontosGradeBacia(this);
		List<PontoDado> pontosDado = new ArrayList<PontoDado>();
		for(PontoGrade ponto: pontosGrade){
			pontosDado.add(ponto);
		}
		return pontosDado;
	}
	
}
