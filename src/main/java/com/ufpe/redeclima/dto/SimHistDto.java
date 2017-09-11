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

import java.util.Date;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.Simulacao;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
public class SimHistDto {
	
	private Long id;
	
	private Date dataInicial;
	
	private Date dataFinal;

	private Usuario usuario;
	
	private Bacia bacia;
	
	private EnumUnidadeTempo enumUnidadeTempo;
	
	private Grade grade;
	
	public SimHistDto(Simulacao sim){
		id = sim.getId();
		dataInicial = sim.getDataInicio();
		dataFinal = sim.getDataFim();
		usuario = sim.getUsuario();
		bacia = sim.getBacia();
		grade = sim.getGrade();
	}
	
	public SimHistDto(SimulacaoObs sim){
		id = sim.getId();
		dataInicial = sim.getDataInicio();
		dataFinal = sim.getDataFim();
		usuario = sim.getUsuario();
		bacia = sim.getBacia();
		grade = new Grade();
		grade.setNome("Grade de Estações");
		grade.setQuantidadeTempoPeriodo(1);
		grade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public EnumUnidadeTempo getEnumUnidadeTempo() {
		return enumUnidadeTempo;
	}

	public void setEnumUnidadeTempo(EnumUnidadeTempo enumUnidadeTempo) {
		this.enumUnidadeTempo = enumUnidadeTempo;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}
	
}
