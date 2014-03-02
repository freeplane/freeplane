<?xml version="1.0" standalone="no" ?>
	<!--
		: Convert from MindManager (c) to Freeplane ( ;) ). : : This code
		released under the GPL. : (http://www.gnu.org/copyleft/gpl.html) : :
		Christian Foltin, June, 2005 : : $Id: mindmanager2mm.xsl,v 1.1.2.3.4.3
		2007/10/17 19:54:36 christianfoltin Exp $ :
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:ap="http://schemas.mindjet.com/MindManager/Application/2003"
	xmlns:cor="http://schemas.mindjet.com/MindManager/Core/2003" xmlns:pri="http://schemas.mindjet.com/MindManager/Primitive/2003"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xhtml="http://www.w3.org/1999/xhtml">

	<xsl:strip-space elements="*" />
	<xsl:output method="xml" indent="yes" encoding="us-ascii" />

	<xsl:template match="/ap:Map">
		<map version="0.9.0">
			<xsl:apply-templates select="ap:OneTopic/ap:Topic" />
		</map>
	</xsl:template>

	<xsl:template match="ap:Topic">
		<node>
			<xsl:attribute name="TEXT">
				<xsl:value-of select="./ap:Text/@PlainText" />
			</xsl:attribute>
			<xsl:attribute name="POSITION">
				<xsl:choose>
					<xsl:when test="ancestor-or-self::ap:Topic/ap:Offset/@CX &gt; 0"><xsl:text>right</xsl:text></xsl:when>
					<xsl:otherwise><xsl:text>left</xsl:text></xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:choose>
				<xsl:when test="./ap:Hyperlink">
					<xsl:attribute name="LINK">
						<xsl:value-of select="./ap:Hyperlink/@Url" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="./ap:Text/ap:Font/@Color">
					<xsl:attribute name="COLOR">
						<xsl:text>#</xsl:text><xsl:value-of
						select="substring(./ap:Text/ap:Font/@Color,3)" />
					</xsl:attribute>
				</xsl:when>
				<xsl:when test="./ap:SubTopicShape/@SubTopicShape='urn:mindjet:Oval'">
					<xsl:attribute name="STYLE">
						<xsl:text>bubble</xsl:text>
					</xsl:attribute>
				</xsl:when>
			</xsl:choose>
			<xsl:apply-templates select="./ap:NotesGroup" />
			<xsl:apply-templates select="./ap:SubTopics/ap:Topic" />
			<!--
				<xsl:for-each select="./ap:SubTopics/ap:Topic"> <xsl:sort
				select="(./ap:Offset/@CX) * -1"/> <xsl:apply-templates select="."/>
				</xsl:for-each>
			-->
			<xsl:apply-templates select="./ap:IconsGroup" />
		</node>
	</xsl:template>

	<xsl:template match="ap:NotesGroup">
		<xsl:element name="richcontent">
			<xsl:attribute name="TYPE">
				<xsl:text>NOTE</xsl:text>
			</xsl:attribute>
			<xsl:copy-of select="ap:NotesXhtmlData/xhtml:html" />
		</xsl:element>
	</xsl:template>

	<xsl:template match="ap:IconsGroup">
		<xsl:apply-templates select="./ap:Icons" />
	</xsl:template>

	<xsl:template match="ap:Icons">
		<xsl:apply-templates select="./ap:Icon" />
	</xsl:template>

	<xsl:template match="ap:Icon[@xsi:type='ap:StockIcon']">
		<xsl:element name="icon">
			<xsl:attribute name="BUILTIN">
				<xsl:choose>
                                <xsl:when
				test="@IconType='urn:mindjet:SmileyAngry'">clanbomber</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:SmileyNeutral'">button_ok</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:SmileySad'">clanbomber</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:SmileyHappy'">ksmiletris</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:SmileyScreaming'">ksmiletris</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:ArrowRight'">forward</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:ArrowLeft'">back</xsl:when>
<!--                                <xsl:when test="@IconType='urn:mindjet:TwoEndArrow'">bell</xsl:when>
                                <xsl:when test="@IconType='urn:mindjet:ArrowDown'">bell</xsl:when>
                                <xsl:when test="@IconType='urn:mindjet:ArrowUp'">bell</xsl:when> -->
                                <xsl:when
				test="@IconType='urn:mindjet:FlagGreen'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagYellow'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagPurple'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagBlack'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagBlue'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagOrange'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:FlagRed'">flag</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:ThumbsUp'">button_ok</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Calendar'">bell</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Emergency'">messagebox_warning</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:OnHold'">knotify</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Stop'">button_cancel</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Prio1'">full-1</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Prio2'">full-2</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Prio3'">full-3</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Prio4'">full-4</xsl:when>
                                <xsl:when
				test="@IconType='urn:mindjet:Prio5'">full-5</xsl:when>
<!--
	                                <xsl:when test="@IconType='urn:mindjet:JudgeHammer'">bell</xsl:when>
                                <xsl:when test="@IconType='urn:mindjet:Dollar'">bell</xsl:when>
                                <xsl:when test="@IconType='urn:mindjet:Resource1'">bell</xsl:when>
									-->
	<!--					<xsl:when test="@IconType='urn:mindjet:Resource1'">button_ok</xsl:when> -->
					<xsl:otherwise>
						<xsl:text>messagebox_warning</xsl:text>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
		</xsl:element>
	</xsl:template>



	<xsl:template match="node()|@*" />


</xsl:stylesheet>
