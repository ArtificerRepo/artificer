<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:kc="urn:jboss:domain:keycloak:1.0"
    exclude-result-prefixes="kc">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server' or name()='domain']/*[name()='extensions']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="extensions" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <!-- Extension Config -->
      <xsl:element name="extension" namespace="{$currentNS}">
        <xsl:attribute name="module">org.keycloak.keycloak-subsystem</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="profile" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <!-- Subsystem Config -->
      <xsl:element name="subsystem" namespace="urn:jboss:domain:keycloak:1.0"/>
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

