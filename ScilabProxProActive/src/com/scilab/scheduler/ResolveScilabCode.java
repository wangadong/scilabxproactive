package com.scilab.scheduler;

import java.io.Serializable;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class ResolveScilabCode extends JavaExecutable{
    
	private String scilabCode;
    
	@Override
	public void init(Map<String,Serializable> args){
    	scilabCode=(String) args.get("scilabCode");
    }
	
	@Override
	public Serializable execute(TaskResult... results) throws Throwable {

        System.out.println("Task terminated");

        return (scilabCode);
	}

}
