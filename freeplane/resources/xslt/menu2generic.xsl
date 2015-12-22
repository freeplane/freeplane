<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" encoding="UTF-8" omit-xml-declaration="no" standalone="no" indent="yes"/> 
	<xsl:template match="/">
      		<FreeplaneUIEntries>
      		<Entry builder="main_menu">
				<xsl:apply-templates select="/*/*"/>
			</Entry>
		</FreeplaneUIEntries>
	</xsl:template>
	
<xsl:template match="*">
	<xsl:element name="Entry" >
		<xsl:if test="name()='menu_separator'">
			<xsl:attribute name="builder">
				<xsl:text>separator</xsl:text>
			</xsl:attribute>
		</xsl:if>
		<xsl:if test="*[name()='menu_radio_action']">
			<xsl:attribute name="builder">
				<xsl:text>radio_button_group</xsl:text>
			</xsl:attribute>
		</xsl:if>
		<xsl:apply-templates select="@*"/>
		<xsl:apply-templates select="*"/>
	</xsl:element>
</xsl:template>

<xsl:template match="@action">
	<xsl:attribute name="name">
			<xsl:value-of select="."/>
	</xsl:attribute>
</xsl:template>

<xsl:template match="@name_ref">
</xsl:template>

<xsl:template match="@menu_key">
	<xsl:attribute name="builder">
		<xsl:value-of select="."/>
	</xsl:attribute>
</xsl:template>

<xsl:template match="@*">
	<xsl:copy/>
</xsl:template>

</xsl:stylesheet>