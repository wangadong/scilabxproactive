package com.scilab.scheduler;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class CopyOfResolveScilabCode extends JavaExecutable{
    
	private String scilabCode;
	private long userId;
	private String taskName;
    
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
        System.out.println("开始执行sci代码");
        String taskPath="C:\\ScilabCloudV2\\test"+File.separator+userId+File.separator+taskName;
        String savePath=taskPath+File.separator+"res.out";
        File saveFile=new File(savePath);
		try {
			Process process = Runtime.getRuntime().exec(
					"cmd /c java -jar C:/ScilabDistribution/executeV2helper.jar"
							+ "\"" + savePath + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }
		try {
			Process process = Runtime.getRuntime().exec(
					"cmd /c java -jar C:/ScilabDistribution/executeV2.jar"
							+ " \"" + scilabCode + "\" " + "\"" + savePath + "\" "
							+ "\"" + scilabCode + "\">\"" + savePath + "\"");

        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Task terminated");
        ArrayList<File> files=new ArrayList<File>();
        files.add(new File(savePath));
        File png = new File(taskPath+"figure.png");
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
