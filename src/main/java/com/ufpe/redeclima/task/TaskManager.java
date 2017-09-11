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

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.SeriePrevisaoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.dto.SimulacaoObsDto;
import com.ufpe.redeclima.dto.SimulacaoSerieDto;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.util.EnumTipoResponsavel;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * Esta classe representa o planificador de tarefas do sistema, aqui se definem os tempos em que tarefas automáticas serão realizadas, 
 * cada tarefa do procedimento esta definido para serem executados em intervalos de tempo fixo ou horários fixos, aqui também se definem
 * os tempos dos timers que são usados para ativar a execução da tarefa
 * */
@Component
public class TaskManager implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(TaskManager.class);
	
	/* Flag de ativação geral do schedule */
	@Value("${jaxws.schedule_ativo}")
	private String scheduleAtivos;
	
	/* Flag geral de ativação do schedule */
	private boolean scheduleAtivo;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private SeriePrevisaoDao seriePrevisaoDao;
	
	@Autowired
	private AtualizacaoANATask anaTask;
	
	@Autowired
	private AtualizacaoANAFTPTask anaftpTask;
	
	@Autowired
	private AtualizacaoCPTECFTPTask cptecftpTask;
	
	@Autowired
	private ExecutarSimulacaoTask executarSimulacaoTask;
	
	@Autowired
	private InicializacaoDadosTask inicializacaoDadosTask;
	
	@Autowired
	private ReexecutarAtualizacaoGrib reexecutarAtualizacaoGrib;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	/**
	 *  Este método executa a atualização de dados das estacões telemétricas de ANA cada um período fixo determinado
	 *  a atualização se realiza cada 15 minutos todos os dias o conjunto de estações que serão atualizadas
	 *  deverão ser estações que disponibilizam os dados cada 15 minutos 
	 *  */
	@Scheduled(cron="0 3,18,33,48 * * * ?")
	public void executarAtualizacaoANA(){
		
		if (scheduleAtivo){
			
			logger.info("Executando tarefa de atualização de estações telemétricas 15 minutos em " + new Date() + " ... ");
			
			List<Estacao> estacoes = estacaoDao.listEstacoes(EnumTipoResponsavel.ANA); 
				
			if (estacoes!=null && !estacoes.isEmpty()){
				for(Estacao estacao: estacoes){
					anaTask.setEstacao(estacao);
					anaTask.run();
				}
			}
			
			logger.info("Terminando tarefa de atualização de estações telemétricas 15 minutos em " + new Date() + " ... ");
		}
	}
	
	/**
	 * Este metodo executa a atualização de dados das estações telemetricas de ANA de dados obtidos via FTP
	 * */
	@Scheduled(cron="0 0,15,30,45 * * * ?")
	public void executarAtualizacaoANAFTP(){
		
		if (scheduleAtivo){
			
			logger.info("Executando tarefa de atualizacao estacoes telemetricas via FTP 1 hora em " + new Date() + " ... ");
			
			List<Estacao> estacoes = estacaoDao.listEstacoes(EnumTipoResponsavel.ANA);
				
			if(estacoes!=null && !estacoes.isEmpty()){
				for(Estacao estacao: estacoes){
					anaftpTask.setEstacao(estacao);
					anaftpTask.run();
				}
			}

			logger.info("Tarefa de atualização de estações telemetricas via FTP terminada em " + new Date() + " ... ");
			
		}
	}
	
	/**
	 * Este metodo se executa a atualização de dados a partir das grades de dados de previsão via FTP de arquivos GRIBs
	 * */
	@Scheduled(cron="0 0 13,15,17,19,21,23 * * ?")
	public void executarAtualizacaoCPTECFTP(){
		
		if (scheduleAtivo){
			
			logger.info("Executando tarefa de atualizacao de dados de previsão em " + new Date() + " ... ");
			
			for(Grade grade: gradeDao.list()){
				if (grade.isBuscaAtiva()){
					logger.info("Executando tarefa de atualização de dados de previsão para a grade " + grade.getNome() + " ...");
					cptecftpTask.setGradeAtual(grade);
					cptecftpTask.run();
					logger.info("Finalizou a tarefa de atualização de dados de previsão para a grade " + grade.getNome());
				}
			}
	 		
			logger.info("Tarefa de atualização de dados de previsão em " + new Date() + " ... ");
		}
		
	}
	
	/**
	 * Este metodo se executa todo dia em determinadas horas com objetivo de processar os arquivos que não foram processados
	 * provavelmente por problemas no download
	 * */
	@Scheduled(cron="0 0 0,2,4,8,10,12,14,16,18,20,22 * * ?")
	public void reexecutarAtualizacaoCPTECFTP(){
		
		if (scheduleAtivo){
			logger.info("Executando tarefa de re-execução da atualização de dados de previsão em " + new Date() + " ... ");
			reexecutarAtualizacaoGrib.run();
	 		logger.info("Tarefa de re-execução da atualização de dados de previsão em " + new Date() + " ... ");
		}
		
	}
	
	/**
	 * Executa a simulação HEC-HMS e HEC-RAS em esse ordem, a simulação se executa para uma bacia e referente a informação de previsão de 
	 * uma grade em um periodo determinado, tambem se deverá indicar a unidade de dados da serie, pudendo ser horas ou dias
	 * */
	@Scheduled(cron="0 0 0,3,22 * * ?")
	public void executarSimulacaoSerie() {
		
		if (scheduleAtivo){
			
			logger.info("Executando simulacao HMS-RAS com os dados de series de previsão " + new Date() + " ... ");
			
			List<Bacia> bacias = baciaDao.listOperativas();
			
			SimulacaoSerieDto simulacaoDto = (SimulacaoSerieDto) applicationContext.getBean("simulacaoSerieDto");
			simulacaoDto.setUsuario(usuarioDao.findByLogin("ADMIN"));
			simulacaoDto.setUnidade(EnumUnidadeTempo.HORA); //TODO Para a simulação se usa a unidade de horas mas poderia ser diferente dependendo do tipo de grade
			
			List<SeriePrevisao> series;
			
			for(Bacia bacia: bacias){
				simulacaoDto.setBacia(bacia);
				series = seriePrevisaoDao.listNaoSimuladas(simulacaoDto);
				for (SeriePrevisao serie: series){
					//TODO checar se a grade cobre alguma parte da superficie da bacia na verdade poderia ser feito com 
					simulacaoDto.setSeriePrevisao(serie);
					executarSimulacaoTask.run(simulacaoDto);
				}
			}
			
			logger.info("Finalizou a simulação HMS-RAS com dados de series de previsão em " + new Date() + " ... ");
			
		}
	}
	
	@Scheduled(cron="0 0 4,19 * * ?")
	public void executarSimulacaoObservadaAutomatica() {
		
		if (scheduleAtivo){
			
			logger.info("Executando simulacao HMS-RAS com os dados de series de observação " + new Date() + " ... ");
			
			List<Bacia> bacias = baciaDao.listOperativas();
			
			SimulacaoObsDto simulacaoObsDto = (SimulacaoObsDto) applicationContext.getBean("simulacaoObsDto");
			simulacaoObsDto.setUsuario(usuarioDao.findByLogin("ADMIN"));
			simulacaoObsDto.setUnidade(EnumUnidadeTempo.HORA); //TODO Para a simulação se usa a unidade de horas mas poderia ser diferente dependendo do tipo de grade
			simulacaoObsDto.setDataFinal(new Date());
			Calendar calendario = Calendar.getInstance();
			calendario.setTime(new Date());
			calendario.add(Calendar.DAY_OF_MONTH, -4);
			simulacaoObsDto.setDataInicial(calendario.getTime());
			
			for(Bacia bacia: bacias){
				simulacaoObsDto.setBacia(bacia);
				executarSimulacaoTask.run(simulacaoObsDto);
			}	
			
			logger.info("Finalizou a simulação HMS-RAS com dados de series de observação em " + new Date() + " ... ");
			
		}
	}
	
	/**
	 * Este metodo executa a simulação HEC-HMS e HEC-RAS em esse ordem, a simulação se executa para uma bacia e referenta a informação de previsão de 
	 * uma grade em um periodo determinado, tambem se deverá indicar a unidade de dados da serie, pudendo ser horas ou dias
	 * */
	public int executarSimulacao(SimDto simDto) {
		
		logger.info("Executando simulacao HMS-RAS com os dados de previsao " + new Date() + " ... ");
		executarSimulacaoTask.run(simDto);
		logger.info("Finalizou a simulação HMS-RAS com dados de previsão em " + new Date() + " ... ");
		
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		scheduleAtivo = new Boolean(scheduleAtivos);
	}

	public boolean isScheduleAtivo() {
		return scheduleAtivo;
	}

	public void setScheduleAtivo(boolean scheduleAtivo) {
		this.scheduleAtivo = scheduleAtivo;
	}
	
	
}
