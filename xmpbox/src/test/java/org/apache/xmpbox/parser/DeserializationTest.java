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
import org.apache.xmpbox.xml.XmpSerializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeserializationTest
{

    protected ByteArrayOutputStream bos;

    protected XmpSerializer serializer;

    @Before
    public void init() throws Exception
    {
        bos = new ByteArrayOutputStream();
        serializer = new XmpSerializer();
    }

    @Test
    public void testStructuredRecursive() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml");

        DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);

    }

    @Test
    public void testEmptyLi() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml");

        DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);

    }

    @Test
    public void testEmptyLi2() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");

        DomXmpParser xdb = new DomXmpParser();

        XMPMetadata meta = xdb.parse(fis);
        DublinCoreSchema dc = meta.getDublinCoreSchema();
        dc.getCreatorsProperty();
    }

    @Test
    public void testGetTitle() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");

        DomXmpParser xdb = new DomXmpParser();

        XMPMetadata meta = xdb.parse(fis);
        DublinCoreSchema dc = meta.getDublinCoreSchema();
        String s = dc.getTitle(null);
        Assert.assertEquals("title value", s);
    }

    @Test
    public void testAltBagSeq() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml");

        DomXmpParser xdb = new DomXmpParser();

        xdb.parse(fis);
        // XMPMetadata metadata=xdb.parse(fis);
        // SaveMetadataHelper.serialize(metadata, true, System.out);
    }

    @Test
    public void testIsartorStyleWithThumbs() throws Exception
    {

        InputStream fis = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml");

        DomXmpParser xdb = new DomXmpParser();

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
        Assert.assertEquals(new Integer(162), thumb.getHeight());
        Assert.assertEquals(new Integer(216), thumb.getWidth());
        Assert.assertEquals("JPEG", thumb.getFormat());
        Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

        thumb = thumbs.get(1);
        Assert.assertEquals(new Integer(162), thumb.getHeight());
        Assert.assertEquals(new Integer(216), thumb.getWidth());
        Assert.assertEquals("JPEG", thumb.getFormat());
        Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

    }

    @Test
    public void testWithNoXPacketStart() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadStart, e.getErrorType());
        }
    }

    @Test
    public void testWithNoXPacketEnd() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.XpacketBadEnd, e.getErrorType());
        }
    }

    @Test
    public void testWithNoRDFElement() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithTwoRDFElement() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithInvalidRDFElementPrefix() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testWithRDFRootAsText() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.Format, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedSchema() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoSchema, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedPropertyWithDefinedSchema() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoType, e.getErrorType());
        }
    }

    @Test
    public void testUndefinedStructuredWithDefinedSchema() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml");

        DomXmpParser xdb = new DomXmpParser();
        try
        {
            xdb.parse(fis);
            Assert.fail("Should fail during parse");
        }
        catch (XmpParsingException e)
        {
            Assert.assertEquals(ErrorType.NoValueType, e.getErrorType());
        }
    }

    @Test
    public void testRdfAboutFound() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml");
        DomXmpParser xdb = new DomXmpParser();
        XMPMetadata meta = xdb.parse(fis);
        List<XMPSchema> schemas = meta.getAllSchemas();
        for (XMPSchema xmpSchema : schemas)
        {
            Assert.assertNotNull(xmpSchema.getAboutAttribute());
        }
    }

    @Test
    public void testWihtAttributesAsProperties() throws Exception
    {
        InputStream fis = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml");
        DomXmpParser xdb = new DomXmpParser();
        XMPMetadata meta = xdb.parse(fis);

        AdobePDFSchema pdf = meta.getAdobePDFSchema();
        Assert.assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

        DublinCoreSchema dc = meta.getDublinCoreSchema();
        Assert.assertEquals("application/pdf", dc.getFormat());

        XMPBasicSchema basic = meta.getXMPBasicSchema();
        Assert.assertNotNull(basic.getCreateDate());

    }

}
