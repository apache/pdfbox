/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.apache.xmpbox.parser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.TimeZone;
import javax.xml.transform.TransformerException;

import org.apache.xmpbox.DateConverter;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPMediaManagementSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.DateType;
import org.apache.xmpbox.type.DefinedStructuredType;
import org.apache.xmpbox.type.ThumbnailType;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;
import org.apache.xmpbox.xml.XmpSerializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * DomXmpParser imports the XML into an internal representation. XmpSerializer exports this into
 * XML. The result may look different, but should be the same from a data point of view.
 *
 * @author Tilman Hausherr
 */
public class DeserializationTest
{
    private ByteArrayOutputStream baos;

    private XmpSerializer serializer;

    private DomXmpParser xdb;

    private static TimeZone defaultTZ;

    @BeforeClass
    static public void initAll()
    {
        defaultTZ = TimeZone.getDefault();
        // Need to set a timezone or date values will be different depending on test location
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Before
    public void init() throws XmpParsingException
    {
        baos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
        xdb = new DomXmpParser();
    }

    @AfterClass
    static public void finishAll()
    {
        TimeZone.setDefault(defaultTZ);
    }

    @Test
    public void testStructuredRecursive() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");
        XMPMetadata metadata = xdb.parse(is);
        checkTransform(metadata, "62495942572014793625872774972947435765670563107818217447706375288846297812281", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testEmptyLi() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");
        XMPMetadata metadata = xdb.parse(is);
        checkTransform(metadata, "95754993383010030299848397520773287413798669761891751126809013411187892693280", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testEmptyLi2() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(is);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        dc.getCreatorsProperty();
        checkTransform(metadata, "39450703080437563739186076111811684356424147071014681699119272065568305393521", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testGetTitle() throws XmpParsingException, BadFieldValueException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(is);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        String s = dc.getTitle(null);
        Assert.assertEquals("title value", s);
        is.close();
    }

    @Test
    public void testAltBagSeq() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");
        XMPMetadata metadata=xdb.parse(is);
        checkTransform(metadata, "89123270336154452745819041017446278583816329940574853160909598044560152910018", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testIsartorStyleWithThumbs()
            throws XmpParsingException, IOException, BadFieldValueException, TransformerException, NoSuchAlgorithmException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml");

        XMPMetadata metadata = xdb.parse(is);

        // <xmpMM:DocumentID>
        Assert.assertEquals("uuid:09C78666-2F91-3A9C-92AF-3691A6D594F7", metadata.getXMPMediaManagementSchema()
                .getDocumentID());

        // <xmp:CreateDate>
        // <xmp:ModifyDate>
        // <xmp:MetadataDate>
        Assert.assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getCreateDate());
        Assert.assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getModifyDate());
        Assert.assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getMetadataDate());

        // THUMBNAILS TEST
        List<ThumbnailType> thumbs = metadata.getXMPBasicSchema().getThumbnailsProperty();
        Assert.assertNotNull(thumbs);
        Assert.assertEquals(2, thumbs.size());

        ThumbnailType thumb = thumbs.get(0);
        Assert.assertEquals(Integer.valueOf(162), thumb.getHeight());
        Assert.assertEquals(Integer.valueOf(216), thumb.getWidth());
        Assert.assertEquals("JPEG", thumb.getFormat());
        Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

        thumb = thumbs.get(1);
        Assert.assertEquals(Integer.valueOf(162), thumb.getHeight());
        Assert.assertEquals(Integer.valueOf(216), thumb.getWidth());
        Assert.assertEquals("JPEG", thumb.getFormat());
        Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

        // Check the extension schema (also serves as example on how to retrieve)
        XMPSchema acmeMailSchema = metadata.getSchema("http://www.acme.com/ns/email/1/");
        DateType deliveryDate = (DateType) acmeMailSchema.getProperty("Delivery-Date");
        assertEquals("2007-11-09T09:55:36+01:00", deliveryDate.getStringValue());
        DefinedStructuredType dst = (DefinedStructuredType) acmeMailSchema.getProperty("From");
        assertEquals("[name=TextType:John Doe]", dst.getProperty("name").toString());
        assertEquals("[mailto=TextType:john@acme.com]", dst.getProperty("mailto").toString());

        checkTransform(metadata, "64755266855514150823517184659364700851455308334441170957883187622624192802093", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testWithNoXPacketStart() throws XmpParsingException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadStart, e.getErrorType());
        }
    }

    @Test
    public void testWithNoXPacketEnd() throws XmpParsingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadEnd, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testWithNoRDFElement() throws XmpParsingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testWithTwoRDFElement() throws XmpParsingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testWithInvalidRDFElementPrefix() throws IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testWithRDFRootAsText() throws IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testUndefinedSchema() throws IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoSchema, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testUndefinedPropertyWithDefinedSchema() throws IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoType, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testUndefinedStructuredWithDefinedSchema() throws IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml");
        try
        {
            xdb.parse(is);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoValueType, e.getErrorType());
        }
        finally
        {
            is.close();
        }
    }

    @Test
    public void testRdfAboutFound() throws XmpParsingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(is);
        List<XMPSchema> schemas = metadata.getAllSchemas();
        for (XMPSchema xmpSchema : schemas)
        {
            Assert.assertNotNull(xmpSchema.getAboutAttribute());
        }
        is.close();
    }

    @Test
    public void testWithAttributesAsProperties() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        // also serves as a test for the changes in PDFBOX-2378
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        XMPMetadata metadata = xdb.parse(is);

        AdobePDFSchema pdf = metadata.getAdobePDFSchema();
        Assert.assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        Assert.assertEquals("application/pdf", dc.getFormat());

        XMPBasicSchema basic = metadata.getXMPBasicSchema();
        Assert.assertNotNull(basic.getCreateDate());

        PDFAIdentificationSchema pdfaIdentificationSchema = metadata.getPDFIdentificationSchema();
        Assert.assertEquals("B", pdfaIdentificationSchema.getConformance());
        Assert.assertEquals(1, (int) pdfaIdentificationSchema.getPart());

        XMPMediaManagementSchema xmpMediaManagementSchema = metadata.getXMPMediaManagementSchema();
        Assert.assertEquals("e7127190-445c-11ea-0000-b3bc74086807", xmpMediaManagementSchema.getDocumentID());

        checkTransform(metadata, "27499224985683016678197540524065114038595582230834506941950503218519476041225", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testSpaceTextValues() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        // check values with spaces at start or end
        // in this case, the value should not be trimmed
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/only_space_fields.xmp");
        XMPMetadata metadata = xdb.parse(is);
        // check producer
        Assert.assertEquals(" ", metadata.getAdobePDFSchema().getProducer());
        // check creator tool
        Assert.assertEquals("Canon ",metadata.getXMPBasicSchema().getCreatorTool());
        
        checkTransform(metadata, "9220923061800113567693538810355030344095407871190202111473587642358933618073", metadata.getAllSchemas().size());
        is.close();
    }

    @Test
    public void testMetadataParsing() throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, XmpParsingException
    {
        XMPMetadata metadata = XMPMetadata.createXMPMetadata();

        DublinCoreSchema dc = metadata.createAndAddDublinCoreSchema();
        dc.setCoverage("coverage");
        dc.addContributor("contributor1");
        dc.addContributor("contributor2");
        dc.addDescription("x-default", "Description");

        AdobePDFSchema pdf = metadata.createAndAddAdobePDFSchema();
        pdf.setProducer("Producer");
        pdf.setPDFVersion("1.4");

        checkTransform(metadata, "24727341753942351260821151680330022244742411666459385225917195999704816908515", metadata.getAllSchemas().size());
    }

    /**
     * PDFBOX-6029: serialize an empty date property, this brought a NullPointerException.
     *
     * @throws XmpParsingException
     * @throws TransformerException
     * @throws NoSuchAlgorithmException 
     * @throws UnsupportedEncodingException 
     */
    @Test
    public void testEmptyDate() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        String xmpmeta = "<?xpacket begin=\"﻿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
                + "<x:xmpmeta x:xmptk=\"Adobe XMP Core 4.2.1-c041 52.342996, 2008/05/07-20:48:00\" xmlns:x=\"adobe:ns:meta/\">\n"
                + "  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "   <rdf:Description rdf:about=\"\" xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\">\n"
                + "    <xmp:CreateDate></xmp:CreateDate>\n"
                + "   </rdf:Description>\n"
                + "  </rdf:RDF>\n"
                + "</x:xmpmeta>\n"
                + "<?xpacket end=\"w\"?>";
        XMPMetadata metadata = xdb.parse(xmpmeta.getBytes("UTF-8"));
        checkTransform(metadata, "19030153876683461724958694183980892665426846590791273142114566290124997390122", metadata.getAllSchemas().size());
    }

    private void checkTransform(XMPMetadata metadata, String expected, int expectedSchemaCount)
            throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, XmpParsingException
    {
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString("UTF-8").replace("\r\n", "\n");
        byte[] ba = replaced.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals("output:\n" + replaced, expected, result);
        XMPMetadata xmp = xdb.parse(baos.toByteArray()); // tests round trip
        assertEquals(expectedSchemaCount, xmp.getAllSchemas().size());
    }
}
