package com.ufpe.redeclima.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.ResultadoSerieRAS;
import com.ufpe.redeclima.model.Rio;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.SeriePrevisao;
import com.ufpe.redeclima.model.SimulacaoSerie;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;

@Component
public class ResultadoSerieRasDao {
	
	/* Diretorio dos workspaces dos usuarios */
	@Value("${parameter.path_workspaces}")
	private String pathWorkspaces;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	@Autowired
	private SimulacaoSerieDao simulacaoSerieDao;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(ResultadoSerieRAS resultadoRas) {
		entityManager.persist(resultadoRas);
	}
	
	@Transactional
	public void saveOrUpdate(ResultadoSerieRAS resultadoRas) {
		entityManager.merge(resultadoRas);
	}
	
	public ResultadoSerieRAS findById(long simulacaoId, long secaoId, Date data){
		
		ResultadoSerieRAS resultadoRas;
		try{
			resultadoRas = (ResultadoSerieRAS) entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId and r.secao.id=:secaoId and r.data=:data ")
					                    .setParameter("simulacaoId", simulacaoId)
					                    .setParameter("secaoId", secaoId)
					                    .setParameter("data", data)
					                    .getSingleResult();
			
		}catch (NoResultException nre){
			return null;
		}
		
		return resultadoRas;
	}
	
	/**
	 * Retorna a ultima data da serie
	 * @param simulacaoId identificador da simulação
	 * */
	public Date getUltimaData(SimulacaoSerie simulacao){
		
		Date ultimaData;
		try{	
			
			ultimaData = (Date) entityManager.createQuery("select distinct max(r.data) from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId ")
	                .setParameter("simulacaoId", simulacao.getId())
	                .getSingleResult();
			
		}catch (NoResultException nre){
			return null;
		}
		
		return ultimaData;
	}
	
	/**
	 * Retorna os valores do ultimo dia da serie toda de todas as seções
	 * @param simulacaoId identificador da simulação
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadoSerieRAS> listUltimoDia(SimulacaoSerie simulacao){
		Date ultimaData = getUltimaData(simulacao);
		
		if (ultimaData == null){
			return null;
		}
		
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(ultimaData);
		calendario.set(Calendar.HOUR_OF_DAY, 0);
		calendario.set(Calendar.MINUTE, 0);
		calendario.set(Calendar.SECOND, 0);
		
		Date dataInicial = calendario.getTime();
		
		calendario.set(Calendar.HOUR_OF_DAY, 23);
		calendario.set(Calendar.MINUTE, 59);
		calendario.set(Calendar.SECOND, 59);
		
		Date dataFinal = calendario.getTime();
		
		return entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId and r.data>=:dataInicial and r.data<=:dataFinal order by r.data")
					                    .setParameter("simulacaoId", simulacao.getId())
					                    .setParameter("dataInicial", dataInicial)
					                    .setParameter("dataFinal", dataFinal)
					                    .getResultList();
	}
	
	/**
	 * Retorna os valores do ultimo dia da serie toda de todas as seções
	 * @param simulacaoId identificador da simulação
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadoSerieRAS> listUltimoDia(SimulacaoSerie simulacao, Secao secao){
		Date ultimaData = getUltimaData(simulacao);
		
		if (ultimaData == null){
			return null;
		}
		
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(ultimaData);
		calendario.set(Calendar.HOUR_OF_DAY, 0);
		calendario.set(Calendar.MINUTE, 0);
		calendario.set(Calendar.SECOND, 0);
		
		Date dataInicial = calendario.getTime();
		
		calendario.set(Calendar.HOUR_OF_DAY, 23);
		calendario.set(Calendar.MINUTE, 59);
		calendario.set(Calendar.SECOND, 59);
		
		Date dataFinal = calendario.getTime();
		
		return entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId and r.secao.id =:secaoId and r.data>=:dataInicial and r.data<=:dataFinal order by r.data")
					                    .setParameter("simulacaoId", simulacao.getId())
					                    .setParameter("secaoId", secao.getId())
					                    .setParameter("dataInicial", dataInicial)
					                    .setParameter("dataFinal", dataFinal)
					                    .getResultList();
	}
	
	public double mediaHistoricaSerieUltimoDia(Usuario usuario, Bacia bacia, Grade grade, Secao secao){
		double media = 0.0;
		int n = 0;
		List<SimulacaoSerie> series = simulacaoSerieDao.list(usuario , bacia, grade);
		if (series != null && !series.isEmpty()){
			for (SimulacaoSerie ss: series){
				List<ResultadoSerieRAS> results = listUltimoDia(ss, secao);
				if (results!=null && !results.isEmpty()){
					for (ResultadoSerieRAS r: results){
						media = media + r.getElevacao();
					}
					n = n + results.size();
				}
			}
			if (n!=0){
				media = media / n;
			}
			
		}
		return media;
	}
	
	/**
	 * Este método retorna a lista de resultados para uma simulacao
	 * @param simulacaoId identificador da simulação
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadoSerieRAS> listBy(SimulacaoSerie simulacao){
		
			return entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId order by r.data")
					                    .setParameter("simulacaoId", simulacao.getId())
					                    .getResultList();
	}
	
	
	/**
	 * Este método retorna a lista de resultados para uma simulacao e uma seção
	 * @param simulacaoId identificador da simulação
	 * @param secaoId identificador da seção
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadoSerieRAS> getResultados(Long simulacaoId, Long secaoId){
		
			return entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId and r.secao.id=:secaoId ")
					                    .setParameter("simulacaoId", simulacaoId)
					                    .setParameter("secaoId", secaoId)
					                    .getResultList();
	}
	
	
	public List<ResultadoSerieRAS> list(SeriePrevisao serie, Secao secao){
		return list(serie.getId(), secao.getId());
	}
	
	/**
	 * Este método retorna a lista de resultados para uma serie de previssão e para uma seção
	 * @param serieId identificador da serie de previssão
	 * @param secaoId identificador da seção
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadoSerieRAS> list(long serieId, long secaoId){
		
			return entityManager.createQuery("from ResultadoSerieRAS r where r.simulacao.serie.id=:serieId and r.secao.id=:secaoId order by r.data ")
					                    .setParameter("serieId", serieId)
					                    .setParameter("secaoId", secaoId)
					                    .getResultList();
	}
	
	private Rio rioConteudo(String descripcao, List<Rio> rios){
		
		if (!rios.isEmpty()){
			for(Rio r:rios){
				if (descripcao.contains(r.getNome())){
					return r;
				}
			}
		}
		return null;
	}
	
	private Trecho trechoConteudo(String descripcao, Rio rio){
	
		if (rio.getTrechos()!=null && !rio.getTrechos().isEmpty()){
			for(Trecho t: rio.getTrechos()){
				if (descripcao.contains(t.getNome())){
					return t;
				}
			}
		}
		return null;
	}
	
	/**
	 * Este método atualiza os dados de simulacao a partir dos arquivos temporais de resultado
	 * @param simulacao simulação para o qual se armazenarão os dados de resultados
	 * */
	@Transactional
	public void salvarArquivosTemporais(SimulacaoSerie simulacao){
		
		String pathArquivosTemporal = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS").toString();
		
		String pathArquivoCSV = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS", simulacao.getBacia().getNome() + "BulkFileCSV.csv").toString();
		
		File diretorio = new File(pathArquivosTemporal);
		
		File[] arquivos = diretorio.listFiles();
		
		File csvFile;
		
		BufferedReader reader;
		
		BufferedWriter writer;
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("ddMMMyyyy HHmm", Locale.US);

		if(arquivos!=null && arquivos.length > 0){
			
			try {
				
				csvFile = new File(pathArquivoCSV);
				
				writer = new BufferedWriter(new FileWriter(csvFile));
				
				List<Rio> rios = rioDao.list();
				
				for (File arquivo: arquivos){
					
					reader = new BufferedReader(new FileReader(arquivo));
					
					String currentLine;
					
					if ((currentLine = reader.readLine()) != "END"){ //Se o arquivo esta vazio so contem a linha END
						
						String[] partes = currentLine.split("/");
						String rio_trecho = partes[1];
						Rio rio = rioConteudo(rio_trecho, rios);
						Trecho trecho = trechoConteudo(rio_trecho, rio);
						Date data = dataFormat.parse(partes[5]);
						
						currentLine = reader.readLine();
						
						currentLine = reader.readLine();
						
						String ordinates = (currentLine.split(",")[1]).split("Ordinates")[0].trim();
						
						int[] secoes = new int[Integer.parseInt(ordinates)];
						double[] valores = new double[Integer.parseInt(ordinates)];
						
						currentLine = reader.readLine();
								
						for (int i=0; i<Integer.parseInt(ordinates); i++){
							secoes[i] = new Double(reader.readLine().trim()).intValue();
						}
						
						for (int i=0; i<Integer.parseInt(ordinates); i++){
							valores[i] = Double.parseDouble(reader.readLine().trim());
						}
						
						reader.close();
						
						for(int i=0; i<Integer.parseInt(ordinates); i++){
							
							Secao secao = secaoDao.findByAttributes(secoes[i], trecho.getId());
							if (secao!=null){
								writer.write(simulacao.getId()+","+secao.getId()+","+dataFormat.format(data)+","+valores[i]+",0");
								writer.newLine();
							}
						}
					}
				}
				
				writer.close();	
				
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@Transactional
	public void salvarResultados(SimulacaoSerie simulacao){
		
		String pathArquivoCSV = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS", simulacao.getBacia().getNome() + "BulkFileCSV.csv").toString();
		
		entityManager.createNativeQuery("copy t_resultado_serie_ras (simulacao_id, secao_id, data, elevacao, fluxo) from '" + pathArquivoCSV + "' WITH DELIMITER ','")
	                 .executeUpdate();
	}
	
	@Transactional
	public void removerResultados(SimulacaoSerie simulacao){
		entityManager.createQuery("delete from ResultadoSerieRAS r where r.simulacao.id=:simulacaoId").setParameter("simulacaoId", simulacao.getId()).executeUpdate();
	}

}
