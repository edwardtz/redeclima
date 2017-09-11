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
package com.ufpe.redeclima.task;

import java.util.Locale;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.exception.SimulacaoParameterException;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.task.batch.BatchParam;

/**
* Esta classe executa a tarefa de preparar os dados e executar o simulador HMS. Os dados de simulação são os correspondentes a uma bacia e grade determinada, para cada ponto da grade
* se cria um arquivo com os dados de previsão e depois se executa o dssts sob esses arquivos, os quais geram um arquivo de extensão .dss que serve como entrada para o simulador HMS
**/
@Component
public class ExecutarSimulacaoTask implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ExecutarSimulacaoTask.class);
	
	/* Sistema operacional */
	private String so;
	
	/* Parêmetros de simulação */
	private SimDto simulacaoDto;
	
	@Autowired
	private SimpleJobLauncher jobLauncher;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private BatchParam batchParam;
	
	private boolean isWindows() {
		return (so.toUpperCase().indexOf("WIN") >= 0);
	}
 
	private boolean isMac() {
		return (so.toUpperCase().indexOf("MAC") >= 0);
	}
 
	private boolean isUnix() {
		return (so.toUpperCase().indexOf("LINUX") >=0 || so.toUpperCase().indexOf("NIX") >= 0 || so.toUpperCase().indexOf("NUX") >= 0 || so.toUpperCase().indexOf("AIX") > 0 );
	}
 
	private boolean isSolaris() {
		return (so.toUpperCase().indexOf("SUNOS") >= 0);
	}
	
	public void run(SimDto dto){
		simulacaoDto = dto;
		run();
	}
	
	/**
	 * Executa a simulação do HMS e RAS
	 * */
	public void run() {

		so = System.getProperty("os.name");
		
		if (!simulacaoDto.getBacia().isAllConfig()){
			logger.error("Configuração dos modelos da bacia incompletos");
			throw new SimulacaoParameterException("Configuração dos modelos da bacia incompletos");
		}
		
		if (simulacaoDto.getBacia()==null){
			logger.error("Bacia não definida para rodar a simulação");
			throw new SimulacaoParameterException("A bacia não esta definida");
		}
		
		if (simulacaoDto==null){
			logger.error("Grade nao definida para rodar a simulação");
			throw new SimulacaoParameterException("A grade não esta definida");
		}
		
		if (simulacaoDto.getDataInicial()==null){
			logger.error("Data inicial não definida");
			throw new SimulacaoParameterException("Data inicial não definida");
		}
		
		if (simulacaoDto.getDataFinal()==null){
			logger.error("Data final não definida");
			throw new SimulacaoParameterException("Data final não definida");
		}
		
		if (isUnix()){
			
			logger.error("HEC-HMS não implementado para Linux");
		        
		}else if (isSolaris()){
			
			logger.error("HEC-HMS não implementado para Solaris");
			
		}else if (isMac()){
			
			logger.error("HEC-HMS não implementado para Mac");
			
		}else if (isWindows()){
		
			
			Locale currentLocale = Locale.getDefault();
			
			logger.debug("Locale configurado: " + currentLocale.toString());
			
			Job job = (Job) applicationContext.getBean("executarSimulacao");

			try {
				
				batchParam.add(simulacaoDto);
				
				JobParametersBuilder paramBuilder = new JobParametersBuilder();
				paramBuilder.addDate("data", new Date());
				paramBuilder.addString("hashParam", simulacaoDto.getHash());
				
				JobExecution execution = jobLauncher.run(job, paramBuilder.toJobParameters());
				
				if (execution.getStatus() == BatchStatus.FAILED){
					simulacaoDto.getEstadoSimulacao().finalizar();
					batchParam.remove(simulacaoDto);
					logger.error("Estado de saida do processo com error: " + execution.getStatus());
					logger.error("Descrição do error: " + execution.getAllFailureExceptions());
				}

			} catch (Exception e) {
				simulacaoDto.getEstadoSimulacao().finalizar();
				batchParam.remove(simulacaoDto);
				logger.error("Error de processamento da simulação");
				logger.error("Detalhe do error: " + e.getMessage());
			}
			
			batchParam.remove(simulacaoDto);
			
		}else{
			logger.error("HEC-HMS só disponivel para Windows");
		}
	}

}
