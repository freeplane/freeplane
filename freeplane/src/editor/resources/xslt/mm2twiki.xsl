<?xml version="1.0" encoding="iso-8859-1"?>
<!--
    (c) by Stephen Fitch, 2005
    This file is licensed under the GPL.
-->

<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:output method="text" indent="no"/>

        <xsl:strip-space elements="*"/>
        
	<xsl:template match="map">            
            	<xsl:apply-templates select="node"/>		
	</xsl:template>
        
        <!-- match "node" -->
	<xsl:template match="node">
		<xsl:variable name="depth">
			<xsl:apply-templates select=".." mode="depthMesurement"/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$depth=0">
                                <xsl:choose>
                                    <xsl:when test="@LINK">
                                        <xsl:text>---+ [[</xsl:text><xsl:value-of select="@LINK"/><xsl:text> </xsl:text><xsl:value-of select="@TEXT"/><xsl:text>]]</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:text>---+ </xsl:text><xsl:value-of select="@TEXT"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>&#xA;</xsl:text>
                                <xsl:apply-templates select="hook"/>
				<xsl:apply-templates select="node"/>
			</xsl:when>
                        <xsl:otherwise>
				<xsl:choose>
					<xsl:when test="ancestor::node[@FOLDED='true']">
						<xsl:apply-templates select=".." mode="childoutput">
							<xsl:with-param name="nodeText">	
                                                            <xsl:value-of select="@TEXT"/>
							</xsl:with-param>
						</xsl:apply-templates>						
					</xsl:when>
					<xsl:otherwise>                                         
						<xsl:apply-templates select=".." mode="childoutput">                                                 
							<xsl:with-param name="nodeText">
                                                            <xsl:if test="$depth=1">
                                                                <xsl:text>&#xA;</xsl:text>
                                                            </xsl:if>
                                                            <xsl:call-template name="spaces">
                                                                <xsl:with-param name="count" 
                                                                select="$depth * 3"/>
                                                            </xsl:call-template>
                                                            <!-- Do we have text with a LINK attribute? -->
                                                            <xsl:choose>
                                                                <xsl:when test="@LINK">
                                                                    <xsl:text>* [[</xsl:text><xsl:value-of select="@LINK"/><xsl:text> </xsl:text><xsl:value-of select="@TEXT"/><xsl:text>]]</xsl:text>
                                                                </xsl:when>
                                                                <xsl:otherwise>
                                                                    <xsl:text>* </xsl:text><xsl:value-of select="@TEXT"/>
                                                                    </xsl:otherwise>
                                                               </xsl:choose>
                                                               <xsl:text>&#xA;</xsl:text>
							</xsl:with-param>                                                        
						</xsl:apply-templates>
					</xsl:otherwise>
				</xsl:choose>
				<!-- <xsl:apply-templates select="hook|@LINK"/> -->
				<xsl:apply-templates select="node"/>
                        </xsl:otherwise>
		</xsl:choose>
	</xsl:template>
        
        <xsl:template name="spaces">
            <xsl:param name="count" select="1"/>
            <xsl:if test="$count > 0">
                <xsl:text> </xsl:text>
                <xsl:call-template name="spaces">
                    <xsl:with-param name="count" select="$count - 1"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:template>      
        
        <!-- hook -->
        <xsl:template match="hook"/>
        
        <!-- hook -->
        <xsl:template match="hook[@NAME='accessories/plugins/NodeNote.properties']">
		<xsl:choose>
			<xsl:when test="./text">
				<xsl:value-of select="./text"/>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
        
        <!-- Node - Output -->
        <xsl:template match="node" mode="childoutput">
            <xsl:param name="nodeText"></xsl:param>
            <xsl:copy-of select="$nodeText"/>
        </xsl:template>
        
        <!-- Node Depth Mesurement -->
        <xsl:template match="node" mode="depthMesurement">
            <xsl:param name="depth" select=" '0' "/>
                <xsl:apply-templates select=".." mode="depthMesurement">
                    <xsl:with-param name="depth" select="$depth + 1"/>
                </xsl:apply-templates>
	</xsl:template>
        
        <!-- Map Depth Mesurement -->
        <xsl:template match="map" mode="depthMesurement">
            <xsl:param name="depth" select=" '0' "/>
            <xsl:value-of select="$depth"/>
	</xsl:template>
		
</xsl:stylesheet>
