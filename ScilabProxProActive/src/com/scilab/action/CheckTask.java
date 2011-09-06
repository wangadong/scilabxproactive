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
import com.scilab.manager.Task;
import com.scilab.pojo.TaskInfo;
import com.scilab.pojo.UserInfo;

/**
 * action 获取结果，查询状态，保存任务
 * 
 * @author wangadong
 * @version 1.0
 * @see ScilabTaskHost
 * 
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
	private String nodeName;
	private String nodeIP;
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
			tresult=result.getResult(result.getName());
		} catch (UnknownTaskException e1) {
			e1.printStackTrace();
		}
		ArrayList<File> list=null;
		try {
			list = (ArrayList<File>)tresult.value();
		} catch (Throwable e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		File resultcode = list.get(0);
		File png=null;
		if(list.size()==2){
			png = list.get(1);
			imgPath=png.getPath();
		}
		else
			imgPath = null;
		// 从结果文档中读取字符流并保存为字符串格式
		if (resultcode != null) {
			try {
				InputStreamReader isr = new InputStreamReader(
						new FileInputStream(resultcode), "UTF-8");
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				StringBuffer result = new StringBuffer();
				while ((line = br.readLine()) != null) {
					result.append(line);
					result.append("<br>");// 添加页面换行符
				}
				resultContent = result.toString();
				System.out.println(resultContent);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				return "resultFail";//结果文件不存在，跳转查询失败页面
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		// Ajax
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
		//获取任务查询ID
		userId = calcUserId();
		String saveStatue;
		JobManager jm=JobManager.getInstance();
		//userID<10000为注册用户
		if (userId < 10000) {
			System.out.println(userId + taskname);
			saveStatue = "Unable to save !";
			// 判断该用户的该任务名是否存在
			if (jm.getIdMap().containsKey(userId + taskname)){
				TaskInfo ti=dao.isExist(taskname, userId);
				if(ti == null){
					ti =new TaskInfo();
				}
				ti.setTaskName(taskname);
				ti.setUserId(userId);
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

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		nodeName = nodeName;
	}

	public void setNodeIP(String nodeIP) {
		this.nodeIP = nodeIP;
	}

	public String getNodeIP() {
		return nodeIP;
	}
}
