<?xml version='1.0'?>


	<!--
		MINDMAPEXPORTFILTER tex  Latex input
		
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
	<xsl:output omit-xml-declaration="yes"  method="text"/>

  <xsl:param name="sectionLevel1" select="'section'"/>
  <xsl:param name="sectionLevel2" select="'subsection'"/>
  <xsl:param name="sectionLevel3" select="'subsubsection'"/>

	<xsl:template match="map">
		<xsl:apply-templates select="node/node" />
  </xsl:template>

<!-- ======= Body ====== -->

<!-- Sections Processing -->
<xsl:template match="node">

<xsl:variable name="target" select="arrowlink/@DESTINATION"/>

<xsl:choose>
<xsl:when test="hook[@NAME='ExternalObject']">
<xsl:text>\begin{figure}[htb]
\begin{center}
\includegraphics[width=12cm]{</xsl:text>
<xsl:value-of select="substring-before(hook/@URI, '.png')"/>
<xsl:text>}
\caption{</xsl:text>
<xsl:apply-templates select="@TEXT|richcontent" />
<xsl:text>}
\end{center}
\end{figure}
</xsl:text>
</xsl:when>
<xsl:when test="count(ancestor::node())-2&lt;=1">
<xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel1"/>
<xsl:text>{</xsl:text>
<xsl:apply-templates select="@TEXT|richcontent" /><xsl:text>}
</xsl:text></xsl:when>
<xsl:when test="node and not(@LOCALIZED_STYLE_REF) and count(ancestor::node())-2=2">
<xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel2"/>
<xsl:text>{</xsl:text>
<xsl:apply-templates select="@TEXT|richcontent" /><xsl:text>}
</xsl:text></xsl:when>
<xsl:when test="node and not(@LOCALIZED_STYLE_REF) and count(ancestor::node())-2=3">
<xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel3"/>
<xsl:text>{</xsl:text>
<xsl:apply-templates select="@TEXT|richcontent" /><xsl:text>}
</xsl:text></xsl:when>
<xsl:when test="node and not(@LOCALIZED_STYLE_REF)">
<xsl:text>\par \textbf</xsl:text>
<xsl:text>{</xsl:text>
<xsl:apply-templates select="@TEXT|richcontent"/>
<xsl:text>}
</xsl:text>
</xsl:when>
<xsl:when test="@TEXT=''"></xsl:when>
<xsl:otherwise>

<xsl:choose>
<!-- read a latex file from the file system (export only) -->
<xsl:when test="starts-with(@TEXT, '\latexinput{') and substring(@TEXT, string-length(@TEXT))='}'">
  <xsl:value-of select="document(substring-before(substring-after(@TEXT, '\latexinput{'), '}'))/*"/>
</xsl:when>

<!-- treat a node as latex when '\latex[ \n]' prefix is present -->
<xsl:when test="starts-with(@TEXT, '\latex ') or starts-with(@TEXT, '\latex&#10;')">

  <xsl:value-of select="substring-after(@TEXT, '\latex')"/>
  <xsl:text>

</xsl:text>    
</xsl:when>

<!-- treat a node as latex when '\unparsedlatex[ \n]' prefix is present (export only) -->
<xsl:when test="starts-with(@TEXT, '\unparsedlatex ') or starts-with(@TEXT, '\unparsedlatex&#10;')">

  <xsl:value-of select="substring-after(@TEXT, '\unparsedlatex')"/>
  <xsl:text>

</xsl:text>    
</xsl:when>

<!-- treat a node as latex with format=(LaTeX|Unparsed LaTeX) -->
<xsl:when test="@FORMAT='latexPatternFormat' or @FORMAT='unparsedLatexPatternFormat'">
  <!--<xsl:apply-templates select="@TEXT|richcontent"  mode="rawLatex"/>-->
  <xsl:value-of select="@TEXT"/>
  <xsl:text>

  </xsl:text>    
</xsl:when>
<!-- non-latex content: escape! -->
<xsl:otherwise>
  <xsl:apply-templates select="@TEXT|richcontent"  mode="addEol"/>
</xsl:otherwise>
</xsl:choose>
</xsl:otherwise>
</xsl:choose>

<xsl:apply-templates select="node" />
</xsl:template>

<xsl:template match="richcontent"  >
	<xsl:apply-templates select="html"/>
</xsl:template>
<!--Text Process -->
<!--<xsl:apply-templates select="Body/node()"/>-->

<!-- End of Sections Processing -->
<xsl:template match="@TEXT" mode="addEol">
<!--
	<xsl:text>
\par </xsl:text>	<xsl:apply-templates select="."/>
-->
<xsl:text>

</xsl:text>
<xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="richcontent" mode="addEol">
	<xsl:apply-templates select="."/>
</xsl:template>

<!-- LaTeXChar: A recursive function that generates LaTeX special characters -->
<xsl:template match = "@*|text()" mode="rawLatex">
	<xsl:value-of select="."/>
</xsl:template>
<xsl:template match = "@*|text()">	
  <xsl:call-template name="esc">
    <xsl:with-param name="c" select='"&#160;"'/>
    <xsl:with-param name="s">
  <xsl:call-template name="esc">
    <xsl:with-param name="c" select='"#"'/>
    <xsl:with-param name="s">
      <xsl:call-template name="esc">
    <xsl:with-param name="c" select='"$"'/>
    <xsl:with-param name="s">
      <xsl:call-template name="esc">
        <xsl:with-param name="c" select='"%"'/>
        <xsl:with-param name="s">
          <xsl:call-template name="esc">
        <xsl:with-param name="c" select='"&amp;"'/>
        <xsl:with-param name="s">
          <xsl:call-template name="esc">
            <xsl:with-param name="c" select='"~"'/>
            <xsl:with-param name="s">
              <xsl:call-template name="esc">
            <xsl:with-param name="c" select='"_"'/>
            <xsl:with-param name="s">
              <xsl:call-template name="esc">
                <xsl:with-param name="c" select='"^"'/>
                <xsl:with-param name="s">
                  <xsl:call-template name="esc">
                <xsl:with-param name="c" select='"{"'/>
                <xsl:with-param name="s">
                  <xsl:call-template name="esc">
                    <xsl:with-param name="c" select='"}"'/>
                    <xsl:with-param name="s">
                      <xsl:call-template name="esc">
                    <xsl:with-param name="c" select='"\"'/>
                    	<xsl:with-param name="s">
                     		<xsl:value-of select="."/>
                    	</xsl:with-param>
                      </xsl:call-template>
                    </xsl:with-param>
                  </xsl:call-template>
                </xsl:with-param>
                  </xsl:call-template>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:with-param>
              </xsl:call-template>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
          </xsl:call-template>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:with-param>
      </xsl:call-template>
    </xsl:with-param>
  </xsl:call-template>
  </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<xsl:template name="esc">
  <xsl:param name="s"/>
  <xsl:param name="c"/>

  <xsl:choose>
    <xsl:when test='contains($s, $c)'>
      <xsl:value-of select='substring-before($s, $c)'/>
      <xsl:choose>
    <xsl:when test='$c = "\"'>
      <xsl:text>\textbackslash </xsl:text>
    </xsl:when>
    <xsl:when test='$c = "&#160;"'>
      <xsl:text> </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      	<xsl:text>\</xsl:text>
      <xsl:value-of select='$c'/>
    </xsl:otherwise>
      </xsl:choose>

      <xsl:call-template name="esc">
    <xsl:with-param name='c' select='$c'/>
    <xsl:with-param name='s' select='substring-after($s, $c)'/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select='$s'/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- End of LaTeXChar template -->


<!-- XHTML -->
<xsl:template match="html">
  <xsl:apply-templates select="body"/>
</xsl:template>

<!-- body sections -->
<xsl:template match="h1">
  <xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel1"/><xsl:text>{</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}
  </xsl:text>
</xsl:template>

<xsl:template match="h2">
  <xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel2"/><xsl:text>{</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}
  </xsl:text>
</xsl:template>

<xsl:template match="h3">
  <xsl:text>\</xsl:text><xsl:value-of select="$sectionLevel3"/><xsl:text>{</xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}
  </xsl:text>
</xsl:template>

<!-- section labels. -->
<!-- lists -->
<xsl:template match="ul">
  <xsl:text>\begin{itemize}
  </xsl:text>
  <xsl:for-each select="li">
    <xsl:text>\item </xsl:text>
    <xsl:apply-templates />
  </xsl:for-each>
  <xsl:text>
    \end{itemize}
  </xsl:text>
</xsl:template>

<xsl:template match="ol">
  <xsl:text>\begin{enumerate}
  </xsl:text>
  <xsl:for-each select="li">
    <xsl:text>\item </xsl:text>
    <xsl:apply-templates />
  </xsl:for-each>
  <xsl:text>
    \end{enumerate}
  </xsl:text>
</xsl:template>

<xsl:template match="dl">
  <xsl:text>\begin{description}
  </xsl:text>
  <xsl:for-each select="*">
    <xsl:if test='local-name() = "dt"'>
      <xsl:text>\item[</xsl:text>
    </xsl:if>
    <xsl:apply-templates />

    <xsl:if test='local-name() = "dt"'>
      <xsl:text>] </xsl:text>
    </xsl:if>
  </xsl:for-each>
  <xsl:text>
    \end{description}
  </xsl:text>
</xsl:template>

<!-- tables -->
<xsl:template match="table">
  <xsl:text>\begin{center}</xsl:text>
  <xsl:text>\begin{tabular}{|</xsl:text>
  <xsl:for-each select="tr[1]/*">
    <xsl:text>c|</xsl:text>
  </xsl:for-each>
  <xsl:text>}&#10;</xsl:text>

  <xsl:for-each select="tr">
    <xsl:text>\hline&#10;</xsl:text>
    <xsl:for-each select="*">
      <xsl:if test="name() = 'th'">{\bf </xsl:if>
      <xsl:apply-templates />
      <xsl:if test="name() = 'th'">}</xsl:if>
      <xsl:if test="position() != last()">
    <xsl:text> &amp; </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text> \\&#10;</xsl:text>
  </xsl:for-each>
  <xsl:text>\hline&#10;</xsl:text>

  <xsl:text>\end{tabular}&#10;</xsl:text>
  <xsl:text>\end{center}&#10;</xsl:text>
</xsl:template>

<!-- ol, img code untested -->
<xsl:template match="img[@class='graphics'
          or @class='includegraphics']">
  <xsl:text>\includegraphics[width=</xsl:text>
  <xsl:value-of select="@width"/>
  <xsl:text>,height=</xsl:text>
  <xsl:value-of select="@height"/>
  <xsl:text>]{</xsl:text>
  <xsl:value-of select="@src"/>
  <xsl:text>}</xsl:text>
</xsl:template>


<!-- blockquote -->
<xsl:template match="blockquote">
  <xsl:text>
    \begin{quote}
  </xsl:text>
  <xsl:apply-templates />
  <xsl:text>
    \end{quote}
  </xsl:text>
</xsl:template>

<!-- misc pre/verbatim -->
<xsl:template match="pre">
  <xsl:text>\begin{verbatim}</xsl:text>
  <xsl:apply-templates mode="verbatim"/>
  <xsl:text>\end{verbatim}</xsl:text>
</xsl:template>


<!-- paragraphs -->

<xsl:template match="br">
	<xsl:text> \newline&#10;</xsl:text>
</xsl:template>

<xsl:template match="p">
  <xsl:choose>
  	<xsl:when test="string(.) != ''">
  <xsl:apply-templates/>
  <xsl:text>\\&#10;</xsl:text>
  	</xsl:when>
  	<xsl:otherwise>
		<xsl:text>&#10;&#10;</xsl:text>
  	</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- phrase markup -->

<xsl:template match="em|dfn">
  <xsl:text>{\em </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="code">
  <xsl:text>{\tt </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="tt">
  <xsl:text>{\tt </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="i">
  <xsl:text>{\it </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="b">
  <xsl:text>{\bf </xsl:text>
  <xsl:apply-templates/>
  <xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="q">
  <xsl:text>``</xsl:text>
  <xsl:apply-templates />
  <xsl:text>''</xsl:text>
</xsl:template>

<xsl:template match="samp">
  <!-- pass-thru, for \Sigma -->
  <xsl:text>$</xsl:text>
  <xsl:value-of select='.'/>
  <xsl:text>$</xsl:text>
</xsl:template>

<xsl:template match="samp" mode="math">
  <!-- pass-thru, for \Sigma -->
  <xsl:value-of select='.'/>
</xsl:template>
</xsl:stylesheet>

