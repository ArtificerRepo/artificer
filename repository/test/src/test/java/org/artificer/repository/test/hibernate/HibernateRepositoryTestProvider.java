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
package org.artificer.repository.test.hibernate;

import org.artificer.repository.filter.ServletCredentialsFilter;
import org.artificer.repository.hibernate.HibernateUtil;
import org.artificer.repository.hibernate.file.FileManagerFactory;
import org.artificer.repository.test.RepositoryTestProvider;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public class HibernateRepositoryTestProvider implements RepositoryTestProvider {

    private final Map<String, String> extraProperties;

    public HibernateRepositoryTestProvider(Map<String, String> extraProperties) {
        this.extraProperties = extraProperties;
    }

    @Override
    public void before() throws Exception {
        HibernateUtil.setPersistenceUnit("ArtificerTest");

        System.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        System.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        System.setProperty("hibernate.connection.url", "jdbc:h2:mem:dbHibernateTest;DB_CLOSE_DELAY=-1;MVCC=true");
        System.setProperty("hibernate.connection.username", "sa");
        System.setProperty("hibernate.cache.use_second_level_cache", "false");
        System.setProperty("hibernate.cache.use_query_cache", "false");
        System.setProperty("hibernate.search.default.directory_provider", "ram");
//        System.setProperty("hibernate.show_sql", "true");
        if (extraProperties != null) {
            for (String key : extraProperties.keySet()) {
                String value = extraProperties.get(key);
                System.setProperty(key, value);
            }
        }

        FileManagerFactory.reset();

        ServletCredentialsFilter.setUsername("junituser");

        new HibernateUtil.HibernateTask<Void>() {
            @Override
            protected Void doExecute(EntityManager entityManager) throws Exception {
                // H2 specific
                entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate();
                entityManager.createNativeQuery("SET LOCK_MODE 0").executeUpdate();
                List<Object[]> tables = entityManager.createNativeQuery("SHOW TABLES").getResultList();
                for (Object[] table : tables) {
                    entityManager.createNativeQuery("TRUNCATE TABLE " + table[0]).executeUpdate();
                }
                entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY true").executeUpdate();
                entityManager.createNativeQuery("SET LOCK_MODE 3").executeUpdate();

                return null;
            }
        }.execute();
    }

    @Override
    public void after() throws Exception {
    }
}
