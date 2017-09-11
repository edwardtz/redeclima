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
package com.ufpe.redeclima.task.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.interfaces.SimDto;

/**
 * @author edwardtz
 * Intercepta a llamada ao processo HMS/RAS e implementa o protocolo de acesso à area critica para o par de parametros usuario e bacia
 * no nivel dos processos HEC-HMS/RAS num workspace de usuario pode executar uma convinação por vez da dupla (usuario, bacia)
 */
@Component
public class SimulacaoExecutionListener implements JobExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(SimulacaoExecutionListener.class);
	
	private String hashParam;
	
	private SimDto simDto;
	
	@Autowired
	private SemaphoreSim semaphoreSim;
	
	@Autowired
	private BatchParam batchParam;
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.JobExecutionListener#beforeJob(org.springframework.batch.core.JobExecution)
	 */
	public void beforeJob(JobExecution jobExecution) {
		simDto = batchParam.getParam(jobExecution.getJobParameters().getString("hashParam"));
		// Entrando na area critica
		logger.info("Execução simulacao entrou na zona critica ");
				
		try {
			semaphoreSim.getSemaforo(simDto).acquire();
		} catch (InterruptedException e) {
			semaphoreSim.getSemaforo(simDto).release();
			logger.error("Error ao tentar entrar na zona critica de execução de RAS");
			logger.error("Detalhe do erro " + e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.JobExecutionListener#afterJob(org.springframework.batch.core.JobExecution)
	 */
	public void afterJob(JobExecution jobExecution) {

		logger.info("Saindo da zona critica ");
		semaphoreSim.getSemaforo(simDto).release();
		
	}

	public String getHashParam() {
		return hashParam;
	}

	public void setHashParam(String hashParam) {
		this.hashParam = hashParam;
	}
}
