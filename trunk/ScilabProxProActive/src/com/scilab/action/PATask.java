package com.scilab.action;

import org.objectweb.proactive.extensions.masterworker.interfaces.Task;
import org.objectweb.proactive.extensions.masterworker.interfaces.WorkerMemory;

public class PATask implements Task<String>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public PATask(){	
	}
	
	public PATask(String content){
		this.content=content;
	}
	public String run(WorkerMemory memory) throws Exception{
		return "this is a test for task";
	}
	
	String content;
}
