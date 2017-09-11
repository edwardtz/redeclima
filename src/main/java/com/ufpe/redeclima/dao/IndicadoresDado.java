package com.ufpe.redeclima.dao;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.ResultadoSerieRAS;
import com.ufpe.redeclima.model.ResultadosObsRAS;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.SimulacaoSerie;
import com.ufpe.redeclima.model.Usuario;

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

/**
 * @author edwardtz
 *
 */
@Component
public class IndicadoresDado {

	@Autowired
	private SimulacaoSerieDao simulacaoSerieDao;
	
	@Autowired
	private ResultadoSerieRasDao resultadoSerieRasDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private ResultadoObsRasDao resultadoObsRasDao;
	
	/**
	 * Retorna a media da diferencia absoluta entre valores de nivel observado e os niveis do resultado ras para uma secao
	 * */
	public double mediaDiferenciaAbsoluta(Usuario usuario, Bacia bacia, Grade grade, Date dataInicial, Date dataFinal, Secao secao, Estacao estacao){

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		
		// Vou buscar os valores da ultima serie dos simulados 
		List<ResultadoSerieRAS> resultados = new ArrayList<ResultadoSerieRAS>();
		
		List<SimulacaoSerie> series = simulacaoSerieDao.listByDataFinal(usuario, bacia, grade, dataInicial, dataFinal);
		if (series!=null && !series.isEmpty()){
			for (SimulacaoSerie ss: series){
				List<ResultadoSerieRAS> results = resultadoSerieRasDao.listUltimoDia(ss, secao);
				if (results != null){
					resultados.addAll(results);
				}
				
			}
		}
		
		// Vou buscar os valores observados
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(dataFinal);
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		dataFinal = calendar.getTime();
		
		List<JSONObject> dadosNivelEstacao = dadosEstacaoTelemetricaDao.listSerieDadosHora(estacao, dataInicial, dataFinal);
		
		double media = 0.0;
		int n = 0;
		Date dataEstacao;
		Date dataResultado;
		
		try {
			
			for (ResultadoSerieRAS r: resultados){
				calendar.setTime(r.getData());
				dataResultado = calendar.getTime();
				for (JSONObject d: dadosNivelEstacao){
					dataEstacao = dateFormat.parse(d.getString("data"));
					if (dataResultado.compareTo(dataEstacao)==0 && d.getDouble("nivel")>=0 && r.getElevacao()>=0 ){
						double dif = Math.abs(d.getDouble("nivel") - r.getElevacao());
						media = media + Math.abs(dif);
						n++;
					}
				}
			}
		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (n>0){
			media = media / n;
		}
		
		return media;
	}
	
	/**
	 * Retorna a media da diferencia absoluta entre valores de nivel observado e os niveis do resultado ras para uma secao
	 * */
	public double mediaDiferenciaAbsolutaObservados(Usuario usuario, SimulacaoObs simulacao, Secao secao, Estacao estacao){

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		
		// Vou buscar os valores da ultima serie dos simulados 
		List<ResultadosObsRAS> resultados = resultadoObsRasDao.getResultados(simulacao, secao);
		
		List<JSONObject> dadosNivelEstacao = dadosEstacaoTelemetricaDao.listSerieDadosHora(estacao, simulacao.getDataInicio(), simulacao.getDataFim());
		
		double media = 0.0;
		int n = 0;
		Date dataEstacao;
		Date dataResultado;
		
		Calendar calendar = Calendar.getInstance();
		
		try {
			
			for (ResultadosObsRAS r: resultados){
				calendar.setTime(r.getData());
				dataResultado = calendar.getTime();
				for (JSONObject d: dadosNivelEstacao){
					dataEstacao = dateFormat.parse(d.getString("data"));
					if (dataResultado.compareTo(dataEstacao)==0 && d.getDouble("nivel")>=0 && r.getElevacao()>=0 ){
						double dif = Math.abs(d.getDouble("nivel") - r.getElevacao());
						media = media + Math.abs(dif);
						n++;
					}
				}
			}
		
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		if (n>0){
			media = media / n;
		}
		
		return media;
	}
	
}
