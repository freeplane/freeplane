<?xml version="1.0" encoding="UTF-8" ?>

<!--
        : This code released under the GPL.
        : (http://www.gnu.org/copyleft/gpl.html)
    Document   : mm2text.xsl
    Created on : 01 February 2004, 17:17
    Author     : joerg feuerhake joerg.feuerhake@free-penguin.org
    Description: transforms freemind mm format to html, handles crossrefs and adds numbering. feel free to customize it while leaving the ancient authors
                    mentioned. thank you
    ChangeLog:
    
    See: http://freemind.sourceforge.net/
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"  indent="no" encoding="ISO-8859-1" />
<xsl:key name="refid" match="node" use="@ID" />

    <xsl:template match="/">
        <xsl:text>#MindMapExport FreemindVersion:</xsl:text><xsl:value-of select="map/@version"/>
        <xsl:text>&#xA;</xsl:text>
        <xsl:text>&#xA;</xsl:text><xsl:apply-templates/>  
    </xsl:template>

<xsl:template match="node">
    <xsl:variable name="thisid" select="@ID"/>
    <xsl:variable name="target" select="arrowlink/@DESTINATION"/>
    <xsl:number level="multiple" count="node" format="1"/><xsl:text> </xsl:text><xsl:value-of select="@TEXT"/>
        <xsl:if test="arrowlink/@DESTINATION != ''">
            <xsl:text> (see:</xsl:text>
            <xsl:for-each select="key('refid', $target)">
                <xsl:value-of select="@TEXT"/>
            </xsl:for-each>
            <xsl:text>)</xsl:text>
         </xsl:if>
     <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet> 
