package org.overlord.sramp.govenance;

import java.io.File;
import java.io.FileInputStream;

public class Multipart 
{
    StringBuilder text;
    static String CRLF = "\r\n";
    String boundary;
    public Multipart()
    {
        text = new StringBuilder();
        boundary = Long.toHexString(
            System.currentTimeMillis()); 
    }
    public String getContent()
    {
        return text.toString();
    }
    public String getBoundary()
    {
        return boundary;
    }
    public int getLength()
    {
        return text.length();
    }
    public void putStandardParam( String name, 
        String value, String encoding )
    {
        StringBuilder sb = new StringBuilder();
        sb.append("--" + boundary).append(CRLF);
        sb.append("Content-Disposition: form-data; "
            +"name=\""+name+"\"");
        sb.append(CRLF);
        sb.append("Content-Type: text/plain; charset=" 
            + encoding );
        sb.append(CRLF);
        sb.append(CRLF);
        sb.append(value);
        sb.append(CRLF);
        text.append( sb.toString() );
    }
    public void putBinaryFileParam( String name, 
        String fileName, String mimeType, 
        String encoding ) throws Exception
    {
        // compose the header
        StringBuilder sb = new StringBuilder();
        sb.append( "--"+boundary );
        sb.append( CRLF );
        sb.append("content-disposition: form-data; "
            +"name=\"" );
        sb.append( name );
        sb.append( "\";  filename=\"");
        sb.append( fileName );
        sb.append( "\"" );
        sb.append( CRLF );
        sb.append("Content-Type: "+mimeType ); 
        sb.append( CRLF );
        sb.append("Content-Transfer-Encoding: binary");
        sb.append( CRLF ); // need two of these
        sb.append( CRLF );
        text.append( sb.toString() );
        // now for the file
        File input = new File( fileName );
        FileInputStream fis = new FileInputStream(input);
        byte[] data = new byte[(int)input.length()];
        fis.read( data );
        fis.close();
        text.append( new String(data,encoding) );
        text.append( CRLF );
    }
    public void finish()
    {
        text.append( "--" );
        text.append( boundary );
        text.append( "--" );
        text.append( CRLF );
    }
}
