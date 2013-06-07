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
                <style type="text/css">
                </style>
                <link rel="stylesheet" type="text/css" href="MigrationReport.css"/>
                <script src="jQuery.js"/>
            </head>
            <body>
                <h1>Migration report</h1>
                
                <h2>Summary</h2>
                <xsl:call-template name="MigrationSummary"/>
                
                <h2>Source server</h2>
                <h3>Files comparison against distribution archive</h3>
                <xsl:call-template name="ComparisonResult"/>
                
                <h2>Source server configuration</h2>
                <xsl:call-template name="MigrationData"/>
                
                <h2>Actions to migrate to the target server</h2>
                <xsl:call-template name="Actions"/>
                
                <xsl:if test="finalException">
                    <div class="finalException">
                        <h2>Error</h2>
                        <p><xsl:value-of select="finalException/text()"/></p>
                    </div>
                </xsl:if>
                
            </body>
        </html>
    </xsl:template>
    
    <!-- Summary -->
    <xsl:template name="MigrationSummary">
        <!--<xsl:value-of select="concat(name(), ' | ', position(), ' | ', count(child::*))"/>-->
        
        <table class="flat">
            <tr>
                <td>Source server:</td>
                <td><xsl:value-of select="/migrationReport/sourceServer/@formatted"/></td>
                <td><xsl:value-of select="/migrationReport/sourceServer/@dir"/></td>
            </tr>
            <tr>
                <td>Target server:</td>
                <td>JBoss EAP 6.1</td>
            </tr>            
        </table>
        <xsl:call-template name="Config"/>
    </xsl:template>

    <!-- Config -->
    <xsl:template name="Config">
        <table class="flat vertBorder">
            <tr> <th>Dry run</th>         <td><xsl:value-of select="/migrationReport/config/globalConfig/dryRun"/></td> </tr>
            <tr> <th>Skip validation</th> <td><xsl:value-of select="/migrationReport/config/globalConfig/skipValidation"/></td> </tr>
            <tr> <th>Test run</th>        <td><xsl:value-of select="/migrationReport/config/globalConfig/testRun"/></td> </tr>
        </table>
    </xsl:template>

    <!-- Comparison result -->
    <xsl:template name="ComparisonResult">
        <a href="#" onclick="$('#comparison').slideToggle(1200)">show/hide</a>
        <table class="flat data vertBorder fs90" id="comparison">
            <tr>
                <th>Result</th>
                <th>File</th>
            </tr>
            <xsl:for-each select="/migrationReport/comparisonResult/matches/match">
                <tr>
                    <td class="match {@result}">
                        <xsl:value-of select="@result"/>
                    </td>
                    <td><xsl:value-of select="@path"/></td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>

    <!-- Config data (MigrationData) -->
    <xsl:template name="MigrationData">
        <xsl:for-each select="/migrationReport/configsData/configData">
            <div class="migratorData">
                <h4><xsl:value-of select="@fromMigrator"/></h4>
                <table class="fragments flat vertBorder" style="border-collapse: collapse;">
                    <xsl:for-each select="configFragments/configFragment">
                        <tr>
                            <td><xsl:value-of select="text()"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </div>
        </xsl:for-each>
    </xsl:template>

    <!-- Actions -->
    <xsl:template name="Actions">
        <xsl:for-each select="/migrationReport/actions/action">
            <div class="action {@fromMigrator}" id="{@id}">

                <div>
                    From migrator <xsl:value-of select="@fromMigrator"/>
                </div>
                
                <xsl:if test="warnings/*">
                    <h4>Warnings</h4>
                    <table class="warnings wid100p flat vertBorder">
                    <xsl:for-each select="warnings/warning">
                        <tr>
                            <td><xsl:value-of select="text()"/></td>
                        </tr>
                    </xsl:for-each>
                    </table>
                </xsl:if>
            </div>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Catch-all template - ignore whatever is not specified above. -->
    <xsl:template match="@*|node()"/>

</xsl:stylesheet>
