<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:as22="urn:jboss:domain:2.2"
  xmlns:jms20="urn:jboss:domain:messaging:2.0"
  xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan as22 jms20" version="1.0">

  <xsl:output method="xml" encoding="UTF-8" indent="yes"
    xalan:indent-amount="2" />

  <!-- Copy everything. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

  <!-- Adds the JMS destinations -->
  <xsl:template name="add-jms-destinations">
    <jms-topic name="SRAMPTopic">
      <entry name="sramp/events/topic" />
      <!-- Needed for remote JMS -->
      <entry name="jboss/exported/jms/sramp/events/topic"/>
    </jms-topic>
  </xsl:template>

  <!-- Adds the JMS security -->
  <xsl:template name="add-jms-security">
    <security-setting match="jms.topic.SRAMPTopic">
      <permission type="send" roles="artificer" />
      <permission type="consume" roles="artificer" />
      <permission type="createNonDurableQueue" roles="artificer" />
      <permission type="deleteNonDurableQueue" roles="artificer" />
      <permission type="createDurableQueue" roles="artificer" />
      <permission type="deleteDurableQueue" roles="artificer" />
    </security-setting>
  </xsl:template>

  <!-- Add jms-destinations if missing entirely. -->
  <xsl:template
    match="as22:profile/jms20:subsystem/jms20:hornetq-server[not(jms20:jms-destinations)]"
    xmlns="urn:jboss:domain:messaging:2.0">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <jms-destinations>
        <xsl:call-template name="add-jms-destinations" />
      </jms-destinations>
    </xsl:copy>
  </xsl:template>

  <!-- Add the destinations if jms-destinations already existed and was *not* 
    created above. -->
  <xsl:template
    match="as22:profile/jms20:subsystem/jms20:hornetq-server/jms20:jms-destinations[not(jms20:jms-topic[@name = 'SRAMPTopic'])]"
    xmlns="urn:jboss:domain:messaging:2.0">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-destinations" />
    </xsl:copy>
  </xsl:template>

  <!-- Add security-setting -->
  <xsl:template
    match="as22:profile/jms20:subsystem/jms20:hornetq-server/jms20:security-settings"
    xmlns="urn:jboss:domain:messaging:2.0">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-security" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>