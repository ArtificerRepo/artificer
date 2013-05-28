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
import java.util.Map;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeTarget;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.visitors.ArtifactVisitorAdapter;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;

/**
 * Linker for WSDL artifacts. This class is responsible for forming links between derived WSDL artifacts and
 * the artifacts they they reference. Ultimately this means forming relationships between derived artifacts
 * and already-stored artifacts referenced by them.  This linker is designed to only pay attention to
 * references that could not be resolved internally to the WSDL.
 *
 * @author eric.wittmann@redhat.com
 */
public class WsdlLinker extends ArtifactVisitorAdapter {

    private static ThreadLocal<LinkerContext> linkerContext = new ThreadLocal<LinkerContext>();

    /**
     * Constructor.
     */
    public WsdlLinker() {
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

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        super.visit(artifact);

        ElementTarget element = artifact.getElement();
        XsdTypeTarget type = artifact.getType();
        BaseArtifactType artifactRef = null;

        if (element != null && element.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = element.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            artifactRef = findArtifact(ArtifactTypeEnum.ElementDeclaration, ref);
            if (artifactRef != null) {
                element.setValue(artifactRef.getUuid());
            } else {
                artifact.setElement(null);
            }
        } else if (type != null && type.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = type.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            artifactRef = findArtifact(ArtifactTypeEnum.ComplexTypeDeclaration, ref);
            if (artifactRef != null) {
                type.setValue(artifactRef.getUuid());
            } else {
                artifactRef = findArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, ref);
                if (artifactRef != null) {
                    type.setValue(artifactRef.getUuid());
                } else {
                    artifact.setType(null);
                }
            }
        }
    }

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
     */
    @Override
    public void visit(OperationInput artifact) {
        super.visit(artifact);

        MessageTarget message = artifact.getMessage();
        if (message != null && message.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = message.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.Message, ref);
            if (artifactRef != null) {
                message.setValue(artifactRef.getUuid());
            } else {
                artifact.setMessage(null);
            }
        }
    }

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
     */
    @Override
    public void visit(OperationOutput artifact) {
        super.visit(artifact);

        MessageTarget message = artifact.getMessage();
        if (message != null && message.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = message.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.Message, ref);
            if (artifactRef != null) {
                message.setValue(artifactRef.getUuid());
            } else {
                artifact.setMessage(null);
            }
        }
    }

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
     */
    @Override
    public void visit(Fault artifact) {
        super.visit(artifact);

        MessageTarget message = artifact.getMessage();
        if (message != null && message.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = message.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.Message, ref);
            if (artifactRef != null) {
                message.setValue(artifactRef.getUuid());
            } else {
                artifact.setMessage(null);
            }
        }
    }

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
     */
    @Override
    public void visit(Binding artifact) {
        super.visit(artifact);

        PortTypeTarget portType = artifact.getPortType();
        if (portType != null && portType.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = portType.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.PortType, ref);
            if (artifactRef != null) {
                portType.setValue(artifactRef.getUuid());
            } else {
                artifact.setPortType(null);
            }
        }
    }

    /**
     * @see org.overlord.sramp.common.visitors.ArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
     */
    @Override
    public void visit(Port artifact) {
        super.visit(artifact);

        BindingTarget binding = artifact.getBinding();
        if (binding != null && binding.getOtherAttributes().containsKey(WsdlDeriver.UNRESOLVED_REF)) {
            String encodedRef = binding.getOtherAttributes().remove(WsdlDeriver.UNRESOLVED_REF);
            QName ref = QName.valueOf(encodedRef);
            BaseArtifactType artifactRef = findArtifact(ArtifactTypeEnum.Binding, ref);
            if (artifactRef != null) {
                binding.setValue(artifactRef.getUuid());
            } else {
                artifact.setBinding(null);
            }
        }
    }

    /**
     * Uses the linker context to search for an artifact
     * @param artifactType
     * @param reference
     */
    private BaseArtifactType findArtifact(ArtifactTypeEnum artifactType, QName reference) {
        LinkerContext lcontext = linkerContext.get();
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put("namespace", reference.getNamespaceURI());
        criteria.put("ncName", reference.getLocalPart());
        Collection<BaseArtifactType> artifacts = lcontext.findArtifacts(artifactType.getModel(), artifactType.getType(), criteria);
        if (artifacts != null && !artifacts.isEmpty()) {
            // TODO need a more interesting way to dis-ambiguate the results
            return artifacts.iterator().next();
        } else {
            return null;
        }
    }

}
