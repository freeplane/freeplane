<?xml version="1.0" encoding="UTF-8"?>
<!---

xsltproc prefs2map.xsl freeplane/resources/xml/preferences.xml \
         > preferences.mm

-->

<xsl:stylesheet version="1.0"
		xmlns="http://freeplane.sf.net/mm/1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" indent="yes"
	  encoding="UTF-8" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />


  <xsl:template match="preferences_structure">
    <map version="freeplane 1.3.0">
      <attribute_registry SHOW_ATTRIBUTES="hide"/>
      <xsl:apply-templates select="*"/>
    </map>
  </xsl:template>

  <xsl:template match="tabbed_pane">
    <xsl:element name="node">
      <xsl:attribute name="TEXT">Preferences</xsl:attribute>
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>


  <xsl:template name="gen_node">
    <xsl:param name="content" />
    <xsl:param name="type" />
    <node>
      <xsl:attribute name="TEXT"><xsl:value-of select="$content" /></xsl:attribute>

      <attribute NAME="type">
	<xsl:attribute name="VALUE"><xsl:value-of select="$type" /></xsl:attribute>
      </attribute>
      <xsl:for-each select="@*">
	<attribute NAME="type">
	  <xsl:attribute name="NAME"><xsl:value-of select="name()" /></xsl:attribute>
	  <xsl:attribute name="VALUE"><xsl:value-of select="." /></xsl:attribute>
	</attribute>
      </xsl:for-each>
      <xsl:apply-templates select="*"/>
      </node>
  </xsl:template>


  <xsl:template match="*">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="content" select="@name" />
      <xsl:with-param name="type"><xsl:value-of select="name()" /></xsl:with-param>
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="choice">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="content" select="@value" />
      <xsl:with-param name="type"><xsl:value-of select="name()" /></xsl:with-param>
    </xsl:call-template>
  </xsl:template>



</xsl:stylesheet>
