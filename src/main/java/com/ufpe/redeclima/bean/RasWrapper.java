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
package com.ufpe.redeclima.bean;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.RioDao;
import com.ufpe.redeclima.dao.SecaoDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Rio;
import com.ufpe.redeclima.model.Secao;
import com.ufpe.redeclima.model.Trecho;

/**
 * @author edwardtz
 * Implementa as funções basicas para administrar o repositorio dos modelos
 */
@Component
@Scope("prototype")
public class RasWrapper {

	
	/* Path de download local ode os arquivos serão salvos */
	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	/* Retorna o nome do arquivo de projeto RAS, esto asume que so tem um .prj no diretorio do RAS */
	private String identificarNomeProjetoRAS(Bacia bacia){
		
		String pathArquivoPrj = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
		File dir = new File(pathArquivoPrj);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".prj")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/* Retorna o nome do arquivo do current plan pela extensão ej p01 ou p02, esto asume que os numeros pXX não se repetem */
	private String identificarNomePlan(Bacia bacia, String extensao){
		
		String pathArquivoPrj = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
		File dir = new File(pathArquivoPrj);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith("." + extensao)){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	private String identificarNomeCurrentPlan(Bacia bacia){
		
		String nomeArquivoProjetoRAS = identificarNomeProjetoRAS(bacia);
		
		String pathArquivoPrj = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoProjetoRAS).toString();
	
		File inputFile = new File(pathArquivoPrj);
		
		BufferedReader reader;
		
		String nomeArquivoCurrentPlan=null;
		
		String extensaoPlan = null;
		
		try {
			if (inputFile.exists()){
				
					reader = new BufferedReader(new FileReader(inputFile));
					
					String currentLine;
					
					while(((currentLine = reader.readLine()) != null)) {
						if (currentLine.startsWith("Current Plan=")){
							extensaoPlan = currentLine.split("=")[1];
							nomeArquivoCurrentPlan = identificarNomePlan(bacia, extensaoPlan);
							reader.close();
							return nomeArquivoCurrentPlan;
						}
					}
					
					
				
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return nomeArquivoCurrentPlan;
	}
	
	
	/* Retorna o nome do arquivo de geometria correspondente à bacia */
	private String identificarNomeArquivoGeometria(Bacia bacia, String extensao){
		
		String pathArquivoGeom = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
		File dir = new File(pathArquivoGeom);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith("." + extensao)){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	private String identificarNomeArquivoGeometria(Bacia bacia){
		
		String nomeArquivoCurrentPlan = identificarNomeCurrentPlan(bacia);
		
		String pathArquivoPlan = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoCurrentPlan).toString();
	
		File inputFile = new File(pathArquivoPlan);
		
		BufferedReader reader;
		
		String nomeArquivoGeom=null;
		
		String extensaoGeom = null;
		
		try {
			if (inputFile.exists()){
				
					reader = new BufferedReader(new FileReader(inputFile));
					
					String currentLine;
					
					while(((currentLine = reader.readLine()) != null)) {
						if (currentLine.startsWith("Geom File=")){
							extensaoGeom = currentLine.split("=")[1];
							nomeArquivoGeom = identificarNomeArquivoGeometria(bacia, extensaoGeom);
							reader.close();
							return nomeArquivoGeom;
						}
					}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return nomeArquivoGeom;
	}
	
	/**
	 * Inicializa os dados de rios, trechos e seções correspondentes a uma bacia configurados na geometría dom modelo RAS
	 * */
	public void InicializarRios(Bacia bacia){
		
		String nomeArquivoGeometria = identificarNomeArquivoGeometria(bacia);
		
		String pathArquivoGeometria = Paths.get(pathModelos, bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoGeometria).toString();
		
		File inputFile = new File(pathArquivoGeometria);
				
		BufferedReader reader;
				
		Rio rio=null;
				
		Trecho trecho=null;
				
		if (inputFile.exists()){
					
			try {
				reader = new BufferedReader(new FileReader(inputFile));
					
				// Elimiar todas as referencias aos registros Gage:
				String currentLine;
						
				while((currentLine = reader.readLine()) != null){
						
					if(currentLine.startsWith("River Reach=")){
						String[] partes = currentLine.split("=")[1].split(",");
						String rioNome = partes[0].trim().toUpperCase();
						String trechoNome = partes[1].trim().toUpperCase();
						rio = rioDao.findByNome(rioNome);
						if (rio == null){
							rio = new Rio();
							rio.setNome(rioNome);
							rioDao.save(rio);
						}
								
						trecho = trechoDao.findByNome(trechoNome);
						if(trecho == null){
							trecho = new Trecho();
							trecho.setNome(trechoNome);
							trecho.setRio(rio);
							trechoDao.save(trecho);
						}
					}
							
					if (currentLine.startsWith("Type RM Length L Ch R =")){
						String[] partes = currentLine.split("=")[1].split(",");
						int secaoDistancia = new Double(partes[1]).intValue();
						
						Secao secao = secaoDao.findByAttributes(secaoDistancia, trecho.getId());
								
						if (secao==null){
							secao = new Secao();
							secao.setTrecho(trecho);
							secao.setDistancia(secaoDistancia);
							secaoDao.saveOrUpdate(secao);
						}
					}
				}
						
				reader.close();
						
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}
