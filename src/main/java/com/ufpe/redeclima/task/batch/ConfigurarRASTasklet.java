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

import java.io.File;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.SimpleSystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.SystemProcessExitCodeMapper;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
@Component
@StepScope
public class ConfigurarRASTasklet extends StepExecutionListenerSupport implements Tasklet, InitializingBean {

	private String hashParam;
	
	private SimDto simDto;
	
	/* Unidade de entrada de dados da simulação, pode ser dia, hora, etc */
	private EnumUnidadeTempo unidade;
	
	private String[] environmentParams = null;

	private File workingDirectory = null;

	private SystemProcessExitCodeMapper systemProcessExitCodeMapper = new SimpleSystemProcessExitCodeMapper();

	private long timeout;

	private long checkInterval = 1000;

	private StepExecution execution = null;

	private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

	private boolean interruptOnCancel = false;
	
	@Autowired
	private Workspace workspace;
	
	@Autowired
	private BatchParam batchParam;
	
	/* (non-Javadoc)
	 * @see org.springframework.batch.core.step.tasklet.Tasklet#execute(org.springframework.batch.core.StepContribution, org.springframework.batch.core.scope.context.ChunkContext)
	 */
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		
    	workspace.modificarArquivosRAS(simDto);
    	
    	return RepeatStatus.FINISHED;
	}
	
	/**
	 * Get a reference to {@link StepExecution} for interrupt checks during system command execution.
	 */
	@Override
	public void beforeStep(StepExecution stepExecution) {
		this.execution = stepExecution;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(systemProcessExitCodeMapper, "SystemProcessExitCodeMapper tem que ser inicializado");
		Assert.isTrue(timeout > 0, "valor do timeout tem que ser maior que zero");
		Assert.notNull(taskExecutor, "taskExecutor é obrigatorio");
		Assert.notNull(hashParam, "Falta parâmetro do processo");
		simDto = batchParam.getParam(hashParam);
		Assert.notNull(simDto, "Falta parâmetro do processo");
		Assert.notNull(simDto.getBacia(), "Bacia não informada");
		//Assert.notNull(simDto.getGrade(), "Grade não informada");
		Assert.notNull(simDto.getDataInicial(), "Data inicial não informada");
		Assert.notNull(simDto.getDataFinal(), "Data final não informada");
		Assert.notNull(simDto.getUsuario(), "Usuario não informado");
		workspace.setUsuario(simDto.getUsuario());
	}

	public EnumUnidadeTempo getUnidade() {
		return unidade;
	}

	public void setUnidade(EnumUnidadeTempo unidade) {
		this.unidade = unidade;
	}


	public String[] getEnvironmentParams() {
		return environmentParams;
	}

	public void setEnvironmentParams(String[] environmentParams) {
		this.environmentParams = environmentParams;
	}

	public File getWorkingDirectory() {
		return workingDirectory;
	}

	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public SystemProcessExitCodeMapper getSystemProcessExitCodeMapper() {
		return systemProcessExitCodeMapper;
	}

	public void setSystemProcessExitCodeMapper(
			SystemProcessExitCodeMapper systemProcessExitCodeMapper) {
		this.systemProcessExitCodeMapper = systemProcessExitCodeMapper;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public long getCheckInterval() {
		return checkInterval;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public StepExecution getExecution() {
		return execution;
	}

	public void setExecution(StepExecution execution) {
		this.execution = execution;
	}

	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	public boolean isInterruptOnCancel() {
		return interruptOnCancel;
	}

	public void setInterruptOnCancel(boolean interruptOnCancel) {
		this.interruptOnCancel = interruptOnCancel;
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
