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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.GribLog;

@Component
public class GribLogDao {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHH");
	
	@PersistenceContext
	private EntityManager entityManager;

	@Transactional
	public void save(GribLog gribLog) {
		entityManager.persist(gribLog);
	}
	
	@Transactional
	public void saveOrUpdate(GribLog gribLog){
		entityManager.merge(gribLog);
	}
	
	public GribLog findById(String id){
		return entityManager.find(GribLog.class, id);
	}
	
	private Date getDataDesde(String nomeArquivo){
		try {
			String[] partes = nomeArquivo.split("\\+");
			String dataDesde = partes[0].substring(partes[0].length() - 10);
			return dateFormat.parse(dataDesde);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Date getDataPrevisao(String nomeArquivo){
		try {
			String[] partes = nomeArquivo.split("\\+");
			String parteDataPrevisao  = partes[1].replace(".grb", " ").trim();
			return dateFormat.parse(parteDataPrevisao);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Registra uma entrada no log para um arquivo grib, indicando que o sistema tem identificado um arquivo para fazer download ou para processar
	 * */
	@Transactional
	public void registrarEntrada(String nomeArquivo, Grade grade){
		GribLog registro = findById(nomeArquivo);
		if (registro==null){
			registro = new GribLog();
			registro.setId(nomeArquivo);
			registro.setDataDesde(getDataDesde(nomeArquivo));
			registro.setDataPrevisao(getDataPrevisao(nomeArquivo));
			registro.setGrade(grade);
			entityManager.persist(registro);
		}
	}
	
	/**
	 * Registra que o download do arquivo foi efetuado com sucesso
	 * */
	@Transactional
	public void registrarFimDownload(String nomeArquivo, Grade grade){
		GribLog registro = findById(nomeArquivo);
		if (registro!=null){
			registro.setDataDownload(new Date());
			entityManager.merge(registro);
		}else{
			registro = new GribLog();
			registro.setId(nomeArquivo);
			registro.setDataDesde(getDataDesde(nomeArquivo));
			registro.setDataPrevisao(getDataPrevisao(nomeArquivo));
			registro.setDataDownload(new Date());
			registro.setGrade(grade);
			entityManager.persist(registro);
		}
	}
	
	/**
	 * Registra que o arquivo foi processado e os dados salvados na BD
	 * */
	@Transactional
	public void registrarProcessado(String nomeArquivo, Grade grade){
		GribLog registro = findById(nomeArquivo);
		if (registro!=null){
			registro.setDataProcessado(new Date());
			// Para o caso onde o download do arquivo foi interrumpido, ele esta registrado mais nao completo o dowload
			if (registro.getDataDownload()==null){
				registro.setDataDownload(new Date());
			}
			entityManager.merge(registro);
		}else{
			registro = new GribLog();
			registro.setId(nomeArquivo);
			registro.setDataDesde(getDataDesde(nomeArquivo));
			registro.setDataPrevisao(getDataPrevisao(nomeArquivo));
			registro.setDataProcessado(new Date());
			// No caso que seja um arquivo copiado na pasta manualmente
			registro.setDataDownload(new Date());
			registro.setGrade(grade);
			entityManager.persist(registro);
		}
	}
	
	/**
	 * Registra que os dados da serie temporal do arquivo foram processados e os dados salvados na BD
	 * */
	@Transactional
	public void registrarProcessadoSeries(String nomeArquivo){
		GribLog registro = findById(nomeArquivo);
		if(registro!=null && registro.getDataDownload()!=null){
			registro.setDataProcessadoSerie(new Date());
			saveOrUpdate(registro);
		}
	}
	
	public boolean isDownloaded(String nomeArquivo){
		GribLog registro = findById(nomeArquivo);
		if (registro !=null){
			return registro.isDownloaded();
		}
		return false;
	}
	
	public boolean isProcessado(String nomeArquivo){
		GribLog registro = findById(nomeArquivo);
		if (registro !=null){
			return registro.isProcessado();
		}
		return false;
	}
	
	public boolean isProcessadoSeries(String nomeArquivo){
		GribLog registro = findById(nomeArquivo);
		if (registro !=null){
			return registro.isProcessadoSerie();
		}
		return false;
	}
	
	@SuppressWarnings("unchecked")
	public List<GribLog> list() {
		return entityManager.createQuery("from GribLog l").getResultList();
	}
	
	@SuppressWarnings("unchecked")
	public List<GribLog> listNaoProcessados() {
		return entityManager.createQuery("from GribLog l where l.dataProcessado is null").getResultList();
	}
	
}
