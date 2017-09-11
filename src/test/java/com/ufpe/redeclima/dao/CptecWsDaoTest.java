package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class CptecWsDaoTest {

	@Autowired
	private CptecWsDao cptecWsDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	private Grade grade;
	
	
	@Before
	public void init(){
		grade = new Grade();
		grade.setBuscaAtiva(true);
		grade.setLatitudeInicial(-1D);
		grade.setLongitudeInicial(-1D);
		grade.setNome("rec_5L_eta_15km");
		grade.setPasso(-0.15);
		grade.setTipoGrade(EnumTipoGrade.KM15.getId());
		grade.setRemotePathFtp("/modelos/io/tempo/regional/Eta15km/rec_grib");
		grade.setQuantidadeTempoPeriodo(3);
		grade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
		gradeDao.save(grade);
		
	}
	
	@Test
	public void testDownloadDadosPrevisao() {
		cptecWsDao.downloadDadosPrevisao(grade, "2013111312");
	}
	
}
