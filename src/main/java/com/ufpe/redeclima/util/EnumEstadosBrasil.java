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

public enum EnumEstadosBrasil {

	AC(1,"AC","Acre"),
	AL(2,"AL", "Alagoas"),
	AP(3,"AP","Amapá"),
	AM(4,"AM","Amazonas"),
	BA(5,"BA","Bahia"),
	CE(6,"CE","Ceará"),
	DF(7,"DF","Distrito Federal"),
	ES(8,"ES","Espírito Santo"),
	GO(9,"GO","Goiás"),
	MA(10,"MA","Maranhão"),
	MT(11,"MT","Mato Grosso"),
	MS(12,"MS","Mato Grosso do Sul"),
	MG(13,"MG","Minas Gerais"),
	PA(14,"PA","Pará"),
	PB(15,"PB","Paraíba"),
	PR(16,"PR","Paraná"),
	PE(17,"PE","Pernambuco"),
	PI(18,"PI","Piauí"),
	RJ(19,"RJ","Rio de Janeiro"),
	RN(20,"RN","Rio Grande do Norte"),
	RS(21,"RS","Rio Grande do Sul"),
	RO(22,"RO","Rondônia"),
	RR(23,"RR","Roraima"),
	SC(24,"SC","Santa Catarina"),
	SP(25,"SP","São Paulo"),
	SE(26,"SE","Sergipe"),
	TO(27,"TO","Tocantins");

	private int id;
	
	private String uf;
	
	private String descricao;
	
	EnumEstadosBrasil(int id, String uf, String descricao){
		this.id=id;
		this.uf=uf;
		this.descricao=descricao;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUf() {
		return uf;
	}

	public void setUf(String uf) {
		this.uf = uf;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
	
	public static EnumEstadosBrasil getById(int id){
		return values()[id];
	}
	
}
