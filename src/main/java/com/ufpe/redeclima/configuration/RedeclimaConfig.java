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
package com.ufpe.redeclima.configuration;


import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.task.InicializacaoDadosTask;
import com.ufpe.redeclima.task.InicializacaoGRIBData;


/**
 * Esta classe tem a função de inicializar a BD, configurar os parametros gerais do sistema e criar os bean no inicio da aplicação
 * tambem implementa as interfaces InitializingBean e DisposableBean as quais executam metodos quando se inicia e apaga a aplicação no servidor,.
 * Toda informação o o objetos que devam estar criados no inicio do sistema se deveram criar em esta classe
 * */
@Configuration
public class RedeclimaConfig implements InitializingBean, DisposableBean{

	@Autowired
	private InicializacaoDadosTask initDados;
	
	@Autowired
	private InicializacaoGRIBData inicializacaoGRIBData;
	
	@Autowired
	private GradeDao gradeDao;

	/**
	 * Este metodo tem como objetivo inicializar os dados de previsao a partir dos gribs que esteverem na pasta de download
	 * no caso de atualização de bd ou sistema ficar fora do ar por un tempo a bd pode ser atualizada copiando as pastas
	 * */
	private void inicializarGrib(){
		for (Grade grade : gradeDao.listAtivas()){
			inicializacaoGRIBData.setGradeAtual(grade);
			inicializacaoGRIBData.run();
		}
	}
	
	/**
	 * Este metodo se executa no inicio do aplicativo no servidor (Startup)
	 * */
	public void afterPropertiesSet() throws Exception {
		/* Inicializa a base de dados com dados basicos no inicio da aplicação */
		initDados.run();
		
		/* Inicializa os dados de previsao a partir de arquivos grib */
		inicializarGrib();
		
	}
	
	/**
	 * Este metodo se executa no apagado do aplicativo no servidor (Shutting down)
	 * */
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
