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
    <div class="header" style="background-color: #3B4D64; border-bottom: 1ex solid #243446;">
        <h1 style="padding: 1ex; color: white; margin: 0; ">Migration report</h1>
    </div>

    <div id="container">
        <h2><div class="icon"/><span>Summary</span></h2>
        <xsl:call-template name="MigrationSummary"/>

        <h2><div class="icon"/><span>Source server</span></h2>
        <h3>Files comparison against distribution archive</h3>
        <xsl:call-template name="ComparisonResult"/>

        <h2><div class="icon"/><span>Source server configuration</span></h2>
        <xsl:call-template name="MigrationData"/>

        <h2><div class="icon"/><span>Actions to migrate to the target server</span></h2>
        <xsl:call-template name="Actions"/>

        <xsl:if test="finalException">
            <div class="finalException">
                <h2>Error</h2>
                <p><xsl:value-of select="finalException/text()"/></p>
            </div>
        </xsl:if>
    </div>
</body>
</html>
    </xsl:template>
    
    <!-- Summary -->
    <xsl:template name="MigrationSummary">
        <!--<xsl:value-of select="concat(name(), ' | ', position(), ' | ', count(child::*))"/>-->
        
        <table class="" style="margin: 2ex 0;">
            <tr>
                <th>Source server:</th>
                <td><xsl:value-of select="/migrationReport/sourceServer/@formatted"/></td>
                <td><xsl:value-of select="/migrationReport/sourceServer/@dir"/></td>
            </tr>
            <tr>
                <th>Target server:</th>
                <td>JBoss EAP 6.1</td>
            </tr>            
            <tr> <th>Dry run:</th>         <td><xsl:value-of select="/migrationReport/config/globalConfig/dryRun"/></td> </tr>
            <tr> <th>Skip validation:</th> <td><xsl:value-of select="/migrationReport/config/globalConfig/skipValidation"/></td> </tr>
            <tr> <th>Test run:</th>        <td><xsl:value-of select="/migrationReport/config/globalConfig/testRun"/></td> </tr>
        </table>
        
    </xsl:template>

    <!-- Comparison result -->
    <xsl:template name="ComparisonResult">
        <div class="box comparison">
            <a href="#" onclick="$('#comparison').slideToggle(10)">show/hide</a>
            <table class="flat data vertBorder fs90" id="comparison">
                <tr> <th colspan="2">Result</th> <th>File</th> </tr>
                <xsl:for-each select="/migrationReport/comparisonResult/matches/match[@result != 'MATCH']">
                    <tr class="match {@result}">
                        <td class="icon"><div/></td>
                        <td class="result"> <xsl:value-of select="@result"/> </td>
                        <td><xsl:value-of select="@path"/></td>
                    </tr>
                </xsl:for-each>
            </table>
        </div>
    </xsl:template>

    <!-- Source server config data (MigrationData) -->
    <xsl:template name="MigrationData">
        <xsl:for-each select="/migrationReport/configsData/configData">
            <div class="box migrationData">
                <h4><div class="icon"/><xsl:value-of select="@fromMigrator"/></h4>
                <div class="padding">
                    <table class="fragments flat vertBorder" style="border-collapse: collapse;">
                        <xsl:for-each select="configFragments/configFragment">
                            <tr>
                                <td class="icon"><div/></td> <td><xsl:value-of select="text()"/></td>
                            </tr>
                        </xsl:for-each>
                    </table>
                </div>
            </div>
        </xsl:for-each>
    </xsl:template>

    <!-- Actions -->
    <xsl:template name="Actions">
        <xsl:for-each select="/migrationReport/actions/action">
            <div class="box action {@fromMigrator}" id="{@id}">
                <h4><div class="icon"/>From migrator <xsl:value-of select="@fromMigrator"/></h4>
                <div class="padding">
                    <p class="desc"><xsl:value-of select="desc/text()"/></p>
                    <xsl:if test="warnings/*">
                        <div class="padding">
                            <!--<h4>Warnings</h4>-->
                            <table class="warnings wid100p flat vertBorder">
                            <xsl:for-each select="warnings/warning">
                                <tr>
                                    <td class="icon"><div/></td> <td><xsl:value-of select="text()"/></td>
                                </tr>
                            </xsl:for-each>
                            </table>
                        </div>
                    </xsl:if>
                </div>
            </div>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Catch-all template - ignore whatever is not specified above. -->
    <xsl:template match="@*|node()"/>

</xsl:stylesheet>
