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
import java.io.InputStream;
import java.util.List;

import org.apache.xmpbox.DateConverter;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.ThumbnailType;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpParsingException.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.apache.xmpbox.xml.XmpSerializer;

class DeserializationTest {

    protected ByteArrayOutputStream bos;

    protected XmpSerializer serializer;

    @BeforeEach
    public void init() throws Exception
    {
        bos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
    }

    @Test
    void testStructuredRecursive() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");

        final DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);

    }

    @Test
    void testEmptyLi() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");

        final DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);

    }

    @Test
    void testEmptyLi2() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");

        final DomXmpParser xdb = new DomXmpParser();

        final XMPMetadata meta = xdb.parse(fis);
        final DublinCoreSchema dc = meta.getDublinCoreSchema();
        dc.getCreatorsProperty();
    }

    @Test
    void testGetTitle() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");

        final DomXmpParser xdb = new DomXmpParser();

        final XMPMetadata meta = xdb.parse(fis);
        final DublinCoreSchema dc = meta.getDublinCoreSchema();
        final String s = dc.getTitle(null);
        assertEquals("title value", s);
    }

    @Test
    void testAltBagSeq() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");

        final DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);
        // XMPMetadata metadata=xdb.parse(fis);
        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test
    void testIsartorStyleWithThumbs() throws Exception
    {

        final InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml");

        final DomXmpParser xdb = new DomXmpParser();

        final XMPMetadata metadata = xdb.parse(fis);

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
        final List<ThumbnailType> thumbs = metadata.getXMPBasicSchema().getThumbnailsProperty();
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
    void testWithNoXPacketStart() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testWithNoXPacketEnd() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testWithNoRDFElement() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testWithTwoRDFElement() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testWithInvalidRDFElementPrefix() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testWithRDFRootAsText() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testUndefinedSchema() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testUndefinedPropertyWithDefinedSchema() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testUndefinedStructuredWithDefinedSchema() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml");

        final DomXmpParser xdb = new DomXmpParser();
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
    void testRdfAboutFound() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        final DomXmpParser xdb = new DomXmpParser();
        final XMPMetadata meta = xdb.parse(fis);
        final List<XMPSchema> schemas = meta.getAllSchemas();
        for (final XMPSchema xmpSchema : schemas)
        {
            assertNotNull(xmpSchema.getAboutAttribute());
        }
    }

    @Test
    void testWihtAttributesAsProperties() throws Exception
    {
        final InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        final DomXmpParser xdb = new DomXmpParser();
        final XMPMetadata meta = xdb.parse(fis);

        final AdobePDFSchema pdf = meta.getAdobePDFSchema();
        assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        final DublinCoreSchema dc = meta.getDublinCoreSchema();
        assertEquals("application/pdf", dc.getFormat());

        final XMPBasicSchema basic = meta.getXMPBasicSchema();
        assertNotNull(basic.getCreateDate());

    }

    @Test
    void testSpaceTextValues() throws Exception
    {
        // check values with spaces at start or end
        // in this case, the value should not be trimmed
        final InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/only_space_fields.xmp");
        final DomXmpParser xdb = new DomXmpParser();
        final XMPMetadata meta = xdb.parse(is);
        // check producer
        assertEquals(" ", meta.getAdobePDFSchema().getProducer());
        // check creator tool
        assertEquals("Canon ",meta.getXMPBasicSchema().getCreatorTool());

    }
}
