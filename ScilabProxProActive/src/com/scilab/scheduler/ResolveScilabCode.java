package com.scilab.scheduler;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class ResolveScilabCode extends JavaExecutable{
    
	private String scilabCode;
	private String userId;
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
        String taskPath="C:\\ScilabCloudV2\\temp"+
        					File.separator+userId+File.separator+taskName;
        String savePath=taskPath+File.separator+"res.out";
		File myFilePath = new File(savePath).getParentFile();
		if (!myFilePath.exists()) {
			if (myFilePath.mkdirs())
				System.out.println("创建文件目录" + myFilePath);
		} else {
			if (deleteFile(myFilePath))
				System.out.println("删除目录");
			if (!myFilePath.exists())
				System.out.println("更新成功");
			myFilePath.mkdirs();
		}
		try {
			Process process = Runtime.getRuntime().exec(
					"cmd /c java -jar C:/ScilabCloudV2/executeV2.jar"
							+ " \"" + scilabCode + "\" " + "\"" + savePath + "\" "
							+ "\"" + scilabCode + "\">\"" + savePath + "\"");
			final InputStream is=process.getInputStream();
			final InputStream es=process.getErrorStream();
			new WatchThread(process,is).start();
			new WatchThread(process,es).start();
			
			process.waitFor();
			
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f=new File(savePath);
        while(!f.exists());
        BufferedReader br = new BufferedReader(new FileReader(f));
        String str="";
        String temp;
        while((temp = br.readLine() ) != null)
        {
        	str+=temp+"<br/>";
        }	 
        ArrayList<Object> allResults=new ArrayList<Object>();
        allResults.add(str);
        File png = new File(taskPath+"figure.png");
        if(png.exists())
        	allResults.add(ImageIO.read(png));
        return (allResults);
	}
	
	public String getScilabCode(){
		return scilabCode;
	}
	public void setScilabCode(String scilabCode){
		this.scilabCode=scilabCode;
	}
	// 删除文件及文件夹
	public boolean deleteFile(File f) {
		if (f.exists()) {
			if (f.isFile())
				return f.delete();
			else if (f.isDirectory()) {
				File[] files = f.listFiles();
				for (int i = 0; i < files.length; i++) {
					if (!deleteFile(files[i]))
						return false;
				}
				return f.delete();
			} else
				return false;
		} else
			return true;
	}
}
class WatchThread extends Thread { 
	Process p; 
	InputStream is;
	boolean over; 
	public WatchThread(Process p,InputStream is) { 
		this.p = p; 
		this.is=is;
		over = false; 
	} 

	public void run() { 
		try { 
			if (p == null) return; 
			BufferedReader br =new BufferedReader(
					new InputStreamReader(is)
				); 
			while (true) { 
				if (p==null || over) { 
					break; 
				} 
				while(br.readLine()!=null); 
			} 
		} catch (Exception e) { 
			e.printStackTrace(); 
		} 
	} 
	
	public void setOver(boolean over) { 
		this.over = over; 
	} 
} 