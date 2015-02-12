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
package org.artificer.integration.switchyard.artifactbuilder;

import java.util.Collection;
import java.util.Map;

import org.artificer.integration.artifactbuilder.CriteriaQueryRelationshipSource;
import org.artificer.integration.java.model.JavaModel;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.artificer.integration.switchyard.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brett Meyer
 */
public class JavaRelationshipSource extends CriteriaQueryRelationshipSource {

    private static Logger LOGGER = LoggerFactory.getLogger(JavaRelationshipSource.class);
    
    private String packageName;
    
    private String shortName;

    public JavaRelationshipSource(String className, Target target, Collection targetCollection, String model,
            String... types) {
        super(target, targetCollection, model, types);
        
        int idx = className.lastIndexOf('.');
        packageName = className.substring(0, idx);
        shortName = className.substring(idx + 1);
        
        // shortcut for SwitchYard
        packageName = packageName.replace("java:", "");
        packageName = packageName.replace("class:", "");
    }

    @Override
    protected void addCriteria(Map<String, String> criteria) {
        criteria.put(JavaModel.PROP_PACKAGE_NAME, packageName);
        criteria.put(JavaModel.PROP_CLASS_NAME, shortName);
    }
    
    @Override
    protected void notFound() {
        LOGGER.debug(Messages.i18n.format("NO_JAVA_CLASS", packageName + "." + shortName)); //$NON-NLS-1$ //$NON-NLS-2$
    }

}
