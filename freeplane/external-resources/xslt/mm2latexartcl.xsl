<?xml version='1.0'?>


	<!--
		MINDMAPEXPORTFILTER tex %xslt_export.latex
		
		: This code	released under the GPL. 
		: (http://www.gnu.org/copyleft/gpl.html) 
		Document : mm2latexarticl.xsl 
		Created on : 01 February 2004, 17:17
		Author : joerg feuerhake joerg.feuerhake@free-penguin.org 
		Description: transforms freeplane mm format to latex scrartcl, 
		handles crossrefs ignores the rest. 
		feel free to customize it while leaving the ancient
		authors mentioned. thank you 
		Thanks to: Tayeb.Lemlouma@inrialpes.fr	for writing the LaTeX escape scripts and giving inspiration 
		
		ChangeLog:	See: http://freeplane.sourceforge.net/
	-->

<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	version='1.0'>
	<xsl:output omit-xml-declaration="yes" />

	<xsl:template match="map">

		<xsl:text>
\documentclass[a4paper,12pt,single,pdftex]{scrartcl}
\usepackage{ngerman}
 \usepackage{color}  
 \usepackage{html}  
 \usepackage{times}  
 \usepackage{graphicx} 
 \usepackage{fancyheadings}  
 \usepackage{hyperref}  
 \setlength{\parindent}{0.6pt} 
 \setlength{\parskip}{0.6pt} 
 \title{</xsl:text>
		<xsl:value-of select="node/@TEXT" />
		<xsl:text>}
 </xsl:text>
		<!-- ======= Document Begining ====== -->
		<xsl:text>

\begin{document} 
\maketitle
\newpage

</xsl:text>
		<!-- ======= Heading ====== -->
		<xsl:apply-templates select="Heading" />
		<xsl:apply-templates select="node" />


		<xsl:text>
\newpage
%\tableofcontents

\end{document}
</xsl:text>
</xsl:template>


<!-- ======= Body ====== -->

<!-- Sections Processing -->
<xsl:template match="node">
<xsl:variable name="target" select="arrowlink/@DESTINATION"/>

<xsl:if test="@ID != ''">
<xsl:text>\label{</xsl:text><xsl:value-of select="@ID"/><xsl:text>}</xsl:text>
</xsl:if>

<xsl:if test="(count(ancestor::node())-2)=1">
<xsl:text>\section</xsl:text>
<xsl:text>{</xsl:text>
<xsl:value-of select="@TEXT"/><xsl:text>}

</xsl:text></xsl:if>
<xsl:if test="(count(ancestor::node())-2)=2">
<xsl:text>\subsection</xsl:text>
<xsl:text>{</xsl:text>
<xsl:value-of select="@TEXT"/><xsl:text>}

</xsl:text></xsl:if>

<xsl:if test="(count(ancestor::node())-2)=3">
<xsl:text>\subsubsection</xsl:text>
<xsl:text>{</xsl:text>
<xsl:value-of select="@TEXT"/><xsl:text>}

</xsl:text></xsl:if>

<xsl:if test="arrowlink/@DESTINATION != ''">
<xsl:text>\ref{</xsl:text>
<xsl:value-of select="arrowlink/@DESTINATION"/>
<xsl:text>}</xsl:text>
</xsl:if>

<xsl:if test="(count(ancestor::node())-2)=4">
<xsl:text>\paragraph</xsl:text>
<xsl:text>{</xsl:text>
<xsl:value-of select="@TEXT"/><xsl:text>}

</xsl:text>
<xsl:if test="current()/node">
<xsl:call-template name="itemization">
</xsl:call-template>
</xsl:if>
</xsl:if>

<!--<xsl:if test="(count(ancestor::node())-2)>4">

<xsl:call-template name="itemization"/>

</xsl:if>-->

<xsl:if test="5 > (count(ancestor::node())-2)">
<xsl:apply-templates select="node"/>
</xsl:if>


</xsl:template>

<xsl:template name="itemization">
<xsl:param name="i" select="current()/node" />

<xsl:text>\begin{itemize}
</xsl:text>
<xsl:for-each select="$i">

<xsl:if test="@ID != ''">
<xsl:text>\label{</xsl:text><xsl:value-of select="@ID"/><xsl:text>}</xsl:text>
</xsl:if>

<xsl:text>\item </xsl:text>
<xsl:value-of select="@TEXT"/>

<xsl:if test="arrowlink/@DESTINATION != ''">
<xsl:text>\ref{</xsl:text>
<xsl:value-of select="arrowlink/@DESTINATION"/>
<xsl:text>}</xsl:text>
</xsl:if>

<xsl:text>
</xsl:text>

</xsl:for-each >

<xsl:if test="$i/node">
<xsl:call-template name="itemization">
<xsl:with-param name="i" select="$i/node"/>
</xsl:call-template>
</xsl:if>
<xsl:text>\end{itemize}
</xsl:text>
</xsl:template>
<!--Text Process -->
<!--<xsl:apply-templates select="Body/node()"/>-->

<!-- End of Sections Processing -->



<!-- LaTeXChar: A recursif function that generates LaTeX special characters -->
<xsl:template name="LaTeXChar">
<xsl:param name="i"/>
<xsl:param name="l"/>

<xsl:variable name="SS">
<xsl:value-of select="substring(normalize-space(),$l - $i + 1,1)" />
</xsl:variable>

<xsl:if test="$i > 0">

<xsl:choose>
 <xsl:when test="$SS = 'é'">
 <xsl:text>\'{e}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'ê'">
 <xsl:text>\^{e}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'è'">
 <xsl:text>\`{e}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'ï'">
 <xsl:text>\"{\i}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'î'">
 <xsl:text>\^{i}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'à'">
 <xsl:text>\`{a}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'á'">
 <xsl:text>\'{a}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'â'">
 <xsl:text>\^{a}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'ç'">
 <xsl:text>\c{c}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'ô'">
 <xsl:text>\^{o}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'ù'">
 <xsl:text>\`{u}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = 'û'">
 <xsl:text>\^{u}</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = '|'">
 <xsl:text>$|$</xsl:text>
 </xsl:when>
 <xsl:when test="$SS = '_'">
 <xsl:text>\_</xsl:text>
 </xsl:when>
 <xsl:otherwise><xsl:value-of select="$SS"/></xsl:otherwise>
</xsl:choose>

<xsl:text></xsl:text> 

<xsl:call-template name="LaTeXChar">
<xsl:with-param name="i" select="$i - 1"/>
<xsl:with-param name="l" select="$l"/>

</xsl:call-template>
</xsl:if>

</xsl:template>
<!-- End of LaTeXChar template -->





<!-- Enumerates Process -->
<xsl:template match="Enumerates">
<xsl:text>
\begin{enumerate}</xsl:text>
<xsl:for-each select="Item">
<xsl:text>
\item </xsl:text>
<xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>
\end{enumerate}
</xsl:text>
</xsl:template> <!--Enumerates Process -->

<!-- Items Process -->
<xsl:template match="Items">
<xsl:text>
\begin{itemize}</xsl:text>
<xsl:for-each select="node">
<xsl:text>
\item </xsl:text>
<xsl:value-of select="@TEXT"/>
</xsl:for-each>
<xsl:text>
\end{itemize}
</xsl:text>
</xsl:template> <!--Items Process -->

</xsl:stylesheet>

