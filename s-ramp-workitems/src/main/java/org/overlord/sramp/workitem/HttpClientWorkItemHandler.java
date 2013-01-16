package org.overlord.sramp.workitem;


import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;

public class HttpClientWorkItemHandler implements WorkItemHandler {


  public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {

    // extract parameters

    String urlStr = (String) workItem.getParameter("Url");

    String method = (String) workItem.getParameter("Method");
    
    String uuid = (String) workItem.getParameter("Uuid");
    
    if (uuid!=null && urlStr.contains("${uuid}")) {
    	urlStr = urlStr.replace("${uuid}", uuid);
    }

    // call http endpoint

    System.out.println(method + " TO: " + urlStr );
    
    try {
    
    	URL url = new URL(urlStr);
    	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    	connection.setRequestMethod(method);
    	connection.setConnectTimeout(10000);
    	connection.setReadTimeout(10000);
    	connection.connect();
    	int responseCode = connection.getResponseCode();
    	if (responseCode == 200) {
    		InputStream is = (InputStream) connection.getContent();
    		String reply = IOUtils.toString(is);
    		System.out.println("reply=" + reply);
    	} else {
    	    workItem.getParameters().put("Status", "ERROR");
    	    workItem.getParameters().put("StatusMsg", "endpoint " + urlStr + " could not be reached");
    		System.err.println("endpoint could not be reached");
    	}
    	
    } catch (Exception e) {
    	e.printStackTrace();
    }
    // notify manager that work item has been completed

    manager.completeWorkItem(workItem.getId(), null);

  }


  public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {

    // Do nothing, notifications cannot be aborted

  }


}
