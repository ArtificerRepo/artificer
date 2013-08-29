<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:mod="urn:jboss:module:1.1"
  xmlns="urn:jboss:module:1.1" xmlns:xalan="http://xml.apache.org/xalan" exclude-result-prefixes="mod xalan"
  version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="mod:module/mod:dependencies">
    <xsl:choose>
      <!-- If it already has the commons io dependency, then just copy everything as-is -->
      <xsl:when test="mod:module[@name='org.apache.commons.io']">
        <xsl:copy>
          <xsl:apply-templates select="@*|node()|text()" />
        </xsl:copy>
      </xsl:when>
      <!-- Otherwise, we need to insert the commons io dependency -->
      <xsl:otherwise>
        <dependencies>
          <xsl:copy-of select="./node() | ./text()" />
        <module name="org.apache.commons.io" />
        </dependencies>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>