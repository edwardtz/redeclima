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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

/**
 * @author edwardtz
 * Esta classe carrega os dados geograficos dos limites das bacias desde um arquivo KML, que depois será asociado ao modelo do mapa
 * 
 */

@Component
public class LimitesBaciasKMLDao implements InitializingBean {

	/* Objeto que armazena os dados das polilineas dos limites das bacias */
	private List<Polyline> polylineBacias;
	 
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {

		polylineBacias = new ArrayList<Polyline>();
		  
		String src = "db/Bacias_PE_6.kml";
	    InputStream is = getClass().getClassLoader().getResourceAsStream(src);
	    Kml kml = Kml.unmarshal(is);
	    Feature feature = kml.getFeature();
	    Document document = (Document) feature;
	    List<Feature> featureList = document.getFeature();
	    for(Feature documentFeature : featureList) {
	    	
	    	Polyline polyline = new Polyline();
			
            if(documentFeature instanceof Placemark) {
                Placemark placemark = (Placemark) documentFeature;
                Geometry geometry = placemark.getGeometry();
                LineString polygon = (LineString) geometry;
                //Shared coordinates  
                for (Coordinate coordinate: polygon.getCoordinates()){
                	LatLng coord = new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
                	polyline.getPaths().add(coord);
                }
                polyline.setData(placemark.getName());
                polylineBacias.add(polyline);
            }
        }
	}

	public List<Polyline> getPolylineBacias() {
		return polylineBacias;
	}

	public void setPolylineBacias(List<Polyline> polylineBacias) {
		this.polylineBacias = polylineBacias;
	}

	

}
