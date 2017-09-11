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
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.TaskManager;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@Controller
@Scope("session")
public class PanelSimulacaoSerieController implements InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(PanelSimulacaoSerieController.class);
	
	private SeriePrevisao seriePrevisao;
	
	private Bacia bacia;
	
	private Estacao estacaoAtual;
	
	private Secao secao;
	
	private Trecho trecho;
	
	private EnumUnidadeTempo unidadeTempo;
	
	//private Usuario usuario;
	
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
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		List<Bacia> bacias = baciaDao.listOperativas();
		
		if (bacia==null && bacias!=null && !bacias.isEmpty()){
			bacia = baciaDao.listOperativas().get(0);
		}
		
		//usuario = usuarioDao.findByLogin("ADMIN");
		
		unidadeTempo = EnumUnidadeTempo.HORA;
		maximoNivel = 0;
		minimoNivel = 0;
		
		estacaoAtual = estacaoDao.listEstacoesANA(bacia).get(0);
		
		secao = secaoDao.findByDistancia(estacaoAtual.getSecaoRefId()); 
		
	}
	
	public void atualizarPanel(){
		
		secao = secaoDao.findByDistancia(estacaoAtual.getSecaoRefId());
		
		System.out.println("Se envio mensajem de atualização");
		
	}
	
	public SeriePrevisao getSeriePrevisao() {
		return seriePrevisao;
	}

	public void setSeriePrevisao(SeriePrevisao seriePrevisao) {
		this.seriePrevisao = seriePrevisao;
	}

	public Bacia getBacia() {
		return bacia;
	}

	public void setBacia(Bacia bacia) {
		this.bacia = bacia;
	}

	public EnumUnidadeTempo getUnidadeTempo() {
		return unidadeTempo;
	}

	public void setUnidadeTempo(EnumUnidadeTempo unidadeTempo) {
		this.unidadeTempo = unidadeTempo;
	}

	private void atualizarDadosSim(){
		
		dadosNivelSim = resultadoSerieRasDao.list(seriePrevisao, secao);
				
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
		
		dadosNivelEstacao =  dadosEstacaoTelemetricaDao.listSerieDadosHora(estacaoAtual, seriePrevisao.getDataInicio(), seriePrevisao.getDataFim());
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

	public List<Estacao> getEstacoes(){
		return estacaoDao.listEstacoesANA(bacia);
	}
	
	public List<Bacia> getBacias(){
		return baciaDao.listOperativas();
	}
	
}
