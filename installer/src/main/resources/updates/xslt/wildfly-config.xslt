<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    >

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="*[name()='profile']/*[name()='subsystem']/*[name()='server']/*[name()='http-listener'][@name = 'default']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="http-listener" namespace="{$currentNS}">
      <xsl:apply-templates select="@*|node()|text()" />
      <!-- No limit to POST file size -->
      <xsl:attribute name="max-post-size">-1</xsl:attribute>
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

