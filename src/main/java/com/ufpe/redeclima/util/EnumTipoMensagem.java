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
package com.ufpe.redeclima.util;

/**
 * @author edwardtz
 *
 */
public enum EnumTipoMensagem {

	ALERTAOBSCHUVA(1,"ALERTAOBSCHUVA", "Alerta de valores observados de chuva"),
	ALERTAOBSNIVEL(2,"ALERTAOBSNIVEL", "Alerta de valores observados de nivel"),
	ALERTAPREVCHUVA(3,"ALERTAPREVCHUVA","Alerta de valores de previsão de chuva"),
	ALERTASIMOBS(4,"ALERTASIMOBS","Alerta de valores de simulação baseado em dados de observação"),
	ALERTASIMPREV(5,"ALERTASIMPREV","Alerta de valores de simulação baseado em dados de previsão");
	
	private long id;
	
	private String codigo;
	
	private String descripcao;
	
	EnumTipoMensagem(long id, String codigo, String descripcao){
		this.id=id;
		this.codigo=codigo;
		this.descripcao=descripcao;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcao() {
		return descripcao;
	}

	public void setDescripcao(String descripcao) {
		this.descripcao = descripcao;
	}
	
	public static EnumTipoMensagem getById(int id){
		return values()[id];
	}
}
