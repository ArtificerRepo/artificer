package org.guvnor.sramp.repository;


public interface DerivedArtifacts {

    /**
     * Create the derived artifacts from the artifact.
     * @param <T>
     * @param entityClass
     * @param identifier, String ID of the original artifact.
     * @return
     */
    public <T> T createDerivedArtifact(Class<T> entityClass, String identifier) throws DerivedArtifactsCreationException, UnsupportedFiletypeException;

}
