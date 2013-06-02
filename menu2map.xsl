<?xml version="1.0" encoding="UTF-8"?>
<!---

xsltproc menu2map.xsl freeplane/resources/xml/mindmapmodemenu.xml > mindmapmodemenu.mm

-->

<xsl:stylesheet version="1.0"
		xmlns="http://freeplane.sf.net/mm/1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" indent="yes"
	  encoding="UTF-8" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />


  <xsl:template match="menu_structure">
    <map version="freeplane 1.3.0">
      <attribute_registry SHOW_ATTRIBUTES="hide"/>
      <node>
	<xsl:apply-templates select="*"/>
      </node>
    </map>
  </xsl:template>


  <xsl:template name="gen_node">
    <xsl:param name="content" />
    <xsl:param name="type" select="name()"/>
    <node>
      <xsl:attribute name="TEXT"><xsl:value-of select="$content" /></xsl:attribute>

      <attribute NAME="type">
	<xsl:attribute name="VALUE">
	  <xsl:value-of select="substring-after($type, 'menu_')" />
	</xsl:attribute>
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

  <xsl:template match="menu_category | menu_submenu">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="content" select="@name" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="menu_action | menu_radio_action">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="content" select="@action" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="menu_separator">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="content" select="'---'" />
    </xsl:call-template>
  </xsl:template>


</xsl:stylesheet>
