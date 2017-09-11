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

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.ResultadoRasDao;
import com.ufpe.redeclima.dao.SimulacaoDao;
import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.model.Simulacao;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.batch.EstadoSimulacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@Component
@Scope("prototype")
public class SimulacaoDto implements Serializable, SimDto {

	private static final long serialVersionUID = 1L;

	/* Bacia de referencia para a simulação */ 
	private Bacia bacia;
	
	/* Grade de referencia para a simulação */
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
	private DadoPontoGradeDao dadoPontoGradeDao;
	
	@Autowired
	private SimulacaoDao simulacaoDao;
	
	@Autowired
	private ResultadoRasDao resultadoRasDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private Workspace workspace;

	public SimulacaoDto(){
		estadoSimulacao = new EstadoSimulacao();
	}
	
	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public EnumUnidadeTempo getUnidade() {
		return unidade;
	}

	public void setUnidade(EnumUnidadeTempo unidade) {
		this.unidade = unidade;
	}

	public boolean isContinua() {
		return continua;
	}

	public void setContinua(boolean continua) {
		this.continua = continua;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.dto.SimDto#getHash()
	 */
	public String getHash() {
		return bacia.getId() + "#" + grade.getId() + "#" + dataInicial + "#" + dataFinal + "#" + usuario.getId() + "#" + unidade.getId();
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#list(com.ufpe.redeclima.model.PontoGrade, com.ufpe.redeclima.interfaces.SimDto)
	 */
	public List<JSONObject> list(PontoDado pontoGrade, SimDto simDto) {
		return dadoPontoGradeDao.list((PontoGrade) pontoGrade, simDto);
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarArquivosTemporais(com.ufpe.redeclima.interfaces.SimDto)
	 */
	public void salvarArquivosTemporais() {

		Simulacao simulacao = simulacaoDao.findByAttributes(this);
		
		workspace.setUsuario(usuario);
		StringBuilder sb = new StringBuilder();
		sb.append(workspace.getHMSLog(bacia));
		sb.append(System.getProperty("line.separator"));
		sb.append(workspace.getRASLog(bacia));
		
		if(simulacao==null){
			simulacao = new Simulacao();
			simulacao.setBacia(bacia);
			simulacao.setGrade(grade);
			simulacao.setUsuario(usuario);
			simulacao.setDataInicio(dataInicial);
			simulacao.setDataFim(dataFinal);
			simulacao.setLog(sb.toString());
			simulacaoDao.save(simulacao);
		}else {
			simulacao.setLog(sb.toString());
			simulacaoDao.saveOrUpdate(simulacao);
		}
		
		resultadoRasDao.salvarArquivosTemporais(simulacao);
		
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.SimDto#salvarResultados()
	 */
	public void salvarResultados() {

		Simulacao simulacao = simulacaoDao.findByAttributes(this);
		
		//Remover resultados anteriores da simulação
		resultadoRasDao.removerResultados(simulacao);
		//Adicionar novos resultados
		resultadoRasDao.salvarResultados(simulacao);
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
