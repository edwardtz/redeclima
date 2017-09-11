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

import java.util.List;

import javax.faces.bean.RequestScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.AreaRecorteDao;
import com.ufpe.redeclima.model.AreaRecorte;

/**
 * @author edwardtz
 *
 */
@Controller
@RequestScoped
public class AreaRecorteController {

	private static final Logger logger = LoggerFactory.getLogger(AreaRecorteController.class);
	
	private AreaRecorte areaRecorte;
	
	private List<AreaRecorte> areasRecorte;
	
	@Autowired
	private AreaRecorteDao areaRecorteDao;
	
	public AreaRecorteController(){
		areaRecorte = new AreaRecorte();
	}
	
	public String save() {
		areaRecorteDao.saveOrUpdate(areaRecorte);
		areaRecorte = new AreaRecorte();
		invalidateAreasRecorte();
		return "listaAreasRecorte";
	}
	
	public String adicionarAreaRecorte(){
		areaRecorte = new AreaRecorte();
		return "areaRecorteForm";
	}
	
	public void excluirAreaRecorte(){
		//TODO checar que grade não tenha pontdos de grade asociados
		//TODO checar que os pontos de grade ão tenham asociados dados
		//TODO checar que a grade não tenha pontos assoaciados a uma bacia
		areaRecorteDao.remove(areaRecorte);
		invalidateAreasRecorte();
	}
	
	private void invalidateAreasRecorte(){
		areasRecorte = null;
	}

	public AreaRecorte getAreaRecorte() {
		return areaRecorte;
	}

	public void setAreaRecorte(AreaRecorte areaRecorte) {
		this.areaRecorte = areaRecorte;
	}

	public List<AreaRecorte> getAreasRecorte() {
		this.areasRecorte = areaRecorteDao.list();
		return areasRecorte;
	}

	public void setAreasRecorte(List<AreaRecorte> areasRecorte) {
		this.areasRecorte = areasRecorte;
	}

	
}
