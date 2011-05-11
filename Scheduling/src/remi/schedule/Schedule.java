package remi.schedule;

import java.io.IOException;
import java.security.KeyException;
import java.security.PublicKey;

import org.ow2.proactive.authentication.crypto.CredData;
import org.ow2.proactive.authentication.crypto.Credentials;
import org.ow2.proactive.scheduler.common.Scheduler;
import org.ow2.proactive.scheduler.common.SchedulerAuthenticationInterface;
import org.ow2.proactive.scheduler.common.SchedulerConnection;
import org.ow2.proactive.scheduler.common.exception.UserException;
import org.ow2.proactive.scheduler.common.job.JobEnvironment;
import org.ow2.proactive.scheduler.common.job.JobId;
import org.ow2.proactive.scheduler.common.job.TaskFlowJob;
import org.ow2.proactive.scheduler.common.task.JavaTask;

public class Schedule {
	public static void main(String arg[]){
		TaskFlowJob job = new TaskFlowJob();
		String i="One";
		job.setName("myTestOne");
        job.setPriority(org.ow2.proactive.scheduler.common.job.JobPriority.NORMAL);
        job.setCancelJobOnError(false);
        job.setLogFile("~/test/log/file" + i + ".log");
        job.setDescription("Job description" + i);
        job.addGenericInformation("test", "" + i);
        job.addGenericInformation("job", "" + i);
        JobEnvironment je = new JobEnvironment();
        try {
        	je.setJobClasspath(new String[] {
        			"/auto/sop-nas2a/u/sop-nas2a/vol/home_oasis/xinwang/workspace/ProactiveTest/bin"
        			,"" });
        	} catch (IOException e1) {
        		// TODO Auto-generated catch block
        		e1.printStackTrace();
        }
        job.setEnvironment(je);
		JavaTask taskOne=new JavaTask();
		taskOne.setExecutableClassName("org.ow2.proactive.scheduler.examples.WaitAndPrint");
		System.out.println(taskOne.getExecutableClassName());
		taskOne.setName("task one");
		
		try {
			job.addTask(taskOne);
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
		} catch (Exception e) {
		    //cannot join scheduler !
		    e.printStackTrace();
		}
	}
}
