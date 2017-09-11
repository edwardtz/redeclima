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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.ufpe.redeclima.model.Estacao;

@Component
public class ANAWSDao {
	
	private static final Logger logger = LoggerFactory.getLogger(ANAWSDao.class);
	
	@Value("${jaxws.ana}")
	String uriWebService;
	
	@Value("${jaxws.anaFtp}")
	String ftpHost;
	
	@Value("${jaxws.pathDownloadAnaFtp}")
	String pathDownload;
	
	@Value("${jaxws.remotePathDownloadAnaFtp}")
	String remoteFolderPath;
	
	@Value("${jaxws.usuarioAnaFtp}")
	String usuario;
	
	@Value("${jaxws.senhaAnaFtp}")
	String senha;
	
	@Autowired
	private FTPEstacaoLogDao ftpEstacaoLogDao;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	
	public Document getDataEstacao(int codEstacao, Date dataInicio, Date dataFim){
		
		URL url;
		HttpURLConnection connection;
		Document documento=null;
		
		try {
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy"); 
			url = new URL(uriWebService + "?codEstacao=" + codEstacao + "&dataInicio=" + dateFormat.format(dataInicio) + "&dataFim=" + dateFormat.format(dataFim));
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			InputStream xml = connection.getInputStream();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			documento = db.parse(xml);
			documento.getDocumentElement().normalize();
			
		
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (IOException e) {
			logger.error("Error atualizando estação telemetrica " + codEstacao);
			logger.error("Falha probavel servidor sem conexão à internet");
			logger.error("Detalhe do erro " + e.getMessage());
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return documento;
		
	}
	
	public void getDataEstacaoFTP(int codigoEstacao){
		
		FTPClient ftpClient = new FTPClient();
	    FTPClientConfig config= new FTPClientConfig(FTPClientConfig.SYST_NT);
	    ftpClient.configure(config);
	    String[] listResponse = null;
	    FileOutputStream arquivoSaida=null;
	    
	    boolean error = false;
	    
	    try {
	      
	      int reply;
	      ftpClient.connect(ftpHost);
	      ftpClient.login(usuario, senha);
	      ftpClient.setDataTimeout(5000);
	      ftpClient.setBufferSize(1024);
	      ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
	      
	      // Depois da conexao verificar se o codigo de resposta foi com sucesso 
	      reply = ftpClient.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftpClient.disconnect();
	        System.out.println("FTP server rejeitou a conexao em " + new Date());
	        System.exit(1);
	      }
	      
	      System.out.println("Conectado a " + ftpHost);
	      System.out.println(ftpClient.getReplyString());
	      
	      ftpClient.enterLocalPassiveMode(); 
	      
	      int cwd =  ftpClient.cwd(remoteFolderPath);
	      
	      int pasv =  ftpClient.pasv();
	      
	      listResponse =  ftpClient.listNames();
	      
	      String codigoEstacaoString = Integer.toString(codigoEstacao);
	      
	      for(int i=0; i<listResponse.length; i++){
	    	 
	    	 if(listResponse[i].startsWith(codigoEstacaoString)){
		          // Criar arquivo remoto para download. 
	    		  arquivoSaida = new FileOutputStream(Paths.get(pathDownload, listResponse[i]).toString());
		          System.out.println("Criando arquivo " + Paths.get(pathDownload, listResponse[i]).toString());
		          // Fazer download do arquivo remoto
		          try {
		        	  
		        	  ftpClient.retrieveFile(remoteFolderPath + "/" + listResponse[i], arquivoSaida);
		        	  System.out.println("Finalizou o download do arquivo " + listResponse[i] + ftpClient.getReplyString());
			      
		          }catch (IOException e){
		        	  ftpClient.retrieveFile(remoteFolderPath + "/" + listResponse[i], arquivoSaida);
		        	  System.out.println("Reintentando 1 vez ...");
		          }
			      
		          if (arquivoSaida != null) {
	                  arquivoSaida.close();
	                  System.out.println("Fechou arquivo " + listResponse[i]);
	              }
	    	 }
	      }
	      
	      System.out.println("Finalizou a transferencia de arquivos para a estação " + codigoEstacao);
	      ftpClient.logout();
	      
	      
	    } catch(IOException e) {
	      error = true;
	      e.printStackTrace();
	      System.out.println("Error de excepcao");
	    } finally {
	      if(ftpClient.isConnected()) {
	        try {
	        	 if (arquivoSaida != null) {
	        		 arquivoSaida.close();
	                }
	          ftpClient.disconnect();
	          System.out.println("Desconectando cliente ftp");
	        } catch(IOException ioe) {
	          // do nothing
	        }
	      }
	    }
	    
	}

	private Date obterPeriodoAtual(){
		
		Date agora = new Date();
		
		Calendar calendario = Calendar.getInstance();
	    calendario.set(Calendar.MINUTE, 0);
		calendario.set(Calendar.SECOND, 0);
		  
		Date data0 = calendario.getTime();
		  
		calendario.set(Calendar.MINUTE, 15);
		calendario.set(Calendar.SECOND, 0);
		  
		Date data15 = calendario.getTime();
		
		calendario.set(Calendar.MINUTE, 30);
		calendario.set(Calendar.SECOND, 0);
		  
		Date data30 = calendario.getTime();
		
		calendario.set(Calendar.MINUTE, 45);
		calendario.set(Calendar.SECOND, 0);
		  
		Date data45 = calendario.getTime();
		
		if (agora.after(data0) && agora.before(data15)){
			return data0;
		}else if (agora.after(data15) && agora.before(data30)){
			return data15;
		}else if (agora.after(data30) && agora.before(data45)){
			return data30;
		}else {
			return data45;
		}
	}
	
	public void getDataEstacaoFTPAtual(int codigoEstacao){
		
		FTPClient ftpClient = new FTPClient();
	    FTPClientConfig config= new FTPClientConfig(FTPClientConfig.SYST_NT); //TODO: modificar para adapta-lo ao sistema operacional automaticamente
	    ftpClient.configure(config);
	    FileOutputStream arquivoSaida=null;
	    Date periodoAtual = obterPeriodoAtual();
	    
	    boolean error = false;
	    
	    try {
	      
	      int reply;
	      ftpClient.connect(ftpHost);
	      ftpClient.login(usuario, senha);
	      ftpClient.setDataTimeout(5000);
	      ftpClient.setBufferSize(1024);
	      ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
	      
	      // Depois da conexao verificar se o codigo de resposta foi com sucesso 
	      reply = ftpClient.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftpClient.disconnect();
	        System.out.println("FTP server rejeitou a conexao em " + new Date());
	        System.exit(1);
	      }
	      
	      System.out.println("Conectado a " + ftpHost);
	      System.out.println(ftpClient.getReplyString());
	      
	      ftpClient.enterLocalPassiveMode(); 
	      
	      int cwd =  ftpClient.cwd(remoteFolderPath);
	      
	      int pasv =  ftpClient.pasv();
	      
	      String codigoEstacaoString = Integer.toString(codigoEstacao);
	      
	      String nomeArquivo = codigoEstacaoString + "___" + dateFormat.format(periodoAtual) + ".MIS";
	      
          // Criar arquivo remoto para download.
		  arquivoSaida = new FileOutputStream(Paths.get(pathDownload, nomeArquivo).toString());
          System.out.println("Criando arquivo " + Paths.get(pathDownload, nomeArquivo).toString());
          // Fazer download do arquivo remoto
          try {
        	  
        	  ftpClient.retrieveFile(remoteFolderPath + "/" + nomeArquivo, arquivoSaida);
        	  System.out.println("Finalizou o download do arquivo " + nomeArquivo + ftpClient.getReplyString());
	      
          }catch (IOException e){
        	  ftpClient.retrieveFile(remoteFolderPath + "/" + nomeArquivo, arquivoSaida);
        	  System.out.println("Reintentando 1 vez ...");
          }
	      
          if (arquivoSaida != null) {
              arquivoSaida.close();
              System.out.println("Fechou arquivo " + nomeArquivo);
          }
	      
	      System.out.println("Finalizou a transferencia de arquivos para a estação " + codigoEstacao);
	      ftpClient.logout();
	      
	      
	    } catch(IOException e) {
	      error = true;
	      e.printStackTrace();
	    } finally {
	      if(ftpClient.isConnected()) {
	        try {
	        	 if (arquivoSaida != null) {
	        		 arquivoSaida.close();
	             }
	        	 ftpClient.disconnect();
	        	 System.out.println("Desconectando cliente ftp");
	        } catch(IOException ioe) {
	          // do nothing
	        }
	      }
	    }
	    
	}

	/**
	 * Baixa os arquivos .MIS do FTP associados a uma estação
	 * @param estação
	 * */
	public void baixarDadosFtp(Estacao estacao){
		
		FTPClient ftpClient = new FTPClient();
	    FTPClientConfig config= new FTPClientConfig(FTPClientConfig.SYST_NT); //TODO: modificar para adapta-lo ao sistema operacional automaticamente
	    ftpClient.configure(config);
	    FileOutputStream arquivoSaida=null;
	    String[] listaResposta = null;
	    
	    try {
	      
	      int reply;
	      ftpClient.connect(ftpHost);
	      ftpClient.login(usuario, senha);
	      ftpClient.setDataTimeout(5000);
	      ftpClient.setBufferSize(0);
	      ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
	      
	      // Depois da conexao verificar se o codigo de resposta foi com sucesso 
	      reply = ftpClient.getReplyCode();

	      if(!FTPReply.isPositiveCompletion(reply)) {
	        ftpClient.disconnect();
	        logger.error("FTP server rejeitou a conexao em " + new Date());
	        return;
	      }
	      
	      logger.info("Conectado a " + ftpHost);
	      
	      logger.info("Resposta obtida " + ftpClient.getReplyString());
	      
	      ftpClient.enterLocalPassiveMode(); 
	      
	      int cwd =  ftpClient.cwd(remoteFolderPath);
	      
	      int pasv =  ftpClient.pasv();
	      
    	  listaResposta =  ftpClient.listNames();
    	  
    	  String codigoEstacao = String.valueOf(estacao.getCodigo());
	      
    	  for(int i=0; i < listaResposta.length; i++){
    		  if (listaResposta[i].startsWith(codigoEstacao) && listaResposta[i].endsWith(".MIS")){
    			 if (ftpEstacaoLogDao.findById(listaResposta[i]) == null) {
    				// Criar arquivo remoto para download.
    				arquivoSaida = new FileOutputStream(Paths.get(pathDownload, listaResposta[i]).toString());
    				 // Fazer download do arquivo remoto
    		          try {
    		        	  
    		        	  ftpClient.retrieveFile(remoteFolderPath + "/" + listaResposta[i], arquivoSaida);
    		        	  logger.info("Finalizou o download do arquivo " + listaResposta[i] + ftpClient.getReplyString());
    			      
    		          }catch (IOException e){
    		        	  ftpClient.retrieveFile(remoteFolderPath + "/" + listaResposta[i], arquivoSaida);
    		        	  logger.warn("Reintentando 1 vez ...");
    		          }
    		          
    		          if (arquivoSaida != null) {
    		              arquivoSaida.close();
    		              logger.info("Fechou arquivo " + listaResposta[i]);
    		          }
    			 }
    		  }
    	  }
    	  
    	  logger.info("Finalizou a transferencia de arquivos para a estação " + codigoEstacao);
	      
    	  ftpClient.logout();
    	  
	    } catch(IOException e) {
	    	logger.error("Error de entada/saida baixando arquivos para a estação " + estacao.getCodigo());
	    } finally {
		      if(ftpClient.isConnected()) {
		        try {
		        	 if (arquivoSaida != null) {
		        		 arquivoSaida.close();
		             }
		        	 ftpClient.disconnect();
		        	 logger.info("Desconectando cliente ftp");
		        } catch(IOException ioe) {
		          // do nothing
		        }
		      }
	    }
	    
	}

}
