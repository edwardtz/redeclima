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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import com.ufpe.redeclima.util.EnumUnidadeTempo;


/** 
 * Esta classe representa a entidade que armazena os dados das grades utilizadas para coleita de dados e execução da simulação, os dados das grades são enviados pela CPTEC 
 * e cobrem pontos na área da bacia o área de estudo, a característica da grade esta definida pelas coordenadas de latitude e longitude do ponto inicial e uma variável passo que
 * indica una variação em graus entre cada linha da grade, junto com a quantidade de pontos em cada eixo determinam a área total da grade
 *   */
@Entity
@Table(name="t_grade")
public class Grade implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_grade", unique=true, nullable=false)
	private Long id;
	
	/* Nome que identifica a grade, em geral coincide com o prefixo do nome do arquivo */
	@Column(name="nome", unique=true, nullable=false)
	private String nome;
	
	/* Indica o tipo da grade, o tipo da grade este caracterizado pela variação da variavel de passo, a distancia entre as linhas da grade */
	@Column(name="tipo_grade", nullable=false)
	private Integer tipoGrade;
	
	/* Latitude do ponto inicial da grade */
	@Column(name="latitude_inicial", nullable=false)
	private Double latitudeInicial;
	
	/* Longitude do ponto inicial da grade */
	@Column(name="longitude_inicial", nullable=false)
	private Double longitudeInicial;
	
	/* Variavel que indica a variação entre cada pondo da grade */
	@Column(name="passo", nullable=false)
	private Double passo;
	
	/* Path remoto do servidor ftp no qual se deverão pegar os arquivos de grib correspondentes a esta grade */
	@Column(name="remote_path_ftp", nullable=false)
	private String remotePathFtp;
	
	/* Indicador de se se deverão o nçao atualizar os dados para esta grade  */
	@Column(name="busca_ativa", nullable=false)
	private boolean buscaAtiva;
	
	/* Pontos da grade */
	@OneToMany(mappedBy = "grade", fetch=FetchType.EAGER)
	private Set<PontoGrade> pontosGrade;
	
	/* Asociação entre grades e areas de recorte */
	@ManyToMany
    @JoinTable(name="t_grade_area_recorte_map", 
                   joinColumns={@JoinColumn(name="grade_id", referencedColumnName="id_grade")}, 
                   inverseJoinColumns={@JoinColumn(name="area_recorte_id", referencedColumnName="id_area_recorte")})
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<AreaRecorte> areasRecorte;
	
	/* Quantidade de tempo associado ao periodo em que se obtem os dados de previsao desta grade, tem grades que os dados são 1 hora, outras de tres horas, outras diarias, etc */
	@Column(name="quantidade_tempo_periodo", nullable=false)
	private Integer quantidadeTempoPeriodo;
	
	/* Unidade de tempo do campo quantidadeTempoPeriodo */
	@Column(name="unidade_tempo_periodo", nullable=false)
	private Integer unidadeTempoPeriodo;
	
	/* Quantidade de pontos de grade no eixo de latitude a partir da coordenada inicial */
	@Column(name="numero_pontos_latitude")
	private Integer numeroPontosLatitude;
	
	/* Quantidade de pontos de grade no eixo de longitude a partir da coordenada inicial */
	@Column(name="numero_pontos_longitude")
	private Integer numeroPontosLongitude;
	
	/** 
	 * Este metodo verifica se um ponto esta conteudo em alguma das áreas de recorte associadas à grade
	 * @param ponto ponto de grade
	 * */
	public boolean estaConteudoEmAreaRecorte(PontoGrade ponto){
		return estaConteudoEmAreaRecorte(ponto.getLongitude(), ponto.getLatitude()); 
	}
	
	/** 
	 * Este metodo verifica se um par de coordenadas longitude latitude estão conteudas em alguma das áreas de recorte associadas à grade
	 * @param longitude coordenada longitude
	 * @param latitude coordenada latitude
	 * */
	public boolean estaConteudoEmAreaRecorte(double longitude, double latitude){
		if(areasRecorte!=null){
			for(AreaRecorte area: areasRecorte){
				if(area.estaConteudo(longitude, latitude)){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Retorna a descrição da unidade de tempo com a qual foi configurada a grade
	 * */
	public String getUnidadeTempoDescricao(){
		
		if (unidadeTempoPeriodo == EnumUnidadeTempo.SEGUNDO.getId()){
			return EnumUnidadeTempo.SEGUNDO.getDescricao();
		} else if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			return EnumUnidadeTempo.MINUTO.getDescricao();
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){
			return EnumUnidadeTempo.HORA.getDescricao();
		} else {
			return EnumUnidadeTempo.DIA.getDescricao();
		}
	}
	
	/**
	 * Retorna as unidades de tempo posiveis para simulação
	 * TODO adicionar este metodo como interface
	 * */
	public EnumUnidadeTempo[] getUnidadesTempoSimulacao(){
		
		EnumUnidadeTempo[] unidades = new EnumUnidadeTempo[2];
		
		if (unidadeTempoPeriodo == EnumUnidadeTempo.SEGUNDO.getId()){
			unidades[0] = EnumUnidadeTempo.SEGUNDO;
		} else if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			unidades[0] = EnumUnidadeTempo.MINUTO;
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){
			unidades[0] = EnumUnidadeTempo.HORA;
		}
		unidades[1] = EnumUnidadeTempo.DIA;
		
		return unidades;
	}
	
	/**
	 * Retorna a unidade de tempo da grade
	 * TODO adicionar este metodo como interface
	 * */
	public EnumUnidadeTempo getUnidadeTempo(){
		
		
		if (unidadeTempoPeriodo == EnumUnidadeTempo.SEGUNDO.getId()){
			return  EnumUnidadeTempo.SEGUNDO;
		} else if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			return EnumUnidadeTempo.MINUTO;
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){
			return EnumUnidadeTempo.HORA;
		}
		return EnumUnidadeTempo.DIA;
		
	}
	
	
	/**
	 * Este metodo retorna o ultimo time series da grade dependendo do periodo configurado. Os dados da grade tem un periodo, exemplo 3 hs, então cada 3hs se tem um valor
	 * de previsão correspondente nessa grade, o ultimo time series para uma grade de 3 hs seria 21 hs, 0 minutos, 0 segundos (21:00:00).
	 * Para uma grade configurada com periodo de 15 minutos, o ultimo time series seria 23 horas, 45 minutos, 0 segundos (23:45:00)
	 * Em geral se a grade esta em dias então o time series é 24:00:00
	 *          se a grade esta em horas então o time series é (24-Qh):0:0, Qh: quantidade de horas
	 *          se a grade esta em minutos então o time series é 23:(60-Qm):0, Qm: quantidade de minutos
	 * Esta informação e usada pelo procedimento de simulação para configurar os arquivos de plan (.pNN) e os arquivos de projeto (.prj) do modelo RAS
	 * 
	 * @param dataFinal data final pasada como parametro do procedimento de simulação          
	 * */
	public Date obterUltimoTempoSerie(Date dataFinal){
	
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(dataFinal);
		
        if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			
			calendario.set(Calendar.HOUR_OF_DAY, 23);
			calendario.set(Calendar.MINUTE, 60 - quantidadeTempoPeriodo);
			calendario.set(Calendar.SECOND, 0);
			return calendario.getTime();
			
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){

			calendario.set(Calendar.HOUR_OF_DAY, 24 - quantidadeTempoPeriodo);
			calendario.set(Calendar.MINUTE, 0);
			calendario.set(Calendar.SECOND, 0);
			return calendario.getTime();
			
		}
		
		calendario.set(Calendar.HOUR_OF_DAY, 24);
		calendario.set(Calendar.MINUTE, 0);
		calendario.set(Calendar.SECOND, 0);
		return calendario.getTime();
		
	}
	
	/**
	 * Este metodo retorna o seguinte time series da grade dependendo do periodo configurado. Os dados da grade tem un periodo, exemplo 3 hs, então cada 3hs se tem um valor
	 * de previsão correspondente nessa grade, o seguinte time series para uma grade de 3 hs, para a data 23APR2013 18:00:00 seria 23APR2013 21:00:00.
	 * Para uma grade configurada com periodo de 15 minutos, o seguinte time series, para a data 23APR2013 18:00:00 seria 23APR2013 18:15:00
	 * 
	 * @param data data da qual se quer calcular o seguinte time series          
	 * */
	public Date obterSeguinteTempoSerie(Date data){
	
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(data);
		
        if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			
			calendario.add(Calendar.MINUTE, quantidadeTempoPeriodo);
			return calendario.getTime();
			
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){

			calendario.add(Calendar.HOUR_OF_DAY, quantidadeTempoPeriodo);
			return calendario.getTime();
			
		}
		
		calendario.add(Calendar.DAY_OF_MONTH, 1);
		return calendario.getTime();
		
	}
	
	/**
	 * Este metodo retorna o anterior time series da grade dependendo do periodo configurado. Os dados da grade tem un periodo, exemplo 3 hs, então cada 3hs se tem um valor
	 * de previsão correspondente nessa grade, o seguinte time series para uma grade de 3 hs, para a data 23APR2013 18:00:00 seria 23APR2013 15:00:00.
	 * Para uma grade configurada com periodo de 15 minutos, o seguinte time series, para a data 23APR2013 18:00:00 seria 23APR2013 17:45:00
	 * 
	 * @param data data da qual se quer calcular o anterior time series          
	 * */
	public Date obterAnteriorTempoSerie(Date data){
	
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(data);
		
        if (unidadeTempoPeriodo == EnumUnidadeTempo.MINUTO.getId()){
			
			calendario.add(Calendar.MINUTE, -quantidadeTempoPeriodo);
			return calendario.getTime();
			
		}else if (unidadeTempoPeriodo == EnumUnidadeTempo.HORA.getId()){

			calendario.add(Calendar.HOUR_OF_DAY, -quantidadeTempoPeriodo);
			return calendario.getTime();
			
		}
		
		calendario.add(Calendar.DAY_OF_MONTH, -1);
		return calendario.getTime();
		
	}
	
	
	public Set<PontoGrade> getPontosGrade() {
		return pontosGrade;
	}

	public void setPontosGrade(Set<PontoGrade> pontosGrade) {
		this.pontosGrade = pontosGrade;
	}

	public Grade(){
		this.buscaAtiva=true;
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

	public Integer getTipoGrade() {
		return tipoGrade;
	}

	public void setTipoGrade(Integer tipoGrade) {
		this.tipoGrade = tipoGrade;
	}

	public Double getLatitudeInicial() {
		return latitudeInicial;
	}

	public void setLatitudeInicial(Double latitudeInicial) {
		this.latitudeInicial = latitudeInicial;
	}

	public Double getLongitudeInicial() {
		return longitudeInicial;
	}

	public void setLongitudeInicial(Double longitudeInicial) {
		this.longitudeInicial = longitudeInicial;
	}

	public Double getPasso() {
		return passo;
	}

	public void setPasso(Double passo) {
		this.passo = passo;
	}
	
	public String getRemotePathFtp() {
		return remotePathFtp;
	}

	public void setRemotePathFtp(String remotePathFtp) {
		this.remotePathFtp = remotePathFtp;
	}

	public boolean isBuscaAtiva() {
		return buscaAtiva;
	}
	
	public boolean getBuscaAtiva() {
		return buscaAtiva;
	}

	public void setBuscaAtiva(boolean buscaAtiva) {
		this.buscaAtiva = buscaAtiva;
	}
	
	public List<AreaRecorte> getAreasRecorte() {
		return areasRecorte;
	}

	public void setAreasRecorte(List<AreaRecorte> areasRecorte) {
		this.areasRecorte = areasRecorte;
	}
	
	public Integer getQuantidadeTempoPeriodo() {
		return quantidadeTempoPeriodo;
	}

	public void setQuantidadeTempoPeriodo(Integer quantidadeTempoPeriodo) {
		this.quantidadeTempoPeriodo = quantidadeTempoPeriodo;
	}

	public Integer getUnidadeTempoPeriodo() {
		return unidadeTempoPeriodo;
	}

	public void setUnidadeTempoPeriodo(Integer unidadeTempoPeriodo) {
		this.unidadeTempoPeriodo = unidadeTempoPeriodo;
	}

	public Integer getNumeroPontosLatitude() {
		return numeroPontosLatitude;
	}

	public void setNumeroPontosLatitude(Integer numeroPontosLatitude) {
		this.numeroPontosLatitude = numeroPontosLatitude;
	}

	public Integer getNumeroPontosLongitude() {
		return numeroPontosLongitude;
	}

	public void setNumeroPontosLongitude(Integer numeroPontosLongitude) {
		this.numeroPontosLongitude = numeroPontosLongitude;
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
        if (!(object instanceof Grade)) {
            return false;
        }

        Grade other = (Grade) object;

        if (((this.id == null) && (other.id != null)) || ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }
	
}
