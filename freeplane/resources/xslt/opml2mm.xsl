<?xml version='1.0' encoding='ISO-8859-1'?>
<xsl:stylesheet version='1.0'
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
	<xsl:output method="xml" indent="yes" encoding="us-ascii" />
	<xsl:template match='/'>
		<map version='0.7.1'>
			<xsl:apply-templates select='opml' />
		</map>
	</xsl:template>
	<xsl:template match='opml'>
		<xsl:apply-templates select='body' />
	</xsl:template>
	<xsl:template match='body'>
		<node>
			<xsl:attribute name='COLOR'>#006633</xsl:attribute>
			<xsl:attribute name='TEXT'><xsl:value-of select='//title' /></xsl:attribute>
			<xsl:attribute name='FOLDED'>true</xsl:attribute>
			<font Name='SansSerif' SIZE='18' />
			<xsl:apply-templates select='outline' />
		</node>
	</xsl:template>
	<xsl:template match='outline'>
		<xsl:choose>
			<xsl:when test='count(child::*)!=0'>
				<node>
					<xsl:attribute name='COLOR'>#006633</xsl:attribute>
					<xsl:attribute name='TEXT'><xsl:value-of
						select='@text' /></xsl:attribute>
					<xsl:attribute name='FOLDED'>true</xsl:attribute>
					<font Name='SansSerif' SIZE='18' />
					<xsl:apply-templates select='outline' />
				</node>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test='@type=&apos;link&apos;'>
						<node>
							<xsl:attribute name='COLOR'>#006633</xsl:attribute>
							<xsl:attribute name='TEXT'><xsl:value-of
								select='@text' /></xsl:attribute>
							<xsl:attribute name='LINK'>
							<xsl:choose>
							<xsl:when
								test='contains(@url,&apos;.opml&apos;) or contains(@url,&apos;.OPML&apos;)'>
								<xsl:value-of select='concat(@url,&apos;.mm&apos;)' />
								</xsl:when>
								<xsl:otherwise>
								<xsl:value-of select='@url' />
								</xsl:otherwise> 
								</xsl:choose>
								</xsl:attribute>
							<font Name='SansSerif' SIZE='16' />
							<xsl:apply-templates select='outline' />
						</node>
					</xsl:when>
					<xsl:when test='@type=&apos;img&apos;'>
						<node>
							<xsl:attribute name='TEXT'>
							<xsl:value-of
								select='concat(&apos;&lt;html&gt;&lt;img src=&quot;&apos;,@url,&apos;&quot;&gt;&apos;)' />
							</xsl:attribute>
							<font Name='SansSerif' SIZE='16' />
							<xsl:apply-templates select='outline' />
						</node>
					</xsl:when>
					<xsl:otherwise>
						<node>
							<xsl:attribute name='TEXT'>
							<xsl:value-of select='@text' />
							</xsl:attribute>
							<font Name='SansSerif' SIZE='16' BOLD='true' />
							<xsl:apply-templates select='outline' />
						</node>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>