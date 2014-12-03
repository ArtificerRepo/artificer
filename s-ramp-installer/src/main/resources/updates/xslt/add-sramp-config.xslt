<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:kc="urn:jboss:domain:keycloak:1.0"
    exclude-result-prefixes="kc">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']/kc:subsystem">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="subsystem" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <!-- Secure Deployment Config -->
      <xsl:if test="not(kc:secure-deployment[@name='s-ramp-ui.war'])">
          <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.0">
            <xsl:attribute name="name">s-ramp-ui.war</xsl:attribute>
            <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.0">governance</xsl:element>
            <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.0">s-ramp-ui</xsl:element>
            <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.0">
                <xsl:attribute name="name">secret</xsl:attribute>
                password
            </xsl:element>
          </xsl:element>
      </xsl:if>
      <xsl:if test="not(kc:secure-deployment[@name='s-ramp-server.war'])">
          <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.0">
            <xsl:attribute name="name">s-ramp-server.war</xsl:attribute>
            <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.0">governance</xsl:element>
            <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.0">s-ramp-server</xsl:element>
            <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.0">
                <xsl:attribute name="name">secret</xsl:attribute>
                password
            </xsl:element>
            <xsl:element name="enable-basic-auth" namespace="urn:jboss:domain:keycloak:1.0">true</xsl:element>
          </xsl:element>
      </xsl:if>
    </xsl:element>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

