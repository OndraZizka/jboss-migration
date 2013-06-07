<?xml version="1.0" encoding="UTF-8"?>
<!--
    Author:  Ondrej Zizka, ozizka@redhat.com
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    <xsl:output method="html"/>

    <!-- Syntax at http://www.w3.org/TR/xslt -->
    <xsl:template match="/">
        <html>
            <head>
                <title>Migration report</title>
            </head>
            <body>
                <h1>Migration report</h1>
                
                <h2>Summary</h2>
                <xsl:call-template name="MigrationSummary"/>
                
                <h2>Source server</h2>
                <h3>Files comparison against distribution archive</h3>
                
                <h2>Source server configuration</h2>
                
                <h2>Actions to migrate to the target server</h2>
                
                <xsl:if test="finalException">
                    <h2>Error</h2>
                </xsl:if>
                
            </body>
        </html>
    </xsl:template>
    
    <!-- Summary -->
    <xsl:template name="MigrationSummary">
        <table>
            <tr>
                <td>Source server:</td>
                <td><xsl:value-of select="/sourceServer/@formatted"/></td>
                <td><xsl:value-of select="/sourceServer/@dir"/></td>
            </tr>
            <tr>
                <td>Target server:</td>
                <td>JBoss EAP 6.1</td>
            </tr>            
        </table>
    </xsl:template>

</xsl:stylesheet>
