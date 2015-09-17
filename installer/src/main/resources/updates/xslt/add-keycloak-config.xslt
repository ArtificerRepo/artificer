<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <xsl:template match="/*[name()='server' or name()='domain']/*[name()='extensions']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="extensions" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <xsl:element name="extension" namespace="{$currentNS}">
        <xsl:attribute name="module">org.keycloak.keycloak-server-subsystem</xsl:attribute>
      </xsl:element>
      <xsl:element name="extension" namespace="{$currentNS}">
        <xsl:attribute name="module">org.keycloak.keycloak-adapter-subsystem</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="profile" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />

      <xsl:element name="subsystem" namespace="urn:jboss:domain:keycloak-server:1.1">
        <xsl:element name="web-context" namespace="urn:jboss:domain:keycloak-server:1.1">
          auth
        </xsl:element>
      </xsl:element>

      <xsl:element name="subsystem" namespace="urn:jboss:domain:keycloak:1.1">
        <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.1">
          <xsl:attribute name="name">artificer</xsl:attribute>
          <xsl:element name="realm-public-key" namespace="urn:jboss:domain:keycloak:1.1">MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCrVrCuTtArbgaZzL1hvh0xtL5mc7o0NqPVnYXkLvgcwiC3BjLGw1tGEGoJaXDuSaRllobm53JBhjx33UNv+5z/UMG4kytBWxheNVKnL6GgqlNabMaFfPLPCF8kAgKnsi79NMo+n6KnSY8YeUmec/p2vjO2NjsSAVcWEQMVhJ31LwIDAQAB</xsl:element>
          <xsl:element name="auth-server-url" namespace="urn:jboss:domain:keycloak:1.1">/auth</xsl:element>
          <xsl:element name="ssl-required" namespace="urn:jboss:domain:keycloak:1.1">none</xsl:element>
          <xsl:element name="principal-attribute" namespace="urn:jboss:domain:keycloak:1.1">preferred_username</xsl:element>
        </xsl:element>

        <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.1">
          <xsl:attribute name="name">artificer-ui.war</xsl:attribute>
          <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.1">artificer</xsl:element>
          <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.1">artificer-ui</xsl:element>
          <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.1">
            <xsl:attribute name="name">secret</xsl:attribute>
            2b0ad840-ab4d-11e4-bcd8-0800200c9a66
          </xsl:element>
        </xsl:element>

        <xsl:element name="secure-deployment" namespace="urn:jboss:domain:keycloak:1.1">
          <xsl:attribute name="name">artificer-server.war</xsl:attribute>
          <xsl:element name="realm" namespace="urn:jboss:domain:keycloak:1.1">artificer</xsl:element>
          <xsl:element name="resource" namespace="urn:jboss:domain:keycloak:1.1">artificer-server</xsl:element>
          <xsl:element name="credential" namespace="urn:jboss:domain:keycloak:1.1">
            <xsl:attribute name="name">secret</xsl:attribute>
            6d274880-ab4d-11e4-bcd8-0800200c9a66
          </xsl:element>
          <xsl:element name="enable-basic-auth" namespace="urn:jboss:domain:keycloak:1.1">true</xsl:element>
        </xsl:element>
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

