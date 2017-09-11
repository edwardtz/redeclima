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
package com.ufpe.redeclima.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.DocumentoDao;
import com.ufpe.redeclima.model.Documento;

/**
 * @author edwardtz
 *
 */
@Controller
public class DocumentacaoController {
	
	/* Diretorio dos modelos de simulação */
	@Value("${parameter.path_doc}")
	private String pathDoc;

	private static final Logger logger = LoggerFactory.getLogger(DocumentacaoController.class);
	
	private StreamedContent manual; 
	
	private StreamedContent docDownload; 
	
	private UploadedFile docUpload; 
	
	private Documento documento;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private DocumentoDao documentoDao;
	
	public String adicionarDocumento(){
		documento = new Documento();
		docUpload = null;
		return "documentoForm";
	}
	
	public String apagar(){
		documentoDao.remove(documento);
		return "listaDocumentos";
	}
	
	public String salvarDoc(){
		documento.setNome(docUpload.getFileName());
		documento.setDataAtualizacao(new Date());
		documentoDao.saveOrUpdate(documento);
		return "listaDocumentos";
	}
	
	public void upload(FileUploadEvent event) {  
        docUpload = event.getFile();
		FacesMessage msg = new FacesMessage("Arquivo", docUpload.getFileName() + " foi carregado.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        try {
			copyFile(docUpload.getFileName(), event.getFile().getInputstream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public StreamedContent getManual(){
		
		Resource resource = applicationContext.getResource("resources/doc/Manual.pdf");
		InputStream stream;
		try {
			stream = resource.getInputStream();
			manual = new DefaultStreamedContent(stream, "application/pdf", "Manual do Usuário MAVEN.pdf");
		} catch (IOException e) {
			logger.error("Error exportando os dados do arquivo Manual do Usuário MAVEN.pdf");
			logger.error("Detalhe do error" + e.getMessage());
		}
		
		return manual;
	}
	
	public StreamedContent getFileDoc(){
		
		String arquivoNome = Paths.get(pathDoc, documento.getNome()).toString();
		InputStream stream;
		try {
			stream = new FileInputStream(arquivoNome);
			docDownload = new DefaultStreamedContent(stream, "application/pdf", documento.getNome());
		} catch (IOException e) {
			logger.error("Error exportando os dados do arquivo Manual do Usuário MAVEN.pdf");
			logger.error("Detalhe do error" + e.getMessage());
		}
		
		return docDownload;
	}
	
	public void copyFile(String fileName, InputStream in) {
    	
    	try {
			
    		String nomeArquivo = Paths.get(pathDoc, fileName).toString();
    		OutputStream out = new FileOutputStream(new File(nomeArquivo));
			
    		int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			
			in.close();
			out.flush();
			out.close();
			
    	} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    }

	public List<Documento> getDocumentos(){
		return documentoDao.list();
	}
	
	public Documento getDocumento() {
		return documento;
	}

	public void setDocumento(Documento documento) {
		this.documento = documento;
	}


	public UploadedFile getDocUpload() {
		return docUpload;
	}


	public void setDocUpload(UploadedFile docUpload) {
		this.docUpload = docUpload;
	}
	
	
	
}
