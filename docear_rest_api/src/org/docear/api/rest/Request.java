package org.docear.api.rest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class Request {
	private final RequestMethod method;
	
	public Request(RequestMethod method) {
		this.method = method;
	}

	public RequestMethod getRequestMethod() {
		return method;
	}
	
	public Response query(String query) throws Exception {
		//TODO build HTTP Header with or w/o security token
		//TODO parse HTTP response and status code
				

	       
		URL url = new URL("https://api.docear.org/");
		URLConnection conn = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        
        while ((inputLine = reader.readLine()) != null) {
            System.out.println(inputLine);
        }
		return null;
	}

}
