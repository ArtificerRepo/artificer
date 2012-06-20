/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.atom.models;

import static org.overlord.sramp.atom.Constants.*;
import static org.overlord.sramp.atom.MediaType.*;

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
        xmlDocCollection.getAccept().add(new AppAccept(APPLICATION_ATOM_XML_ENTRY));
        
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
