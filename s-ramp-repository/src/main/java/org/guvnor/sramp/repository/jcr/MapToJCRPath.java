package org.guvnor.sramp.repository.jcr;

import org.guvnor.sramp.repository.UnsupportedFiletypeException;

public class MapToJCRPath {

    /**
     * "/artifact/<fileExtension>/<fileName>"
     * 
     * @param artifactFileName
     * @return
     * @throws UnsupportedFiletypeException 
     */
    public static String getArtifactPath(String artifactFileName) throws UnsupportedFiletypeException {
        String fileExtension = getFileExtension(artifactFileName);
        String path = "/artifact/" + fileExtension + "/"+ artifactFileName;
        return path;
    }
    /**
     * "/s-ramp/<model>/<artifactType>/<fileName>"
     * 
     * @param artifactFileName
     * @return
     * @throws UnsupportedFiletypeException 
     */
    protected static String getDerivedArtifactPath(String artifactFileName) throws UnsupportedFiletypeException {
        String fileExtention = getFileExtension(artifactFileName);
        String[] pathElements = getModel(fileExtention);
        String path = "/s-ramp/" + pathElements[0] + "/" + pathElements[1] + "/"+ artifactFileName;
        return path;
    }
    
    protected static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".")+1, fileName.length()).toLowerCase();
    }
    
    protected static String[] getModel(String fileExtention) throws UnsupportedFiletypeException {
        if ("xsd".equals(fileExtention)) return new String[] {"xsd","XsdDocument"};
        else if ("xml".equals(fileExtention)) return new String[] {"xml","XmlDocument"};
        else throw new UnsupportedFiletypeException("File extention '" + fileExtention + "' is not yet supported.");
    }
}
