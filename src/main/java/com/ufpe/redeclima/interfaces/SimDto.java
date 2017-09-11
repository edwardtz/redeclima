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

import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.batch.EstadoSimulacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
public interface SimDto {

	/**
	 * Retorna o valor do hash que identifica o dto de entrada
	 * */
	public String getHash();
	
	/**
	 * Retorna a bacia da simulação
	 * */
	public Bacia getBacia();
	
	/**
	 * Configura a bacia da simulação
	 * */
	public void setBacia(Bacia bacia);
	
	/**
	 * Retorna a grade da simulação
	 * */
	public Grade getGrade();
	
	/**
	 * Configura a grade da simulação
	 * */
	public void setGrade(Grade grade);
	
	
	/**
	 * Retorna a data inicial da simulação
	 * */
	public Date getDataInicial();
	
	/**
	 * Configura a data inicial da simulação
	 * */
	public void setDataInicial(Date dataInicial);
	
	
	/**
	 * Retorna a data final da simulação
	 * */
	public Date getDataFinal();
	
	/**
	 * Configura a data final da simulação
	 * */
	public void setDataFinal(Date dataFinal);
	
	/**
	 * Retorna a unidade de tempo da simulação
	 * */
	public EnumUnidadeTempo getUnidade();
	
	/**
	 * Configura a unidade de tempo da simulação
	 * */
	public void setUnidade(EnumUnidadeTempo unidade);
	
	/**
	 * Retorna o usuario que esta rodando a simulação
	 * */
	public Usuario getUsuario();
	
	/**
	 * Configura o usuario que esta rodando a simulação
	 * */
	public void setUsuario(Usuario usuario);
	
	/**
	 * Retorna os dados de entrada do modelo HMS
	 * */
	public List<JSONObject> list(PontoDado pontoGrade, SimDto simDto);
	
	/**
	 * Cria um conjunto de arquivos previos para salvar os resultados na BD
	 * */
	public void salvarArquivosTemporais();
	
	/**
	 * Salva os resultados RAS na BD
	 * */
	public void salvarResultados();
	
	
	/**
	 * Retorna o estado atual da simulação
	 * */
	public EstadoSimulacao getEstadoSimulacao();
	
	/**
	 * Configura o estado atual da simulação
	 * */
	public void setEstadoSimulacao(EstadoSimulacao estadoSimulacao);
	
	
	/**
	 * Retorna os pontos de dados usados como entrada na simulação
	 * */
	public List<PontoDado> getPontosDados();
	
}
