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

class DeserializationTest
{

    private ByteArrayOutputStream baos;

    private XmpSerializer serializer;

    private DomXmpParser xdb;

    private static TimeZone defaultTZ;

    @BeforeClass
    static void initAll()
    {
        defaultTZ = TimeZone.getDefault();
        // Need to set a timezone or date values will be different depending on test location
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Before
    void init() throws XmpParsingException
    {
        baos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
        xdb = new DomXmpParser();
    }

    @AfterClass
    static void finishAll()
    {
        TimeZone.setDefault(defaultTZ);
    }

    @Test
    public void testStructuredRecursive() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");
        XMPMetadata metadata = xdb.parse(fis);
        checkTransform(metadata, "50429052370059903229869639943824137435756655804864824611365505219590816799783");
    }

    @Test
    public void testEmptyLi() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");
        XMPMetadata metadata = xdb.parse(fis);
        checkTransform(metadata, "92757984740574362800045336947395134346147179161385043989715484359442690118913");
    }

    @Test
    public void testEmptyLi2() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        dc.getCreatorsProperty();
        checkTransform(metadata, "84846877440303452108560435796840772468446174326989274262473618453524301429629");
    }

    @Test
    public void testGetTitle() throws XmpParsingException, BadFieldValueException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        String s = dc.getTitle(null);
        Assert.assertEquals("title value", s);
    }

    @Test
    public void testAltBagSeq() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");
        XMPMetadata metadata=xdb.parse(fis);
        checkTransform(metadata, "16805992283807186369849610414335227396239089071611806706387795179375897398118");
    }

    @Test
    public void testIsartorStyleWithThumbs()
            throws XmpParsingException, IOException, BadFieldValueException, TransformerException, NoSuchAlgorithmException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml");

        XMPMetadata metadata = xdb.parse(fis);

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
    }

    @Test
    public void testWithNoXPacketStart() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadStart, e.getErrorType());
        }
    }

    @Test
    public void testWithNoXPacketEnd() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadEnd, e.getErrorType());
        }
    }

    @Test
    public void testWithNoRDFElement() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithTwoRDFElement() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithInvalidRDFElementPrefix()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithRDFRootAsText()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoSchema, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedPropertyWithDefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoType, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedStructuredWithDefinedSchema()
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml");
        try
        {
            xdb.parse(fis);
            fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoValueType, e.getErrorType());
        }
    }

    @Test
    public void testRdfAboutFound() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata metadata = xdb.parse(fis);
        List<XMPSchema> schemas = metadata.getAllSchemas();
        for (XMPSchema xmpSchema : schemas)
        {
            Assert.assertNotNull(xmpSchema.getAboutAttribute());
        }
    }

    @Test
    public void testWihtAttributesAsProperties() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        XMPMetadata metadata = xdb.parse(fis);

        AdobePDFSchema pdf = metadata.getAdobePDFSchema();
        Assert.assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        DublinCoreSchema dc = metadata.getDublinCoreSchema();
        Assert.assertEquals("application/pdf", dc.getFormat());

        XMPBasicSchema basic = metadata.getXMPBasicSchema();
        Assert.assertNotNull(basic.getCreateDate());
        
        checkTransform(metadata, "91466370449938102905842936306160100538543510664071400903097987792216034311743");
    }

    @Test
    public void testSpaceTextValues() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
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
    }

    private void checkTransform(XMPMetadata metadata, String expected)
            throws TransformerException, NoSuchAlgorithmException, UnsupportedEncodingException
    {
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString("UTF-8").replace("\r\n", "\n");
        byte[] ba = replaced.getBytes("UTF-8");
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals(expected, result);
    }
}
