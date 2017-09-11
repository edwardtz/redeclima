package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.DadoPontoGrade;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class DadoPontoGradeDaoTest {

	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private DadoPontoGradeDao dadosPontoGradeDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	private Grade grade;
	
	@Before
	public void init(){
		
		grade = gradeDao.findByNome("rec_5L_eta_15km");
		
		if (grade==null){
			grade = new Grade();
			grade.setLatitudeInicial(-1.0);
			grade.setLongitudeInicial(-1.0);
			grade.setPasso(0.15);
			grade.setTipoGrade(EnumTipoGrade.KM15.getId());
			grade.setNome("rec_5L_eta_15km");
			grade.setRemotePathFtp("/modelos/io/tempo/regional/Eta15km/rec_grib");
			grade.setQuantidadeTempoPeriodo(3);
			grade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
			gradeDao.save(grade);
		}
	}
	
	@Ignore
	public void testAtualizarPontoGrade() {
		String nomeArquivo = "rec_5L_eta_15km2013053000+2013053000.grb";
		pontoGradeDao.atualizarPontoGrade(nomeArquivo, grade);
		assertNotNull(pontoGradeDao.list());
	}
	
	@Test
	public void testAtualizarDadosPontoGrade(){
		
		String nomeArquivo = "rec_5L_eta_15km2013053000+2013053000.grb";
		
		pontoGradeDao.atualizarPontoGrade(nomeArquivo, grade);
		
		List<PontoGrade> listadepontos = pontoGradeDao.list();
		
		PontoGrade p = pontoGradeDao.findByCoordGrade(-39.94931411743164, -12.85000228881836, 1);
		
		dadosPontoGradeDao.atualizarDadosPontoGrade(nomeArquivo, grade);
		
		
		
		List<DadoPontoGrade> listadedadosdepontos = dadosPontoGradeDao.list();
		
		assertNotNull(dadosPontoGradeDao.list());
		
	}
	
	@Test
	public void doTest(){
		assertTrue(true);
	}


}
