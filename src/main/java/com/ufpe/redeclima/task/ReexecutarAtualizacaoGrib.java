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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.CptecWsDao;
import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.GribLogDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.GribLog;

/**
 * @author edwardtz
 * Esta classe se encarga de realizar a re-execução de atualização de arquivos grib que não foram corretamente processados
 */
@Component
public class ReexecutarAtualizacaoGrib implements Runnable {

	/* Path de download local ode os arquivos serão salvos */
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathLocalDownload;
	
	/* Maximo de dias anteriores disponiveis na fonte FTP */
	@Value("${grib.maximoDiasAnterioresDinsponiveis}")
	private String maximoDiasAnterioresDinsponiveis;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private DadoPontoGradeDao dadosPontoGradeDao;
	
	@Autowired
	private CptecWsDao cptecWsDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private GribLogDao  gribLogDao;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {

		int maximoDiasAnteriores = Integer.parseInt(maximoDiasAnterioresDinsponiveis);
		Calendar calendario = Calendar.getInstance();
		calendario.add(Calendar.DAY_OF_MONTH, -maximoDiasAnteriores);
		Date dataLimiteDisponivel = calendario.getTime();
		
		List<GribLog> registros = gribLogDao.listNaoProcessados();
		
		if (registros!=null){
			
			for(GribLog registro: registros){
				
				if (registro.getDataPrevisao().before(dataLimiteDisponivel)){
					
					dadosPontoGradeDao.inserirComoFaltante(registro);
					
				}else {
					// Fazer download dos arquivos para a data atual
					cptecWsDao.downloadDadosPrevisao(registro);
					
					dadosPontoGradeDao.atualizarDadosPontoGrade(registro);
				}
			}
		}
		
		// Finalmente conferir se tem intervalos faltantes
		List<Grade> gradesAtivas = gradeDao.listAtivas();
		calendario = Calendar.getInstance();
		
		for(Grade grade: gradesAtivas){
			
			Date dataInicio = dadosPontoGradeDao.obterPrimeiraData(grade);
			calendario.setTime(dataInicio);
			calendario.set(Calendar.HOUR_OF_DAY, 0);
			calendario.set(Calendar.MINUTE, 0);
			calendario.set(Calendar.SECOND, 0);
			dataInicio = calendario.getTime();
			
			Date dataFim = dadosPontoGradeDao.obterUltimaData(grade);
			
			List<Date> datas = dadosPontoGradeDao.listaDatasIntervalosFaltantes(grade, dataInicio, dataFim);
			
			dadosPontoGradeDao.preencherIntervalosFaltantes(grade, datas);
			
			Collections.sort(datas);
			
		}
		
	}

}
