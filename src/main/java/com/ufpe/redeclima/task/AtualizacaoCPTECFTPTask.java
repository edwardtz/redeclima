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

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.CptecWsDao;
import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.DadoSeriePrevisaoDao;
import com.ufpe.redeclima.dao.GribLogDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.SeriePrevisaoDao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SeriePrevisao;

/**
 *  Esta classe executa a tarefa de atualizar os dados de previsão desde o site da CPTEC, inicialmente faz o download dos arquivos em formato GRIB, depois extrae os dados de previsão de chuva
 *  e os armazena na BD	
 * */
@Component
public class AtualizacaoCPTECFTPTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(AtualizacaoCPTECFTPTask.class);
	
	/* Path de download local ode os arquivos serão salvos */
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathLocalDownload;
	
	
	/* Quantidade de dias de previsão que são baixados */
	@Value("${jaxws.cptecDiasPrevisao}")
	private String quantidadeDias;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private DadoPontoGradeDao dadosPontoPrevisaoDao;
	
	@Autowired
	private DadoSeriePrevisaoDao dadoSeriePrevisaoDao;
	
	@Autowired
	private CptecWsDao cptecWsDao;
	
	@Autowired
	private GribLogDao  gribLogDao;
	
	@Autowired
	private SeriePrevisaoDao seriePrevisaoDao;
	
	/* Grade atual que esta sendo processada */
	private Grade gradeAtual;
	
	/* Formato da data dos nomes dos arquivos sem o digito da hora */
	private SimpleDateFormat dateFormatFolder = new SimpleDateFormat("yyyyMMdd");
	
	/* Formato da data dos nomes dos diretorios com o digito da hora */
	private SimpleDateFormat dateFormatFolderWithHour = new SimpleDateFormat("yyyyMMddHH");
	
	/* Obtem o nome da pasta para o periodo atual */
	private String getPastaPeriodoAtual() {
		
		Date agora = new Date();
		
		return dateFormatFolder.format(agora) + "12";
		
	}
	
	public void run() {
		
		File pastaDownloadLocal;
		
		String pastaPeriodoAtual;
		
		String pathLocalPastaPeriodoAtual;
		
		pastaPeriodoAtual = getPastaPeriodoAtual();
		
		SeriePrevisao serie;
		
		
			
		// Fazer download dos arquivos para a data atual
		cptecWsDao.downloadDadosPrevisao(gradeAtual, pastaPeriodoAtual);
			
		pathLocalPastaPeriodoAtual = Paths.get(pathLocalDownload, gradeAtual.getNome(), pastaPeriodoAtual).toString();
			
		pastaDownloadLocal = new File(pathLocalPastaPeriodoAtual);

		File[] listaDeArquivos = pastaDownloadLocal.listFiles();
			
		// Ordenar a lista de arquivos do diretorio para serem processados em ordem ascendente
		Arrays.sort(listaDeArquivos, NameFileComparator.NAME_COMPARATOR);
			
		for(File arquivo : listaDeArquivos){
				   
			if (arquivo.getName().endsWith(".grb") && !gribLogDao.isProcessado(arquivo.getName())){
					
				pontoGradeDao.atualizarPontoGrade(pathLocalPastaPeriodoAtual, arquivo.getName(),gradeAtual);
					
				dadosPontoPrevisaoDao.atualizarDadosPontoGrade(pathLocalPastaPeriodoAtual, arquivo.getName(), gradeAtual);
			}
		}
		
			
		Date dataRodada;
		
		try {
				
			// Primero passo salvar a serie
			dataRodada = dateFormatFolderWithHour.parse(pastaPeriodoAtual);
			Calendar calendario = Calendar.getInstance();
			calendario.setTime(dataRodada);
			calendario.add(Calendar.DAY_OF_MONTH, Integer.parseInt(quantidadeDias));
			Date dataFim = gradeAtual.obterUltimoTempoSerie(calendario.getTime());
			serie = seriePrevisaoDao.findOrCreate(dataRodada, dataRodada, dataFim, gradeAtual);
			
			// Processamento de series temporais
			for(File arquivo : listaDeArquivos){
				   
				if (arquivo.getName().endsWith(".grb") && !gribLogDao.isProcessadoSeries(arquivo.getName())){
					
					dadoSeriePrevisaoDao.atualizarDadosSerie(pathLocalPastaPeriodoAtual, arquivo.getName(), serie);
				}
			}
			
		} catch (ParseException e) {
			logger.error("Error ao converter a data" + pastaPeriodoAtual);
			logger.error("Detalhe do error: " + e.getMessage());
		}
		
		listaDeArquivos = pastaDownloadLocal.listFiles();
		
		for(File arquivo : listaDeArquivos){
			   
			if (!arquivo.getName().endsWith(".grb")){
				
				arquivo.delete();
			}
			
		}
		
	}

	public Grade getGradeAtual() {
		return gradeAtual;
	}

	public void setGradeAtual(Grade gradeAtual) {
		this.gradeAtual = gradeAtual;
	}
	
	

}
