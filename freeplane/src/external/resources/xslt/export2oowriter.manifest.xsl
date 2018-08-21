<?xml version="1.0" encoding="iso-8859-1"?>
<!--
/*Freeplane - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2008  Christian Foltin and others.
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
 * This stylesheet is for generating `META-INF/manifest.xml` for ODF-Files (Open Document Format),
 * used e.g. for exporting to OpenOffice/LibeOffice Writer documents.
 */
-->
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">

	<xsl:output method="xml" version="1.0" indent="yes"
		encoding="UTF-8" omit-xml-declaration="no" />
	<xsl:strip-space elements="*" />

	<xsl:template match="map">
		<manifest:manifest
			xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0">
			<manifest:file-entry manifest:media-type="application/vnd.oasis.opendocument.text"
				manifest:full-path="/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/statusbar/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/accelerator/current.xml" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/accelerator/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/floater/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/popupmenu/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/progressbar/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/menubar/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/toolbar/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/images/Bitmaps/" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Configurations2/images/" />
			<manifest:file-entry manifest:media-type="application/vnd.sun.xml.ui.configuration"
				manifest:full-path="Configurations2/" />
			<manifest:file-entry manifest:media-type="text/xml"
				manifest:full-path="content.xml" />
			<manifest:file-entry manifest:media-type="text/xml"
				manifest:full-path="styles.xml" />
			<manifest:file-entry manifest:media-type="text/xml"
				manifest:full-path="meta.xml" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Thumbnails/thumbnail.png" />
			<manifest:file-entry manifest:media-type=""
				manifest:full-path="Thumbnails/" />
			<manifest:file-entry manifest:media-type="text/xml"
				manifest:full-path="settings.xml" />
		</manifest:manifest>
	</xsl:template>

</xsl:stylesheet>
