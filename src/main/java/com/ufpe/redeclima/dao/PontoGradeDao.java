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
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ucar.nc2.NCdumpW;

import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;

@Component
public class PontoGradeDao {

	private static final Logger logger = LoggerFactory.getLogger(PontoGradeDao.class);
	
	@Value("${jaxws.pathLocalDownloadCptecFtp}")
	private String pathDownload;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private GribLogDao gribLogDao;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	private NumberFormat numberFormat;
	
	public PontoGradeDao(){
	
		numberFormat = NumberFormat.getInstance(Locale.US);
		numberFormat.setMinimumFractionDigits(4);
		numberFormat.setMaximumFractionDigits(4);
		
	}

	private PontoGrade formatarCoordenadas(PontoGrade pontoGrade){
		pontoGrade.setLongitude(Double.parseDouble(numberFormat.format(pontoGrade.getLongitude())));
		pontoGrade.setLatitude(Double.parseDouble(numberFormat.format(pontoGrade.getLatitude())));
		return pontoGrade;
	}
	
	private double formatarCoordenada(Double coordenada){
		return Double.parseDouble(numberFormat.format(coordenada));
	}
	
	@Transactional
	public void save(PontoGrade pontoGrade) {
		entityManager.persist(formatarCoordenadas(pontoGrade));
	}
	
	public PontoGrade findById(Long id){
		return entityManager.find(PontoGrade.class, id);
	}
	
	@Transactional
	public void atualizarPontoGrade(String nomeArquivo, Grade grade){

		if (grade==null){
			logger.error("Não existe grade definida para o arquivo " + nomeArquivo);
			return;
		}
		this.atualizarPontoGrade(pathDownload, nomeArquivo, grade);
	}
	
	@Transactional
	public void atualizarPontoGrade(String pastaDownloadLocal, String nomeArquivo, Grade grade){

		String pathfile = Paths.get(pastaDownloadLocal, nomeArquivo).toString();
		
		OutputStream outputstream;
		OutputStreamWriter stream;
		BufferedWriter buffer;
		
		FileInputStream fileInputStream;
		InputStreamReader inputStream;
		BufferedReader bufferReader;
		
		
		try {
			
			
			outputstream = new FileOutputStream(Paths.get(pastaDownloadLocal, "dumpCoordenadas_" + nomeArquivo.replace(".grb", ".txt")).toString());
			stream = new OutputStreamWriter(outputstream);
			buffer = new BufferedWriter(stream);
			
			
			NCdumpW.print(pathfile, buffer, false, true, false, true, "Total_precipitation_surface", null);
			buffer.close();
			stream.close();
			outputstream.close();
			
			fileInputStream = new FileInputStream(Paths.get(pastaDownloadLocal, "dumpCoordenadas_" + nomeArquivo.replace(".grb", ".txt")).toString());
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
			
			fileInputStream.close();
			
			PontoGrade pontoGrid = null;
			double latitude = 0;
			double longitude = 0;
			
			for (int i=0; i < lat; i++){
				
				if(i==0){
					latitude = Double.parseDouble(coordenadasLatitude[i].replace('{', ' ').trim());
				}else if (i == lat-1){
					latitude = Double.parseDouble(coordenadasLatitude[i].replace('}', ' ').trim());
				}else{
					latitude = Double.parseDouble(coordenadasLatitude[i].trim());
				}
				
				
				for (int j=0; j < lon; j++){
					
					if(j==0){
						longitude = Double.parseDouble(coordenadasLongitude[j].replace('{', ' ').trim());
					}else if (j == lon-1){
						longitude = Double.parseDouble(coordenadasLongitude[j].replace('}', ' ').trim());
					}else{
						longitude = Double.parseDouble(coordenadasLongitude[j].trim());
					}
					
					if (grade.estaConteudoEmAreaRecorte(longitude, latitude)){
						pontoGrid = new PontoGrade();
						pontoGrid.setLatitude(latitude);
						pontoGrid.setLongitude(longitude);
						pontoGrid.setGrade(grade);
						
						if (findByCoordGrade(pontoGrid)==null){
							save(pontoGrid);
						}
					}
				}
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			//TODO insertar log
		} catch (IOException e) {
			gribLogDao.registrarEntrada(nomeArquivo, grade);
			logger.error("Error processando arquivo " + nomeArquivo);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<PontoGrade> list(){
		return entityManager.createQuery("from PontoGrade p").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public PontoGrade findByCoordGrade(PontoGrade pontoGrade){
		return this.findByCoordGrade(pontoGrade.getLongitude(), pontoGrade.getLatitude(), pontoGrade.getGrade().getId());
	}
	
	/**
	 * Retorna um ponto a partir dos dados de grade e coordenada
	 * @param longitude
	 * @param latitude
	 * @param gradeId
	 * */
	@SuppressWarnings("unchecked")
	public PontoGrade findByCoordGrade(double longitude, double latitude, long gradeId){
		List<Object> lista = entityManager.createQuery("from PontoGrade p where p.longitude =:longitude and p.latitude =:latitude and p.grade.id =:gradeId ")
				.setParameter("longitude", formatarCoordenada(longitude))
				.setParameter("latitude", formatarCoordenada(latitude))
				.setParameter("gradeId", gradeId)
				.getResultList();
		
		if (lista!=null && !lista.isEmpty()){
			return (PontoGrade) lista.get(0);
		}
		return null;
	}
	
	
	public List<PontoGrade> getPontosGradeBacia(SimDto simDto){
		return getPontosGradeBacia(simDto.getBacia(), simDto.getGrade());
	}
	
	/** 
	 * Retorna os pontos de grade coteudos na área de uma bacia  
	 * @param bacia bacia que contem a area
	 * @param grade grade de referencia 
	 * */
	@SuppressWarnings("unchecked")
	public List<PontoGrade> getPontosGradeBacia(Bacia bacia, Grade grade){
		return entityManager.createQuery("from PontoGrade p where p.latitude <=:y1 and  p.latitude >=:y2 and p.longitude >=:x1 and p.longitude <=:x2 and p.grade.id =:grade_id ")
				.setParameter("y1", bacia.getLatitudeY1())
				.setParameter("y2", bacia.getLatitudeY2())
				.setParameter("x1", bacia.getLongitudeX1())
				.setParameter("x2", bacia.getLongitudeX2())
				.setParameter("grade_id", grade.getId())
				.getResultList();

	}
	
	/** 
	 * Retorna todos os pontos da grade passada como parâmetro  
	 * @param grade grade de referencia 
	 * */
	@SuppressWarnings("unchecked")
	public List<PontoGrade> getPontosGrade(Grade grade){
		return entityManager.createQuery("from PontoGrade p where p.grade.id =:grade_id ")
				.setParameter("grade_id", grade.getId())
				.getResultList();

	}
	
}
