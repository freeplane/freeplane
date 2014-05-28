<?xml version="1.0" encoding="UTF-8"?>
<!---

xsltproc freeplane/resources/xslt/mm2menu.xsl \
    freeplane/resources/xml/mindmapmodemenu.mm \
    > freeplane/resources/xml/mindmapmodemenu.xml

-->

<xsl:stylesheet version="1.0"
		xmlns="http://freeplane.sf.net/ui/menu/1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" indent="yes"
	  encoding="UTF-8" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />

  <xsl:template match="map">
    <menu_structure>
      <xsl:apply-templates select="*"/>
    </menu_structure>
  </xsl:template>


  <xsl:template name="gen_node">
    <xsl:element name="menu_{attribute[@NAME='type']/@VALUE}">
      <xsl:for-each select="attribute[not(@NAME='type')]">
	<xsl:attribute name="{@NAME}">
	  <xsl:value-of select="@VALUE" />
	</xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="*"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="node[attribute]">
    <xsl:call-template name="gen_node" />
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='--ignore--']]">
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='category']]">
    <xsl:call-template name="gen_node" />
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='submenu']]">
    <xsl:call-template name="gen_node" />
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='action']]">
    <xsl:call-template name="gen_node" />
  </xsl:template>

  <xsl:template match="node[attribute[@NAME='type' and @VALUE='radio_action']]">
    <xsl:call-template name="gen_node" />
  </xsl:template>


  <xsl:template match="node[attribute[@NAME='type' and @VALUE='separator']]">
    <xsl:call-template name="gen_node" />
  </xsl:template>


  <xsl:template match="richcontent">
    <!-- suppress outputting richcontent nodes -->
  </xsl:template>

</xsl:stylesheet>
