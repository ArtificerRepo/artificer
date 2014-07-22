<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:oc="urn:jboss:domain:overlord-configuration:1.0"
    exclude-result-prefixes="oc">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />


  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']/oc:subsystem/oc:configurations/oc:configuration[@name = 'overlord']/oc:properties">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="properties" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">overlord.headerui.apps.s-ramp-ui.href</xsl:attribute>
        <xsl:attribute name="value">/s-ramp-ui/</xsl:attribute>
      </xsl:element>
      <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">overlord.headerui.apps.s-ramp-ui.label</xsl:attribute>
        <xsl:attribute name="value">Repository</xsl:attribute>
      </xsl:element>
      <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">overlord.headerui.apps.s-ramp-ui.primary-brand</xsl:attribute>
        <xsl:attribute name="value">JBoss Overlord</xsl:attribute>
      </xsl:element>
      <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">overlord.headerui.apps.s-ramp-ui.secondary-brand</xsl:attribute>
        <xsl:attribute name="value">S-RAMP Repository</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>


  <xsl:template match="/*[name()='server' or name()='domain']//*[name()='profile']/oc:subsystem/oc:configurations">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="configurations" namespace="{$currentNS}">
      <xsl:apply-templates select="./node()|./text()" />
      <!-- S-RAMP Config -->
      <xsl:element name="configuration" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">sramp</xsl:attribute>
        <xsl:element name="properties" namespace="urn:jboss:domain:overlord-configuration:1.0">
          <!-- No properties defined - all defaults are OK -->
        </xsl:element>
      </xsl:element>
      <!-- S-RAMP UI Config -->
      <xsl:element name="configuration" namespace="urn:jboss:domain:overlord-configuration:1.0">
        <xsl:attribute name="name">sramp-ui</xsl:attribute>
        <xsl:element name="properties" namespace="urn:jboss:domain:overlord-configuration:1.0">
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.endpoint</xsl:attribute>
            <xsl:attribute name="value">${overlord.baseUrl}/s-ramp-server</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.provider</xsl:attribute>
            <xsl:attribute name="value">org.overlord.sramp.ui.server.api.SAMLBearerTokenAuthenticationProvider</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.issuer</xsl:attribute>
            <xsl:attribute name="value">/s-ramp-ui</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.service</xsl:attribute>
            <xsl:attribute name="value">/s-ramp-server</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.sign-assertions</xsl:attribute>
            <xsl:attribute name="value">true</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.keystore</xsl:attribute>
            <xsl:attribute name="value">${overlord.auth.saml-keystore}</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.keystore-password</xsl:attribute>
            <xsl:attribute name="value">${overlord.auth.saml-keystore-password}</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.key-alias</xsl:attribute>
            <xsl:attribute name="value">${overlord.auth.saml-key-alias}</xsl:attribute>
          </xsl:element>
          <xsl:element name="property" namespace="urn:jboss:domain:overlord-configuration:1.0">
            <xsl:attribute name="name">s-ramp-ui.atom-api.authentication.saml.key-password</xsl:attribute>
            <xsl:attribute name="value">${overlord.auth.saml-key-alias-password}</xsl:attribute>
          </xsl:element>
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
