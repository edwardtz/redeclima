package com.ufpe.redeclima.task;

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.SimpleThreadScope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.ufpe.redeclima.dao.ANAWSDao;
import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.dto.SimulacaoObsDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class TaskManagerTest extends AbstractTestExecutionListener {

	@Autowired
	TaskManager taskmanager;
	
	@Autowired
	AtualizacaoANAFTPTask anaftpTask;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private ANAWSDao anaWsDao;
	
	@Autowired
	private AtualizacaoCPTECFTPTask cptecftpTask;
	
	@Autowired
	private InicializacaoDadosTask inicializacaoDadosTask;
	
	@Autowired
	private ExecutarSimulacaoTask executarSimulacaoTask;
	
	@Autowired
	private SimulacaoObsDto simulacaoObsDto;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Test
	public void doTest(){
	
	}
	
	@Override
	public void prepareTestInstance(TestContext testContext) throws Exception {

		if (testContext.getApplicationContext() instanceof GenericApplicationContext) {
			GenericApplicationContext context = (GenericApplicationContext) testContext.getApplicationContext();
			ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
			Scope requestScope = new SimpleThreadScope();
			beanFactory.registerScope("request", requestScope);
			Scope sessionScope = new SimpleThreadScope();
			beanFactory.registerScope("session", sessionScope);
		}
	}
	
	@Ignore
	public void testExecutarAtualizacaoANAFTP() {

		Estacao e1 = new Estacao();
		e1.setId((long) 11);
		e1.setCodigo(39590000);
		
		Estacao e2 = new Estacao();
		e2.setId((long) 12);
		e2.setCodigo(39541000);
		
		Estacao e3 = new Estacao();
		e3.setId((long) 13);
		e3.setCodigo(39530000);
		
		Estacao e4 = new Estacao();
		e4.setId((long) 14);
		e4.setCodigo(39550000);
		
		Estacao e5 = new Estacao();
		e5.setId((long) 15);
		e5.setCodigo(39571000);
		
		Estacao e6 = new Estacao();
		e6.setId((long) 16);
		e6.setCodigo(39580000);
		
		Estacao e7 = new Estacao();
		e7.setId((long) 17);
		e7.setCodigo(39560000);
		
		Estacao e8 = new Estacao();
		e8.setId((long) 18);
		e8.setCodigo(39540750);
		
		List<Estacao> estacoes = new LinkedList<Estacao>();
		estacoes.add(e1);
		estacoes.add(e2);
		estacoes.add(e3);
		estacoes.add(e4);
		estacoes.add(e5);
		estacoes.add(e6);
		estacoes.add(e7);
		estacoes.add(e8);
		
		for(Estacao estacao: estacoes){
			anaftpTask.setEstacao(estacao);
			anaftpTask.run();
		}
		
		System.out.println("M8: Tarefa de atualização de estações telemetricas via FTP terminada em " + new Date() + " ... ");
		
	}
	
	@Ignore
	public void executarAtualizacaoCPTECFTPTest(){
		cptecftpTask.run();
	}
	
	@Ignore
	public void testInicializacaoDados(){
		inicializacaoDadosTask.run();
	}
	

}
