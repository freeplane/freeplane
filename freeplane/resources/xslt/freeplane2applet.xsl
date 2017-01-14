<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!--
/*Freeplane - A Program for creating and viewing Mindmaps
 *Copyright (C) 2006  Christian Foltin <christianfoltin@users.sourceforge.net>
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
      
  -->

  <xsl:output method="xml" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" 
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
    encoding="utf-8"
    indent="yes"/>
 
<!-- fc, 20.10.2004: The following parameters are set by freeplane. -->
<xsl:param name="destination_dir">./</xsl:param>
<xsl:param name="propertyList"/>
<!--
    
    -->
  <xsl:template match="/">
    <html>
      <head>
        <!-- look if there is any node inside the map (there should never be none, but who knows?) 
             and take its text as the title -->
        <title><xsl:call-template name="output-title" /></title>
          <style type="text/css">
/*<![CDATA[*/
body { margin-left:0px; margin-right:0px; margin-top:0px; margin-bottom:0px; height:100% }
html { height:100% }
/*]]>*/ 
          </style>
      </head>
        <body>
        <xsl:element name="applet">
            <xsl:attribute name="code">org.freeplane.main.applet.FreeplaneApplet.class</xsl:attribute>
            <xsl:attribute name="archive">
            <xsl:text>./</xsl:text>
            <xsl:value-of select="$destination_dir"/>
            <xsl:text>./freeplaneviewer.jar,./kitfox-svg-salamander-1.1.1-p1.jar</xsl:text>
            </xsl:attribute>
            <xsl:attribute name="width">100%</xsl:attribute>
            <xsl:attribute name="height">100%</xsl:attribute>
            <xsl:element name="param">
            	<xsl:attribute name="name">jnlp_href</xsl:attribute>
            	<xsl:attribute name="value">
            		<xsl:text>./</xsl:text>
            		<xsl:value-of select="$destination_dir"/>
            		<xsl:text>./freeplane_applet.jnlp</xsl:text>
            	</xsl:attribute>
            </xsl:element>
            <xsl:element name="param">
                <xsl:attribute name="name">browsemode_initial_map</xsl:attribute>
                <xsl:attribute name="value">./<xsl:value-of select="$destination_dir"/>map.mm</xsl:attribute>
            </xsl:element>
            <param name="selection_method" value="selection_method_direct"/>
            <param name="codebase_lookup" value="false"/>
            <xsl:element name="script">
            <xsl:text>document.write("</xsl:text>
            <xsl:text disable-output-escaping="yes">&lt;param name=&apos;location_href&apos; value=&apos;" + window.location.href +"&apos;/&gt;</xsl:text>
            <xsl:text>");</xsl:text>
            </xsl:element>
            <xsl:call-template name="appletParameters">
                <xsl:with-param name="propertyList" select="$propertyList"/>
            </xsl:call-template>
        </xsl:element>
       </body>
    </html>
  </xsl:template>

	<xsl:template name="output-title">
		<!-- look if there is any node inside the map (there should never be
			none, but who knows?) and take its text as the title -->
		<xsl:choose>
		<xsl:when test="/map/node/@TEXT">
			<xsl:value-of select="normalize-space(/map/node/@TEXT)" />
		</xsl:when>
		<xsl:when test="/map/node/richcontent[@TYPE='NODE']">
			<xsl:variable name="t">
				<xsl:apply-templates select="/map/node/richcontent[@TYPE='NODE']/html/body" mode="strip-tags" />			
			</xsl:variable>
			<xsl:value-of select="normalize-space($t)" />
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>Mind Map</xsl:text>
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

    <xsl:template name="appletParameters">
       <xsl:param name="propertyList"/>
       <xsl:if test="$propertyList">
            <xsl:variable name="property" select="substring-before($propertyList, '$$$')"/>
            <xsl:variable name="name" select="substring-before($property, '=')"/>
            <xsl:variable name="value" select="substring-after($property, '=')"/>
            <xsl:call-template name="appletParam">
                <xsl:with-param name="name" select="$name"/>
                <xsl:with-param name="value" select="$value"/>
            </xsl:call-template>
            <xsl:variable name="otherProperties" select="substring-after($propertyList, '$$$')"/>
            <xsl:call-template name="appletParameters">
                <xsl:with-param name="propertyList" select="$otherProperties"/>
            </xsl:call-template>
       </xsl:if>
    </xsl:template>
	
	<xsl:template name="appletParam">
       <xsl:param name="name"/>
       <xsl:param name="value"/>
            <xsl:if test="$value">
                <xsl:element name="param">
                    <xsl:attribute name="name"><xsl:value-of select="$name"/></xsl:attribute>
                    <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
                </xsl:element>
            </xsl:if>
	</xsl:template>
	
	<xsl:template match="text()|@*"  mode="strip-tags">
		  <xsl:value-of select="string(.)"/>
	</xsl:template>


</xsl:stylesheet>
