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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="t_estacao_tipo_map")
public class EstacaoTipoMap implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="estacoes_id_estacao")
	private Long estacaoId;
	
	@Id
	@Column(name="tipos_id")
	private Long tipoEstacaoId;

	public Long getEstacaoId() {
		return estacaoId;
	}

	public void setEstacaoId(Long estacaoId) {
		this.estacaoId = estacaoId;
	}

	public Long getTipoEstacaoId() {
		return tipoEstacaoId;
	}

	public void setTipoEstacaoId(Long tipoEstacaoId) {
		this.tipoEstacaoId = tipoEstacaoId;
	}
	
	
	
	

}
