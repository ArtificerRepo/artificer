package org.artificer.atom.providers;

import org.apache.commons.io.IOUtils;
import org.artificer.atom.i18n.Messages;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerServerException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

/**
 * @author Brett Meyer
 */
public class AbstractArtificerExceptionProvider {

    public Response toResponse(ArtificerServerException exception, Response.Status responseStatus, String mediaType) {
        Response.ResponseBuilder builder = Response.status(responseStatus);
        builder.header("Error-Message", getRootCause(exception).getMessage());
        builder.type(mediaType);
        String stack = getRootStackTrace(exception);
        builder.entity(stack);
        return builder.build();
    }

    public void writeTo(ArtificerException error, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        if (httpHeaders != null)
            httpHeaders.putSingle("Error-Message", getRootCause(error).getMessage());
        String stack = getRootStackTrace(error);
        entityStream.write(stack.getBytes("UTF-8"));
        entityStream.flush();
    }

    protected String getStacktrace(InputStream entityStream) throws IOException {
        List<String> lines = IOUtils.readLines(entityStream);
        StringBuilder buffer = new StringBuilder();
        for (String line : lines) {
            buffer.append(line).append("\n");
        }
        return buffer.toString();
    }

    protected String getMessage(MultivaluedMap<String, String> httpHeaders) {
        String msg = httpHeaders == null ? null : httpHeaders.getFirst("Error-Message");
        if (msg == null) {
            msg = Messages.i18n.format("UNKNOWN_ARTIFICER_ERROR");
        }
        return msg;
    }

    /**
     * Gets the root stack trace as a string.
     * @param t
     */
    public static String getRootStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        getRootCause(t).printStackTrace(writer);
        return sw.getBuffer().toString();
    }

    /**
     * Gets the root exception from the given {@link Throwable}.
     * @param t
     */
    public static Throwable getRootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root)
            root = root.getCause();
        return root;
    }
}
