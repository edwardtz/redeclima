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
package com.ufpe.redeclima.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Embeddable;

@Embeddable
public class DadosEstacaoTelemetrica15MinId implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private Long id;
	
	private Date data;
	
	public DadosEstacaoTelemetrica15MinId(){
		
	}
	
	public DadosEstacaoTelemetrica15MinId(Long estacaoId, Date data){
		this.id = estacaoId;
		this.data = data;
	}
	
	@Override
    public boolean equals(Object obj) {
        if(obj instanceof DadosEstacaoTelemetrica15MinId){
        	
        	DadosEstacaoTelemetrica15MinId chuvaPK = (DadosEstacaoTelemetrica15MinId) obj;
 
            if(!chuvaPK.getId().equals(id)){
            	return false;
            }
 
            if(chuvaPK.getData().compareTo(data)!=0){
            	return false;
            }
 
            return true;
        }
 
        return false;
    }
 
    @Override
    public int hashCode() {
        return id.hashCode() + data.hashCode();
    }

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
}