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
 * Define os posiveis tipos de grade para os pontos de previssão, generalmente definida pela distancia da variavel de passo, a variavel de passo define a distancia
 * de separação entre os pontos da grade
 * */
public enum EnumTipoGrade {

		KM5(1,"5KM","Tipo grade de 5KM"),
		KM10(2,"10KM","Tipo grade de 10KM"),
		KM15(3,"15KM","Tipo grade de 15KM");
		
		private int id;
		
		private String code;
		
		private String descricao;
		
		EnumTipoGrade(int id, String code, String descricao){
			this.id=id;
			this.code=code;
			this.descricao=descricao;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getdescricao() {
			return descricao;
		}

		public void setdescricao(String descricao) {
			this.descricao = descricao;
		}
		
		public static EnumTipoGrade getById(int id){
			return values()[id];
		}
}
