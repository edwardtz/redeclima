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
import javax.persistence.Table;

/**
 * @author edwardtz
 * Esta classe representa uma area de recorte, definida por dos pontos nas coordenadas de longitude e latitude, periodicamente se atualizarão os dados 
 * de previsão que se encontrem conteudos dentro da area de recorte
 */
@Entity
@Table(name="t_area_recorte")
public class AreaRecorte implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_area_recorte", unique=true, nullable=false)
	private Long id;
	
	@Column(name="nome", unique=true, nullable=false)
	private String nome;
	
	@Column(name="longitudeX1", nullable=false)
	private Double longitudeX1;
	
	@Column(name="latitudeY1", nullable=false)
	private Double latitudeY1;
	
	@Column(name="longitudeX2", nullable=false)
	private Double longitudeX2;
	
	@Column(name="latitudeY2", nullable=false)
	private Double latitudeY2;
	
	@ManyToMany
	@JoinTable(name="t_grade_area_recorte_map", 
    				joinColumns={@JoinColumn(name="area_recorte_id", referencedColumnName="id_area_recorte")}, 
    				inverseJoinColumns={@JoinColumn(name="grade_id", referencedColumnName="id_grade")})
	private List<Grade> grades;

	
	/**
	 * Este metodo verifica se um par coordenadas longitude e latitude esta conteudo na area de recorte
	 * @param longitude coordenada logitude
	 * @param latitude coordenada latitude
	 * */
	public boolean estaConteudo(double longitude, double latitude){
		if (latitudeY1 >= latitude && latitude >= latitudeY2 && longitudeX1 <= longitude && longitude <= longitudeX2){
			return true;
		}
		return false;
	}
	
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

	public List<Grade> getGrades() {
		return grades;
	}

	public void setGrades(List<Grade> grades) {
		this.grades = grades;
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
        if (!(object instanceof AreaRecorte)) {
            return false;
        }

        AreaRecorte other = (AreaRecorte) object;

        if (((this.id == null) && (other.id != null)) || ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }
	
}
