package org.overlord.sramp.repository;

import java.util.ServiceLoader;


public class PersistenceFactory {

    public static PersistenceManager newInstance() {

        for (PersistenceManager manager : ServiceLoader.load(PersistenceManager.class)) {
            return manager;
        }
        return null;
    }
}
