<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:as="urn:jboss:domain:1.2"
  xmlns="urn:jboss:domain:1.2" xmlns:inf="urn:jboss:domain:infinispan:1.2"
  xmlns:ds="urn:jboss:domain:datasources:1.0" xmlns:scan="urn:jboss:domain:deployment-scanner:1.1"
  xmlns:sd="urn:jboss:domain:security:1.1" xmlns:xalan="http://xml.apache.org/xalan" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="as:profile/ds:subsystem/ds:datasources/ds:datasource[@jndi-name='java:jboss/datasources/jbpmDS']/ds:connection-url">
                    <connection-url>jdbc:h2:${jboss.server.data.dir}/jbpm5/jbpm</connection-url>
  </xsl:template>

  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>