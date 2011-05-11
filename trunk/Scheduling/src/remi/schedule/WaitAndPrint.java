package remi.schedule;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class WaitAndPrint extends JavaExecutable {
	
	@Override
    public Serializable execute(TaskResult... results) throws Throwable {
        String message;

        try {
            System.err.println("Démarrage de la tache WaitAndPrint");
            System.out.println("Parameters are : ");

            for (TaskResult tRes : results) {
                if (tRes.hadException()) {
                    System.out.println("\t " + tRes.getTaskId() + " : " + tRes.getException().getMessage());
                } else {
                    System.out.println("\t " + tRes.getTaskId() + ": " + tRes.value());
                }
            }

            message = "success";
            Thread.sleep(10000);

        } catch (Exception e) {
            message = "crashed";
            e.printStackTrace();
        }

        System.out.println("Task terminated");

        return (message + "\t slept for 10 sec");
    }
}
