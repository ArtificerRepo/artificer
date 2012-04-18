package org.guvnor.sramp.atom.models;

import static org.guvnor.sramp.atom.Constants.*;
import static org.guvnor.sramp.atom.MediaType.*;
import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.app.AppAccept;
import org.jboss.resteasy.plugins.providers.atom.app.AppCategories;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.jboss.resteasy.plugins.providers.atom.app.AppWorkspace;

/**
 * @author kstam
 *
 */
public class CoreModel {
    
    /**
     * Creates the app:workspace object for the Core Model.
     * 
     * @param hrefBase - Reference of the base URL of the server.
     * @return AppWorkspace - app:workspace object representing the Core Model
     */
    public static AppWorkspace getWorkSpace(String hrefBase) {
        
        AppWorkspace workspace = new AppWorkspace();
        workspace.setTitle("Core Model");
        
        //XML Document
        AppCollection xmlDocCollection = new AppCollection(
                (hrefBase + "/s-ramp/core/XmlDocument").replaceAll("//s-ramp","/s-ramp"),
                "XML Documents");
        xmlDocCollection.getAccept().add(new AppAccept(APPLICATION_XML));
        
        AppCategories xmlDocCategories = new AppCategories();
        xmlDocCategories.setFixed(true);
        Category xmlDocCategory = new Category();
        xmlDocCategory.setScheme(URN_X_S_RAMP);
        xmlDocCategory.setTerm(XML_DOCUMENT_TERM);
        xmlDocCategory.setLabel(XML_DOCUMENT);
        xmlDocCategories.getCategory().add(xmlDocCategory);
        
        xmlDocCollection.getCategories().add(xmlDocCategories);
        workspace.getCollection().add(xmlDocCollection);
        
        //TODO Add Document?
        
        //Core Model Objects
        AppCollection coreCollection = new AppCollection(
                (hrefBase + "/s-ramp/core").replaceAll("//s-ramp","/s-ramp"),
                "Core Model Objects");
        coreCollection.getAccept().add(new AppAccept(APPLICATION_ZIP));
        
        AppCategories categories = new AppCategories();
        categories.setFixed(true);
        Category category = new Category();
        category.setScheme(URN_X_S_RAMP);
        category.setTerm(XML_DOCUMENT_TERM);
        category.setLabel(XML_DOCUMENT);
        categories.getCategory().add(category);
        //TODO Document category?
        
        coreCollection.getCategories().add(categories);
        workspace.getCollection().add(coreCollection);
        return workspace;
    }
}
