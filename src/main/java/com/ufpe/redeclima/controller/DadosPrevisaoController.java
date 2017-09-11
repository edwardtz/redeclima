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

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.DadoSeriePrevisaoDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.SeriePrevisaoDao;
import com.ufpe.redeclima.dao.SimulacaoSerieDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.dto.SimulacaoSerieDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.TaskManager;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
@Controller
@Scope("session")
public class DadosPrevisaoController implements InitializingBean{
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;
	
	private static final Logger logger = LoggerFactory.getLogger(DadosPrevisaoController.class);
	
	private List<SeriePrevisao> series;
	
	private List<Grade> grades;
	
	private Grade gradeSelected;
	
	private SeriePrevisao serieSelected;
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	private StreamedContent file; 
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private SeriePrevisaoDao seriePrevisaoDao;
	
	@Autowired
	private DadoSeriePrevisaoDao dadoSeriePrevisaoDao;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		grades = gradeDao.list();
		
		if (gradeSelected == null && grades != null && grades.size() > 0){
			gradeSelected = grades.get(0);
		}
	}

	public StreamedContent getFile(){
		String fileName = gradeSelected.getNome() + "_" + formatFileDate.format(serieSelected.getDataInicio()) + "-" + formatFileDate.format(serieSelected.getDataFim()) + ".csv";
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
		    
			String [][] dados = dadoSeriePrevisaoDao.exportarDados(serieSelected);
			int filas, colunas;
			FileWriter writer = new FileWriter(fileName);
			
			if (dados!=null){
				filas = dados.length;
				colunas = dados[0].length;
				for(int i=0; i<filas; i++){
					for(int j=0; j<colunas; j++){
						writer.append(dados[i][j]);
						writer.append(',');
					}
					writer.append('\n');
				}
			}
			
		    writer.flush();
		    writer.close();
		}
		catch(IOException e)
		{
		     logger.error("Error de entrada/saida gerando o arquivo csv ");
		     logger.error("Detalhe do error: " + e.getMessage());
		} 
	}
	
	public StreamedContent getFileResultados(){
		//TODO adicionar a funcionlidade
		System.out.println("TODO adicionar funcionalidade de exportação de resultados RAS para as series de previsão");
		return null;
	}
	
	public List<SeriePrevisao> getSeries() {
		series = seriePrevisaoDao.list(gradeSelected);
		return series;
	}

	public void setSeries(List<SeriePrevisao> series) {
		this.series = series;
	}

	public List<Grade> getGrades() {
		return grades;
	}

	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}

	public Grade getGradeSelected() {
		return gradeSelected;
	}

	public void setGradeSelected(Grade gradeSelected) {
		this.gradeSelected = gradeSelected;
	}

	public SeriePrevisao getSerieSelected() {
		return serieSelected;
	}

	public void setSerieSelected(SeriePrevisao serieSelected) {
		this.serieSelected = serieSelected;
	}
	
}
