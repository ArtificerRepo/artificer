package org.artificer.ui.client.local.services;

import org.artificer.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.commons.gwt.client.local.widgets.SortableTemplatedWidgetTable;

import javax.enterprise.context.ApplicationScoped;

/**
 * Local service responsible for holding application state.
 * 
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ApplicationStateService {

    // ArtifactsPage
	private Integer artifactsPage = 1;
    private ArtifactFilterBean artifactsFilter = new ArtifactFilterBean();
    private String artifactsSearchText = "";
    private SortableTemplatedWidgetTable.SortColumn artifactsSortColumn = null;

    // ArtifactsDetailsPage add relationship
    private String newRelationshipSourceUuid = null;
    private String newRelationshipType = null;

    public Integer getArtifactsPage() {
        return artifactsPage;
    }

    public void setArtifactsPage(Integer artifactsPage) {
        this.artifactsPage = artifactsPage;
    }

    public ArtifactFilterBean getArtifactsFilter() {
        return artifactsFilter;
    }

    public void setArtifactsFilter(ArtifactFilterBean artifactsFilter) {
        this.artifactsFilter = artifactsFilter;
    }

    public String getArtifactsSearchText() {
        return artifactsSearchText;
    }

    public void setArtifactsSearchText(String artifactsSearchText) {
        this.artifactsSearchText = artifactsSearchText;
    }

    public SortableTemplatedWidgetTable.SortColumn getArtifactsSortColumn() {
        return artifactsSortColumn;
    }

    public SortableTemplatedWidgetTable.SortColumn getArtifactsSortColumn(SortableTemplatedWidgetTable.SortColumn defaultValue) {
        return artifactsSortColumn != null ? artifactsSortColumn : defaultValue;
    }

    public void setArtifactsSortColumn(SortableTemplatedWidgetTable.SortColumn artifactsSortColumn) {
        this.artifactsSortColumn = artifactsSortColumn;
    }

    public String getNewRelationshipSourceUuid() {
        return newRelationshipSourceUuid;
    }

    public void setNewRelationshipSourceUuid(String newRelationshipSourceUuid) {
        this.newRelationshipSourceUuid = newRelationshipSourceUuid;
    }

    public String getNewRelationshipType() {
        return newRelationshipType;
    }

    public void setNewRelationshipType(String newRelationshipType) {
        this.newRelationshipType = newRelationshipType;
    }

    public boolean inNewRelationshipMode() {
        return newRelationshipSourceUuid != null && newRelationshipType != null;
    }
}
