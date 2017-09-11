package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15Min;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15MinId;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.util.EnumTipoResponsavel;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class DadosEstacaoTelemetricaDaoTest {

	@Autowired
	private ANAWSDao anaWsDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private BaciaDao baciaDao;
	
	private Estacao estacao;
	
	private Bacia bacia;
	
	@Before
	public void Init(){
		
		bacia = new Bacia();
		bacia.setNome("Bacia de Teste");
		bacia.setLongitudeX1(-39D);
		bacia.setLongitudeX2(-35D);
		bacia.setLatitudeY1(-9D);
		bacia.setLatitudeY2(-7D);
		baciaDao.save(bacia);
		
		estacao =  new Estacao();
		estacao.setNome("EstacaoTest");
		estacao.setCodigo(39571000);
		estacao.setBacia(bacia);
		estacao.setResponsavel(EnumTipoResponsavel.ANA);
		estacaoDao.save(estacao);
	}
	
	@Test
	public void testUpdateDadosEstacao() {
		
		Calendar calendar = Calendar.getInstance();
		
		Date dataFim = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, -5);
		Date dataInicio = calendar.getTime();
		
		List<Estacao> estacoesEmEstudo = estacaoDao.listEstacoesANA(bacia);
		
		assertNotNull(estacoesEmEstudo);
		
		assertFalse(estacoesEmEstudo.isEmpty());
		
		Document document = anaWsDao.getDataEstacao(estacoesEmEstudo.get(0).getCodigo(), dataInicio, dataFim);
		
		assertNotNull("Falha na carrega do arquivo xml", document);
		
		dadosEstacaoTelemetricaDao.updateDadosChuvas15Min(estacao, document);
		
		List<DadosEstacaoTelemetrica15Min> dados = dadosEstacaoTelemetricaDao.list();
		
		assertNotNull("Os dados não foram salvos",dados);
	}
	
	@Test
	public void testApagarDadosEstacao15Min(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);	
		
		Date dataInicio = calendar.getTime();
		
		System.out.println(dataInicio);
		
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);	
		
		Date dataFim = calendar.getTime();
		
		DadosEstacaoTelemetrica15MinId identificador = new DadosEstacaoTelemetrica15MinId(estacao.getId(), dataInicio);
		
		DadosEstacaoTelemetrica15Min dado = dadosEstacaoTelemetricaDao.findById(identificador);
		
		System.out.println(dado);
		
		dadosEstacaoTelemetricaDao.apagarDadosChuvas15Min(estacao, dataInicio, dataFim);
		
		System.out.println("Estacao não encontrada" + dadosEstacaoTelemetricaDao.findById(new DadosEstacaoTelemetrica15MinId(estacao.getId(), dataInicio)));
		
	}
	
	@Test
	public void jsonTest(){
		 JSONArray array =  new JSONArray();
		 JSONObject jo1 = new JSONObject();
		 jo1.put("Year", "2004");
		 jo1.put("Sales", 1000);
		 array.add(jo1);
		 JSONObject jo2 = new JSONObject();
		 jo2.put("Year", "2005");
		 jo2.put("Sales", 1170);
		 array.add(jo2);
		 System.out.println(array);
		 
		 JSONArray array2 =  new JSONArray();
		 JSONArray array22 =  new JSONArray();
		 JSONArray array23 =  new JSONArray();
		 JSONArray array24 =  new JSONArray();
		
		 array22.add("Year");
		 array22.add("Sales");
		 array2.add(array22);
		
		 array23.add("2004");
		 array23.add(1000);
		 array2.add(array23);
		 
		 array24.add("2005");
		 array24.add(1170);
		 array2.add(array24);
		 
		 
		 System.out.println(array2);

	}
	
	@Test
	public void queryGroupTest(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);	
		
		Date dataFim = calendar.getTime();
		
		calendar.add(Calendar.DAY_OF_MONTH,-2);	
		
		Date dataInicio = calendar.getTime();
		
		List<JSONObject> lista = dadosEstacaoTelemetricaDao.listSerieDadosHora(estacao, dataInicio, dataFim);
		int i=0;
		
	}
	
	@Test
	public void atualizarDadosEstacaoFTP2Test(){
		int[] estacoes = {39530000, 39541000, 39540750, 39571000, 39550000, 39560000, 39580000, 39590000};
		Estacao estacao = new Estacao();
		for(int i=0; i<estacoes.length; i++){
			estacao.setCodigo(estacoes[i]);
			dadosEstacaoTelemetricaDao.atualizarDadosEstacaoFTP(estacao);
		}
	}

}
