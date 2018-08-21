<?xml version="1.0" encoding="iso-8859-1"?>

<xsl:stylesheet version="1.0"     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"    xmlns:xlink="http://www.w3.org/1999/xlink">
    <xsl:output method="text" indent="no"/>
    <xsl:strip-space elements="*"/>

     <xsl:template match="map">
        <xsl:apply-templates select="node"/>
    </xsl:template>

    <!-- NODE -->
    <xsl:template match="node">
        <xsl:variable name="depth">
            <xsl:apply-templates select=".." mode="depthMesurement"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$depth=0">
                <xsl:text># Freeplane map "</xsl:text><xsl:value-of select="@TEXT"/><xsl:text>"&#xA;</xsl:text>
                <xsl:apply-templates select="node"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$depth=1">
                        <xsl:if test="@TEXT='TASKS'">
                            <!--xsl:text> TASK </xsl:text-->
                            <xsl:apply-templates select="node" mode="task"/>
                        </xsl:if>
                    </xsl:when>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ATTRIBUTE -->
    <xsl:template match="attribute">
        <xsl:variable name="depth">
            <xsl:apply-templates select=".." mode="depthMesurement"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="@NAME='task'">
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="spaces"><xsl:with-param name="count" select="($depth - 2) * 4"/></xsl:call-template>
                <xsl:value-of select="@NAME"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="@VALUE"/>
                <xsl:text>&#xA;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

   <!-- ATTRIBUTE TASK_ID-->
    <xsl:template match="attribute" mode="task_id">
        <xsl:if test="@NAME='task'">
            <xsl:value-of select="@VALUE"/>
        </xsl:if>
    </xsl:template>

    <!-- NODE TASK -->
    <xsl:template match="node" mode="task">
        <xsl:variable name="depth">
            <xsl:apply-templates select=".." mode="depthMesurement"/>
        </xsl:variable>
        <xsl:variable name="task_id">
            <xsl:apply-templates select="attribute" mode="task_id"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="@TEXT='#'">
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$task_id!=''">
                        <xsl:text>&#xA;</xsl:text>
                        <xsl:call-template name="spaces"><xsl:with-param name="count" select="($depth - 2) * 4"/></xsl:call-template>
                        <xsl:text>task </xsl:text><xsl:value-of select="$task_id"/><xsl:text> "</xsl:text><xsl:value-of select="@TEXT"/><xsl:text>" {&#xA;</xsl:text>
                        <xsl:apply-templates select="attribute"/>
                        <xsl:apply-templates select="node" mode="task"/>
                        <!-- koniec task -->
                        <xsl:call-template name="spaces"><xsl:with-param name="count" select="($depth - 2) * 4"/></xsl:call-template>
                        <xsl:text>}&#xA;</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="node" mode="task"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
   </xsl:template>

    <!-- Node Depth Mesurement -->
    <xsl:template match="node" mode="depthMesurement">
        <xsl:param name="depth" select=" '0' "/>
            <xsl:apply-templates select=".." mode="depthMesurement">
                <xsl:with-param name="depth" select="$depth + 1"/>
            </xsl:apply-templates>
    </xsl:template>

    <!-- Map Depth Mesurement -->
    <xsl:template match="map" mode="depthMesurement">
        <xsl:param name="depth" select=" '0' "/>
        <xsl:value-of select="$depth"/>
    </xsl:template>

    <xsl:template name="spaces">
        <xsl:param name="count" select="1"/>
        <xsl:if test="$count > 0">
            <xsl:text> </xsl:text>
            <xsl:call-template name="spaces">
                <xsl:with-param name="count" select="$count - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
