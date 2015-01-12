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
package org.overlord.sramp.repository.jcr.modeshape.query;

import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.repository.jcr.ClassificationHelper;
import org.overlord.sramp.repository.jcr.query.SrampToJcrSql2QueryVisitor;
import org.overlord.sramp.repository.jcr.query.SrampToJcrSql2QueryVisitorProvider;

import javax.jcr.Session;

/**
 * @author Brett Meyer.
 */
public class ModeShapeQueryVisitorProvider implements SrampToJcrSql2QueryVisitorProvider {

    @Override
    public SrampToJcrSql2QueryVisitor createQueryVisitor(Session session, ClassificationHelper classificationHelper) throws SrampException {
        return new ModeShapeQueryVisitor(session, classificationHelper);
    }
}
