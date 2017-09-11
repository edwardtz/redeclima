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
package com.ufpe.redeclima.controller;


import java.util.ArrayList;
import java.util.List;

import javax.faces.model.SelectItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.TipoEstacaoDao;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.TipoEstacao;
import com.ufpe.redeclima.util.EnumEstadoEstacao;
import com.ufpe.redeclima.util.EnumEstadosBrasil;
import com.ufpe.redeclima.util.EnumOperador;
import com.ufpe.redeclima.util.EnumTipoResponsavel;
import com.ufpe.redeclima.util.EnumTipoTransmissao;

@Controller
@Scope("session")
public class EstacaoController {
	
	private static final Logger logger = LoggerFactory.getLogger(EstacaoController.class);
	
	private Estacao estacao;
	
	private List<Estacao> estacoes;
	
	private List<TipoEstacao> tiposSelected;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private TipoEstacaoDao tipoEstacaoDao;
	
	public EstacaoController(){
		estacao = new Estacao();
	}
	
	public String save() {
		estacao.setTipos(tiposSelected);
		estacaoDao.saveOrUpdate(estacao);
		estacao = new Estacao();
		invalidateEstacoes();
		return "listaEstacoes";
	}
	
	public String adicionarEstacao(){
		estacao = new Estacao();
		return "estacaoFormView";
	}
	
	public void excluirEstacao(){
		estacaoDao.remove(estacao);
		invalidateEstacoes();
	}
	
	private void invalidateEstacoes() {
		estacoes = null;
	}

	public List<Estacao> getEstacoes() {
		if (estacoes == null) {
			estacoes = estacaoDao.list();
		}
		return estacoes;
	}
	
	/* Este metodo retorna os tipos de estação disponiveis  */
	public List<TipoEstacao> getTiposEstacao(){
		return tipoEstacaoDao.list();
	}
	
	public SelectItem[] getEstadosDesc()  {  
        SelectItem[] options = new SelectItem[EnumEstadoEstacao.values().length + 1];  
        options[0] = new SelectItem("", ""); 
        int i=0;
        for(EnumEstadoEstacao estado: EnumEstadoEstacao.values()){
        	options[i + 1] = new SelectItem(estado, estado.getDescripcao()); 
        	i++;
        }
        return options;  
    }  
	
	public EnumTipoResponsavel[] getResponsaveis(){
		return EnumTipoResponsavel.values();
	}
	
	/* Este metodo retorna os operadores  */
	public EnumOperador[] getOperadores(){
		return EnumOperador.values();
	}
	
	public EnumEstadosBrasil[] getEstados(){
		return EnumEstadosBrasil.values();
	}

	/* Este metodo retorna os tipose de transmissao  */
	public EnumTipoTransmissao[] getTiposTransmissao(){
		return EnumTipoTransmissao.values();
	}
	
	public Estacao getEstacao() {
		return estacao;
	}

	public void setEstacao(Estacao estacao) {
		this.estacao = estacao;
	}

	public void setEstacoes(List<Estacao> estacoes) {
		this.estacoes = estacoes;
	}
	
	public List<Bacia> getBacias(){
		return baciaDao.list();
	}
	
	public List<TipoEstacao> getTiposSelected() {
		tiposSelected = new ArrayList<TipoEstacao>();
		if (estacao.getId() != null){
			tiposSelected.addAll(estacao.getTipos());
		}
		return tiposSelected;
	}

	public void setTiposSelected(List<TipoEstacao> tiposSelected) {
		this.tiposSelected = tiposSelected;
	}
	
	
}
