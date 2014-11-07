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
package org.overlord.sramp.common.derived;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Base class for all derivers that are based on XML documents.  This base class
 * does some of the boilerplate such as parsing the document.  Child classes are
 * responsible for processing the resulting document in interesting ways.
 * 
 * @deprecated See {@link AbstractXmlArtifactBuilder}
 *
 * @author eric.wittmann@redhat.com
 */
@Deprecated
public abstract class AbstractXmlDeriver implements ArtifactDeriver {

	/**
	 * Constructor.
	 */
	public AbstractXmlDeriver() {
	}

	/**
	 * @see org.overlord.sramp.common.repository.derived.ArtifactDeriver#derive(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.io.InputStream)
	 */
	@Override
	public Collection<BaseArtifactType> derive(BaseArtifactType artifact, InputStream content) throws IOException {
	    Collection<BaseArtifactType> derivedArtifacts = createDerivedArtifactCollection();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); //$NON-NLS-1$
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(content);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			StaticNamespaceContext nsCtx = new StaticNamespaceContext();
			configureNamespaceMappings(nsCtx);
			xpath.setNamespaceContext(nsCtx);

			Element rootElement = document.getDocumentElement();
			XmlDeriverContext context = new XmlDeriverContext(rootElement, xpath);
			derive(derivedArtifacts, artifact, context);

			// Set the relatedDocument relationship for all derived artifacts
			for (BaseArtifactType derivedArtifact : derivedArtifacts) {
			    if (derivedArtifact instanceof DerivedArtifactType) {
			        DerivedArtifactType dat = (DerivedArtifactType) derivedArtifact;
			        if (dat.getRelatedDocument() == null) {
			            DocumentArtifactTarget related = new DocumentArtifactTarget();
			            related.setValue(artifact.getUuid());
			            related.setArtifactType(DocumentArtifactEnum.fromValue(artifact.getArtifactType()));
			            dat.setRelatedDocument(related);
			        }
			    } else {
			        Relationship genericRelationship = SrampModelUtils.getGenericRelationship(derivedArtifact, "relatedDocument"); //$NON-NLS-1$
			        if (genericRelationship == null) {
			            SrampModelUtils.addGenericRelationship(derivedArtifact, "relatedDocument", artifact.getUuid()); //$NON-NLS-1$
			        }
			    }
			}
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}

		return derivedArtifacts;
	}

    /**
     * Sub-classes could provide an alternate collection here.  For example, the {@link WsdlDeriver}
     * creates an indexed artifact collection so that it can reference derived artifacts and make
     * connections/relationships.
     */
    protected Collection<BaseArtifactType> createDerivedArtifactCollection() {
        return new ArrayList<BaseArtifactType>();
    }

	/**
	 * Performs an x-query against the given context node.
	 * @param xpath
	 * @param query
	 * @param returnType
	 * @throws XPathExpressionException
	 */
	protected Object query(XPath xpath, Element context, String query, QName returnType) throws XPathExpressionException {
		XPathExpression expr = xpath.compile(query);
		return expr.evaluate(context, returnType);
	}

	/**
	 * Derives content for the given artifact.
	 * @param derivedArtifacts
	 * @param artifact
	 * @param xmlDeriverContext
	 * @throws IOException
	 */
	protected abstract void derive(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType artifact, XmlDeriverContext xmlDeriverContext) throws IOException;

	/**
	 * Configures the namespace mappings that will be available when executing XPath
	 * queries.
	 * @param namespaceContext
	 */
	protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
	}

	/**
	 * A context passed to the xml deriver's derive method.
	 * @author eric.wittmann@redhat.com
	 */
    public static class XmlDeriverContext {
        private Element rootElement;
        private XPath xpath;

        /**
         * Constructor.
         * @param rootElement
         * @param xpath
         */
        public XmlDeriverContext(Element rootElement, XPath xpath) {
            this.rootElement = rootElement;
            this.xpath = xpath;
        }

        /**
         * Adds a namespace mapping to the current namespace context.  Useful if not all namespaces
         * in the document being derived are known up-front.
         * @param prefix
         * @param namespaceURI
         */
        public void addNamespaceMapping(String prefix, String namespaceURI) {
            ((StaticNamespaceContext) xpath.getNamespaceContext()).addMapping(prefix, namespaceURI);
        }

        /**
         * @return the rootElement
         */
        public Element getRootElement() {
            return rootElement;
        }

        /**
         * @param rootElement the rootElement to set
         */
        public void setRootElement(Element rootElement) {
            this.rootElement = rootElement;
        }

        /**
         * @return the xpath
         */
        public XPath getXpath() {
            return xpath;
        }

        /**
         * @param xpath the xpath to set
         */
        public void setXpath(XPath xpath) {
            this.xpath = xpath;
        }

    }

}
