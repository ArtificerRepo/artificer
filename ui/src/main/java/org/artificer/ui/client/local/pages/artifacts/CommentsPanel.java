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
package org.artificer.ui.client.local.pages.artifacts;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import org.artificer.ui.client.shared.beans.ArtifactCommentBean;

import java.util.List;

/**
 * @author Brett Meyer.
 */
public class CommentsPanel extends FlowPanel implements HasValue<List<ArtifactCommentBean>> {

    private List<ArtifactCommentBean> value;

    @Override
    public List<ArtifactCommentBean> getValue() {
        return value;
    }

    public void addComment(ArtifactCommentBean commentBean) {
        value.add(commentBean);
        createComment(commentBean);
        ValueChangeEvent.fire(this, value);
    }

    @Override
    public void setValue(List<ArtifactCommentBean> value) {
        setValue(value, false);
    }

    @Override
    public void setValue(List<ArtifactCommentBean> value, boolean fireEvents) {
        this.value = value;

        for (ArtifactCommentBean commentBean : value) {
            createComment(commentBean);
        }

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<List<ArtifactCommentBean>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    private void createComment(ArtifactCommentBean commentBean) {
        ParagraphElement pElement = Document.get().createPElement();
        pElement.setInnerHTML(commentBean.toString());
        add(HTMLPanel.wrap(pElement));
    }
}
