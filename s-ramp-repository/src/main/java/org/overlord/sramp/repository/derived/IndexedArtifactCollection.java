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
package org.overlord.sramp.repository.derived;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.XsdType;

/**
 * A {@link List} implementation that also indexes the WSDL related artifacts
 * added to it.  This index can be used for fast lookup of various types of
 * derived WSDL content.
 *
 * @author eric.wittmann@redhat.com
 */
public class IndexedArtifactCollection extends LinkedList<DerivedArtifactType> {

	private static final long serialVersionUID = 3333444885794949531L;

	private Map<QName, ElementDeclaration> elementIndex = new HashMap<QName, ElementDeclaration>();
	private Map<QName, XsdType> schemaTypeIndex = new HashMap<QName, XsdType>();
	private Map<QName, Message> messageIndex = new HashMap<QName, Message>();

	/**
	 * Constructor.
	 */
	public IndexedArtifactCollection() {
	}

	/**
	 * @see java.util.LinkedList#add(java.lang.Object)
	 */
	@Override
	public boolean add(DerivedArtifactType artifact) {
		indexArtifact(artifact);
		return super.add(artifact);
	}

	/**
	 * Adds the given artifact to the appropriate index.
	 * @param artifact
	 */
	private void indexArtifact(DerivedArtifactType artifact) {
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

}
