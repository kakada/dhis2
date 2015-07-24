<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:java="org.hisp.dhis.dxf2.gml.GmlConversionUtils"
  exclude-result-prefixes="java">

  <xsl:param name="precision">4</xsl:param>

  <xsl:template match="gml:coordinates" mode="multipleCoordinates">
    <xsl:text>[</xsl:text>
    <xsl:value-of select="java:gmlCoordinatesToString(normalize-space(.),$precision)"
      disable-output-escaping="yes"/>
    <xsl:text>]</xsl:text>
    <xsl:if test="position() != last()">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gml:coordinates" mode="singleCoordinate">
    <xsl:value-of select="java:gmlCoordinatesToString(normalize-space(.),$precision)"
      disable-output-escaping="yes"/>
  </xsl:template>

  <xsl:template match="gml:pos">
    <xsl:value-of select="java:gmlPosToString(normalize-space(.),$precision)"
      disable-output-escaping="yes" />
  </xsl:template>

  <xsl:template match="gml:posList">
    <xsl:text>[</xsl:text>
    <xsl:value-of select="java:gmlPosListToString(normalize-space(.),$precision)"
      disable-output-escaping="yes"/>
    <xsl:text>]</xsl:text>
    <xsl:if test="position() != last()">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gml:Polygon">
    <featureType>Polygon</featureType>
    <coordinates>
      <xsl:text>[[</xsl:text>
      <xsl:apply-templates select=".//gml:coordinates" mode="multipleCoordinates"/>
      <xsl:apply-templates select=".//gml:posList"/>
      <xsl:text>]]</xsl:text>
      <xsl:if test="position() != last()">
        <xsl:text>,</xsl:text>
      </xsl:if>
    </coordinates>
  </xsl:template>

  <xsl:template match="gml:MultiPolygon">
    <featureType>MultiPolygon</featureType>
    <coordinates>
      <xsl:text>[</xsl:text>
      <xsl:apply-templates select=".//gml:polygonMember"/>
      <xsl:text>]</xsl:text>
    </coordinates>
  </xsl:template>

  <xsl:template match="gml:Point">
    <featureType>Point</featureType>
    <coordinates>
      <xsl:apply-templates select=".//gml:coordinates" mode="singleCoordinate"/>
      <xsl:apply-templates select=".//gml:pos"/>
    </coordinates>
  </xsl:template>

  <xsl:template match="gml:polygonMember">
    <xsl:text>[</xsl:text>
    <xsl:apply-templates select=".//gml:coordinates" mode="multipleCoordinates"/>
    <xsl:apply-templates select=".//gml:posList"/>
    <xsl:text>]</xsl:text>
    <xsl:if test="position() != last()">
      <xsl:text>,</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template match="gml:featureMember">
    <xsl:variable name="uid"  select=".//*[local-name()='uid'  or local-name()='UID'  or local-name()='Uid']" />
    <xsl:variable name="code" select=".//*[local-name()='code' or local-name()='CODE' or local-name()='Code']" />
    <xsl:variable name="name" select=".//*[local-name()='name' or local-name()='NAME' or local-name()='Name']" />
    <organisationUnit>
      <xsl:choose> <!-- Priority is uid, code, name. First match in order excludes the others -->
        <xsl:when test="$uid != ''">
          <xsl:attribute name="id"> <!-- 'uid' is mapped to 'id' in dxf2 -->
            <xsl:value-of select="$uid" />
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$code != ''">
          <xsl:attribute name="code">
            <xsl:value-of select="$code" />
          </xsl:attribute>
        </xsl:when>
        <xsl:when test="$name != ''">
          <xsl:attribute name="name">
            <xsl:value-of select="$name" />
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
      <xsl:apply-templates select="./child::node()/child::node()/gml:Polygon|./child::node()/child::node()/gml:MultiPolygon|./child::node()/child::node()/gml:Point"/>
      <active>true</active>
    </organisationUnit>
  </xsl:template>

  <xsl:template match="/">
    <dxf xmlns="http://dhis2.org/schema/dxf/2.0">
      <organisationUnits>
        <xsl:apply-templates select=".//gml:featureMember"/>
      </organisationUnits>
    </dxf>
  </xsl:template>

</xsl:stylesheet>
