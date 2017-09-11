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
package com.ufpe.redeclima.dao;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ucar.nc2.NCdumpW;

import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.DadoPontoGrade;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.GribLog;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@Component
public class DadoPontoGradeDao  { // implements HMSInput
	
	private static final Logger logger = LoggerFactory.getLogger(DadoPontoGradeDao.class);
	
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathLocalDownload;
	
	@Value("${grib.fatorAjusteChuva}")
	private String fatorAjusteChuva;
	
	@Autowired
	private GribLogDao gribLogDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private GradeDao gradeDao;
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
	
	private Date getDataGrade(String nomeArquivo) throws ParseException{
			String[] partes = nomeArquivo.split("\\+");
			String parteHasta  = partes[1].replace(".grb", " ").trim();
			return dateFormat.parse(parteHasta);
	}
	
	@PersistenceContext
	private EntityManager entityManager;
	
	public DadoPontoGrade findById(Date dataPrevisao, long pontoGradeId){
		
		DadoPontoGrade dadoPontoGrade;
		try{
			dadoPontoGrade = (DadoPontoGrade) entityManager.createQuery("from DadoPontoGrade d where d.dataPrevisao =:dataPrevisao and d.pontoGrade.id =:pontoGradeId")
					  .setParameter("dataPrevisao", dataPrevisao)
					  .setParameter("pontoGradeId", pontoGradeId)
					  .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return dadoPontoGrade;
	}
	
	@Transactional
	public void save(DadoPontoGrade dadoGradeChuva) {	
		entityManager.persist(dadoGradeChuva);
	}
	
	@Transactional
	public void update(DadoPontoGrade dadoGradeChuva) {	
		entityManager.merge(dadoGradeChuva);
	}
	
	@Transactional
	public void atualizarDadosPontoGrade(String nomeArquivo, Grade grade){
		if (grade==null){
			logger.error("Não existe grade definida para o arquivo " + nomeArquivo);
			return;
		}
		this.atualizarDadosPontoGrade(pathLocalDownload, nomeArquivo, grade);
	}
	
	@Transactional
	public void atualizarDadosPontoGrade(GribLog registro){
		if (registro.getGrade()==null){
			logger.error("Não existe grade definida para o arquivo " + registro.getId());
			return;
		}
		this.atualizarDadosPontoGrade(pathLocalDownload, registro.getId(), registro.getGrade());
	}
	
	@Transactional
	public void atualizarDadosPontoGrade(String pastaPeriodoAtual, String nomeArquivo, Grade grade){
		
		if (gribLogDao.isDownloaded(nomeArquivo)){
			
			String pathfile = Paths.get(pastaPeriodoAtual, nomeArquivo).toString();
			
			int factorAjuste = Integer.parseInt(fatorAjusteChuva);
			
			OutputStream outputstream;
			OutputStreamWriter stream;
			BufferedWriter buffer;
			
			FileInputStream fileInputStream;
			InputStreamReader inputStream;
			BufferedReader bufferReader = null;
			Date dataGrade = null;
			
			try {
				
				dataGrade = getDataGrade(nomeArquivo);
				
				outputstream = new FileOutputStream(Paths.get(pastaPeriodoAtual, "dumpCoordenadas_" + nomeArquivo.replace(".grb", ".txt")).toString());
				stream = new OutputStreamWriter(outputstream);
				buffer = new BufferedWriter(stream);
				
				NCdumpW.print(pathfile, buffer, false, true, false, true, "Total_precipitation_surface", null);
				buffer.close();
				stream.close();
				outputstream.close();
				
				fileInputStream = new FileInputStream(Paths.get(pastaPeriodoAtual, "dumpCoordenadas_" + nomeArquivo.replace(".grb", ".txt")).toString());
				inputStream = new InputStreamReader(fileInputStream);
				bufferReader = new BufferedReader(inputStream);
				
				String linha = null;
				int lon = 0;
				int lat = 0;
				
				while ((lon == 0 && lat == 0 && (linha = bufferReader.readLine()) != null))   {
					
					if (linha.startsWith("  dimensions:")){
						linha = bufferReader.readLine();
						lon = Integer.parseInt(linha.substring("    lon =".length(), linha.indexOf(";")).trim());
						linha = bufferReader.readLine();
						lat = Integer.parseInt(linha.substring("    lat =".length(), linha.indexOf(";")).trim());
					}
				}
				
				String[] coordenadasLatitude = null;
				String[] coordenadasLongitude = null;
				
				while ((linha = bufferReader.readLine()) != null){
					if(linha.startsWith(" data:")){
						linha = bufferReader.readLine();
						if(linha.startsWith("lat =")){
							coordenadasLatitude = bufferReader.readLine().split(",");	
						}
						linha = bufferReader.readLine();
						if(linha.startsWith("lon =")){
							coordenadasLongitude = bufferReader.readLine().split(",");	
						}
						
					}
				}
				
				if (lat>0){
					coordenadasLatitude[0] = coordenadasLatitude[0].replace('{', ' ').trim();
					coordenadasLatitude[lat-1] = coordenadasLatitude[lat-1].replace('}', ' ').trim();
				}
				
				if (lon>0){
					coordenadasLongitude[0] = coordenadasLongitude[0].replace('{', ' ').trim();
					coordenadasLongitude[lon-1] = coordenadasLongitude[lon-1].replace('}', ' ').trim();
				}
				
				
				outputstream = new FileOutputStream(Paths.get(pastaPeriodoAtual, "dumpDados_" + nomeArquivo.replace(".grb", ".txt")).toString());
				stream = new OutputStreamWriter(outputstream);
				buffer = new BufferedWriter(stream);
				
				
				NCdumpW.print(pathfile, buffer, false, false, false, true, "Total_precipitation_surface", null);
				buffer.close();
				stream.close();
				outputstream.close();
				
				fileInputStream = new FileInputStream(Paths.get(pastaPeriodoAtual, "dumpDados_" + nomeArquivo.replace(".grb", ".txt")).toString());
				inputStream = new InputStreamReader(fileInputStream);
				bufferReader = new BufferedReader(inputStream);
				
				String[] fila = null;
				DadoPontoGrade dadoPontoGrade = null;
				double latitude = 0;
				double longitude = 0;
				
				while ((linha = bufferReader.readLine()) != null){
					if(linha.startsWith(" data:")){
						linha = bufferReader.readLine();
						if (linha.startsWith("Total_precipitation_surface =")){
							
							linha = bufferReader.readLine();
							linha = bufferReader.readLine();
							
							for (int i=0; i < lat; i++){
								
								linha = bufferReader.readLine();
								fila = linha.split(",");
								latitude = Double.parseDouble(coordenadasLatitude[i].trim());
								
								for(int j = 0; j < lon; j++ ){

									longitude = Double.parseDouble(coordenadasLongitude[j].trim());
									
									if (grade.estaConteudoEmAreaRecorte(longitude, latitude)){
										
										PontoGrade pontoGrade = pontoGradeDao.findByCoordGrade(longitude, latitude, grade.getId());
										
										if (pontoGrade !=null){
											
											dadoPontoGrade = new DadoPontoGrade();
											dadoPontoGrade.setDataPrevisao(dataGrade); 
											dadoPontoGrade.setPontoGrade(pontoGrade);
											
											if(j==0){
												dadoPontoGrade.setChuva(factorAjuste * Double.parseDouble(fila[j].replace('{', ' ').trim()));
											}else if (j == lon-1){
												dadoPontoGrade.setChuva(factorAjuste * Double.parseDouble(fila[j].replace('}', ' ').trim()));
											}else{
												dadoPontoGrade.setChuva(factorAjuste * Double.parseDouble(fila[j].trim()));
											}
											
											DadoPontoGrade pontoExistente = entityManager.find(DadoPontoGrade.class, dadoPontoGrade);
											if(pontoExistente!=null){
												pontoExistente.setChuva(dadoPontoGrade.getChuva());
												update(pontoExistente);
											}else{
												save(dadoPontoGrade);
											}
										}
									}
								}
							}
						}
					}
				}
				
				gribLogDao.registrarProcessado(nomeArquivo, grade);
				bufferReader.close();
				inputStream.close();
				fileInputStream.close();
				
				
			} catch (FileNotFoundException e) {
				// TODO incorporar log 
				e.printStackTrace();
			} catch (IOException e) {
				gribLogDao.registrarEntrada(nomeArquivo, grade);
				logger.error("Error processando arquivo " + nomeArquivo + ": Error de entrada/saida");
				logger.error("*****************************************************************************************************");
				logger.error("Detalhe do error: " + e.getMessage());
				logger.error("*****************************************************************************************************");
			} catch (ParseException e) {
				// TODO incorporar log
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Este metodo insere os dados faltantes correspondentes ao periodo do registro para uma grade
	 * @param registro registro que contem os dados do arquivo procesado
	 * */
	@Transactional
	public void inserirComoFaltante(GribLog registro){
		
		Set<PontoGrade> pontos = registro.getGrade().getPontosGrade();
		for(PontoGrade ponto: pontos){
			if (registro.getGrade().estaConteudoEmAreaRecorte(ponto)){
				
				if (findById(registro.getDataPrevisao(), ponto.getId())==null){
					
					DadoPontoGrade dadoFaltante = new DadoPontoGrade();
					dadoFaltante.setPontoGrade(ponto);
					dadoFaltante.setDataPrevisao(registro.getDataPrevisao());
					dadoFaltante.setChuva(-901D);
					save(dadoFaltante);
				}
			}
		}
		gribLogDao.registrarFimDownload(registro.getId(), registro.getGrade());
		gribLogDao.registrarProcessado(registro.getId(), registro.getGrade());
	}
	
	
	@SuppressWarnings("unchecked")
	public List<DadoPontoGrade> list(){
		return entityManager.createQuery("from DadoPontoGrade d").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<DadoPontoGrade> listByGradeData(PontoGrade pontoGrade, Date dataInicio){
		return entityManager.createQuery("from DadoPontoGrade d where d.pontoGrade.id =:pontoGradeId and d.dataPrevisao >=:dataInicio")
				.setParameter("pontoGradeId", pontoGrade.getId())
				.setParameter("dataInicio", dataInicio)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> listByGradeDataDiario(PontoGrade pontoGrade, Date dataInicio){
		List<Object[]> resultado = entityManager.createQuery("select sum(d.chuva), DAY(d.dataPrevisao) " +
				                                    " from DadoPontoGrade d where d.pontoGrade.id =:pontoGradeId and d.dataPrevisao >=:dataInicio" +
				                                    " group by DAY(d.dataPrevisao) " +
				                                    " order by DAY(d.dataPrevisao)")
				.setParameter("pontoGradeId", pontoGrade.getId())
				.setParameter("dataInicio", dataInicio)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		for (Object[] d: resultado){
			JSONObject jobject = new JSONObject();
			jobject.put("dia", (Integer)d[1]);
			jobject.put("chuva", (Double)d[0]);
			resultsList.add(jobject);
		}
		
		return resultsList;
	}
	
	/**
	 * Retorna os dados de previsão chuvas diarios em um periodo para um ponto de grade
	 * @param pontoGrade ponto de referencia da grade
	 * @param dataInicio data inicio da serie de previsão
	 * @param dataFim data fim da erie de previsão
	 * @return  dados de pronostico de chuvas
	 * */
	@SuppressWarnings("unchecked")
	public List<JSONObject> listByGradeDataDiario(PontoGrade pontoGrade, Date dataInicio, Date dataFim){
		List<Object[]> resultado = entityManager.createQuery("select sum(d.chuva), YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao) " +
				                                    " from DadoPontoGrade d where d.pontoGrade.id =:pontoGradeId and d.dataPrevisao >=:dataInicio and d.dataPrevisao <=:dataFim " +
				                                    " group by YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao) " +
				                                    " order by YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao)")
				.setParameter("pontoGradeId", pontoGrade.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		SimpleDateFormat dateFormatJSON = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		
		Calendar calendar = Calendar.getInstance();
		
		for (Object[] d: resultado){
			JSONObject jobject = new JSONObject();
			calendar.set(((Integer)d[1]).intValue(), ((Integer)d[2]).intValue() -1 , ((Integer)d[3]).intValue() , 7 , 0);
			jobject.put("data", dateFormatJSON.format(calendar.getTime()));
			jobject.put("chuva", (Double)d[0]);
			resultsList.add(jobject);
		}
		
		return resultsList;
	}
	
	/**
	 * Retorna os dados de previsão chuvas horario em um periodo para um ponto de grade
	 * @param pontoGrade ponto de referencia da grade
	 * @param dataInicio data inicio da serie de previsão
	 * @param dataFim data fim da erie de previsão
	 * @return  dados de pronostico de chuvas
	 * */
	@SuppressWarnings("unchecked")
	public List<JSONObject> listByGradeDataHorario(PontoGrade pontoGrade, Date dataInicio, Date dataFim){
		List<Object[]> resultado = entityManager.createQuery("select d.chuva, d.dataPrevisao " +
				                                    " from DadoPontoGrade d where d.pontoGrade.id =:pontoGradeId and d.dataPrevisao >=:dataInicio and d.dataPrevisao <=:dataFim " +
				                                    " order by d.dataPrevisao")
				.setParameter("pontoGradeId", pontoGrade.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		SimpleDateFormat dateFormatJSON = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		
		for (Object[] d: resultado){
			JSONObject jobject = new JSONObject();
			jobject.put("data", dateFormatJSON.format((Date)d[1]));
			jobject.put("chuva", (Double)d[0]);
			resultsList.add(jobject);
		}
		
		return resultsList;
	}
	
	/**
	 * Este metodo retorna a primeira data de registro de dados para uma grade
	 * @param grade grade para a qual se retornarao os dados
	 * */
	public Date obterPrimeiraData(Grade grade){
		Date primeiraData = (Date) entityManager.createQuery("select min(d.dataPrevisao) from DadoPontoGrade d, PontoGrade p" +
				" where d.pontoGrade.id = p.id and p.grade.id =:gradeId ")
				.setParameter("gradeId", grade.getId())
				.getSingleResult();
		
		if (primeiraData == null){
			Calendar calendario = Calendar.getInstance();
			calendario.add(Calendar.DAY_OF_MONTH, -6); //TODO pasar para properties ou constantes quantidade de dias para atras que em geral o sistema mantem os gribs
			return calendario.getTime();
		}
		
		return primeiraData; 
	}
	
	
	/**
	 * Este metodo retorna a ultima data de registro de dados para uma grade
	 * @param grade grade para a qual se retornarao os dados
	 * */
	public Date obterUltimaData(Grade grade){
		Date ultimaData = (Date) entityManager.createQuery("select max(d.dataPrevisao) from DadoPontoGrade d, PontoGrade p" +
				          " where d.pontoGrade.id = p.id and p.grade.id =:gradeId ")
				          .setParameter("gradeId", grade.getId())
				          .getSingleResult();
		
		if (ultimaData == null){
			return new Date();
		}		
		
		return ultimaData;
	}
	
	
	/**
	 * Este metodo retorna todos os intervalos faltantes entre duas datas para uma determinada grade
	 * @param grade identifica a grade de dados
	 * @param dataInicio inicio da serie
	 * @param dataFim fim da serie
	 * */
	@SuppressWarnings("unchecked")
	public List<Date> listaDatasIntervalosFaltantes(Grade grade, Date dataInicio, Date dataFim){
		
		String interval = grade.getQuantidadeTempoPeriodo() + " " + grade.getUnidadeTempo().getCodigo();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		List<Timestamp> resultado;
		List<Date> resultsList = new ArrayList<Date>();
		resultado = entityManager.createNativeQuery("select \"getMissingTimes\"(:dataInicio, :dataFim, :intervalo)")
			      .setParameter("dataInicio", dateFormat.format(dataInicio))
			      .setParameter("dataFim", dateFormat.format(dataFim))
			      .setParameter("intervalo", interval)
			      .getResultList();
				
		for (Timestamp d: resultado){
			resultsList.add(new Date(d.getTime()));
		}
		
		
		return resultsList;
		
	}
	
	/**
	 * Este metodo preenche os intervalos faltantes de uma grade
	 * @param grade identidicador da grade
	 * @param datas lista de datas dos intervalos faltantes
	 * */
	@Transactional
	public void preencherIntervalosFaltantes(Grade grade, List<Date> datas){
		if (datas!=null){
			Set<PontoGrade> pontosGrade = grade.getPontosGrade();
			for(Date data: datas){
				for(PontoGrade ponto: pontosGrade){
					DadoPontoGrade dadoFaltante = new DadoPontoGrade();
					dadoFaltante.setPontoGrade(ponto);
					dadoFaltante.setDataPrevisao(data);
					dadoFaltante.setChuva(-901D);
					save(dadoFaltante);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.ufpe.redeclima.interfaces.HMSInput#list(com.ufpe.redeclima.interfaces.SimDto)
	 */
	public List<JSONObject> list(PontoGrade pontoGrade, SimDto simDto) {
		
		List<JSONObject> dados = null;
		
		if (simDto.getUnidade()==EnumUnidadeTempo.HORA){
			dados = listByGradeDataHorario(pontoGrade, simDto.getDataInicial(), simDto.getDataFinal());
		}else if (simDto.getUnidade()==EnumUnidadeTempo.DIA){
			dados = listByGradeDataDiario(pontoGrade, simDto.getDataInicial(), simDto.getDataFinal());
		}
		
		return dados;
		
	}
	
	
}
