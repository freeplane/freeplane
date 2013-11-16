<?xml version="1.0" encoding="UTF-8"?>
<!--
/*Freeplane - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2008  Christian Foltin and others.
 *This file is Copyright (C) 2013 Hartmut Goebel
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This stylesheet is for generating `styles.xml` for ODF-Files (Open Document Format),
 * used e.g. for exporting to OpenOffice/LibeOffice Writer documents.
 */
-->
<stylesheet version="1.0"
	    xmlns="http://www.w3.org/1999/XSL/Transform"
	    xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	    xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	    xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0">

  <output method="xml" version="1.0" indent="yes"
	      encoding="UTF-8" omit-xml-declaration="no" />
  <strip-space elements="*" />

  <template match="map">
    <office:document-styles
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
	xmlns:math="http://www.w3.org/1998/Math/MathML"
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
	xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:ooo="http://openoffice.org/2004/office"
	xmlns:ooow="http://openoffice.org/2004/writer"
	xmlns:oooc="http://openoffice.org/2004/calc"
	xmlns:dom="http://www.w3.org/2001/xml-events" office:version="1.0">
	<office:font-face-decls>
		<style:font-face style:name="StarSymbol"
			svg:font-family="StarSymbol" />
		<style:font-face style:name="DejaVu Sans"
			svg:font-family="&apos;DejaVu Sans&apos;"
			style:font-family-generic="roman" style:font-pitch="variable" />
		<style:font-face style:name="DejaVu Sans1"
			svg:font-family="&apos;DejaVu Sans&apos;"
			style:font-family-generic="swiss" style:font-pitch="variable" />
		<style:font-face style:name="DejaVu Sans2"
			svg:font-family="&apos;DejaVu Sans&apos;"
			style:font-family-generic="system" style:font-pitch="variable" />
	</office:font-face-decls>
	<office:styles>
		<style:default-style style:family="graphic">
			<style:graphic-properties draw:shadow-offset-x="0.3cm"
				draw:shadow-offset-y="0.3cm"
				draw:start-line-spacing-horizontal="0.283cm"
				draw:start-line-spacing-vertical="0.283cm"
				draw:end-line-spacing-horizontal="0.283cm"
				draw:end-line-spacing-vertical="0.283cm"
				style:flow-with-text="false" />
			<style:paragraph-properties
				style:text-autospace="ideograph-alpha" style:line-break="strict"
				style:writing-mode="lr-tb"
				style:font-independent-line-spacing="false">
				<style:tab-stops />
			</style:paragraph-properties>
			<style:text-properties style:use-window-font-color="true"
				fo:font-size="12pt" fo:language="de" fo:country="DE"
				style:font-size-asian="12pt" style:language-asian="de"
				style:country-asian="DE" style:font-size-complex="12pt"
				style:language-complex="de" style:country-complex="DE" />
		</style:default-style>
		<style:default-style style:family="paragraph">
			<style:paragraph-properties
				fo:hyphenation-ladder-count="no-limit"
				style:text-autospace="ideograph-alpha"
				style:punctuation-wrap="hanging" style:line-break="strict"
				style:tab-stop-distance="1.251cm" style:writing-mode="page" />
			<style:text-properties style:use-window-font-color="true"
				style:font-name="DejaVu Sans" fo:font-size="12pt" fo:language="de"
				fo:country="DE" style:font-name-asian="DejaVu Sans2"
				style:font-size-asian="12pt" style:language-asian="de"
				style:country-asian="DE" style:font-name-complex="DejaVu Sans2"
				style:font-size-complex="12pt" style:language-complex="de"
				style:country-complex="DE" fo:hyphenate="false"
				fo:hyphenation-remain-char-count="2"
				fo:hyphenation-push-char-count="2" />
		</style:default-style>
		<style:default-style style:family="table">
			<style:table-properties table:border-model="collapsing" />
		</style:default-style>
		<style:default-style style:family="table-row">
			<style:table-row-properties fo:keep-together="auto" />
		</style:default-style>
		<style:style style:name="Standard" style:family="paragraph"
			style:class="text" />
		<style:style style:name="Text_20_body"
			style:display-name="Text body" style:family="paragraph"
			style:parent-style-name="Standard" style:class="text">
			<style:paragraph-properties fo:margin-top="0cm"
				fo:margin-bottom="0.212cm" />
		</style:style>
		<call-template name="child-paragraph-style">
		  <with-param name="parentname" >Text_20_body</with-param>
		  <with-param name="stylename" >Text body Note</with-param>
		</call-template>
		<call-template name="child-paragraph-style">
		  <with-param name="parentname" >Text_20_body</with-param>
		  <with-param name="stylename" >Text body Details</with-param>
		</call-template>
		<style:style style:name="Heading" style:family="paragraph"
			style:parent-style-name="Standard"
			style:next-style-name="Text_20_body" style:class="text">
			<style:paragraph-properties fo:margin-top="0.423cm"
				fo:margin-bottom="0.212cm" fo:keep-with-next="always" />
			<style:text-properties style:font-name="DejaVu Sans1"
				fo:font-size="14pt" style:font-name-asian="DejaVu Sans2"
				style:font-size-asian="14pt" style:font-name-complex="DejaVu Sans2"
				style:font-size-complex="14pt" />
		</style:style>
		<style:style style:name="Heading_20_1"
			style:display-name="Heading 1" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="1">
			<style:text-properties fo:font-size="115%"
				fo:font-weight="bold" style:font-size-asian="115%"
				style:font-weight-asian="bold" style:font-size-complex="115%"
				style:font-weight-complex="bold" />
		</style:style>
		<style:style style:name="Heading_20_2"
			style:display-name="Heading 2" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="2">
			<style:text-properties fo:font-size="14pt"
				fo:font-style="italic" fo:font-weight="bold"
				style:font-size-asian="14pt" style:font-style-asian="italic"
				style:font-weight-asian="bold" style:font-size-complex="14pt"
				style:font-style-complex="italic" style:font-weight-complex="bold" />
		</style:style>
		<style:style style:name="Heading_20_3"
			style:display-name="Heading 3" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="3">
			<style:text-properties fo:font-size="14pt"
				fo:font-weight="bold" style:font-size-asian="14pt"
				style:font-weight-asian="bold" style:font-size-complex="14pt"
				style:font-weight-complex="bold" />
		</style:style>
		<style:style style:name="Heading_20_4"
			style:display-name="Heading 4" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="4">
			<style:text-properties fo:font-size="85%"
				fo:font-style="italic" fo:font-weight="bold"
				style:font-size-asian="85%" style:font-style-asian="italic"
				style:font-weight-asian="bold" style:font-size-complex="85%"
				style:font-style-complex="italic" style:font-weight-complex="bold"/>
		</style:style>
		<style:style style:name="Heading_20_5"
			style:display-name="Heading 5" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="5">
			<style:text-properties fo:font-size="85%"
				fo:font-weight="bold" style:font-size-asian="85%"
				style:font-weight-asian="bold" style:font-size-complex="85%"
				style:font-weight-complex="bold"/>
		</style:style>
		<style:style style:name="Heading_20_6"
			style:display-name="Heading 6" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="text"
			style:default-outline-level="6">
			<style:text-properties fo:font-size="75%"
				fo:font-weight="bold" style:font-size-asian="75%"
				style:font-weight-asian="bold" style:font-size-complex="75%"
				style:font-weight-complex="bold"/>
		</style:style>
		<style:style style:name="Heading_20_7"
			style:display-name="Heading 7" style:family="paragraph"
			style:parent-style-name="Heading" style:class="text"
			style:next-style-name="Text_20_body"
			style:default-outline-level="7">
			<style:text-properties fo:font-size="75%"
				fo:font-weight="bold" style:font-size-asian="75%"
				style:font-weight-asian="bold" style:font-size-complex="75%"
				style:font-weight-complex="bold"/>
		</style:style>
		<style:style style:name="List" style:family="paragraph"
			style:parent-style-name="Text_20_body" style:class="list" />
		<style:style style:name="List_20_1" style:family="paragraph"
			     style:display-name="List 1"
			     style:parent-style-name="List" style:class="list" />
		<style:style style:name="Numbering_20_1" style:family="paragraph"
			     style:display-name="Numbering 1"
			     style:parent-style-name="List" style:class="list" />
		<!-- a paragraph style for marking conversion errors -->
		<style:style style:name="Error" style:family="paragraph"
			     style:parent-style-name="Text_20_body">
		  <style:paragraph-properties
		      style:border-line-width="0.026cm 0.062cm 0.053cm"
		      fo:padding="0.15cm"
		      fo:border="4pt double #ff0000"/>
		</style:style>
		<!-- and a character style for the added error text -->
		<style:style style:name="ErrorIntro" style:family="text">
		  <style:text-properties
		      fo:color="#ff0000"
		      style:text-line-through-style="none"
		      style:text-line-through-width="none" fo:font-weight="bold"/>
		</style:style>
		<style:style style:name="Caption" style:family="paragraph"
			style:parent-style-name="Standard" style:class="extra">
			<style:paragraph-properties fo:margin-top="0.212cm"
				fo:margin-bottom="0.212cm" text:number-lines="false"
				text:line-number="0" />
			<style:text-properties fo:font-size="12pt"
				fo:font-style="italic" style:font-size-asian="12pt"
				style:font-style-asian="italic" style:font-size-complex="12pt"
				style:font-style-complex="italic" />
		</style:style>
		<style:style style:name="Index" style:family="paragraph"
			style:parent-style-name="Standard" style:class="index">
			<style:paragraph-properties text:number-lines="false"
				text:line-number="0" />
		</style:style>
		<style:style style:name="Title" style:family="paragraph"
			style:parent-style-name="Heading" style:next-style-name="Subtitle"
			style:class="chapter">
			<style:paragraph-properties fo:text-align="center"
				style:justify-single-word="false" />
			<style:text-properties fo:font-size="18pt"
				fo:font-weight="bold" style:font-size-asian="18pt"
				style:font-weight-asian="bold" style:font-size-complex="18pt"
				style:font-weight-complex="bold" />
		</style:style>
		<style:style style:name="Subtitle" style:family="paragraph"
			style:parent-style-name="Heading"
			style:next-style-name="Text_20_body" style:class="chapter">
			<style:paragraph-properties fo:text-align="center"
				style:justify-single-word="false" />
			<style:text-properties fo:font-size="14pt"
				fo:font-style="italic" style:font-size-asian="14pt"
				style:font-style-asian="italic" style:font-size-complex="14pt"
				style:font-style-complex="italic" />
		</style:style>
		<style:style style:name="Bullet_20_Symbols"
			style:display-name="Bullet Symbols" style:family="text">
			<style:text-properties style:font-name="StarSymbol"
				fo:font-size="9pt" style:font-name-asian="StarSymbol"
				style:font-size-asian="9pt" style:font-name-complex="StarSymbol"
				style:font-size-complex="9pt" />
		</style:style>
		<style:style style:name="Numbering_20_Symbols"
			     style:display-name="Numbering Symbols"
			     style:family="text"/>

		<!--- pre-defined styles -->
		<apply-templates select=".//stylenode[starts-with(@LOCALIZED_TEXT,'defaultstyle.')]" />
		<!--- custom styles -->
		<apply-templates select=".//stylenode[@LOCALIZED_TEXT='styles.user-defined']//stylenode" />

		<text:outline-style>
		  <call-template name="gen-outline-style">
		    <with-param name="level" select="10"/> <!-- define 10 level-styles -->
		  </call-template>
		</text:outline-style>

		<!-- generate the numbering list definition -->
		<text:list-style style:name="Numbering_20_1" style:display-name="Numbering 1">
		  <call-template name="gen-numbering-list-style">
		    <with-param name="level" select="10"/> <!-- define 10 level-styles -->
		    <with-param name="indent" select="5"/> <!-- indent per level in millimeters -->
		  </call-template>
		</text:list-style>
		<!-- generate the bullet list definition -->
		<text:list-style style:name="List_20_1" style:display-name="List 1">
		  <call-template name="gen-bullet-list-style">
		    <with-param name="chars" select="'&#9679;&#9675;&#9632;&#9679;&#9675;&#9632;&#9679;&#9675;&#9632;&#9679;'"/>
		    <with-param name="indent" select="4"/> <!-- indent per level in millimeters -->
		  </call-template>
		</text:list-style>

		<text:notes-configuration text:note-class="footnote"
			style:num-format="1" text:start-value="0"
			text:footnotes-position="page" text:start-numbering-at="document" />
		<text:notes-configuration text:note-class="endnote"
			style:num-format="i" text:start-value="0" />
		<text:linenumbering-configuration text:number-lines="false"
			text:offset="0.499cm" style:num-format="1"
			text:number-position="left" text:increment="5" />
	</office:styles>
	<office:automatic-styles>
		<style:page-layout style:name="pm1">
			<style:page-layout-properties fo:page-width="20.999cm"
				fo:page-height="29.699cm" style:num-format="1"
				style:print-orientation="portrait" fo:margin-top="2cm"
				fo:margin-bottom="2cm" fo:margin-left="2cm" fo:margin-right="2cm"
				style:writing-mode="lr-tb" style:footnote-max-height="0cm">
				<style:footnote-sep style:width="0.018cm"
					style:distance-before-sep="0.101cm"
					style:distance-after-sep="0.101cm" style:adjustment="left"
					style:rel-width="25%" style:color="#000000" />
			</style:page-layout-properties>
			<style:header-style />
			<style:footer-style />
		</style:page-layout>

	</office:automatic-styles>
	<office:master-styles>
		<style:master-page style:name="Standard"
			style:page-layout-name="pm1" />
	</office:master-styles>
      </office:document-styles>
   </template>

   <template name="child-paragraph-style">
    <param name="stylename" />
    <param name="parentname" />
    <style:style style:family="paragraph" style:class="text">
      <attribute name="style:parent-style-name"><value-of select="translate($parentname, ' ', '_')"/></attribute>
      <attribute name="style:name"><value-of select="translate($stylename, ' ', '_')"/></attribute>
      <attribute name="style:display-name"><value-of select="$stylename"/></attribute>
    </style:style>
   </template>

   <template name="paragraph-style">
    <param name="stylename" />
    <style:style style:family="paragraph" style:class="text"
		 style:parent-style-name="Text_20_body"><!-- todo: think about using a non-hardcoded parent-->
      <attribute name="style:name"><value-of select="translate($stylename, ' ', '_')"/></attribute>
      <attribute name="style:display-name"><value-of select="$stylename"/></attribute>
      <style:text-properties>
	<attribute name="fo:color"><value-of select="@COLOR"/></attribute>
	<attribute name="fo:font-size"><value-of select="font/@SIZE"/>pt</attribute>
	<if test="font/@ITALIC='true'">
	  <attribute name="fo:font-style">italic</attribute>
	</if>
	<if test="font/@BOLD='true'">
	  <attribute name="fo:font-weight">bold</attribute>
	</if>
      </style:text-properties>
      <style:paragraph-properties>
	<attribute name="fo:background-color"><value-of select="@BACKGROUND_COLOR"/></attribute>
      </style:paragraph-properties>
    </style:style>
    <!-- Declare child-styles for Details and Note -->
    <call-template name="child-paragraph-style">
      <with-param name="parentname" ><value-of select="$stylename"/></with-param>
      <with-param name="stylename" ><value-of select="concat($stylename, ' Details')"/></with-param>
    </call-template>
    <call-template name="child-paragraph-style">
      <with-param name="parentname" ><value-of select="$stylename"/></with-param>
      <with-param name="stylename" ><value-of select="concat($stylename, ' Note')"/></with-param>
    </call-template>
   </template>

   <template match="stylenode[@TEXT]">
     <call-template name="paragraph-style">
       <with-param name="stylename" select="@TEXT" />
     </call-template>
   </template>

   <template match="stylenode[starts-with(@LOCALIZED_TEXT,'defaultstyle.')]" >
     <call-template name="paragraph-style">
       <with-param name="stylename" select="substring-after(@LOCALIZED_TEXT,'defaultstyle.')" />
     </call-template>
   </template>

   <!-- templates for generating uniform styles -->

   <template name="gen-outline-style">
     <param name="level" />
     <if test="$level &gt; 1">
       <call-template name="gen-outline-style">
	 <with-param name="level" select="$level -1"/>
       </call-template>
     </if>
     <text:outline-level-style style:num-format="1">
       <attribute name="text:level"><value-of select="$level" /></attribute>
       <attribute name="text:display-levels"><value-of select="$level" /></attribute>
       <style:list-level-properties text:min-label-distance="0.381cm" />
     </text:outline-level-style>
   </template>

   <template name="gen-numbering-list-style">
     <param name="level" />
     <param name="indent" select="5" /><!-- indent per level in millimeters -->
     <if test="$level &gt; 1">
       <call-template name="gen-numbering-list-style">
	 <with-param name="level" select="$level -1"/>
	 <with-param name="indent" select="$indent"/>
       </call-template>
     </if>
     <text:list-level-style-number
	 text:style-name="Numbering_20_Symbols" style:num-suffix="." style:num-format="1">
       <attribute name="text:level"><value-of select="$level" /></attribute>
       <style:list-level-properties text:list-level-position-and-space-mode="label-alignment">
	 <style:list-level-label-alignment text:label-followed-by="listtab">
	   <attribute name="text:list-tab-stop-position"><value-of select="concat(string($level*$indent), 'mm')" /></attribute>
	   <attribute name="fo:margin-left"><value-of select="concat(string($level*$indent), 'mm')" /></attribute>
	   <attribute name="fo:text-indent"><value-of select="concat(string(-$indent), 'mm')" /></attribute>
	 </style:list-level-label-alignment>
       </style:list-level-properties>
     </text:list-level-style-number>
   </template>

   <template name="gen-bullet-list-style">
     <param name="chars" />
     <param name="indent" select="5" /><!-- indent per level in millimeters -->
     <param name="level" select="1"/>
     <text:list-level-style-bullet text:style-name="Bullet_20_Symbols">
       <attribute name="text:level"><value-of select="$level" /></attribute>
       <attribute name="text:bullet-char"><value-of select="substring($chars,1,1)" /></attribute>
       <style:list-level-properties text:list-level-position-and-space-mode="label-alignment">
	 <style:list-level-label-alignment text:label-followed-by="listtab">
	   <attribute name="text:list-tab-stop-position"><value-of select="concat(string($level*$indent), 'mm')" /></attribute>
	   <attribute name="fo:margin-left"><value-of select="concat(string($level*$indent), 'mm')" /></attribute>
	   <attribute name="fo:text-indent"><value-of select="concat(string(-$indent), 'mm')" /></attribute>
	 </style:list-level-label-alignment>
       </style:list-level-properties>
     </text:list-level-style-bullet>
     <if test="string-length($chars) &gt; 1">
       <call-template name="gen-bullet-list-style">
	 <with-param name="chars" select="substring($chars,2)"/>
	 <with-param name="indent" select="$indent"/>
	 <with-param name="level" select="$level+1"/>
       </call-template>
     </if>
   </template>

</stylesheet>
