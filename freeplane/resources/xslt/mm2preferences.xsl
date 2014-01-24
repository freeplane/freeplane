<?xml version="1.0" encoding="UTF-8"?>
<!---

xsltproc freeplane/ant/mm2preferences.xsl \
    freeplane/resources/xml/preferences.mm \
    > freeplane/resources/xml/preferences.xml

-->

<xsl:stylesheet version="1.0"
		xmlns="http://freeplane.sf.net/ui/preferences/1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" indent="yes"
	  encoding="UTF-8" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />

  <xsl:template match="map">
    <preferences_structure>
      <xsl:comment>

This file was generated automatically from a mindmap.
Do not edit this file, edit the original mindmap instead.

      </xsl:comment>
      <tabbed_pane>
	<xsl:apply-templates select="*"/>
      </tabbed_pane>
    </preferences_structure>
  </xsl:template>


  <xsl:template name="gen_node">
    <xsl:param name="set_name_attr" select="true()"/>
    <xsl:element name="{attribute[@NAME='type']/@VALUE}">
      <xsl:if test="$set_name_attr">
	<xsl:attribute name="name">
	  <xsl:value-of select="attribute[@NAME='name']/@VALUE" />
	</xsl:attribute>
      </xsl:if>
      <xsl:for-each select="attribute[not(@NAME='type' or @NAME='name')]">
	<xsl:attribute name="{@NAME}">
	  <xsl:value-of select="@VALUE" />
	</xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="node[attribute]">
    <xsl:call-template name="gen_node">
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='--ignore--']]">
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='choice']]">
    <xsl:call-template name="gen_node">
      <xsl:with-param name="set_name_attr" select="false()" />
    </xsl:call-template>
  </xsl:template>


  <xsl:template match="richcontent">
    <!-- suppress outputting richcontent nodes -->
  </xsl:template>

</xsl:stylesheet>
