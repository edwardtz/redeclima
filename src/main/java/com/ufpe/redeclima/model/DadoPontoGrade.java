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
package com.ufpe.redeclima.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Esta classe representa um valor de previsão de chuva registrado para um ponto da grade em uma determinada data
 * */
@Entity
@Table(name="t_dado_ponto_grade")
public class DadoPontoGrade implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="dataPrevisao", nullable=false)
	private Date dataPrevisao;
	
	@Id
	@ManyToOne (cascade=CascadeType.DETACH, fetch=FetchType.EAGER)
	@JoinColumn(name="ponto_grade_id", nullable=false)
	private PontoGrade pontoGrade;
	
	@Column(name="chuva", nullable=false)
	private Double chuva;
	
	public Date getDataPrevisao() {
		return dataPrevisao;
	}

	public void setDataPrevisao(Date dataPrevisao) {
		this.dataPrevisao = dataPrevisao;
	}

	public Double getChuva() {
		return chuva;
	}

	public void setChuva(Double chuva) {
		this.chuva = chuva;
	}

	public PontoGrade getPontoGrade() {
		return pontoGrade;
	}

	public void setPontoGrade(PontoGrade pontoGrade) {
		this.pontoGrade = pontoGrade;
	}
}
