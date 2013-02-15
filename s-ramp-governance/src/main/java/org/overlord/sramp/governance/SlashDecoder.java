package org.overlord.sramp.governance;

import java.io.UnsupportedEncodingException;

public class SlashDecoder {

    /**
     * Replacing all *2F by a forward slash '/'.
     * 
     * @param param
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String decode(String param) throws UnsupportedEncodingException {
        return param.replaceAll("\\*2F", "/");
    }
}
