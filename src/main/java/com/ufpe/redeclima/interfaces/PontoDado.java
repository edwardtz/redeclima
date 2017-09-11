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
package com.ufpe.redeclima.interfaces;

/**
 * @author edwardtz
 *
 */
public interface PontoDado {

	/**
	 * Retorna a coordenada latitude do ponto de dados
	 * */
	public Double getLatitude();
	
	/**
	 * Retorna a coordenada de longitude do ponto de dados
	 * */
	public Double getLongitude();
	
	
	/**
	 * Retorna a coordenada de altitude do ponto de dados
	 * */
	public Double getAltitude();
	
	/**
	 * Retorna o identificador do ponto em relação ao arquivo de configuração gage
	 * */
	public String getGageId();
	
	/**
	 * Retorna o indice de identificação do objeto na BD
	 * */
	public Long getId();
	
}
