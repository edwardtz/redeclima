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
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Esta classe representa a entidade que armazena os dados de uma Bacia hidrológica, esta definida por um nome e dois pontos P1(longitudeX1,latitudeY1) e P2(longitudeX2,latitudeX2).
 * Cada ponto definido pelas coordenadas em formato decimais correspondentes a longitude e latitude, esses dois pontos definem um retângulo correspondente 
 * a área aproximada que delimita a bacia
 * */
@Entity
@Table(name="t_bacia")
public class Bacia implements Serializable{
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_bacia", unique=true, nullable=false)
	private Long id;
	
	@Column(name="nome", unique=true, nullable=false)
	private String nome;
	
	@Column(name="x1", nullable=false)
	private Double longitudeX1;
	
	@Column(name="y1", nullable=false)
	private Double latitudeY1;
	
	@Column(name="x2", nullable=false)
	private Double longitudeX2;
	
	@Column(name="y2", nullable=false)
	private Double latitudeY2;
	
	@Column(name="config_rios")
	private boolean configRios=false;
	
	@Column(name="config_secoes")
	private boolean configSecoes=false;
	
	@Column(name="config_hms")
	private boolean configHms=false;
	
	@Column(name="configRas")
	private boolean configRas=false;
	
	@OneToMany(mappedBy="bacia", targetEntity=Estacao.class)
    private Collection<Estacao> estacoes;
	
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

	public Collection<Estacao> getEstacoes() {
		return estacoes;
	}

	public void setEstacoes(Collection<Estacao> estacoes) {
		this.estacoes = estacoes;
	}

	public Double getLongitudeX1() {
		return longitudeX1;
	}

	public void setLongitudeX1(Double longitudeX1) {
		this.longitudeX1 = longitudeX1;
	}

	public Double getLatitudeY1() {
		return latitudeY1;
	}

	public void setLatitudeY1(Double latitudeY1) {
		this.latitudeY1 = latitudeY1;
	}

	public Double getLongitudeX2() {
		return longitudeX2;
	}

	public void setLongitudeX2(Double longitudeX2) {
		this.longitudeX2 = longitudeX2;
	}

	public Double getLatitudeY2() {
		return latitudeY2;
	}

	public void setLatitudeY2(Double latitudeY2) {
		this.latitudeY2 = latitudeY2;
	}
	
	public boolean isConfigRios() {
		return configRios;
	}
	
	public boolean getConfigRios() {
		return configRios;
	}
	
	public boolean isAllConfig(){
		return isConfigRios() && isConfigSecoes() && isConfigHms() && isConfigRas();
	}
	
	public boolean getIsAllConfig(){
		return isAllConfig();
	}

	public void setConfigRios(boolean configRios) {
		this.configRios = configRios;
	}

	public boolean isConfigSecoes() {
		return configSecoes;
	}
	
	public boolean getConfigSecoes() {
		return configSecoes;
	}

	public void setConfigSecoes(boolean configSecoes) {
		this.configSecoes = configSecoes;
	}

	public boolean isConfigHms() {
		return configHms;
	}
	
	public boolean getConfigHms() {
		return configHms;
	}

	public void setConfigHms(boolean configHms) {
		this.configHms = configHms;
	}

	public boolean isConfigRas() {
		return configRas;
	}
	
	public boolean getConfigRas() {
		return configRas;
	}

	public void setConfigRas(boolean configRas) {
		this.configRas = configRas;
	}

	
	@Override
    public int hashCode() {
        int hash = 0;

        hash += ((id != null)
                ? id.hashCode()
                : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Bacia)) {
            return false;
        }

        Bacia other = (Bacia) object;

        if (((this.id == null) && (other.id != null)) || ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }

}
