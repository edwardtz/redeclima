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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Esta classe de utiliza para armazenar o registro das actividades de download e processamento dos arquivos de dados das grades ativas, quando o sistem baixa um arquivo do site FTP
 * o sistem registra a data e hora que o arquivo baixado, depos o sistema registra a data e hora em que o arquivo foi processado.
 * */
@Entity
@Table(name="t_grib_log")
public class GribLog implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name="id_grib_log", unique=true, nullable=false)
	private String id;
	
	/** Data de incio da previsão dos dados do arquivo, concide com a primeira data no nome do arquivo GRIB */
	@Column(name="data_desde", nullable=false)
	private Date dataDesde;
	
	/** Data de previsão dos dados do arquivo, coincide com a segunda data no nome do arquivo GRIB */
	@Column(name="data_previsao", nullable=false)
	private Date dataPrevisao;
	
	/** Data efetiva quando o arquivo foi baixado do site do FTP */
	@Column(name="data_download")
	private Date dataDownload;
	
	/** Data efetiva quando o arquivo foi processado pelo sistema */
	@Column(name="data_processado")
	private Date dataProcessado;
	
	/** Data efetiva quando o arquivo foi processado pelo sistema para salvar as series temporais */
	@Column(name="data_processado_serie")
	private Date dataProcessadoSerie;
	
	/** Grade que esta associado o registro */
	@ManyToOne
	private Grade grade;
	
	public boolean isDownloaded(){
		if (dataDownload != null){
			return true;
		}
		return false;
	}
	
	public boolean isProcessado(){
		if (dataProcessado != null){
			return true;
		}
		return false;
	}
	
	public boolean isProcessadoSerie(){
		if (dataProcessadoSerie != null){
			return true;
		}
		return false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getDataDesde() {
		return dataDesde;
	}

	public void setDataDesde(Date dataDesde) {
		this.dataDesde = dataDesde;
	}

	public Date getDataPrevisao() {
		return dataPrevisao;
	}

	public void setDataPrevisao(Date dataPrevisao) {
		this.dataPrevisao = dataPrevisao;
	}

	public Date getDataDownload() {
		return dataDownload;
	}

	public void setDataDownload(Date dataDownload) {
		this.dataDownload = dataDownload;
	}

	public Date getDataProcessado() {
		return dataProcessado;
	}

	public void setDataProcessado(Date dataProcessado) {
		this.dataProcessado = dataProcessado;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	public Date getDataProcessadoSerie() {
		return dataProcessadoSerie;
	}

	public void setDataProcessadoSerie(Date dataProcessadoSerie) {
		this.dataProcessadoSerie = dataProcessadoSerie;
	}
	
}
