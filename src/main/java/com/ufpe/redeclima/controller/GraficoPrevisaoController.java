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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.model.PontoGrade;

/**
 * @author edwardtz
 *
 */
@Controller
@RequestMapping("/previsao")
@Scope("session")
public class GraficoPrevisaoController {

	private static final Logger logger = LoggerFactory.getLogger(GraficoPrevisaoController.class);
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;
	
	/* Data inicio da serie */
	private Date dataInicio;
	
	/* Data fim da serie  */
	private Date dataFim;
	
	/* Ponto grade de previsao */
	private PontoGrade pontoGrade;
	
	/* Dados da serie */
	private List<JSONObject> dados;
	
	/* Valores maximos e minimos da serie */
	private double maximoChuva;
	
	private double minimoChuva;
	
	private int unidadeTempo;
	
	private StreamedContent file; 
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	@Autowired
	private DadoPontoGradeDao dadoPontoGradeDao; 
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired(required=true)
	@Value("${jaxws.cptecDiasPrevisao}")
	private String diasPrevisao;
	
	public GraficoPrevisaoController(){
		
		Calendar calendario = Calendar.getInstance();
		calendario.set(Calendar.HOUR_OF_DAY, 0);
		calendario.set(Calendar.MINUTE, 1);
		calendario.set(Calendar.SECOND, 0);
		dataInicio = calendario.getTime();
		calendario.add(Calendar.DAY_OF_MONTH, 3);
		dataFim = calendario.getTime();
		
		unidadeTempo = 0;
		maximoChuva = 0;
		minimoChuva = 0;
	}
	
	public String atualizarDadosPrevisao(){
		if (this.unidadeTempo==0){
			atualizarDados();
		}else{
			atualizarDadosPorDia();
		}
		return "previsaoPontoGradeView";
	}
	
	private void atualizarDados(){
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String pontoGradeId = paramMap.get("pontoGradeId");
		if (pontoGradeId!=null){
			pontoGrade = pontoGradeDao.findById(Long.valueOf(pontoGradeId));
		}
		dados =  dadoPontoGradeDao.listByGradeDataHorario(pontoGrade, dataInicio, dataFim);
	}
	
	private void atualizarDadosPorDia(){
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String pontoGradeId = paramMap.get("pontoGradeId");
		if (pontoGradeId!=null){
			pontoGrade = pontoGradeDao.findById(Long.valueOf(pontoGradeId));
		}
		dados =  dadoPontoGradeDao.listByGradeDataDiario(pontoGrade, dataInicio, dataFim);
	}
	
	public StreamedContent getFile(){
		
		String fileName = "Ponto" + pontoGrade.getId() + "-LAT" + pontoGrade.getLatitude() + "LON" + pontoGrade.getLongitude() + "_" + formatFileDate.format(dataInicio) + "-" + formatFileDate.format(dataFim) + ".csv";
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
			
			writer.append("Data,Valor chuva");
			writer.append('\n');
			
			if (dados!=null){
				
				for(JSONObject d: dados){
					writer.append(d.getString("data"));
					writer.append(',');
					writer.append(d.getString("chuva"));
					writer.append('\n');
				}
			}
				
			writer.flush();
		    writer.close();
		
		} catch(IOException e){
		     logger.error("Error de entrada/saida gerando o arquivo csv ");
		     logger.error("Detalhe do error: " + e.getMessage());
		} 
	}
	
	public String mostrarDadosMonitoramento (){
		if (this.unidadeTempo==0){
			atualizarDados();
		}else{
			atualizarDadosPorDia();
		}
		return "previsaoPontoView";
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

	public PontoGrade getPontoGrade() {
		return pontoGrade;
	}

	public void setPontoGrade(PontoGrade pontoGrade) {
		this.pontoGrade = pontoGrade;
	}

	public List<JSONObject> getDados() {
		return dados;
	}
	
	public JSONArray getDadosChuva(){
		maximoChuva=0;
		minimoChuva=0;
		atualizarDadosPrevisao();
		JSONArray dadosJSON =  new JSONArray();
		for (JSONObject d: dados){
			JSONObject row = new JSONObject();
			row.put("data", d.getString("data"));
			row.put("chuva", d.getDouble("chuva"));
			dadosJSON.add(row);
			if (maximoChuva < d.getDouble("chuva")){
				maximoChuva = (float)d.getDouble("chuva");
			}
			if (minimoChuva > d.getDouble("chuva")){
				minimoChuva = (float)d.getDouble("chuva");
			}
		}
        return dadosJSON;
	}

	public void setDados(List<JSONObject> dados) {
		this.dados = dados;
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
