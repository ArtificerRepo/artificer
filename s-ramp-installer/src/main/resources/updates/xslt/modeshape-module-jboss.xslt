<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="*[name()='module']/*[name()='dependencies']">
    <xsl:choose>
      <!-- If it already has the jacc dependency, then just copy everything as-is -->
      <xsl:when test="*[name()='module'][@name='javax.security.jacc.api']">
        <xsl:copy>
          <xsl:apply-templates select="@*|node()|text()" />
        </xsl:copy>
      </xsl:when>
      <!-- Otherwise, we need to insert the jacc api dependency -->
      <xsl:otherwise>
        <xsl:variable name="currentNS" select="namespace-uri(.)" />
        <xsl:element name="dependencies" namespace="{$currentNS}">
          <xsl:copy-of select="./node() | ./text()" />
          <xsl:element name="module" namespace="{$currentNS}">
            <xsl:attribute name="name">javax.security.jacc.api</xsl:attribute>
          </xsl:element>
        </xsl:element>
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