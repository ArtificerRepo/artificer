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
package org.artificer.ui.client.shared.beans;

import com.google.gwt.i18n.client.DateTimeFormat;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.databinding.client.api.Bindable;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Brett Meyer.
 */
@Portable
@Bindable
public class ArtifactCommentBean implements Serializable {

    private static final long serialVersionUID = ArtifactCommentBean.class.hashCode();

    private String createdBy;

    private Date createdOn;

    private String text;

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("<strong>").append(createdBy).append(" (").append(df.format(createdOn)).append(")</strong><br/>");
        sb.append(text);
        return sb.toString();
    }
}
