package org.overlord.sramp.ui.client.local.pages.ontologies;

import org.overlord.sramp.ui.client.shared.beans.OntologySummaryBean;

public class OntologiesUtil {
    /**
     * Creates a label to use in the lsit of ontologies.
     *
     * @param ontology
     */
    public static String createOntologyLabel(final OntologySummaryBean ontology) {
        String label = ontology.getBase();
        if (ontology.getLabel() != null && ontology.getLabel().trim().length() > 0) {
            label += " (" + ontology.getLabel() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return label;
    }
}
