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
package org.overlord.sramp.repository;

<<<<<<< HEAD
import org.overlord.commons.services.ServiceRegistryUtil;
=======
import org.overlord.commons.config.services.ServiceRegistryUtil;
>>>>>>> 5b3b6bf6b5acf60e408c713328b4a8cf7e491105
import org.overlord.sramp.repository.i18n.Messages;


public class DerivedArtifactsFactory {

    public static DerivedArtifacts newInstance()  {
        DerivedArtifacts daManager = ServiceRegistryUtil.getSingleService(DerivedArtifacts.class);
        if (daManager == null)
            throw new RuntimeException(Messages.i18n.format("MISSING_DERIVED_ARTIFACTS_PROVIDER")); //$NON-NLS-1$
        return daManager;
    }
}
