<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:mod="urn:jboss:module:1.3"
  xmlns="urn:jboss:module:1.3"
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="mod xalan"
  version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="mod:module/mod:dependencies">
    <xsl:choose>
      <!-- If it already has the jacc dependency, then just copy everything as-is -->
      <xsl:when test="mod:module[@name='javax.security.jacc.api']">
        <xsl:copy>
          <xsl:apply-templates select="@*|node()|text()" />
        </xsl:copy>
      </xsl:when>
      <!-- Otherwise, we need to insert the jacc api dependency -->
      <xsl:otherwise>
        <dependencies>
          <xsl:copy-of select="./node() | ./text()" />
          <module name="javax.security.jacc.api" />
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