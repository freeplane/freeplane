<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!--
/*Freeplane - A Program for creating and viewing Mindmaps
 *Copyright (C) 2016 
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

  <xsl:output method="xml" encoding="utf-8" indent="yes"/>
 
<xsl:param name="file_ref"/>
<xsl:param name="destination_dir"/>
<xsl:param name="propertyList"/>
<!--
    
    -->
  <xsl:template match="/">
  
  	<xsl:element name="jnlp">
  		<xsl:attribute name="spec">1.0+</xsl:attribute>
  		<xsl:attribute name="href"><xsl:value-of select="$file_ref"/></xsl:attribute>
  		<xsl:attribute name="codebase"><xsl:value-of select="$destination_dir"/></xsl:attribute>
	    <information>
	        <title>Freeplane Viewer Applet</title>
	        <vendor>Freeplane Team</vendor>
	    </information>
	    <resources>
	        <!-- Application Resources -->
	        <j2se version="1.7+"
	              href="http://java.sun.com/products/autodl/j2se"/>
	              <jar href="freeplaneviewer.jar" main="true"/>
	    </resources>
	    <security>
	        <all-permissions/>
	    </security>
	    <applet-desc
	         name="Freeplane Viewer Applet"
	         main-class="org.freeplane.main.applet.FreeplaneApplet"
	         width="800"
	         height="600">
			 <param name="browsemode_initial_map" value="./map.mm"/>
			 <param name="selection_method" value="selection_method_direct"/>
			 <param name="launched_by_java_web_start" value="true"/>
			<xsl:call-template name="appletParameters">
                <xsl:with-param name="propertyList" select="$propertyList"/>
            </xsl:call-template>			 
	     </applet-desc>
	     <update check="background"/>
  	</xsl:element>
  
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
