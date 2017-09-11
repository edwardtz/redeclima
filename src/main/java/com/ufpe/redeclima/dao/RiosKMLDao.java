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
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
 * Esta classe carrega os dados geograficos das calhas dos rios das bacias desde um arquivo KML, que depois será asociado ao modelo do mapa
 */
@Component
public class RiosKMLDao implements InitializingBean {

	/* Diretorio dos modelos de simulação */
	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	/* Objeto que armazena os dados das polilineas das calhas dos rios */
	private List<Polyline> polylineRios;
	
	@Autowired
	private BaciaDao baciaDao;
	

	private void carregarRios() throws FileNotFoundException{
		
		polylineRios = new ArrayList<Polyline>();
		
		List<Bacia> bacias = baciaDao.list();
		if (bacias != null & !bacias.isEmpty()){
			for (Bacia b: bacias){
				
				String src = Paths.get(pathModelos, b.getNome().toUpperCase(), b.getNome() + "_Rios.kml").toString();
			    
				File fileKml = new File(src);
			    
			    if (fileKml.exists()){
			    	
			    	InputStream is = new FileInputStream(src);   //.getClassLoader().getResourceAsStream(src);
				    
				    if (is != null){
				    	
				    	Kml kml = Kml.unmarshal(is);
					    Document document = (Document) kml.getFeature();
					    List<Feature> featureList = document.getFeature();
					    Folder folder = (Folder) featureList.get(0);
					    
					    for(Feature pmk : folder.getFeature()) {
					    	
					    	Polyline polyline = new Polyline();
							
				            if(pmk instanceof Placemark) {
				                Placemark placemark = (Placemark) pmk;
				                Geometry geometry = placemark.getGeometry();
				                MultiGeometry polygon = (MultiGeometry) geometry;
				                LineString linha = (LineString) polygon.getGeometry().get(0);
				                //Shared coordinates  
				                for (Coordinate coordinate: linha.getCoordinates()){
				                	LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
				                	polyline.getPaths().add(coord);
				                }
				                polyline.setData(placemark.getName());
				                polylineRios.add(polyline);
				            }
				        
					    }
				    }
			    }
			}
		}
	}
	
    public void carregarRios(Bacia bacia) throws FileNotFoundException{
		
		polylineRios = new ArrayList<Polyline>();
		
				
		String src = Paths.get(pathModelos, bacia.getNome().toUpperCase(), bacia.getNome() + "_Rios.kml").toString();
			    
		File fileKml = new File(src);
			    
		if (fileKml.exists()){
			    	
			InputStream is = new FileInputStream(src);  
				    
			if (is != null){
				    	
				Kml kml = Kml.unmarshal(is);
				Document document = (Document) kml.getFeature();
				List<Feature> featureList = document.getFeature();
				Folder folder = (Folder) featureList.get(0);
					    
				for(Feature pmk : folder.getFeature()) {
					    	
					Polyline polyline = new Polyline();
							
					if(pmk instanceof Placemark) {
						Placemark placemark = (Placemark) pmk;
						Geometry geometry = placemark.getGeometry();
						MultiGeometry polygon = (MultiGeometry) geometry;
						LineString linha = (LineString) polygon.getGeometry().get(0);
						//Shared coordinates  
						for (Coordinate coordinate: linha.getCoordinates()){
							LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
							polyline.getPaths().add(coord);
						}
						polyline.setData(placemark.getName());
						polylineRios.add(polyline);
					}
				        
				}
			}
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		carregarRios();
	}

	public List<Polyline> getPolylineRios() {
		return polylineRios;
	}

	public void setPolylineRios(List<Polyline> polylineRios) {
		this.polylineRios = polylineRios;
	}


	
	
}
