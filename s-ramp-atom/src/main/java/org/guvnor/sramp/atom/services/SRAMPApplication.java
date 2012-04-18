package org.guvnor.sramp.atom.services;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class SRAMPApplication extends Application
{
   private Set<Object> singletons = new HashSet<Object>();
   private Set<Class<?>> empty = new HashSet<Class<?>>();

   public SRAMPApplication()
   {
      singletons.add(new EntryResource());
      singletons.add(new ServiceDocumentResource());
      singletons.add(new FeedResource());
      singletons.add(new XsdDocumentResource());
   }

   @Override
   public Set<Class<?>> getClasses()
   {
      return empty;
   }

   @Override
   public Set<Object> getSingletons()
   {
      return singletons;
   }
}
