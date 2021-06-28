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

  <xsl:template match="node[starts-with(@TEXT, '&quot;')]" priority="2">
  	<xsl:variable name="length" select="string-length(@TEXT)"/>
  	<xsl:value-of select="substring(@TEXT,2,($length - 2))"/>
  </xsl:template>
  
  <xsl:template match="node[@TEXT]" priority="1">
  	<xsl:variable name="position" select="position()"/>
  	<xsl:if test="not(../node[$position - 1 and starts-with(@TEXT, '&quot;')])"> 
		<xsl:text>&#10;</xsl:text>
  	</xsl:if>
  	<xsl:element name="{@TEXT}">
		<xsl:apply-templates select="attribute"/>
		<xsl:apply-templates select="node"/>
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
