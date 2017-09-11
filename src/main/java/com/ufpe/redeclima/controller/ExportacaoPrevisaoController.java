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
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.DadoSeriePrevisaoDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.SeriePrevisaoDao;
import com.ufpe.redeclima.model.DadoSeriePrevisao;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.model.SeriePrevisao;

/**
 * @author edwardtz
 *
 */
@Controller
@Scope("session")
public class ExportacaoPrevisaoController implements InitializingBean {
	
	private static final Logger logger = LoggerFactory.getLogger(ExportacaoPrevisaoController.class);
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;
	
	private PontoGrade pontoGrade;
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	private SeriePrevisao serieSelected;
	
	private List<SeriePrevisao> series;
	
	private StreamedContent file; 
	
	@Autowired
	private SeriePrevisaoDao seriePrevisaoDao;
	
	@Autowired
	private DadoSeriePrevisaoDao dadoSeriePrevisaoDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String pontoGradeId = paramMap.get("pontoGradeId");
		
		if (pontoGradeId!=null && pontoGrade==null){
			pontoGrade = pontoGradeDao.findById(Long.valueOf(pontoGradeId));
		}
		
		if (series==null && pontoGrade!=null){
			series = seriePrevisaoDao.list(pontoGrade.getGrade());
		}
	}
	
	public StreamedContent getFile(){
		String fileName = "Ponto" + pontoGrade.getId() + "-LAT" + pontoGrade.getLatitude() + "LON" + pontoGrade.getLongitude() + "_" + formatFileDate.format(serieSelected.getDataInicio()) + "-" + formatFileDate.format(serieSelected.getDataFim()) + ".csv";
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
		    
			List<DadoSeriePrevisao> dados = dadoSeriePrevisaoDao.listBy(pontoGrade, serieSelected);
			
			FileWriter writer = new FileWriter(fileName);
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			
			writer.append("Data,Valor chuva");
			writer.append('\n');
			
			if (dados!=null){
				
				for(DadoSeriePrevisao d: dados){
					writer.append(dateFormat.format(d.getDataPrevisao()));
					writer.append(',');
					writer.append(d.getChuva().toString());
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
	
	public List<SeriePrevisao> getSeries() {
		
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		String pontoGradeId = paramMap.get("pontoGradeId");
		
		if (pontoGradeId!=null && pontoGrade==null){
			pontoGrade = pontoGradeDao.findById(Long.valueOf(pontoGradeId));
		}
		
		series = seriePrevisaoDao.list(pontoGrade.getGrade());
		return series;
	}

	public PontoGrade getPontoGrade() {
		return pontoGrade;
	}

	public void setPontoGrade(PontoGrade pontoGrade) {
		this.pontoGrade = pontoGrade;
	}

	public SeriePrevisao getSerieSelected() {
		return serieSelected;
	}

	public void setSerieSelected(SeriePrevisao serieSelected) {
		this.serieSelected = serieSelected;
	}

	public void setSeries(List<SeriePrevisao> series) {
		this.series = series;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}
	
}
