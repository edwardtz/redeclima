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

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class RiosKMLDaoTest {

	@Autowired
	private RiosKMLDao riosKMLDao;
	
	@Test
	public void testRios() {

		List<Polyline> lineas = riosKMLDao.getPolylineRios();
		assertNotNull(lineas);
		
	}
	
	@Test
	public void testSecoesARGis(){
	
		List<Polyline> polylineSecoes = new ArrayList<Polyline>();
		  
		String src = "db/UnaSecsARGis.kml";
	    InputStream is = getClass().getClassLoader().getResourceAsStream(src);
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
                polylineSecoes.add(polyline);
            }
        }
	    
	}

	@Test
	public void testSecoesRas(){
	
		List<Polyline> polylineSecoes = new ArrayList<Polyline>();
		  
		String src = "db/UnaSecsRas.kml";
	    InputStream is = getClass().getClassLoader().getResourceAsStream(src);
	    Kml kml = Kml.unmarshal(is);
	    Document document = (Document) kml.getFeature();
	    List<Feature> featureList = document.getFeature();
	    Folder folder = (Folder) featureList.get(0);
	    
	    for(Feature pmk : folder.getFeature()) {
	    	
	    	Polyline polyline = new Polyline();
			
            if(pmk instanceof Placemark) {
                Placemark placemark = (Placemark) pmk;
                Geometry geometry = placemark.getGeometry();
                LineString linha = (LineString) placemark.getGeometry();
                //Shared coordinates  
                for (Coordinate coordinate: linha.getCoordinates()){
                	LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                	polyline.getPaths().add(coord);
                }
                polyline.setData(placemark.getName());
                polylineSecoes.add(polyline);
            }
        }
	    
	}
}
