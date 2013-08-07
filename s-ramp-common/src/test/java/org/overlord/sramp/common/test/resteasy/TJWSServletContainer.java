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
package org.overlord.sramp.common.test.resteasy;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class TJWSServletContainer
{
   public static TJWSEmbeddedJaxrsServer tjws;

   public static ResteasyDeployment start() throws Exception
   {
      return start(""); //$NON-NLS-1$
   }

   public static ResteasyDeployment start(String bindPath) throws Exception
   {
      return start(bindPath, null, null, null);
   }

   public static ResteasyDeployment start(String bindPath, Hashtable<String,String> initParams) throws Exception
   {
      return start(bindPath, null, initParams, null);
   }

   public static ResteasyDeployment start(String bindPath, Hashtable<String,String> initParams, Hashtable<String,String> contextParams) throws Exception
   {
      return start(bindPath, null, initParams, contextParams);
   }
   
   public static void start(ResteasyDeployment deployment) throws Exception
   {
      System.out.println("[Embedded Container Start]"); //$NON-NLS-1$
      tjws = new TJWSEmbeddedJaxrsServer();
      tjws.setDeployment(deployment);
      tjws.setPort(TestPortProvider.getPort());
      tjws.setRootResourcePath(""); //$NON-NLS-1$
      tjws.setSecurityDomain(null);
      tjws.setBindAddress(TestPortProvider.getHost());
      tjws.start();
   }

   public static ResteasyDeployment start(String bindPath, SecurityDomain domain) throws Exception
   {
      return start(bindPath, domain, null, null);
   }
   
   public static ResteasyDeployment start(String bindPath, SecurityDomain domain, Hashtable<String,String> initParams, Hashtable<String,String> contextParams) throws Exception
   {
      ResteasyDeployment deployment = new ResteasyDeployment();
      deployment.setSecurityEnabled(true);
      String applicationClass = null;
      if (contextParams != null)
      {
         applicationClass = contextParams.get("javax.ws.rs.Application"); //$NON-NLS-1$
         String mediaTypeMappingsString = contextParams.get("resteasy.media.type.mappings"); //$NON-NLS-1$
         if (mediaTypeMappingsString != null)
         {
            Map<String, String> mediaTypeMappings = new HashMap<String, String>();
            String[] mappings = mediaTypeMappingsString.split(","); //$NON-NLS-1$
            for (int i = 0; i < mappings.length; i++)
            {
               String[] mapping = mappings[i].split(":"); //$NON-NLS-1$
               mediaTypeMappings.put(mapping[0], mapping[1]);
            }
            deployment.setMediaTypeMappings(mediaTypeMappings);
         }
      }
      if (applicationClass == null && initParams != null)
      {
         applicationClass = initParams.get("javax.ws.rs.Application");  //$NON-NLS-1$
      }
      if (applicationClass != null)
      {
         deployment.setApplicationClass(applicationClass);
      }
      return start(bindPath, domain, deployment, initParams, contextParams);
   }

   public static ResteasyDeployment start(String bindPath, SecurityDomain domain, ResteasyDeployment deployment, Hashtable<String,String> initParams, Hashtable<String,String> contextParams) throws Exception
   {
       System.out.println("[Embedded Container Start]"); //$NON-NLS-1$
      tjws = new TJWSEmbeddedJaxrsServer();
      tjws.setDeployment(deployment);
      tjws.setPort(TestPortProvider.getPort());
      tjws.setRootResourcePath(bindPath);
      tjws.setSecurityDomain(domain);
      tjws.setInitParameters(initParams);
      tjws.setContextParameters(contextParams);
      tjws.setBindAddress(TestPortProvider.getHost());
      tjws.start();
      return tjws.getDeployment();
   }

   public static void stop() throws Exception
   {
      System.out.println("[Embedded Container Stop]"); //$NON-NLS-1$
      if (tjws != null)
      {
         try
         {
            tjws.stop();
         }
         catch (Exception e)
         {

         }
      }
      tjws = null;
   }

}