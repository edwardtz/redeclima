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
package com.ufpe.redeclima.task;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import com.ufpe.redeclima.dao.ANAWSDao;
import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15Min;
import com.ufpe.redeclima.model.Estacao;

/**
*  Esta classe executa a tarefa de atualizar os dados de nível, chuva e vazão das estações telemétricas. 
*  Este procedimento se executa para cada estação telemétrica. 
*  O primeiro e obter a data da última atualização feita para a estação telemétrica. 
*  Desde a fonte de dados algumas medições podem ficar temporalmente instáveis, por esse motivo e que se pegam três dias 
*  à trás com o para corrigir esses valores instáveis  
* */
@Component
public class AtualizacaoANATask implements Runnable {

	@Autowired
	private ANAWSDao anaWsDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	private Estacao estacao;
	
	public void run() {
		
		
		Date ultimaData = dadosEstacaoTelemetricaDao.obterUltimaDataAtualizacao(estacao);
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		calendar.add(Calendar.DAY_OF_MONTH, -3);
		
		if (ultimaData.before(calendar.getTime())){
			calendar.setTime(ultimaData);
		}
		
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);	
		
		Date dataInicio = calendar.getTime();
		
		calendar.setTime(new Date());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);	
		
		Date dataFim = calendar.getTime();
		
		dadosEstacaoTelemetricaDao.apagarDadosChuvas15Min(estacao, dataInicio, dataFim);
		
		Document document = anaWsDao.getDataEstacao(estacao.getCodigo(), dataInicio, dataFim);
		
		dadosEstacaoTelemetricaDao.updateDadosChuvas15Min(estacao, document);
		
		DadosEstacaoTelemetrica15Min ultimoRegistro = dadosEstacaoTelemetricaDao.getUltimoRegistro(estacao);
		
		estacaoDao.atualizarEstado(estacao, ultimoRegistro);
		
	}
	
	public Estacao getEstacao() {
		return estacao;
	}

	public void setEstacao(Estacao estacao) {
		this.estacao = estacao;
	}
	
}
