/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.common.derived;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocumentTarget;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.visitors.ArtifactVisitorAdapter;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;

/**
 * Linker for XSD artifacts. This class is responsible for forming links between derived XSD artifacts and
 * the artifacts they they reference. Ultimately this means forming relationships between derived artifacts
 * and already-stored artifacts referenced by them.  This linker is designed to only pay attention to
 * references that could not be resolved internally to the XSD.
 *
 * @author Brett Meyer
 */
public class XsdLinker extends ArtifactVisitorAdapter {

    private static ThreadLocal<LinkerContext> linkerContext = new ThreadLocal<LinkerContext>();

    /**
     * Constructor.
     */
    public XsdLinker() {
    }

    /**
     * Resolves any missing references found on the given derived artifact.
     * @param context
     * @param derivedArtifact
     */
    public void link(LinkerContext context, BaseArtifactType derivedArtifact) {
        linkerContext.set(context);
        ArtifactVisitorHelper.visitArtifact(this, derivedArtifact);
    }

    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);

        visitXsdImports(artifact.getImportedXsds());
    }
    
    protected void visitXsdImports(List<XsdDocumentTarget> targetCollection) {
        Iterator<XsdDocumentTarget> itr = targetCollection.iterator();
        while (itr.hasNext()) {
            XsdDocumentTarget xsdDocumentTarget = itr.next();
            if (xsdDocumentTarget.getOtherAttributes().containsKey(XsdDeriver.UNRESOLVED_REF)) {
                String targetNamespace = xsdDocumentTarget.getOtherAttributes().remove(XsdDeriver.UNRESOLVED_REF);
                
                Map<String, String> criteria = new HashMap<String, String>();
                criteria.put("targetNamespace", targetNamespace);
                BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.XsdDocument, criteria);
                
                if (artifactRef != null) {
                    xsdDocumentTarget.setValue(artifactRef.getUuid());
                } else {
                    itr.remove();
                }
            }
        }
    }
    
    protected BaseArtifactType findArtifact(ArtifactTypeEnum artifactType, Map<String, String> criteria) {
        LinkerContext lcontext = linkerContext.get();
        Collection<BaseArtifactType> artifacts = lcontext.findArtifacts(artifactType.getModel(), artifactType.getType(), criteria);
        if (artifacts != null && !artifacts.isEmpty()) {
            // TODO need a more interesting way to dis-ambiguate the results
            return artifacts.iterator().next();
        } else {
            return null;
        }
    }

}
