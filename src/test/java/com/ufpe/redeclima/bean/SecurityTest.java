/**
 * Pro
 */
package com.ufpe.redeclima.bean;

import java.text.ParseException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ufpe.redeclima.task.TaskManager;

/**
 * @author edwardtz
 *
 */
@ContextConfiguration("/test-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class SecurityTest {
	
	@Autowired
	private TaskManager taskManager;
	
	@Test
	public void doTest(){
		
	}
	
}
