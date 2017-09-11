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
package com.ufpe.redeclima.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15Min;
import com.ufpe.redeclima.model.Estacao;

@Controller
@RequestMapping("/monitoramento")
@Scope("session")
public class GraficoMonitoramentoEstacaoController implements InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(GraficoMonitoramentoEstacaoController.class);
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;
	
	/* Data inicio da serie */
	private Date dataInicio;
	
	/* Data fim da serie  */
	private Date dataFim;
	
	/* Dados da serie */
	private List<DadosEstacaoTelemetrica15Min> dados;
	
	/* Dados da serie por hora */
	private List<JSONObject> dadosPorHora;
	
	/* Estacao atual selecionada */
	private Estacao estacaoActual;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	private StreamedContent file; 
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	/* Valores maximos e minimos da serie */
	private double maximoChuva;
	
	private double minimoChuva;
	
	private double maximoNivel;
	
	private double minimoNivel;
	
	private double maximoVazao;
	
	private double minimoVazao;
	
	private int ultimosDias;
	
	private int unidadeTempo;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	public GraficoMonitoramentoEstacaoController(){
		ultimosDias = 7;
		unidadeTempo = 0;
		maximoChuva=0;
		maximoNivel=0;
		maximoVazao=0;
		minimoChuva=0;
		minimoNivel=0;
		minimoVazao=0;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -7); 
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		dataInicio = calendar.getTime();
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFim = calendar.getTime();
	}
	
	private void atualizarDados(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataFim);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFim = calendar.getTime();
		
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String estacaoId = paramMap.get("estacaoId");
		if (estacaoId!=null){
			estacaoActual = estacaoDao.findById(Long.valueOf(estacaoId));
		}
		dados =  dadosEstacaoTelemetricaDao.listSerieDados(estacaoActual, dataInicio, dataFim);
	}
	
	private void atualizarDadosPorHora(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataFim);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFim = calendar.getTime();
		
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String estacaoId = paramMap.get("estacaoId");
		if (estacaoId!=null){
			estacaoActual = estacaoDao.findById(Long.valueOf(estacaoId));
		}
		dadosPorHora =  dadosEstacaoTelemetricaDao.listSerieDadosHora(estacaoActual, dataInicio, dataFim);
	}
	
	public String mostrarDadosMonitoramento (){
		return "monitoramentoEstacaoView";
	}
	
	public StreamedContent getFile(){
		
		String fileName = "Estation" + estacaoActual.getNome() + "-LAT" + estacaoActual.getLatitude() + "LON" + estacaoActual.getLongitude() + "_" + formatFileDate.format(dataInicio) + "-" + formatFileDate.format(dataFim) + ".csv";
		String absoluteFileName = Paths.get(pathtransfer, fileName).toString();
		generateCsvFile(absoluteFileName);
		InputStream stream;
		try {
			stream = new FileInputStream(absoluteFileName);
			file = new DefaultStreamedContent(stream, "text/plain", fileName); 
		} catch (FileNotFoundException e) {
			logger.error("Error ao criar o arquivo " +  fileName);
			logger.error("Detalhe do error: " + e.getMessage());
		}
		return file;
	}
	
	private void generateCsvFile(String fileName){
		
		try {
		    
			FileWriter writer = new FileWriter(fileName);
			
			writer.append("Data,Chuva,Nivel,Vazao");
			writer.append('\n');
			
			double nivel=0;
			
			if (this.unidadeTempo==0){
				if (dados!=null){
					for(DadosEstacaoTelemetrica15Min d: dados){
						writer.append(dateFormat.format(d.getId().getData()));
						writer.append(',');
						writer.append(String.valueOf(d.getChuva()));
						writer.append(',');
						writer.append(String.valueOf(d.getNivel()));
						writer.append(',');
						writer.append(String.valueOf(d.getVazao()));
						writer.append('\n');
					}
				}
			}else{
				if (dadosPorHora!=null){
					for(JSONObject d: dadosPorHora){
						writer.append(d.getString("data"));
						writer.append(',');
						writer.append(d.getString("chuva"));
						writer.append(',');
						writer.append(String.valueOf(d.getDouble("nivel")));
						writer.append(',');
						writer.append(d.getString("vazao"));
						writer.append('\n');
					}
				}
			}
			
			writer.flush();
		    writer.close();
		
		} catch(IOException e){
		     logger.error("Error de entrada/saida gerando o arquivo csv ");
		     logger.error("Detalhe do error: " + e.getMessage());
		} 
	}
	
	//TODO trocar getDataChuva x getDadosChuva
	public JSONArray getDataChuva(){
		if (this.unidadeTempo==0){
			return getDataChuva15min();
		}else{
			return getDataChuvasPorHora();
		}
	}
	
	//TODO trocar getDataChuva15min getDadosChuva15min
	public JSONArray getDataChuva15min(){
		atualizarDados();
		
		if (dados!=null && dados.size()>0){
			maximoChuva=0;
			minimoChuva=10000;
		}else{
			maximoChuva=1;
			minimoChuva=0;
		}
		
		JSONArray data =  new JSONArray();
		for (DadosEstacaoTelemetrica15Min d: dados){
			JSONObject row = new JSONObject();
			row.put("data", dateFormat.format(d.getId().getData()));
			row.put("chuva", d.getChuva());
			data.add(row);
			if (maximoChuva < d.getChuva()){
				maximoChuva = d.getChuva();
			}
			if (minimoChuva > d.getChuva()){
				minimoChuva = d.getChuva();
			}
		}
        return data;
	}
	
	public JSONArray getDataChuvasPorHora(){
		atualizarDadosPorHora();
		
		if (dadosPorHora!=null && dadosPorHora.size()>0){
			maximoChuva=0;
			minimoChuva=10000;
		}else{
			maximoChuva=1;
			minimoChuva=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosPorHora){
			JSONObject row = new JSONObject();
			row.put("data", d.getString("data"));
			row.put("chuva", d.getDouble("chuva"));
			data.add(row);
			if (maximoChuva < d.getDouble("chuva")){
				maximoChuva = (float)d.getDouble("chuva");
			}
			if (minimoChuva > d.getDouble("chuva")){
				minimoChuva = (float)d.getDouble("chuva");
			}
		}
        return data;
	}
	
	public JSONArray getDataNivel(){
		if (this.unidadeTempo==0){
			return getDataNivel15min();
		}else{
			return getDataNivelPorHora();
		}
	}
	
	public JSONArray getDataNivel15min(){

		if (dados!=null && dados.size()>0){
			maximoNivel=0;
			minimoNivel=10000;
		}else{
			maximoNivel=1;
			minimoNivel=0;
		}
		
		JSONArray data =  new JSONArray();
		for (DadosEstacaoTelemetrica15Min d: dados){
			JSONObject row = new JSONObject();
			row.put("data", dateFormat.format(d.getId().getData()));
			row.put("nivel", d.getNivel());
			data.add(row);
			if (maximoNivel < d.getNivel()){
				maximoNivel = (float) d.getNivel();
			}
			if (minimoNivel > d.getNivel()){
				minimoNivel = (float) d.getNivel();
			}
		}
        return data;
	}
	
	public JSONArray getDataNivelPorHora(){

		if (dadosPorHora!=null && dadosPorHora.size()>0){
			maximoNivel=0;
			minimoNivel=10000;
		}else{
			maximoNivel=1;
			minimoNivel=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosPorHora){
			JSONObject row = new JSONObject();
			row.put("data", d.getString("data"));
			row.put("nivel", d.getDouble("nivel"));
			data.add(row);
			if (maximoNivel < d.getDouble("nivel")){
				maximoNivel = (float)d.getDouble("nivel");
			}
			if (minimoNivel > d.getDouble("nivel")){
				minimoNivel = (float)d.getDouble("nivel");
			}
		}
        return data;
	}
	
	public JSONArray getDataVazao(){
		if (this.unidadeTempo==0){
			return getDataVazao15min();
		}else{
			return getDataVazaoPorHora();
		}
	}
	
	public JSONArray getDataVazao15min(){
		
		if (dados!=null && dados.size()>0){
			maximoVazao=0;
			minimoVazao=10000;
		}else{
			maximoVazao=1;
			minimoVazao=0;
		}
		
		JSONArray data =  new JSONArray();
		for (DadosEstacaoTelemetrica15Min d: dados){
			JSONObject row = new JSONObject();
			row.put("data", dateFormat.format(d.getId().getData()));
			row.put("vazao", d.getVazao());
			data.add(row);
			if (maximoVazao < d.getVazao()){
				maximoVazao = d.getVazao();
			}
			if (minimoVazao > d.getVazao()){
				minimoVazao = d.getVazao();
			}
		}
        return data;
	}
	
	public JSONArray getDataVazaoPorHora(){
		
		if (dadosPorHora!=null && dadosPorHora.size()>0){
			maximoVazao=0;
			minimoVazao=10000;
		}else{
			maximoVazao=1;
			minimoVazao=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosPorHora){
			JSONObject row = new JSONObject();
			row.put("data", d.getString("data"));
			row.put("vazao", d.getDouble("vazao"));
			data.add(row);
			if (maximoVazao < d.getDouble("vazao")){
				maximoVazao = (float)d.getDouble("vazao");
			}
			if (minimoVazao > d.getDouble("vazao")){
				minimoVazao = (float)d.getDouble("vazao");
			}
		}
        return data;
	}
	
	public Date getDataInicio() {
		return dataInicio;
	}

	public void setDataInicio(Date dataInicio) {
		this.dataInicio = dataInicio;
	}

	public Date getDataFim() {
		return dataFim;
	}

	public void setDataFim(Date dataFim) {
		this.dataFim = dataFim;
	}
	
	public Estacao getEstacaoActual() {
		return estacaoActual;
	}

	public void setEstacaoActual(Estacao estacaoActual) {
		this.estacaoActual = estacaoActual;
	}

	public String getDataFimFiltroString() {
		return dateFormat.format(dataFim);
	}

	public double getMaximoChuva() {
		return maximoChuva;
	}

	public void setMaximoChuva(double maximoChuva) {
		this.maximoChuva = maximoChuva;
	}

	public double getMinimoChuva() {
		return minimoChuva;
	}

	public void setMinimoChuva(double minimoChuva) {
		this.minimoChuva = minimoChuva;
	}

	public double getMaximoNivel() {
		return maximoNivel;
	}

	public void setMaximoNivel(double maximoNivel) {
		this.maximoNivel = maximoNivel;
	}

	public double getMinimoNivel() {
		return minimoNivel;
	}

	public void setMinimoNivel(double minimoNivel) {
		this.minimoNivel = minimoNivel;
	}

	public double getMaximoVazao() {
		return maximoVazao;
	}

	public void setMaximoVazao(double maximoVazao) {
		this.maximoVazao = maximoVazao;
	}

	public double getMinimoVazao() {
		return minimoVazao;
	}

	public void setMinimoVazao(double minimoVazao) {
		this.minimoVazao = minimoVazao;
	}

	public int getUltimosDias() {
		return ultimosDias;
	}

	public void setUltimosDias(int ultimosDias) {
		this.ultimosDias = ultimosDias;
	}

	public int getUnidadeTempo() {
		return unidadeTempo;
	}

	public void setUnidadeTempo(int unidadeTempo) {
		this.unidadeTempo = unidadeTempo;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}
}