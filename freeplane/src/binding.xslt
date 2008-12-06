<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>
    <xsl:key name="classkey" match="/binding/mapping" use="@class"/>

<xsl:template match="/ | node() | @* | comment() | processing-instruction()">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="/ | node() | @* | comment() | processing-instruction()" mode="intern">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>
<xsl:template match="mapping/structure[@map-as and position()=1]" mode="intern">
<xsl:element name="structure"> 
<!-- find parent type -->
    <xsl:attribute name="map-as"><xsl:value-of select="key('classkey', ./@map-as)/@name"/>_type</xsl:attribute> 
</xsl:element>
</xsl:template>

<xsl:template match="mapping[not(@abstract)]">

  <!-- base mapping --> <xsl:element name="mapping"> 
    <xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute> 
    <xsl:attribute name="type-name"><xsl:value-of select="@name"/>_type</xsl:attribute>
    <xsl:attribute name="abstract">true</xsl:attribute>
    <xsl:apply-templates select="node()" mode="intern"/>
  </xsl:element>

  <!-- concrete mapping --> <xsl:element name="mapping"> 
    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
<xsl:if test="structure/@map-as">
    <xsl:attribute name="extends"><xsl:value-of select="structure/@map-as"/></xsl:attribute> 
</xsl:if>
    <xsl:attribute name="class"><xsl:value-of select="@class"/></xsl:attribute> 
    <xsl:element name="structure"> 
        <xsl:attribute name="map-as"><xsl:value-of select="@name"/>_type</xsl:attribute>
    </xsl:element>
  </xsl:element>

  

</xsl:template>

<xsl:template match="mapping/structure[@map-as]">
<xsl:element name="structure"> 
    <xsl:attribute name="map-as"><xsl:value-of select="../@name"/></xsl:attribute> 
</xsl:element>
</xsl:template>

</xsl:stylesheet>
