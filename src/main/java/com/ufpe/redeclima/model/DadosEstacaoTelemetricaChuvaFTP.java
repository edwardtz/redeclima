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
import javax.persistence.Table;

/**
 * Esta classe representa a entidade que armazena as medições de chuva emitidos pelas estações telemétricas em períodos de 15 minutos,
 * desde a fonte de dados de FTP
 * Cada uma de estas medições de chuva está associada a uma estação telemétrica em um determinado tempo
 * */
@Entity
@Table(name="t_dados_estacao_telemetrica_chuva_ftp")
public class DadosEstacaoTelemetricaChuvaFTP implements Serializable {

	private static final long serialVersionUID = 1L;

	/* Identificador da estação que emitiu a medição */
	@Id
	@Column(name="estacao_id")
	private Long estacaoId;
	
	/* Data em que a medição foi feita */
	@Id
	@Column(name="data")
	private Date data;
	
	/* Valor de chuva correspondente a medição */
	@Column(name="valor")
	private Float valor;

	
	public Long getEstacaoId() {
		return estacaoId;
	}

	public void setEstacaoId(Long estacaoId) {
		this.estacaoId = estacaoId;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public Float getValor() {
		return valor;
	}

	public void setValor(Float valor) {
		this.valor = valor;
	}
	
}
