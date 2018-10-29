<?xml version='1.0'?>
<!--MINDMAPEXPORTFILTER tex  Latex Beamer  
License     : This code is released under the GPL. [http://www.gnu.org/copyleft/gpl.html]
Document    : mm2latexbeamer_richcontent.xsl based on mm2latexbeamer 
Description : Transforms freemind mm format to latex beamer presentations.

Orginal idea created by Joerg Feuerhake [joerg.feuerhake@free-penguin.org]
(original stylesheet) and Robert Ladstaetter [robert@ladstaetter.info] 
(small adaptions to fit into latex beamer scheme)
Bug fixing and features added by: Igor G. Olaizola [igor.go@gmail.com]
Richcontent adaptations made by: Igor G. Olaizola
Attribute feautres added by: Igor G. Olaizola
Image insertion (full slide and two columns) added by: Igor G. Olaizola

ChangeLog : 
Created on  : 01 February 2004
Updated    : 30 December 2006
Modified	: 29 November 2007 
Modified    : 30 April 2008: bug fixing, good idea but it didn't work (iolaizola)
Modified    : 21 October 2008 (iolaizola, some new modifications to 
		support images);
Modified    : 23 October 2008, cleanup
Modified    : 23 October 2008: Extension to more richcontent "notes" (iolaizola)
Modified    : 28 October: some minor format changes
Modified    : 07 December 2008: Including text in richcontent mode (v1.5)
Modified    : 09 December 2008: Bug fixing in richcontent mode (v1.6)
Modified    : 01 January 2009: Notes in the third level accepted as 
		main text for the slide (v.1.7) (iolaizola)
Modified    : 01 January 2009: Bug fixing: Notes were not fully compatible
		 with "items" richcontent, some html spacing issues solved (v.1.71) (iolaizola) 
Modified    : 07 January 2009: HTML code of images can be now directly 
		edited in freemind (<p> effect) (v.1.72) (iolaizola)
Modified   : 04 May 2009: Fixing some bugs detected in version 1.72 ( iolaizola)
Modified:  : 21 July 2009: One attribute can be read from 3d, level.  
		(allowframebreaks, shrink, plain) version 1.74 (iolaizola)
Modified   : 21 July 2009: More than one attribute can be read from 3d level. (v.1.75)(iolaizola)
Modified   : 21 July 2009: Coma correction with attributes. (v.1.76) (iolaizola)
Modified   : 28 April 2010: Figure captions added as attributes (v.1.77) (rodrigo.goya) and
first page established as "Plain".
Modified   : 5 May 2010: Variable width columns allowed in two column mode. (rodrigo.goya & iolaizola). (v1.80)
NOTE: . From now on, "allowframebreaks, plain and shrink" attributes will be the only attributes which don't require the  attribute name (due to backwards compatibility reasons).
Modified:  : 6 May 2010: Multiple attributes allowed. (iolaizola). Note, default frame format is "shrink", there are problems to place the comas with the XLS template. (v 1.81)
Modified    : 9 May 2010. Coma issues solved with format styles. Now there is no need for a defalut frame style like "shrink" (iolaizola) (v.1.83)
Modified    : 9 May 2010. "squeeze" format style included (iolaizola) (v.1.835)
Modified    : 11 May 2010. "width" option added as attribute for figures and improvements to allow multiple attributes (iolaizola) (v.1.84)
Modified    : 11 May 2010. "framestyle" attribute name featured (iolaizola) (v.1.85)
Modified    : 13 May 2010  "subtitle" "author" and "date" attributes in main node.  (iolaizola) (v.1.9)
Modified    : 13 May 2010  "Unescape encoding activated. (iolaizola) (v.1.95)
Modified    : 17 May 2010 "Appendix" option added (v.1.955) (rodrigo.goya & iolaizola)
Modified    : 19 May 2010Issues fixed: Author, date, options.... (v.1.958)(iolaizola) \begin{document} and \end{document} are included within the content.tex file. Ported to XSL 2.0. Fixed compatibility issues for Saxon9. Ready to be tested for v2.0
Modified    : 20 May 2010. "Institute option added in main node (v.1.96)
Modified: 1 October 2010: Minor bug correction: "heigth" -> "height"
Modified: 23 July 2010: Changes in strucutre. In order to avoid the dependance on the main doc. A main documente will be directly generated. It will call the "theme" as attribute in the main node. Default theme will be assigned. (iolaizola)
Modified: 3 October 2010: Frame background properties added through attributes (iolaizola 1.99)
Modified: 5 October 2010: Frame background color properties added. First 2.0 release candidate (iolaizola)
Modified: 22 September 2011: Clean up and other changes proposed by Guy Kloss. PDF metadata added. "{" substituted by "\begin{frame}" (iolaizola).
Modified: 7 February 2012: Error corrections from last version, (2 title pages, hpyersetup errors, etc. (iolaizola).
Modified: 7 February 2012: Cloud -> Block option added .(iolaizola )
Modified: 7 February 2012: Cloud -> Freeplane Latex Equation hook included .(iolaizola 2.02)
Modified: 8 February 2012: Latex compiler errors solved (empty itemize sections) .(iolaizola 2.03)
Modified; 14th February 2012: "figuresp" template extended to make compatible with width, height, scacle attributes (like figures) [iolaizola 2.04]
Modified_ 16th of June 2014: Compatibility issues with ##1.3.x## version of freeplane. 
	1- LaTeX equations are identified as  "FORMAT="latexPatternFormat" within the node info. We keep the "hook" option for backwards compatibility. Warning: LaTeX Format will be exclusivelly considered for formulas (it will create an "equation" environment). 
	2- Now there are more "hooks" because IMAGES can also be inserted as "hooks" therefore, the old equation filter "hook" has to be adjusted to the specific type "hook equation". Hooks with NAME=ExternalObject will be considered as images. 
Modified: October 2018: Enable listings environment. (jose1711)

Thanks to: Gorka Marcos and Myriam Alustiza for giving the xsl syntax support 			  


See: http://freemind.sourceforge.net/
-->
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='2.0'>  
<xsl:output omit-xml-declaration="yes" encoding="ISO-8859-1" use-character-maps="returns" /> 


<xsl:character-map name="returns">
  <xsl:output-character character="&#13;" string="&#xD;"/>
 </xsl:character-map> 



<xsl:template match="map">

<!-- ==== HEADER ==== -->
<xsl:text>


\documentclass[usepdftitle=false,professionalfonts,compress ]{beamer}

%Packages to be included
\usepackage[latin1]{inputenc}
%\usepackage{beamerthemesplit}
\usepackage{graphics,epsfig, subfigure}
\usepackage{url}
\usepackage[T1]{fontenc}
\usepackage[english]{babel}
\usepackage{listings}
\lstset{basicstyle={\ttfamily},
basewidth={0.5em}}
\RequirePackage{eurosym}
\usepackage{hyperref}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%% PDF meta data inserted here %%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\hypersetup{
	pdftitle={</xsl:text><xsl:value-of select="node/@TEXT" disable-output-escaping="yes"/><xsl:text>},
	pdfauthor={</xsl:text><xsl:if test="node/attribute/@NAME = 'author' "><xsl:value-of select="node/attribute[@NAME = 'author']/@VALUE" disable-output-escaping="yes"/></xsl:if><xsl:text>}
}


</xsl:text>


%%%%%% Beamer Theme %%%%%%%%%%%%%
<xsl:choose>
	<xsl:when test="node/attribute/@NAME = 'theme' ">
		<xsl:text>&#xD;\usetheme[]{</xsl:text>
		<xsl:value-of select="node/attribute[@NAME = 'theme']/@VALUE" disable-output-escaping="yes"/>
		<xsl:text>}</xsl:text>
	</xsl:when>
	<xsl:otherwise>
		<xsl:text>
\usetheme[]{Darmstadt}
		</xsl:text>
	</xsl:otherwise>
</xsl:choose>


<xsl:text disable-output-escaping="yes">
\title{</xsl:text><xsl:value-of select="node/@TEXT" disable-output-escaping="yes"/><xsl:text>}</xsl:text>

<xsl:if test="node/attribute/@NAME = 'subtitle' ">
	<xsl:text>&#xD;\subtitle{</xsl:text>
	<xsl:value-of select="node/attribute[@NAME = 'subtitle']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
</xsl:if>
<xsl:if test="node/attribute/@NAME = 'author' ">
	<xsl:text>&#xD;\author{</xsl:text>
	<xsl:value-of select="node/attribute[@NAME = 'author']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
</xsl:if>
<xsl:if test="node/attribute/@NAME = 'institute' ">
	<xsl:text>&#xD;\institute{</xsl:text>
	<xsl:value-of select="node/attribute[@NAME = 'institute']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
</xsl:if>
<xsl:if test="node/attribute/@NAME = 'date' ">
	<xsl:text>&#xD;\date{</xsl:text>
	<xsl:value-of select="node/attribute[@NAME = 'date']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
</xsl:if>






%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%% Begin Document  %%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


<xsl:text>

\begin{document}
\frame[plain]{
	\frametitle{}
	\titlepage
	\vspace{-0.5cm}
	\begin{center}
	%\frontpagelogo
	\end{center}
}
\frame{
	\tableofcontents[hideallsubsections]
}
</xsl:text>





%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%   
%%%%%%%%%% Content starts here %%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


<!-- <xsl:apply-templates select="node"/> -->
<xsl:apply-templates/>

<xsl:text>

\end{document}</xsl:text>

</xsl:template>


<!-- ======= Body ====== -->


<xsl:template match="richcontent">
</xsl:template> <!--Avoids to write notes contents at the end of the document-->

<xsl:template match="node">

	<xsl:if test="(count(ancestor::node())-2)=0">
		<xsl:apply-templates/>
	</xsl:if>

		
	<xsl:if test="(count(ancestor::node())-2)=1">
		<xsl:if test="(current()/attribute/@NAME = 'appendix')">
				<xsl:text>\appendix{</xsl:text>
				<xsl:value-of select="normalize-space(current()/attribute/@VALUE)" disable-output-escaping="yes"/>
				<xsl:text>}&#xD; </xsl:text>
		</xsl:if>
		<xsl:text>\section{</xsl:text><xsl:value-of select="@TEXT" disable-output-escaping="yes"/><xsl:text>}
		</xsl:text>
		<xsl:apply-templates/>
	</xsl:if>
	
	<xsl:if test="(count(ancestor::node())-2)=2">
		<xsl:text>\subsection{</xsl:text><xsl:value-of select="@TEXT" disable-output-escaping="yes"/><xsl:text>}</xsl:text>
		<xsl:if test="current()/richcontent/html/body/img">
			<xsl:call-template name="figures"></xsl:call-template>
		</xsl:if>
		<xsl:apply-templates/>
	</xsl:if>
	<xsl:if test="(count(ancestor::node())-2)=3"> <!-- We are starting a frame-->
		
		<xsl:if test="current()/attribute/@NAME = 'backgroundpicture' ">
			<xsl:text>
\usebackgroundtemplate{\includegraphics	[width=\paperwidth,height=\paperheight]%
	{</xsl:text>
			<xsl:value-of select="current()/attribute[@NAME = 'backgroundpicture']/@VALUE" disable-output-escaping="yes"/>
			<xsl:text>}}&#xD;</xsl:text>
		</xsl:if>
		
		<xsl:if test="current()/attribute/@NAME = 'backgroundcolor' ">
			<xsl:text>&#xD;\beamertemplateshadingbackground{</xsl:text>
			<xsl:value-of select="current()/attribute[@NAME = 'backgroundcolor']/@VALUE" disable-output-escaping="yes"/>
			<xsl:text>}{</xsl:text>
			<xsl:value-of select="current()/attribute[@NAME = 'backgroundcolor']/@VALUE" disable-output-escaping="yes"/>
			<xsl:text>}&#xD;</xsl:text>
		</xsl:if>
		
		<xsl:text>\begin{frame}</xsl:text>

<!--We look if there are attributes in the frame in order to put properties or not and we put them as [property1, property2]: Only "allowframebreaks" "shrink"  "plain", "squeeze" and "fragile" are allowed"-->
		<xsl:if test="(current()/attribute/@VALUE = 'allowframebreaks') or (current()/attribute/@VALUE = 'shrink') or (current()/attribute/@VALUE = 'plain') or (current()/attribute/@VALUE = 'squeeze') or (current()/attribute/@VALUE = 'fragile') or (current()/attribute/@NAME = 'framestyle' )">
				<xsl:call-template name="framestyle"></xsl:call-template>
		</xsl:if>


		<xsl:text>\frametitle{</xsl:text><xsl:value-of select="normalize-space(@TEXT)" disable-output-escaping="yes"/>
		<xsl:text>}&#xD;</xsl:text>
			
			<!--We look if there are images in the frame in order to put columns or not-->
			<!--<xsl:if test="current()/node/richcontent/html/body">
				<xsl:text> Note detected</xsl:text>
			</xsl:if>-->

			<xsl:if test = "contains(current()/richcontent/@TYPE,'NOTE') ">
				<xsl:call-template name="richtext"></xsl:call-template>
			</xsl:if>

 <!-- in older versions (1.1.x, 1.2.x) images are nodes without TEXT. When they are alone in a slide the itemizing part and two column building template must be skipped.-->
			<xsl:choose>
				<xsl:when test="current()/node/@TEXT">
					<xsl:choose>
						<xsl:when test="current()/node/richcontent/html/body/img or current()/node/richcontent/html/body/p/img or (current()/node/hook/@NAME='ExternalObject' and count(*) &gt; 1)"> <!-- now it is compatible with 1.3.x versions -->
							<xsl:call-template name="build_two_column_frame"></xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="current()/node and current()/node/@TEXT != ''">
								<xsl:call-template name="itemization"></xsl:call-template>
							</xsl:if>
							<xsl:if test="current()/node/richcontent/html/body/img">
								<xsl:call-template name="figures"></xsl:call-template>
							</xsl:if>
							<xsl:if test="current()/node/richcontent/html/body/p/img">
								<xsl:call-template name="figuresp"></xsl:call-template>
							</xsl:if>
								<xsl:if test="current()/node/hook/@NAME='ExternalObject'">
								<xsl:call-template name="ExternalObject"></xsl:call-template>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise> 
					<xsl:if test="current()/node/richcontent/html/body/img">
						<xsl:call-template name="figures"></xsl:call-template>
					</xsl:if>	
					<xsl:if test="current()/node/richcontent/html/body/p/img">
						<xsl:call-template name="figuresp"></xsl:call-template>
					</xsl:if>
					<xsl:if test="current()/node/richcontent/html/body/p/@text">
						<xsl:call-template name="itemization"></xsl:call-template>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xD;\end{frame}</xsl:text>
		<xsl:apply-templates/>
	</xsl:if>
</xsl:template>

<xsl:template name="itemization">
	<xsl:param name="i" select="current()/node"/>
	<xsl:if test="not(./cloud) and not(../cloud) and not(../../cloud)">
		<xsl:text>&#xD;&#xA;&#x9;\begin{itemize}&#xD;&#xA;</xsl:text>
    </xsl:if>	
	<xsl:for-each select="$i">
		<xsl:choose>
			<xsl:when  test="current()/cloud">
				<xsl:text>\begin{block}{</xsl:text>
				<xsl:value-of select="@TEXT" disable-output-escaping="yes"/>
				<xsl:text>}&#xA;</xsl:text>
				<xsl:if test="current()/node">
					<xsl:call-template name="textify"></xsl:call-template>
				</xsl:if>		
				<xsl:text>\end{block}</xsl:text>
			</xsl:when>			
			<xsl:when test="@TEXT and not(../cloud) and not(../../cloud)">  <!-- normal node case-->
				<xsl:text>	\item </xsl:text>

				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text> $</xsl:text>
				</xsl:if>

				<xsl:value-of select="@TEXT" disable-output-escaping="yes"/>

				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>$&#xA;&#x9;</xsl:text>
				</xsl:if>

				<xsl:text>&#xD;</xsl:text>
				<xsl:if  test="current()/hook/@EQUATIION"> <!-- Equation filter for 1.1.x and 1.2.x versions-->
					<xsl:text>&#xA;\begin{equation*}&#xA;&#x9;</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
				<xsl:text>&#xA;\end{equation*}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:when test="current()/richcontent/html/body/p/@text and not(../cloud) and not(../../cloud)">
				<xsl:text>	\item </xsl:text>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text> $</xsl:text>
				</xsl:if>
				<xsl:call-template name="richtext"></xsl:call-template>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>$&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:if  test="current()/hook/@EQUATIION">
					<xsl:text>&#xA;\begin{equation*}&#xA;&#x9;</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
				<xsl:text>&#xA;\end{equation*}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="current()/node">
			<xsl:call-template name="itemization"></xsl:call-template>
		</xsl:if>
		<xsl:text>
		</xsl:text>	
	</xsl:for-each>

	<xsl:if test="not(./cloud) and not(../cloud) and not(../../cloud)">
		<xsl:text>		\end{itemize}&#xA;</xsl:text>
    </xsl:if>	
</xsl:template>

<!-- template to parse and insert rich text (html, among <p> in Latex \item-s -->
<xsl:template name="richtext">
	<xsl:param name="i" select="current()/richcontent/html/body/p"/>
	<xsl:for-each select="$i">
		<xsl:value-of select="normalize-space(translate(.,'&#x0d;&#x0a;', '  '))" disable-output-escaping="yes"/>
	</xsl:for-each>
</xsl:template>


<!-- template to parse and insert text in block mode-->
<xsl:template name="textify">
	<xsl:param name="i" select="current()/node"/>
	<xsl:for-each select="$i">
		<xsl:choose>
			<xsl:when test="@TEXT">
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text>&#xA;\begin{equation}</xsl:text>
				</xsl:if>
				<xsl:value-of select="@TEXT" disable-output-escaping="yes"/>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>\end{equation}&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:if  test="current()/hook/@EQUATIION">
					<xsl:text>&#xA;\begin{equation}&#xA;&#x9;</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
					<xsl:text>&#xA;\end{equation}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:when test="current()/richcontent/html/body/p/@text ">
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text>&#xA;\begin{equation*}</xsl:text>
				</xsl:if>
				<xsl:call-template name="richtext"></xsl:call-template><xsl:text>&#xD;</xsl:text>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>\end{equation}&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:if  test="current()/hook/@EQUATIION">
					<xsl:text>&#xA;\begin{equation}</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
					<xsl:text>\end{equation}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="current()/node">
			<xsl:call-template name="block_itemize"></xsl:call-template>
		</xsl:if>
		<xsl:text>
		</xsl:text>	
	</xsl:for-each>
</xsl:template>

<xsl:template name="block_itemize">
	<xsl:param name="i" select="current()/node"/>
	<xsl:text>&#xD;&#xA;&#x9;\begin{itemize}&#xD;&#xA;</xsl:text>
	<xsl:for-each select="$i">
		<xsl:choose>
			<xsl:when test="@TEXT">
				<xsl:text>	\item </xsl:text>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text>&#xA;\begin{equation*}&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:value-of select="@TEXT" disable-output-escaping="yes"/>
				<xsl:text>&#xD;</xsl:text>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>&#xA;\end{equation*}&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:if  test="current()/hook/@EQUATIION">
					<xsl:text>&#xA;\begin{equation}</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
					<xsl:text>\end{equation}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:when test="current()/richcontent/html/body/p/@text">
				<xsl:text>	\item </xsl:text>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions begin-->
					<xsl:text>&#xA;\begin{equation}</xsl:text>
				</xsl:if>
				<xsl:call-template name="richtext"></xsl:call-template>
				<xsl:if test="current()/@FORMAT = 'latexPatternFormat'"> <!-- Equation filter for 1.3.x versions end-->
					<xsl:text>\end{equation}&#xA;&#x9;</xsl:text>
				</xsl:if>
				<xsl:if  test="current()/hook/@EQUATIION">
					<xsl:text>&#xA;\begin{equation}</xsl:text>
					<xsl:value-of select="current()/hook/@EQUATION" disable-output-escaping="yes"/>
					<xsl:text>\end{equation}</xsl:text>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
			</xsl:otherwise>
		</xsl:choose>
		
		<xsl:if test="current()/node">
			<xsl:call-template name="block_itemize"></xsl:call-template>
		</xsl:if>
		<xsl:text>
		</xsl:text>	
	</xsl:for-each>

	<!--<xsl:if test="current()/richcontent">
			<xsl:call-template name="figures"></xsl:call-template>
		</xsl:if>-->
	<xsl:text>		\end{itemize}&#xA;</xsl:text>
</xsl:template>

						
<!-- template to parse and insert figures New version provided by Rodrigo Goya-->


<!-- template to parse and insert figures with manually edited html. (inside <p>)-->
<xsl:template name="figuresp">
	<xsl:text>
		\includegraphics[</xsl:text>
	<xsl:choose>
		<xsl:when test="current()/node/attribute/@NAME = 'width'">
			<xsl:text>width=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'width']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'height'">
			<xsl:text>height=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'height']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'scale'">
			<xsl:text>scale=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'scale']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>width=.97\textwidth</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>]{</xsl:text>
		<xsl:value-of 	select="current()/node/richcontent/html/body/p/img/@src" disable-output-escaping="yes"/>
		<xsl:text>}
		</xsl:text>
</xsl:template>




<xsl:template name="figures">
	<xsl:text>
\begin{figure}
	\includegraphics[</xsl:text>
	<xsl:choose>
		<xsl:when test="current()/node/attribute/@NAME = 'width'">
			<xsl:text>width=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'width']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'height'">
			<xsl:text>height=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'height']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'scale'">
			<xsl:text>scale=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'scale']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>width=.97\textwidth</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>]{</xsl:text>
	<xsl:value-of select="current()/node/richcontent/html/body/img/@src" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
	<xsl:if test="current()/node/attribute/@NAME = 'caption'">
		<xsl:text>\newline{\scriptsize{</xsl:text>
		<xsl:value-of select="current()/node/attribute[@NAME = 'caption']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}}</xsl:text>
	</xsl:if>
	<xsl:text>\end{figure}</xsl:text>
</xsl:template>





<!-- template to parse and insert figures with 1.3.x format: hook with @NAME=ExternalObject. (inside <p>)-->

<xsl:template name="ExternalObject">
	<xsl:text>
\begin{figure}
	\includegraphics[</xsl:text>
	<xsl:choose>
		<xsl:when test="current()/node/attribute/@NAME = 'width'">
			<xsl:text>width=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'width']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'height'">
			<xsl:text>height=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'height']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:when test="current()/node/attribute/@NAME = 'scale'">
			<xsl:text>scale=</xsl:text>
			<xsl:value-of select="current()/node/attribute[@NAME = 'scale']/@VALUE" disable-output-escaping="yes"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:text>width=.97\textwidth</xsl:text>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:text>]{</xsl:text>
		<xsl:value-of select="current()/node/hook/@URI" disable-output-escaping="yes"/>
	<xsl:text>}</xsl:text>
	<xsl:if test="current()/node/attribute/@NAME = 'caption'">
		<xsl:text>\newline{\scriptsize{</xsl:text>
		<xsl:value-of select="current()/node/attribute[@NAME = 'caption']/@VALUE" disable-output-escaping="yes"/>
	<xsl:text>}}</xsl:text>
	</xsl:if>
	<xsl:text>\end{figure}</xsl:text>
</xsl:template>






<!--We look if there are images in the frame in order to put columns or not-->
				<!--<xsl:if test="current()/node/richcontent/html/body">
					<xsl:text> Note detected</xsl:text>
			<xsl:value-of select="1 - current()/attribute/@VALUE + 0.18"/>
				</xsl:if>-->
<xsl:template name="build_two_column_frame">
	<xsl:text>\begin{columns}&#xD;</xsl:text>
	<xsl:choose>
		<xsl:when test="current()/attribute/@NAME = 'leftcolumnwidth' ">
			<xsl:text>	\begin{column}{</xsl:text>
			<xsl:value-of select="current()/attribute/@VALUE" disable-output-escaping="yes"/>
			<xsl:text>\textwidth}&#xD;</xsl:text>
		</xsl:when>
	  <xsl:otherwise>
	  	<xsl:text>	\begin{column}{0.65\textwidth}&#xD;</xsl:text>
	  </xsl:otherwise>
	</xsl:choose>
	
	<xsl:if test="current()/node">
		<xsl:call-template name="itemization"></xsl:call-template>
	</xsl:if>
	<xsl:text>	\end{column}&#xD;</xsl:text>

	<xsl:if test="current()/node/richcontent/html/body/img">
		<xsl:choose>
			<xsl:when test="current()/attribute/@NAME = 'leftcolumnwidth' ">
				<xsl:text>	\begin{column}{</xsl:text>
				<xsl:value-of select="1 - number(current()/attribute[@NAME = 'leftcolumnwidth']/@VALUE) + .18"/>
				<xsl:text>\textwidth}&#xD;</xsl:text>
			</xsl:when>
		  <xsl:otherwise>
		  	<xsl:text>	\begin{column}{0.53\textwidth}&#xD;</xsl:text>
		  </xsl:otherwise>
		</xsl:choose>
		<xsl:call-template name="figures"></xsl:call-template>
		<xsl:text>\end{column}&#xD;</xsl:text>
	</xsl:if>

	<xsl:if test="current()/node/richcontent/html/body/p/img">
		<xsl:text>\begin{column}{0.53\textwidth}&#xD;</xsl:text>
		<xsl:call-template name="figuresp"></xsl:call-template>
		<xsl:text>\end{column}&#xD;</xsl:text>
	</xsl:if>


	<xsl:if test="current()/node/hook/@NAME='ExternalObject'"> <!--New format in 1.3.x-->
		<xsl:text>\begin{column}{0.53\textwidth}&#xD;</xsl:text>
		<xsl:call-template name="ExternalObject"></xsl:call-template>
		<xsl:text>\end{column}&#xD;</xsl:text>
	</xsl:if>
	<xsl:text>\end{columns}&#xD;</xsl:text>
	
</xsl:template>

<xsl:template match="text">
   <Notes><xsl:value-of select="text" disable-output-escaping="yes"/></Notes>
 </xsl:template>


<xsl:template name="framestyle"> <!--Takes the attributes of the freemind and puts all the values as [value1, value2]   http://www.xml.com/pub/a/2002/04/03/attributes.html-->
	<xsl:param name="att" select="current()/attribute/@VALUE"/> 
	<xsl:param name="nameatt" select="current()/attribute/@NAME"/>

	<xsl:for-each select="current()/attribute/@*"> <!-- it produces a warning, we should avoid using this empty loop -->
	</xsl:for-each>
	<xsl:text>[</xsl:text>
	<xsl:call-template name="extract_framestyle_attributes">
		<xsl:with-param name="counter" select="1" />
		<xsl:with-param name="att" select="$att"/> 
		<xsl:with-param name="nameatt" select="$nameatt"/>
		<xsl:with-param name="numel" select="last()"/> 
		<xsl:with-param name="num_framestyle_atts" select="0"/>
	</xsl:call-template>
	<xsl:text>]</xsl:text>
 </xsl:template>


<xsl:template name="extract_framestyle_attributes"> 
	<xsl:param name="counter" />
	<xsl:param name="att" />
	<xsl:param name="nameatt" />
	<xsl:param name="numel" />
	<xsl:param name="num_framestyle_atts" /> 
	
	<xsl:if test="$counter &lt; $numel + 1">
		<xsl:choose>			
			<xsl:when test="$att[$counter] = 'shrink' or  $att[$counter] = 'plain' or $att[$counter] = 'fragile' or  $att[$counter] = 'allowframebreaks'   or  $att[$counter] = 'squeeze' or $nameatt[$counter] = 'framestyle' "> 
				<xsl:if test="$num_framestyle_atts &gt; 0"> <!--We check if it is the first element or not to put the coma.-->
				<xsl:text>,</xsl:text>
				</xsl:if>
				<xsl:value-of select="$att[$counter]/." disable-output-escaping="yes"/><!--putting the attribute value.-->

				<xsl:call-template name="extract_framestyle_attributes">
						<xsl:with-param name="counter" select="$counter + 1" />
						<xsl:with-param name="att"  select="$att"/>
						<xsl:with-param name="nameatt" select="$nameatt" />
						<xsl:with-param name="numel" select="$numel" />
						<xsl:with-param name="num_framestyle_atts" select="$num_framestyle_atts + 1"/> 
				</xsl:call-template>
			</xsl:when>

			<xsl:otherwise>
			<xsl:call-template name="extract_framestyle_attributes">
						<xsl:with-param name="counter" select="$counter + 1" />
						<xsl:with-param name="att" select="$att"/>
						<xsl:with-param name="nameatt" select="$nameatt"/>
						<xsl:with-param name="numel" select="$numel"/>
						<xsl:with-param name="num_framestyle_atts" select="$num_framestyle_atts"/> 
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>	
	</xsl:if>
</xsl:template>

 <!-- End of LaTeXChar template -->

</xsl:stylesheet>

