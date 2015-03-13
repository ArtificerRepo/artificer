package org.artificer.ui.client.local.pages.artifacts;

import org.artificer.ui.client.local.util.UploadResult;

/**
 * The {@link org.artificer.ui.server.servlets.ArtifactUploadServlet} returns a JSON map as the response.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUploadResult extends UploadResult {

    /**
     * Constructor.
     */
    protected ArtifactUploadResult() {
    }

    /**
     * Convert the string returned by the {@link org.artificer.ui.server.servlets.ArtifactUploadServlet} into JSON and
     * then from there into an {@link ArtifactUploadResult} bean.
     * @param resultData
     */
    public static ArtifactUploadResult fromResult(String resultData) {
        int startIdx = resultData.indexOf('{');
        int endIdx = resultData.lastIndexOf('}') + 1;
        resultData = "(" + resultData.substring(startIdx, endIdx) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        return fromJSON(ArtifactUploadResult.class, resultData);
    }

    /**
     * @return the uuid
     */
    public final String getUuid() {
        return get("uuid"); //$NON-NLS-1$
    }

    /**
     * @return the model
     */
    public final String getModel() {
        return get("model"); //$NON-NLS-1$
    }

    /**
     * @return the type
     */
    public final String getType() {
        return get("type"); //$NON-NLS-1$
    }

    /**
     * @return true if the response is due to a s-ramp package upload
     */
    public final boolean isBatch() {
        return "true".equals(get("batch")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * @return the total number of items in the s-ramp package
     */
    public final int getBatchTotal() {
        return new Integer(get("batchTotal")); //$NON-NLS-1$
    }

    /**
     * @return the number of successful items in the package
     */
    public final int getBatchNumSuccess() {
        return new Integer(get("batchNumSuccess")); //$NON-NLS-1$
    }

    /**
     * @return the number of failed items in the package
     */
    public final int getBatchNumFailed() {
        return new Integer(get("batchNumFailed")); //$NON-NLS-1$
    }

}
