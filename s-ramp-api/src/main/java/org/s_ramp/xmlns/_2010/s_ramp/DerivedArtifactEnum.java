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
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.01.17 at 01:09:37 PM EST 
//


package org.s_ramp.xmlns._2010.s_ramp;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for derivedArtifactEnum.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="derivedArtifactEnum">
 *   &lt;restriction base="{http://s-ramp.org/xmlns/2010/s-ramp}baseArtifactEnum">
 *     &lt;enumeration value="DerivedArtifactType"/>
 *     &lt;enumeration value="PolicyAttachment"/>
 *     &lt;enumeration value="PolicyExpression"/>
 *     &lt;enumeration value="AttributeDeclaration"/>
 *     &lt;enumeration value="ElementDeclaration"/>
 *     &lt;enumeration value="XsdType"/>
 *     &lt;enumeration value="ComplexTypeDeclaration"/>
 *     &lt;enumeration value="SimpleTypeDeclaration"/>
 *     &lt;enumeration value="WsdlDerivedArtifactType"/>
 *     &lt;enumeration value="NamedWsdlDerivedArtifactType"/>
 *     &lt;enumeration value="WsdlService"/>
 *     &lt;enumeration value="Port"/>
 *     &lt;enumeration value="Binding"/>
 *     &lt;enumeration value="PortType"/>
 *     &lt;enumeration value="BindingOperation"/>
 *     &lt;enumeration value="BindingOperationInput"/>
 *     &lt;enumeration value="BindingOperationFault"/>
 *     &lt;enumeration value="Operation"/>
 *     &lt;enumeration value="OperationInput"/>
 *     &lt;enumeration value="Fault"/>
 *     &lt;enumeration value="Message"/>
 *     &lt;enumeration value="Part"/>
 *     &lt;enumeration value="BindingOperationOutput"/>
 *     &lt;enumeration value="OperationOutput"/>
 *     &lt;enumeration value="WsdlExtension"/>
 *     &lt;enumeration value="SoapAddress"/>
 *     &lt;enumeration value="SoapBinding"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "derivedArtifactEnum")
@XmlEnum(BaseArtifactEnum.class)
public enum DerivedArtifactEnum {

    @XmlEnumValue("DerivedArtifactType")
    DERIVED_ARTIFACT_TYPE(BaseArtifactEnum.DERIVED_ARTIFACT_TYPE),
    @XmlEnumValue("PolicyAttachment")
    POLICY_ATTACHMENT(BaseArtifactEnum.POLICY_ATTACHMENT),
    @XmlEnumValue("PolicyExpression")
    POLICY_EXPRESSION(BaseArtifactEnum.POLICY_EXPRESSION),
    @XmlEnumValue("AttributeDeclaration")
    ATTRIBUTE_DECLARATION(BaseArtifactEnum.ATTRIBUTE_DECLARATION),
    @XmlEnumValue("ElementDeclaration")
    ELEMENT_DECLARATION(BaseArtifactEnum.ELEMENT_DECLARATION),
    @XmlEnumValue("XsdType")
    XSD_TYPE(BaseArtifactEnum.XSD_TYPE),
    @XmlEnumValue("ComplexTypeDeclaration")
    COMPLEX_TYPE_DECLARATION(BaseArtifactEnum.COMPLEX_TYPE_DECLARATION),
    @XmlEnumValue("SimpleTypeDeclaration")
    SIMPLE_TYPE_DECLARATION(BaseArtifactEnum.SIMPLE_TYPE_DECLARATION),
    @XmlEnumValue("WsdlDerivedArtifactType")
    WSDL_DERIVED_ARTIFACT_TYPE(BaseArtifactEnum.WSDL_DERIVED_ARTIFACT_TYPE),
    @XmlEnumValue("NamedWsdlDerivedArtifactType")
    NAMED_WSDL_DERIVED_ARTIFACT_TYPE(BaseArtifactEnum.NAMED_WSDL_DERIVED_ARTIFACT_TYPE),
    @XmlEnumValue("WsdlService")
    WSDL_SERVICE(BaseArtifactEnum.WSDL_SERVICE),
    @XmlEnumValue("Port")
    PORT(BaseArtifactEnum.PORT),
    @XmlEnumValue("Binding")
    BINDING(BaseArtifactEnum.BINDING),
    @XmlEnumValue("PortType")
    PORT_TYPE(BaseArtifactEnum.PORT_TYPE),
    @XmlEnumValue("BindingOperation")
    BINDING_OPERATION(BaseArtifactEnum.BINDING_OPERATION),
    @XmlEnumValue("BindingOperationInput")
    BINDING_OPERATION_INPUT(BaseArtifactEnum.BINDING_OPERATION_INPUT),
    @XmlEnumValue("BindingOperationFault")
    BINDING_OPERATION_FAULT(BaseArtifactEnum.BINDING_OPERATION_FAULT),
    @XmlEnumValue("Operation")
    OPERATION(BaseArtifactEnum.OPERATION),
    @XmlEnumValue("OperationInput")
    OPERATION_INPUT(BaseArtifactEnum.OPERATION_INPUT),
    @XmlEnumValue("Fault")
    FAULT(BaseArtifactEnum.FAULT),
    @XmlEnumValue("Message")
    MESSAGE(BaseArtifactEnum.MESSAGE),
    @XmlEnumValue("Part")
    PART(BaseArtifactEnum.PART),
    @XmlEnumValue("BindingOperationOutput")
    BINDING_OPERATION_OUTPUT(BaseArtifactEnum.BINDING_OPERATION_OUTPUT),
    @XmlEnumValue("OperationOutput")
    OPERATION_OUTPUT(BaseArtifactEnum.OPERATION_OUTPUT),
    @XmlEnumValue("WsdlExtension")
    WSDL_EXTENSION(BaseArtifactEnum.WSDL_EXTENSION),
    @XmlEnumValue("SoapAddress")
    SOAP_ADDRESS(BaseArtifactEnum.SOAP_ADDRESS),
    @XmlEnumValue("SoapBinding")
    SOAP_BINDING(BaseArtifactEnum.SOAP_BINDING);
    private final BaseArtifactEnum value;

    DerivedArtifactEnum(BaseArtifactEnum v) {
        value = v;
    }

    public BaseArtifactEnum value() {
        return value;
    }

    public static DerivedArtifactEnum fromValue(BaseArtifactEnum v) {
        for (DerivedArtifactEnum c: DerivedArtifactEnum.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v.toString());
    }

}
