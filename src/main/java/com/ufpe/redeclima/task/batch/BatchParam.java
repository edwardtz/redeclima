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

import java.util.HashMap;

import org.springframework.stereotype.Component;

import com.ufpe.redeclima.interfaces.SimDto;

/**
 * @author edwardtz
 * Esta clase funciona como um collaborator nos processos de execução batch
 */
@Component
public class BatchParam {

	private HashMap<String, SimDto> params = new HashMap<String, SimDto>();

	/**
	 * Adiciona um parametro de simulação
	 * */
	public void add(SimDto simDto){
		if(simDto != null){
			params.put(simDto.getHash(), simDto);
		}
	}
	
	/**
	 * Elimina um parametro de simulação
	 * */
	public void remove(SimDto simDto){
		if (simDto != null){
			params.remove(simDto.getHash());
		}
	}
	
	/**
	 * Retorna o dto baseado na key pasada como parametro
	 * */
	public SimDto getParam(String key){
		return params.get(key);
	}
	
	public HashMap<String, SimDto> getParams() {
		return params;
	}

	public void setParams(HashMap<String, SimDto> params) {
		this.params = params;
	}
	
	
}
