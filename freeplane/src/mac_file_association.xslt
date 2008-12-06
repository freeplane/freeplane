<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<xsl:output method="xml"/>

<xsl:template match="/plist/dict">
	<dict>
	<xsl:variable name="test" select="./key[text()='CFBundleDocumentTypes']"/>
	<xsl:value-of select="$test"/>
	<xsl:choose>
		<xsl:when test="count($test) &gt; 0 "/>
		<xsl:otherwise>
			<key>CFBundleDocumentTypes</key>
			<array>
				<dict>
					<key>CFBundleTypeOSTypes</key>
					<array>
						<string>MM</string>
					</array>
					<!--	<key>CFBundleTypeIconFile</key>
								<string>129</string> -->
					<key>CFBundleTypeName</key>
					<string>Mindmap</string>
					<key>CFBundleTypeExtensions</key>
					<array>
						<string>mm</string>
					</array>
					<key>CFBundleTypeRole</key>
					<string>Editor</string>
				</dict>
			</array>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:apply-templates/>
	</dict>	
</xsl:template>



<xsl:template match="/ | node() | @* | comment() | processing-instruction()">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>



</xsl:stylesheet>