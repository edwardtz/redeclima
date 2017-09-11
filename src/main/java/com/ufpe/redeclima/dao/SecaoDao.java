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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.Polyline;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Secao;

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
public class SecaoDao {
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(Secao secao) {
		entityManager.persist(secao);
	}
	
	@Transactional
	public void saveOrUpdate(Secao secao) {
		entityManager.merge(secao);
	}
	
	public Secao findById(Long id){
		return entityManager.find(Secao.class, id);
	}
	
	public Secao findByAttributes(int distancia, Long trechoId){
		Secao secao;
		try{
			secao = (Secao) entityManager.createQuery("select s from Secao s where s.distancia=:distancia and s.trecho.id =:trechoId")
					.setParameter("distancia", distancia)
					.setParameter("trechoId", trechoId)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return secao;
	}
	
	public Secao findByDistancia(int distancia){
		Secao secao;
		try{
			secao = (Secao) entityManager.createQuery("select s from Secao s where s.distancia=:distancia ")
					.setParameter("distancia", (Integer)distancia)
					.getSingleResult();
		}catch (NoResultException nre){
			return null;
		}
		return secao;
	}
	
	@SuppressWarnings("unchecked")
	public List<Secao> list() {
		return entityManager.createQuery("select s from Secao s").getResultList();
	}

}
