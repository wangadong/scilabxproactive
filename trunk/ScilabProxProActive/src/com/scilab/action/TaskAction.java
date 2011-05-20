package com.scilab.action;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Random;
import java.util.Vector;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.examples.masterworker.PIExample;
import org.objectweb.proactive.extensions.masterworker.ProActiveMaster;
import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

import com.scilab.manager.PAMaster;
import com.scilab.manager.PATask;
import com.scilab.manager.ScilabTaskHost;
import com.scilab.manager.ScilabTaskHostService;
import com.scilab.manager.Task;
import com.scilab.pojo.TaskInfo;
import com.scilab.pojo.UserInfo;

/**
 * action 任务提交
 * 
 * @author wangadong
 * @version 1.0
 * @see ScilabTaskHost
 * @see Task
 */
public class TaskAction extends BaseAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7457971171013823063L;
	TaskInfo task;// 任务POJO类
	private String taskname;// 任务名
	private String content;// scilab代码
	private long userId;// 用户ID
	private String resultFolder;// 结果保存路径

	/**
	 * 提交任务到任务管理器
	 * @return String 页面跳转字符
	 */
	@SuppressWarnings("deprecation")
	public String submitTask() {
		//获取用户session
		UserInfo userinfo = (UserInfo) getSession().getAttribute("user");
		if (userinfo == null)
			userinfo = (UserInfo) getSession().getAttribute("usertmp");//若非注册用户则获取游客session
		taskname = task.getTaskName();//从页面获取任务名
		content = task.getTaskContent();//从页面获取scilab代码
		//若为第一次提交，则为其创建游客session
		if (userinfo == null) {
			UserInfo userinfo_tmp = new UserInfo();
			userinfo_tmp.setUserId((long) new Random().nextInt(10000) + 10000);//创建用户ID大于10000的session以区别注册用户
			System.out.println(userinfo_tmp.getUserId());
			userinfo_tmp.setUserName("usertmp");
			getSession().setAttribute("usertmp", userinfo_tmp);
			userId = userinfo_tmp.getUserId();

		} else {
			userId = userinfo.getUserId();
		}
		System.out.println(System.getProperty("user.dir"));
		/*
		 * 从这里开始加入了ProActive MW的东西
		 */
		
		
		
		
		

		TaskFlowJob job = new TaskFlowJob();
		String i="One";
		job.setName(userId+taskname+"job");
        job.setPriority(org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setLogFile("~/test/log/file" + i + ".log");
        job.setDescription("Job description" + i);
        job.addGenericInformation("test", "" + i);
        job.addGenericInformation("job", "" + i);
        JobEnvironment je = new JobEnvironment();
        try {
        	je.setJobClasspath(new String[] {
        			"..\\webapps\\ScilabProxProActive\\WEB-INF\\classes" });
        	} catch (IOException e1) {
        		// TODO Auto-generated catch block
        		e1.printStackTrace();
        }
        job.setEnvironment(je);
		JavaTask task=new JavaTask();
		task.setExecutableClassName("com.scilab.scheduler.ResolveScilabCode");
		System.out.println(task.getExecutableClassName());
		task.setName(userId+taskname);
		task.addArgument("scilabCode",content);
		
		try {
			job.addTask(task);
		} catch (UserException e) {
			e.printStackTrace();
		}
		//join an existing ProActive Scheduler retrieving an authentication interface.
		try {
		    SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin("rmi://localhost:1099");
		    //connect and log to the Scheduler. Valid username and password are defined by the administrator
		    Scheduler scheduler = null;
		    
		    try {
		        // (1) preferred authentication method
		        scheduler = auth.login(Credentials.getCredentials());
		    } catch (KeyException ke) {
		        try {
		            // (2) alternative authentication method
		            PublicKey pubKey = auth.getPublicKey();
		            if (pubKey == null) {
		                 pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
		            }
		            scheduler = auth.login(Credentials.createCredentials(new CredData("user", "pwd"), pubKey));
		        } catch (KeyException ke2) {
		            //cannot find public key !
		        }
		    }
		    // submitting a new job and get the associated id
		    JobId myJobId = scheduler.submit(job);
		    JobResult result;
		    do{
		    	result=scheduler.getJobResult(myJobId);
		    }while(result==null);
		    System.out.print(result.getAllResults()+myJobId.value());
		} catch (Exception e) {
		    //cannot join scheduler !
		    e.printStackTrace();
		}
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//获取任务结果保存的绝对路径
		resultFolder = getRequest().getRealPath("/") + "ScilabResult"
				+ File.separatorChar + userId + File.separatorChar + taskname
				+ File.separatorChar + userId + taskname + ".txt";
//		System.out.println(resultFolder);
		boolean bool = true;//PAMaster.getMaster();//.(taskname, content,
		//		userId, resultFolder);
		// setMessage("任务名称：" + taskname + "  " + "scilab代码：" + content);
		//创建页面新增任务列表代码
		setMessage("Task Name：" + taskname
				+ "<a href='./CheckTask!getResult?taskname=" + taskname
				+ "'>Check the Result</a>     <a href='./CheckTask!saveTask?taskname="
				+ taskname + "'>Save my Task</a>");
		if (bool)
			return SUCCESS;//提交成功则跳转成功页面
		else
			return ERROR;//提交错误则跳转错误页面
	}

	private String message;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTaskname() {
		return taskname;
	}

	public void setTaskname(String taskname) {
		this.taskname = taskname;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public TaskInfo getTask() {
		return task;
	}

	public void setTask(TaskInfo task) {
		this.task = task;
	}

	public String getResultFolder() {
		return resultFolder;
	}

	public void setResultFolder(String resultFolder) {
		this.resultFolder = resultFolder;
	}

}