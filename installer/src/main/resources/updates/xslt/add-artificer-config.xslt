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
      <xsl:if test="not(kc:secure-deployment[@name='artificer-ui.war'])">
          <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.0">
            <xsl:attribute name="name">artificer-ui.war</xsl:attribute>
            <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.0">artificer</xsl:element>
            <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.0">artificer-ui</xsl:element>
            <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.0">
                <xsl:attribute name="name">secret</xsl:attribute>
                2b0ad840-ab4d-11e4-bcd8-0800200c9a66
            </xsl:element>
          </xsl:element>
      </xsl:if>
      <xsl:if test="not(kc:secure-deployment[@name='artificer-server.war'])">
          <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.0">
            <xsl:attribute name="name">artificer-server.war</xsl:attribute>
            <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.0">artificer</xsl:element>
            <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.0">artificer-server</xsl:element>
            <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.0">
                <xsl:attribute name="name">secret</xsl:attribute>
                6d274880-ab4d-11e4-bcd8-0800200c9a66
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

