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
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.xmpbox.xml.XmpSerializer;

class DeserializationTest
{

    protected ByteArrayOutputStream bos;

    protected XmpSerializer serializer;

    private DomXmpParser xdb;

    @BeforeEach
    void init() throws XmpParsingException
    {
        bos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
        xdb = new DomXmpParser();
    }

    @Test
    void testStructuredRecursive() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");

        xdb.parse(fis);
    }

    @Test
    void testEmptyLi() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");

        xdb.parse(fis);
    }

    @Test
    void testEmptyLi2() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata meta = xdb.parse(fis);
        DublinCoreSchema dc = meta.getDublinCoreSchema();
        dc.getCreatorsProperty();
    }

    @Test
    void testGetTitle() throws XmpParsingException, BadFieldValueException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        XMPMetadata meta = xdb.parse(fis);
        DublinCoreSchema dc = meta.getDublinCoreSchema();
        String s = dc.getTitle(null);
        assertEquals("title value", s);
    }

    @Test
    void testAltBagSeq() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");
        XMPMetadata metadata=xdb.parse(fis);
        checkTransform(metadata, "96393121650279079517435472505216876362174786845882116375023427217573081361024");
    }

    @Test
    void testIsartorStyleWithThumbs() throws XmpParsingException, IOException, BadFieldValueException
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
        XMPMetadata meta = xdb.parse(fis);
        List<XMPSchema> schemas = meta.getAllSchemas();
        for (XMPSchema xmpSchema : schemas)
        {
            assertNotNull(xmpSchema.getAboutAttribute());
        }
    }

    @Test
    void testWihtAttributesAsProperties() throws XmpParsingException
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        XMPMetadata meta = xdb.parse(fis);

        AdobePDFSchema pdf = meta.getAdobePDFSchema();
        assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        DublinCoreSchema dc = meta.getDublinCoreSchema();
        assertEquals("application/pdf", dc.getFormat());

        XMPBasicSchema basic = meta.getXMPBasicSchema();
        assertNotNull(basic.getCreateDate());
    }

    @Test
    void testSpaceTextValues() throws XmpParsingException
    {
        // check values with spaces at start or end
        // in this case, the value should not be trimmed
        InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/only_space_fields.xmp");
        XMPMetadata meta = xdb.parse(is);
        // check producer
        assertEquals(" ", meta.getAdobePDFSchema().getProducer());
        // check creator tool
        assertEquals("Canon ",meta.getXMPBasicSchema().getCreatorTool());
    }

    private void checkTransform(XMPMetadata metadata, String expected)
            throws TransformerException, NoSuchAlgorithmException, IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        byte[] ba = replaced.getBytes(StandardCharsets.UTF_8);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals(expected, result);
    }
}
