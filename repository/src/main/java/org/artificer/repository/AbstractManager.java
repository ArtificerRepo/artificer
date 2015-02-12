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
package org.artificer.repository;

/**
 * @author Brett Meyer.
 */
public interface AbstractManager {

    /**
     * More often than not, this won't be used -- S-RAMP should simply use the credentials available through
     * REST/Servlet calls.  However, if the s-ramp-server is used through EJB, we'll need another way to auth
     * with the managers.  Support through this method.
     */
    public void login(String username, String password);
}
