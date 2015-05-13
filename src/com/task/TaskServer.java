package com.task;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;

import org.apache.log4j.Logger;

import com.seal.expand.Seal;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

public class TaskServer {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		URI ServerURI=UriBuilder.fromUri("http://localhost/").port(7788).build();
    	startServer(ServerURI);
        System.out.println("服务已启动，请访问："+ServerURI);
    }    
	
    protected static SelectorThread startServer(URI serverURI) throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();
        
        initParams.put("javax.ws.rs.Application","com.task.App");
//        initParams.put("com.sun.jersey.config.property.packages" ,"com.task.service");
        
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(serverURI, initParams); 
        return threadSelector;
    }
}
