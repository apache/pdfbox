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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.xmpbox.xml.XmpSerializer;

class DeserializationTest
{

    private ByteArrayOutputStream baos;

    private XmpSerializer serializer;

    private DomXmpParser xdb;

    private static TimeZone defaultTZ;

    @BeforeAll
    static void initAll()
    {
        defaultTZ = TimeZone.getDefault();
        // Need to set a timezone or date values will be different depending on test location
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeEach
    void init() throws XmpParsingException
    {
        baos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
        xdb = new DomXmpParser();
    }

    @AfterAll
    static void finishAll()
    {
        TimeZone.setDefault(defaultTZ);
    }

    @Test
    void testStructuredRecursive() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");
        XMPMetadata metadata = xdb.parse(fis);
        checkTransform(metadata, "50429052370059903229869639943824137435756655804864824611365505219590816799783");
    }

    @Test
    void testEmptyLi() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");
        XMPMetadata metadata = xdb.parse(fis);
        checkTransform(metadata, "92757984740574362800045336947395134346147179161385043989715484359442690118913");
    }

    @Test
    void testEmptyLi2() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        dc.getCreatorsProperty();
        checkTransform(metadata, "84846877440303452108560435796840772468446174326989274262473618453524301429629");
    }

    @Test
    void testGetTitle() throws XmpParsingException, BadFieldValueException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        String s = dc.getTitle(null);
        assertEquals("title value", s);
    }

    @Test
    void testAltBagSeq() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");
        XMPMetadata metadata=xdb.parse(fis);
        checkTransform(metadata, "16805992283807186369849610414335227396239089071611806706387795179375897398118");
    }

    @Test
    void testIsartorStyleWithThumbs()
            throws XmpParsingException, IOException, BadFieldValueException, TransformerException, NoSuchAlgorithmException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml");

        XMPMetadata metadata = xdb.parse(fis);

        // <xmpMM:DocumentID>
        assertEquals("uuid:09C78666-2F91-3A9C-92AF-3691A6D594F7", metadata.getXMPMediaManagementSchema()
                .getDocumentID());

        // <xmp:CreateDate>
        // <xmp:ModifyDate>
        // <xmp:MetadataDate>
        assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getCreateDate());
        assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getModifyDate());
        assertEquals(DateConverter.toCalendar("2008-01-18T16:59:54+01:00"), metadata.getXMPBasicSchema()
                .getMetadataDate());

        // THUMBNAILS TEST
        List<ThumbnailType> thumbs = metadata.getXMPBasicSchema().getThumbnailsProperty();
        assertNotNull(thumbs);
        assertEquals(2, thumbs.size());

        ThumbnailType thumb = thumbs.get(0);
        assertEquals(Integer.valueOf(162), thumb.getHeight());
        assertEquals(Integer.valueOf(216), thumb.getWidth());
        assertEquals("JPEG", thumb.getFormat());
        assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

        thumb = thumbs.get(1);
        assertEquals(Integer.valueOf(162), thumb.getHeight());
        assertEquals(Integer.valueOf(216), thumb.getWidth());
        assertEquals("JPEG", thumb.getFormat());
        assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

        checkTransform(metadata, "29120813843205587378639665706339183422557956085575883885304382528664692315203");
    }

    @Test
    void testWithNoXPacketStart() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.XpacketBadStart, e.getErrorType());
        }
    }

    @Test
    void testWithNoXPacketEnd() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.XpacketBadEnd, e.getErrorType());
        }
    }

    @Test
    void testWithNoRDFElement() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    void testWithTwoRDFElement() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    void testWithInvalidRDFElementPrefix()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    void testWithRDFRootAsText()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    void testUndefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.NoSchema, e.getErrorType());
        }
    }

    @Test
    void testUndefinedPropertyWithDefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.NoType, e.getErrorType());
        }
    }

    @Test
    void testUndefinedStructuredWithDefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            assertEquals(ErrorType.NoValueType, e.getErrorType());
        }
    }

    @Test
    void testRdfAboutFound() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        List<XMPSchema> schemas = metadata.getAllSchemas();
        for (XMPSchema xmpSchema : schemas)
        {
            assertNotNull(xmpSchema.getAboutAttribute());
        }
    }

    @Test
    void testWihtAttributesAsProperties() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        XMPMetadata metadata = xdb.parse(fis);

        AdobePDFSchema pdf = metadata.getAdobePDFSchema();
        assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        assertEquals("application/pdf", dc.getFormat());

        XMPBasicSchema basic = metadata.getXMPBasicSchema();
        assertNotNull(basic.getCreateDate());
        
        checkTransform(metadata, "91466370449938102905842936306160100538543510664071400903097987792216034311743");
    }

    @Test
    void testSpaceTextValues() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        // check values with spaces at start or end
        // in this case, the value should not be trimmed
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/only_space_fields.xmp");
        XMPMetadata metadata = xdb.parse(is);
        // check producer
        assertEquals(" ", metadata.getAdobePDFSchema().getProducer());
        // check creator tool
        assertEquals("Canon ",metadata.getXMPBasicSchema().getCreatorTool());
        
        checkTransform(metadata, "65475542891943378255730260794798768587695617138297196920293698476028940113080");
    }

    @Test
    void testMetadataParsing() throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
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
    void testEmptyDate() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
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
        XMPMetadata xmp = xdb.parse(xmpmeta.getBytes(StandardCharsets.UTF_8));
        checkTransform(xmp, "8175296932768628269367133054275876764131784758539061072921527253098102430315");
    }

    private void checkTransform(XMPMetadata metadata, String expected)
            throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString(StandardCharsets.UTF_8.displayName()).replace("\r\n", "\n");
        byte[] ba = replaced.getBytes(StandardCharsets.UTF_8);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals(expected, result, "output:\n" + replaced);
    }
}
