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
  <xsl:import href="mm2latexinput.xsl"/>
	<xsl:output omit-xml-declaration="yes" />

	<xsl:template match="/"> <!-- map -->

		<xsl:text>
\documentclass[a4paper,12pt,single,pdftex]{scrartcl}
%\usepackage[ngerman]{babel}
\usepackage{color}
\usepackage{amsmath}
\usepackage{times}
\usepackage{graphicx}
\usepackage{fancyheadings}
\usepackage{hyperref}
\setlength{\parindent}{0.6pt}
\setlength{\parskip}{0.6pt}
\title{</xsl:text>
		<xsl:value-of select="/map/node/@TEXT" />
		<xsl:text>}
 </xsl:text>
		<!-- ======= Document Begining ====== -->
		<xsl:text>

\begin{document} 
\maketitle
\newpage
%\tableofcontents
\newpage

</xsl:text>

<!-- call stuff in mm2latexinput.xsl -->
<xsl:apply-templates select="map"/>

<xsl:text>

\end{document}
</xsl:text>
</xsl:template>

</xsl:stylesheet>