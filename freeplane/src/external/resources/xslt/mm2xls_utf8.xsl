<?xml version="1.0" encoding="UTF-8"?>
	<!--
		MINDMAPEXPORTFILTER xls;xml %xslt_export.ms_excel
		
		(c) by Naoki Nose, Eric Lavarde 2006 This code is licensed under the GPL.
		(http://www.gnu.org/copyleft/gpl.html) 2006-12-10: added support for
		notes and attributes (EWL)
	-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="urn:schemas-microsoft-com:office:spreadsheet"
	xmlns:o="urn:schemas-microsoft-com:office:office" xmlns:x="urn:schemas-microsoft-com:office:excel"
	xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"
		standalone="yes" />

	<xsl:template match="/map">
		<xsl:processing-instruction name="mso-application">
			progid="Excel.Sheet"
		</xsl:processing-instruction>
		<Workbook>
			<Styles>
				<Style ss:ID="s16" ss:Name="attribute_cell">
					<Borders>
						<Border ss:Position="Bottom" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Left" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Right" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Top" ss:LineStyle="Continuous"
							ss:Weight="1" />
					</Borders>
				</Style>
				<Style ss:ID="s17" ss:Name="attribute_header">
					<Borders>
						<Border ss:Position="Bottom" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Left" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Right" ss:LineStyle="Continuous"
							ss:Weight="1" />
						<Border ss:Position="Top" ss:LineStyle="Continuous"
							ss:Weight="1" />
					</Borders>
					<Font ss:Bold="1" />
				</Style>
			</Styles>
			<Worksheet ss:Name="Freeplane Sheet">
				<Table>
					<xsl:apply-templates select="node">
						<xsl:with-param name="index" select="1" />
					</xsl:apply-templates>
				</Table>
			</Worksheet>
		</Workbook>
	</xsl:template>

	<xsl:template match="node">
		<xsl:param name="index" />
		<Row>
			<Cell ss:Index="{$index}">
				<xsl:call-template name="output-node-text-as-data" />
			</Cell>
			<xsl:if test="attribute">
				<Cell ss:StyleID="s17">
					<Data ss:Type="String">Names</Data>
				</Cell>
				<Cell ss:StyleID="s17">
					<Data ss:Type="String">Values</Data>
				</Cell>
			</xsl:if>
		</Row>
		<xsl:apply-templates select="attribute">
			<xsl:with-param name="index" select="$index + 1" />
		</xsl:apply-templates>
		<xsl:apply-templates select="node">
			<xsl:with-param name="index" select="$index + 1" />
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="attribute">
		<xsl:param name="index" />
		<Row>
			<Cell ss:Index="{$index}" ss:StyleID="s16">
				<Data ss:Type="String">
					<xsl:value-of select="@NAME" />
				</Data>
			</Cell>
			<Cell ss:StyleID="s16">
				<Data ss:Type="String">
					<xsl:value-of select="@VALUE" />
				</Data>
			</Cell>
		</Row>
	</xsl:template>

	<xsl:template name="output-node-text-as-data">
		<xsl:choose>
			<xsl:when test="richcontent[@TYPE='NODE']">
				<xsl:element name="ss:Data" namespace="urn:schemas-microsoft-com:office:spreadsheet">
					<xsl:attribute name="ss:Type">String</xsl:attribute>
					<xsl:copy-of select="richcontent[@TYPE='NODE']/html/body/*" />
				</xsl:element>
			</xsl:when>
			<xsl:otherwise>
				<Data ss:Type="String">
					<xsl:value-of select="@TEXT" />
				</Data>
				<!-- xsl:value-of select="normalize-space(@TEXT)" / -->
			</xsl:otherwise>
		</xsl:choose>
		<xsl:call-template name="output-note-text-as-comment" />
	</xsl:template>

	<xsl:template name="output-note-text-as-comment">
		<xsl:if test="richcontent[@TYPE='NOTE' or @TYPE='DETAILS']">
			<Comment>
				<xsl:element name="ss:Data" namespace="urn:schemas-microsoft-com:office:spreadsheet">
					<xsl:copy-of select="richcontent[@TYPE='DETAILS']/html/body/*" />
					<xsl:copy-of select="richcontent[@TYPE='NOTE']/html/body/*" />
				</xsl:element>
			</Comment>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>

 	  	 
