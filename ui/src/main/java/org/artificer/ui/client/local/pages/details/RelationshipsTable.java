/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.local.pages.details;

import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;
import org.jboss.errai.ui.nav.client.local.TransitionAnchorFactory;
import org.overlord.commons.gwt.client.local.widgets.TemplatedWidgetTable;
import org.artificer.ui.client.local.pages.ArtifactDetailsPage;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsBean;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A table of artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
@Dependent
public class RelationshipsTable extends TemplatedWidgetTable implements HasValue<Map<String, ArtifactRelationshipsBean>> {

    @Inject
    protected TransitionAnchorFactory<ArtifactDetailsPage> toDetailsPageLinkFactory;

    /**
     * Constructor.
     */
    public RelationshipsTable() {
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    @Override
    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<Map<String, ArtifactRelationshipsBean>> handler) {
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    @Override
    public Map<String, ArtifactRelationshipsBean> getValue() {
        return null;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Map<String, ArtifactRelationshipsBean> value) {
        setValue(value, false);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
     */
    @Override
    public void setValue(Map<String, ArtifactRelationshipsBean> value, boolean fireEvents) {
        Set<String> keys = new TreeSet<String>(value.keySet());
        for (String key : keys) {
            addHeadingRow(key);
            ArtifactRelationshipsBean relationships = value.get(key);
            for (ArtifactRelationshipBean relationship : relationships.getRelationships()) {
                addDataRow(relationship);
            }
        }
    }

    /**
     * Adds a heading row to the table.  One of these gets added per
     * relationship type.
     * @param key
     */
    private void addHeadingRow(String key) {
        int rowIdx = this.rowElements.size();
        InlineLabel heading = new InlineLabel(key);
        add(rowIdx, 0, heading).setAttribute("colspan", "4"); //$NON-NLS-1$ //$NON-NLS-2$
        getRow(rowIdx).setClassName("sramp-relationship-type"); //$NON-NLS-1$
    }

    /**
     * Adds a single data row to the table.
     * @param relationship
     */
    public void addDataRow(final ArtifactRelationshipBean relationship) {
        int rowIdx = this.rowElements.size();
        DateTimeFormat format = DateTimeFormat.getFormat("MM/dd/yyyy"); //$NON-NLS-1$

        Anchor name = toDetailsPageLinkFactory.get("uuid", relationship.getTargetUuid()); //$NON-NLS-1$
        name.setText(relationship.getTargetName());
        InlineLabel type = new InlineLabel(relationship.getTargetType());
        InlineLabel modified = new InlineLabel(format.format(relationship.getTargetLastModified()));
//        InlineLabel actions = new InlineLabel("");

        add(rowIdx, 0, name);
        add(rowIdx, 1, type);
        add(rowIdx, 2, modified);
//        add(rowIdx, 3, actions);
    }

}
