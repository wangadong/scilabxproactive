package org.ow2.proactive.scheduler.examples;

import java.io.Serializable;

import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;

public class TestTaskOne extends JavaExecutable {

	@Override
	public Serializable execute(TaskResult... results) throws Throwable {
		System.out.println("test 1");
		return "Test";
	}
	
}
