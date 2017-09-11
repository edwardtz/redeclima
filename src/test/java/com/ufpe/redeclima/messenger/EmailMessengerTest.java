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
package com.ufpe.redeclima.messenger;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.MensagemDao;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Mensagem;
import com.ufpe.redeclima.util.EnumTipoMensagem;
import com.ufpe.redeclima.util.EnumTipoResponsavel;

/**
 * @author edwardtz
 *
 */
@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class EmailMessengerTest {

	
	@Autowired
	private EmailMessenger emailMessenger;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private MensagemDao mensagemDao;
	
	/**
	 * Test method for {@link com.ufpe.redeclima.messenger.EmailMessenger#sendMesssage()}.
	 */
	@Test
	public void testSendMesssage() {
		
		List<Estacao> estacoes = estacaoDao.listEstacoes(EnumTipoResponsavel.ANA);
		StringBuilder messageBuilder = new StringBuilder();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Date dataInicial=null;
		Date dataFinal=null;
		try {
			dataInicial = dateFormat.parse("19/03/2014 00:01");
			dataFinal = dateFormat.parse("19/03/2014 23:59");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (estacoes!=null){
			for(Estacao estacao: estacoes){
				double chuvaHoraria = dadosEstacaoTelemetricaDao.sumaChuva(estacao, dataInicial, dataFinal);
				//boolean enviada = mensagemDao.alertaEnviada(estacao, EnumTipoMensagem.ALERTAOBSCHUVA, dataInicialCheck, dataFinal);
				
				if(chuvaHoraria>=15 ){ //&& !enviada
					messageBuilder.append("Estação " + estacao.getNome() + " resgistrou " + String.format("%.2f", (double) chuvaHoraria) + " mm de chuva nas ultimas 12 horas");
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append("Periodo entre " + dateFormat.format(dataInicial) + " e " + dateFormat.format(dataFinal));
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append("Bacia " + "Longitude " + String.format("%.2f", (double) estacao.getLongitude()) + " Latitude " + String.format("%.2f", (double) estacao.getLatitude()));
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append(System.getProperty("line.separator"));
					Mensagem msg = new Mensagem();
					msg.setDataEnvio(new Date());
					msg.setEstacao(estacao);
					msg.setTipo(EnumTipoMensagem.ALERTAOBSCHUVA);
					mensagemDao.save(msg);
				}
			}
			
			if (messageBuilder.toString().length() > 0){
						emailMessenger.sendMesssage("edwardtz@gmail.com", "Teste MAVEN Aviso de Alerta dados observados " + dateFormat.format(new Date()), messageBuilder.toString());
			}
			
		}
		
	}

}
