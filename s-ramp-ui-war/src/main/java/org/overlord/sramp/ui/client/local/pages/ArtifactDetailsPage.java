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
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.nav.client.local.PageState;
import org.jboss.errai.ui.shared.api.annotations.AutoBound;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.overlord.sramp.ui.client.local.DOMUtil;
import org.overlord.sramp.ui.client.local.services.ArtifactRpcService;
import org.overlord.sramp.ui.client.local.services.IRpcServiceInvocationHandler;
import org.overlord.sramp.ui.client.shared.beans.ArtifactBean;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;

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

    @PageState
    private String uuid;

    @Inject @AutoBound
    protected DataBinder<ArtifactBean> artifact;

    protected Element pageContent;

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
        pageContent.setAttribute("style", "display:none");
    }

    /**
     * @see org.overlord.sramp.ui.client.local.pages.AbstractPage#onPageShowing()
     */
    @Override
    protected void onPageShowing() {
        pageContent.setAttribute("style", "display:none");
        artifactService.get(uuid, new IRpcServiceInvocationHandler<ArtifactBean>() {
            @Override
            public void onReturn(ArtifactBean data) {
                updateArtifactMetaData(data);
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
        pageContent.removeAttribute("style");
    }

}
