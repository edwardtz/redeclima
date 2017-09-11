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
package com.ufpe.redeclima.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;

/**
 * @author edwardtz
 * Compacta e descompacta arquivos de upload ao sistema
 */
@Component
public class Unziper {
	
	
	/**
     * Unzip it
     * @param zipFile arquivo zip de entrada
     * @param outputFolder pasta onde se descomprimira o arquivo
     */
    public void unZipIt(String zipFile, String outputFolder){
 
    	byte[] buffer = new byte[1024];
 
    	try{
 
    		// Cria o diretorio se não existir
    		File folder = new File(outputFolder);
    		if(!folder.exists()){
    			folder.mkdir();
    		}
 
    		// Obter o conteudo do zip
    		ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
    		
    		// Obter a lista de arquivos so zip
    		ZipEntry ze = zis.getNextEntry();
    		

    		while(ze!=null){
				 
    			String fileName = ze.getName();
    			
    			if(ze.isDirectory()){
    				File newFile = new File(outputFolder + File.separator + fileName);
    				newFile.mkdir();
    			}else{
    				
    				File newFile = new File(outputFolder + File.separator + fileName);
        			
        			System.out.println("file unzip : "+ newFile.getAbsoluteFile());
        			
        			//Cria todas as pastas que não existam
        			//ou se não se obtera FileNotFoundException para as pastas comprimidas
        			new File(newFile.getParent()).mkdirs();
        			
        			FileOutputStream fos = new FileOutputStream(newFile);             
     
        			int len;
        			
        			while ((len = zis.read(buffer)) > 0) {
        				fos.write(buffer, 0, len);
        			}
     
        			fos.close();
        			
    			}
    			
    			ze = zis.getNextEntry();
    			   
    		}
    		
    		zis.closeEntry();
    		zis.close();
 
    		System.out.println("Done");
 
    	}catch(IOException ex){
    		ex.printStackTrace(); 
    	}
   }
	

}
