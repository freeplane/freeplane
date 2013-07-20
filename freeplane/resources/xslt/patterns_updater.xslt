<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template 
		match="/ | node() | @* | comment() | processing-instruction()">
		<xsl:copy>
			<xsl:apply-templates select="@* | node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="node">
		<xsl:choose> 
			<xsl:when test="@background_color">
				<xsl:element name="pattern_node_background_color">
					<xsl:attribute name="value"><xsl:value-of select="@background_color"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_background_color/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@color">
				<xsl:element name="pattern_node_color">
					<xsl:attribute name="value"><xsl:value-of select="@color"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_color/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@style">
				<xsl:element name="pattern_node_style">
					<xsl:attribute name="value"><xsl:value-of select="@style"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_style/>
			</xsl:otherwise>
		</xsl:choose>
<!--		<xsl:choose> 
			<xsl:when test="@text">
				<xsl:element name="pattern_node_text">
					<xsl:attribute name="value"><xsl:value-of select="@text"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_text/>
			</xsl:otherwise>
		</xsl:choose>-->
		<xsl:apply-templates select="font"/>
		<xsl:choose> 
			<xsl:when test="@icon and @icon != 'none'">
				<xsl:element name="pattern_icon">
					<xsl:attribute name="value"><xsl:value-of select="@icon"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:when test="@icon = 'none'">
				<pattern_icon/>
		    </xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:apply-templates select="edge"/>
		<xsl:apply-templates select="child"/>
	</xsl:template>

	
	<xsl:template match="edge">
		<xsl:choose> 
			<xsl:when test="@color">
				<xsl:element name="pattern_edge_color">
					<xsl:attribute name="value"><xsl:value-of select="@color"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_edge_color/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@style">
				<xsl:element name="pattern_edge_style">
					<xsl:attribute name="value"><xsl:value-of select="@style"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_edge_style/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@width">
				<xsl:element name="pattern_edge_width">
					<xsl:attribute name="value"><xsl:value-of select="@width"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_edge_width/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="font">
		<xsl:choose> 
			<xsl:when test="@name">
				<xsl:element name="pattern_node_font_name">
					<xsl:attribute name="value"><xsl:value-of select="@name"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_font_name/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@bold">
				<xsl:element name="pattern_node_font_bold">
					<xsl:attribute name="value"><xsl:value-of select="@bold"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_font_bold/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@italic">
				<xsl:element name="pattern_node_font_italic">
					<xsl:attribute name="value"><xsl:value-of select="@italic"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_font_italic/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:choose> 
			<xsl:when test="@size">
				<xsl:element name="pattern_node_font_size">
					<xsl:attribute name="value"><xsl:value-of select="@size"/></xsl:attribute>
				</xsl:element>
		    </xsl:when>
			<xsl:otherwise>
				<pattern_node_font_size/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="child">
		<xsl:element name="pattern_child">
			<xsl:attribute name="value"><xsl:value-of select="@pattern"/></xsl:attribute>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
