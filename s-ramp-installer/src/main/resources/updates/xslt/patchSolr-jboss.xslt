<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to patch org.apache.solr:3.6.2 module.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:m="urn:jboss:module:1.2"
  xmlns="urn:jboss:module:1.2" xmlns:xalan="http://xml.apache.org/xalan" 
  exclude-result-prefixes="xalan m" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="m:dependencies/m:module[@name='org.apache.lucene' and not(@slot)]">
        <module name="org.apache.lucene" slot="3.6.2" />
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>