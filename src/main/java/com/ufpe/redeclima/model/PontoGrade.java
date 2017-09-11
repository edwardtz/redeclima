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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.ufpe.redeclima.interfaces.PontoDado;

/** 
 * Esta classe representa a entidade que armazena os dados que definem os pontos da grade, um pont de grade esta defiido pelas coordenadas latitude e longitude 
 * e pela a qual grade que pertece 
 * */
@Entity
@Table(name="t_ponto_grade" , uniqueConstraints=@UniqueConstraint(columnNames={"latitude", "longitude", "grade_id"}))
public class PontoGrade implements Serializable, PontoDado {
	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_ponto_grade", nullable=false)
	private Long id;
	
	/* Latitude em formato de graus decimales do ponto de grade */
	@Column(name="latitude", nullable=false)
	private Double latitude;
	
	/* Longitude em formato de graus decimales do pnto de grade */
	@Column(name="longitude", nullable=false)
	private Double longitude;
	
	/* Altitude em formato de graus decimales do pnto de grade */
	@Column(name="altitude")
	private Double altitude;
	
	/* Grade a qual pertece o ponto */
	@ManyToOne (cascade=CascadeType.DETACH, fetch=FetchType.EAGER)
	@JoinColumn(name="grade_id")
	private Grade grade;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}
	
	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.PontoDado#getGageId()
	 */
	public String getGageId() {
		return "Ponto_" + id;
	}
	
	
	@Override
    public int hashCode() {
        int hash = 0;

        hash += ((id!=null && latitude != null && longitude !=null)
                ? id.hashCode() + latitude.hashCode() + longitude.hashCode() + grade.hashCode()
                : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {

        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PontoGrade)) {
            return false;
        }

        PontoGrade other = (PontoGrade) object;

        if (((this.id ==null) && (other.id!=null)) ||
        	((this.longitude == null) && (other.longitude != null)) || 
        	((this.latitude == null) && (other.latitude != null)) ||
        	((this.grade == null) && (other.grade !=null)) ||
        	((this.id != null) && !id.equals(other.id)) ||
        	((this.longitude != null) && !this.longitude.equals(other.longitude)) || 
        	((this.latitude != null) && !this.latitude.equals(other.latitude)) ||
        	((this.grade != null) && (!this.grade.equals(other.grade)))) {
            return false;
        }

        return true;
    }

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.PontoDado#getAltitude()
	 */
	public Double getAltitude() {
		return altitude;
	}

}
