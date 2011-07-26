package com.scilab.manager;

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
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

public class JobManager {
	private static final JobManager _INSTANCE=new JobManager();
	private Scheduler scheduler;
	private JobManager(){
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
		    e.printStackTrace();
		}
	}
	public JobId submit(String taskname, String content, long userId, String resultFolder){
		System.out.print("任务开始提交");
		TaskFlowJob job = new TaskFlowJob();
		String i="One";
		job.setName(userId+taskname+new Date()+"job");
        job.setPriority(org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setLogFile("~/test/log/file" + i + ".log");
        job.setDescription("Job description" + i);
        job.addGenericInformation("test", "" + i);
        job.addGenericInformation("job", "" + i);
        JobEnvironment je = new JobEnvironment();
        try {
        	je.setJobClasspath(new String[] {
        			"","C:/ScilabDistribution/tomcat6.0/webapps/ScilabProxProActive/WEB-INF/classes/" });
        	} catch (IOException e1) {
        		e1.printStackTrace();
        }
        job.setEnvironment(je);
		JavaTask task=new JavaTask();
		task.setExecutableClassName("com.scilab.scheduler.ResolveScilabCode");
		//System.out.println(task.getExecutableClassName());
		task.setName(userId+taskname+new Date()+"task");
		task.addArgument("scilabCode",content);
		try {
			job.addTask(task);
		} catch (UserException e) {
			e.printStackTrace();
		}
		return submitJob(job);
	}
	public JobId submitJob(Job job){
	    JobId jobId=null;
		try {
			jobId = scheduler.submit(job);
			/*留个白，不知是不是要在这获得结果
			JobResult result;
		    do{
				result=scheduler.getJobResult(jobId);
		   }while(result==null);
		   */
		}
		/*
		 * 你说我加这么多catch语句有用么？还是加一个就行啊
		*/
		  catch (NotConnectedException e) {
			e.printStackTrace();
		} catch (PermissionException e) {
			e.printStackTrace();
		} catch (SubmissionClosedException e) {
			e.printStackTrace();
		} catch (JobCreationException e) {
			e.printStackTrace();
		}// catch (UnknownJobException e) {
		//	e.printStackTrace();
		//}
		return jobId;
	}
	public synchronized static JobManager getInstance(){
			return _INSTANCE;
	}
	public Scheduler getScheduler(){
		return scheduler;
	}
}
