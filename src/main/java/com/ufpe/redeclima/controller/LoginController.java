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

import javax.faces.bean.SessionScoped;

import org.springframework.context.annotation.Scope;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.model.Usuario;

/**
 * @author edwardtz
 *
 */
@Controller("loginController")
@Scope("session")
public class LoginController {

	private Usuario usuario;
	 
    public LoginController() {
        usuario = new Usuario();
        SecurityContext context = SecurityContextHolder.getContext();
        if (context instanceof SecurityContext){
            Authentication authentication = context.getAuthentication();
            if (authentication instanceof Authentication){
                usuario.setLogin(((User)authentication.getPrincipal()).getUsername());
            }
        }
    }
 
    public Usuario getUsuario() {
    		usuario = new Usuario();
            SecurityContext context = SecurityContextHolder.getContext();
            if (context instanceof SecurityContext){
                Authentication authentication = context.getAuthentication();
                if (authentication instanceof Authentication){
                    usuario.setLogin(((User)authentication.getPrincipal()).getUsername());
                }
            }
    	return usuario;
    }
 
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

	
	
}
