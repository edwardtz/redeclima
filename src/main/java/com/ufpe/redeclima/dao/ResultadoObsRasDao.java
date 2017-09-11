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
import com.ufpe.redeclima.model.ResultadosObsRAS;
import com.ufpe.redeclima.model.Rio;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;

@Component
public class ResultadoObsRasDao {
	
	/* Diretorio dos workspaces dos usuarios */
	@Value("${parameter.path_workspaces}")
	private String pathWorkspaces;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(ResultadosObsRAS resultadoRas) {
		entityManager.persist(resultadoRas);
	}
	
	@Transactional
	public void saveOrUpdate(ResultadosObsRAS resultadoRas) {
		entityManager.merge(resultadoRas);
	}
	
	public ResultadosObsRAS findById(long simulacaoId, long secaoId, Date data){
		
		ResultadosObsRAS resultadoRas;
		try{
			resultadoRas = (ResultadosObsRAS) entityManager.createQuery("from ResultadosObsRAS r where r.simulacao.id=:simulacaoId and r.secao.id=:secaoId and r.data=:data ")
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
	 * Este método retorna a lista de resultados para uma simulação
	 * @param simulacao simulação ao que pertence os resultados
	 * */
	public List<ResultadosObsRAS> getResultados(SimulacaoObs simulacao){
		return this.getResultados(simulacao.getId(), simulacao.getDataInicio(), simulacao.getDataFim());
	}
	
	/**
	 * Este método retorna a lista de resultados para uma simulacao
	 * @param simulacaoId identificador da simulação
	 * @param dataInicio data incio do período
	 * @param dataFim data fim do período
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadosObsRAS> getResultados(Long simulacaoId, Date dataInicio, Date dataFim){
		
			return entityManager.createQuery("from ResultadosObsRAS r where r.simulacao.id=:simulacaoId and r.data>=:dataInicio and r.data<=:dataFim ")
					                    .setParameter("simulacaoId", simulacaoId)
					                    .setParameter("dataInicio", dataInicio)
					                    .setParameter("dataFim", dataFim)
					                    .getResultList();
	}
	
	
	public List<ResultadosObsRAS> getResultados(SimulacaoObs simulacao, Secao secao){
		return getResultados(simulacao.getId(), secao.getId());
	}
	
	/**
	 * Este método retorna a lista de resultados para uma simulacao e uma seção
	 * @param simulacaoId identificador da simulação
	 * @param secaoId identificador da seção
	 * */
	@SuppressWarnings("unchecked")
	public List<ResultadosObsRAS> getResultados(Long simulacaoId, Long secaoId){
		
			return entityManager.createQuery("from ResultadosObsRAS r where r.simulacao.id=:simulacaoId and r.secao.id=:secaoId ")
					                    .setParameter("simulacaoId", simulacaoId)
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
	 * @param simulacao simulação para ao qual se armazenarão os dados de resultados
	 * */
	@Transactional
	public void salvarArquivosTemporais(SimulacaoObs simulacao){
		
		String pathArquivosTemporal = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS").toString();
		
		String pathArquivoCSV = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS", simulacao.getBacia().getNome() + "BulkFileCSV.csv").toString();
		
		File diretorio = new File(pathArquivosTemporal);
		
		File[] arquivos = diretorio.listFiles();
		
		File csvFile;
		
		BufferedReader reader;
		
		BufferedWriter writer;
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("ddMMMyyyy HHmm",Locale.US);

		if(arquivos!=null && arquivos.length > 0){
			
			try {
				
				csvFile = new File(pathArquivoCSV);
				
				writer = new BufferedWriter(new FileWriter(csvFile));
				
				List<Rio> rios = rioDao.list();
				
				for (File arquivo: arquivos){
					
					reader = new BufferedReader(new FileReader(arquivo));
					
					String currentLine;
					
					if ((currentLine = reader.readLine()).compareTo("END FILE") != 0){ //Se o arquivo esta vazio so contem a linha END FILE
						
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
	
	/**
	 * Salvar resultados na BD desde o arquivo CSV
	 * */
	@Transactional
	public void salvarResultados(SimulacaoObs simulacao){
		
		String pathArquivoCSV = Paths.get(pathWorkspaces, simulacao.getUsuario().getLogin().toUpperCase(), simulacao.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS", simulacao.getBacia().getNome() + "BulkFileCSV.csv").toString();
		
		entityManager.createNativeQuery("copy t_resultados_obs_ras (simulacao_id, secao_id, data, elevacao, fluxo) from '" + pathArquivoCSV + "' WITH DELIMITER ','").executeUpdate();
	}
	
	@Transactional
	public void removerResultados(SimulacaoObs simulacao){
		entityManager.createQuery("delete from ResultadosObsRAS r where r.simulacao.id=:simulacaoId").setParameter("simulacaoId", simulacao.getId()).executeUpdate();
	}
	
	public double mediaNivel(SimulacaoObs simulacao, Secao secao){
		return mediaNivel(simulacao.getId(), secao.getId());
	}
	
	public double mediaNivel(long simulacaoId, long secaoId){
		double media = 0.0;
		media = (Double) entityManager.createQuery("select avg(r.elevacao) from ResultadosObsRAS r where r.simulacao.id=:simulacaoId and  r.secao.id=:secaoId and r.elevacao >=0 ")
									  .setParameter("simulacaoId", simulacaoId)
									  .setParameter("secaoId", secaoId)
									  .getSingleResult();
		return media;
	}

}
