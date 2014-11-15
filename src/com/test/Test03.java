package com.test;

import java.io.*;
import java.net.*;
import java.security.SecureRandom;
import java.security.cert.*;
import javax.net.ssl.*;


public class Test03 {

	public static void main(String args[]) {
		String BASE_URL = "http://www.baidu.com/s?wd=黑龙江";
		try {
			TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager(){
		        public X509Certificate[] getAcceptedIssuers(){return null;}
		        public void checkClientTrusted(X509Certificate[] certs, String authType){}
		        public void checkServerTrusted(X509Certificate[] certs, String authType){}
		    }};
			
			SSLContext sc = SSLContext.getInstance("TLS");
		    sc.init(null, trustAllCerts, new SecureRandom());
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    
			Proxy proxy = new Proxy(Proxy.Type.HTTP,   
					new InetSocketAddress("127.0.0.1", 8580));  
			URL url = new URL(BASE_URL);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);
			connection.setRequestProperty("User-agent","Mozilla/5.0");
			connection.setInstanceFollowRedirects(false);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			System.out.println("=============================");
			String lines;
			while ((lines = reader.readLine()) != null) {
				System.out.println(lines);
			}
			reader.close();
			// 断开连接
			connection.disconnect();
			System.out.println("=============================");

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
