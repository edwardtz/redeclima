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

import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.DadoSeriePrevisaoDao;
import com.ufpe.redeclima.dao.GribLogDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.SeriePrevisaoDao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SeriePrevisao;

/**
 * @author edwardtz
 * Esta classe executa a tarefa de inicializar os dados de previsão a partir dos arquivos grib que ja esteveram disponiveis 
 * na pasta de download
 */
@Component
public class InicializacaoGRIBData implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(InicializacaoGRIBData.class);

	/* Path de download local ode os arquivos serão salvos */
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathLocalDownload;
	
	/* Quantidade de dias de previsão que são baixados */
	@Value("${jaxws.cptecDiasPrevisao}")
	private String quantidadeDias;
	
	@Autowired
	private GribLogDao gribLogDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private DadoPontoGradeDao dadoPontoGradeDao;
	
	@Autowired
	private DadoSeriePrevisaoDao dadoSeriePrevisaoDao;
	
	@Autowired
	private SeriePrevisaoDao seriePrevisaoDao;
	
	private Grade gradeAtual;
	
	/* Formato da data dos nomes dos arquivos */
	private SimpleDateFormat dateFormatFolder = new SimpleDateFormat("yyyyMMddHH");
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		File pastaDownloadLocal;
		
		String pastaPeriodoAtual;
		
		String pathLocalPastaPeriodoAtual;
		
		File raiz = new File(Paths.get(pathLocalDownload, gradeAtual.getNome()).toString());
		
		File[] diretorios = raiz.listFiles();
		
		if (diretorios!=null){
			
			// Ordenar a lista de diretorios para serem processados em ordem ascendente
			Arrays.sort(diretorios, NameFileComparator.NAME_COMPARATOR);
			
			if(diretorios!=null){
				
				for (File diretorio : diretorios){
					
					pastaPeriodoAtual = diretorio.getName();
					
					pathLocalPastaPeriodoAtual = Paths.get(pathLocalDownload, gradeAtual.getNome(), pastaPeriodoAtual).toString();
					
					pastaDownloadLocal = new File(pathLocalPastaPeriodoAtual);

					File[] listaDeArquivos = pastaDownloadLocal.listFiles();
					
					// Ordenar a lista de arquivos do diretorio para serem processados em ordem ascendente
					Arrays.sort(listaDeArquivos, NameFileComparator.NAME_COMPARATOR);
					
					for(File arquivo : listaDeArquivos){
						   
						if (arquivo.getName().endsWith(".grb") && !gribLogDao.isProcessado(arquivo.getName())){
							
							if (gribLogDao.findById(arquivo.getName())==null){
								gribLogDao.registrarEntrada(arquivo.getName(), gradeAtual);
								if (arquivo.length()>0){
									gribLogDao.registrarFimDownload(arquivo.getName(), gradeAtual);
								}
							}
							
							pontoGradeDao.atualizarPontoGrade(pathLocalPastaPeriodoAtual, arquivo.getName(), gradeAtual);
							
							dadoPontoGradeDao.atualizarDadosPontoGrade(pathLocalPastaPeriodoAtual,arquivo.getName(), gradeAtual);
						}
						
					}
					
					// Primero passo salvar a serie
					Date dataRodada;
					
					try {
						dataRodada = dateFormatFolder.parse(pastaPeriodoAtual);
						Calendar calendario = Calendar.getInstance();
						calendario.setTime(dataRodada);
						calendario.add(Calendar.DAY_OF_MONTH, Integer.parseInt(quantidadeDias));
						Date dataFim = gradeAtual.obterUltimoTempoSerie(calendario.getTime());
						SeriePrevisao serie = seriePrevisaoDao.findOrCreate(dataRodada, dataRodada, dataFim, gradeAtual);
						serie = seriePrevisaoDao.findByAttributes(dataRodada, dataRodada, dataFim, gradeAtual);
						
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
