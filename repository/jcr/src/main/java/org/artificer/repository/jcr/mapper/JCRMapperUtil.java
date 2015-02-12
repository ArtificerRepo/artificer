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
package org.artificer.repository.jcr.mapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * @author Brett Meyer
 */
public class JCRMapperUtil {
    
    /**
     * Gets a String property from the JCR node.  Returns null if the property does not exist on the node.
     * 
     * @param jcrNode
     * @param propertyName
     * @return String
     * @throws RepositoryException
     */
    public static String getPropertyString(Node jcrNode, String propertyName) throws RepositoryException {
        if (jcrNode.hasProperty(propertyName)) {
            return jcrNode.getProperty(propertyName).getString();
        } else {
            return null;
        }
    }
    
    /**
     * Gets a multi-value property from the JCR node.  Returns null if the property does not exist on the node.
     * 
     * @param jcrNode
     * @param propertyName
     * @return Value[]
     * @throws RepositoryException
     */
    public static Value[] getPropertyArray(Node jcrNode, String propertyName) throws RepositoryException {
        if (jcrNode.hasProperty(propertyName)) {
            return jcrNode.getProperty(propertyName).getValues();
        } else {
            return null;
        }
    }
}
