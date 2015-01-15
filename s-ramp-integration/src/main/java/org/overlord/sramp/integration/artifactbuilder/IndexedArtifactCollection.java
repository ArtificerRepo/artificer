/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.integration.artifactbuilder;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType;

/**
 * A {@link List} implementation that indexes the artifacts added to it.  This index can be used for fast lookup of
 * various types of primary and derived artifacts, while building the set prior to persisting.
 *
 * @author eric.wittmann@redhat.com
 */
public class IndexedArtifactCollection extends LinkedList<BaseArtifactType> {

	private static final long serialVersionUID = 3333444885794949531L;

	private Map<QName, ElementDeclaration> elementIndex = new HashMap<QName, ElementDeclaration>();
	private Map<QName, XsdType> schemaTypeIndex = new HashMap<QName, XsdType>();
	private Map<QName, Message> messageIndex = new HashMap<QName, Message>();
	private Map<QName, PortType> portTypeIndex = new HashMap<QName, PortType>();
	private Map<String, Operation> operationIndex = new HashMap<String, Operation>();
	private Map<QName, Binding> bindingIndex = new HashMap<QName, Binding>();
    private Map<String[], XsdDocument> xsdDocumentIndex = new HashMap<String[], XsdDocument>(); // TODO: UGLY
    private Map<String, WsdlDocument> wsdlDocumentIndex = new HashMap<String, WsdlDocument>();

	private QName mostRecentPortType;

	/**
	 * @see java.util.LinkedList#add(java.lang.Object)
	 */
	@Override
	public boolean add(BaseArtifactType artifact) {
	    // Pre-set the UUIDs for all the derived artifacts.  This is useful
        // if something downstream needs to reference them.
        if (StringUtils.isBlank(artifact.getUuid())) {
            artifact.setUuid(UUID.randomUUID().toString());
        }
        
		indexArtifact(artifact);
		
		return super.add(artifact);
	}

	/**
	 * Adds the given artifact to the appropriate index.
	 * @param artifact
	 */
	private void indexArtifact(BaseArtifactType artifact) {
		if (artifact instanceof ElementDeclaration) {
			ElementDeclaration element = (ElementDeclaration) artifact;
			QName key = new QName(element.getNamespace(), element.getNCName());
			elementIndex.put(key, element);
		} else if (artifact instanceof SimpleTypeDeclaration) {
			SimpleTypeDeclaration simpleType = (SimpleTypeDeclaration) artifact;
			QName key = new QName(simpleType.getNamespace(), simpleType.getNCName());
			schemaTypeIndex.put(key, simpleType);
		} else if (artifact instanceof ComplexTypeDeclaration) {
			ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) artifact;
			QName key = new QName(complexType.getNamespace(), complexType.getNCName());
			schemaTypeIndex.put(key, complexType);
		} else if (artifact instanceof Message) {
			Message message = (Message) artifact;
			QName key = new QName(message.getNamespace(), message.getNCName());
			messageIndex.put(key, message);
		} else if (artifact instanceof PortType) {
			PortType portType = (PortType) artifact;
			QName key = new QName(portType.getNamespace(), portType.getNCName());
			portTypeIndex.put(key, portType);
			mostRecentPortType = key;
		} else if (artifact instanceof Operation) {
			Operation operation = (Operation) artifact;
			if (mostRecentPortType != null) {
				String key = mostRecentPortType.toString() + ":" + operation.getNCName(); //$NON-NLS-1$
				operationIndex.put(key, operation);
			}
		} else if (artifact instanceof Binding) {
			Binding binding = (Binding) artifact;
			QName key = new QName(binding.getNamespace(), binding.getNCName());
			bindingIndex.put(key, binding);
		} else if (artifact instanceof XsdDocument) {
            XsdDocument xsdDocument = (XsdDocument) artifact;
            String[] key = new String[] { xsdDocument.getTargetNamespace(), xsdDocument.getName() };
            xsdDocumentIndex.put(key, xsdDocument);
        } else if (artifact instanceof WsdlDocument) {
            WsdlDocument wsdlDocument = (WsdlDocument) artifact;
            wsdlDocumentIndex.put(wsdlDocument.getTargetNamespace(), wsdlDocument);
        }
	}

	/**
	 * Find an element by QName.
	 * @param name
	 */
	public ElementDeclaration lookupElement(QName name) {
		return elementIndex.get(name);
	}

	/**
	 * Find a type (simple or complex) by QName.
	 * @param name
	 */
	public XsdType lookupType(QName name) {
		return schemaTypeIndex.get(name);
	}

	/**
	 * Find a message by QName.
	 * @param name
	 */
	public Message lookupMessage(QName name) {
		return messageIndex.get(name);
	}

	/**
	 * Find a port type by QName.
	 * @param name
	 */
	public PortType lookupPortType(QName name) {
		return portTypeIndex.get(name);
	}

	/**
	 * Find an operation by Port Type and QName.
	 * @param portTypeName
	 * @param operationName
	 */
	public Operation lookupOperation(QName portTypeName, String operationName) {
		String key = mostRecentPortType.toString() + ":" + operationName; //$NON-NLS-1$
		return operationIndex.get(key);
	}

	/**
	 * Find a binding by QName.
	 * @param name
	 */
	public Binding lookupBinding(QName name) {
		return bindingIndex.get(name);
	}

    /**
     * Find an XsdDocument by targetNamespace and filename.
     * @param targetNamespace
     */
    public XsdDocument lookupXsdDocument(String targetNamespace, String filename) {
        String[] key = new String[] { targetNamespace, filename };
        return xsdDocumentIndex.get(key);
    }

    /**
     * Find a WsdlDocument by targetNamespace.
     * @param targetNamespace
     */
    public WsdlDocument lookupWsdlDocument(String targetNamespace) {
        return wsdlDocumentIndex.get(targetNamespace);
    }

}
