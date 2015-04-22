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
package org.artificer.repository.hibernate.query;

import org.apache.lucene.document.Document;
import org.hibernate.search.bridge.LuceneOptions;
import org.hibernate.search.bridge.builtin.TikaBridge;

/**
 * Hibernate Search's TikaBridge throws an exception if given a null value.  Since ArtificerArtifact's 'content'
 * and 'contentPath' are mutually exclusive, one will always have a null.
 *
 * Instead, use this bridge that gracefully handles the nulls.
 *
 * @author Brett Meyer.
 */
public class ArtificerTikaBridge extends TikaBridge {

    @Override
    public void set(String name, Object value, Document document, LuceneOptions luceneOptions) {
        if (value != null) {
            super.set(name, value, document, luceneOptions);
        }
    }
}
