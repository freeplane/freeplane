<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:output method="xml" version="1.0" indent="yes"
		encoding="ascii" omit-xml-declaration="yes" />
	<xsl:strip-space elements="*" />

	<xsl:template match="/">
		<map version="freeplane 1.5.5">
			<xsl:apply-templates select="node()" />
		</map>
	</xsl:template>

	<xsl:template match="*">
		<xsl:element name="node">
			<xsl:attribute name="TEXT">
				<xsl:value-of select="name()" />
			</xsl:attribute>
			<xsl:apply-templates select="@*" />
			<xsl:if test="not(ancestor::*)">
				<hook NAME="MapStyle" background="#ffffff">
					<properties show_icon_for_attributes="false"
						fit_to_viewport="false;" />

					<map_styles>
						<stylenode LOCALIZED_TEXT="styles.root_node" STYLE="oval"
							UNIFORM_SHAPE="true" VGAP_QUANTITY="24.0 pt">
							<font SIZE="24" />
							<stylenode LOCALIZED_TEXT="styles.predefined" POSITION="right"
								STYLE="bubble">
								<stylenode LOCALIZED_TEXT="default" COLOR="#000000"
									STYLE="bubble"
									SHAPE_HORIZONTAL_MARGIN="3.0 px" SHAPE_VERTICAL_MARGIN="2.0 px"
									MAX_WIDTH_QUANTITY="240 pt"
									MIN_WIDTH_QUANTITY="240 pt" 
									VGAP_QUANTITY="2.0 px">
									<font NAME="Arial" SIZE="8" BOLD="false" ITALIC="false" />
								</stylenode>
							</stylenode>
						</stylenode>
					</map_styles>
				</hook>
					<xsl:for-each select="namespace::*">
						<xsl:if test="string() != 'http://www.w3.org/XML/1998/namespace'">
							<attribute_layout NAME_WIDTH="50 pt" VALUE_WIDTH="180 pt"/>
							<xsl:element name="attribute">
								<xsl:attribute name="NAME">
									<xsl:text>xmlns</xsl:text>
									<xsl:if test="name() != ''">
										<xsl:text>:</xsl:text>
									</xsl:if>
									<xsl:value-of select="name()" />
								</xsl:attribute>
								<xsl:attribute name="VALUE">
									<xsl:value-of select="string()" />
								</xsl:attribute>
							</xsl:element>
						</xsl:if>
					</xsl:for-each>
			</xsl:if>
			<xsl:apply-templates select="node()" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="@*">
		<attribute_layout NAME_WIDTH="50 pt" VALUE_WIDTH="180 pt"/>
		<xsl:element name="attribute">
			<xsl:attribute name="NAME">
				<xsl:value-of select="name()" />
			</xsl:attribute>
			<xsl:attribute name="VALUE">
				<xsl:value-of select="string()" />
			</xsl:attribute>
		</xsl:element>
	</xsl:template>
</xsl:stylesheet>
	