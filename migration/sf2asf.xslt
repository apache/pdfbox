<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:str="xalan://java.lang.String">

  <xsl:output method="text" encoding="UTF-8"/>

  <xsl:template match="artifact[status!='Open']"/>

  <xsl:template match="artifact[status='Open']">
    <xsl:value-of select="submitted_date"/>
    <xsl:text>;</xsl:text>
    <xsl:apply-templates select="artifact_type"/>
    <xsl:text>;</xsl:text>
    <xsl:apply-templates select="category"/>
    <xsl:text>;"</xsl:text>
    <xsl:value-of select="str:replaceAll(string(summary),'&quot;','&quot;&quot;')"/>
    <xsl:text>";"[imported from SourceForge]
</xsl:text>
    <xsl:call-template name="url">
      <xsl:with-param name="atid" select="artifact_type"/>
      <xsl:with-param name="aid" select="@id"/>
    </xsl:call-template>
    <xsl:text>
</xsl:text>Originally submitted by <xsl:value-of select="submitted_by"/> on <xsl:value-of select="submitted_date"/>.

<xsl:value-of select="str:replaceAll(string(detail),'&quot;','&quot;&quot;')"/>
    <xsl:apply-templates select="existingfiles"/>
    <xsl:apply-templates select="follow_ups"/>
    <xsl:text>"
</xsl:text>
  </xsl:template>

  <xsl:template match="artifact_type[.=552835]">New Feature</xsl:template>
  <xsl:template match="artifact_type[.=773649]">New Feature</xsl:template>
  <xsl:template match="artifact_type[.=831990]">New Feature</xsl:template>
  <xsl:template match="artifact_type[.=552834]">Improvement</xsl:template>
  <xsl:template match="artifact_type[.=773648]">Improvement</xsl:template>
  <xsl:template match="artifact_type[.=831989]">Improvement</xsl:template>
  <xsl:template match="artifact_type">Bug</xsl:template>

  <xsl:template match="category[.='lucene']">Lucene</xsl:template>
  <xsl:template match="category[.='PDModel']">PDModel</xsl:template>
  <xsl:template match="category[.='PDModel.AcroForm']">PDModel.AcroForm</xsl:template>
  <xsl:template match="category[.='parsing']">Parsing</xsl:template>
  <xsl:template match="category[.='PDFReader']">PDFReader</xsl:template>
  <xsl:template match="category[.='text extraction']">Text extraction</xsl:template>
  <xsl:template match="category[.='writing']">Writing</xsl:template>
  <xsl:template match="category[.='utilities']">Utilities</xsl:template>
  <xsl:template match="category">
    <xsl:choose>
      <xsl:when test="substring(../artifact_type,1,1)=7">FontBox</xsl:when>
      <xsl:when test="substring(../artifact_type,1,1)=8">JempBox</xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="file">

[attachment on SourceForge]
<xsl:call-template name="url">
  <xsl:with-param name="type">download</xsl:with-param>
  <xsl:with-param name="atid" select="../../artifact_type"/>
  <xsl:with-param name="aid" select="../../@id"/>
  <xsl:with-param name="file" select="id"/>
</xsl:call-template><xsl:text>
</xsl:text>
    <xsl:value-of select="name"/> (<xsl:value-of select="filetype"/>), <xsl:value-of select="filesize"/> bytes<xsl:text>
</xsl:text>
    <xsl:value-of select="str:replaceAll(string(description),'&quot;','&quot;&quot;')"/>
  </xsl:template>

  <xsl:template match="item">

[comment on SourceForge]
Originally sent by <xsl:value-of select="sender"/>.
<xsl:value-of select="str:replaceAll(string(text),'&quot;','&quot;&quot;')"/>
  </xsl:template>

  <xsl:template match="text()"/>

  <xsl:template name="url">
    <xsl:param name="type">index</xsl:param>
    <xsl:param name="atid"/>
    <xsl:param name="aid"/>
    <xsl:param name="file"/>

    <xsl:text>http://sourceforge.net/tracker/</xsl:text>
    <xsl:value-of select="$type"/><xsl:text>.php?</xsl:text>
    <xsl:choose>
      <xsl:when test="substring($atid,1,1)=5">group_id=78314</xsl:when>
      <xsl:when test="substring($atid,1,1)=7">group_id=149227</xsl:when>
      <xsl:when test="substring($atid,1,1)=8">group_id=164503</xsl:when>
    </xsl:choose>
    <xsl:text>&amp;atid=</xsl:text><xsl:value-of select="$atid"/>
    <xsl:text>&amp;aid=</xsl:text><xsl:value-of select="$aid"/>
    <xsl:if test="$file!=''">
      <xsl:text>&amp;file_id=</xsl:text><xsl:value-of select="$file"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
