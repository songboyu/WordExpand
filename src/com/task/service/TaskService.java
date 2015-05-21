package com.task.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.task.TaskV2;

@Path("/task")
public class TaskService {
	@POST @Path("/createTask")  
	@Consumes("application/x-www-form-urlencoded")  
	public static String createTask(@FormParam("taskId")String taskId, @FormParam("seeds")String seeds){
		TaskV2 t = new TaskV2();
		return "{\"result\":\""+t.createTask(taskId, seeds)+"\"}";
	}
}
