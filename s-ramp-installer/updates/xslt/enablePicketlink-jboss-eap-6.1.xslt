<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns="urn:jboss:domain:1.4" xmlns:as="urn:jboss:domain:1.4"
  xmlns:pl="urn:jboss:picketlink:1.0" xmlns:xalan="http://xml.apache.org/xalan" 
  exclude-result-prefixes="pl xalan as" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="as:server/as:extensions[not(as:extension[@module='org.picketlink'])]">
    <extensions>
      <xsl:apply-templates select="@* | *" />
      <extension module="org.picketlink"/>
    </extensions>
  </xsl:template>

  <xsl:template match="as:profile[not(pl:subsystem)]">
    <profile>
      <subsystem xmlns="urn:jboss:picketlink:1.0" />
      <xsl:apply-templates select="@* | *" />
    </profile>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>