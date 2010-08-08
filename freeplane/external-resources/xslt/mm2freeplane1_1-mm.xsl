<?xml version="1.0" standalone="no" ?>
	<!--
		MINDMAPEXPORTFILTER mm Freeplane 1.1
	-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" encoding="UTF-8"/>

<xsl:template match="conditional_styles"/>
<xsl:template match="stylenode"/>

<xsl:template match="@*|node()">
  <xsl:copy>
    <xsl:apply-templates select="@*|node()"/>
  </xsl:copy>
</xsl:template>

</xsl:stylesheet>
