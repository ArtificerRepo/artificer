/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.server.core.api;

import org.artificer.common.ArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.InputStream;

/**
 * A service contract for interacting with artifacts.  Used as the logic/actions behind the Atom REST services, in
 * addition to impls exposed as EJBs.
 *
 * @author Brett Meyer.
 */
public interface ArtifactService extends AbstractService {

    public BaseArtifactType create(BaseArtifactType artifact) throws Exception;

    public BaseArtifactType create(ArtifactType artifactType, BaseArtifactType artifact) throws Exception;

    /**
     * Upload the given artifact.
     *
     * @param model
     * @param type
     * @param fileName
     * @param is
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(String model, String type, String fileName, InputStream is)
            throws Exception;

    /**
     * Upload the given artifact.
     *
     * @param fileName
     * @param is
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(String fileName, InputStream is) throws Exception;

    /**
     * Upload the given artifact.
     *
     * @param artifactType
     * @param fileName
     * @param is
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(ArtifactType artifactType, String fileName, InputStream is)
            throws Exception;

    /**
     * Upload the given artifact.  This byte[] version exists primarily for EJB clients, where the non-Serializable
     * InputStreams/Files cannot be sent.
     *
     * @param model
     * @param type
     * @param fileName
     * @param contentBytes
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(String model, String type, String fileName, byte[] contentBytes)
            throws Exception;

    /**
     * Upload the given artifact.  This byte[] version exists primarily for EJB clients, where the non-Serializable
     * InputStreams/Files cannot be sent.
     *
     * @param fileName
     * @param contentBytes
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(String fileName, byte[] contentBytes) throws Exception;

    /**
     * Upload the given artifact.  This byte[] version exists primarily for EJB clients, where the non-Serializable
     * InputStreams/Files cannot be sent.
     *
     * @param artifactType
     * @param fileName
     * @param contentBytes
     * @return BaseArtifactType
     * @throws Exception
     */
    public BaseArtifactType upload(ArtifactType artifactType, String fileName, byte[] contentBytes)
            throws Exception;

    public void updateMetaData(String model, String type, String uuid, BaseArtifactType updatedArtifact)
            throws Exception;

    public void updateMetaData(ArtifactType artifactType, String uuid,
            BaseArtifactType updatedArtifact) throws Exception;

    public void updateMetaData(BaseArtifactType updatedArtifact) throws Exception;

    public void updateContent(String model, String type, String uuid, String fileName, InputStream is)
            throws Exception;

    public void updateContent(ArtifactType artifactType, String uuid,
            String fileName, InputStream is) throws Exception;

    /**
     * Update the given artifact content.  This byte[] version exists primarily for EJB clients, where the non-Serializable
     * InputStreams/Files cannot be sent.
     *
     * @param model
     * @param type
     * @param uuid
     * @param fileName
     * @param contentBytes
     * @throws Exception
     */
    public void updateContent(String model, String type, String uuid, String fileName, byte[] contentBytes)
            throws Exception;

    /**
     * Update the given artifact content.  This byte[] version exists primarily for EJB clients, where the non-Serializable
     * InputStreams/Files cannot be sent.
     *
     * @param artifactType
     * @param uuid
     * @param fileName
     * @param contentBytes
     * @throws Exception
     */
    public void updateContent(ArtifactType artifactType, String uuid,
            String fileName, byte[] contentBytes) throws Exception;

    public BaseArtifactType addComment(ArtifactType artifactType, String uuid, String text) throws Exception;

    public BaseArtifactType getMetaData(String model, String type, String uuid) throws Exception;

    public BaseArtifactType getMetaData(ArtifactType artifactType, String uuid) throws Exception;

    public InputStream getContent(String model, String type, String uuid) throws Exception;

    public InputStream getContent(ArtifactType artifactType, String uuid) throws Exception;

    public InputStream getContent(ArtifactType artifactType, BaseArtifactType artifact) throws Exception;

    public byte[] getContentBytes(String model, String type, String uuid) throws Exception;

    public byte[] getContentBytes(ArtifactType artifactType, String uuid) throws Exception;

    public byte[] getContentBytes(ArtifactType artifactType, BaseArtifactType artifact) throws Exception;

    public void delete(String model, String type, String uuid) throws Exception;

    public void delete(ArtifactType artifactType, String uuid) throws Exception;

    public void deleteContent(String model, String type, String uuid) throws Exception;

    public void deleteContent(ArtifactType artifactType, String uuid) throws Exception;
}
