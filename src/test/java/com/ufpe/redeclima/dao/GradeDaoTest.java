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
package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.model.AreaRecorte;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class GradeDaoTest {

	@Autowired
	private AreaRecorteDao areaRecorteDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	private AreaRecorte areaRecorte;
	
	private Grade novaGrade;
	
	@Before
	public void init(){
		areaRecorte = new AreaRecorte();
		areaRecorte.setNome("UmaArea");
		areaRecorte.setLatitudeY1(-1D);
		areaRecorte.setLatitudeY2(-2D);
		areaRecorte.setLongitudeX1(-3D);
		areaRecorte.setLongitudeX2(-4D);
		areaRecorteDao.save(areaRecorte);
		
	}
	
	/**
	 * Test method for {@link com.ufpe.redeclima.dao.GradeDao#saveOrUpdate(com.ufpe.redeclima.model.Grade)}.
	 */
	@Test
	public void testSaveOrUpdate() {
		
		
		novaGrade = new Grade();
		novaGrade.setBuscaAtiva(true);
		novaGrade.setLatitudeInicial(-4D);
		novaGrade.setLongitudeInicial(-3D);
		novaGrade.setNome("UmaGrade");
		novaGrade.setPasso(0.03);
		novaGrade.setRemotePathFtp("/hyf");
		novaGrade.setTipoGrade(EnumTipoGrade.KM10.getId());
		novaGrade.setQuantidadeTempoPeriodo(3);
		novaGrade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
		
		List<AreaRecorte> lista = new ArrayList<AreaRecorte>();
		lista.add(areaRecorte);
		novaGrade.setAreasRecorte(lista);
		gradeDao.save(novaGrade);
		
		assertNotNull(novaGrade);
		assertNotNull(novaGrade.getId());
		
		Grade buscada = gradeDao.findByNome("UmaGrade");
		
		assertNotNull(buscada);
		
		AreaRecorte areaRecorte2 = new AreaRecorte();
		areaRecorte2.setNome("UmaArea2");
		areaRecorte2.setLatitudeY1(-12D);
		areaRecorte2.setLatitudeY2(-22D);
		areaRecorte2.setLongitudeX1(-32D);
		areaRecorte2.setLongitudeX2(-42D);
		areaRecorteDao.save(areaRecorte2);
		
		lista = new ArrayList<AreaRecorte>();
		lista.add(areaRecorte2);
		
		novaGrade.setAreasRecorte(lista);
		gradeDao.saveOrUpdate(novaGrade);
		buscada = gradeDao.findByNome("UmaGrade");
		List<AreaRecorte> areas = buscada.getAreasRecorte();
		assertNotNull(buscada);
		
	}

}
