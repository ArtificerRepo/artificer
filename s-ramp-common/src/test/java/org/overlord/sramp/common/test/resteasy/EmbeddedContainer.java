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

import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.spi.ResteasyDeployment;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class EmbeddedContainer
{
   private static Class<?> bootstrap = TJWSServletContainer.class;

   public static Class getBootstrap()
   {
      return bootstrap;
   }

   /**
    * Exists for tests that require a servlet container behind the scenes.
    *
    * @return
    */
   public static boolean isServlet()
   {
      return true;
   }

   static
   {
      String boot = System.getProperty("org.resteasy.test.embedded.container");
      if (boot != null)
      {
         try
         {
            bootstrap = Thread.currentThread().getContextClassLoader().loadClass(boot);
         }
         catch (ClassNotFoundException e)
         {
            throw new RuntimeException(e);
         }
      }
   }

   public static void setBootstrap(Class bootstrap)
   {
      EmbeddedContainer.bootstrap = bootstrap;
   }

   public static ResteasyDeployment start() throws Exception
   {
      return start("/", (Hashtable<String,String>) null);
   }
   
   public static ResteasyDeployment start(String bindPath) throws Exception
   {
      return start(bindPath, null, null);
   }
   
   public static ResteasyDeployment start(Hashtable<String,String> initParams) throws Exception
   {
      return start("/", initParams);
   }

   public static ResteasyDeployment start(Hashtable<String,String> initParams, Hashtable<String,String> contextParams) throws Exception
   {
      return start("/", initParams, contextParams);
   }
   
   public static ResteasyDeployment start(String bindPath, Hashtable<String,String> initParams) throws Exception
   {
      Method start = bootstrap.getMethod("start", String.class, Hashtable.class);
      return (ResteasyDeployment) start.invoke(null, bindPath, initParams);
   }

   public static ResteasyDeployment start(String bindPath, Hashtable<String,String> initParams, Hashtable<String,String> contextParams) throws Exception
   {
      Method start = bootstrap.getMethod("start", String.class, Hashtable.class, Hashtable.class);
      return (ResteasyDeployment) start.invoke(null, bindPath, initParams, contextParams);
   }
   
   public static ResteasyDeployment start(String bindPath, SecurityDomain domain) throws Exception
   {
      Method start = bootstrap.getMethod("start", String.class, SecurityDomain.class);
      return (ResteasyDeployment) start.invoke(null, bindPath, domain);

   }

   public static void start(ResteasyDeployment deployment) throws Exception
   {
      Method start = bootstrap.getMethod("start", ResteasyDeployment.class);
      start.invoke(null, deployment);

   }

   public static void stop() throws Exception
   {
      Method stop = bootstrap.getMethod("stop");
      stop.invoke(null);
   }
}