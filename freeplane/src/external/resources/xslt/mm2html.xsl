<?xml version="1.0" encoding="UTF-8" ?>

<!--
    MINDMAPEXPORTFILTER html;htm %xslt_export.html

    : This code released under the GPL.
    : (http://www.gnu.org/copyleft/gpl.html) Document : mindmap2html.xsl
    Created on : 01 February 2004, 17:17 Author : joerg feuerhake
    joerg.feuerhake@free-penguin.org Description: transforms freeplane mm
    format to html, handles crossrefs font declarations and colors. feel
    free to customize it while leaving the ancient authors mentioned.
    thank you ChangeLog: See: http://freeplane.sourceforge.net/
  -->

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no" encoding="ISO-8859-1" />

  <xsl:template match="/">
    <xsl:variable name="mapversion" select="map/@version" />

    <html>
      <head>
        <title><xsl:value-of select="map/node/@TEXT"/>//mm2html.xsl FreeplaneVersion:<xsl:value-of select="$mapversion"/></title>
        <style>
          body{
          font-size:10pt;
          color:rgb(0,0,0);
          backgound-color:rgb(255,255,255);
          font-family:sans-serif;
          }
          p.info{
          font-size:8pt;
          text-align:right;
          color:rgb(127,127,127);
          }
        </style>
      </head>
      <body>

        <xsl:apply-templates/>

        <p class="info">
          <xsl:value-of select="map/node/@TEXT"/>//mm2html.xsl FreeplaneVersion:<xsl:value-of select="$mapversion"/>
        </p>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="map_styles">
  </xsl:template>

  <xsl:template match="node">

    <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
    <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
    <xsl:variable name="nodetext" select="@TEXT"/>
    <xsl:variable name="thisid" select="@ID"/>
    <xsl:variable name="thiscolor" select="@COLOR"/>
    <xsl:variable name="fontface" select="font/@NAME"/>
    <xsl:variable name="fontbold" select="font/@BOLD"/>
    <xsl:variable name="fontitalic" select="font/@ITALIC"/>
    <xsl:variable name="fontsize" select="font/@SIZE"/>
    <xsl:variable name="target" select="arrowlink/@DESTINATION"/>

    <ul>
      <li>

        <xsl:if test="@ID != ''">
          <a>
            <xsl:attribute name="name">
              <xsl:value-of select="$thisid"/>
            </xsl:attribute>
          </a>
        </xsl:if>

        <xsl:if test="arrowlink/@DESTINATION != ''">
          <a >
            <xsl:attribute name="style">
              <xsl:if test="$thiscolor != ''">
                <xsl:text>color:</xsl:text><xsl:value-of select="$thiscolor"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontface != ''">
                <xsl:text>font-family:</xsl:text><xsl:value-of select="translate($fontface,$ucletters,$lcletters)"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontsize != ''">
                <xsl:text>font-size:</xsl:text><xsl:value-of select="$fontsize"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontbold = 'true'">
                <xsl:text>font-weight:bold;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontitalic = 'true'">
                <xsl:text>font-style:italic;</xsl:text>
              </xsl:if>
            </xsl:attribute>

            <xsl:attribute name="href">
              <xsl:text>#</xsl:text><xsl:value-of select="$target"/>
            </xsl:attribute>

            <xsl:value-of select="$nodetext"/>
          </a>
        </xsl:if>

        <xsl:if test="not(arrowlink/@DESTINATION)">

          <pre>

            <xsl:attribute name="style">
              <xsl:if test="$thiscolor != ''">
                <xsl:text>color:</xsl:text><xsl:value-of select="$thiscolor"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontface != ''">
                <xsl:text>font-family:</xsl:text><xsl:value-of select="translate($fontface,$ucletters,$lcletters)"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontsize != ''">
                <xsl:text>font-size:</xsl:text><xsl:value-of select="$fontsize"/><xsl:text>;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontbold = 'true'">
                <xsl:text>font-weight:bold;</xsl:text>
              </xsl:if>
              <xsl:if test="$fontitalic = 'true'">
                <xsl:text>font-style:italic;</xsl:text>
              </xsl:if>
            </xsl:attribute>
            <xsl:value-of select="$nodetext"/>
        </pre>
        </xsl:if>


        <xsl:apply-templates/>
        <xsl:if test="current()/node/hook/@NAME='ExternalObject'">
          <xsl:call-template name="ExternalObject"></xsl:call-template>
        </xsl:if>

      </li>
    </ul>
  </xsl:template>

  <xsl:template name="ExternalObject">
    <img>
      <xsl:attribute name="src">
        <xsl:value-of select="current()/node/hook/@URI" disable-output-escaping="yes"/>
      </xsl:attribute>
    </img>
  </xsl:template>


</xsl:stylesheet>
