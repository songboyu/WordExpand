package com.task.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;  

import com.task.Task;

@Path("/task")
public class TaskService {
	@POST @Path("/createTask")  
	@Consumes("application/x-www-form-urlencoded")  
	public static String createTask(@FormParam("taskId")String taskId, @FormParam("seeds")String seeds){
		Task t = new Task();
		return "{\"result\":\""+t.createTask(taskId, seeds)+"\"}";
	}
}
