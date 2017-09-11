package com.ufpe.redeclima.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.RioDao;
import com.ufpe.redeclima.dao.SecaoDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.dto.SimulacaoDto;
import com.ufpe.redeclima.exception.RedeclimaException;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.DadoPontoGrade;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.util.EnumTipoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SimulacaoHMSTaskTest {

	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	/* Diretorio do executavel do dssts */
	@Value("${parameter.dss_executable_path}")
	private String executablePath;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private ExecutarSimulacaoTask simulacaoHmsTask;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private DadoPontoGradeDao dadoPontoGradeDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private InicializacaoDadosTask inicializacaoDadosTask;
	
	private Grade grade;
	
	private Bacia bacia;
	
	@Before
	public void init(){
		
        inicializacaoDadosTask.run();
        
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
		
		List<PontoGrade> pontos =  new ArrayList<PontoGrade>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		PontoGrade pontoGrade = new PontoGrade();
		pontoGrade.setLatitude(-8.35);
		pontoGrade.setLongitude(-36.499258);
		pontoGrade.setGrade(grade);
		if (pontoGradeDao.findByCoordGrade(pontoGrade)==null){
			pontoGradeDao.save(pontoGrade);
		}
		pontos.add(pontoGrade);
		
		
		pontoGrade = new PontoGrade();
		pontoGrade.setLatitude(-8.35);
		pontoGrade.setLongitude(-36.349256);
		pontoGrade.setGrade(grade);
		if (pontoGradeDao.findByCoordGrade(pontoGrade)==null){
			pontoGradeDao.save(pontoGrade);
		}
		pontos.add(pontoGrade);
		
		bacia = baciaDao.findByNome("Mundau");
		
		pontos = pontoGradeDao.list();
		
		for(PontoGrade p: pontos){
			DadoPontoGrade dado = new DadoPontoGrade();
			dado.setPontoGrade(p);
			dado.setDataPrevisao(calendar.getTime());
			dado.setChuva(3.0D+p.getId());
			dadoPontoGradeDao.save(dado);
		}
	}
	
	@Ignore
	public void testGenerateDss() {
		List<Bacia> bacias = baciaDao.list();
		Bacia bacia = bacias.get(0);
		SimulacaoDto dto = new SimulacaoDto();
		dto.setBacia(bacia);
		dto.setGrade(grade);
		Calendar calendario = Calendar.getInstance();
		dto.setDataInicial(calendario.getTime());
		calendario.add(Calendar.DAY_OF_MONTH, 3);
		dto.setDataFinal(calendario.getTime());
		dto.setUnidade(EnumUnidadeTempo.DIA);
		dto.setContinua(true);
		simulacaoHmsTask.run(dto);
		Assert.assertNotNull(bacias);
	}
	
	@Ignore
	public void exportarResultadosRasTest(){
		
		String scriptExportacaoRas = Paths.get(pathModelos, "UNA", "HEC", "RAS", "Una" + "SimulaEventosDssutl.txt").toString();
		String commandShell = "cmd /c start /WAIT " + Paths.get(executablePath, "DSSUTL.EXE").toString() + " INPUT=" + scriptExportacaoRas;
		
		Process process=null;
		try {
			process = Runtime.getRuntime().exec(commandShell);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		int exitVal=0;
		try {
			exitVal = process.waitFor();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		if (exitVal!=0){
			
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));

		    // read the output from the command
		    String saidaTexto=null;
		    String s=null;
		    try {
				while ((s = stdInput.readLine()) != null) {
				     saidaTexto = saidaTexto + s;
				     System.out.println(s);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    // read any errors from the attempted command
		    System.out.println("Este é o error standard do comando (se existe):\n");
		    s=null;
		    try {
				while ((s = stdError.readLine()) != null) {
					saidaTexto = saidaTexto + s;
					System.out.println(s);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			throw new RedeclimaException("Error na execução do RAS código de saida " + exitVal + saidaTexto);
		}
	}
	
	@Test
	public void testBatch(){
		SimulacaoDto dto = new SimulacaoDto();
		dto.setDataInicial(new Date());
		dto.setDataFinal(new Date());
		dto.setBacia(bacia);
		dto.setGrade(grade);
		simulacaoHmsTask.run(dto);
	}
	
}
