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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import com.ufpe.redeclima.dto.SimulacaoSerieDto;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.DadoSeriePrevisao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 *
 */
@Component
public class DadoSeriePrevisaoDao {
	
	private static final Logger logger = LoggerFactory.getLogger(DadoSeriePrevisaoDao.class);
	
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
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void save(DadoSeriePrevisao dadoSeriePrevisao) {	
		entityManager.persist(dadoSeriePrevisao);
	}
	
	@Transactional
	public void saveOrUpdate(DadoSeriePrevisao dadoSeriePrevisao) {	
		entityManager.merge(dadoSeriePrevisao);
	}
	
	
	public DadoSeriePrevisao findById(Date dataPrevisao, SeriePrevisao serie, PontoGrade pontoGrade){
		return findById(dataPrevisao, serie.getId(), pontoGrade.getId());
	}
	
	public DadoSeriePrevisao findById(Date dataPrevisao, long serieId, long pontoGradeId){
		
		DadoSeriePrevisao dadoSeriePrevisao;
		try{
			dadoSeriePrevisao = (DadoSeriePrevisao) entityManager.createQuery("from DadoSeriePrevisao d where d.dataPrevisao=:dataPrevisao and d.serie.id=:serieId and d.pontoGrade.id=:pontoGradeId ")
					  .setParameter("dataPrevisao", dataPrevisao)
					  .setParameter("serieId", serieId)
					  .setParameter("pontoGradeId", pontoGradeId)
					  .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return dadoSeriePrevisao;
	}
	
	@Transactional
	public void atualizarDadosSerie(String nomeArquivo, SeriePrevisao serie){
		if(serie==null){
			logger.error("Não existe serie definida para o arquivo " + nomeArquivo);
			return;
		} else if (serie.getGrade()==null){
			logger.error("Não existe grade definida para o arquivo " + nomeArquivo);
			return;
		}
		atualizarDadosSerie(pathLocalDownload, nomeArquivo, serie);
	}
	
	@Transactional
	public void atualizarDadosSerie(String pastaPeriodoAtual, String nomeArquivo, SeriePrevisao serie){
		
		if (gribLogDao.isDownloaded(nomeArquivo)){
			
			String pathfile = Paths.get(pastaPeriodoAtual, nomeArquivo).toString();
			
			int factorAjuste = Integer.parseInt(fatorAjusteChuva);
			
			OutputStream outputstream;
			OutputStreamWriter stream;
			BufferedWriter buffer;
			
			FileInputStream fileInputStream;
			InputStreamReader inputStream;
			BufferedReader bufferReader = null;
			Date dataPrevisao = null;
			
			try {
				
				dataPrevisao = getDataPrevisao(nomeArquivo);
				
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
				DadoSeriePrevisao dadoSeriePrevisao = null;
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
									
									Grade grade = serie.getGrade();
									
									if (grade.estaConteudoEmAreaRecorte(longitude, latitude)){
										
										PontoGrade pontoGrade = pontoGradeDao.findByCoordGrade(longitude, latitude, grade.getId());
										
										if (pontoGrade !=null){
											
											dadoSeriePrevisao = new DadoSeriePrevisao();
											dadoSeriePrevisao.setDataPrevisao(dataPrevisao); 
											dadoSeriePrevisao.setPontoGrade(pontoGrade);
											dadoSeriePrevisao.setSerie(serie);
											
											if(j==0){
												dadoSeriePrevisao.setChuva(factorAjuste * Double.parseDouble(fila[j].replace('{', ' ').trim()));
											}else if (j == lon-1){
												dadoSeriePrevisao.setChuva(factorAjuste * Double.parseDouble(fila[j].replace('}', ' ').trim()));
											}else{
												dadoSeriePrevisao.setChuva(factorAjuste * Double.parseDouble(fila[j].trim()));
											}
											
											DadoSeriePrevisao pontoExistente = findById(dataPrevisao, serie.getId(), pontoGrade.getId());
											if(pontoExistente!=null){
												pontoExistente.setChuva(dadoSeriePrevisao.getChuva());
												saveOrUpdate(pontoExistente);
											}else{
												save(dadoSeriePrevisao);
											}
										}
									}
								}
							}
						}
					}
				}
				
				gribLogDao.registrarProcessadoSeries(nomeArquivo);
				bufferReader.close();
				inputStream.close();
				fileInputStream.close();
				
				
			} catch (FileNotFoundException e) {
				logger.error("*****************************************************************************************************");
				logger.error("Arquivo no encontrado" + nomeArquivo);
				logger.error("Detalhe do error: " + e.getMessage());
				logger.error("*****************************************************************************************************");
			} catch (IOException e) {
				logger.error("*****************************************************************************************************");
				logger.error("Error processando serie arquivo " + nomeArquivo + ": Error de entrada/saida");
				logger.error("Detalhe do error: " + e.getMessage());
				logger.error("*****************************************************************************************************");
			} catch (ParseException e) {
				logger.error("*****************************************************************************************************");
				logger.error("Error de parsing");
				logger.error("Detalhe do error: " + e.getMessage());
				logger.error("*****************************************************************************************************");
			}
		}
	}
	
	
	/**
	 * Retorna a lista de pontos de grade de uma serie
	 * @param serie serie de dados
	 * */
	@SuppressWarnings("unchecked")
	public List<PontoGrade> listPontos(SeriePrevisao serie){
		return entityManager.createQuery("select distinct d.pontoGrade.id from DadoSeriePrevisao d where d.serie.id=:serieId ")
				 .setParameter("serieId", serie.getId())
				 .getResultList();
	}
	
	/**
	 * Retorna a lista de datas de uma serie
	 * @param serie serie de dados
	 * */
	@SuppressWarnings("unchecked")
	public List<Date> serieTempo(SeriePrevisao serie){
		return entityManager.createQuery("select distinct d.dataPrevisao from DadoSeriePrevisao d where d.serie.id=:serieId order by d.dataPrevisao ")
				 .setParameter("serieId", serie.getId())
				 .getResultList();
	}
	
	/**
	 * Retorna uma matris no seguinte formato
	 * Date     | 2013/11/21 00:00:00 | 202013/11/21 03:00:00 | ... 
	 * PontoNome|    0.1			  |          0.3	      | ...
	 * .
	 * .
	 * .
	 * PontoNomeN|    0.1			  |          0.3	      | ...
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public String[][] exportarDados(SeriePrevisao serie){
		
		List<DadoSeriePrevisao> dados = entityManager.createQuery("from DadoSeriePrevisao d where d.serie.id=:serieId order by d.pontoGrade.id ")
													 .setParameter("serieId", serie.getId())
													 .getResultList();
		
		List<Date> dates = serieTempo(serie);
		
		int colunas = dates.size();
		
		int filas = listPontos(serie).size();
		
		String[][] resultado = new String[filas+1][colunas+1];
		
		resultado[0][0]="Date";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		
		int j=1;
		for (Date d: dates){
			resultado[0][j]=dateFormat.format(d);
			j++;
		}
		
		PontoGrade pontoAtual = dados.get(0).getPontoGrade();
		resultado[1][0]="P" + dados.get(0).getPontoGrade().getId();
		
		int i=1;
		j=0;
		for (DadoSeriePrevisao dado: dados){
			if (pontoAtual.getId() != dado.getPontoGrade().getId()){
				pontoAtual=dado.getPontoGrade();
				j=0;
				i++;
				resultado[i][j]="P" + dado.getPontoGrade().getId();
			}
			j++;
			resultado[i][j]=dado.getChuva().toString();
		}
		
		return resultado;
	}

	
	/**
	 * Retorna os dados de uma serie de previsão para um ponto dado
	 * @param serie serie de previsão
	 * @param pontoGrade ponto da serie
	 * */
	@SuppressWarnings("unchecked")
	public List<DadoSeriePrevisao> listBy(PontoGrade pontoGrade, SeriePrevisao serie){
		
		return entityManager.createQuery("from DadoSeriePrevisao d where d.serie.id=:serieId and d.pontoGrade.id=:pontoGradeId order by d.dataPrevisao  ")
													 .setParameter("serieId", serie.getId())
													 .setParameter("pontoGradeId", pontoGrade.getId())
													 .getResultList();
	}
	
	/**
	 * Retorna os dados de uma serie de previsão para um ponto dado
	 * @param serie serie de previsão
	 * @param pontoGrade ponto da serie
	 * */
	@SuppressWarnings("unchecked")
	public List<JSONObject> listBy(PontoGrade pontoGrade, SimDto simDto){
		
		SimulacaoSerieDto params = (SimulacaoSerieDto) simDto;
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		List<Object[]> resultado;
		
		SimpleDateFormat dateFormatJSON = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		
		if (simDto.getUnidade()==EnumUnidadeTempo.HORA){
			
			resultado = entityManager.createQuery("select d.chuva, d.dataPrevisao" +
					                                            " from DadoSeriePrevisao d where d.serie.id=:serieId and d.pontoGrade.id=:pontoGradeId " +
					                                            " order by d.dataPrevisao  ")
					                                .setParameter("serieId", params.getSeriePrevisao().getId())
					                                .setParameter("pontoGradeId", pontoGrade.getId())
					                                .getResultList();

			
		
			for (Object[] d: resultado){
				JSONObject jobject = new JSONObject();
				jobject.put("data", dateFormatJSON.format((Date)d[1]));
				jobject.put("chuva", (Double)d[0]);
				resultsList.add(jobject);
			}


		}else if (simDto.getUnidade()==EnumUnidadeTempo.DIA){
		
			resultado = entityManager.createQuery("select sum(d.chuva), YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao) " +
                    											" from DadoSeriePrevisao d where d.serie.id=:serieId and d.pontoGrade.id =:pontoGradeId " +
                    											" group by YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao) " +
																" order by YEAR(d.dataPrevisao), MONTH(d.dataPrevisao), DAY(d.dataPrevisao)")
													.setParameter("serieId", params.getSeriePrevisao().getId())
													.setParameter("pontoGradeId", pontoGrade.getId())
													.getResultList();
			
			
			Calendar calendar = Calendar.getInstance();
			
			for (Object[] d: resultado){
				JSONObject jobject = new JSONObject();
				calendar.set(((Integer)d[1]).intValue(), ((Integer)d[2]).intValue() -1 , ((Integer)d[3]).intValue() , 7 , 0);
				jobject.put("data", dateFormatJSON.format(calendar.getTime()));
				jobject.put("chuva", (Double)d[0]);
				resultsList.add(jobject);
			}
				
		}
		
		return resultsList;
	}
	
	
	/**
	 * Retorna a data de previsão correspondente do arquivo desde um fragmento do nome do arquivo
	 * */
	private Date getDataPrevisao(String nomeArquivo) throws ParseException{
		String[] partes = nomeArquivo.split("\\+");
		String parteHasta  = partes[1].replace(".grb", " ").trim();
		return dateFormat.parse(parteHasta);
	}
	
}
