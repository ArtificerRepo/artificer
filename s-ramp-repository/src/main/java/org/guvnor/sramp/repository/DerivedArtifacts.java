package org.guvnor.sramp.repository;


public interface DerivedArtifacts {

    /**
     * Create the derived artifacts from the artifact.
     * @param <T>
     * @param entityClass
     * @param filePath
     * @return
     */
    public <T> T createDerivedArtifact(Class<T> entityClass, String artifactFileName) throws DerivedArtifactsCreationException, UnsupportedFiletypeException;

}
