/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.governance;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.configuration.ConfigurationException;
import org.overlord.sramp.client.SrampClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 *
 */
public class SRAMPMonitor extends TimerTask {

    private Logger log = LoggerFactory.getLogger(this.getClass());
	private Timer timer = null;
	Governance governance = new Governance();

	private long interval = governance.getQueryInterval();
	private long acceptableLagTime = governance.getAcceptableLagtime();

	public SRAMPMonitor() throws ConfigurationException {
		super();
		timer = new Timer(true);
		timer.scheduleAtFixedRate(this, 0, interval);
	}

	@Override
	public boolean cancel() {
		timer.cancel();
		return super.cancel();
	}


	@Override
    public synchronized void run()
	{
	    try {
    		if (firedOnTime(scheduledExecutionTime()) && isAppserverReady()) {
    			long startTime = System.currentTimeMillis();

    			QueryExecutor queryExecutor = new QueryExecutor();
    			queryExecutor.execute();

                long endTime   = System.currentTimeMillis();

                if ((endTime-startTime) > interval) {
                	log.debug("Notification background task duration exceeds the JUDDI_NOTIFICATION_INTERVAL" +
                			" of " + interval + ". Notification background task took "
                			+ (endTime - startTime) + " milliseconds.");
                } else {
                	log.debug("Notification background task took " + (endTime - startTime) + " milliseconds.");
                }
    		} else {
    			log.debug("Skipping current notification cycle because lagtime is too great.");
    		}
	    } catch (ConfigException confEx) {
	        log.error(confEx.getMessage());
	    } catch (SrampClientException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
 	}
	/**
	 * Checks to see that the event are fired on time. If they are late this may indicate that the server
	 * is under load. The acceptableLagTime is configurable using the "juddi.notification.acceptable.lagtime"
	 * property and is defaulted to 1000ms. A negative value means that you do not care about the lag time
	 * and you simply always want to go do the notification work.
	 *
	 * @param scheduleExecutionTime
	 * @return true if the server is within the acceptable latency lag.
	 */
	private boolean firedOnTime(long scheduleExecutionTime) {
		long lagTime = System.currentTimeMillis() - scheduleExecutionTime;
		if (lagTime <= acceptableLagTime || acceptableLagTime < 0) {
			return true;
		} else {
			log.debug("NotificationTimer is lagging " + lagTime + " milli seconds behind. A lag time "
					+ "which exceeds an acceptable lagtime of " + acceptableLagTime + "ms indicates "
					+ "that the registry server is under load or was in sleep mode. We are therefore skipping this notification "
					+ "cycle.");
			return false;
		}
	}

	/**
	 * Checks if we can ready the S-RAMP repository as well as the BPM API.
	 *
	 * @return
	 * @throws MalformedURLException
	 */
	private boolean isAppserverReady() throws MalformedURLException {
	    boolean isReady = true;
	    String serviceDocumentUrl = governance.getSrampUrl().toExternalForm() + "/s-ramp/servicedocument";
	    isReady =  urlExists(serviceDocumentUrl);
	    if (isReady) {
	        String bpmUrl = governance.getJbpmUrl().toExternalForm();
	        isReady = urlExists(bpmUrl);
	        if (!isReady) log.debug("Cannot yet connect to the BPM API at: " + governance.getJbpmUrl().toExternalForm());
	    } else {
	        if (!isReady) log.debug("Cannot yet connect to the S-RAMP repo at: " + governance.getSrampUrl().toExternalForm());
	    }
	    return isReady;
	}

    /**
     * Returns true if the given URL can be accessed.
     * @param checkUrl
     */
    public boolean urlExists(String checkUrl) {
        try {
            URL checkURL = new URL(checkUrl);
            HttpURLConnection checkConnection = (HttpURLConnection) checkURL.openConnection();
            checkConnection.setRequestMethod("HEAD");
            checkConnection.setConnectTimeout(10000);
            checkConnection.setReadTimeout(10000);
            checkConnection.connect();
            return (checkConnection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }


}
