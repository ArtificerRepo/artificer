/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.repository.hibernate.entity;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Entity
@Indexed
@Analyzer(impl = StandardAnalyzer.class)
public class ArtificerWsdlDocumentArtifact extends ArtificerDocumentArtifact {

    private List<ArtificerArtifact> elements = new ArrayList<>();

    private List<ArtificerArtifact> attributes = new ArrayList<>();

    private List<ArtificerArtifact> simpleTypes = new ArrayList<>();

    private List<ArtificerArtifact> complexTypes = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> services = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> ports = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> parts = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> messages = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> faults = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> portTypes = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> operations = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> operationInputs = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> operationOutputs = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> bindings = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> bindingOperations = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> bindingOperationInputs = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> bindingOperationOutputs = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> bindingOperationFaults = new ArrayList<>();

    private List<ArtificerWsdlDerivedArtifact> extensions = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerArtifact> getElements() {
        return elements;
    }

    public void setElements(List<ArtificerArtifact> elements) {
        this.elements = elements;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerArtifact> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ArtificerArtifact> attributes) {
        this.attributes = attributes;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerArtifact> getSimpleTypes() {
        return simpleTypes;
    }

    public void setSimpleTypes(List<ArtificerArtifact> simpleTypes) {
        this.simpleTypes = simpleTypes;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerArtifact> getComplexTypes() {
        return complexTypes;
    }

    public void setComplexTypes(List<ArtificerArtifact> complexTypes) {
        this.complexTypes = complexTypes;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getServices() {
        return services;
    }

    public void setServices(List<ArtificerWsdlDerivedArtifact> services) {
        this.services = services;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getPorts() {
        return ports;
    }

    public void setPorts(List<ArtificerWsdlDerivedArtifact> ports) {
        this.ports = ports;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getParts() {
        return parts;
    }

    public void setParts(List<ArtificerWsdlDerivedArtifact> parts) {
        this.parts = parts;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getMessages() {
        return messages;
    }

    public void setMessages(List<ArtificerWsdlDerivedArtifact> messages) {
        this.messages = messages;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getFaults() {
        return faults;
    }

    public void setFaults(List<ArtificerWsdlDerivedArtifact> faults) {
        this.faults = faults;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getPortTypes() {
        return portTypes;
    }

    public void setPortTypes(List<ArtificerWsdlDerivedArtifact> portTypes) {
        this.portTypes = portTypes;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getOperations() {
        return operations;
    }

    public void setOperations(List<ArtificerWsdlDerivedArtifact> operations) {
        this.operations = operations;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getOperationInputs() {
        return operationInputs;
    }

    public void setOperationInputs(List<ArtificerWsdlDerivedArtifact> operationInputs) {
        this.operationInputs = operationInputs;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getOperationOutputs() {
        return operationOutputs;
    }

    public void setOperationOutputs(List<ArtificerWsdlDerivedArtifact> operationOutputs) {
        this.operationOutputs = operationOutputs;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getBindings() {
        return bindings;
    }

    public void setBindings(List<ArtificerWsdlDerivedArtifact> bindings) {
        this.bindings = bindings;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getBindingOperations() {
        return bindingOperations;
    }

    public void setBindingOperations(List<ArtificerWsdlDerivedArtifact> bindingOperations) {
        this.bindingOperations = bindingOperations;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getBindingOperationInputs() {
        return bindingOperationInputs;
    }

    public void setBindingOperationInputs(List<ArtificerWsdlDerivedArtifact> bindingOperationInputs) {
        this.bindingOperationInputs = bindingOperationInputs;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getBindingOperationOutputs() {
        return bindingOperationOutputs;
    }

    public void setBindingOperationOutputs(List<ArtificerWsdlDerivedArtifact> bindingOperationOutputs) {
        this.bindingOperationOutputs = bindingOperationOutputs;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getBindingOperationFaults() {
        return bindingOperationFaults;
    }

    public void setBindingOperationFaults(List<ArtificerWsdlDerivedArtifact> bindingOperationFaults) {
        this.bindingOperationFaults = bindingOperationFaults;
    }

    @OneToMany(cascade = CascadeType.ALL)
    public List<ArtificerWsdlDerivedArtifact> getExtensions() {
        return extensions;
    }

    public void setExtensions(List<ArtificerWsdlDerivedArtifact> extensions) {
        this.extensions = extensions;
    }
}
