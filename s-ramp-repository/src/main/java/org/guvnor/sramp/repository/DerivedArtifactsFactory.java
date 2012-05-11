package org.guvnor.sramp.repository;

import java.util.ServiceLoader;


public class DerivedArtifactsFactory {

    public static DerivedArtifacts newInstance()  {

        for (DerivedArtifacts manager : ServiceLoader.load(DerivedArtifacts.class)) {
            return manager;
        }
        return null;
    }
}
