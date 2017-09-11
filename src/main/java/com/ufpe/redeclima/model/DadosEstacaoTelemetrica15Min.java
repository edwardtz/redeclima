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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15MinId;

/**
 * Esta classe representa a entidade que armazena as medições de um conjunto de indicadores emitidos pelas estações telemétricas em períodos de 15 minutos,
 * esses indicadores podem ser tais como nível, vazão, chuva, etc
 * Cada uma de estas medições está associada a uma estação telemétrica em um determinado tempo
 * */
@Entity
@Table(name="t_dados_estacao_telemetrica_15_min")
@NamedQuery(name="DadosEstacaoTelemetrica15Min.findByEstacaoIdAndData", query = "select d from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId and d.Id.data = :data")
public class DadosEstacaoTelemetrica15Min {
	
	@EmbeddedId DadosEstacaoTelemetrica15MinId Id;
	
	/* Medição de chuva em mm */
	@Column(name="chuva")
	private double chuva;
	
	/* Medição de vazão em metros cúbicos x segundo mts3/seg */
	@Column(name="vazao")
	private double vazao;
	
	/* Medição de nivel em metros mts */
	@Column(name="nivel")
	private double nivel;
	
	@ManyToOne @MapsId(value="id") Estacao estacao;

	public DadosEstacaoTelemetrica15MinId getId() {
		return Id;
	}

	public void setId(DadosEstacaoTelemetrica15MinId id) {
		Id = id;
	}

	public double getChuva() {
		return chuva;
	}

	public void setChuva(double chuva) {
		this.chuva = chuva;
	}

	public double getVazao() {
		return vazao;
	}

	public void setVazao(double vazao) {
		this.vazao = vazao;
	}

	public double getNivel() {
		return nivel;
	}

	public void setNivel(double nivel) {
		this.nivel = nivel;
	}

	public Estacao getEstacao() {
		return estacao;
	}

	public void setEstacao(Estacao estacao) {
		this.estacao = estacao;
	}

	@Override
    public int hashCode() {
        int hash = 0;

        hash += ((Id != null)
                ? Id.hashCode()
                : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DadosEstacaoTelemetrica15Min)) {
            return false;
        }

        DadosEstacaoTelemetrica15Min other = (DadosEstacaoTelemetrica15Min) object;

        if (((this.Id == null) && (other.Id != null)) || ((this.Id != null) && !this.Id.equals(other.Id))) {
            return false;
        }

        return true;
    }
	
	
}

