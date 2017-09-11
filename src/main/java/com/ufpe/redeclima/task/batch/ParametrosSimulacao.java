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
package com.ufpe.redeclima.task.batch;

import org.springframework.stereotype.Component;

/**
 * @author edwardtz
 * Contem os parâmetros de simulação tais como timeouts, variaveis globais, etc
 */
@Component
public class ParametrosSimulacao {

	/* Timeout correspondente ao tempo maximo de execução do modelo RAS */
	private int rasTimeOut = 100000;
	
	/* Timeout correspondente ao tempo maximo de execução do modelos HMS */
	private int hmsTimeOut = 50000;
	
	public int getRasTimeOut() {
		return rasTimeOut;
	}

	public void setRasTimeOut(int rasTimeOut) {
		this.rasTimeOut = rasTimeOut;
	}

	public int getHmsTimeOut() {
		return hmsTimeOut;
	}

	public void setHmsTimeOut(int hmsTimeOut) {
		this.hmsTimeOut = hmsTimeOut;
	}
	
	
	
	
}
