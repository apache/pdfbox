/*

 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.

 */

package org.apache.xmpbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializationException;
import org.junit.jupiter.api.Test;

/**
 * Test XMP MetaData Transformer
 * 
 * @author a183132
 * 
 */
class XMPMetaDataTest
{
    @Test
    void testAddingSchem()
    {
        final XMPMetadata metadata = XMPMetadata.createXMPMetadata();
        final String tmpNsURI = "http://www.test.org/schem/";
        final XMPSchema tmp = new XMPSchema(metadata, tmpNsURI, "test");
        tmp.addQualifiedBagValue("BagContainer", "Value1");
        tmp.addQualifiedBagValue("BagContainer", "Value2");
        tmp.addQualifiedBagValue("BagContainer", "Value3");

        tmp.addUnqualifiedSequenceValue("SeqContainer", "Value1");
        tmp.addUnqualifiedSequenceValue("SeqContainer", "Value2");
        tmp.addUnqualifiedSequenceValue("SeqContainer", "Value3");

        tmp.addProperty(metadata.getTypeMapping().createText(null, "test", "simpleProperty", "YEP"));

        final XMPSchema tmp2 = new XMPSchema(metadata, "http://www.space.org/schem/", "space", "space");
        tmp2.addUnqualifiedSequenceValue("SeqSpContainer", "ValueSpace1");
        tmp2.addUnqualifiedSequenceValue("SeqSpContainer", "ValueSpace2");
        tmp2.addUnqualifiedSequenceValue("SeqSpContainer", "ValueSpace3");

        metadata.addSchema(tmp);

        metadata.addSchema(tmp2);

        // Check schema getting
        assertEquals(tmp, metadata.getSchema(tmpNsURI));
        assertNull(metadata.getSchema("THIS URI NOT EXISTS !"));

        final List<XMPSchema> vals = metadata.getAllSchemas();
        assertTrue(vals.contains(tmp));
        assertTrue(vals.contains(tmp2));
    }

    /*
     * @Test public void displayResult() throws TransformException { System.out.println
     * ("info used:\n XPacketBegin:"+metadata.getXpacketBegin()+ "\n XPacketID:"+metadata.getXpacketId());
     * SaveMetadataHelper.serialize(metadata, true, System.out);
     * 
     * }
     */

    @Test
    void testTransformerExceptionMessage() throws XmpSerializationException
    {
        assertThrows(org.apache.xmpbox.xml.XmpSerializationException.class, () -> {
	        throw new XmpSerializationException("TEST");
	    });  
    }

    @Test
    void testTransformerExceptionWithCause() throws XmpSerializationException
    {
        assertThrows(org.apache.xmpbox.xml.XmpSerializationException.class, () -> {
	        throw new XmpSerializationException("TEST", new Throwable());
	    });
    }

    @Test
    void testInitMetaDataWithInfo() throws Exception
    {
        final String xpacketBegin = "TESTBEG";
        final String xpacketId = "TESTID";
        final String xpacketBytes = "TESTBYTES";
        final String xpacketEncoding = "TESTENCOD";
        final XMPMetadata metadata = XMPMetadata.createXMPMetadata(xpacketBegin, xpacketId, xpacketBytes, xpacketEncoding);
        assertEquals(xpacketBegin, metadata.getXpacketBegin());
        assertEquals(xpacketId, metadata.getXpacketId());
        assertEquals(xpacketBytes, metadata.getXpacketBytes());
        assertEquals(xpacketEncoding, metadata.getXpacketEncoding());
    }
    
    /**
     * Test whether the bug reported in PDFBOX-3257 and PDFBOX-3258 has been fixed: setting
     * CreateDate twice must not insert two elements, and fixing this must not interfere with the
     * handling of lists.
     *
     * @throws IOException
     * @throws XmpParsingException 
     */
    @Test
    void testPDFBOX3257() throws IOException, XmpParsingException
    {
        // taken from file test-landscape2.pdf
        final String xmpmeta = "<?xpacket id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
                + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 4.0-c316 44.253921, Sun Oct 01 2006 17:14:39\">\n"
                + "   <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:xap=\"http://ns.adobe.com/xap/1.0/\">\n"
                + "         <xap:CreatorTool>Acrobat PDFMaker 8.1 for Word</xap:CreatorTool>\n"
                + "         <xap:ModifyDate>2008-11-12T15:29:43+01:00</xap:ModifyDate>\n"
                + "         <xap:CreateDate>2008-11-12T15:29:40+01:00</xap:CreateDate>\n"
                + "         <xap:MetadataDate>2008-11-12T15:29:43+01:00</xap:MetadataDate>\n"
                + "      </rdf:Description>\n"
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:pdf=\"http://ns.adobe.com/pdf/1.3/\">\n"
                + "         <pdf:Producer>Acrobat Distiller 8.1.0 (Windows)</pdf:Producer>\n"
                + "      </rdf:Description>\n"
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n"
                + "         <dc:format>application/pdf</dc:format>\n"
                + "         <dc:creator>\n"
                + "            <rdf:Seq>\n"
                + "               <rdf:li>R002325</rdf:li>\n"
                + "            </rdf:Seq>\n"
                + "         </dc:creator>\n"
                + "         <dc:subject>\n"
                + "            <rdf:Bag>\n"
                + "               <rdf:li>one</rdf:li>\n"
                + "               <rdf:li>two</rdf:li>\n"
                + "               <rdf:li>three</rdf:li>\n"
                + "               <rdf:li>four</rdf:li>\n"
                + "            </rdf:Bag>\n"
                + "         </dc:subject>\n"
                + "         <dc:title>\n"
                + "            <rdf:Alt>\n"
                + "               <rdf:li xml:lang=\"x-default\"> </rdf:li>\n"
                + "            </rdf:Alt>\n"
                + "         </dc:title>\n"
                + "      </rdf:Description>\n"
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:xapMM=\"http://ns.adobe.com/xap/1.0/mm/\">\n"
                + "         <xapMM:DocumentID>uuid:31ae92cf-9a27-45e0-9371-0d2741e25919</xapMM:DocumentID>\n"
                + "         <xapMM:InstanceID>uuid:2c7eb5da-9210-4666-8cef-e02ef6631c5e</xapMM:InstanceID>\n"
                + "      </rdf:Description>\n"
                + "   </rdf:RDF>\n"
                + "</x:xmpmeta>\n"
                + "<?xpacket end=\"w\"?>";
        final DomXmpParser xmpParser = new DomXmpParser();
        xmpParser.setStrictParsing(false);
        //IOUtils.copy(meta.createInputStream(),System.out);
        final XMPMetadata xmp = xmpParser.parse(xmpmeta.getBytes());
        final XMPBasicSchema basicSchema = xmp.getXMPBasicSchema();
        final Calendar createDate1 = basicSchema.getCreateDate();
        basicSchema.setCreateDate(new GregorianCalendar());
        final Calendar createDate2 = basicSchema.getCreateDate();
        assertNotEquals(createDate1, createDate2, "CreateDate has not been set");
        
        // check that bugfix does not interfere with lists of properties with same name
        final DublinCoreSchema dublinCoreSchema = xmp.getDublinCoreSchema();
        final List<String> subjects = dublinCoreSchema.getSubjects();
        assertEquals(4, subjects.size());
    }

}
