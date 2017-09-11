package com.ufpe.redeclima.dao;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Estacao;

@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public class ANAWSDaoTest {

	@Autowired
	private ANAWSDao webServiceDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Test
	public void testGetDataEstacao() {
			
		Calendar calendar = Calendar.getInstance();
		
		Date dataFim = calendar.getTime();
		calendar.add(Calendar.DAY_OF_MONTH, -1);
		Date dataInicio = calendar.getTime();
				
		webServiceDao.getDataEstacao(39571000, dataInicio, dataFim);
	}
	
	@Test
	public void getDataEstacaoFTP() {
		int[] estacoes = {39530000, 39541000, 39540750, 39571000, 39550000, 39560000, 39580000, 39590000};
		Estacao estacao = new Estacao();
		for(int i=0; i<estacoes.length; i++){
			estacao.setCodigo(estacoes[i]);
			webServiceDao.baixarDadosFtp(estacao);
		}
	}
	
	
	@Test
	public void xmlParserMissingData(){
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try {
			File fXmlFile = new File("src/test/resources/DadosHidrometeorologicosMissing.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			FileInputStream in = new FileInputStream(fXmlFile);
			Document doc = dBuilder.parse(in);
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("DadosHidrometereologicos");
			List<Element> missingValues = new ArrayList<Element>(); 
			
			//Definir o elemento anterior
			Date dataAnterior=null;
			Calendar calendario = Calendar.getInstance();
			
			for (int temp = 0; temp < nList.getLength(); temp++) {
				Node nNode = nList.item(temp);
				Element eElement = (Element) nNode;
				if (temp>0){
					calendario.setTime(dataAnterior);
					calendario.add(Calendar.MINUTE, -15);
					Date dataProximaSecuencia =  calendario.getTime();
					Date dataNodoActual = dateFormat.parse(eElement.getElementsByTagName("DataHora").item(0).getTextContent());
					while(dataProximaSecuencia.getTime() != dataNodoActual.getTime()){
						Element novoDado = doc.createElement("DadosHidrometereologicos");
						Element novoCodigoEstacao = doc.createElement("CodEstacao");
						novoCodigoEstacao.setTextContent("00000000");
						Element novaData = doc.createElement("DataHora");
						novaData.setTextContent(dateFormat.format(dataProximaSecuencia));
						Element novaVazao = doc.createElement("Vazao");
						novaVazao.setTextContent("");
						Element novoNivel = doc.createElement("Nivel");
						novoNivel.setTextContent("");
						Element novaChuva = doc.createElement("Chuva");
						novaChuva.setTextContent("");
						novoDado.appendChild(novoCodigoEstacao);
						novoDado.appendChild(novaData);
						novoDado.appendChild(novaVazao);
						novoDado.appendChild(novoNivel);
						novoDado.appendChild(novaChuva);
						missingValues.add(novoDado);
						
						calendario.add(Calendar.MINUTE, -15);
						dataProximaSecuencia = calendario.getTime();
					}
				}
				dataAnterior = dateFormat.parse(eElement.getElementsByTagName("DataHora").item(0).getTextContent());
			}
			
			nList = doc.getElementsByTagName("DadosHidrometereologicos");
			
			assertNotNull("O arquivo não foi carregado",doc);
			assertNotNull("O arquivo esta mal formado",doc.getDocumentElement());
			
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
