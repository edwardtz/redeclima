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
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.util.EnumEstadoEstacao;
import com.ufpe.redeclima.util.EnumEstadosBrasil;
import com.ufpe.redeclima.util.EnumOperador;
import com.ufpe.redeclima.util.EnumOperadorTelefonia;
import com.ufpe.redeclima.util.EnumTipoResponsavel;
import com.ufpe.redeclima.util.EnumTipoTransmissao;

/**
 * Esta classe representa a entidade que armazena os dados de estações telemétricas
 * */
@Entity
@Table(name="t_estacao")
public class Estacao implements Serializable, PontoDado {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id_estacao", unique=true, nullable=false)
	private Long id;
	
	/* Código de identificação da estação */
	@Column(name="codigo", unique=true, nullable=false)
	private Integer codigo;

	/* Nome de identificação da estação */
	@Column(name="nome", length=100, nullable=false)
	private String nome;
	
	/* Nome do municipio ao qual pertece a estação */
	@Column(name="municipio", length=50, nullable=false)
	private String municipio;
	
	/* Latitude em formato geográfico decimal */
	@Column(name="latitude", nullable=false)
	private Double latitude;
	
	/* Longitude em formato geográfico decimal */
	@Column(name="longitude", nullable=false)
	private Double longitude;
	
	/* Tipo de estação */
	@ManyToMany(cascade=CascadeType.DETACH, fetch=FetchType.EAGER)
	@JoinTable(name="t_estacao_tipo_map") 
	private List<TipoEstacao> tipos;
	
	/* Entidade responsavel pelo gerenciamento da estação */
	@Enumerated(EnumType.STRING)
	@Column(name="resposavel", nullable=false)
	private EnumTipoResponsavel responsavel;
	
	/* Operador da estação encarregado da manutenção */
	@Enumerated(EnumType.STRING)
	@Column(name="operador", length=50)
	private EnumOperador operador;
	
	/* Longitude UTM da estação */
	@Column(name="xutm")
	private long xutm;
	
	/* Latitude UTM da estação */
	@Column(name="yutm")
	private long yutm;
	
	/* Coordenada geográfica */
	@Column(name="coordenadas", length=100)
	private String coordenadas;
	
	/* Altitude posição da estação */
	@Column(name="altitude")
	private Double altitude;
	
	/* Area de drenagem da estação   */
	@Column(name="areaDrenagem")
	private Float areaDrenagem;
	
	/* Unidade do estado */
	@Enumerated(EnumType.STRING)
	@Column(name="uf", length=2)
	private EnumEstadosBrasil uf;
	
	/* Indicador de se a estação foi ou não instalada  */
	@Column(name="instalada")
	private Boolean instalada;
	
	/* Tipo de transmissão que aestação usa para enviar os dados */
	@Enumerated(EnumType.STRING)
	@Column(name="tipoTransmissao", length=100)
	private EnumTipoTransmissao tipoTransmissao;
	
	/* Operador de telefonia no caso que a estação use rede de celular telefónica para transmitir os dados */
	@Enumerated(EnumType.STRING)
	@Column(name="operadorTelefonia", length=100)
	private EnumOperadorTelefonia operadorTelefonia;
	
	/* Número de telefone da estação */
	@Column(name="telefone", length=100)
	private String telefone;
	
	/* Senha da estação */
	@Column(name="senha", length=50)
	private String senha;
	
	/* Patrimonio */
	@Column(name="patrimonio")
	private String patrimonio;
	
	/* Abrigo */
	@Column(name="abrigo")
	private String abrigo;
	
	/* Indicador de se a fonte de energia é baseada em energia de panies solares */
	@Column(name="painelSolar")
	private String painelSolar;
	
	/* Indicador de se a estação tem pluviômetro */
	@Column(name="pluviometro", length=100)
	private String pluviometro;
	
	/* Indicador se a estação tem medidor de pressão */
	@Column(name="pressao", length=100)
	private String pressao;
	
	/* Indicador de se a estação tem radar */
	@Column(name="radar", length=100)
	private String radar;
	
	/* Metros de cabo da estação */
	@Column(name="caboMetros")
	private Float caboMetros;
	
	/* Endereço ip do servidor de dados */
	@Column(name="enderecoServidorDados")
	private String enderecoServidorDados;
	
	/* Login do servidor de dados */
	@Column(name="loginServidorDados", length=50)
	private String loginServidorDados;
	
	/* Senha do servidor de dados */
	@Column(name="senhaServidorDados", length=50)
	private String senhaServidorDados;
	
	/* Data de inicio da operação da estação  */
	@Column(name="dateInicioOperacao")
	private Date dataInicioOperacao;
	
	/* Tipo de dados da estação */
	@Column(name="tipoDado", length=50)
	private String tipodoDado;
	
	/* Observações gerais  */
	@Column(name="observacoes", length=100)
	private String observacoes;
	
	/* Ajuste regua */
	@Column(name="ajuste_regua")
	private Double ajusteRegua;
	
	@Column(name="secao_ref_if") //TODO tirar quando esteja pronta a integração de postgis procurar por distancia mais perto
	private Integer secaoRefId;
	
	/* Bacia hidrológica na qual esta associada a estação */
	@ManyToOne (cascade=CascadeType.DETACH, fetch=FetchType.EAGER)
	private Bacia bacia;
	
	/* Indicador do estado operativo da estação */
	@Enumerated(EnumType.STRING)
	@Column(name="estadoEstacao")
	private EnumEstadoEstacao estadoEstacao;
	
	/* Ultima data na qual foi atualizado o estado da estação */
	@Column(name="dataAtualizacaoEstado")
	private Date dataAtualizacaoEstado;
	
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

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}
	
	public String getMunicipio() {
		return municipio;
	}

	public void setMunicipio(String municipio) {
		this.municipio = municipio;
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

	public List<TipoEstacao> getTipos() {
		return tipos;
	}

	public void setTipos(List<TipoEstacao> tipos) {
		this.tipos = tipos;
	}

	public EnumTipoResponsavel getResponsavel() {
		return responsavel;
	}

	public void setResponsavel(EnumTipoResponsavel responsavel) {
		this.responsavel = responsavel;
	}

	public void setCaboMetros(Float caboMetros) {
		this.caboMetros = caboMetros;
	}

	public long getXutm() {
		return xutm;
	}

	public void setXutm(long xutm) {
		this.xutm = xutm;
	}

	public long getYutm() {
		return yutm;
	}

	public void setYutm(long yutm) {
		this.yutm = yutm;
	}

	public String getCoordenadas() {
		return coordenadas;
	}

	public void setCoordenadas(String coordenadas) {
		this.coordenadas = coordenadas;
	}

	public Double getAltitude() {
		return altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	public Float getAreaDrenagem() {
		return areaDrenagem;
	}

	public void setAreaDrenagem(Float areaDrenagem) {
		this.areaDrenagem = areaDrenagem;
	}

	public EnumEstadosBrasil getUf() {
		return uf;
	}

	public void setUf(EnumEstadosBrasil uf) {
		this.uf = uf;
	}

	public Boolean isInstalada() {
		return instalada;
	}
	
	public Boolean getInstalada() {
		return instalada;
	}

	public void setInstalada(Boolean instalada) {
		this.instalada = instalada;
	}

	public EnumTipoTransmissao getTipoTransmissao() {
		return tipoTransmissao;
	}

	public void setTipoTransmissao(EnumTipoTransmissao tipoTransmissao) {
		this.tipoTransmissao = tipoTransmissao;
	}

	public EnumOperadorTelefonia getOperadorTelefonia() {
		return operadorTelefonia;
	}

	public void setOperadorTelefonia(EnumOperadorTelefonia operadorTelefonia) {
		this.operadorTelefonia = operadorTelefonia;
	}

	public String getTelefone() {
		return telefone;
	}

	public void setTelefone(String telefone) {
		this.telefone = telefone;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public String getPatrimonio() {
		return patrimonio;
	}

	public void setPatrimonio(String patrimonio) {
		this.patrimonio = patrimonio;
	}

	public String getAbrigo() {
		return abrigo;
	}

	public void setAbrigo(String abrigo) {
		this.abrigo = abrigo;
	}

	public String getPainelSolar() {
		return painelSolar;
	}

	public void setPainelSolar(String painelSolar) {
		this.painelSolar = painelSolar;
	}

	public String getPluviometro() {
		return pluviometro;
	}

	public void setPluviometro(String pluviometro) {
		this.pluviometro = pluviometro;
	}

	public String getPressao() {
		return pressao;
	}

	public void setPressao(String pressao) {
		this.pressao = pressao;
	}

	public String getRadar() {
		return radar;
	}

	public void setRadar(String radar) {
		this.radar = radar;
	}

	public float getCaboMetros() {
		return caboMetros;
	}

	public void setCaboMetros(float caboMetros) {
		this.caboMetros = caboMetros;
	}

	public String getEnderecoServidorDados() {
		return enderecoServidorDados;
	}

	public void setEnderecoServidorDados(String enderecoServidorDados) {
		this.enderecoServidorDados = enderecoServidorDados;
	}

	public String getLoginServidorDados() {
		return loginServidorDados;
	}

	public void setLoginServidorDados(String loginServidorDados) {
		this.loginServidorDados = loginServidorDados;
	}

	public String getSenhaServidorDados() {
		return senhaServidorDados;
	}

	public void setSenhaServidorDados(String senhaServidorDados) {
		this.senhaServidorDados = senhaServidorDados;
	}

	public Date getDataInicioOperacao() {
		return dataInicioOperacao;
	}

	public void setDataInicioOperacao(Date dataInicioOperacao) {
		this.dataInicioOperacao = dataInicioOperacao;
	}

	public String getTipodoDado() {
		return tipodoDado;
	}

	public void setTipodoDado(String tipodoDado) {
		this.tipodoDado = tipodoDado;
	}

	public String getObservacoes() {
		return observacoes;
	}

	public void setObservacoes(String observacoes) {
		this.observacoes = observacoes;
	}
	
	public EnumOperador getOperador() {
		return operador;
	}

	public void setOperador(EnumOperador operador) {
		this.operador = operador;
	}
	
	public EnumEstadoEstacao getEstadoEstacao() {
		return estadoEstacao;
	}

	public void setEstadoEstacao(EnumEstadoEstacao estadoEstacao) {
		this.estadoEstacao = estadoEstacao;
	}

	public Date getDataAtualizacaoEstado() {
		return dataAtualizacaoEstado;
	}

	public void setDataAtualizacaoEstado(Date dataAtualizacaoEstado) {
		this.dataAtualizacaoEstado = dataAtualizacaoEstado;
	}
	
	public Double getAjusteRegua() {
		return ajusteRegua;
	}

	public void setAjusteRegua(Double ajusteRegua) {
		this.ajusteRegua = ajusteRegua;
	}
	
	public Integer getSecaoRefId() {
		return secaoRefId;
	}

	public void setSecaoRefId(Integer secaoRefId) {
		this.secaoRefId = secaoRefId;
	}
	
	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.PontoDado#getGageId()
	 */
	public String getGageId() {
		return "Estacao_" + codigo;
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
        if (!(object instanceof Estacao)) {
            return false;
        }

        Estacao other = (Estacao) object;

        if (((this.id == null) && (other.id != null)) || ((this.id != null) && !this.id.equals(other.id))) {
            return false;
        }

        return true;
    }

}
