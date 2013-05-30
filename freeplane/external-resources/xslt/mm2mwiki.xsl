<?xml version="1.0" encoding="utf-8"?>
    <!--
	MINDMAPEXPORTFILTER mwiki %xslt_export.mediawiki
        (c) by Stephen Fitch, 2005 
        (c) by Dimitry Polivaev, 2010 
        This file is licensed under the GPL.
    -->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:output method="text" indent="no" />
    <xsl:strip-space elements="*" />
    <xsl:template match="/map">
        <xsl:apply-templates select="node" />
    </xsl:template>
    <xsl:template match="@TEXT">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>
    <!-- match "node" -->
    <xsl:template match="node">
    	<xsl:variable name="depth" select="count(ancestor::node)" />
		<xsl:if test="@TEXT">
			<xsl:choose>
		    	<xsl:when test="node and @TEXT and not(contains(@TEXT, '&#xA;')) and $depth &lt;= 5">
		    		<xsl:call-template name="chars">
		    			<xsl:with-param name="char" select="'='"/>
		    			<xsl:with-param name="count" select="$depth"/>
		    		</xsl:call-template>
					<xsl:value-of select="normalize-space(@TEXT)" />
					<xsl:call-template name="chars">
						<xsl:with-param name="char" select="'='" />
						<xsl:with-param name="count" select="$depth" />
					</xsl:call-template>
		    	</xsl:when>
		    	<xsl:when test="contains(@TEXT, '&#xA;')">
			        <xsl:text>&#xA;&lt;pre&gt;&#xA;</xsl:text>
					<xsl:value-of select="@TEXT" />
        			<xsl:text>&#xA;&lt;/pre&gt;&#xA;</xsl:text>
		    	</xsl:when>
				<xsl:otherwise>
				<xsl:value-of select="normalize-space(@TEXT)" />
					<xsl:text>&#xA;</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xA;</xsl:text>
    	</xsl:if>
		<xsl:apply-templates select="richcontent[@TYPE='NODE']"/>
		<xsl:apply-templates select="richcontent[@TYPE='DETAILS']"/>
		<xsl:apply-templates select="richcontent[@TYPE='NOTE']"/>
		<xsl:apply-templates select="node"/>
    </xsl:template>
    
     <xsl:template match="node" mode="indent">
     </xsl:template>
    
	<xsl:template match="richcontent">
		<xsl:if test="@TYPE='DETAILS'">
			<xsl:text>&#xA;DETAILS: </xsl:text>
		</xsl:if>
		<xsl:if test="@TYPE='NOTE'">
			<xsl:text>&#xA;NOTE: </xsl:text>
		</xsl:if>
		<xsl:apply-templates mode="html"/>
		<xsl:text>&#xA;&#xA;</xsl:text>
	</xsl:template>

    <xsl:template name="chars">
        <xsl:param name="count" select="1" />
        <xsl:param name="char" select="' '" />
        <xsl:if test="$count > 0">
            <xsl:value-of select="$char"/>
            <xsl:call-template name="chars">
                <xsl:with-param name="count" select="$count - 1" />
                <xsl:with-param name="char" select="$char" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" mode="html">
        <xsl:value-of select="normalize-space(.)" />
    </xsl:template>
	<xsl:template match="p|br|div" mode="html">
		<xsl:if test="preceding-sibling::*">
			<xsl:text>&#xA;&#xA;</xsl:text>
		</xsl:if>
		<xsl:apply-templates mode="html"/>
	</xsl:template>
	
    <xsl:template match="li" mode="html">
        <xsl:text>&#xA;*</xsl:text>
        <xsl:apply-templates mode="html" />
    </xsl:template>
    <xsl:template match="i" mode="html">
        <xsl:text>''</xsl:text>
        <xsl:apply-templates mode="html" />
        <xsl:text>''</xsl:text>
    </xsl:template>
    <xsl:template match="b" mode="html">
        <xsl:text>'''</xsl:text>
        <xsl:apply-templates mode="html" />
        <xsl:text>'''</xsl:text>
    </xsl:template>
    <xsl:template match="tt" mode="html">
        <xsl:text>&lt;tt&gt;</xsl:text>
         <xsl:apply-templates mode="html" />
        <xsl:text>&lt;/tt&gt;</xsl:text>
    </xsl:template>
    <xsl:template match="pre" mode="html">
        <xsl:text>&#xA;&lt;pre&gt;&#xA;</xsl:text>
         <xsl:apply-templates mode="html" />
        <xsl:text>&#xA;&lt;/pre&gt;&#xA;</xsl:text>
    </xsl:template>
    
    <xsl:template match="table" mode="html">
    	<xsl:text>&#xA;{| </xsl:text>
        <xsl:apply-templates select="@*" mode="table_attributes" />
         <xsl:apply-templates mode="html" />
    	<xsl:text>&#xA;|}&#xA;</xsl:text>
    </xsl:template>
    <xsl:template match="th" mode="html">
    	<xsl:text>&#xA;! </xsl:text>
        <xsl:apply-templates select="@*" mode="table_attributes" />
         <xsl:apply-templates mode="html" />
    </xsl:template>
    <xsl:template match="tr" mode="html">
    	<xsl:text>&#xA;|- </xsl:text>
       	<xsl:if test="@*">
	         <xsl:apply-templates select="@*" mode="table_attributes" />
	         <xsl:text> | </xsl:text>
       	</xsl:if>
         <xsl:apply-templates mode="html" />
    </xsl:template>
    <xsl:template match="td" mode="html">
       	<xsl:text>&#xA;| </xsl:text>
       	<xsl:if test="@*">
	         <xsl:apply-templates select="@*" mode="table_attributes" />
	         <xsl:text> | </xsl:text>
       	</xsl:if>
         <xsl:apply-templates mode="html" />
    </xsl:template>
    
    <xsl:template match="@*" mode="table_attributes">
         <xsl:value-of select = "name(.)"/>
         <xsl:text>="</xsl:text>
         <xsl:value-of select = "string(.)" />
         <xsl:text>" </xsl:text>
    </xsl:template>
 </xsl:stylesheet>

 	  	 

 	  	 

 	  	 
