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
 * Define os códigos de sensores para leitura dos arquivos de estações vi FTP
 * Os canais 9 e 13 são usados para dados com periodicidade de 15 minutos de chuva e nível por radar, respectivamente.
 * Os canais 10 e 14 são utilizados para os mesmos sensores porém com periodicidade de 60 minutos (utilizados apenas para transmissão).
 * Nos programas mais recentes foram adotados a discretização de 15 minutos para todas as PCDs, e por isso não está mais sendo utilizado os canais 10 e 14.
 *
 * O canal 01 refere-se ao dado de nível coletado por transdutor de pressão (mesmo dado que é coletado pelo sensor radar no canal 13) com periodicidade de 15 minutos.
 * Pode haver estações apenas com radar, apenas com transdutor de pressão ou com ambos sensores de nível. Quando o sensor não está conectado, ou está com problema, aparece o código [5] no lugar da medida.
 */
public enum EnumCodigoSensorEstacao {

	CHUVA_60_MIN(1,"0010","Chuva transmissão 60 minutos"),
	CHUVA_RADAR(2,"0009","Chuva Radar 15 minutos"),
	NIVEL_60_MIN(3,"0014","Nivel transmissão 60 minutos"),
	NIVEL_RADAR(4,"0013","Nivel Radar 15 minutos"),
	NIVEL_TRANSDUTOR(5,"0001","Nivel Transdutor 15 minutos");
	
	private int id;
	
	private String code;
	
	private String descripcao;
	
	EnumCodigoSensorEstacao(int id, String code, String descripcao){
		this.id=id;
		this.code=code;
		this.descripcao=descripcao;
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

	public String getDescripcao() {
		return descripcao;
	}

	public void setDescripcao(String descripcao) {
		this.descripcao = descripcao;
	}
	
	public static EnumCodigoSensorEstacao getById(int id){
		return values()[id];
	}
	
}
