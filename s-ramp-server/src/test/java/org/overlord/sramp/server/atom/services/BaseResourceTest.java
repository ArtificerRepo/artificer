/**
 *
 */
package org.overlord.sramp.server.atom.services;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:ryan@damnhandy.com">Ryan J. McDonough</a>
 * @version $Revision:$
 */
public abstract class BaseResourceTest
{

   protected static ResteasyDeployment deployment;
   protected static Dispatcher dispatcher;

   @BeforeClass
   public static void before() throws Exception
   {
      deployment = EmbeddedContainer.start();
      dispatcher = deployment.getDispatcher();
   }

   @AfterClass
   public static void after() throws Exception
   {
      EmbeddedContainer.stop();
      dispatcher = null;
      deployment = null;
   }

   public Registry getRegistry()
   {
      return deployment.getRegistry();
   }

   public ResteasyProviderFactory getProviderFactory()
   {
      return deployment.getProviderFactory();
   }

   /**
    * @param resource
    */
   public static void addPerRequestResource(Class<?> resource)
   {
      deployment.getRegistry().addPerRequestResource(resource);
   }

   public String readString(InputStream in) throws IOException
   {
      char[] buffer = new char[1024];
      StringBuilder builder = new StringBuilder();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      int wasRead = 0;
      do
      {
         wasRead = reader.read(buffer, 0, 1024);
         if (wasRead > 0)
         {
            builder.append(buffer, 0, wasRead);
         }
      }
      while (wasRead > -1);

      return builder.toString();
   }
}