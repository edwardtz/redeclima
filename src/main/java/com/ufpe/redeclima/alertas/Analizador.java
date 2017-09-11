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
package com.ufpe.redeclima.alertas;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.MensagemDao;
import com.ufpe.redeclima.dao.TipoNotificacaoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.messenger.EmailMessenger;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Mensagem;
import com.ufpe.redeclima.model.TipoNotificacao;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumPerfil;
import com.ufpe.redeclima.util.EnumTipoMensagem;
import com.ufpe.redeclima.util.EnumTipoResponsavel;


/**
 * @author edwardtz
 * Esta classe se encarrega de registrar os logs de execução e atividades que precissarem ficar registradas no sistema
 * para seu monitoramento, entre os eventos de ineteresse podem estar, eventos de segurança login/logout mudança de dados e configurações
 * erros especificos, etc
 */
@Aspect
@Component
public class Analizador {

	@Value("${alerta.chuva.observada}")
	private String limiteChuvaValor;
	
	private static final Logger logger = LoggerFactory.getLogger(Analizador.class);
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private EmailMessenger emailMessenger;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private MensagemDao mensagemDao;
	
	@Autowired
	private TipoNotificacaoDao tipoNotificacaoDao;
	
	@Pointcut("execution(* com.ufpe.redeclima.task.TaskManager.executarAtualizacaoANA(..))")
	public void runpoint(){}
	
	@Before("runpoint()")
	public void logBefore() {
	}
	
	@After("runpoint()")
	public void logAfter() {
		
		if (limiteChuvaValor==null){
			limiteChuvaValor="0.0";
		}
		
		double limiteChuva = Double.valueOf(limiteChuvaValor);
		
		List<Estacao> estacoes = estacaoDao.listEstacoes(EnumTipoResponsavel.ANA);
		
		Calendar calendario = Calendar.getInstance();
		Date dataFinal = new Date();
		calendario.setTime(dataFinal);
		calendario.add(Calendar.HOUR_OF_DAY, -12);
		StringBuilder messageBuilder = new StringBuilder();
		Date dataInicial = calendario.getTime();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		boolean informar = false;
		
		calendario.setTime(dataFinal);
		calendario.add(Calendar.HOUR_OF_DAY, -6);
		Date dataInicialCheck = calendario.getTime();
		
		messageBuilder.append("Sistema de alerta de enchentes MAVEN informa o seguinte evento ");
		messageBuilder.append(System.getProperty("line.separator"));
		messageBuilder.append(System.getProperty("line.separator"));
		
		if (estacoes!=null){
			for(Estacao estacao: estacoes){
				double chuvaPeriodo = dadosEstacaoTelemetricaDao.sumaChuva(estacao, dataInicial, dataFinal);
				
				// Verificar se ja não foi enviado num periodo de 6 hs, se ja foi enviado so enviará novamente se o evento persistir daqui a 6 hs
				boolean enviada = mensagemDao.alertaEnviada(estacao, EnumTipoMensagem.ALERTAOBSCHUVA, dataInicialCheck, dataFinal);
				
				if (chuvaPeriodo >= limiteChuva && !enviada){
					messageBuilder.append("Estação " + estacao.getNome() + " resgistrou " + String.format("%.2f", (double) chuvaPeriodo) + " mm de chuva nas ultimas 12 horas");
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append("Periodo entre " + dateFormat.format(dataInicial) + " e " + dateFormat.format(dataFinal));
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append("Bacia " + estacao.getBacia().getNome() + " Longitude " + String.format("%.2f", (double) estacao.getLongitude()) + " Latitude " + String.format("%.2f", (double) estacao.getLatitude()));
					messageBuilder.append(System.getProperty("line.separator"));
					messageBuilder.append(System.getProperty("line.separator"));
					informar=true;
					Mensagem msg = new Mensagem();
					msg.setDataEnvio(dataFinal);
					msg.setEstacao(estacao);
					msg.setTipo(EnumTipoMensagem.ALERTAOBSCHUVA);
					mensagemDao.save(msg);
				}
				
			}
		}
			
		if (informar){
			TipoNotificacao tipo = tipoNotificacaoDao.findById(EnumTipoMensagem.ALERTAOBSCHUVA.getId());
			
			List<Usuario> usuarios = tipo.getUsuarios();
			
			if(usuarios != null){
				for(Usuario usuario: usuarios){
					emailMessenger.sendMesssage(usuario.getEmail(), "MAVEN Aviso de Alerta dados observados " + dateFormat.format(new Date()), messageBuilder.toString());
				}
			}
		}
			
		logger.info("Terminou de atualizar as estações, e hora de verificar os criterios");
	}

	
}
	
