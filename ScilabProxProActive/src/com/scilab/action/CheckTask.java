package com.scilab.action;

import java.io.*;
import java.util.*;

import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UnknownTaskException;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.task.TaskResult;

import com.scilab.dao.impl.TaskDao;
import com.scilab.manager.JobManager;

import com.scilab.pojo.TaskInfo;
import com.scilab.pojo.UserInfo;

/**
 * action 获取结果，查询状态，保存任务
 * 
 * @author remi liu , wangadong
 * @version 2.0
 * 
 */
public class CheckTask extends BaseAction {
	private String taskname;// 任务名，与页面对应
	private String resultFolder;// 任务结果保存路径
	private String taskStatue;// 任务状态
	private UserInfo userinfo;// 用户信息
	private TaskDao dao = new TaskDao();// 任务信息与数据库接口
	private Long userId;// 用户ID
	private String resultContent;// scilab代码，与页面代码对应
	private File file;
	private String imgPath;// 结果图片保存相对地址
	private JobResult result;
	private TaskResult tresult;
	/**
	 * 获取结果<br>
	 * 从结果文档中读取结果并存为字符串在页面显示
	 * 
	 * 
	 * @return String 页面跳转字段
	 */
	@SuppressWarnings("deprecation")
	public String getResult() {
		userId=calcUserId();
		imgPath = null;
		System.out.println(userId + taskname);
		JobManager jm=JobManager.getInstance();
		if(jm.getIdMap().containsKey(userId+taskname)){
			
			try {
				result = jm.getScheduler().getJobResult(
						jm.getIdMap().get(userId + taskname)
						);
			} catch (NotConnectedException e){
				e.printStackTrace();
			} catch (PermissionException e) {
				e.printStackTrace();
			} catch (UnknownJobException e) {
				e.printStackTrace();
			}
		}
		else
			return "resultFail";
		try {
			if(result==null)
				return
					"resultFail";
			tresult=result.getResult(result.getName());
		} catch (UnknownTaskException e1) {
			e1.printStackTrace();
		}
		ArrayList<Object> list=null;
		try {
			list = (ArrayList<Object>)tresult.value();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String resultcode = (String) list.get(0);
		if(list.size()==2){
		    System.out.println("return the png");
		    imgPath=getRequest().getRealPath("/") + "ScilabResult/" + userId
			+ "/" + taskname + "/" + "figure0.png";
			File myFilePath = new File(imgPath).getParentFile();
			if (!myFilePath.exists()) {
				if (myFilePath.mkdirs())
					System.out.println("create folder" + myFilePath);
			} else {
				if (deleteFile(myFilePath))
					System.out.println("delete folder");
				if (!myFilePath.exists())
					System.out.println("update success");
				myFilePath.mkdirs();
			}
		    byte[] png=(byte[])list.get(1);
		    File f=new File(imgPath);
		   	try {
				f.createNewFile();
			   	BufferedOutputStream output = new BufferedOutputStream(
			   			new FileOutputStream(f));
			   	output.write(png);
		    	output.flush();
		    	output.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			imgPath = "ScilabResult/" + userId + "/" + taskname + "/"
			+ "figure0.png";
		}
		else
			imgPath = null;
		// 从结果文档中读取字符流并保存为字符串格式
		if (resultcode != null) {
			resultContent = resultcode;
			System.out.println(resultcode);
			
			return "resultSucc";
		}
		return "resultFail";
	}

	/**
	 * 获取当前任务状态
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getStatue() throws IOException {
		// 获取任务查询ID
		userId = calcUserId();
		System.out.println(userId + taskname);
		getResponse().setContentType("text/html; charset=utf-8");
		getResponse().setHeader("Cache-Control", "no-cache"); // 不定义缓存
		getResponse().setCharacterEncoding("utf-8");
		PrintWriter out = getResponse().getWriter();
		JobState js= JobManager.getInstance().getStatus(
					JobManager.getInstance().getIdMap().get(userId + taskname),
					userId + taskname
				);
		js.getTasks();
		taskStatue = js.getStatus().toString();
		out.write(taskStatue);
		out.close();// 输出状态内容
		System.out.println(taskStatue);
		return null;
	}
	
	/**
	 * 保存任务到数据库
	 * 
	 * @return null 调用ajax，不进行页面跳转
	 * @throws IOException
	 */
	public String saveTask() throws IOException {
		userId = calcUserId();
		String saveStatue;
		JobManager jm=JobManager.getInstance();
		//userID<10000为注册用户
		if (userId < 10000) {
			System.out.println(userId + taskname);
			saveStatue = "Unable to save !";
			if (jm.getIdMap().containsKey(userId + taskname)){
				TaskInfo ti=dao.isExist(taskname, userId);
				if(ti == null){
					ti =new TaskInfo();
				}
				ti.setTaskName(taskname);
				ti.setUserId(userId);
				ti.setSaveTime(new Date());
				try {
					ti.setTaskStatue(jm.getScheduler().getJobState(
							jm.getIdMap().get(userId + taskname)
							).toString());
				} catch (NotConnectedException e) {
					e.printStackTrace();
				} catch (UnknownJobException e) {
					e.printStackTrace();
				} catch (PermissionException e) {
					e.printStackTrace();
				}
				TaskDao dao = new TaskDao();//建立数据库连接
				//保存任务到数据库
				if (dao.saveOrUpdateTask(ti)) {
					saveStatue = "Task saved successfully !";
				}
			}
		} else {
			saveStatue = "Ooops! You can't save the task as a visitor!";//若用户ID>10000,则为游客访问，不允许保存任务
		}
		//Ajax
		getResponse().setContentType("text/html; charset=utf-8");
		getResponse().setHeader("Cache-Control", "no-cache"); // 不定义缓存
		getResponse().setCharacterEncoding("utf-8");
		PrintWriter out = getResponse().getWriter();
		out.write(saveStatue);
		out.close();
		return null;//Ajax输出，不进行页面跳转
	}
	
	public long calcUserId(){
		// 获取当前用户session，并得到用户ID
		userinfo = (UserInfo) getSession().getAttribute("user");
		if (userinfo == null) {
			userinfo = (UserInfo) getSession().getAttribute("usertmp");
		}
		return userinfo.getUserId();
	}
	public String getTaskStatue() {
		return taskStatue;
	}

	public void setTaskStatue(String taskStatue) {
		this.taskStatue = taskStatue;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public String getResultContent() {
		return resultContent;
	}

	public void setResultContent(String resultContent) {
		this.resultContent = resultContent;
	}

	public String getTaskname() {
		return taskname;
	}

	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}

	public String getResultFolder() {
		return resultFolder;
	}

	public void setResultFolder(String resultFolder) {
		this.resultFolder = resultFolder;
	}
	
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
