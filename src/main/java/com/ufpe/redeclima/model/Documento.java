package com.ufpe.redeclima.model;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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

/**
 * @author edwardtz
 *
 */
@Entity
@Table(name="t_documento")
public class Documento implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_documento")
	private Long id;
	
	@Column(name="nome", unique=true, nullable=false)
	private String nome;
	
	@Column(name="data_atualizacao", nullable=false)
	private Date dataAtualizacao;
	
	@Column(name="versao")
	private Integer versao=0;
	
	@Column(name="descricao")
	private String descricao;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Integer getVersao() {
		return versao;
	}

	public void setVersao(Integer versao) {
		this.versao = versao;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Date getDataAtualizacao() {
		return dataAtualizacao;
	}

	public void setDataAtualizacao(Date dataAtualizacao) {
		this.dataAtualizacao = dataAtualizacao;
	}
	
	
	
}
