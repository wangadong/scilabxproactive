package com.scilab.scheduler;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class ResolveScilabCode extends JavaExecutable{
    
	private String scilabCode;
    
	@Override
	public void init(Map<String,Serializable> args){
    	try {
			super.init(args);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	@Override
	public Serializable execute(TaskResult... results) throws Throwable {
        try {
            System.err.println("开始执行sci代码");
            System.out.println("Parameters are : ");

            for (TaskResult tRes : results) {
                if (tRes.hadException()) {
                    System.out.println("\t " + tRes.getTaskId() + " : " + tRes.getException().getMessage());
                } else {
                    System.out.println("\t " + tRes.getTaskId() + ": " + tRes.value());
                }
            }
            String savePath="E:\\res.out";
			Process process = Runtime.getRuntime().exec(
					"cmd /c java -jar C:/ScilabDistribution/executeV2.jar"
							+ " \"" + scilabCode + "\" " + "\"" + savePath + "\" "
							+ "\"" + scilabCode + "\">\"" + savePath + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Task terminated");
        ArrayList<File> files=new ArrayList<File>();
        files.add(new File("E:/res.out"));
        File png = new File("E:/figure.png");
        if(png.exists())
        	files.add(png);
        return (files);
	}
	
	public String getScilabCode(){
		return scilabCode;
	}
	public void setScilabCode(String scilabCode){
		this.scilabCode=scilabCode;
	}
}
