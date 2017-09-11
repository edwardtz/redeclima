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
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import net.sf.json.JSONObject;

import org.apache.commons.io.comparator.NameFileComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15Min;
import com.ufpe.redeclima.model.DadosEstacaoTelemetrica15MinId;
import com.ufpe.redeclima.model.DadosEstacaoTelemetricaChuvaFTP;
import com.ufpe.redeclima.model.DadosEstacaoTelemetricaNivelFTP;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.util.EnumCodigoSensorEstacao;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Component
public class DadosEstacaoTelemetricaDao {

	private static final Logger logger = LoggerFactory.getLogger(DadosEstacaoTelemetricaDao.class);
	
	//TODO: passar a query simple
	private static String INSERT_DADO_ESTACAO_TELEMETRICA_15_MIN = "INSERT INTO t_dados_estacao_telemetrica_15_min (data, chuva, nivel, vazao, estacao_id_estacao) VALUES(?,?,?,?,?)";
	
	@Value("${jaxws.pathDownloadAnaFtp}")
	private String pathDownload;
	
	@Autowired
	private FTPEstacaoLogDao ftpEstacaoLogDao;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(DadosEstacaoTelemetrica15Min dadoChuva) {
		entityManager.persist(dadoChuva);
	}
	
	@Transactional
	public void saveOrUpdate(DadosEstacaoTelemetrica15Min dadoChuva) {
		entityManager.merge(dadoChuva);
	}
	
	@Transactional
	public DadosEstacaoTelemetrica15Min findById(DadosEstacaoTelemetrica15MinId id){
		return entityManager.find(DadosEstacaoTelemetrica15Min.class, id);
	}
	
	@Transactional
	public void updateDadosChuvas15Min(Estacao estacao, Document document){
		
		if (document!=null){
			Calendar calendario = Calendar.getInstance();
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //TODO: melhora passar o formato para uma propertie
			
			Query query = entityManager.createNativeQuery(INSERT_DADO_ESTACAO_TELEMETRICA_15_MIN, DadosEstacaoTelemetrica15Min.class);
			
			NodeList nList = document.getElementsByTagName("DadosHidrometereologicos");
			
			//Definir o elemento anterior
			Date dataAnterior=null;
			
			Date dataProximaSecuencia=null;
			
			DadosEstacaoTelemetrica15Min dado = new DadosEstacaoTelemetrica15Min();
			
			try {
				
				for (int i = 0; i < nList.getLength(); i++) {
					
					Node nNode = nList.item(i);
					
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						
						Element eElement = (Element) nNode;
						
						if (i>0){
							calendario.setTime(dataAnterior);
							calendario.add(Calendar.MINUTE, -15);
							dataProximaSecuencia =  dateFormat.parse(dateFormat.format(calendario.getTime()));
							Date dataNodoActual = dateFormat.parse(eElement.getElementsByTagName("DataHora").item(0).getTextContent());
							while(dataProximaSecuencia.after(dataNodoActual)){
								query.setParameter(1, dateFormat.parse(dateFormat.format(dataProximaSecuencia)));
								query.setParameter(2, -901.0);
								query.setParameter(3, -901.0);
								query.setParameter(4, -901.0);
								query.setParameter(5, estacao.getId());
								query.executeUpdate();
								
								calendario.setTime(dataProximaSecuencia);
								calendario.add(Calendar.MINUTE, -15);
								dataProximaSecuencia = dateFormat.parse(dateFormat.format(calendario.getTime()));
							}
							dataAnterior = dataProximaSecuencia;
						}else{
							dataAnterior = dateFormat.parse(eElement.getElementsByTagName("DataHora").item(0).getTextContent());
						}
						
						
						query.setParameter(1, dateFormat.parse(eElement.getElementsByTagName("DataHora").item(0).getTextContent()));
						
						if (eElement.getElementsByTagName("Chuva").item(0).getTextContent() != ""){
							query.setParameter(2, Double.parseDouble(eElement.getElementsByTagName("Chuva").item(0).getTextContent()));	
						}else{
							query.setParameter(2, -901.0);
						}
						
						if (eElement.getElementsByTagName("Vazao").item(0).getTextContent() != ""){
							query.setParameter(4, Double.parseDouble(eElement.getElementsByTagName("Vazao").item(0).getTextContent()));
						}else{
							query.setParameter(4, -901.0);
						}
						
						if (eElement.getElementsByTagName("Nivel").item(0).getTextContent() != ""){
							// Ajuste da regua para as estações da ANA
							double nivel = Double.parseDouble(eElement.getElementsByTagName("Nivel").item(0).getTextContent());
							nivel = (nivel/100) + estacao.getAjusteRegua();
							query.setParameter(3, nivel);
						}else {
							query.setParameter(3, -901.0);
						}
						query.setParameter(5, estacao.getId());
						
						query.executeUpdate();
					}
				}
			} catch (DOMException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	@Transactional
	public void apagarDadosChuvas15Min(Estacao estacao, Date dataInicio, Date dataFim){
		
		//TODO: simplificar
		Query query = entityManager.createQuery("delete from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId and d.Id.data >= :dataInicio and d.Id.data <= :dataFim ");
		query.setParameter("estacaoId", estacao.getId());
		query.setParameter("dataInicio", dataInicio);
		query.setParameter("dataFim", dataFim);
		query.executeUpdate();
		query = entityManager.createQuery("delete from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId and d.Id.data = :dataInicio ");
		query.setParameter("estacaoId", estacao.getId());
		query.setParameter("dataInicio", dataInicio, TemporalType.DATE);
		query.executeUpdate();
		query = entityManager.createQuery("delete from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId and d.Id.data = :dataFim ");
		query.setParameter("estacaoId", estacao.getId());
		query.setParameter("dataFim", dataFim, TemporalType.DATE);
		query.executeUpdate();
	}
	
	public Date obterUltimaDataAtualizacao(Estacao estacao){
		
		Query query = entityManager.createQuery("select max(d.Id.data) from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId")
				                   .setMaxResults(1)
				                   .setParameter("estacaoId", estacao.getId());
				                   
		Object ultimaData = query.getSingleResult();
		
		if (ultimaData!=null){
			return (Date) ultimaData;
		}else {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.DAY_OF_MONTH, 4);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			//calendar.set(2012, 3, 2, 0, 0, 0); //TODO: melhora, passar esso para uma propertie
			return calendar.getTime();
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<DadosEstacaoTelemetrica15Min> listSerieDados(Estacao estacao, Date dataInicio, Date dataFim) {
		return entityManager.createQuery("from DadosEstacaoTelemetrica15Min d where d.Id.id = :estacaoId and d.Id.data >= :dataInicio and d.Id.data <= :dataFim order by d.Id.data " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<DadosEstacaoTelemetricaChuvaFTP> listSerieDadosChuvaFTP(Estacao estacao, Date dataInicio, Date dataFim) {
		return entityManager.createQuery("from DadosEstacaoTelemetricaChuvaFTP d where d.estacaoId = :estacaoId and d.data >= :dataInicio and d.data <= :dataFim order by d.data " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<DadosEstacaoTelemetricaNivelFTP> listSerieDadosNivelFTP(Estacao estacao, Date dataInicio, Date dataFim) {
		return entityManager.createQuery("from DadosEstacaoTelemetricaNivelFTP d where d.estacaoId = :estacaoId and d.data >= :dataInicio and d.data <= :dataFim order by d.data " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> listSerieDadosHora(Estacao estacao, Date dataInicio, Date dataFim) {
		
		List<Object[]> results = entityManager.createQuery("select sum(d.chuva), avg(d.nivel), avg(d.vazao), YEAR(d.Id.data), MONTH(d.Id.data), DAY(d.Id.data), HOUR(d.Id.data) " +
				                         " from DadosEstacaoTelemetrica15Min d " +
				                         " where d.Id.id = :estacaoId and d.Id.data >= :dataInicio and d.Id.data <= :dataFim " +
				                         " group by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data), HOUR(d.Id.data) " +
				                         " order by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data), HOUR(d.Id.data) " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		
		for (Object[] d: results){
			JSONObject jobject = new JSONObject();
			calendar.set(((Integer)d[3]).intValue(), ((Integer)d[4]).intValue() -1 , ((Integer)d[5]).intValue() , ((Integer)d[6]).intValue() , 0);
			jobject.put("data", dateFormat.format(calendar.getTime()));
			jobject.put("chuva", (Double)d[0]);
			jobject.put("nivel",(Double)d[1]);
			jobject.put("vazao", (Double)d[2]);
			resultsList.add(jobject);
		}
		
		return resultsList;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> listSerie(Estacao estacao, SimDto simDto){
		
		List<Object[]> results=null;
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		
		if(simDto.getUnidade() == EnumUnidadeTempo.HORA){
			
			results = entityManager.createQuery("select sum(d.chuva), avg(d.nivel), avg(d.vazao), YEAR(d.Id.data), MONTH(d.Id.data), DAY(d.Id.data), HOUR(d.Id.data) " +
                    							" from DadosEstacaoTelemetrica15Min d " +
                    							" where d.Id.id = :estacaoId and d.Id.data >= :dataInicio and d.Id.data <= :dataFim and d.nivel >= 0 " +
                    							" group by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data), HOUR(d.Id.data) " +
                    							" order by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data), HOUR(d.Id.data) " )
                    							.setParameter("estacaoId", estacao.getId())
                    							.setParameter("dataInicio", simDto.getDataInicial())
                    							.setParameter("dataFim", simDto.getDataFinal())
                    							.getResultList();
					
		}else if (simDto.getUnidade() == EnumUnidadeTempo.DIA){
			
			results = entityManager.createQuery("select sum(d.chuva), avg(d.nivel), avg(d.vazao), YEAR(d.Id.data), MONTH(d.Id.data), DAY(d.Id.data) " +
					" from DadosEstacaoTelemetrica15Min d " +
					" where d.Id.id = :estacaoId and d.Id.data >= :dataInicio and d.Id.data <= :dataFim and d.nivel >= 0 " +
					" group by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data) " +
					" order by YEAR(d.Id.data),MONTH(d.Id.data),DAY(d.Id.data) " )
					.setParameter("estacaoId", estacao.getId())
					.setParameter("dataInicio", simDto.getDataInicial())
					.setParameter("dataFim", simDto.getDataFinal())
					.getResultList();
		}
		
		if (results != null && !results.isEmpty()){
			for (Object[] d: results){
				JSONObject jobject = new JSONObject();
				calendar.set(((Integer)d[3]).intValue(), ((Integer)d[4]).intValue() -1 , ((Integer)d[5]).intValue());
				jobject.put("data", dateFormat.format(calendar.getTime()));
				jobject.put("chuva", (Double)d[0]);
				jobject.put("nivel",(Double)d[1]);
				jobject.put("vazao", (Double)d[2]);
				resultsList.add(jobject);
			}
		}
		
		return resultsList;
		
	}

	@SuppressWarnings("unchecked")
	public List<JSONObject> listSerieDadosHoraChuvaFTP(Estacao estacao, Date dataInicio, Date dataFim) {
		
		List<Object[]> results = entityManager.createQuery("select sum(d.valor), YEAR(d.data), MONTH(d.data), DAY(d.data), HOUR(d.data) " +
				                         " from DadosEstacaoTelemetricaChuvaFTP d " +
				                         " where d.estacaoId = :estacaoId and d.data >= :dataInicio and d.data <= :dataFim " +
				                         " group by YEAR(d.data),MONTH(d.data),DAY(d.data), HOUR(d.data) " +
				                         " order by YEAR(d.data),MONTH(d.data),DAY(d.data), HOUR(d.data) " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		
		for (Object[] d: results){
			JSONObject jobject = new JSONObject();
			calendar.set(((Integer)d[1]).intValue(), ((Integer)d[2]).intValue() -1 , ((Integer)d[3]).intValue() , ((Integer)d[4]).intValue() , 0);
			jobject.put("data", dateFormat.format(calendar.getTime()));
			jobject.put("chuva", (Double)d[0]);
			resultsList.add(jobject);
		}
		
		return resultsList;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<JSONObject> listSerieDadosHoraNivelFTP(Estacao estacao, Date dataInicio, Date dataFim) {
		
		List<Object[]> results = entityManager.createQuery("select avg(d.valor), YEAR(d.data), MONTH(d.data), DAY(d.data), HOUR(d.data) " +
				                         " from DadosEstacaoTelemetricaNivelFTP d " +
				                         " where d.estacaoId = :estacaoId and d.data >= :dataInicio and d.data <= :dataFim " +
				                         " group by YEAR(d.data),MONTH(d.data),DAY(d.data), HOUR(d.data) " +
				                         " order by YEAR(d.data),MONTH(d.data),DAY(d.data), HOUR(d.data) " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicio", dataInicio)
				.setParameter("dataFim", dataFim)
				.getResultList();
		
		List<JSONObject> resultsList = new ArrayList<JSONObject>();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
		Calendar calendar = Calendar.getInstance();
		
		for (Object[] d: results){
			JSONObject jobject = new JSONObject();
			calendar.set(((Integer)d[1]).intValue(), ((Integer)d[2]).intValue() -1 , ((Integer)d[3]).intValue() , ((Integer)d[4]).intValue() , 0);
			jobject.put("data", dateFormat.format(calendar.getTime()));
			jobject.put("nivel", (Double)d[0]);
			resultsList.add(jobject);
		}
		
		return resultsList;
		
	}
	
	@SuppressWarnings("unchecked")
	public List<DadosEstacaoTelemetrica15Min> list() {
		return entityManager.createQuery("from DadosEstacaoTelemetrica15Min d").getResultList();
	}
	
	/**
	 * Retorna o ultimo registro de dados que existe na BD para uma estação
	 * @param estacao estação da qual se quer saber o ultimo registro na BD
	 * */
	@SuppressWarnings("unchecked")
	public DadosEstacaoTelemetrica15Min getUltimoRegistro(Estacao estacao) {
		DadosEstacaoTelemetrica15Min ultimoRegistro;
		try{
			ultimoRegistro = (DadosEstacaoTelemetrica15Min) entityManager.createQuery("from DadosEstacaoTelemetrica15Min d where d.Id.id =:estacaoId and d.Id.data = (select max(d2.Id.data) from DadosEstacaoTelemetrica15Min d2 where d2.Id.id = :estacaoId) ")
					.setParameter("estacaoId", estacao.getId())
				    .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return ultimoRegistro;
	}
	
	/**
	 * Retorna o ultimo valor de vazão registrado positivo para uma estação
	 * */
	public Double getUltimoValorVazao(Estacao estacao, Date dataInicial){
		Double vazao;
		try{
			vazao = (Double) entityManager.createQuery("select d.vazao from DadosEstacaoTelemetrica15Min d where d.Id.id =:estacaoId and d.Id.data = (select max(d2.Id.data) from DadosEstacaoTelemetrica15Min d2 " +
																																					 " where d2.Id.id = :estacaoId and d2.Id.data >=:dataInicial and d2.vazao >= 0 ) ")
					.setParameter("estacaoId", estacao.getId())
					.setParameter("dataInicial", dataInicial)
				    .getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return vazao;
	}
	
	
	/**
	 * Lê os arquivos .MIS correspondentes à estação desde um diretorio local e atualiza a BD
	 * @param estacao estação para atualizar
	 * */
	@Transactional
	public void atualizarDadosEstacaoFTP(Estacao estacao){
		
		logger.info("Inicio da atualização de dados para a estação codigo " + estacao.getCodigo());
		
		DadosEstacaoTelemetricaChuvaFTP dadoChuva;
		
		DadosEstacaoTelemetricaNivelFTP dadoNivel;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");
		
		FileInputStream fstream;
		
		String codigoEstacaoString = Integer.toString(estacao.getCodigo());
				
		try {
			File directorio = new File(pathDownload);
			
			File[] listaDeArquivos = directorio.listFiles();
			
			// Ordenar a lista de arquivos do diretorio, garantiza que serão processados em ordem cronologico
			Arrays.sort(listaDeArquivos, NameFileComparator.NAME_COMPARATOR);
			
			for(File arquivo : listaDeArquivos){
				   
				if (arquivo.getName().startsWith(codigoEstacaoString)){
					
					fstream = new FileInputStream(arquivo);
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					
					String linha;
					String codigoSensor=null;
					
					while ((linha = br.readLine()) != null)   {
						
						if (linha.startsWith("<STATION>")){
							
							int inicCodigoSensor = linha.indexOf("<SENSOR>");
							int fimCodigoSensor = linha.indexOf("</SENSOR>");
							System.out.println("Valor do inicCodigo" + inicCodigoSensor);
							System.out.println("Valor do fimCodigoSensor" + fimCodigoSensor);
							codigoSensor = linha.substring(inicCodigoSensor+"<SENSOR>".length(), fimCodigoSensor);
						
						}else{
							
							String[] data = linha.split(";");
							if (data!=null && data.length >=2){
								if(codigoSensor.compareTo(EnumCodigoSensorEstacao.CHUVA_60_MIN.getCode())==0 || codigoSensor.compareTo(EnumCodigoSensorEstacao.CHUVA_RADAR.getCode())==0){
									dadoChuva = new DadosEstacaoTelemetricaChuvaFTP();
									dadoChuva.setData(dateFormat.parse((String)data[0]+" "+(String)data[1]));
									dadoChuva.setEstacaoId(estacao.getId());
									if (((String)data[2]).startsWith("[")){ //TODO confirmar os diferentes codigos existentes [05], [10], [03], etc
										dadoChuva.setValor((float) -901.0);
									}else{
										dadoChuva.setValor(Float.parseFloat((String)data[2]));
									}
									DadosEstacaoTelemetricaChuvaFTP dadoExistente = entityManager.find(DadosEstacaoTelemetricaChuvaFTP.class, dadoChuva);
									if (dadoExistente!=null){
										dadoExistente.setValor(dadoChuva.getValor());
										entityManager.merge(dadoExistente);
									}else{
										entityManager.persist(dadoChuva);
									}
									
								}else if (codigoSensor.compareTo(EnumCodigoSensorEstacao.NIVEL_60_MIN.getCode())==0 ||
										  codigoSensor.compareTo(EnumCodigoSensorEstacao.NIVEL_RADAR.getCode())==0 ||
										  codigoSensor.compareTo(EnumCodigoSensorEstacao.NIVEL_TRANSDUTOR.getCode())==0){
									
									dadoNivel = new DadosEstacaoTelemetricaNivelFTP();
									dadoNivel.setData(dateFormat.parse((String)data[0]+" "+(String)data[1]));
									dadoNivel.setEstacaoId(estacao.getId());
									if (((String)data[2]).startsWith("[")){ //TODO confirmar os diferentes codigos existentes [05], [10], [03], etc
										dadoNivel.setValor((float) -901.0);
									}else{
										// Ajuste da regua da ANA
										float valorNivel = Float.parseFloat((String)data[2]);
										valorNivel = (float) ((valorNivel/100) + estacao.getAjusteRegua());
										dadoNivel.setValor(valorNivel);
									}
									DadosEstacaoTelemetricaNivelFTP dadoExistente = entityManager.find(DadosEstacaoTelemetricaNivelFTP.class, dadoNivel);
									if (dadoExistente!=null){
										dadoExistente.setValor(dadoNivel.getValor());
										entityManager.merge(dadoExistente);
									}else{
										entityManager.persist(dadoNivel);
									}
								}
							}
						}
					}
					
					fstream.close();
					in.close();
					br.close();
					
					// Registra que o arquivo ja foi processado
					ftpEstacaoLogDao.registrarComoProcessado(arquivo.getName(), codigoEstacaoString);
					
					boolean wasDeleted = arquivo.delete();
					
					if (!wasDeleted){
					     logger.error("Imposivel apagar o arquivo " + arquivo.getName());
					}
				}
			}
		
		} catch (IOException e) {
			logger.error("Error de entrada/saida processando dados de FTP da estação " + estacao.getCodigo());
			logger.error("Detalhe do error : " + e.getMessage());
		} catch (ParseException e) {
			logger.error("Error na leitura do registro do arquivo processando dados de FTP da estação " + estacao.getCodigo());
			logger.error("Detalhe do error : " + e.getMessage());
		} 
		
		logger.info("Fim da atualização de dados para a estação codigo " + estacao.getCodigo());
		
	}
	
	public Double mediaHistoricaNivel(Estacao estacao){
		return mediaHistoricaNivel(estacao.getId());
	}
	
	public Double mediaHistoricaNivel(long estacaoId){
	
		Double media=0.0;
		
		try{
			media = (Double) entityManager.createQuery("select avg(d.nivel) from DadosEstacaoTelemetrica15Min d where d.estacao.id =:estacaoId and d.nivel >=0 ")
					                    .setParameter("estacaoId", estacaoId)
					                    .getSingleResult();
			
		}catch (NoResultException nre){
			return null;
		}
		
		if (media == null){
			media = 0.0;
		}
		
		return media;
		
	}
	
	public Double mediaNivel(Estacao estacao, Date dataInicial, Date dataFinal){
		return mediaNivel(estacao.getId(), dataInicial, dataFinal);
	}
	
	public Double mediaNivel(long estacaoId, Date dataInicial, Date dataFinal){
	
		Double media=0.0;
		
		try{
			media = (Double) entityManager.createQuery("select avg(d.nivel) from DadosEstacaoTelemetrica15Min d where d.estacao.id =:estacaoId and d.id.data >=:dataInicial and d.id.data <=:dataFinal and d.nivel >= 0")
					                    .setParameter("estacaoId", estacaoId)
					                    .setParameter("dataInicial", dataInicial)
					                    .setParameter("dataFinal", dataFinal)
					                    .getSingleResult();
			
		}catch (NoResultException nre){
			return null;
		}
		
		if (media == null){
			media = 0.0;
		}
		
		return media;
		
	}
	
	/**
	 * Retorna a suma da chuva registrada na estação durante o período informado
	 * */
	public Double sumaChuva(Estacao estacao, Date dataInicial, Date dataFinal){
		return sumaChuva(estacao.getId(), dataInicial, dataFinal);
	}
	
	/**
	 * Retorna a suma da chuva registrada na estação durante o período informado
	 * */
	public Double sumaChuva(long estacaoId, Date dataInicial, Date dataFinal){
	
		Double suma=0.0;
		
		try{
			suma = (Double) entityManager.createQuery("select sum(d.chuva) from DadosEstacaoTelemetrica15Min d " +
													  "where d.estacao.id =:estacaoId and d.id.data >=:dataInicial and d.id.data <=:dataFinal and d.chuva >= 0")
					                    .setParameter("estacaoId", estacaoId)
					                    .setParameter("dataInicial", dataInicial)
					                    .setParameter("dataFinal", dataFinal)
					                    .getSingleResult();
			
		}catch (NoResultException nre){
			return null;
		}
		
		if (suma == null){
			suma = 0.0;
		}
		
		return suma;
		
	}
	
}
