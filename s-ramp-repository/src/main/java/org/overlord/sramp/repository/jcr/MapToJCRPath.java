package org.overlord.sramp.repository.jcr;

public class MapToJCRPath {

    private static int folderDepth     = 3;
    private static String PATH         = "/artifact/%1$s/";
    /**
     * "/artifact/<fileExtension>/[btree]"
     * 
     * @param uuid - Universally Unique ID
     * @param type - fileType (xsd, xml etc)
     * @return path: "/artifact/<type>/[btree]"
     */
    public static String getArtifactPath(String uuid, String type) {
        return String.format(PATH, type) + bTreePath(uuid, type);
    }
    /**
     *  * "/s-ramp/<fileExtension>/[btree]"
     * 
     * @param artifactFileName
     * @return path: "/s-ramp/<type>/[btree]"
     */
    public static String getDerivedArtifactPath(String path) {
        return path.replace("artifact", "s-ramp");
    }
    
    private static String bTreePath (String uuid, String type) {
        String bTreePath = "";
        for (int i=0; i < folderDepth; i++) {
            bTreePath += uuid.substring(2*i, 2*i+2) + "/";
        }
        bTreePath += uuid.substring(folderDepth * 2);
        return bTreePath;
    }
    
}
