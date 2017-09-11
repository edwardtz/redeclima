package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaciaDaoTest {

	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	private Bacia bacia;
	
	private Grade grade;
	
	
	@Before
	public void Init(){
		bacia = new Bacia();
		bacia.setNome("BaciaUna");
		bacia.setLongitudeX1(-1D);
		bacia.setLongitudeX2(-1D);
		bacia.setLatitudeY1(-1D);
		bacia.setLatitudeY2(-1D);
		baciaDao.save(bacia);
		
		List<Grade> listaGrades = gradeDao.list();
		
		if (listaGrades == null || listaGrades.size() ==0){
			grade = new Grade();
			grade.setLatitudeInicial(-1D);
			grade.setLongitudeInicial(-1D);
			grade.setNome("rec_5L_eta_15km");
			grade.setPasso(0.15);
			grade.setRemotePathFtp("/remote");
			grade.setTipoGrade(EnumTipoGrade.KM15.getId());
			grade.setQuantidadeTempoPeriodo(3);
			grade.setUnidadeTempoPeriodo(EnumUnidadeTempo.HORA.getId());
			gradeDao.save(grade);
		}else{
			grade = listaGrades.get(0);
		}
		
		PontoGrade pontoGrade = new PontoGrade();
		pontoGrade.setLatitude(-8.35);
		pontoGrade.setLongitude(-36.499258);
		pontoGrade.setGrade(grade);
		pontoGradeDao.save(pontoGrade);
		
		pontoGrade = new PontoGrade();
		pontoGrade.setLatitude(-8.35);
		pontoGrade.setLongitude(-36.349256);
		pontoGrade.setGrade(grade);
		pontoGradeDao.save(pontoGrade);
		
		
		
	}
	
	
	@Test
	public void test() {
		
	}

}
