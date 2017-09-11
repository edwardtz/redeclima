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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dto.SecaoDto;
import com.ufpe.redeclima.model.Bacia;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * @author edwardtz
 *
 */
@Component
public class SecoesKMLDao implements InitializingBean {

	/* Diretorio dos modelos de simulação */
	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	/* Armazena as seções dos rios clasificados por cada uma das bacias */
	private HashMap<String, List<Polyline>> secoes = new HashMap<String, List<Polyline>>();
	
	@Autowired
	private BaciaDao baciaDao;
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		List<Bacia> bacias = baciaDao.list();
		
		if (bacias!=null && !bacias.isEmpty()){
			for(Bacia b: bacias){
				
				String src = Paths.get(pathModelos, b.getNome().toUpperCase(), b.getNome() + "_Secoes.kml").toString();
			    
				File fileKml = new File(src);
				
				if (fileKml.exists()){
					
					InputStream is = new FileInputStream(src);
					
					if (is != null){
						Kml kml = Kml.unmarshal(is);
					    Document document = (Document) kml.getFeature();
					    List<Polyline> secs;
					    try{
					    	secs = leerFormatoRAS(document); // Se não e de um jeito e do outro
					    } catch(ClassCastException e){
					    	secs = leerFormatoARGis(document);
					    }
					    
					    secoes.put(b.getNome(), secs);
					}
					
				}
			}
		}
	}
	
	public void carregarSecoes(Bacia bacia) throws Exception{
		
		String src = Paths.get(pathModelos, bacia.getNome().toUpperCase(), bacia.getNome() + "_Secoes.kml").toString();
			    
		File fileKml = new File(src);
				
		if (fileKml.exists() && secoes.get(bacia.getNome()) == null){
					
			InputStream is = new FileInputStream(src);
					
			if (is != null){
				Kml kml = Kml.unmarshal(is);
				Document document = (Document) kml.getFeature();
				List<Polyline> secs;
				try{
					secs = leerFormatoRAS(document); // Se não e de um jeito e do outro
				} catch(ClassCastException e){
					secs = leerFormatoARGis(document);
				}
				secoes.put(bacia.getNome(), secs);
			}
		}
	}
	
	private List<Polyline> leerFormatoARGis(Document document){
		
		List<Polyline> polylineSecoes = new ArrayList<Polyline>();
		List<Feature> featureList = document.getFeature();
	    Folder folder = (Folder) featureList.get(0);
	    
	    for(Feature pmk : folder.getFeature()) {
	    	
	    	Polyline polyline = new Polyline();
			
            if(pmk instanceof Placemark) {
                Placemark placemark = (Placemark) pmk;
                Geometry geometry = placemark.getGeometry();
                MultiGeometry polygon = (MultiGeometry) geometry;
                LineString linha = (LineString) polygon.getGeometry().get(0);
                // Shared coordinates  
                for (Coordinate coordinate: linha.getCoordinates()){
                	LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                	polyline.getPaths().add(coord);
                }
                
                org.jsoup.nodes.Document doc = Jsoup.parse(placemark.getDescription());
                Element table = doc.select("table").first();
                Iterator<Element> iterator = table.select("td").iterator();
                
                SecaoDto secao = new SecaoDto();
                
                while(iterator.hasNext()){
                	String texto = iterator.next().text();
                	
                	if (texto.startsWith("Station")){
                		String numeroSecao = iterator.next().text();
                		secao.setNomeSecao(numeroSecao.split("\\.")[0]);
                	}
                	
                	if (texto.startsWith("River")){
                		String nomeRio = iterator.next().text();
                		nomeRio = Normalizer.normalize(nomeRio, Form.NFD);
                		nomeRio = nomeRio.replaceAll("[^\\p{ASCII}]", "");
                		nomeRio = nomeRio.toUpperCase();
                		secao.setNomeRio(nomeRio);
                	}
                	
                	if (texto.startsWith("Reach")){
                		String nomeTrecho = iterator.next().text();
                		nomeTrecho = Normalizer.normalize(nomeTrecho, Form.NFD);
                		nomeTrecho = nomeTrecho.replaceAll("[^\\p{ASCII}]", "");
                		nomeTrecho = nomeTrecho.toUpperCase();
                		secao.setNomeTrecho(nomeTrecho);
                	}
                	
                }
                
                polyline.setData(secao);
                polylineSecoes.add(polyline);
            }
        }
	    
	    return polylineSecoes;
	}
	
	private double calcularCentroLatitudeSecao(List<Coordinate> coordenadas){
		double suma=0;
		for(Coordinate c: coordenadas){
			suma = suma + c.getLatitude();
		}
		return suma / coordenadas.size();
	}
	
	private double calcularCentroLongitude(List<Coordinate> coordenadas){
		double suma=0;
		for(Coordinate c: coordenadas){
			suma = suma + c.getLongitude();
		}
		return suma / coordenadas.size();
	}
	
	private List<Polyline> leerFormatoRAS(Document document){
		
		List<Polyline> polylineSecoes = new ArrayList<Polyline>();
		List<Feature> featureList = document.getFeature();
	    Folder folder = (Folder) featureList.get(0);
	    
	    for(Feature pmk : folder.getFeature()) {
	    	
	    	Polyline polyline = new Polyline();
			
            if(pmk instanceof Placemark) {
                Placemark placemark = (Placemark) pmk;
                LineString linha = (LineString) placemark.getGeometry();
                //Shared coordinates  
                for (Coordinate coordinate: linha.getCoordinates()){
                	LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                	polyline.getPaths().add(coord);
                }
                SecaoDto secao = new SecaoDto();
                secao.setNomeRio(placemark.getExtendedData().getSchemaData().get(0).getSimpleData().get(3).getValue());
                secao.setNomeTrecho(placemark.getExtendedData().getSchemaData().get(0).getSimpleData().get(4).getValue());
                secao.setNomeSecao(placemark.getExtendedData().getSchemaData().get(0).getSimpleData().get(5).getValue());
                secao.setLatitudeCentro(calcularCentroLatitudeSecao(linha.getCoordinates()));
                secao.setLongitudeCentro(calcularCentroLongitude(linha.getCoordinates()));
                polyline.setData(secao);
                polylineSecoes.add(polyline);
            }
        }
	    
	    return polylineSecoes;
	}
	
	
	/**
	 * Retorna as seções de uma bacia
	 * */
	public List<Polyline> getSecoes(Bacia bacia){
		return getSecoes(bacia.getNome());
	}
	
	/**
	 * Retorna as seções de uma bacia
	 * */
	public List<Polyline> getSecoes(String nomeBacia){
		return secoes.get(nomeBacia);
	}
	
}
