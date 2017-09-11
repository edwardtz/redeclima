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

import javax.faces.bean.RequestScoped;

import org.primefaces.model.DualListModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.AreaRecorteDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.model.AreaRecorte;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */

@Controller
@RequestScoped
public class GradeController implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GradeController.class);
	
	private Grade grade;
	
	private List<Grade> grades;
	
	private DualListModel<AreaRecorte> areasRecorte;
	
	private List<AreaRecorte> areasDisponiveis;
	
	private List<AreaRecorte> areasAsociadas;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private AreaRecorteDao areaRecorteDao;
	
	public GradeController(){
		grade = new Grade();
		areasDisponiveis = new ArrayList<AreaRecorte>();
		areasAsociadas = new ArrayList<AreaRecorte>();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		areasDisponiveis = areaRecorteDao.list();
		
		areasAsociadas = grade.getAreasRecorte();
		
		if (areasAsociadas != null){
			areasDisponiveis.removeAll(areasAsociadas);
		}else{
			areasAsociadas = new ArrayList<AreaRecorte>();
			grade.setAreasRecorte(areasAsociadas);
		}
		
		areasRecorte = new DualListModel<AreaRecorte>(areasDisponiveis,areasAsociadas);
	}
	
	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}
	
	public String save() {
		areasAsociadas = areasRecorte.getTarget();
		grade.setAreasRecorte(areasAsociadas);
		gradeDao.saveOrUpdate(grade);
		grade = new Grade();
		grade.setBuscaAtiva(true);
		invalidateGrades();
		return "listaGrades";
	}
	
	public String adicionarGrade(){
		grade = new Grade();
		grade.setBuscaAtiva(true);
		return "gradeFormView";
	}
	
	public void excluirGrade(){
		//TODO checar que grade não tenha pontdos de grade asociados
		//TODO checar que os pontos de grade ão tenham asociados dados
		//TODO checar que a grade não tenha pontos assoaciados a uma bacia
		gradeDao.remove(grade);
		invalidateGrades();
	}
	
	/* Retorna os tipos de grades disponivels */
	public EnumTipoGrade[] getTiposGrade(){
		return EnumTipoGrade.values();
	}

	/* Retorna as unidades de tempo para as grades */
	public EnumUnidadeTempo[] getUnidadesTempo(){
		return EnumUnidadeTempo.values();
	}
	
	private void invalidateGrades() {
		grades = null;
	}
	
	public List<Grade> getGrades() {
		if (grades == null) {
			grades = gradeDao.list();
		}
		return grades;
	}

	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}

	public DualListModel<AreaRecorte> getAreasRecorte() {
		
		
		areasDisponiveis = areaRecorteDao.list();
		
		areasAsociadas = grade.getAreasRecorte();
		
		if (areasAsociadas != null){
			areasDisponiveis.removeAll(areasAsociadas);
		}else{
			areasAsociadas = new ArrayList<AreaRecorte>();
			grade.setAreasRecorte(areasAsociadas);
		}
		
		areasRecorte = new DualListModel<AreaRecorte>(areasDisponiveis,areasAsociadas);
		
		return areasRecorte;
	}

	public void setAreasRecorte(DualListModel<AreaRecorte> areasRecorte) {
		this.areasRecorte = areasRecorte;
	}

	public List<AreaRecorte> getAreasDisponiveis() {
		return areasDisponiveis;
	}

	public void setAreasDisponiveis(List<AreaRecorte> areasDisponiveis) {
		this.areasDisponiveis = areasDisponiveis;
	}

	public List<AreaRecorte> getAreasAsociadas() {
		return areasAsociadas;
	}

	public void setAreasAsociadas(List<AreaRecorte> areasAsociadas) {
		this.areasAsociadas = areasAsociadas;
	}

	
	
}
