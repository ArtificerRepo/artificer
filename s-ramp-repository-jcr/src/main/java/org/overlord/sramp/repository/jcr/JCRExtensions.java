/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import java.io.InputStream;

import javax.jcr.Binary;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

/**
 * Certain capabilities are provided by JCR implementations that extend the JCR spec that can be reused, saving
 * S-RAMP from having to provide similar functionality internally.  The repository module should override these methods
 * if the impl provides a cleaner or more performant alternative.  Otherwise, this acts as a base class providing
 * from-scratch solutions.
 * 
 * Note that I'm writing this under the assumption that the method set will be fairly limited.  If it grows sufficiently
 * large, this should be attempted into a better design pattern.
 * 
 * @author Brett Meyer
 */
public abstract class JCRExtensions {
    
    private static JCRExtensions instance = null;
    
    // TODO: Not sure how I feel about this pattern.  Wire it elsewhere?
    public static JCRExtensions getInstance() throws Exception {
        if (instance == null) {
            instance = JCRRepositoryFactory.getInstance().getExtensions();
        }
        return instance;
    }
    
    /**
     * Generate the SHA-1 hash for the given {@link Binary} value.
     * 
     * @param binary The Binary value to hash
     * @return String the SHA-1 hash
     * @throws Exception
     */
    public String getSha1Hash(Binary binary) throws Exception {
        InputStream inputStream = null;
        try {
            inputStream = binary.getStream();
            String shaHex = DigestUtils.shaHex(inputStream);
            return shaHex;
        } finally {
            if (inputStream != null) {
                IOUtils.closeQuietly(inputStream);
            }
        }
    }
}
