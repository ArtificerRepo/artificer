<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:as14="urn:jboss:domain:1.4" xmlns:as15="urn:jboss:domain:1.5"
  xmlns:as16="urn:jboss:domain:1.6" xmlns:jms13="urn:jboss:domain:messaging:1.3"
  xmlns:jms14="urn:jboss:domain:messaging:1.4" xmlns:xalan="http://xml.apache.org/xalan"
  exclude-result-prefixes="xalan as14 as15 as16 jms13 jms14" version="1.0">

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
      <permission type="send" roles="overlorduser" />
      <permission type="consume" roles="overlorduser" />
      <permission type="createNonDurableQueue" roles="overlorduser" />
      <permission type="deleteNonDurableQueue" roles="overlorduser" />
      <permission type="createDurableQueue" roles="overlorduser" />
      <permission type="deleteDurableQueue" roles="overlorduser" />
    </security-setting>
  </xsl:template>

  <!-- ************************* -->
  <!-- Support for JBoss EAP 6.1 -->
  <!-- ************************* -->

  <!-- Add jms-destinations if missing entirely. -->
  <xsl:template
    match="as14:profile/jms13:subsystem/jms13:hornetq-server[not(jms13:jms-destinations)]"
    xmlns="urn:jboss:domain:messaging:1.3">
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
    match="as14:profile/jms13:subsystem/jms13:hornetq-server/jms13:jms-destinations[not(jms13:jms-topic[@name = 'SRAMPTopic'])]"
    xmlns="urn:jboss:domain:messaging:1.3">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-destinations" />
    </xsl:copy>
  </xsl:template>

  <!-- Add security-setting -->
  <xsl:template
    match="as14:profile/jms13:subsystem/jms13:hornetq-server/jms13:security-settings"
    xmlns="urn:jboss:domain:messaging:1.3">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-security" />
    </xsl:copy>
  </xsl:template>

  <!-- ************************* -->
  <!-- Support for JBoss EAP 6.2 -->
  <!-- ************************* -->

  <!-- Add jms-destinations if missing entirely. -->
  <xsl:template
    match="as15:profile/jms14:subsystem/jms14:hornetq-server[not(jms14:jms-destinations)]"
    xmlns="urn:jboss:domain:messaging:1.4">
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
    match="as15:profile/jms14:subsystem/jms14:hornetq-server/jms14:jms-destinations[not(jms14:jms-topic[@name = 'SRAMPTopic'])]"
    xmlns="urn:jboss:domain:messaging:1.4">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-destinations" />
    </xsl:copy>
  </xsl:template>

  <!-- Add security-setting -->
  <xsl:template
    match="as15:profile/jms14:subsystem/jms14:hornetq-server/jms14:security-settings"
    xmlns="urn:jboss:domain:messaging:1.4">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-security" />
    </xsl:copy>
  </xsl:template>

  <!-- ************************* -->
  <!-- Support for JBoss EAP 6.3 -->
  <!-- ************************* -->

  <!-- Add jms-destinations if missing entirely. -->
  <xsl:template
    match="as16:profile/jms14:subsystem/jms14:hornetq-server[not(jms14:jms-destinations)]"
    xmlns="urn:jboss:domain:messaging:1.4">
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
    match="as16:profile/jms14:subsystem/jms14:hornetq-server/jms14:jms-destinations[not(jms14:jms-topic[@name = 'SRAMPTopic'])]"
    xmlns="urn:jboss:domain:messaging:1.4">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-destinations" />
    </xsl:copy>
  </xsl:template>

  <!-- Add security-setting -->
  <xsl:template
    match="as16:profile/jms14:subsystem/jms14:hornetq-server/jms14:security-settings"
    xmlns="urn:jboss:domain:messaging:1.4">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />

      <xsl:call-template name="add-jms-security" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>