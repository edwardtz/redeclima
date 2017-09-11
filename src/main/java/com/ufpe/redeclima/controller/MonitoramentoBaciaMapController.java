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
package com.ufpe.redeclima.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.primefaces.context.RequestContext;
import org.primefaces.event.map.OverlaySelectEvent;
import org.primefaces.model.map.DefaultMapModel;
import org.primefaces.model.map.LatLng;
import org.primefaces.model.map.MapModel;
import org.primefaces.model.map.Marker;
import org.primefaces.model.map.Overlay;
import org.primefaces.model.map.Polyline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.DadoPontoGradeDao;
import com.ufpe.redeclima.dao.EstacaoDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.LimitesBaciasKMLDao;
import com.ufpe.redeclima.dao.PontoGradeDao;
import com.ufpe.redeclima.dao.ResultadoObsRasDao;
import com.ufpe.redeclima.dao.ResultadoRasDao;
import com.ufpe.redeclima.dao.RiosKMLDao;
import com.ufpe.redeclima.dao.SecoesKMLDao;
import com.ufpe.redeclima.dao.SimulacaoDao;
import com.ufpe.redeclima.dao.SimulacaoObsDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.dto.PontoGradeDto;
import com.ufpe.redeclima.dto.SecaoDto;
import com.ufpe.redeclima.dto.SimHistDto;
import com.ufpe.redeclima.dto.SimulacaoDto;
import com.ufpe.redeclima.dto.SimulacaoObsDto;
import com.ufpe.redeclima.interfaces.SimDto;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Grade;
import com.ufpe.redeclima.model.PontoGrade;
import com.ufpe.redeclima.model.Simulacao;
import com.ufpe.redeclima.model.SimulacaoObs;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.task.TaskManager;
import com.ufpe.redeclima.task.batch.EstadoSimulacao;
import com.ufpe.redeclima.util.EnumEstadoEstacao;
import com.ufpe.redeclima.util.EnumEstadosSimulacao;
import com.ufpe.redeclima.util.EnumTipoDados;
import com.ufpe.redeclima.util.EnumTipoOverlay;
import com.ufpe.redeclima.util.EnumTipoResponsavel;
import com.ufpe.redeclima.util.EnumUnidadeTempo;

@Controller
@Scope("session")
public class MonitoramentoBaciaMapController implements InitializingBean{

	private static final Logger logger = LoggerFactory.getLogger(MonitoramentoBaciaMapController.class);
	
	@Value("${dn.server}")
	private String dnServer;
	
	private Estacao estacaoSelected;
	
	private PontoGrade pontoGradeSelected;
	
	private String rioSecaoSelected;
	
	private String trechoSecaoSelected;
	
	private String secaoSelected;
	
	private String log;
	
	/* Centro da bacia na coordenada de longitude */
	private double centerX;
	
	/* Centro da bacia na coordenada de latitude */
	private double centerY;
	
	/* Modelo de mapa da bacia que integra Primafaces com Google Map*/
	private MapModel simpleModel;
	
	/* Estacão seleccionada no Google Map */
	private Overlay markerSelected;
	
	/* Flag para mostrar a grade */
	private boolean mostrarGrade;
	
	/* Flag para mostrar as secoes apos a execução da simulação */
	private boolean mostrarSecoes;
	
	/* Variavel para saber se o objeto seleccionado e uma estação, ponto da grade ou seção */
	private int tipoOverlay;
	
	/* Simulacao seleccionado do historico */
	private SimHistDto simulacaoSelected;
	
	/* Formatador do date de entrada usado para seleccionar a data desde o historico */
	private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private int zoom;
	
	private EnumTipoDados tipoDados;
	
	private SimDto simDto;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private EstacaoDao estacaoDao;
	
	@Autowired
	private TaskManager taskManager;
	
	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private PontoGradeDao pontoGradeDao;
	
	@Autowired
	private LimitesBaciasKMLDao limitesBaciasKMLDao;
	
	@Autowired
	private RiosKMLDao riosKMLDao;
	
	@Autowired
	private SecoesKMLDao secoesKMLDao;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private SimulacaoDao simulacaoDao;
	
	@Autowired
	private SimulacaoObsDao simulacaoObsDao;
	
	@Autowired
	private ResultadoRasDao resultadoRasDao;
	
	@Autowired
	private ResultadoObsRasDao resultadoObsRasDao;
	
	@Autowired
	private DadoPontoGradeDao dadoPontoGradeDao;
	
	public MonitoramentoBaciaMapController(){
		zoom = 9;
	}
	
	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		
		List<Bacia> bacias = baciaDao.listOperativas();
		
		simDto = (SimulacaoDto) applicationContext.getBean("simulacaoDto");
		
		if (simDto.getBacia()==null && bacias!=null && !bacias.isEmpty()){
			simDto.setBacia(baciaDao.listOperativas().get(0));
		}
		List<Grade> grades = gradeDao.listAtivas();
		if (grades!=null && grades.size()>0){
			simDto.setGrade(grades.get(0));
		}

		carregarCamadas();
		
		String login = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
		
		simDto.setUsuario(usuarioDao.findByLogin(login));
		
		simDto.setUnidade(EnumUnidadeTempo.HORA);
		
		tipoDados = EnumTipoDados.PREVISAO;
	}
	
	/**
	 * Este método carrega as camadas que vão se visualizar no mapa
	 * */
	private void carregarCamadas(){
		
		calcularCentroBaciaAtual();
		
		mostrarEstacoes();
		
		if(mostrarGrade){
			mostrarPontosGrade();
		}
		
		mostrarBacias();
		
		mostrarRios();
		
		if(mostrarSecoes){
			mostrarSecoes();
		}
	}
	
	private void calcularCentroBaciaAtual(){
		
		centerX = (simDto.getBacia().getLongitudeX1() + simDto.getBacia().getLongitudeX2()) / 2; 
		
		centerY = (simDto.getBacia().getLatitudeY1() + simDto.getBacia().getLatitudeY2()) / 2; 
	}
	
	private void mostrarEstacoes(){
		
		simpleModel = new DefaultMapModel();  
        
		List<Estacao> estacoes = estacaoDao.listEstacoes(EnumTipoResponsavel.ANA);
		
		for(Estacao estacao: estacoes){
			LatLng coord = new LatLng(estacao.getLatitude(), estacao.getLongitude());
			Marker marker = new Marker(coord, estacao.getNome());
			marker.setClickable(true);
			marker.setData(estacao);
			//TODO procurar tirar a variavel serverPort e carregar como getResources()
			if (estacao.getEstadoEstacao() == EnumEstadoEstacao.ALT){
				marker.setIcon("http://" + dnServer + "/redeclima/resources/images/red-triangle.png");
			} else if (estacao.getEstadoEstacao() == EnumEstadoEstacao.FDS){
				marker.setIcon("http://" + dnServer + "/redeclima/resources/images/gray-triangle.png");
			}else if (estacao.getEstadoEstacao() == EnumEstadoEstacao.FED){
				marker.setIcon("http://" + dnServer + "/redeclima/resources/images/yellow-triangle.png");
			}else{
				marker.setIcon("http://" + dnServer + "/redeclima/resources/images/blue-triangle3.png");
			}
			
			simpleModel.addOverlay(marker); 
		}
	}
	
	/**
	 * Este método carrega os pontos de grade no mapa
	 * */
	private void mostrarPontosGrade(){
		
		List<PontoGrade> pontos = pontoGradeDao.getPontosGradeBacia(simDto.getBacia(), simDto.getGrade());
		for(PontoGrade ponto: pontos){

			LatLng coord = new LatLng(ponto.getLatitude(), ponto.getLongitude());
			Marker marker = new Marker(coord, "Ponto de grade: " + simDto.getGrade().getNome());
			marker.setTitle(simDto.getGrade().getNome());
			marker.setClickable(true);
			marker.setData(new PontoGradeDto(ponto));
			marker.setIcon("http://" + dnServer + "/redeclima/resources/images/pontoGrade.png");
			
			simpleModel.addOverlay(marker);
			
		}
	}
	
	/**
	 * Este método carrega os limites das bacias no mapa
	 * */
	private void mostrarBacias(){
	
        for (Polyline polyline: limitesBaciasKMLDao.getPolylineBacias()){
        	polyline.setStrokeWeight(2);  
	    	polyline.setStrokeColor("#44ff0000");
	    	for (Bacia bacia: baciaDao.list()){
	    		if (bacia.getNome().toUpperCase().compareTo(((String)polyline.getData()).toUpperCase())==0){
	    			simpleModel.addOverlay(polyline);
		    	}
	    	}
        }
	}
	
	/**
	 * Este método carrega os limites das bacias no mapa
	 * */
	private void mostrarSecoes(){
		List<Polyline> secoes = secoesKMLDao.getSecoes(simDto.getBacia());
		for (Polyline polyline: secoes){
        	polyline.setStrokeWeight(4); 
	    	polyline.setStrokeColor("#880000");
	    	simpleModel.addOverlay(polyline);
        }
	}
	
	/**
	 * Este método é usado para carregar os dados dos rios no mapa
	 * */
	private void mostrarRios(){
		
		for (Polyline polyline: riosKMLDao.getPolylineRios()){
        	polyline.setStrokeWeight(2);  
	    	polyline.setStrokeColor("#0099FF");
	    	simpleModel.addOverlay(polyline);
        }
	}
	
	public void onMarkerSelect(OverlaySelectEvent event) {  
	        
	 	markerSelected = (Overlay) event.getOverlay();  
	 	
	 	if (markerSelected.getData() instanceof Estacao){
	 		tipoOverlay = EnumTipoOverlay.ESTACAO.getId();
	 	}else if(markerSelected.getData() instanceof PontoGradeDto){
	 		tipoOverlay = EnumTipoOverlay.PONTO_GRADE.getId();
	 	}else{
	 		tipoOverlay = EnumTipoOverlay.SECAO.getId();
	 		if (markerSelected.getData() instanceof SecaoDto){
	 			SecaoDto secao = (SecaoDto) markerSelected.getData();
		 		if (secao!=null){
		 			secaoSelected = secao.getNomeSecao();
		 			rioSecaoSelected = secao.getNomeRio();
		 			trechoSecaoSelected = secao.getNomeTrecho();
		 		}
	 		}
	 	}
	} 
	
	public void mostrarResultados(){
		if (tipoOverlay == EnumTipoOverlay.SECAO.getId()){
			RequestContext requestContext = RequestContext.getCurrentInstance();
 	        requestContext.execute("doPopupSimulacao(this,'" + simDto.getUsuario().getId() + "','" + simDto.getBacia().getId() + "','" + simDto.getGrade().getId() + "','" + dateFormat.format(simDto.getDataInicial()) + "','" + dateFormat.format(simDto.getDataFinal()) + "','" + secaoSelected + "','" + rioSecaoSelected + "','" + trechoSecaoSelected + "','" + tipoDados.getId() + "')");
		}
	}
	 
	public void addMessage(FacesMessage message) {  
	        FacesContext.getCurrentInstance().addMessage(null, message);  
	    } 
	
	public String mostrarDadosMonitoramento(){
		return "monitoramentoEstacao";
	}
	
	public String getAtualizarDados(){
		carregarCamadas();
		return null;
	}
	
	public void atualizarDados(){
		carregarCamadas();
	}
	
	public void mostrarOcultarGrade(){
		carregarCamadas();
		RequestContext requestContext = RequestContext.getCurrentInstance();
	    requestContext.execute("stop()");
	}
	
	public void executarSimulacao(){
		
		mostrarSecoes = false;
		
		if (simDto.getDataInicial()==null){
			logger.error("Data inicial não definida");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data inicial não definida", "Data inicial não definida"));
			return;
		}
		
		if (simDto.getDataFinal()==null){
			logger.error("Data final não definida");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data final não definida", "Data inicial não definida"));
			return;
		}
		
		if (simDto.getDataInicial()!=null && simDto.getDataFinal()!=null && simDto.getDataInicial().after(simDto.getDataFinal())){
			logger.error("Data inicial deve ser anterior a data final");
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Data inicial deve ser anterior a data final", "Data inicial deve ser anterior a data final"));
			return;
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(simDto.getDataFinal());
		calendar.set(Calendar.HOUR_OF_DAY, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		simDto.setDataFinal(calendar.getTime());
		
		simDto.getEstadoSimulacao().setProgress(0);
		
		simDto.getEstadoSimulacao().setEstado(EnumEstadosSimulacao.STEP1);
		
		taskManager.executarSimulacao(simDto);
		
		mostrarSecoes = true;
		
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Simulação executada com sucesso"));
        
	}
	
	public void selecionarHistorico(){
		simDto.setDataInicial(simulacaoSelected.getDataInicial());
		simDto.setDataFinal(simulacaoSelected.getDataFinal());
		simDto.setBacia(simulacaoSelected.getBacia());
		simDto.setGrade(simulacaoSelected.getGrade());
		if(!mostrarSecoes){
			mostrarSecoes=true;
		}
		atualizarDados();
	}
	
	public void apagarSimulacao(){
		if (tipoDados == EnumTipoDados.PREVISAO){
			Simulacao simulacaoApagar = simulacaoDao.findById(simulacaoSelected.getId());
			resultadoRasDao.removerResultados(simulacaoApagar);
			simulacaoDao.remove(simulacaoApagar);
		}else {
			SimulacaoObs simulacaoApagar = simulacaoObsDao.findById(simulacaoSelected.getId());
			resultadoObsRasDao.removerResultados(simulacaoApagar);
			simulacaoObsDao.remove(simulacaoApagar);
		}
		
	}
	
	public void mudouTipo(){
		
		SimDto ant = simDto;
		if (tipoDados == EnumTipoDados.OBSERVACAO){
			simDto = (SimulacaoObsDto) applicationContext.getBean("simulacaoObsDto");
		} else if(tipoDados == EnumTipoDados.PREVISAO){
			simDto = (SimulacaoDto) applicationContext.getBean("simulacaoDto");
			List<Grade> grades = gradeDao.listAtivas();
			if (grades!=null && grades.size()>0){
				simDto.setGrade(grades.get(0));
			}
		}
		
		simDto.setBacia(ant.getBacia());
		simDto.setDataInicial(ant.getDataInicial());
		simDto.setDataFinal(ant.getDataFinal());
		simDto.setUnidade(ant.getUnidade());
		simDto.setUsuario(ant.getUsuario());
		
	}
	
	public Collection<Estacao> getEstacoesANA(){
		return estacaoDao.listEstacoesANA(simDto.getBacia());
	}

	public Estacao getEstacaoSelected() {
		return estacaoSelected;
	}

	public void setEstacaoSelected(Estacao estacaoSelected) {
		this.estacaoSelected = estacaoSelected;
	}


	public List<Grade> getGrades() {
		return gradeDao.listAtivas();
	}

	public PontoGrade getPontoGradeSelected() {
		return pontoGradeSelected;
	}

	public void setPontoGradeSelected(PontoGrade pontoGradeSelected) {
		this.pontoGradeSelected = pontoGradeSelected;
	}

	public List<PontoGrade> getPontosGrade() {
		return pontoGradeDao.getPontosGradeBacia(simDto.getBacia(), simDto.getGrade());
	}

	public double getCenterX() {
		return centerX;
	}

	public void setCenterX(double centerX) {
		this.centerX = centerX;
	}

	public double getCenterY() {
		return centerY;
	}

	public void setCenterY(double centerY) {
		this.centerY = centerY;
	}

	public MapModel getSimpleModel() {
		return simpleModel;
	}

	public void setSimpleModel(MapModel simpleModel) {
		this.simpleModel = simpleModel;
	}

	public Overlay getMarkerSelected() {
		return markerSelected;
	}

	public void setMarkerSelected(Overlay markerSelected) {
		this.markerSelected = markerSelected;
	}

	public boolean isMostrarGrade() {
		return mostrarGrade;
	}
	
	public boolean getMostrarGrade(){
		return mostrarGrade;
	}

	public void setMostrarGrade(boolean mostrarGrade) {
		this.mostrarGrade = mostrarGrade;
	}

	public int getTipoOverlay() {
		return tipoOverlay;
	}

	public void setTipoOverlay(int tipoOverlay) {
		this.tipoOverlay = tipoOverlay;
	}
	
	public boolean getIsTipoEstacao(){
		return (tipoOverlay == EnumTipoOverlay.ESTACAO.getId()); 
	}
	
	public boolean getIsTipoPontoGrade(){
		return (tipoOverlay == EnumTipoOverlay.PONTO_GRADE.getId());
	}
	
	public boolean getIsTipoSecao(){
		return (tipoOverlay == EnumTipoOverlay.SECAO.getId());
	}

	public String getSecaoSelected() {
		return secaoSelected;
	}

	public void setSecaoSelected(String secaoSelected) {
		this.secaoSelected = secaoSelected;
	}

	public Usuario getUsuario() {
		return simDto.getUsuario();
	}

	public void setUsuario(Usuario usuario) {
		simDto.setUsuario(usuario);
	}

	public String getRioSecaoSelected() {
		return rioSecaoSelected;
	}

	public void setRioSecaoSelected(String rioSecaoSelected) {
		this.rioSecaoSelected = rioSecaoSelected;
	}

	public String getTrechoSecaoSelected() {
		return trechoSecaoSelected;
	}

	public void setTrechoSecaoSelected(String trechoSecaoSelected) {
		this.trechoSecaoSelected = trechoSecaoSelected;
	}

	public EstadoSimulacao getEstadoSimulacao() {
		if (simDto.getEstadoSimulacao()!=null){
			if (simDto.getEstadoSimulacao().getProgress()<100 && simDto.getEstadoSimulacao().getProgress()!=0){
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(simDto.getEstadoSimulacao().getEstado().getDescripcao()));
			}
		}
		return simDto.getEstadoSimulacao();
	}

	public List<SimHistDto> getSimulacoesHistorial() {
		
		List<SimHistDto> historial = new ArrayList<SimHistDto>();
				
		if (tipoDados == EnumTipoDados.PREVISAO){
			List<Simulacao> sims = simulacaoDao.listByUser(simDto.getUsuario().getId());
			for (Simulacao s: sims){
				SimHistDto simHist = new SimHistDto(s);
				historial.add(simHist);
			}
		}else {
			List<SimulacaoObs> sims = simulacaoObsDao.listByUser(simDto.getUsuario().getId());
			for (SimulacaoObs s: sims){
				SimHistDto simHist = new SimHistDto(s);
				historial.add(simHist);
			}
		}
		return historial;
	}

	public SimHistDto getSimulacaoSelected() {
		return simulacaoSelected;
	}

	public void setSimulacaoSelected(SimHistDto simulacaoSelected) {
		this.simulacaoSelected = simulacaoSelected;
	}

	public List<Bacia> getBacias() {
		return baciaDao.listOperativas();
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public EnumTipoDados[] getTiposDados(){
		return EnumTipoDados.values();
	}
	
	public EnumTipoDados getTipoDados() {
		return tipoDados;
	}

	public void setTipoDados(EnumTipoDados tipoDados) {
		this.tipoDados = tipoDados;
	}

	public SimDto getSimDto() {
		return simDto;
	}

	public void setSimDto(SimDto simDto) {
		this.simDto = simDto;
	}

	public String getLog() {
		
		if (mostrarSecoes){
			
			if(tipoDados == EnumTipoDados.OBSERVACAO){
				SimulacaoObs sim = simulacaoObsDao.findByAttributes(simDto);
				log = sim.getLog();
				if (log!=null){
					log = log.replaceAll("\n", "<br />");
				}
			} else {
				Simulacao sim = simulacaoDao.findByAttributes(simDto);
				log = sim.getLog();
				if (log!=null){
					log = log.replaceAll("\n", "<br />");
				}
			}
			
		} else {
			log="";
		}
		
		return log;
	}

	public void setLog(String log) {
		this.log = log;
	}

	public boolean isMostrarSecoes() {
		return mostrarSecoes;
	}

	public void setMostrarSecoes(boolean mostrarSecoes) {
		this.mostrarSecoes = mostrarSecoes;
	}
	
	
	
}
