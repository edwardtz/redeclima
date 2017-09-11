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
import com.ufpe.redeclima.model.DadosEstacaoTelemetricaChuvaFTP;
import com.ufpe.redeclima.model.DadosEstacaoTelemetricaNivelFTP;
import com.ufpe.redeclima.model.Estacao;

@Controller
@RequestMapping("/monitoramentoFTP")
@Scope("session")
public class GraficoMonitoramentoFTPEstacaoController implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(GraficoMonitoramentoFTPEstacaoController.class);
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;
	
	/* Data inicio da serie */
	private Date dataInicio;
	
	/* Data fim da serie  */
	private Date dataFim;
	
	/* Dados da serie de chuva */
	private List<DadosEstacaoTelemetricaChuvaFTP> dadosChuvas;
	
	/* Dados da serie de nivel */
	private List<DadosEstacaoTelemetricaNivelFTP> dadosNivel;
	
	/* Dados da seria por hora */
	private List<JSONObject> dadosPorHoraChuva;
	
	/* Dados da serie por hora */
	private List<JSONObject> dadosPorHoraNivel;
	
	/* Estacao atual selecionada */
	private Estacao estacaoActual;
	
	private SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	private StreamedContent fileChuva; 
	
	private StreamedContent fileNivel;
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	/* Valores maximos e minimos da serie */
	private float maximoChuva;
	
	private float minimoChuva;
	
	private float maximoNivel;
	
	private float minimoNivel;
	
	private int ultimosDias;
	
	private int unidadeTempo;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	public GraficoMonitoramentoFTPEstacaoController(){
		ultimosDias = 7;
		unidadeTempo = 0;
		maximoChuva=0;
		maximoNivel=0;
		minimoChuva=0;
		minimoNivel=0;
		
	}
	
	private void atualizarDadosChuvas(){
		
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
		dadosChuvas =  dadosEstacaoTelemetricaDao.listSerieDadosChuvaFTP(estacaoActual, dataInicio, dataFim);
	}
	
	private void atualizarDadosNivel(){
		
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
		dadosNivel =  dadosEstacaoTelemetricaDao.listSerieDadosNivelFTP(estacaoActual, dataInicio, dataFim);
	}
	
	private void atualizarDadosPorHoraNivel(){
		
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
		dadosPorHoraNivel =  dadosEstacaoTelemetricaDao.listSerieDadosHoraNivelFTP(estacaoActual, dataInicio, dataFim);
	}
	
	private void atualizarDadosPorHoraChuva(){
		
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
		dadosPorHoraChuva =  dadosEstacaoTelemetricaDao.listSerieDadosHoraChuvaFTP(estacaoActual, dataInicio, dataFim);
	}
	
	public StreamedContent getFileChuva(){
		
		String fileName = "Estation" + estacaoActual.getNome() + "-LAT" + estacaoActual.getLatitude() + "LON" + estacaoActual.getLongitude() + "_" + formatFileDate.format(dataInicio) + "-" + formatFileDate.format(dataFim) + ".csv";
		String absoluteFileName = Paths.get(pathtransfer, fileName).toString();
		generateCsvFileChuva(absoluteFileName);
		InputStream stream;
		try {
			stream = new FileInputStream(absoluteFileName);
			fileChuva = new DefaultStreamedContent(stream, "text/plain", fileName); 
		} catch (FileNotFoundException e) {
			logger.error("Error ao criar o arquivo " +  fileName);
			logger.error("Detalhe do error: " + e.getMessage());
		}
		return fileChuva;
	}
	
	private void generateCsvFileChuva(String fileName){
		
		try {
		    
			FileWriter writer = new FileWriter(fileName);
			
			writer.append("Data,Chuva");
			writer.append('\n');
			
			if (dadosChuvas!=null){
				for(DadosEstacaoTelemetricaChuvaFTP d: dadosChuvas){
					writer.append(dateFormat2.format(d.getData()));
					writer.append(',');
					writer.append(String.valueOf(d.getValor()));
					writer.append('\n');
				}
			}else{
				if (dadosPorHoraChuva!=null){
					for(JSONObject d: dadosPorHoraChuva){
						writer.append(d.getString("data"));
						writer.append(',');
						writer.append(d.getString("chuva"));
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
	
	public StreamedContent getFileNivel(){
		
		String fileName = "Estation" + estacaoActual.getNome() + "-LAT" + estacaoActual.getLatitude() + "LON" + estacaoActual.getLongitude() + "_" + formatFileDate.format(dataInicio) + "-" + formatFileDate.format(dataFim) + ".csv";
		String absoluteFileName = Paths.get(pathtransfer, fileName).toString();
		generateCsvFileNivel(absoluteFileName);
		InputStream stream;
		try {
			stream = new FileInputStream(absoluteFileName);
			fileNivel = new DefaultStreamedContent(stream, "text/plain", fileName); 
		} catch (FileNotFoundException e) {
			logger.error("Error ao criar o arquivo " +  fileName);
			logger.error("Detalhe do error: " + e.getMessage());
		}
		return fileNivel;
	}
	
	private void generateCsvFileNivel(String fileName){
		
		try {
		    
			FileWriter writer = new FileWriter(fileName);
			
			writer.append("Data,Nivel");
			writer.append('\n');
			
			if (dadosNivel!=null){
				for(DadosEstacaoTelemetricaNivelFTP d: dadosNivel){
					writer.append(dateFormat2.format(d.getData()));
					writer.append(',');
					writer.append(String.valueOf(d.getValor()));
					writer.append('\n');
				}
			}else{
				if (dadosPorHoraNivel!=null){
					for(JSONObject d: dadosPorHoraNivel){
						writer.append(d.getString("data"));
						writer.append(',');
						writer.append(d.getString("nivel"));
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
	
	public JSONArray getDataChuva15min(){
		atualizarDadosChuvas();
		
		if (dadosChuvas!=null && dadosChuvas.size()>0){
			maximoChuva=0;
			minimoChuva=10000;
		}else{
			maximoChuva=1;
			minimoChuva=0;
		}
		
		JSONArray data =  new JSONArray();
		for (DadosEstacaoTelemetricaChuvaFTP d: dadosChuvas){
			JSONObject row = new JSONObject();
			row.put("data", dateFormat2.format(d.getData()));
			row.put("chuva", d.getValor());
			data.add(row);
			if (maximoChuva < d.getValor()){
				maximoChuva = d.getValor();
			}
			if (minimoChuva > d.getValor()){
				minimoChuva = d.getValor();
			}
		}
		
        return data;
	}
	
	
	public JSONArray getDataNivel15min(){
		atualizarDadosNivel();
		
		if (dadosNivel!=null && dadosNivel.size()>0){
			maximoNivel=0;
			minimoNivel=10000;
		}else{
			maximoNivel=1;
			minimoNivel=0;
		}
		
		JSONArray data =  new JSONArray();
		for (DadosEstacaoTelemetricaNivelFTP d: dadosNivel){
			JSONObject row = new JSONObject();
			row.put("data", dateFormat2.format(d.getData()));
			row.put("nivel", d.getValor());
			data.add(row);
			if (maximoNivel < d.getValor()){
				maximoNivel = d.getValor();
			}
			if (minimoNivel > d.getValor()){
				minimoNivel = d.getValor();
			}
		}
        return data;
	}
	
	public String mostrarDadosMonitoramento (){
		return "monitoramentoEstacaoViewFTP";
	}
	
	
	public JSONArray getDataChuvasPorHora(){
		
		atualizarDadosPorHoraChuva();

		if (dadosPorHoraChuva!=null && dadosPorHoraChuva.size()>0){
			maximoChuva=0;
			minimoChuva=10000;
		}else{
			maximoChuva=1;
			minimoChuva=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosPorHoraChuva){
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
	
	public JSONArray getDataNivelPorHora(){
		
		atualizarDadosPorHoraNivel();
		
		if (dadosPorHoraNivel!=null && dadosPorHoraNivel.size()>0){
			maximoNivel=0;
			minimoNivel=10000;
		}else{
			maximoNivel=1;
			minimoNivel=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosPorHoraNivel){
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
	
	public JSONArray getDataNivel(){
		if (this.unidadeTempo==0){
			return getDataNivel15min();
		}else{
			return getDataNivelPorHora();
		}
	}
	
	public JSONArray getDataChuva(){
		if (this.unidadeTempo==0){
			return getDataChuva15min();
		}else{
			return getDataChuvasPorHora();
		}
	}
	
	public List<DadosEstacaoTelemetricaChuvaFTP> getDadosChuvas() {
		return dadosChuvas;
	}

	public void setDadosChuvas(List<DadosEstacaoTelemetricaChuvaFTP> dadosChuvas) {
		this.dadosChuvas = dadosChuvas;
	}

	public List<DadosEstacaoTelemetricaNivelFTP> getDadosNivel() {
		return dadosNivel;
	}

	public void setDadosNivel(List<DadosEstacaoTelemetricaNivelFTP> dadosNivel) {
		this.dadosNivel = dadosNivel;
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
		return dateFormat2.format(dataFim);
	}

	public float getMaximoChuva() {
		return maximoChuva;
	}

	public void setMaximoChuva(float maximoChuva) {
		this.maximoChuva = maximoChuva;
	}

	public float getMinimoChuva() {
		return minimoChuva;
	}

	public void setMinimoChuva(float minimoChuva) {
		this.minimoChuva = minimoChuva;
	}

	public float getMaximoNivel() {
		return maximoNivel;
	}

	public void setMaximoNivel(float maximoNivel) {
		this.maximoNivel = maximoNivel;
	}

	public float getMinimoNivel() {
		return minimoNivel;
	}

	public void setMinimoNivel(float minimoNivel) {
		this.minimoNivel = minimoNivel;
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

	public void setFileChuva(StreamedContent fileChuva) {
		this.fileChuva = fileChuva;
	}

	public void setFileNivel(StreamedContent fileNivel) {
		this.fileNivel = fileNivel;
	}

	
	
}
