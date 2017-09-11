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

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ufpe.redeclima.model.Bacia;
import com.ufpe.redeclima.model.Estacao;
import com.ufpe.redeclima.model.Mensagem;
import com.ufpe.redeclima.util.EnumTipoMensagem;

/**
 * @author edwardtz
 *
 */

@Component
public class MensagemDao {
	
	@PersistenceContext
	private EntityManager entityManager;
	
	@Transactional
	public void save(Mensagem mensagem) {
		entityManager.persist(mensagem);
	}
	
	@Transactional
	public void saveOrUpdate(Mensagem mensagem) {
		entityManager.merge(mensagem);
	}
	
	@Transactional
	public void remove(Mensagem mensagem) {
		Mensagem mensagemDelete = findById(mensagem.getId());
		entityManager.remove(mensagemDelete);
	}
	
	public Mensagem findById(Long id){
		return entityManager.find(Mensagem.class, id);
	}
	
	@SuppressWarnings("unchecked")
	public boolean alertaEnviada(Estacao estacao, EnumTipoMensagem tipo, Date dataInicial, Date dataFinal) {
		List<Mensagem> ms = entityManager.createQuery("from Mensagem m where m.estacao.id =:estacaoId and m.dataEnvio>=:dataInicial and m.dataEnvio<=:dataFinal and m.tipo=:codigoTipo " )
				.setParameter("estacaoId", estacao.getId())
				.setParameter("dataInicial", dataInicial)
				.setParameter("dataFinal", dataFinal)
				.setParameter("codigoTipo", tipo)
				.getResultList();
		
		if (ms!=null && !ms.isEmpty()){
			return true;
		}else {
			return false;
		}
	}

}
