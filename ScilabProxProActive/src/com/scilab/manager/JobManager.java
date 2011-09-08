package com.scilab.manager;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UnknownJobException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.JobResult;
import org.ow2.proactive.scheduler.common.job.JobState;
import org.ow2.proactive.scheduler.common.job.JobStatus;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

public class JobManager {
	private static JobManager _INSTANCE=new JobManager();
	private Scheduler scheduler;
	private Map<String,JobId> idMap;
	/*
	 * 初始化
	 */
	private JobManager(){
		idMap = new HashMap<String,JobId>();
	}
	public void initConn(){
		System.out.println("[ScilabCloud] Start connecting to scheduler");
		try {
		    SchedulerAuthenticationInterface auth = SchedulerConnection.waitAndJoin("rmi://localhost:1099");
		    try {
		        scheduler = auth.login(Credentials.getCredentials());
		    } catch (KeyException ke) {
		        try {
		            PublicKey pubKey = auth.getPublicKey();
		            if (pubKey == null) {
		                 pubKey = Credentials.getPublicKey(Credentials.getPubKeyPath());
		            }
		            scheduler = auth.login(Credentials.createCredentials(new CredData("user", "pwd"), pubKey));
		        } catch (KeyException ke2) {
		            System.out.println("[ScilabCloud] can not get public key");
		            ke2.printStackTrace();
		        }
		    }
		} catch (Exception e) {
			System.out.println("[ScilabCloud] scheduler init error");
		    e.printStackTrace();
		}
		System.out.println("[ScilabCloud] Connect success");
	}
	/*
	 * 提交任务
	 */
	public JobId submit(String realpath, String taskname, String content, long userId, String resultFolder){
		System.out.println("[ScilabCloud] Testing the connect with scheduler");
		if((scheduler == null) || (!scheduler.isConnected())){
			initConn();
		}
		System.out.println("[ScilabCloud] start wrapping task");
		Date submitDate = new Date();
		String id=userId+taskname;
		String name="user id"+userId+"task name"+taskname+"time"+submitDate.getTime();
		String description =" of task \"" + taskname + "\"" +
							" of user with id "+ userId +
							" submitted at " +new Date();
		TaskFlowJob job=wrapJob(realpath,name,"This is the job"+description);
		JavaTask task=new JavaTask();
		task.setExecutableClassName("com.scilab.execution.ResolveScilabCode");
		task.setName(name);
		
		task.addArgument("scilabCode",content);
		task.addArgument("userId", String.valueOf(userId));
		task.addArgument("taskName", taskname);
		try {
			job.addTask(task);
		} catch (UserException e) {
			e.printStackTrace();
		}
		System.out.println("[ScilabCloud] start submit task to scheduler");
		JobId jid = submitJob(job);
		idMap.put(id , jid);
		System.out.println("[ScilabCloud] id map updated");
		return jid;
	}
	public JobId submitJob(Job job){
	    JobId id=null;
		try {
			id=scheduler.submit(job);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (SubmissionClosedException e) {
			e.printStackTrace();
		} catch (JobCreationException e) {
			e.printStackTrace();
		}
		System.out.println("[ScilabCloud] Submit finish, return job id");
		return id;
	}
	public JobResult getResult(JobId id){
		System.out.println("[ScilabCloud] Testing the connect with scheduler");
		if((scheduler == null) || (!scheduler.isConnected())){
			initConn();
		}
		System.out.println("[ScilabCloud] Getting the result");
		JobResult result=null;
		try {
			result=scheduler.getJobResult(id);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		}
		return result;
	}
	public JobState getStatus(JobId id,String taskId){
		System.out.println("[ScilabCloud] Testing the connect with scheduler");
		if((scheduler == null) || (!scheduler.isConnected())){
			initConn();
		}
		System.out.println("[ScilabCloud] getting state of task");
		JobState js=null;
		try {
			js = JobManager.getInstance().getScheduler().getJobState(
							JobManager.getInstance().getIdMap().get(taskId)
							);
			System.out.println(js);
		} catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (UnknownJobException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		}// 获取任务状态
		return js;
	}
	public TaskFlowJob wrapJob(String realpath, String jobName, String jobDescription){
		TaskFlowJob job = new TaskFlowJob();
		System.out.println("[ScilabCloud] Start wrapping job");
		job.setName(jobName);
        job.setPriority(JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setLogFile("~/test/log/file/" + jobName + ".log");
        job.setDescription(jobDescription);
        JobEnvironment je = new JobEnvironment();
        try {
        	je.setJobClasspath(new String[] {
        			"",
        			realpath +File.separatorChar + "WEB-INF" + File.separatorChar + "classes" });
        } catch (IOException e1) {
        		e1.printStackTrace();
        }
        job.setEnvironment(je);
        System.out.println("[ScilabCloud] Job wrapping finished");
		return job;
	}
	public synchronized static JobManager getInstance(){
			return _INSTANCE;
	}
	public Scheduler getScheduler(){
		return scheduler;
	}
	public Map<String,JobId> getIdMap(){
		return this.idMap;
	}
}
