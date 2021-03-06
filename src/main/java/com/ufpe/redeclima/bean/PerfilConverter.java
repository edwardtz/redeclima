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
package com.ufpe.redeclima.bean;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.PerfilDao;
import com.ufpe.redeclima.model.Perfil;

@Component("perfilConverter")
public class PerfilConverter implements Converter {
 
	@Autowired
	private PerfilDao perfilDao;
	
	Long getId(String value) {
        Long id;
        id = Long.valueOf(value);
        return id;
    }
	
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		return perfilDao.findById(getId(value));
	}

	public String getAsString(FacesContext arg0, UIComponent arg1, Object object) {
		if (object == null) {
            return null;
        }

        if (object instanceof Perfil) {
            Perfil p = (Perfil) object;
            return Long.toString(p.getId());
        } else {
            throw new IllegalArgumentException("objeto " + object + " possui o tipo " + object.getClass().getName()
                    + "; tipo esperado: " + Perfil.class.getName());
        }
	}
	
}