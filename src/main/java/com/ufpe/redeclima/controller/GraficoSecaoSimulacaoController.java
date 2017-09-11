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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.ResultadoObsRasDao;
import com.ufpe.redeclima.dao.ResultadoRasDao;
import com.ufpe.redeclima.dao.RioDao;
import com.ufpe.redeclima.dao.SecaoDao;
import com.ufpe.redeclima.dao.SimulacaoDao;
import com.ufpe.redeclima.dao.SimulacaoObsDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.model.ResultadosObsRAS;
import com.ufpe.redeclima.model.ResultadosRAS;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.Simulacao;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.util.EnumTipoDados;

/**
 * @author edwardtz
 *
 */
@Controller
@Scope("session")
public class GraficoSecaoSimulacaoController {
	
	private static final Logger logger = LoggerFactory.getLogger(GraficoSecaoSimulacaoController.class);
	
	/* Pasta local desde donde se transfere o arquivo de exportação */
	@Value("${parameter.path_transfer}")
	private String pathtransfer;

	/* Data inicio da serie */
	private Date dataInicio;
	
	/* Data fim da serie  */
	private Date dataFim;
	
	/* Seção da simulação */
	private Secao secao;
	
	/* Dados da serie */
	private List<JSONObject> dados;
	
	/* Formatador do date de entrada */
	SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	/* Formatador do date de saida */
	SimpleDateFormat dateFormatJSON = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	private StreamedContent file; 
	
	private SimpleDateFormat formatFileDate = new SimpleDateFormat("ddMMyyy HH");
	
	/* Valores maximo da serie */
	private double maximoNivel;
	
	/* Valores minimo da serie */
	private double minimoNivel;
	
	/* Valor que identifica a unidade de tempo de agrupamento dos dados que se utilizara */
	private int unidadeTempo;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SecaoDao secaoDao; 
	
	@Autowired
	private ResultadoRasDao resultadoRasDao;
	
	@Autowired
	private SimulacaoDao simulacaoDao;
	
	@Autowired
	private ResultadoObsRasDao resultadoObsRasDao;
	
	@Autowired
	private SimulacaoObsDao simulacaoObsDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	public GraficoSecaoSimulacaoController(){
		
		unidadeTempo = 0;
		maximoNivel = 0;
		minimoNivel = 0;
	}
	
	public String atualizarDadosPrevisao(){
		atualizarDados();
		return "simulacaoSecaoView";
	}
	
	private void atualizarDados(){
		FacesContext context = FacesContext.getCurrentInstance();
		Map<String, String> paramMap = context.getExternalContext().getRequestParameterMap();
		
		String usuarioId = paramMap.get("usuarioId");
		String baciaId = paramMap.get("baciaId");
		String gradeId = paramMap.get("gradeId");
		String dataInicial = paramMap.get("dataInicio");
		String dataFinal = paramMap.get("dataFim");
		String secaoId = paramMap.get("secaoId");
		String nomeTrecho = paramMap.get("nomeTrecho");
		String tipoDado = paramMap.get("tipoDado");
		Calendar calendario = Calendar.getInstance();
		
		if(tipoDado!=null){
			long tipo = Long.parseLong(tipoDado);
			if (tipo == EnumTipoDados.PREVISAO.getId()){
				
				if (secaoId!=null){
					Trecho trecho = trechoDao.findByNome(nomeTrecho.toUpperCase());
					secao = secaoDao.findByAttributes(new Double(secaoId).intValue(), trecho.getId()); 
					Simulacao simulacao=null;
					if (secao != null){
						try {
							calendario.setTime(dateFormat.parse(dataFinal));
							calendario.set(Calendar.HOUR_OF_DAY, 23);
							calendario.set(Calendar.MINUTE, 59);
							calendario.set(Calendar.SECOND, 59);
							dataFim = calendario.getTime();
							dataInicio = dateFormat.parse(dataInicial);
							simulacao = simulacaoDao.findByAttributes(Long.parseLong(usuarioId), Long.parseLong(baciaId), Long.parseLong(gradeId), dataInicio, dataFim);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						List<ResultadosRAS> resultado =  resultadoRasDao.getResultados(simulacao.getId(), secao.getId());
						
						if (resultado != null){
							dados = new ArrayList<JSONObject>();
							for (ResultadosRAS r: resultado){
								JSONObject jobject = new JSONObject();
								jobject.put("data", dateFormatJSON.format(r.getData()));
								jobject.put("nivel", r.getElevacao());
								dados.add(jobject);
							}
						}
					}
				}
				
			} else {
				
				if (secaoId!=null){
					Trecho trecho = trechoDao.findByNome(nomeTrecho.toUpperCase());
					secao = secaoDao.findByAttributes(new Double(secaoId).intValue(), trecho.getId()); 
					SimulacaoObs simulacao=null;
					if (secao != null){
						try {
							calendario.setTime(dateFormat.parse(dataFinal));
							calendario.set(Calendar.HOUR_OF_DAY, 23);
							calendario.set(Calendar.MINUTE, 59);
							calendario.set(Calendar.SECOND, 59);
							dataFim = calendario.getTime();
							dataInicio = dateFormat.parse(dataInicial);
							simulacao = simulacaoObsDao.findByAttributes(Long.parseLong(usuarioId), Long.parseLong(baciaId), dataInicio, dataFim);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						List<ResultadosObsRAS> resultado =  resultadoObsRasDao.getResultados(simulacao.getId(), secao.getId());
						
						if (resultado != null){
							dados = new ArrayList<JSONObject>();
							for (ResultadosObsRAS r: resultado){
								JSONObject jobject = new JSONObject();
								jobject.put("data", dateFormatJSON.format(r.getData()));
								jobject.put("nivel", r.getElevacao());
								dados.add(jobject);
							}
						}
					}
				}
				
			}
		}
		
		
	}
	
	
	public StreamedContent getFile(){
		
		String fileName = "Seção- " + secao.getDistancia() + "-trecho-" + secao.getTrecho().getNome() +"-"+ formatFileDate.format(dataInicio) + "-" + formatFileDate.format(dataFim) + ".csv";
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
			
			writer.append("Data,Nivel do rio na seção");
			writer.append('\n');
			
			if (dados!=null){
				
				for(JSONObject d: dados){
					writer.append(d.getString("data"));
					writer.append(',');
					writer.append(d.getString("nivel"));
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
	
	public String mostrarDadosSimulacao (){
		atualizarDados();
		return "simulacaoSecaoView";
	}

	public JSONArray getDadosNivel(){
		maximoNivel=0;
		minimoNivel=10000;
		atualizarDadosPrevisao();
		JSONArray dadosJSON =  new JSONArray();
		if (dados!=null && !dados.isEmpty()){
			for (JSONObject d: dados){
				JSONObject row = new JSONObject();
				row.put("data", d.getString("data"));
				row.put("nivel", d.getDouble("nivel"));
				dadosJSON.add(row);
				if (maximoNivel < d.getDouble("nivel")){
					maximoNivel = d.getDouble("nivel");
				}
				if (minimoNivel > d.getDouble("nivel")){
					minimoNivel = d.getDouble("nivel");
				}
			}
		}else{
			maximoNivel = 1;
			minimoNivel = 0;
		}
		
        return dadosJSON;
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

	public List<JSONObject> getDados() {
		return dados;
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

	public int getUnidadeTempo() {
		return unidadeTempo;
	}

	public void setUnidadeTempo(int unidadeTempo) {
		this.unidadeTempo = unidadeTempo;
	}

	public void setDados(List<JSONObject> dados) {
		this.dados = dados;
	}

	public Secao getSecao() {
		return secao;
	}

	public void setSecao(Secao secao) {
		this.secao = secao;
	}
	
	
	
}
