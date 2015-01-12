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
package org.overlord.sramp.repository.jcr.query;

import org.overlord.commons.services.ServiceRegistryUtil;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.repository.i18n.Messages;
import org.overlord.sramp.repository.jcr.ClassificationHelper;

import javax.jcr.Session;

/**
 * @author Brett Meyer.
 */
public class SrampToJcrSql2QueryVisitorFactory {

    public static SrampToJcrSql2QueryVisitor newInstance(Session session, ClassificationHelper classificationHelper) throws SrampException {
        SrampToJcrSql2QueryVisitorProvider queryVisitorProvider = ServiceRegistryUtil.getSingleService(
                SrampToJcrSql2QueryVisitorProvider.class);
        if (queryVisitorProvider == null)
            throw new RuntimeException(Messages.i18n.format("MISSING_QUERYVISITOR_PROVIDER")); //$NON-NLS-1$
        return queryVisitorProvider.createQueryVisitor(session, classificationHelper);
    }
}
