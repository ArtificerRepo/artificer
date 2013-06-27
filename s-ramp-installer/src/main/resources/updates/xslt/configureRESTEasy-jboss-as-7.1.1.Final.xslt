<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:as="urn:jboss:domain:1.2"
  xmlns="urn:jboss:domain:1.2" xmlns:inf="urn:jboss:domain:infinispan:1.2"
  xmlns:log="urn:jboss:domain:logging:1.1" exclude-result-prefixes="log inf xalan as sd" 
  xmlns:sd="urn:jboss:domain:security:1.1" xmlns:xalan="http://xml.apache.org/xalan" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="as:server/as:profile/log:subsystem">
        <subsystem xmlns="urn:jboss:domain:logging:1.1">
            <xsl:apply-templates select="@* | *" />
            <logger category="org.jboss.resteasy">
                <level name="ERROR"/>
            </logger>
        </subsystem>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>