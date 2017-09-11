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
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;



/** 
 * Esta classe representa a entidade usuario do sistema 
 * */
@Entity
@Table(name="t_usuario")
public class Usuario implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_usuario", unique=true, nullable=false)
	private Long id;
	
	/* Nome do usuario ou login */
	@Column(name="login", unique=true, nullable=false)
	private String login;
	
	/* Password do usuario */
	@Column(name="password", nullable=false)
	private String password;
	
	/* E-Mail do registro do usuario */
	@Column(name="email", nullable=false)
	private String email;
	
	/* Indicador de se o usuario esta ativo ou não */
	@Column(name="ativo")
	private boolean ativo;
	
	/* Perfil que tem associado o usuario */
	@JoinColumn(name = "id_perfil", nullable = false)
    @ManyToOne(optional = false)
    private Perfil perfil;
	
	/* Nome do usuario */
	@Column(name="nome", nullable=false)
	private String nome;
	
	/* Sobre nome do usuario */
	@Column(name="sobreNome", nullable=false)
	private String sobreNome;
	
	/* Telefone de contato de usuario */
	@Column(name="telefone", nullable=false)
	private String telefone;
	
	/* Tipo de notificação */
	@ManyToMany
	@JoinTable(name="t_usuario_tipo_notificacao_map",
			   joinColumns={@JoinColumn(name="usuario_id", referencedColumnName="id_usuario")}, 
               inverseJoinColumns={@JoinColumn(name="tipo_notificacao_id", referencedColumnName="id")}) 
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<TipoNotificacao> notificacoes;
	
	public void setId(Long id) {
		this.id = id;
	}
	public Long getId() {
		return id;
	}

	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getEmail() {
		return email;
	}

	public Perfil getPerfil() {
		return perfil;
	}

	public void setPerfil(Perfil perfil) {
		this.perfil = perfil;
	}

	public boolean isAtivo() {
		return ativo;
	}

	public void setAtivo(boolean ativo) {
		this.ativo = ativo;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getSobreNome() {
		return sobreNome;
	}
	public void setSobreNome(String sobreNome) {
		this.sobreNome = sobreNome;
	}
	public String getTelefone() {
		return telefone;
	}
	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
	public List<TipoNotificacao> getNotificacoes() {
		return notificacoes;
	}
	public void setNotificacoes(List<TipoNotificacao> notificacoes) {
		this.notificacoes = notificacoes;
	}
	
}	
