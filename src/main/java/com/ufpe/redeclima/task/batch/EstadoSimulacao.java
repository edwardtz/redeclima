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

import java.io.Serializable;

import com.ufpe.redeclima.util.EnumEstadosSimulacao;

/**
 * @author edwardtz
 *
 */
public class EstadoSimulacao implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private double progress;
	
	private EnumEstadosSimulacao estado;
	
	public EstadoSimulacao(){
		progress = 0;
		estado = EnumEstadosSimulacao.STEP1;
	}
	
	public EnumEstadosSimulacao transProximoEstado(){
		
		int indiceEstado = estado.getId() + 1;
		
		if (indiceEstado < EnumEstadosSimulacao.quantidadeEstados()){
			this.estado = EnumEstadosSimulacao.getById(indiceEstado);
			this.progress = progress + 14;
		} else {
			this.progress = 100;
			
		}
		
		return estado;
		
	} 
	
	public EnumEstadosSimulacao finalizar(){
		
		progress = 100;
		estado = EnumEstadosSimulacao.STEP7;
		return estado;
		
	} 
	 
	
	public double getProgress() {
		return progress;
	}

	public void setProgress(double progress) {
		this.progress = progress;
	}

    public EnumEstadosSimulacao getEstado() {
		return estado;
	}

	public void setEstado(EnumEstadosSimulacao estado) {
		this.estado = estado;
	}
	
}
