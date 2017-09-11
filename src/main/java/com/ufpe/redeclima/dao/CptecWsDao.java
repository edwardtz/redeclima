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
package com.ufpe.redeclima.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ibm.icu.util.Calendar;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.GribLog;

/**
 * Esta classe se encarrega de obter os dados da fonte de arquivos GRIB via FTP. A entidade da fonte é da CPTEC
 * */
@Component
public class CptecWsDao {
	
	private static final Logger logger = LoggerFactory.getLogger(CptecWsDao.class);
	
	@Value("${jaxws.cptecFtp}")
	private String ftpHost;
	
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathLocalDownload;
	
	@Value("${jaxws.cptecDiasPrevisao}")
	private  String numeroDiasPrevisao;
	
	@Autowired
	private GribLogDao gribLogDao;
	
	/* Formato de dato conteuda nos nomes dos arquivos GRIB */
	private SimpleDateFormat dateFormatFile = new SimpleDateFormat("yyyyMMddhh");
	
	/* Formato da data dos nomes dos arquivos */
	private SimpleDateFormat dateFormatFolder = new SimpleDateFormat("yyyyMMdd");
	
	/* Este metodo retorna a data de previsao conteuda na segunda parte do nome do arquivo */
	private Date getDataPrevisao(String nomeArquivo){
		try {
			String[] partes = nomeArquivo.split("\\+");
			String parteDataPrevisao  = partes[1].replace(".grb", " ").trim();
			return dateFormatFile.parse(parteDataPrevisao);
		} catch (ParseException e) {
			logger.error("Erro ao obter a data de previsão a partir do nome do arquivo " + nomeArquivo);
		}
		return null;
	}
	
	
	/* 
	 * Este metodo filtra os arquivos remoto e retorna so os arquivos de previsao ate N dias adiante
	 *  */
	private String[] filtrarArquivos(String[] lista){
		ArrayList<String> listaFiltrada = new ArrayList<String>();
		Calendar calendario = Calendar.getInstance();
		calendario.add(Calendar.DAY_OF_MONTH, Integer.valueOf(numeroDiasPrevisao));
		Date dataLimite = calendario.getTime();
		if (lista!=null){
			for(String arquivo: lista){
				if (arquivo.endsWith(".grb") && dataLimite.after(getDataPrevisao(arquivo))){
					listaFiltrada.add(arquivo);
				}
			}
		}
		
		return listaFiltrada.toArray(new String[listaFiltrada.size()]);
		
	}
	
	
	/**
	 * Este metodo se encarrega de fazer o download dos arquivos GRIBs correspondentes a uma grade numa determinada data
	 * @param pastaDataPeriodoAtual nome da pasta associada com o periodo que se quer fazer a atualização
	 * @param grade grade de dados asociada aos arquivos que serão baixados
	 * */
	public void downloadDadosPrevisao(Grade grade, String pastaDataPeriodoAtual){
		
		String pathRemoto = grade.getRemotePathFtp() + "/" + pastaDataPeriodoAtual;
		String pathLocal = Paths.get(pathLocalDownload, grade.getNome()).toString();
		File diretorio = new File(pathLocal);
		
		 if (!diretorio.exists()) {
			 boolean result = diretorio.mkdir();  
			 if(result){    
				 logger.info("Diretorio " + pathLocal + " criado");
			 }
		 }
		 
		 pathLocal = Paths.get(pathLocalDownload, grade.getNome(), pastaDataPeriodoAtual).toString();
		 diretorio = new File(pathLocal);
		 
		 if (!diretorio.exists()) {
			 boolean result = diretorio.mkdir();  
			 if(result){    
				 logger.info("Diretorio " + pathLocal + " criado");
			 }
		 }
		 
		 this.downloadDadosPrevisao(pathLocal, pathRemoto, grade);
		
	}
	
	
	/**
	 * Este metodo se encarrega de fazer o download dos arquivos GRIBs correspondentes a uma grade numa determinada data
	 * @param pathDownloadLocal percurso do diretorio local onde sera armazenado os arquivos
	 * @param pathDownloadRemote percurso do diretorio remoto desde onde sera baixado os arquivos
	 * @param grade grade de dados asociada aos arquivos que serão baixados
	 * */
	public void downloadDadosPrevisao(String pathDownloadLocal, String pathDownloadRemote, Grade grade){

		FTPClient ftpClient = new FTPClient();
	    FTPClientConfig config= new FTPClientConfig();
	    ftpClient.configure(config);
	    String[] listResponse = null;
	    FileOutputStream arquivoSaida=null;
	    
	    try {
	      
	      int reply;
	      
	      ftpClient.connect(ftpHost);
	      ftpClient.login("anonymous", "");
	      logger.info("Resposta FTP : " + ftpClient.getReplyString());
	      ftpClient.setDataTimeout(5000);
	      ftpClient.setBufferSize(0); //TODO: melhorar com o ultimo snapshot da versao 3.3-snapshot
	      ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	      
	      // Depois da conexao verificar se o codigo de resposta foi com sucesso 
	      reply = ftpClient.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftpClient.disconnect();
	        logger.error("FTP server rejeitou a conexao em " + new Date());
	        return;
	      }
	      
	      ftpClient.enterLocalPassiveMode(); 
	      
	      int pasv =  ftpClient.pasv();
	      
	      File theDir = new File(pathDownloadLocal);

    	  // Se a pasta nao existe, se cria
    	  if (!theDir.exists()) {
    	    boolean result = theDir.mkdir();  
    	    if(result){    
    	       logger.info("Diretorio " + theDir.getAbsolutePath() + " creado com sucesso ");
    	     }
    	  }

    	  int cwd =  ftpClient.cwd(pathDownloadRemote);
    	  
	      listResponse =  ftpClient.listNames();
	      
	      listResponse = filtrarArquivos(listResponse);
	      
	      Arrays.sort(listResponse);
	      
	      for(int i=0; i<listResponse.length; i++){
	    	  
		    	 if(listResponse[i].endsWith(".grb") && !gribLogDao.isDownloaded(listResponse[i])){
		    		 
		    		 arquivoSaida = new FileOutputStream(Paths.get(pathDownloadLocal, listResponse[i]).toString());
		    		  
		    		  logger.info("Criando arquivo " + Paths.get(pathDownloadLocal, listResponse[i]).toString());
			          
		    		  // Fazer download do arquivo remoto
			          try {
			        	  gribLogDao.registrarEntrada(listResponse[i], grade);
			        	  
			        	  boolean resultado = ftpClient.retrieveFile(pathDownloadRemote + "/" + listResponse[i], arquivoSaida);
			        	  
			        	  gribLogDao.registrarFimDownload(listResponse[i], grade);
			        	  
			        	  logger.info("Finalizou o download do arquivo " + listResponse[i] + ftpClient.getReplyString());
				      
			          }catch (IOException e){
			        	  logger.info("Reintentando 1 vez ..." + listResponse[i]);
			        	  ftpClient.retrieveFile(pathDownloadRemote + "/" + listResponse[i], arquivoSaida);
			        	  gribLogDao.registrarFimDownload(listResponse[i], grade);
			        	  logger.info("Finalizou o download do arquivo " + listResponse[i] + ftpClient.getReplyString());
			          }
				      
			          if (arquivoSaida != null) {
		                  arquivoSaida.close();
		                  logger.info("Fechou arquivo " + listResponse[i]);
		              }
		    	 }
		  }
	      
	     
	      ftpClient.logout();
	      
	      
	    } catch(IOException e) {
	    	logger.error("*****************************************************************");
	    	logger.error("Error de entrada/saida processando o arquivo " + arquivoSaida);
	    	logger.error("Detalhe do error: " + e.getMessage());
	    	logger.error("*****************************************************************");
	    } finally {
	    	try {  
	    		if(ftpClient.isConnected()) {
		        	 if (arquivoSaida != null) {
		        		 arquivoSaida.close();
		                }
		        	 ftpClient.disconnect();
		        	 logger.info("Desconectando cliente ftp");
	    		}
		    } catch(IOException ioe) {
		          // do nothing
		    }
	    }
	    
	}
	
	/* Obtem o nome da pasta para o registro */
	private String getPastaPeriodo(GribLog registro) {
		
		return dateFormatFolder.format(registro.getDataDesde()) + "12";
		
	}
	
	
	/**
	 * Este metodo faz o download de um arquivo de GRIB determinado baseado nos dados do log
	 * @param registro registro asociado ao arquivo que vai ser baixado
	 * */
	public void downloadDadosPrevisao(GribLog registro){
		
		String pastaPeriodo = getPastaPeriodo(registro);
		String pathRemoto = registro.getGrade().getRemotePathFtp() + "/" + pastaPeriodo;
		String pathLocal = Paths.get(pathLocalDownload, registro.getGrade().getNome()).toString();
		File diretorio = new File(pathLocal);
		
		 if (!diretorio.exists()) {
			 boolean result = diretorio.mkdir();  
			 if(result){    
				 logger.info("Diretorio " +  pathLocal + " criado.");
			 }
		 }
		 
		 pathLocal = Paths.get(pathLocalDownload, registro.getGrade().getNome(), pastaPeriodo).toString();
		 diretorio = new File(pathLocal);
		 
		 if (!diretorio.exists()) {
			 boolean result = diretorio.mkdir();  
			 if(result){    
				 logger.info("Diretorio " +  pathLocal + " criado.");
			 }
		 }
		 
		 downloadDadosPrevisao(pathLocal, pathRemoto, registro);
		
	}

	
	/**
	 * Este metodo se encarrega de fazer o download de um determinado arquivo GRIB correspondentes a uma grade numa determinada data
	 * @param pathDownloadLocal percurso do diretorio local onde sera armazenado os arquivos
	 * @param pathDownloadRemote percurso do diretorio remoto desde onde sera baixado os arquivos
	 * @param registro registro asociado ao arquivo que vai ser baixado
	 * */
	public void downloadDadosPrevisao(String pathDownloadLocal, String pathDownloadRemote, GribLog registro){

		FTPClient ftpClient = new FTPClient();
	    FTPClientConfig config= new FTPClientConfig();
	    ftpClient.configure(config);
	    FileOutputStream arquivoSaida=null;
	    
	    try {
	      
	      int reply;
	      
	      ftpClient.connect(ftpHost);
	      ftpClient.login("anonymous", "");
	      logger.info(ftpClient.getReplyString());
	      ftpClient.setDataTimeout(5000);
	      ftpClient.setBufferSize(0); //TODO: melhorar com o ultimo snapshot da versao 3.3-snapshot
	      ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
	      
	      // Depois da conexao verificar se o codigo de resposta foi com sucesso 
	      reply = ftpClient.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftpClient.disconnect();
	        logger.error("FTP server rejeitou a conexao em " + new Date());
	        return;
	      }
	      
	      logger.info("Conectado a " + ftpHost);
	      
	      logger.info("Resposta FTP " + ftpClient.getReplyString());
	      
	      ftpClient.enterLocalPassiveMode(); 
	      
	      int pasv =  ftpClient.pasv();
	      
	      File theDir = new File(pathDownloadLocal);

    	  // Se a pasta nao existe, se cria
    	  if (!theDir.exists()) {
    	    boolean result = theDir.mkdir();  
    	    if(result){    
    	       logger.info("Diretorio " + pathDownloadLocal + "criado");
    	     }
    	  }

    	  int cwd =  ftpClient.cwd(pathDownloadRemote);
    	  
    	  arquivoSaida = new FileOutputStream(Paths.get(pathDownloadLocal, registro.getId()).toString());
		  
		  // Fazer download do arquivo remoto
          try {
        	  
        	  gribLogDao.registrarEntrada(registro.getId(), registro.getGrade());
        	  
        	  boolean result = ftpClient.retrieveFile(pathDownloadRemote + "/" + registro.getId(), arquivoSaida);
        	  
        	  if (result){
        		  gribLogDao.registrarFimDownload(registro.getId(), registro.getGrade());
        	  }
        	  
          }catch (IOException e){
        	  logger.warn("Reintentando 1 vez ..." + registro.getId());
        	  ftpClient.retrieveFile(pathDownloadRemote + "/" + registro.getId(), arquivoSaida);
        	  gribLogDao.registrarFimDownload(registro.getId(), registro.getGrade());
        	  logger.info("Finalizou o download do arquivo " + registro.getId() + ftpClient.getReplyString());
          }
	      
          if (arquivoSaida != null) {
              arquivoSaida.close();
              logger.info("Fechou arquivo " + registro.getId());
          }
	      
	     
	      ftpClient.logout();
	      
	      
	    } catch(IOException e) {
	      logger.error("Error ao escrever o arquivo " + registro.getId());
	    } finally {
	      if(ftpClient.isConnected()) {
	        try {
	        	 if (arquivoSaida != null) {
	        		 arquivoSaida.close();
	                }
	          ftpClient.disconnect();
	          logger.error("Desconectando cliente ftp");
	        } catch(IOException ioe) {
	        	logger.error("Error ao escrever o arquivo " + registro.getId());
	        }
	      }
	    }
	    
	}
	
}
