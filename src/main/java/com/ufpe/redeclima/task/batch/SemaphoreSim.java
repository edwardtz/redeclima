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
package com.ufpe.redeclima.task.batch;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import org.springframework.stereotype.Component;

import com.ufpe.redeclima.interfaces.SimDto;

/**
 * @author edwardtz
 * Controla o acesso à execução do modelo RAS
 */
@Component
public class SemaphoreSim {

	/* Quantidade máxima do recurso compartilhado de acesso exclusivo */
	private static final int MAX_AVAILABLE = 1;
	
	/* Listado de semaforos um por cada par (usuario, bacia) */
	private HashMap<String, Semaphore> semaforos = new HashMap<String, Semaphore>();
	
	public Semaphore getSemaforo(SimDto simDto){
		/* Semaforo configurado com poltica FIFO */
		Semaphore s = semaforos.get(simDto.getUsuario().getId() + "#" + simDto.getBacia().getId());
		if (s == null){
			s = new Semaphore(MAX_AVAILABLE, true);
			semaforos.put(simDto.getUsuario().getId() + "#" + simDto.getBacia().getId(), s);
		}
		return s;
	}
	
}
