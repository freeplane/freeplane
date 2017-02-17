<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xalan="http://xml.apache.org/xslt"
		exclude-result-prefixes="xalan"
                version="1.0">

<xsl:output method="xml" encoding="UTF-8" standalone="no" indent="yes" xalan:indent-amount="4" />

<xsl:template match="/plist/dict">
  <xsl:copy>
	<key>CFBundleDocumentTypes</key>
	<array>
		<dict>
			<key>CFBundleTypeOSTypes</key>
			<array>
				<string>MM</string>
			</array>
			<key>CFBundleTypeIconFile</key>
			<string>freeplanedoc.icns</string>
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
    <xsl:apply-templates select="@* | node()"/>
    <key>NSHighResolutionCapable</key>
    <true/>
	<key>CFBundleURLTypes</key>
	<array>
	    <dict>
	        <key>CFBundleURLName</key>
	        <string>Freeplane Mind Map</string>
	        <key>CFBundleURLSchemes</key>
	        <array>
	            <string>freeplane</string>
	        </array>
	    </dict>
	</array>    
  </xsl:copy>
</xsl:template>

<xsl:template match="key[text()='JVMVersion']">
	<key>WorkingDirectory</key>
  	<string>$JAVAROOT</string>
    <xsl:copy>
      <xsl:apply-templates select="@* | node()"/>
    </xsl:copy>
</xsl:template>

<xsl:template match="/ | node() | @* | comment() | processing-instruction()">
  <xsl:copy>
    <xsl:apply-templates select="@* | node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>