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
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.interfaces.SimDto;

/**
 * @author edwardtz
 * Intercepta a llamada ao RAS e implementa o protocolo de acesso à area critica
 */
@Component
public class RasExecutionListener implements StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(RasExecutionListener.class);
	
	private String hashParam;
	
	private SimDto simDto;	
	
	@Autowired
	private BatchParam batchParam;
	
	@Autowired
	private SemaphoreRAS semaphoreRAS;
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#beforeStep(org.springframework.batch.core.StepExecution)
	 */
	public void beforeStep(StepExecution stepExecution) {

		simDto = batchParam.getParam(hashParam);
		// Entrando na area critica
		logger.info("Execução RAS Entro na zona critica ");
		logger.info("Parâmetros de entrada UsuarioID: " + simDto.getUsuario().getNome() + " baciaID: " + simDto.getBacia().getId() + " Data Inicial: " + simDto.getDataInicial() + " Data Final: " + simDto.getDataFinal());
				
		try {
			semaphoreRAS.getAvailable().acquire();
		} catch (InterruptedException e) {
			semaphoreRAS.getAvailable().release();
			logger.error("Error ao tentar entrar na zona critica de execução de RAS");
			logger.error("Detalhe do erro " + e.getMessage());
		}
				
	}

	/* (non-Javadoc)
	 * @see org.springframework.batch.core.StepExecutionListener#afterStep(org.springframework.batch.core.StepExecution)
	 */
	public ExitStatus afterStep(StepExecution stepExecution) {
		logger.info("Saindo da zona critica ");
		logger.info("Parâmetros de entrada UsuarioID: " + simDto.getUsuario().getId() + " baciaID: " + simDto.getBacia().getId() + " Data Inicial: " + simDto.getDataInicial() + " Data Final: " + simDto.getDataFinal());
		semaphoreRAS.getAvailable().release();
		return ExitStatus.COMPLETED;
	}

	public String getHashParam() {
		return hashParam;
	}

	public void setHashParam(String hashParam) {
		this.hashParam = hashParam;
	}

	public SimDto getSimDto() {
		return simDto;
	}

	public void setSimDto(SimDto simDto) {
		this.simDto = simDto;
	}

}
