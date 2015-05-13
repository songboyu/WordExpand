package com.task;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import com.task.service.TaskService;

public class App  extends Application{

	@Override
	public Set<Class<?>> getClasses() {
		Set<Class<?>> sets=new HashSet<Class<?>>();
		sets.add(TaskService.class);
		return sets;
	}

}
