<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output xmlns:xalan="http://xml.apache.org/xalan" method="xml" encoding="UTF-8" indent="yes" xalan:indent-amount="2" />

  <!-- Copy everything. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

  <!-- Adds the JMS destinations -->
  <xsl:template name="add-jms-destinations">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="jms-topic" namespace="{$currentNS}">
      <xsl:attribute name="name">SRAMPTopic</xsl:attribute>
      <xsl:element name="entry" namespace="{$currentNS}">
        <xsl:attribute name="name">sramp/events/topic</xsl:attribute>
      </xsl:element>
      <!-- Needed for remote JMS -->
      <xsl:element name="entry" namespace="{$currentNS}">
        <xsl:attribute name="name">jboss/exported/jms/sramp/events/topic</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- Adds the JMS security -->
  <xsl:template name="add-jms-security">
    <xsl:variable name="currentNS" select="namespace-uri(.)" />
    <xsl:element name="security-setting" namespace="{$currentNS}">
      <xsl:attribute name="match">jms.topic.SRAMPTopic</xsl:attribute>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">send</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">consume</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">createNonDurableQueue</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">deleteNonDurableQueue</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">createDurableQueue</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
      <xsl:element name="permission" namespace="{$currentNS}">
        <xsl:attribute name="type">deleteDurableQueue</xsl:attribute>
        <xsl:attribute name="roles">artificer</xsl:attribute>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- Add jms-destinations if missing entirely. -->
  <xsl:template match="*[name()='profile']/*[name()='subsystem']/*[name()='hornetq-server'][not(*[name()='jms-destinations'])]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:variable name="currentNS" select="namespace-uri(.)" />

      <xsl:element name="jms-destinations" namespace="{$currentNS}">
        <xsl:call-template name="add-jms-destinations" />
      </xsl:element>
    </xsl:copy>
  </xsl:template>

  <!-- Add the destinations if jms-destinations already existed and was *not* 
    created above. -->
  <xsl:template match="*[name()='profile']/*[name()='subsystem']/*[name()='hornetq-server']/*[name()='jms-destinations'][not(*[name()='jms-topic'][@name = 'SRAMPTopic'])]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-destinations" />
    </xsl:copy>
  </xsl:template>

  <!-- Add security-setting -->
  <xsl:template match="*[name()='profile']/*[name()='subsystem']/*[name()='hornetq-server']/*[name()='security-settings']">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-security" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>