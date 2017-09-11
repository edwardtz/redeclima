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
 * This enumeration contain the descriptions of properties parameters.properties
 * @author eduardo
 *
 */
 
/**
 * Este enum define os nomes de parâmetros de configuração do sistema
 * */
public enum EnumParameters {
	
	PATHDSS (1,"parameter.path_modelos"),
	PATHRAS (2,"parameter.path_ras"),
	DSS_EXECUTABLE_PATH(3,"parameter.dss_executable_path"),
	OTHERPARAM (3,"other_param");
	
	private final int code;
	
	private final String property_name;
	
	private EnumParameters(int code, String property_name) {
		this.code = code;
		this.property_name = property_name;
	}

	public int getCode() {
		return code;
	}

	public String getProperty_name() {
		return property_name;
	}
	
	public static EnumParameters getById(int id){
		return values()[id];
	}
	
}
