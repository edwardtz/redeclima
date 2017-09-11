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

import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.bean.Workspace;
import com.ufpe.redeclima.dao.PerfilDao;
import com.ufpe.redeclima.dao.TipoNotificacaoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.model.Perfil;
import com.ufpe.redeclima.model.TipoNotificacao;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumPerfil;

@Controller
@Scope("session")
public class UsuarioController implements InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(UsuarioController.class);
	
	private Usuario usuario;
	
	private List<Usuario> usuarios;
	
	private List<TipoNotificacao> tiposSelected;
	
    private SelectItem[] perfiles;

	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private PerfilDao perfilDao;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Workspace workspace;
	
	@Autowired
	private TipoNotificacaoDao tipoNotificacaoDao;
	
	public UsuarioController(){
		perfiles = createPerfilOption();
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		usuario = new Usuario();
		usuario.setAtivo(true);
	}
	
	public Usuario getUsuario() {
		return usuario;
	}
	
	public List<Perfil> getPerfiles(){
		return perfilDao.list();
	}

	public String save() {
		usuario.setNotificacoes(tiposSelected);
		usuarioDao.saveOrUpdate(usuario);
		workspace.setUsuario(usuario);
		workspace.init();
		usuario = new Usuario();
		invalidateUsuarios();
		return "listaUsuarios";
	}
	
	public String adicionarUsuario(){
		usuario = new Usuario();
		usuario.setAtivo(true);
		return "adicionarUsuarioView";
	}
	
	public void excluirUsuario(){
		workspace.setUsuario(usuario);
		workspace.apagar();
		usuarioDao.remove(usuario);
		invalidateUsuarios();
	}
	
	public void validateEmail(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
			String emailStr = (String) value;
			if (-1 == emailStr.indexOf("@")) {
				FacesMessage message = new FacesMessage("E-mail invalido");
				throw new ValidatorException(message);
			}
	}
	
	private void invalidateUsuarios() {
		usuarios = null;
	}

	public List<Usuario> getUsuarios() {
		if (usuarios == null) {
			usuarios = usuarioDao.list();
		}
		return usuarios;
		
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public void setUsuarios(List<Usuario> usuarios) {
		this.usuarios = usuarios;
	}

	public String[] getPerfilesDescricao(){
		return EnumPerfil.getDescricoes();
	}
	
	private SelectItem[] createPerfilOption()  {  
        SelectItem[] options = new SelectItem[EnumPerfil.getDescricoes().length + 1];  
        String[] perfilesDesc = EnumPerfil.getDescricoes();
        options[0] = new SelectItem("", "Selecionar");  
        for(int i = 0; i < perfilesDesc.length; i++) {  
            options[i + 1] = new SelectItem(perfilesDesc[i], perfilesDesc[i]);  
        }  
        return options;  
    }  
  
    public SelectItem[] getPerfilesDesc() {  
        return perfiles;  
    } 
    
    public SelectItem[] getEstadosActivado(){
    	
    	SelectItem[] estados = new SelectItem[3];
    	estados[0] = new SelectItem(null,  "Selecionar");
    	estados[1] = new SelectItem(true,  "true");
    	estados[2] = new SelectItem(false, "false");
    	return estados;
    }
	
	public List<TipoNotificacao> getTiposSelected() {
		tiposSelected = new ArrayList<TipoNotificacao>();
		if (usuario.getId() != null && usuario.getNotificacoes()!=null && !usuario.getNotificacoes().isEmpty()){
			tiposSelected.addAll(usuario.getNotificacoes());
		}
		return tiposSelected;
	}

	public void setTiposSelected(List<TipoNotificacao> tiposSelected) {
		this.tiposSelected = tiposSelected;
	}

	public List<TipoNotificacao> getTiposNotificacao(){
		return tipoNotificacaoDao.list();
	}
}
