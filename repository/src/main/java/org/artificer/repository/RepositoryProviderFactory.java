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

import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerConstants;
import org.artificer.repository.i18n.Messages;
import org.overlord.commons.services.ServiceRegistryUtil;

import java.util.Set;

/**
 * @author Brett Meyer.
 */
public class RepositoryProviderFactory {

    private static RepositoryProvider provider = null;

    public static PersistenceManager persistenceManager() {
        return provider().persistenceManager();
    }

    public static QueryManager queryManager() {
        return provider().queryManager();
    }

    public static AuditManager auditManager() {
        return provider().auditManager();
    }

    private static RepositoryProvider provider() {
        if (provider == null) {
            String providerName = ArtificerConfig.getConfigProperty(ArtificerConstants.ARTIFICER_CONFIG_REPO_PROVIDER, "jpa");
            Set<RepositoryProvider> foundProviders = ServiceRegistryUtil.getServices(RepositoryProvider.class);
            for (RepositoryProvider foundProvider : foundProviders) {
                if (foundProvider.name().equalsIgnoreCase(providerName)) {
                    provider = foundProvider;
                }
            }

            if (provider == null)
                throw new RuntimeException(Messages.i18n.format("MISSING_REPO_PROVIDER"));
        }
        return provider;
    }

    // primarily for testing
    public static void overrideProvider(RepositoryProvider override) {
        provider = override;
    }
}
