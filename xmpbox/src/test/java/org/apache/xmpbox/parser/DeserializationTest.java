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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;

/**
 * DomXmpParser imports the XML into an internal representation. XmpSerializer exports this into
 * XML. The result may look different, but should be the same from a data point of view.
 *
 * @author Tilman Hausherr
 */
@Isolated
@ResourceLock(Resources.TIME_ZONE)
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
    void testStructuredRecursive() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        // not valid XMP according to https://www.pdflib.com/pdf-knowledge-base/xmp/free-xmp-validator/
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/structured_recursive.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            checkTransform(metadata, "62495942572014793625872774972947435765670563107818217447706375288846297812281", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testEmptyLi() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/empty_list.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            checkTransform(metadata, "95754993383010030299848397520773287413798669761891751126809013411187892693280", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testEmptyLi2() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            DublinCoreSchema dc = metadata.getDublinCoreSchema();
            dc.getCreatorsProperty();
            checkTransform(metadata, "39450703080437563739186076111811684356424147071014681699119272065568305393521", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testGetTitle() throws XmpParsingException, BadFieldValueException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            DublinCoreSchema dc = metadata.getDublinCoreSchema();
            String s = dc.getTitle(null);
            assertEquals("title value", s);
        }
    }

    @Test
    void testAltBagSeq() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/AltBagSeqTest.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            checkTransform(metadata, "89123270336154452745819041017446278583816329940574853160909598044560152910018", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testIsartorStyleWithThumbs()
            throws XmpParsingException, BadFieldValueException, TransformerException, NoSuchAlgorithmException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/org/apache/xmpbox/parser/ThumbisartorStyle.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);

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

            // Check the extension schema (also serves as example on how to retrieve)
            XMPSchema acmeMailSchema = metadata.getSchema("http://www.acme.com/ns/email/1/");
            DateType deliveryDate = (DateType) acmeMailSchema.getProperty("Delivery-Date");
            assertEquals("2007-11-09T09:55:36+01:00", deliveryDate.getStringValue());
            DefinedStructuredType dst = (DefinedStructuredType) acmeMailSchema.getProperty("From");
            assertEquals("[name=TextType:John Doe]", dst.getProperty("name").toString());
            assertEquals("[mailto=TextType:john@acme.com]", dst.getProperty("mailto").toString());

            checkTransform(metadata, "64755266855514150823517184659364700851455308334441170957883187622624192802093", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testWithNoXPacketStart() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacket.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.XpacketBadStart, ex.getErrorType());
        }
    }

    @Test
    void testWithNoXPacketEnd() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noxpacketend.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.XpacketBadEnd, ex.getErrorType());
        }
    }

    @Test
    void testWithNoRDFElement() throws XmpParsingException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/noroot.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.Format, ex.getErrorType());
        }
    }

    @Test
    void testWithTwoRDFElement() throws XmpParsingException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/tworoot.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.Format, ex.getErrorType());
        }
    }

    @Test
    void testWithInvalidRDFElementPrefix() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot2.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.Format, ex.getErrorType());
        }
    }

    @Test
    void testWithRDFRootAsText() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/invalidroot.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.Format, ex.getErrorType());
        }
    }

    @Test
    void testUndefinedSchema() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedschema.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.NoSchema, ex.getErrorType());
        }
    }

    @Test
    void testUndefinedPropertyWithDefinedSchema() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedpropertyindefinedschema.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.NoType, ex.getErrorType());
        }
    }

    @Test
    void testUndefinedStructuredWithDefinedSchema() throws IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/invalidxmp/undefinedstructuredindefinedschema.xml"))
        {
            XmpParsingException ex = assertThrows(XmpParsingException.class, () -> xdb.parse(is));
            assertEquals(ErrorType.NoValueType, ex.getErrorType());
        }
    }

    @Test
    void testRdfAboutFound() throws XmpParsingException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/emptyli.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);
            List<XMPSchema> schemas = metadata.getAllSchemas();
            for (XMPSchema xmpSchema : schemas)
            {
                assertNotNull(xmpSchema.getAboutAttribute());
            }
        }
    }

    @Test
    void testWithAttributesAsProperties() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/attr_as_props.xml"))
        {
            XMPMetadata metadata = xdb.parse(is);

            AdobePDFSchema pdf = metadata.getAdobePDFSchema();
            assertEquals("GPL Ghostscript 8.64", pdf.getProducer());

            DublinCoreSchema dc = metadata.getDublinCoreSchema();
            assertEquals("application/pdf", dc.getFormat());

            XMPBasicSchema basic = metadata.getXMPBasicSchema();
            assertNotNull(basic.getCreateDate());

            PDFAIdentificationSchema pdfaIdentificationSchema = metadata.getPDFAIdentificationSchema();
            assertEquals("B", pdfaIdentificationSchema.getConformance());
            assertEquals(1, pdfaIdentificationSchema.getPart());

            XMPMediaManagementSchema xmpMediaManagementSchema = metadata.getXMPMediaManagementSchema();
            assertEquals("e7127190-445c-11ea-0000-b3bc74086807", xmpMediaManagementSchema.getDocumentID());

            checkTransform(metadata, "27499224985683016678197540524065114038595582230834506941950503218519476041225", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testSpaceTextValues() throws XmpParsingException, TransformerException, NoSuchAlgorithmException, IOException
    {
        // check values with spaces at start or end
        // in this case, the value should not be trimmed
        try (InputStream is = DomXmpParser.class.getResourceAsStream("/validxmp/only_space_fields.xmp"))
        {
            XMPMetadata metadata = xdb.parse(is);
            // check producer
            assertEquals(" ", metadata.getAdobePDFSchema().getProducer());
            // check creator tool
            assertEquals("Canon ", metadata.getXMPBasicSchema().getCreatorTool());

            checkTransform(metadata, "9220923061800113567693538810355030344095407871190202111473587642358933618073", metadata.getAllSchemas().size());
        }
    }

    @Test
    void testMetadataParsing() throws TransformerException, NoSuchAlgorithmException, XmpParsingException
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
     */
    @Test
    void testEmptyDate() throws XmpParsingException, TransformerException, NoSuchAlgorithmException
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
        XMPMetadata metadata = xdb.parse(xmpmeta.getBytes(StandardCharsets.UTF_8));
        checkTransform(metadata, "19030153876683461724958694183980892665426846590791273142114566290124997390122", metadata.getAllSchemas().size());
    }

    private void checkTransform(XMPMetadata metadata, String expected, int expectedSchemaCount)
            throws TransformerException, NoSuchAlgorithmException, XmpParsingException
    {
        serializer.serialize(metadata, baos, true);
        String replaced = baos.toString(StandardCharsets.UTF_8).replace("\r\n", "\n");
        byte[] ba = replaced.getBytes(StandardCharsets.UTF_8);
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(ba);
        String result = new BigInteger(1, digest).toString();
        assertEquals(expected, result, "output:\n" + replaced);
        XMPMetadata xmp = xdb.parse(baos.toByteArray()); // tests round trip
        assertEquals(expectedSchemaCount, xmp.getAllSchemas().size());
    }
}
