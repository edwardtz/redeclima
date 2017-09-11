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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.ufpe.redeclima.dao.DadosEstacaoTelemetricaDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.exception.SimulacaoException;
import com.ufpe.redeclima.interfaces.PontoDado;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Trecho;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

/**
 * @author edwardtz
 * Implementa funções basicas sobre a area de trabalho do usuario tais como configuração de arquivos, deletado, atualização das dadas, identificação dos nomes dos arquivos 
 * tais como projeto HMS e RAS, arquivos da geometria, plan, etc
 */
@Component
@Scope("prototype")
public class Workspace {

	private static final Logger logger = LoggerFactory.getLogger(Workspace.class);
	
	/* Diretorio dos modelos de simulação */
	@Value("${parameter.path_modelos}")
	private String pathModelos;
	
	/* Diretorio dos workspaces dos usuarios */
	@Value("${parameter.path_workspaces}")
	private String pathWorkspaces;
	
	private Usuario usuario;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private DadosEstacaoTelemetricaDao dadosEstacaoTelemetricaDao;
	
	/** Retorna o nome do arquivo de projeto RAS, esto asume que so tem um .prj no diretorio do RAS */
	public String identificarNomeProjetoRAS(Bacia bacia){
		
		String pathArquivoPrj = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
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
	
	/* Retorna o nome do arquivo do current plan do RAS pela extensão ej p01 ou p02, esto asume que os numeros pXX não se repetem */
	private String identificarNomePlan(Bacia bacia, String extensao){
		
		String pathArquivoPrj = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
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
	
	/** Retorna o nome do arquivo do current plan do RAS pela extensão ej p01 ou p02, esto asume que os numeros pXX não se repetem */
	public String identificarNomeCurrentPlan(Bacia bacia){
		
		String nomeArquivoProjetoRAS = identificarNomeProjetoRAS(bacia);
		
		String pathArquivoPrj = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoProjetoRAS).toString();
	
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
	
	
	/* Retorna o nome do arquivo U correspondente à bacia */
	private String identificarNomeArquivoU(Bacia bacia, String extensao){
		
		String pathArquivoPlan = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
		File dir = new File(pathArquivoPlan);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith("." + extensao)){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/** Retorna o nome do arquivo U correspondente à bacia */
	public String identificarNomeArquivoU(Bacia bacia){
		
		String nomeArquivoCurrentPlan = identificarNomeCurrentPlan(bacia);
		
		String pathArquivoPlan = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoCurrentPlan).toString();
	
		File inputFile = new File(pathArquivoPlan);
		
		BufferedReader reader;
		
		String nomeArquivoU=null;
		
		String extensaoU = null;
		
		try {
			if (inputFile.exists()){
				
					reader = new BufferedReader(new FileReader(inputFile));
					
					String currentLine;
					
					while(((currentLine = reader.readLine()) != null)) {
						if (currentLine.startsWith("Flow File=")){
							extensaoU = currentLine.split("=")[1];
							nomeArquivoU = identificarNomeArquivoU(bacia, extensaoU);
							reader.close();
							return nomeArquivoU;
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
	
		return nomeArquivoU;
	}
	
	/* Retorna o nome do arquivo de geometria correspondente à bacia */
	private String identificarNomeArquivoGeometria(Bacia bacia, String extensao){
		
		String pathArquivoGeom = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
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
	
	/**
	 * Retorna o nome do arquivo que contem a configuração da geometria, esse arquivo reside na pasta do modelo RAS
	 * */
	public String identificarNomeArquivoGeometria(Bacia bacia){
		
		String nomeArquivoCurrentPlan = identificarNomeCurrentPlan(bacia);
		
		String pathArquivoPlan = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoCurrentPlan).toString();
	
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
	
	/* Retorna o nome do arquivo de logs dentro da pasta do HMS */
	private String identificarArquivoLogHMS(Bacia bacia){
		
		String pathHMS = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathHMS);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".log")){
					return f.getName();
				}
			}
		}
		
		return null;
	}
	
	/* Retorna o nome do arquivo de logs dentro da pasta do HMS */
	private String identificarArquivoLogRAS(Bacia bacia){
		
		String pathHMS = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS").toString();
		
		File dir = new File(pathHMS);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().contains("comp_msgs")){
					return f.getName();
				}
			}
		}
		
		return null;
	}
	
	
	/**
	 * Retorna os logs do simulador HMS
	 * */
	public String getHMSLog(Bacia bacia){
		
		String nomeLog = identificarArquivoLogHMS(bacia);
		
		String pathArquivoLog = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS", nomeLog).toString();
		
		File inputFile = new File(pathArquivoLog);
		
		BufferedReader reader;
		
		StringBuilder sb = new StringBuilder();
		
		try {
			
			if (inputFile.exists()){
				
					reader = new BufferedReader(new FileReader(inputFile));
					
					String currentLine;
					
					sb.append("====================LOG HMS========================");
					sb.append(System.getProperty("line.separator"));
					
					while(((currentLine = reader.readLine()) != null)) {
						sb.append(currentLine);
						sb.append(System.getProperty("line.separator"));
					}
					
					sb.append("====================FIM LOG HMS========================");
					
					reader.close();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
		
	}
	
	/**
	 * Retorna os logs do simulador RAS
	 * */
	public String getRASLog(Bacia bacia){
		
		String nomeLog = identificarArquivoLogRAS(bacia);
		
		String pathArquivoLog = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS", nomeLog).toString();
		
		File inputFile = new File(pathArquivoLog);
		
		BufferedReader reader;
		
		StringBuilder sb = new StringBuilder();
		
		try {
			
			if (inputFile.exists()){
				
					reader = new BufferedReader(new FileReader(inputFile));
					
					String currentLine;
					
					sb.append("====================LOG RAS========================");
					sb.append(System.getProperty("line.separator"));
					
					while(((currentLine = reader.readLine()) != null)) {
						sb.append(currentLine);
						sb.append(System.getProperty("line.separator"));
					}
					
					sb.append("====================FIM LOG RAS========================");
					
					reader.close();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sb.toString();
		
	}
	
	/**
	 * Lee os trechos configurados no arquivo U do RAS e retorna a lista de nomes
	 * */
	public List<String> getNomesTrechosRAS(Bacia bacia){
		
		List<String> nomes = new ArrayList<String>();

		String nomeArquivoU = identificarNomeArquivoU(bacia);
		
		String pathArquivoU = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "RAS", nomeArquivoU).toString();
		
		File inputFile = new File(pathArquivoU);
		
		BufferedReader reader;
		
		try {

			reader = new BufferedReader(new FileReader(inputFile));
			
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null)) {
				
				if (currentLine.startsWith("Initial Flow Loc=")){
					String[] partes = currentLine.split(","); 
					String nomeTrecho = partes[1].trim().toUpperCase();
					nomes.add(nomeTrecho);
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
		
		return nomes;
		
	}
	
	
	/**
	 * Modifica os arquivos de projeto RAS antes da execução da simulação
	 * */
	public void modificarArquivosRAS(SimDto simDto){
		
		// Atualiza o arquivo de projeto
    	modificarArquivoPrj(simDto);
    	
    	// Atualiza o arquivo de plan
    	modificarArquivoPlan(simDto);
    	
    	// Atualizar o arquivo uNN
    	modificarArquivoU(simDto);
	}
	
	/**
	 * Modifica o arquivo prj do projeto RAS antes da execução da simulação
	 * */
	public void modificarArquivoPrj(SimDto simDto){
		
		String nomeArquivoProjetoRAS = identificarNomeProjetoRAS(simDto.getBacia());
		
		String pathArquivoPrj = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", nomeArquivoProjetoRAS).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", simDto.getBacia().getNome() + "TempSimulaEventos.prj").toString();
		
		File inputFile = new File(pathArquivoPrj);
		
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("ddMMMyyyy",Locale.US);
		SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm");
		
		try {
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS Start Date=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS Start Date=")){
				currentLine = "DSS Start Date=" + dataFormat.format(simDto.getDataInicial());
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS Start Time=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS Start Time=")){
				currentLine = "DSS Start Time=" + horaFormat.format(simDto.getDataInicial());
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS End Date=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS End Date=")){
				currentLine = "DSS End Date=" + dataFormat.format(simDto.getDataFinal());
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS End Time=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS End Time=")){
				currentLine = "DSS End Time=" + horaFormat.format(simDto.getDataFinal());
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS File=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS File=")){
				currentLine = "DSS File= ..\\" + simDto.getBacia().getNome() + "SimulaEventos.dss";
				writer.write(currentLine);
				writer.newLine();
			}
			
			
			// Termina de copiar o restante
			while((currentLine = reader.readLine()) != null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Este metodo modifica o arquivo pNN de plan RAS antes da esxecução da simulação
	 * */
	public void modificarArquivoPlan(SimDto simDto){
		
		String nomeArquivoCurrentPlan = identificarNomeCurrentPlan(simDto.getBacia());
		
		String pathArquivoPlan = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", nomeArquivoCurrentPlan).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", simDto.getBacia().getNome() + "TempSimulaEventos.tmp").toString();
		
		File inputFile = new File(pathArquivoPlan);
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("ddMMMyyyy",Locale.US);
		SimpleDateFormat horaFormat = new SimpleDateFormat("HH:mm");
		
		try {
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Short Identifier=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("Short Identifier=")){
				currentLine = "Short Identifier=" + simDto.getBacia().getNome() + "Sim";
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Simulation Date=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("Simulation Date=")){
				Date ultimaTimeSeries = simDto.getGrade().obterUltimoTempoSerie(simDto.getDataFinal());
				currentLine = "Simulation Date=" + dataFormat.format(simDto.getDataInicial()) + "," + horaFormat.format(simDto.getDataInicial()) + "," + dataFormat.format(ultimaTimeSeries) + "," + horaFormat.format(ultimaTimeSeries);
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Computation Interval=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("Computation Interval=")){
				currentLine = "Computation Interval=" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo();
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Output Interval=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("Output Interval=")){
				currentLine = "Output Interval=" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo();
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Instantaneous Interval=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("Instantaneous Interval=")){
				currentLine = "Instantaneous Interval=" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo();
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("DSS File=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("DSS File=")){
				currentLine = "DSS File= ..\\" + simDto.getBacia().getNome() + "SimulaEventos.dss";
				writer.write(currentLine);
				writer.newLine();
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("WQ Max Comp Step=")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if(currentLine.startsWith("WQ Max Comp Step=")){
				currentLine = "WQ Max Comp Step=" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo();
				writer.write(currentLine);
				writer.newLine();
			}
			
			
			// Termina de copiar o restante
			while((currentLine = reader.readLine()) != null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Este metodo retorna a data de inicio do bloco do mes, sempre é o primeiro dia de cada mes
	 * @param dataInicio data de inicio do periodo de simulação
	 * */
	private Date dataInicioBloco(Date dataInicio){
		Calendar calendario = Calendar.getInstance();
		calendario.setTime(dataInicio);
		calendario.set(Calendar.DAY_OF_MONTH, 1);
		return calendario.getTime();
	}
	
	
	/**
	 * Este metodo modifica o arquivo uNN de unstady configuration RAS antes da esxecução da simulação
	 * */
	public void modificarArquivoU(SimDto simDto){
		
		String nomeArquivoU = identificarNomeArquivoU(simDto.getBacia());
		
		String nomeArquivoRun = identificarArquivoRUN(simDto.getBacia());
		
		String nomeRun = "";
		
		if (nomeArquivoRun != null){
			nomeRun = nomeArquivoRun.split("\\.")[0];
		}
		
		String pathArquivoU = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", nomeArquivoU).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", simDto.getBacia().getNome() + "TempSimulaEventos.tmu").toString();
		
		File inputFile = new File(pathArquivoU);
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy",Locale.US);
		
		try {

			Date dataInicioDeBloco = dataInicioBloco(simDto.getDataInicial());
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			String currentLine;
			
			
			while(((currentLine = reader.readLine()) != null)) {
				
				if (currentLine.startsWith("Initial Flow Loc=")){
					String[] partes = currentLine.split(","); 
					String nomeTrecho = partes[1].trim().toUpperCase();
					Trecho trecho = trechoDao.findByNome(nomeTrecho);
					if (trecho.getEstacaoRef()!=null){
						Double valorUltimaVazao = dadosEstacaoTelemetricaDao.getUltimoValorVazao(trecho.getEstacaoRef(), simDto.getDataInicial());
						if (valorUltimaVazao != null){
							if (trecho.getVazaoMinima() != null && trecho.getVazaoMinima() <= valorUltimaVazao){
								partes[3] = valorUltimaVazao.toString();
							}else {
								if(trecho.getVazaoMinima() != null){
									partes[3] = trecho.getVazaoMinima().toString();
								}
							}
							currentLine = partes[0] + "," + partes[1] + "," + partes[2] + "," + partes[3];
						}
					}
					writer.write(currentLine);
					writer.newLine();
					
				} else if (currentLine.startsWith("Interval=")){
					
					currentLine = "Interval=" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo().toUpperCase();
					writer.write(currentLine);
					writer.newLine();
				
				} else if(currentLine.startsWith("DSS File=")){
					
					currentLine = "DSS File= ..\\" + simDto.getBacia().getNome() + "SimulaEventos.dss";
					writer.write(currentLine);
					writer.newLine();
					
				} else if (currentLine!=null && currentLine.startsWith("DSS Path=")){
					
					String[] partes = currentLine.split("/");
					if (partes.length > 1){
						currentLine = "DSS Path=//" + partes[2] + "/" + partes[3] + "/" + dateFormat.format(dataInicioDeBloco).toUpperCase() + "/" + simDto.getGrade().getQuantidadeTempoPeriodo() + simDto.getGrade().getUnidadeTempo().getCodigo().toUpperCase() + "/RUN:" + nomeRun.toUpperCase() + "/";  // Esta parte devera coincidir com os tags do path da saida do HMS
					}
					writer.write(currentLine);
					writer.newLine();
				
				} else {
					
					writer.write(currentLine);
					writer.newLine();
					
				}
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Este método modifica o arquivo ScriptDssUtl resultados RAS apos a execução dos modelos de simulação
	 * @param bacia bacia da simulação
	 * @param grade grade da simulação
	 * @param dataDesde data de inicio dos dados da simulação
	 * @param dataFim data de fim da simulação
	 * */
	public void modificarArquivoScriptExportacaoDss(SimDto simDto){
		
		String nomeArquivoGeometria = identificarNomeArquivoGeometria(simDto.getBacia());
		
		String pathArquivoGeometria = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", nomeArquivoGeometria).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "RAS", simDto.getBacia().getNome() + "SimulaEventosDssutl.txt").toString();
		
		File inputFile = new File(pathArquivoGeometria);
		File tempFile = new File(pathArquivoTemporal);
		
		BufferedReader reader;
		BufferedWriter writer;
		
		SimpleDateFormat dataFormat = new SimpleDateFormat("ddMMMyyyy",Locale.US);
		SimpleDateFormat horaFormat = new SimpleDateFormat("HHmm");
		
		try {
		
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			// Elimiar todas as referencias aos registros Gage:
			String currentLine;
			
			writer.write(Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", simDto.getBacia().getNome() + "SimulaEventos").toString());
			writer.newLine();
			
			writer.write("FO, (f15.6)");
			writer.newLine();
			
			Calendar calendario = Calendar.getInstance();
			int i = 1;
			while((currentLine = reader.readLine()) != null){
				
				if(currentLine.startsWith("River Reach=")){
					String[] partes = currentLine.split("=")[1].split(",");
					String rio = partes[0].trim().toUpperCase();
					String trecho = partes[1].trim().toUpperCase();
					
					Date pivote = simDto.getDataInicial();
					
					String data;
					String hora;
					
					while (pivote.before(simDto.getDataFinal())){
						if (horaFormat.format(pivote).compareTo("0000")==0){
							hora = "2400";
							calendario.setTime(pivote);
							calendario.add(Calendar.DAY_OF_MONTH, -1);
							data = dataFormat.format(calendario.getTime()).toUpperCase();
						} else {
							hora = horaFormat.format(pivote);
							data = dataFormat.format(pivote).toUpperCase();
						}
						
						writer.write("WR.C, TO=" + Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "tmpRAS", simDto.getBacia().getNome() + "ResultRas" + String.format("%04d", i) + ".txt").toString() + ", " + "/" + rio + " " + trecho + "//LOCATION-ELEV//" + data + " " + hora + "/" + simDto.getBacia().getNome().toUpperCase() + "SIM/");
						writer.newLine();
						pivote = simDto.getGrade().obterSeguinteTempoSerie(pivote);
						i++;
					}
				}
				
			}
			
			writer.write("CL, MAIN");
			writer.newLine();
			
			
			writer.close();
			reader.close();
		
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/** Retorna o nome do arquivo de projeto HMS, esto asume que so tem um .hms no diretorio do HMS */
	public String identificarNomeProjetoHMS(Bacia bacia){
		
		String pathArquivoHms = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoHms);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".hms")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/** Retorna o nome do arquivo de RUN do projeto HMS, esto asume que so tem um .RUN no diretorio do HMS */
	public String identificarArquivoRUN(Bacia bacia){
		
		String pathArquivoHms = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoHms);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".run")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Este metodo modifica a lina de path do script em jyton que executa o HMS
	 * */
	public void modificarArquivoScriptHMS(SimDto simDto){
		
		String pathArquivoScript = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", simDto.getBacia().getNome() + "SimulaEventosHMS.script").toString();
		File scriptFile = new File(pathArquivoScript);
		BufferedWriter writer;
		String currentLine = null;
		String nomeArquivoHms = identificarNomeProjetoHMS(simDto.getBacia());
		
		String nomeArquivoRun = identificarArquivoRUN(simDto.getBacia());
		
		String nomeRun = nomeArquivoRun.split("\\.")[0];
		
		String nomeHms = nomeArquivoHms.split("\\.")[0];
		
		try {
			writer = new BufferedWriter(new FileWriter(scriptFile));
			currentLine = "from hms.model.JythonHms import *";
			writer.write(currentLine);
			writer.newLine();
			currentLine = "OpenProject(\"" + nomeHms + "\",\"" + Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS").toString() + "\")"; 
			writer.write(currentLine);
			writer.newLine();
			currentLine = "Compute(\"" + nomeRun + "\")";
			writer.write(currentLine);
			writer.newLine();
			currentLine = "Exit(1)";
			writer.write(currentLine);
			writer.newLine();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Retorna o nome do arquivo de controle HMS, esto asume que so tem um .control no diretorio do HMS */
	public String identificarArquivoControlHMS(Bacia bacia){
		
		String pathArquivoControl = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoControl);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".control")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Este metodo modifica a cada rodada da simulação o arquivo de controle atualizando as datas de simulação
	 * */
	public void modificarArquivoControlHMS(SimDto simDto){
	
		String nomeArquivoControl = identificarArquivoControlHMS(simDto.getBacia());
		
		String pathArquivoControl = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", nomeArquivoControl).toString();
		
		File controlFile = new File(pathArquivoControl);

		BufferedWriter writer;
		
		SimpleDateFormat dateFormatControl = new SimpleDateFormat("d MMMM yyyy", Locale.US);
		
		/* Formatação do tempo HH:mm:ss */
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
		
		/* Formatação do tempo HH:mm sem os segundos */
		SimpleDateFormat timeFormatSemSeg = new SimpleDateFormat("HH:mm");
		
		String nomeControl = nomeArquivoControl.split("\\.")[0];
		
		try {
			
			writer = new BufferedWriter(new FileWriter(controlFile));
			
			String currentLine = "Control: " + nomeControl;
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     Description: Arquivo de Controle HMS";
			writer.write(currentLine);
			writer.newLine();
			
			Date agora = new Date();
			currentLine = "     Last Modified Date: " + dateFormatControl.format(agora);
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     Last Modified Time: " + timeFormat.format(agora);
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     Version: 3.5"; //TODO adicionar desde um properties
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     Start Date: " + dateFormatControl.format(simDto.getDataInicial());
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     Start Time: " + timeFormatSemSeg.format(simDto.getDataInicial());
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     End Date: " + dateFormatControl.format(simDto.getDataFinal());
			writer.write(currentLine);
			writer.newLine();
			
			// Configura o endTime com a ultima data do dia de acordo com a resolução temporal da grade
			currentLine = "     End Time: " + timeFormatSemSeg.format(simDto.getGrade().obterUltimoTempoSerie(simDto.getDataFinal())); 
			writer.write(currentLine);
			writer.newLine();
			
			int timeInterval = 0;
			if (simDto.getGrade().getUnidadeTempoPeriodo() == EnumUnidadeTempo.DIA.getId()){
				timeInterval = 1440; // Minutos
			}else if(simDto.getGrade().getUnidadeTempoPeriodo() == EnumUnidadeTempo.HORA.getId()){
				timeInterval = simDto.getGrade().getQuantidadeTempoPeriodo() * 60;
			}
			
			currentLine = "     Time Interval: " + timeInterval;
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "     State Grid Write Interval: " + timeInterval;
			writer.write(currentLine);
			writer.newLine();
			
			currentLine = "End:";
			writer.write(currentLine);
			writer.newLine();
			
			writer.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Retorna o nome do arquivo de met do HMS, esto asume que so tem um .met no diretorio do HMS */
	public String identificarArquivoMetHMS(Bacia bacia){
		
		String pathArquivoMet = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoMet);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".met")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 *  Este arquivo modifica os dados dos pontos de dados dependendo do conjunto de dados selecionado 
	 *  */
	public void modificarArquivoMetHMS(SimDto simDto, List<PontoDado> pontos){
	
		String nomeArquivoMet = identificarArquivoMetHMS(simDto.getBacia());
		
		String pathArquivoMet = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", nomeArquivoMet).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", simDto.getBacia().getNome() + "TempSimulaEventos.met").toString();
		
		File inputFile = new File(pathArquivoMet);
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		try {
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			// Elimiar todas as referencias aos registros Gage: b
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Gage:")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if (currentLine!=null && currentLine.startsWith("Gage:")){
				currentLine = reader.readLine(); //Lee a linha Type: Recording
				currentLine = reader.readLine(); //Lee a Liha End:
			}
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Precip Method Parameters:")) {
				if (currentLine.startsWith("Gage:")){
					currentLine = reader.readLine(); //Lee a linha Type: Recording
					currentLine = reader.readLine(); //Lee a Liha End:
				}
			}
			
			if (currentLine!=null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			// Termina de copiar o restante
			while((currentLine = reader.readLine()) != null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
			// Adicionar os pontos de dados
			inputFile = new File(pathArquivoMet);
			tempFile = new File(pathArquivoTemporal);
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			currentLine = reader.readLine();

			if (currentLine != null && currentLine.startsWith("Meteorology:")){
				writer.write(currentLine);
				writer.newLine();
			}else{
				writer.close();
				reader.close();
				return;
			}

			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("End:")) {
			    writer.write(currentLine);
			    writer.newLine();
			}
			
			writer.write(currentLine);
			writer.newLine();
			writer.newLine();
			
			// Adicionar os pontos de grade
			for(PontoDado ponto: pontos){
				writer.write("Gage: " + ponto.getGageId());
				writer.newLine();
				writer.write("     Type: Recording");
				writer.newLine();
				writer.write("End:");
				writer.newLine();
				writer.newLine();
			}
			
			currentLine = reader.readLine();
					
			// Escreve o restante do arquivo
			while(((currentLine = reader.readLine()) != null)) {
			    writer.write(currentLine);
			    writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Este metodo gera um arquivo de texto para um ponto de dados para uma bacia
	 * */
	public void gerarArquivoEntradaDssHMS(SimDto simDto, PontoDado ponto, List<JSONObject> dados){
		
		/* Formatação da data dentro dos arquivos txt gerados para cada ponto */
		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMMyyyy", Locale.US);
		
		String pathArquivoDss;
		String pathArquivoTxt;
		String cabecalho;
		File outFile;
		BufferedWriter bufferedWriter;
		String dataInicialTexto = dateFormat.format(simDto.getDataInicial());
		String indicadorUnidade = null;
		
		if(simDto.getUnidade()==EnumUnidadeTempo.HORA){
			indicadorUnidade = simDto.getGrade().getQuantidadeTempoPeriodo() + "HOUR";
		}else if (simDto.getUnidade()==EnumUnidadeTempo.DIA){
			indicadorUnidade = "1DAY";
		}
			
		pathArquivoTxt = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "tmpHMS", "P" + ponto.getId() + ".txt").toString() ;
			
		pathArquivoDss = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", simDto.getBacia().getNome() + "SimulaEventos.dss").toString();
			
		cabecalho = "/" + simDto.getBacia().getNome().toUpperCase() + "/" + "PONTO-" + ponto.getId() + "/PRECIP-INC/" + dataInicialTexto + "/" + indicadorUnidade + "/SIMULAMAVEN/";
			 
		outFile = new File(pathArquivoTxt);
			
		try{
				
			if (!outFile.exists()){
				outFile.createNewFile();
			}
				
			FileWriter fileWriter = new FileWriter(outFile);
			
			bufferedWriter = new BufferedWriter(fileWriter); 
				
			bufferedWriter.write(pathArquivoDss);
			bufferedWriter.newLine();
				
			bufferedWriter.write(cabecalho);
			bufferedWriter.newLine();
				
			bufferedWriter.write("mm");
			bufferedWriter.newLine();
				
			bufferedWriter.write("PER-CUM");
			bufferedWriter.newLine();
				
			bufferedWriter.write(dataInicialTexto+", 0000"); //TODO adicionar que hora de inicioda secuancia sea variavel
			bufferedWriter.newLine();
				
				
			if (dados!=null){
				for (JSONObject dado : dados){
					bufferedWriter.write(((Double)dado.get("chuva")).toString());
					bufferedWriter.newLine();
				}
			}
				
				
			bufferedWriter.write("END");
			bufferedWriter.newLine();
				
			bufferedWriter.close();
			fileWriter.close();
				
		} catch (IOException e) {
			// TODO adicional log
			e.printStackTrace();
		}
	}
	
	/**
	 * Carrega os dados de entrada no arquivo .dss usando os arquivos para cada ponto no diretorio temporal HMS
	 * */
	public void carregarArquivoDss(SimDto simDto){
		
		// Para cada arquivo rodar o dss
    	
    	String pathArquivosTemporaisTxt = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "tmpHMS").toString();
    	
    	Process process;
		
		String commandShell = null;
    	
    	File diretorio = new File(pathArquivosTemporaisTxt);
    	
    	try {
	    	if (diretorio.exists()){
	    		for (File arquivoTxt: diretorio.listFiles()){
	    			if (arquivoTxt.getName().endsWith(".txt")){
	    				commandShell = "cmd /c dssts" + " input=" + arquivoTxt.getPath();
	    				// Execute command
	                    process = Runtime.getRuntime().exec(commandShell);
	                   
						process.waitFor();
						
	    			}
	    		}
	    	}
    	} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
	}
	
	
	/** Retorna o nome do arquivo de calibração do HMS, esto asume que so tem um .gage no diretorio do HMS */
	public String identificarNomeArquivoGage(Bacia bacia){
		
		String pathArquivoGage = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoGage);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".gage")){
					return f.getName(); 
				}
			}
		}
		
		return null;
		
	}
	
	/** Retorna o nome do arquivo de calibração do HMS, esto asume que so tem um .gage no diretorio do HMS */
	public String identificarPathArquivoGage(Bacia bacia){
		
		String pathArquivoGage = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoGage);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".gage")){
					return Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS", f.getName()).toString(); 
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Este metodo gera o arquivo de calibração do modelo para a bacia
	 * */
	public void gerarArquivoGage(SimDto simDto, List<PontoDado> pontos){
		
		
		String nomeArquivoGage = identificarNomeArquivoGage(simDto.getBacia());
		
		String pathArquivoGage = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", nomeArquivoGage).toString();
		
		File outFile;
		
		BufferedWriter bufferedWriter;
		
		outFile = new File(pathArquivoGage);
		
		SimpleDateFormat dateFormat = new  SimpleDateFormat("ddMMMyyyy", Locale.US);
		SimpleDateFormat dataModificacao = new SimpleDateFormat("d MMMM yyyy",Locale.US);
		SimpleDateFormat horaModificacao = new SimpleDateFormat("HH:mm:ss");
		SimpleDateFormat dataPeriodo = new SimpleDateFormat("d MMMM yyyy, HH:mm", Locale.US);
		
		Calendar calendar = Calendar.getInstance(); 
		calendar.setTime(simDto.getDataFinal());
		calendar.set(Calendar.HOUR_OF_DAY, 21);
		calendar.set(Calendar.MINUTE, 0);
		Date dataFinal = calendar.getTime();
		
		String gageManager = nomeArquivoGage.split("\\.")[0];
		
		try {
			if (!outFile.exists()){
				outFile.createNewFile();
			}
			
			bufferedWriter = new BufferedWriter(new FileWriter(outFile)); 
			bufferedWriter.write("Gage Manager: " + gageManager);
			bufferedWriter.newLine();
			bufferedWriter.write("     Version: 3.5");
			bufferedWriter.newLine();
			bufferedWriter.write("     Filepath Separator: \\");
			bufferedWriter.newLine();
			bufferedWriter.write("End:");
			bufferedWriter.newLine();
			
			
			for (PontoDado ponto: pontos){
				bufferedWriter.newLine();
				bufferedWriter.write("Gage: " + ponto.getGageId());
				bufferedWriter.newLine();
				bufferedWriter.write("     Description: Código: " + ponto.getId()+"; Nome: " + "PONTO-" + ponto.getId() + "; Responsável: UFPE/SRHE");
				bufferedWriter.newLine();
				Date agora = new Date();
				bufferedWriter.write("     Last Modified Date: " + dataModificacao.format(agora));
				bufferedWriter.newLine();
				bufferedWriter.write("     Last Modified Time: " + horaModificacao.format(agora));
				bufferedWriter.newLine();
				bufferedWriter.write("     Units System: SI");
				bufferedWriter.newLine();
				bufferedWriter.write("     Latitude Degrees:  " + ponto.getLatitude());
				bufferedWriter.newLine();
				bufferedWriter.write("     Longitude Degrees: " + ponto.getLongitude());
				bufferedWriter.newLine();
				bufferedWriter.write("     Reference Height Units: Meters");
				bufferedWriter.newLine();
				bufferedWriter.write("     Reference Height: 100");
				bufferedWriter.newLine();
				bufferedWriter.write("     Gage Type: Precipitation");
				bufferedWriter.newLine();
				bufferedWriter.write("     Precipitation Type: Incremental");
				bufferedWriter.newLine();
				bufferedWriter.write("     Units: MM");
				bufferedWriter.newLine();
				bufferedWriter.write("     Data Type: PER-CUM");
				bufferedWriter.newLine();
				bufferedWriter.write("     Local to Project: NO");
				bufferedWriter.newLine();
				bufferedWriter.write("     Start Time: " + dataPeriodo.format(simDto.getDataInicial()));
				bufferedWriter.newLine();
				bufferedWriter.write("     End Time: " + dataPeriodo.format(dataFinal));
				bufferedWriter.newLine();
				bufferedWriter.write("     DSS File: " + Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", simDto.getBacia().getNome() + "SimulaEventos.dss" ));
				bufferedWriter.newLine();
				
				String indicadorUnidade = null;
				
				if(simDto.getUnidade()==EnumUnidadeTempo.HORA){
					indicadorUnidade = simDto.getGrade().getQuantidadeTempoPeriodo() + "HOUR";
				}else if (simDto.getUnidade()==EnumUnidadeTempo.DIA){
					indicadorUnidade = "1DAY";
				}
				
				bufferedWriter.write("     Pathname: " + "/" + simDto.getBacia().getNome().toUpperCase() + "/" + "PONTO-" + ponto.getId() + "/PRECIP-INC/" + dateFormat.format(simDto.getDataInicial())+ "-" + dateFormat.format(dataFinal) + "/" + indicadorUnidade + "/SIMULAMAVEN");
				bufferedWriter.newLine();
				bufferedWriter.write("End:");
			}	
			bufferedWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* Copia todos os projetos da pasta padrão do sistema para o workspace do usuario */
	private void copiarProjetosPadrao() {
		
		File dirPadrao = new File(Paths.get(pathModelos).toString());
		if(dirPadrao.isDirectory()) {
		    File[] conteudo = dirPadrao.listFiles();
		    for(File dir: conteudo) {
		    	File newDir = new File(Paths.get(getPathWorkSpace(), dir.getName()).toString());
		    	try {
					FileUtils.copyDirectory(dir, newDir);
				} catch (IOException e) {
					logger.error("Error ao criar a pasta " + newDir.getAbsolutePath());
					logger.error("Detalhe do error " + e.getMessage());
				}
		    }
		}
		
	}
	
	/**
	 * Inicializa o workspace do usuario
	 * */
	public void init(){
		
		if (usuario != null){
			// Criar pasta do usuario em W
			File userworkspace = new File(getPathWorkSpace());
			if (!userworkspace.exists()){
				
				userworkspace.mkdir();
				
				// Copiar todas as pastas e seus conteudos desde modelos para a pasta do usuario
				copiarProjetosPadrao();
				
			}
		}
		
	}
	
	/**
	 * Apaga os arquivos temporais das areas de trabalho para preparar uma nova execução dos modelos
	 * */
	public void apagarArquivosTemporais(Bacia bacia){
		
		String pathHMS = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "tmpHMS").toString();
		File diretorio = new File(pathHMS);
		Assert.notNull(diretorio, "Diretorio " + pathHMS + " não encontrado");
		Assert.state(diretorio.isDirectory());
		
		File[] files = diretorio.listFiles();
		for (int i = 0; i < files.length; i++) {
			boolean deleted = files[i].delete();
			if (!deleted) {
				throw new SimulacaoException("Não se pode deletar o arquivo " + files[i].getPath());
			} else {
				logger.debug(files[i].getPath() + " foi apagado!");
			}
		}
		
		String pathRAS = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "tmpRAS").toString();
		diretorio = new File(pathRAS);
		Assert.notNull(diretorio, "Diretorio " + pathRAS + " não encontrado");
		Assert.state(diretorio.isDirectory());
		
		files = diretorio.listFiles();;
		for (int i = 0; i < files.length; i++) {
			boolean deleted = files[i].delete();
			if (!deleted) {
				throw new SimulacaoException("Não se pode deletar o arquivo " + files[i].getPath());
			} else {
				logger.debug(files[i].getPath() + " foi apagado!");
			}
		}
		
		String pathDss = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", bacia.getNome() + "SimulaEventos.dss").toString();
		
		File arquivoDss = new File(pathDss);
		if (arquivoDss.exists()){
			arquivoDss.delete();
		}
		
		
		String pathDsc = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", bacia.getNome() + "SimulaEventos.dsc").toString();
		
		File arquivoDsc = new File(pathDsc);
		if (arquivoDsc.exists()){
			arquivoDsc.delete();
		}
	}
	
	
	/** Retorna o nome do arquivo de controle HMS, esto asume que so tem um .control no diretorio do HMS */
	public String identificarArquivoHMS(Bacia bacia){
		
		String pathArquivoControl = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase(), "HEC", "HMS").toString();
		
		File dir = new File(pathArquivoControl);
		
		if (dir.exists()){
			for(File f: dir.listFiles()){
				if (f.getName().endsWith(".hms")){
					return f.getName();
				}
			}
		}
		
		return null;
		
	}
	
	/**
	 * Modifica a linha de path do dss no arquivo principal do HMS
	 * */
	public void modificarArquivoHMS(SimDto simDto){
		
		String arquivoHms = identificarArquivoHMS(simDto.getBacia());
		
		String pathArquivoHMS = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", arquivoHms).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", simDto.getBacia().getNome() + "TempSimulaEventos.hms").toString();
		
		File inputFile = new File(pathArquivoHMS);
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		try {
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			// Elimiar todas as referencias aos registros Gage: b
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("     DSS File Name:")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if (currentLine!=null && currentLine.startsWith("     DSS File Name:")){
				currentLine = "     DSS File Name: ..\\" + simDto.getBacia().getNome() + "SimulaEventos.dss";
				writer.write(currentLine);
				writer.newLine();
			}
			
			// Termina de copiar o restante
			while((currentLine = reader.readLine()) != null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Modifica a linha do nome do RUN do arquivo RUN, quando se execute compute(nomeRun) acontece que nomeRun pode ser diferente do nome do arquivo RUN do HMS
	 * para resolver este problema se unifica nomeRun = nomeArquivoRun
	 * */
	public void modificarArquivoRUN(SimDto simDto){
		
		String arquivoRUN = identificarArquivoRUN(simDto.getBacia());
		
		String pathArquivoRUN = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", arquivoRUN).toString();
		
		String pathArquivoTemporal = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), simDto.getBacia().getNome().toUpperCase(), "HEC", "HMS", simDto.getBacia().getNome() + "TempSimulaEventos.tmr").toString();
		
		File inputFile = new File(pathArquivoRUN);
		File tempFile = new File(pathArquivoTemporal);

		BufferedReader reader;
		BufferedWriter writer;
		
		String nomeRun = arquivoRUN.split("\\.")[0];
		
		try {
			
			reader = new BufferedReader(new FileReader(inputFile));
			writer = new BufferedWriter(new FileWriter(tempFile));
			
			// Elimiar todas as referencias aos registros Gage: b
			String currentLine;
			
			while(((currentLine = reader.readLine()) != null) && !currentLine.startsWith("Run:")) {
				writer.write(currentLine);
				writer.newLine();
			}
			
			if (currentLine!=null && currentLine.startsWith("Run:")){
				currentLine = "Run: " + nomeRun;
				writer.write(currentLine);
				writer.newLine();
			}
			
			// Termina de copiar o restante
			while((currentLine = reader.readLine()) != null){
				writer.write(currentLine);
				writer.newLine();
			}
			
			writer.close();
			reader.close();
			
			inputFile.delete();
			tempFile.renameTo(inputFile);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * Apaga o workspace do usuario inteiro 
	 * */
	public void apagar(){
		
		try {
			if (usuario != null){
				File userworkspace = new File(getPathWorkSpace());
					if (userworkspace.exists()){
						FileUtils.deleteDirectory(userworkspace);
					}
			}
		} catch (IOException e) {
			logger.error("Error apagando o workspace do usuario " + usuario.getLogin());
			logger.error("Detalhe do error: " + e.getMessage());
		}	
			
	}
	
	/**
	 * Apaga uma pasta do modelo duma bacia na área de trabalho do usuario
	 * */
	private void apagarModelo(Usuario usuario, Bacia bacia){
		String pathWSBacia = Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase()).toString();
		File dirWSBacia = new File(pathWSBacia);
		if (dirWSBacia.exists()){
			try {
				FileUtils.deleteDirectory(dirWSBacia);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Copia uma pasta do modelo duma bacia na área de trabalho do usuario
	 * */
	private void copiarModelo(Usuario usuario, Bacia bacia){
		
		File dirPadrao = new File(Paths.get(pathModelos, bacia.getNome().toUpperCase()).toString());
		
		if(dirPadrao.isDirectory()) {
			File newDir = new File(Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase(), bacia.getNome().toUpperCase()).toString());
			try {
				FileUtils.copyDirectory(dirPadrao, newDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	/**
	 * Atualiza as áreas de trabalho dos usuarios com um novo modelo para uma bacia recentemente criada ou
	 * atualiza um modelo para as bacias ja existentes
	 * */
	public void atualizarWorkSpaces(Bacia bacia){
		List<Usuario> usuarios = usuarioDao.list();
		if (usuarios != null){
			for (Usuario u: usuarios){
				apagarModelo(u, bacia);
				copiarModelo(u, bacia);
			}
		}
	}
	
	/* Retorna o nome da pasta do worskspace do usuario, este nome corresponde com o login do usuario */
	public String getNomePastaUsuario(){
		return usuario.getLogin().toUpperCase();
	}
	
	/* Retorna o percurso completo do worskspace do usuario */
	public String getPathWorkSpace(){
		return Paths.get(pathWorkspaces, usuario.getLogin().toUpperCase()).toString();
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
}
