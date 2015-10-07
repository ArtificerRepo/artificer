package org.artificer.ui.client.shared.beans;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ArtifactTypeBean {

    public ArtifactTypeBean(String type) {
        super();
        this.type = type;
    }

    public ArtifactTypeBean() {
        super();
    }

    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
