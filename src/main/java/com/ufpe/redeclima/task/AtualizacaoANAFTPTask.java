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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.ANAWSDao;
import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.model.Estacao;

/**
*  Esta classe executa a tarefa de atualizar os dados de nível e chuva das estações telemétricas desde a fonte FTP, a fonte FTP é uma fonte alternativa ao web services
*  com a ventagem de obter os dados mais recentes com menor demora, a atualização se faz por cada estação telemétrica
* */
@Component
public class AtualizacaoANAFTPTask implements Runnable {

	@Value("${jaxws.pathDownloadAnaFtp}")
	private String pathDownload;
	
	@Autowired
	private ANAWSDao anaWsDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	private Estacao estacao;
	
	public void run() {
		
		// Baixar os arquivos
		anaWsDao.baixarDadosFtp(estacao);
		
		// Atualizar a BD e deletar os arquivos
		dadosEstacaoTelemetricaDao.atualizarDadosEstacaoFTP(estacao);
		
	}
	
	public Estacao getEstacao() {
		return estacao;
	}

	public void setEstacao(Estacao estacao) {
		this.estacao = estacao;
	}
	
}
