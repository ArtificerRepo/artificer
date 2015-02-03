<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <xsl:template match="*[name()='extensions']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="extensions" namespace="{$currentNS}">
      <xsl:apply-templates select="@* | *" />
      <xsl:element name="extension" namespace="{$currentNS}">
        <xsl:attribute name="module">org.modeshape</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[name()='profile']">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="profile" namespace="{$currentNS}">
      <xsl:apply-templates select="@* | *" />
      <xsl:element name="subsystem" namespace="urn:jboss:domain:modeshape:2.0">
        <xsl:element name="repository" namespace="urn:jboss:domain:modeshape:2.0">
          <xsl:attribute name="name">sramp</xsl:attribute>
          <xsl:attribute name="cache-name">sramp</xsl:attribute>
          <xsl:attribute name="cache-container">modeshape</xsl:attribute>
          <xsl:attribute name="use-anonymous-upon-failed-authentication">false</xsl:attribute>
          <xsl:attribute name="anonymous-roles">readonly</xsl:attribute>
          <xsl:element name="file-binary-storage" namespace="urn:jboss:domain:modeshape:2.0">
            <xsl:attribute name="path">artificer-data/binary</xsl:attribute>
          </xsl:element>
        </xsl:element>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*[name()='profile']/*[name()='subsystem'][contains(namespace-uri(), 'infinispan')]">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="subsystem" namespace="{$currentNS}">
      <xsl:attribute name="default-cache-container">hibernate</xsl:attribute>
      <xsl:apply-templates select="@* | *" />
      <xsl:element name="cache-container" namespace="{$currentNS}">
        <xsl:attribute name="name">modeshape</xsl:attribute>
        <xsl:element name="local-cache" namespace="{$currentNS}">
          <xsl:attribute name="name">sramp</xsl:attribute>
          <xsl:element name="locking" namespace="{$currentNS}">
            <!-- TODO: Desirable to enable this, but hitting too many known issues with ISPN+Wildfly transactions -->
            <!--<xsl:attribute name="isolation">READ_COMMITTED</xsl:attribute>-->
            <xsl:attribute name="isolation">NONE</xsl:attribute>
          </xsl:element>
          <xsl:element name="transaction" namespace="{$currentNS}">
            <xsl:attribute name="mode">NON_XA</xsl:attribute>
            <!-- TODO: Desirable to enable this, but hitting too many known issues with ISPN+Wildfly transactions -->
            <!--<xsl:attribute name="locking">PESSIMISTIC</xsl:attribute>-->
          </xsl:element>
          <xsl:element name="eviction" namespace="{$currentNS}">
            <xsl:attribute name="strategy">LRU</xsl:attribute>
            <xsl:attribute name="max-entries">1000</xsl:attribute>
          </xsl:element>
          <xsl:element name="string-keyed-jdbc-store" namespace="{$currentNS}">
            <xsl:attribute name="datasource">java:jboss/datasources/srampDS</xsl:attribute>
            <xsl:attribute name="passivation">false</xsl:attribute>
            <xsl:attribute name="purge">false</xsl:attribute>
            <xsl:element name="string-keyed-table" namespace="{$currentNS}">
              <xsl:attribute name="prefix">ispn_bucket</xsl:attribute>
              <xsl:element name="id-column" namespace="{$currentNS}">
                <xsl:attribute name="name">id</xsl:attribute>
                <xsl:attribute name="type">VARCHAR(500)</xsl:attribute>
              </xsl:element>
              <xsl:element name="data-column" namespace="{$currentNS}">
                <xsl:attribute name="name">datum</xsl:attribute>
                <xsl:attribute name="type">BLOB</xsl:attribute>
              </xsl:element>
              <xsl:element name="timestamp-column" namespace="{$currentNS}">
                <xsl:attribute name="name">version</xsl:attribute>
                <xsl:attribute name="type">BIGINT</xsl:attribute>
              </xsl:element>
            </xsl:element>
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