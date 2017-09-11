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
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

/**
 * @author edwardtz
 * Esta classe representa a entidade que armazena os dados dos trechos de um rio
 */
@Entity
@Table(name="t_trecho", uniqueConstraints=@UniqueConstraint(columnNames={"rio_id", "nome"}))
public class Trecho implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_trecho")
	private Long id;
	
	/* Nome do trecho */
	@Column(name="nome", nullable=false, unique=true)
	private String nome;
	
	/* Rio ao qual pertence */
	@ManyToOne
	@JoinColumn(name="rio_id", nullable=false)
	private Rio rio;
	
	/* Secoes do configuradas no trecho */
	@OneToMany(mappedBy="trecho", targetEntity=Secao.class)
	@LazyCollection(LazyCollectionOption.TRUE)
    private Collection<Secao> secoes;
	
	@Column(name="vazao_minima")
	private Double vazaoMinima;
	
	/* Estação de referencia para coleita de condições iniciais do trecho */
	@ManyToOne
	@JoinColumn(name="estacao_id")
	private Estacao estacaoRef;

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

	public Collection<Secao> getSecoes() {
		return secoes;
	}

	public void setSecoes(Collection<Secao> secoes) {
		this.secoes = secoes;
	}

	public Rio getRio() {
		return rio;
	}

	public void setRio(Rio rio) {
		this.rio = rio;
	}

	public Estacao getEstacaoRef() {
		return estacaoRef;
	}

	public void setEstacaoRef(Estacao estacaoRef) {
		this.estacaoRef = estacaoRef;
	}

	public Double getVazaoMinima() {
		return vazaoMinima;
	}

	public void setVazaoMinima(Double vazaoMinima) {
		this.vazaoMinima = vazaoMinima;
	}
	
}
