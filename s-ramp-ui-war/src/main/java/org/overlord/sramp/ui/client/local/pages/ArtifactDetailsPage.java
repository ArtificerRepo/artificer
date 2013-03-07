/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.ui.client.local.pages;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.InitialState;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Bound;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.pages.details.SourceEditor;
import org.overlord.sramp.ui.client.local.services.ArtifactRpcService;
import org.overlord.sramp.ui.client.local.services.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.local.util.DOMUtil;
import org.overlord.sramp.ui.client.local.util.DataBindingDateConverter;
import org.overlord.sramp.ui.client.local.widgets.common.HtmlSnippet;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;

/**
 * The page shown to the user when she clicks on one of the artifacts
 * displayed in the Artifacts Table on the Artifacts page.
 *
 * @author eric.wittmann@redhat.com
 */
@Templated("/org/overlord/sramp/ui/client/local/site/artifact-details.html#page")
@Page(path="details")
@Dependent
public class ArtifactDetailsPage extends AbstractPage {

    @Inject
    protected ArtifactRpcService artifactService;
    protected ArtifactBean currentArtifact;

    @PageState
    private String uuid;

    @Inject @AutoBound
    protected DataBinder<ArtifactBean> artifact;

    @Inject @DataField("core-property-name") @Bound(property="name")
    InlineLabel name;
    @Inject @DataField("core-property-version") @Bound(property="version")
    InlineLabel version;

    @Inject @DataField("link-download-content")
    Anchor downloadContentLink;
    @Inject @DataField("link-download-metaData")
    Anchor downloadMetaDataLink;

    @Inject @DataField("core-property-type") @Bound(property="type")
    Label type;
    @Inject @DataField("core-property-uuid") @Bound(property="uuid")
    Label uuidField;
    @Inject @DataField("core-property-derived") @Bound(property="derived")
    Label derived;
    @Inject @DataField("core-property-createdOn") @Bound(property="createdOn", converter=DataBindingDateConverter.class)
    InlineLabel createdOn;
    @Inject @DataField("core-property-createdBy") @Bound(property="createdBy")
    InlineLabel createdBy;
    @Inject @DataField("core-property-modifiedOn") @Bound(property="updatedOn", converter=DataBindingDateConverter.class)
    InlineLabel modifiedOn;
    @Inject @DataField("core-property-modifiedBy") @Bound(property="updatedBy")
    InlineLabel modifiedBy;

    @Inject @DataField("sramp-artifact-tabs-source")
    Anchor sourceTabAnchor;
    @Inject @DataField("source-tab-progress")
    HtmlSnippet sourceTabProgress;
    @Inject @DataField("artifact-editor")
    SourceEditor sourceEditor;
    protected boolean sourceLoaded;

    protected Element pageContent;
    protected Element editorWrapper;

    /**
     * Constructor.
     */
    public ArtifactDetailsPage() {
    }

    /**
     * Called after the widget is constructed.
     */
    @PostConstruct
    protected void onPostConstruct() {
        pageContent = DOMUtil.findElementById(getElement(), "page-content");
        editorWrapper = DOMUtil.findElementById(getElement(), "editor-wrapper");
    }

    /**
     * @see org.overlord.sramp.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        sourceLoaded = false;
        currentArtifact = null;
        pageContent.setAttribute("style", "display:none");
        sourceTabAnchor.setVisible(false);
        editorWrapper.setAttribute("style", "display:none");
        artifactService.get(uuid, new IRpcServiceInvocationHandler<ArtifactBean>() {
            @Override
            public void onReturn(ArtifactBean data) {
                currentArtifact = data;
                updateArtifactMetaData(data);
            }
            @Override
            public void onError(Throwable error) {
                Window.alert(error.getMessage());
            }
        });
        sourceTabAnchor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!sourceLoaded) {
                    loadSource(currentArtifact);
                }
            }
        });
    }

    /**
     * Loads the artifact's source (async) into the source tab's editor control.
     * @param artifact
     */
    protected void loadSource(ArtifactBean artifact) {
        if (!artifact.isTextDocument()) {
            return;
        }
        sourceTabProgress.setVisible(true);
        artifactService.getDocumentContent(artifact.getUuid(), artifact.getType(), new IRpcServiceInvocationHandler<String>() {
            @Override
            public void onReturn(String data) {
                sourceEditor.setValue(data);
                sourceTabProgress.setVisible(false);
                editorWrapper.removeAttribute("style");
                sourceLoaded = true;
            }
            @Override
            public void onError(Throwable error) {
                Window.alert(error.getMessage());
            }
        });
    }

    /**
     * Called when the artifact meta data is loaded.
     * @param artifact
     */
    protected void updateArtifactMetaData(ArtifactBean artifact) {
        this.artifact.setModel(artifact, InitialState.FROM_MODEL);
        String contentUrl = GWT.getModuleBaseURL() + "services/artifactDownload";
        contentUrl += "?uuid=" + artifact.getUuid() + "&type=" + artifact.getType();
        String metaDataUrl = contentUrl + "&as=meta-data";
        this.downloadContentLink.setHref(contentUrl);
        this.downloadMetaDataLink.setHref(metaDataUrl);
        this.sourceEditor.setValue("");

        if (artifact.isTextDocument()) {
            sourceTabAnchor.setVisible(true);
        }
        pageContent.removeAttribute("style");
    }

}
