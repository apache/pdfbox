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
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.BadFieldValueException;
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
        checkTransform(metadata, "50429052370059903229869639943824137435756655804864824611365505219590816799783");
        is.close();
    }

    @Test
    public void testEmptyLi() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");
        XMPMetadata metadata = xdb.parse(is);
        checkTransform(metadata, "92757984740574362800045336947395134346147179161385043989715484359442690118913");
        is.close();
    }

    @Test
    public void testEmptyLi2() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(is);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        dc.getCreatorsProperty();
        checkTransform(metadata, "84846877440303452108560435796840772468446174326989274262473618453524301429629");
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
        checkTransform(metadata, "16805992283807186369849610414335227396239089071611806706387795179375897398118");
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

        checkTransform(metadata, "29120813843205587378639665706339183422557956085575883885304382528664692315203");
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
        
        checkTransform(metadata, "18065297971979344549773207273794555094175502580946345976611821901439849242965");
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
        
        checkTransform(metadata, "65475542891943378255730260794798768587695617138297196920293698476028940113080");
        is.close();
    }

    @Test
    public void testMetadataParsing() throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
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

        checkTransform(metadata, "90022311886271402508155234494196354960301469636090129252744270615851988530557");
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
    public void testEmptyDate() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
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
        XMPMetadata xmp = xdb.parse(xmpmeta.getBytes("UTF-8"));
        checkTransform(xmp, "8175296932768628269367133054275876764131784758539061072921527253098102430315");
    }

    private void checkTransform(XMPMetadata metadata, String expected)
            throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException, IOException
    {
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString("UTF-8").replace("\r\n", "\n");
        byte[] ba = replaced.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals("output:\n" + replaced, expected, result);
    }
}
