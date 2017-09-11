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

/**
 * @author edwardtz
 *
 */
public class SecaoDto implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String nomeRio;
	
	private String nomeTrecho;
	
	private String nomeSecao;
	
	private double latitudeCentro;
	
	private double longitudeCentro;
	
	private String descricaoHtml;

	public String getNomeRio() {
		return nomeRio;
	}

	public void setNomeRio(String nomeRio) {
		this.nomeRio = nomeRio;
	}

	public String getNomeTrecho() {
		return nomeTrecho;
	}

	public void setNomeTrecho(String nomeTrecho) {
		this.nomeTrecho = nomeTrecho;
	}

	public String getNomeSecao() {
		return nomeSecao;
	}

	public void setNomeSecao(String nomeSecao) {
		this.nomeSecao = nomeSecao;
	}

	public double getLatitudeCentro() {
		return latitudeCentro;
	}

	public void setLatitudeCentro(double latitudeCentro) {
		this.latitudeCentro = latitudeCentro;
	}

	public double getLongitudeCentro() {
		return longitudeCentro;
	}

	public void setLongitudeCentro(double longitudeCentro) {
		this.longitudeCentro = longitudeCentro;
	}

	public String getDescricaoHtml() {
		return descricaoHtml;
	}

	public void setDescricaoHtml(String descricaoHtml) {
		this.descricaoHtml = descricaoHtml;
	}
	
}
