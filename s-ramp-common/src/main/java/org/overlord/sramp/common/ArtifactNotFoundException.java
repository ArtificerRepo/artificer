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
package org.overlord.sramp.common;


/**
 * Exception thrown when the user attempts to access an artifact that does not exist.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactNotFoundException extends SrampUserException {

    private static final long serialVersionUID = 1131976536249817281L;

    /**
     * Constructor.
     */
    public ArtifactNotFoundException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtifactNotFoundException(String uuid) {
        super("No artifact found with UUID: " + uuid);
    }

}
