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
package org.artificer.common;

import javax.xml.namespace.QName;

/**
 * Some S-RAMP constants.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerConstants {

    public static final String DATE_FORMAT  = "EEE, d MMM yyyy HH:mm:ss Z";

	public static final String SRAMP_NS         = "http://docs.oasis-open.org/s-ramp/ns/s-ramp-v1.0";
	public static final String SRAMP_PREFIX     = "s-ramp";
    public static final String ARTIFICER_PREFIX = "artificer";
    // TODO: Not sure if we'll need an actual schema...
    public static final String ARTIFICER_NS     = "http://artificer.jboss.org";

	private static final String SRAMP_CONTENT_SIZE         = "contentSize";
	private static final String SRAMP_CONTENT_TYPE         = "contentType";
    private static final String SRAMP_DERIVED              = "derived";
    private static final String SRAMP_EXTENDED_TYPE        = "extendedType";
    private static final String SRAMP_PROVIDER             = "provider";
    private static final String SRAMP_START_INDEX          = "startIndex";
    private static final String SRAMP_ITEMS_PER_PAGE_INDEX = "itemsPerPage";
    private static final String SRAMP_TOTAL_RESULTS        = "totalResults";

    public static final QName S_RAMP_WRAPPER_ELEM         = new QName(SRAMP_NS, "artifact");

    public static final QName SRAMP_CONTENT_SIZE_QNAME    = new QName(SRAMP_NS, SRAMP_CONTENT_SIZE, SRAMP_PREFIX);
    public static final QName SRAMP_CONTENT_TYPE_QNAME    = new QName(SRAMP_NS, SRAMP_CONTENT_TYPE, SRAMP_PREFIX);
    public static final QName SRAMP_DERIVED_QNAME         = new QName(SRAMP_NS, SRAMP_DERIVED, SRAMP_PREFIX);
    public static final QName SRAMP_PROVIDER_QNAME        = new QName(SRAMP_NS, SRAMP_PROVIDER, SRAMP_PREFIX);
    public static final QName SRAMP_EXTENDED_TYPE_QNAME   = new QName(SRAMP_NS, SRAMP_EXTENDED_TYPE, SRAMP_PREFIX);
    public static final QName SRAMP_START_INDEX_QNAME     = new QName(SRAMP_NS, SRAMP_START_INDEX, SRAMP_PREFIX);
    public static final QName SRAMP_ITEMS_PER_PAGE_QNAME  = new QName(SRAMP_NS, SRAMP_ITEMS_PER_PAGE_INDEX, SRAMP_PREFIX);
    public static final QName SRAMP_TOTAL_RESULTS_QNAME   = new QName(SRAMP_NS, SRAMP_TOTAL_RESULTS, SRAMP_PREFIX);

    public static final String ARTIFICER_RELATIONSHIP_GENERIC = "relationshipGeneric";
    public static final String ARTIFICER_RELATIONSHIP_TYPE = "relationshipType";
    public static final QName ARTIFICER_RELATIONSHIP_GENERIC_QNAME = new QName(ARTIFICER_NS, ARTIFICER_RELATIONSHIP_GENERIC, ARTIFICER_PREFIX);
    public static final QName ARTIFICER_RELATIONSHIP_TYPE_QNAME = new QName(ARTIFICER_NS, ARTIFICER_RELATIONSHIP_TYPE, ARTIFICER_PREFIX);

    // Configuration constants
    public static final String ARTIFICER_CONFIG_FILE_NAME = "artificer.config.file.name";
    public static final String ARTIFICER_CONFIG_FILE_REFRESH = "artificer.config.file.refresh";
    public static final String ARTIFICER_CONFIG_BASEURL = "artificer.config.baseurl";
    public static final String ARTIFICER_CONFIG_AUDITING = "artificer.config.auditing.enabled";
    public static final String ARTIFICER_CONFIG_DERIVED_AUDITING = "artificer.config.auditing.enabled-derived";
    public static final String ARTIFICER_CONFIG_EVENT_JMS_ENABLED = "artificer.config.events.jms.enabled";
    public static final String ARTIFICER_CONFIG_EVENT_JMS_CONNECTIONFACTORY = "artificer.config.events.jms.connectionfactory";
    public static final String ARTIFICER_CONFIG_EVENT_JMS_TOPICS = "artificer.config.events.jms.topics";
    public static final String ARTIFICER_CONFIG_EVENT_JMS_QUEUES = "artificer.config.events.jms.queues";
    public static final String ARTIFICER_CONFIG_REPO_PROVIDER = "artificer.config.repository.provider";
    public static final String ARTIFICER_FILE_STORAGE = "artificer.file.storage";
    public static final String ARTIFICER_FILE_STORAGE_FILESYSTEM_PATH = "artificer.file.storage.filesystem.path";

    // Location of a directory containing JARs which provide custom ArtifactBuilderProviders
    public static final String ARTIFICER_CUSTOM_EXTENSION_DIR = "artificer.extension.customDir";

    public static final String ARTIFICER_CONFIG_MAVEN_READONLY_USERNAME = "artificer.config.maven.readonly-username";

    public static final String ARTIFICER_SNAPSHOT_ALLOWED = "artificer.config.maven.allow-snapshots";

}
