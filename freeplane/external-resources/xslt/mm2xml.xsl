<?xml version="1.0" encoding="UTF-8"?>
<!--
	: mm2xml.xsl : XSL stylesheet to convert from mind map to xml:

	MINDMAPEXPORTFILTER xml XML

	This code released under the GPL. :
-->

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml" version="1.0" indent="yes"
	  encoding="UTF-8" omit-xml-declaration="no" />
  <xsl:strip-space elements="*" />

  <xsl:template match="map">
      <xsl:apply-templates select="node"/>
  </xsl:template>

  <xsl:template match="node[parent::map]" priority="2">
  		<xsl:text>&#10;</xsl:text>
  		<xsl:text disable-output-escaping="yes">&lt;</xsl:text>
  		<xsl:value-of select="@TEXT"/>
  		<xsl:text> </xsl:text>
  		<xsl:apply-templates select="node[position() = 1 and @TEXT='&lt;configuration&gt;']"/>
  		<xsl:text disable-output-escaping="yes">&gt;</xsl:text>
  		<xsl:text>&#10;</xsl:text>
  		<xsl:apply-templates select="node[position() != 1 or @TEXT!='&lt;configuration&gt;']"/>
  		<xsl:text disable-output-escaping="yes">&lt;/</xsl:text>
  		<xsl:value-of select="@TEXT"/>
  		<xsl:text disable-output-escaping="yes">&gt;</xsl:text>
  </xsl:template>
  
  <xsl:template match="node[parent::node/parent::map and position() = 1 and @TEXT='&lt;configuration&gt;']" priority="2">
  	   <xsl:for-each select="attribute">
	   		<xsl:text>&#10;  </xsl:text>
   			<xsl:value-of select="@NAME"/>
   			<xsl:text>="</xsl:text>
   			<xsl:value-of select="@VALUE"/>
   			<xsl:text>" </xsl:text>
  	   </xsl:for-each>
  </xsl:template>
  

  <xsl:template match="node[@TEXT]" priority="1">
  	<xsl:element name="{@TEXT}">
  		<xsl:apply-templates/>
  	</xsl:element>
  </xsl:template>

  <xsl:template match="attribute[@NAME and @VALUE]">
  	<xsl:attribute name="{@NAME}">
  		<xsl:value-of select="@VALUE"/>
  	</xsl:attribute>
  </xsl:template>

  <xsl:template match="*">
  </xsl:template>
  
</xsl:stylesheet>
