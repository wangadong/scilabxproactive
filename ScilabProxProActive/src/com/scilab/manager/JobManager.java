package com.scilab.manager;

import java.io.File;
import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;
import java.util.Date;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.JobCreationException;
import org.ow2.proactive.scheduler.common.exception.NotConnectedException;
import org.ow2.proactive.scheduler.common.exception.PermissionException;
import org.ow2.proactive.scheduler.common.exception.SubmissionClosedException;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.Job;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.JobPriority;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

public class JobManager {
	private static JobManager _INSTANCE=new JobManager();
	private Scheduler scheduler;
	private JobManager(){
		System.out.println("开始注册scheduler");
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
		            //cannot find public key !
		        }
		    }
		} catch (Exception e) {
			System.out.println("scheduler初始化异常");
		    e.printStackTrace();
		}
		System.out.println("注册成功");
	}
	public JobId submit(String realpath, String taskname, String content, long userId, String resultFolder){
		System.out.println("开始封装任务");
		Date submitDate = new Date();
		String name="user id"+userId+"task name"+taskname+"time"+submitDate.getTime();
		String description =" of task \"" + taskname + "\"" +
							" of user with id "+ userId +
							" submitted at " +new Date();
		TaskFlowJob job=wrapJob(realpath,name+"job","This is the job"+description);
		JavaTask task=new JavaTask();
		task.setExecutableClassName("com.scilab.scheduler.ResolveScilabCode");
		task.setName(name + "task");
		task.addArgument("scilabCode",content);
		try {
			job.addTask(task);
		} catch (UserException e) {
			e.printStackTrace();
		}
		System.out.println("开始提交任务");
		return submitJob(job);
	}
	public JobId submitJob(Job job){
	    JobId jobId=null;
		try {
			jobId = scheduler.submit(job);
		}  catch (Exception e) {
			e.printStackTrace();
		}
		return jobId;
	}
	public TaskFlowJob wrapJob(String realpath, String jobName, String jobDescription){
		TaskFlowJob job = new TaskFlowJob();
		System.out.println("开始封装任务job");
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
        System.out.println("任务job封装完毕");
		return job;
	}
	public synchronized static JobManager getInstance(){
			return _INSTANCE;
	}
	public Scheduler getScheduler(){
		return scheduler;
	}
}