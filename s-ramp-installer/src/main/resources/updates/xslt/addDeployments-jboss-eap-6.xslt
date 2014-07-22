<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:od="urn:jboss:domain:overlord-deployment:1.0"
    exclude-result-prefixes="od">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']/od:subsystem/od:deployments">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="deployments" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <xsl:element name="deployment" namespace="{$currentNS}">
        <xsl:attribute name="name">s-ramp-server.war</xsl:attribute>
        <xsl:attribute name="module">org.overlord.sramp.eap.deployments</xsl:attribute>
      </xsl:element>
      <xsl:element name="deployment" namespace="{$currentNS}">
        <xsl:attribute name="name">s-ramp-ui.war</xsl:attribute>
        <xsl:attribute name="module">org.overlord.sramp.eap.deployments</xsl:attribute>
      </xsl:element>
      <xsl:element name="deployment" namespace="{$currentNS}">
        <xsl:attribute name="name">sramp-ds.xml</xsl:attribute>
        <xsl:attribute name="module">org.overlord.sramp.eap.deployments</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
