<?xml version="1.0" encoding="UTF-8"?>
<!-- XSLT file to add the security domains to the standalone.xml -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:output method="xml" encoding="UTF-8"
      doctype-public="-//Jetty//Configure//EN"
      doctype-system="http://www.eclipse.org/jetty/configure.dtd"
   />

  <xsl:template match="/Configure/child::*[last()]">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>

    <xsl:text>
    
    </xsl:text>
    <xsl:comment> =========================================================== </xsl:comment>
    <xsl:text>
    </xsl:text>
    <xsl:comment> Data Source: s-ramp                                         </xsl:comment>
    <xsl:text>
    </xsl:text>
    <xsl:comment> =========================================================== </xsl:comment>
    <xsl:text>
    </xsl:text>
    <New id="S-RAMP_DS" class="org.eclipse.jetty.plus.jndi.Resource"><xsl:text>
      </xsl:text><Arg></Arg><xsl:text>
      </xsl:text><Arg>java:comp/env/jdbc/sramp</Arg><xsl:text>
      </xsl:text><Arg><xsl:text>
        </xsl:text><New class="org.apache.commons.dbcp.BasicDataSource"><xsl:text>
          </xsl:text><Set name="driverClassName">org.h2.Driver</Set><xsl:text>
          </xsl:text><Set name="url">jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</Set><xsl:text>
          </xsl:text><Set name="username">sa</Set><xsl:text>
          </xsl:text><Set name="password"></Set><xsl:text>
          </xsl:text><Set name="validationQuery">SELECT 1</Set><xsl:text>
        </xsl:text></New><xsl:text>
      </xsl:text></Arg><xsl:text>
    </xsl:text></New>
  </xsl:template>

  <!-- Copy everything else. -->
  <xsl:template match="@*|node()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()|text()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
