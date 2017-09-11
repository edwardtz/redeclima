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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;

import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.bean.RasWrapper;
import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.RiosKMLDao;
import com.ufpe.redeclima.dao.SecoesKMLDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.Unziper;

@Controller
@Scope("session")
public class BaciaController implements InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(BaciaController.class);
	
	/* Diretorio dos modelos de simulação */
	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	private Bacia bacia;
	
	private List<Bacia> bacias;	
	
	private UploadedFile file; 
	
	private UploadedFile fileSecoes; 
	
	private UploadedFile fileHms; 
	
	private UploadedFile fileRas; 
	
	private Usuario usuario;
	
	private List<Trecho> trechos;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private Unziper unziper;
	
	@Autowired
	private RasWrapper rasWrapper;
	
	@Autowired
	private Workspace workspace;
	
	@Autowired
	private RiosKMLDao riosKMLDao;
	
	@Autowired
	private SecoesKMLDao secoesKMLDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	public BaciaController(){
		bacia = new Bacia();
	}
	
	public Bacia getBacia() {
		return bacia;
	}

	public String save() {
		baciaDao.saveOrUpdate(bacia);
		bacia = new Bacia();
		invalidateBacias();
		return "listaBacias";
	}
	
	public String saveAndContinue() {
		baciaDao.saveOrUpdate(bacia);
		bacia = baciaDao.findByNome(bacia.getNome());
		invalidateBacias();
		return "configuracaoBacia";
	}
	
	public String finalizarConfiguracao(){
		//Aplicar configuracao para o modelo e fazer tarefas complementarias
		if (bacia.isAllConfig()){
			workspace.atualizarWorkSpaces(bacia);
		}
		return "baciaFormView";
	}
	
	public String adicionarBacia(){
		bacia = new Bacia();
		return "baciaFormView";
	}
	
	public String excluirBacia(){
		//TODO checar que a bacia nao estaja associada a grade
		//TODO checar que a bacia não esteja asociada a estações
		//TODO deletar a bacia, implica deletar as fotos tambem
		baciaDao.remove(bacia);
		invalidateBacias();
		return "listaBacias";
	}
	
	private void invalidateBacias() {
		bacias = null;
	}

	public List<Bacia> getBacias() {
		if (bacias == null) {
			bacias = baciaDao.list();
		}
		return bacias;
	}
	
	public UploadedFile getFile() {  
        return file;  
    }  
  
    public void setFile(UploadedFile file) {  
        this.file = file;  
    }  
  
    public void upload(FileUploadEvent event) {  
            file = event.getFile();
            String nomeNovo = bacia.getNome() + "_Rios.kml";
    		FacesMessage msg = new FacesMessage("Arquivo", file.getFileName() + " foi carregado.");  
            FacesContext.getCurrentInstance().addMessage(null, msg);
            try {
				copyFile(nomeNovo, event.getFile().getInputstream());
				bacia.setConfigRios(true);
				baciaDao.saveOrUpdate(bacia);
				riosKMLDao.carregarRios(bacia);
			} catch (IOException e) {
				e.printStackTrace();
			}
    }
    
    public void uploadSecoes(FileUploadEvent event) {  
        fileSecoes = event.getFile();
        String nomeNovo = bacia.getNome() + "_Secoes.kml";
		FacesMessage msg = new FacesMessage("Arquivo", fileSecoes.getFileName() + " foi carregado.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        try {
			copyFile(nomeNovo, event.getFile().getInputstream());
			bacia.setConfigSecoes(true);
			baciaDao.saveOrUpdate(bacia);
			secoesKMLDao.carregarSecoes(bacia);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void uploadHms(FileUploadEvent event) {  
        fileHms = event.getFile();
		FacesMessage msg = new FacesMessage("Arquivo", fileHms.getFileName() + " foi carregado.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        try {
			copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
			String nomeArquivo = Paths.get(pathModelos, bacia.getNome().toUpperCase(), event.getFile().getFileName()).toString();
			String nomeDir = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC").toString();
			unziper.unZipIt(nomeArquivo, nomeDir);
			bacia.setConfigHms(true);
			baciaDao.saveOrUpdate(bacia);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public void uploadRas(FileUploadEvent event) {  
        fileRas = event.getFile();
		FacesMessage msg = new FacesMessage("Arquivo", fileRas.getFileName() + " foi carregado.");  
        FacesContext.getCurrentInstance().addMessage(null, msg);
        try {
			copyFile(event.getFile().getFileName(), event.getFile().getInputstream());
			String nomeArquivo = Paths.get(pathModelos, bacia.getNome().toUpperCase(), event.getFile().getFileName()).toString();
			String nomeDir = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC").toString();
			unziper.unZipIt(nomeArquivo, nomeDir);
			bacia.setConfigRas(true);
			baciaDao.saveOrUpdate(bacia);
			// Quando subir os arquivos do modelo RAS de uma bacia carregar os dados dos Rios, Trechos e seções da bacia, isso serve para ter os dados de simulação
			rasWrapper.InicializarRios(bacia);
			
		} catch (IOException e) {
			e.printStackTrace();
		} 
    }
    
    public void copyFile(String fileName, InputStream in) {
    	
    	try {
			
    		//Verifica se existe a pasta para a bacia
    		String diretorio = Paths.get(pathModelos, bacia.getNome().toUpperCase()).toString();
    		File dir = new File(diretorio);
    		if (!dir.exists()){
    			dir = new File(diretorio);
    			dir.mkdir();
    			dir = new File(dir, "HEC");
    			dir.mkdir();
    			new File(dir, "tmpHMS").mkdir();
    			new File(dir, "tmpRAS").mkdir();
    			new File(dir, "HMS").mkdir();
    			new File(dir, "RAS").mkdir();
    		}
    		
    		String nomeArquivo = Paths.get(pathModelos, bacia.getNome().toUpperCase(), fileName).toString();
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
    
    public List<Estacao> getEstacoes(){
    	return estacaoDao.listEstacoesANA(bacia);
    }

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public void setBacias(List<Bacia> bacias) {
		this.bacias = bacias;
	}

	public UploadedFile getFileSecoes() {
		return fileSecoes;
	}

	public void setFileSecoes(UploadedFile fileSecoes) {
		this.fileSecoes = fileSecoes;
	}

	public UploadedFile getFileHms() {
		return fileHms;
	}

	public void setFileHms(UploadedFile fileHms) {
		this.fileHms = fileHms;
	}

	public UploadedFile getFileRas() {
		return fileRas;
	}

	public void setFileRas(UploadedFile fileRas) {
		this.fileRas = fileRas;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		String login = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		usuario = usuarioDao.findByLogin(login);	
		workspace.setUsuario(usuario);
		
	}
	
	public String onRowEdit(RowEditEvent event) {  
        
    	Trecho trecho = (Trecho) event.getObject();
    	trechoDao.saveOrUpdate(trecho);
    	trechos=null;
    	FacesMessage msg = new FacesMessage("Trecho Editado", ((Trecho) event.getObject()).getNome());  
        FacesContext.getCurrentInstance().addMessage(null, msg); 
        
        return "configurarRAS";
    }
	
	public String voltarConf(){
		trechos = null;
		return "configuracaoBacias";
	}
    
	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public List<Trecho> getTrechos() {
		
		if (bacia.getId()!=null && trechos==null){
    		List<String> nomes = workspace.getNomesTrechosRAS(bacia);
        	
        	trechos = new ArrayList<Trecho>();
        	
        	if (nomes != null && nomes.size() >= 0){
        		for (String nome: nomes ){
        			Trecho trecho = trechoDao.findByNome(nome.toUpperCase());
        			if (!trechos.contains(trecho)){
        				trechos.add(trecho);
        			}
        		}
        	}
    	}
		
		return trechos;
	}

	public void setTrechos(List<Trecho> trechos) {
		this.trechos = trechos;
	}

}
