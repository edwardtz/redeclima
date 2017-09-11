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
package com.ufpe.redeclima.task;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ufpe.redeclima.dao.BaciaDao;
import com.ufpe.redeclima.dao.GradeDao;
import com.ufpe.redeclima.dao.PerfilDao;
import com.ufpe.redeclima.dao.RioDao;
import com.ufpe.redeclima.dao.SecaoDao;
import com.ufpe.redeclima.dao.TrechoDao;
import com.ufpe.redeclima.dao.UsuarioDao;
import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Perfil;
import com.ufpe.redeclima.model.Usuario;
import com.ufpe.redeclima.util.EnumPerfil;

/**
 *  Esta classe executa a tarefa de inicializar os dados da bd, alguns dados basicos, para iniciar o funcionamento quando a BD esteja em branco
 * */

@Component
public class InicializacaoDadosTask implements Runnable {

	@Autowired
	private GradeDao gradeDao;
	
	@Autowired
	private BaciaDao baciaDao;
	
	@Autowired
	private PerfilDao perfilDao;
	
	@Autowired
	private UsuarioDao usuarioDao;
	
	@Autowired
	private RioDao rioDao;
	
	@Autowired
	private TrechoDao trechoDao;
	
	@Autowired
	private SecaoDao secaoDao;
	
	private void InicializarPerfil(){
		Perfil perfil;
		for(EnumPerfil perfilEnum: EnumPerfil.values()){
			perfil = perfilDao.findByNome(perfilEnum.getCodigo());
			if (perfil==null){
				perfil = new Perfil();
				perfil.setId((long)perfilEnum.getId());
				perfil.setNome(perfilEnum.getCodigo());
				perfil.setDescricao(perfilEnum.getDescripcao());
				perfilDao.saveOrUpdate(perfil);
			}
		}
	}
	
	private void InicializarUsuarios(){
		Usuario usuario = usuarioDao.findByLogin("ADMIN");
		Perfil admin = perfilDao.findByNome("ADMINISTRADOR");
		if(usuario==null){
			usuario = new Usuario();
			usuario.setAtivo(true);
			usuario.setEmail("edwardtz@gmail.com");
			usuario.setLogin("ADMIN");
			usuario.setPassword("admin");
			usuario.setPerfil(admin);
			usuario.setNome("Administrador");
			usuario.setSobreNome("Administrador");
			usuario.setTelefone("00000000");
			usuarioDao.save(usuario);
		}
	}
	
	private void InicializarBacia(){
		Bacia bacia = baciaDao.findByNome("Una");
		if (bacia==null){
			bacia = new Bacia();
			bacia.setNome("Una");
			bacia.setLatitudeY1(-8.29);
			bacia.setLatitudeY2(-9.02);
			bacia.setLongitudeX1(-36.7);
			bacia.setLongitudeX2(-35.19);
			baciaDao.save(bacia);
		}
		
		bacia = baciaDao.findByNome("Mundau");
		if(bacia==null){
			bacia = new Bacia();
			bacia.setNome("Mundau");
			bacia.setLatitudeY1(-8D);
			bacia.setLatitudeY2(-10D);
			bacia.setLongitudeX1(-39D);
			bacia.setLongitudeX2(-35D);
			baciaDao.save(bacia);
		}
	}
	
	public void run() {

		/* Inicializar lista de perfiles de usuarios */
		InicializarPerfil();
		
		/* Inicializar usuario administrador do sistema */
		InicializarUsuarios();
		
		/* Inicializa uma bacia */
		InicializarBacia();
		
	}

	public SecaoDao getSecaoDao() {
		return secaoDao;
	}

	public void setSecaoDao(SecaoDao secaoDao) {
		this.secaoDao = secaoDao;
	}
	
	

}
