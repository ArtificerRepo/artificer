package org.guvnor.sramp.atom;

import java.net.URI;
import java.net.URISyntaxException;

public class SRAMP_UUID {

    public static URI createRandomUUID() {
        try {
            return new URI("urn:uuid:" + java.util.UUID.randomUUID().toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
