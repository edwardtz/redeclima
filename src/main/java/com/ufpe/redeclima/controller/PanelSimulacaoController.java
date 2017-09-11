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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.IndicadoresDado;
import com.ufpe.redeclima.dao.LimitesBaciasKMLDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.ResultadoRasDao;
import com.ufpe.redeclima.dao.ResultadoSerieRasDao;
import com.ufpe.redeclima.dao.RiosKMLDao;
import com.ufpe.redeclima.dao.SecaoDao;
import com.ufpe.redeclima.dao.SecoesKMLDao;
import com.ufpe.redeclima.dao.SimulacaoDao;
import com.ufpe.redeclima.dao.SimulacaoSerieDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.ResultadoSerieRAS;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.SimulacaoSerie;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.TaskManager;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@Controller
@Scope("session")
public class PanelSimulacaoController implements InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(PanelSimulacaoController.class);
	
	private Grade grade;
	
	private Bacia bacia;
	
	private List<Bacia> bacias;
	
	private List<Grade> grades;
	
	private Date dataInicial;
	
	private Date dataFinal;
	
	private Estacao estacaoAtual;
	
	private Secao secao;
	
	private Trecho trecho;
	
	private EnumUnidadeTempo unidadeTempo;
	
	private Usuario usuario;
	
	private SimpleDateFormat dateFormat2 = new SimpleDateFormat("MM/dd/yyyy HH:mm");
	
	/* Dados da serie do nivel simulação */
	private List<ResultadoSerieRAS> dadosNivelSim;
	
	/* Dados da serie do nivel simulação */
	private List<JSONObject> dadosNivelEstacao;
	
	/* Valores maximos e minimos da serie */
	private double maximoNivel;
	
	private double minimoNivel;
	
	/* Valores maximos e minimos da serie RAS */
	private double maximoNivelRas;
	
	private double minimoNivelRas;
	
	private double mediaHistoricaNivelObservado;
	
	private double mediaPeriodoNivelObservado;
	
	private double mediaHistoricaNivelRas;
	
	private double mediaPeriodoNivelRas;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private LimitesBaciasKMLDao limitesBaciasKMLDao;
	
	@Autowired
	private RiosKMLDao riosKMLDao;
	
	@Autowired
	private SecoesKMLDao secoesKMLDao;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private SimulacaoDao simulacaoDao;
	
	@Autowired
	private ResultadoRasDao resultadoRasDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SimulacaoSerieDao simulacaoSerieDao;
	
	@Autowired
	private ResultadoSerieRasDao resultadoSerieRasDao;
	
	@Autowired
	private IndicadoresDado indicadoresDado;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		bacias = baciaDao.listOperativas();
		
		if (bacia==null && bacias!=null && !bacias.isEmpty()){
			bacia = baciaDao.listOperativas().get(0);
		}
		grades = gradeDao.listAtivas();
		if (grades!=null && grades.size()>0){
			grade = grades.get(0);
		}
		
		usuario = usuarioDao.findByLogin("ADMIN");
		
		Calendar calendario = Calendar.getInstance();
		calendario.set(Calendar.HOUR_OF_DAY, 0);
		calendario.set(Calendar.MINUTE, 1);
		calendario.set(Calendar.SECOND, 0);
		dataInicial = calendario.getTime();
		calendario.add(Calendar.DAY_OF_MONTH, 3);
		dataFinal = calendario.getTime();
		
		unidadeTempo = EnumUnidadeTempo.HORA;
		maximoNivel = 0;
		minimoNivel = 0;
		mediaHistoricaNivelObservado = 0.0;
		mediaHistoricaNivelRas = 0.0;
		mediaPeriodoNivelObservado = 0.0;
		mediaPeriodoNivelRas = 0.0;
		
		estacaoAtual = estacaoDao.listEstacoesANA(bacia).get(0); 
		
		secao = secaoDao.findByDistancia(estacaoAtual.getSecaoRefId()); 
		
		mediaHistoricaNivelObservado = dadosEstacaoTelemetricaDao.mediaHistoricaNivel(estacaoAtual);
		
		mediaPeriodoNivelObservado = dadosEstacaoTelemetricaDao.mediaNivel(estacaoAtual, dataInicial, dataFinal);
	}
	
	public void atualizarPanel(){
		
		secao = secaoDao.findByDistancia(estacaoAtual.getSecaoRefId()); 
		
		System.out.println("Se envio mensajem de atualização");
		
	}
	
	public double getMediaDiferenciaAbsoluta(){
		return indicadoresDado.mediaDiferenciaAbsoluta(usuario, bacia, grade, dataInicial, dataFinal, secao, estacaoAtual);
	}
	
	public List<Grade> getGrades() {
		return gradeDao.listAtivas();
	}

	public void setGrades(List<Grade> grades) {
		this.grades = grades;
	}

	public List<Bacia> getBacias() {
		return baciaDao.listOperativas();
	}

	public void setBacias(List<Bacia> bacias) {
		this.bacias = bacias;
	}

	public Grade getGrade() {
		return grade;
	}

	public void setGrade(Grade grade) {
		this.grade = grade;
	}

	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public EnumUnidadeTempo getUnidadeTempo() {
		return unidadeTempo;
	}

	public void setUnidadeTempo(EnumUnidadeTempo unidadeTempo) {
		this.unidadeTempo = unidadeTempo;
	}

	private void atualizarDadosSim(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataFinal);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFinal = calendar.getTime();
		
		dadosNivelSim = null;
		dadosNivelSim = new ArrayList<ResultadoSerieRAS>();
		
		List<SimulacaoSerie> series = simulacaoSerieDao.listByDataFinal(usuario, bacia, grade, dataInicial, dataFinal);
		if (series!=null && !series.isEmpty()){
			for (SimulacaoSerie ss: series){
				List<ResultadoSerieRAS> results = resultadoSerieRasDao.listUltimoDia(ss, secao);
				if (results != null){
					dadosNivelSim.addAll(results);
				}
				
			}
		}
	}

	public JSONArray getDadosNivelSim() {
		
		atualizarDadosSim();
		
		if (dadosNivelSim!=null && dadosNivelSim.size()>0){
			maximoNivelRas=0;
			minimoNivelRas=10000;
		}else{
			maximoNivelRas=1;
			minimoNivelRas=0;
		}
				
		JSONArray data =  new JSONArray();
		if (dadosNivelSim != null){
			for (ResultadoSerieRAS d: dadosNivelSim){
				if (d.getElevacao() >= 0){
					JSONObject row = new JSONObject();
					row.put("data", dateFormat2.format(d.getData()));
					row.put("nivel", d.getElevacao());
					data.add(row);
					if (maximoNivelRas < d.getElevacao()){
						maximoNivelRas = d.getElevacao();
					}
					if (minimoNivelRas > d.getElevacao()){
						minimoNivelRas = d.getElevacao();
					}
				}
			}
		}
		
		return data;
		
	}
	
	private void atualizarDadosEstacao(){
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataFinal);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFinal = calendar.getTime();
		
		dadosNivelEstacao =  dadosEstacaoTelemetricaDao.listSerieDadosHora(estacaoAtual, dataInicial, dataFinal);
	}
	
	public JSONArray getDadosNivelEstacao() {
		
		atualizarDadosEstacao();
		
		if (dadosNivelEstacao!=null && dadosNivelEstacao.size()>0){
			maximoNivel=0;
			minimoNivel=10000;
		}else{
			maximoNivel=1;
			minimoNivel=0;
		}
		
		JSONArray data =  new JSONArray();
		for (JSONObject d: dadosNivelEstacao){
			
			if (d.getDouble("nivel") >= 0){
				
				JSONObject row = new JSONObject();
				row.put("data", d.getString("data"));
				row.put("nivel", d.getDouble("nivel"));
				data.add(row);
				if (maximoNivel < d.getDouble("nivel")){
					maximoNivel = d.getDouble("nivel");
				}
				if (minimoNivel > d.getDouble("nivel")){
					minimoNivel = d.getDouble("nivel");
				}
			}
		}
        
		return data;
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

	public Estacao getEstacaoAtual() {
		return estacaoAtual;
	}

	public void setEstacaoAtual(Estacao estacaoAtual) {
		this.estacaoAtual = estacaoAtual;
	}

	public Secao getSecao() {
		return secao;
	}

	public void setSecao(Secao secao) {
		this.secao = secao;
	}

	public Trecho getTrecho() {
		return trecho;
	}

	public void setTrecho(Trecho trecho) {
		this.trecho = trecho;
	}

	public double getMaximoNivelRas() {
		return maximoNivelRas;
	}

	public void setMaximoNivelRas(double maximoNivelRas) {
		this.maximoNivelRas = maximoNivelRas;
	}

	public double getMinimoNivelRas() {
		return minimoNivelRas;
	}

	public void setMinimoNivelRas(double minimoNivelRas) {
		this.minimoNivelRas = minimoNivelRas;
	}

	public double getMediaHistoricaNivelObservado() {
		mediaHistoricaNivelObservado = dadosEstacaoTelemetricaDao.mediaHistoricaNivel(estacaoAtual);
		return mediaHistoricaNivelObservado;
	}

	public void setMediaHistoricaNivelObservado(double mediaHistoricaNivelObservado) {
		this.mediaHistoricaNivelObservado = mediaHistoricaNivelObservado;
	}

	public double getMediaPeriodoNivelObservado() {
		mediaPeriodoNivelObservado = dadosEstacaoTelemetricaDao.mediaNivel(estacaoAtual, dataInicial, dataFinal);
		return mediaPeriodoNivelObservado;
	}

	public void setMediaPeriodoNivelObservado(double mediaPeriodoNivelObservado) {
		this.mediaPeriodoNivelObservado = mediaPeriodoNivelObservado;
	}

	private Double mediaPeriodoNivel(){
		Double media = 0.0;
		if (dadosNivelSim!=null && !dadosNivelSim.isEmpty()){
			for (ResultadoSerieRAS r: dadosNivelSim){
				media = media + r.getElevacao();
			}
			media = media / dadosNivelSim.size();
		}
		
		return media;
		
	}
	
	public double getMediaHistoricaNivelRas() {
		mediaHistoricaNivelRas = resultadoSerieRasDao.mediaHistoricaSerieUltimoDia(usuario, bacia, grade, secao);
		return mediaHistoricaNivelRas;
	}

	public void setMediaHistoricaNivelRas(double mediaHistoricaNivelRas) {
		this.mediaHistoricaNivelRas = mediaHistoricaNivelRas;
	}

	public double getMediaPeriodoNivelRas() {
		mediaPeriodoNivelRas = mediaPeriodoNivel();
		return mediaPeriodoNivelRas;
	}

	public void setMediaPeriodoNivelRas(double mediaPeriodoNivelRas) {
		this.mediaPeriodoNivelRas = mediaPeriodoNivelRas;
	}
	
	public List<Estacao> getEstacoes(){
		return estacaoDao.listEstacoesANA(bacia);
	}

}
