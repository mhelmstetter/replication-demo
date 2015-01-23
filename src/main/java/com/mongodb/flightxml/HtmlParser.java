package com.mongodb.flightxml;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.slf4j.LoggerFactory;


public class HtmlParser {
	
    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(HtmlParser.class);
    
	static {
		System.getProperties().setProperty("httpclient.useragent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.14) Gecko/20080404 Firefox/2.0.0.14");
	}
	
	private HttpClient httpClient;
	
	public HtmlParser() {
	    
	    //httpClient = new HttpClient();
	    
	    HttpConnectionManagerParams cmparams = new HttpConnectionManagerParams();
	    cmparams.setSoTimeout(10000);
	    cmparams.setTcpNoDelay(true);
	    
	    HttpConnectionManager manager = new SimpleHttpConnectionManager();
	    manager.setParams(cmparams);
	    HttpClientParams params = new HttpClientParams();
	    params.setSoTimeout(5000);
	   
	    
	    params.setHttpElementCharset("UTF-8");
        params.setContentCharset("UTF-8");
        params.setParameter(HttpMethodParams.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
	    
        httpClient = new HttpClient(params, manager);
	    
	}
	
	public InputStream get(String url) throws HttpException, IOException {
		
        
        //httpClient.getHostConfiguration().setProxy("my.proxyhost.com", 80);
        //Credentials defaultcreds = new UsernamePasswordCredentials(httpUsername, httpPassword);
        //httpClient.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT, AuthScope.ANY_REALM), defaultcreds);
        GetMethod method = new GetMethod(url);
        method.setFollowRedirects(true);
        
        InputStream is = null;
        int response = 0;
        boolean success = false;
        int tries = 0;
        while (success == false && tries < 3) {
            try {
            response = httpClient.executeMethod(method);
            is = method.getResponseBodyAsStream();
            if (is == null) {
                continue;
            }
            success = true;
            } catch (Exception e) {
                logger.debug("RETRY");
                try {
                    Thread.sleep(1000 * (tries +1));
                } catch (InterruptedException e1) {
                }
            }
            tries++;
        }
        
        if (logger.isDebugEnabled()) {
        	logger.debug("GET: " + url + ", response code=" + response);
        }
        //String responseBody = method.getResponseBodyAsString();
        if (logger.isTraceEnabled()) {
        	String debug = method.getResponseBodyAsString();
        	logger.trace("responseBody: " + debug);
        }
        return is;
	}	

}
