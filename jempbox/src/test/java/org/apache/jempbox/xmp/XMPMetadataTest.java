/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jempbox.xmp;

import java.io.IOException;
import java.io.StringReader;
import java.util.Calendar;
import java.util.GregorianCalendar;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

public class XMPMetadataTest extends TestCase {

    public void testLoadDublincoreExample() throws IOException {
        // http://dublincore.org/documents/dcmes-xml/
        // had to move the dc namespace declaration to get it parsed
        String xmpmeta = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\">\n" + 
            " <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"\n" + 
            //"    xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
            ">" +
            "  <rdf:Description\n" +
            //">" +
            "      xmlns:dc=\"http://purl.org/dc/elements/1.1/\">" +
            "    <dc:title>The Mona Lisa</dc:title>\n" + 
            "    <dc:description>A painting by ...</dc:description>\n" + 
            "  </rdf:Description>\n" + 
            " </rdf:RDF>\n" + 
            "</x:xmpmeta>";
        XMPMetadata xmp = XMPMetadata.load(new InputSource(new StringReader(xmpmeta)));
        XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
        assertEquals("The Mona Lisa", dc.getTitle());        
        assertEquals("A painting by ...", dc.getDescription());       
    }
    
	public void testExiv2Xmp() throws IOException {
		// XMP block as created by exiv2
		String xmpmeta = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"XMP Core 4.4.0-Exiv2\">\n" + 
				" <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + 
				"  <rdf:Description rdf:about=\"\"\n" + 
				"    xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" + 
				"   <dc:description>\n" + 
				"    <rdf:Alt>\n" + 
				"     <rdf:li xml:lang=\"x-default\">Dublin Core description</rdf:li>\n" + 
				"    </rdf:Alt>\n" + 
				"   </dc:description>\n" + 
				"  </rdf:Description>\n" + 
				" </rdf:RDF>\n" + 
				"</x:xmpmeta>";
		XMPMetadata xmp = XMPMetadata.load(new InputSource(new StringReader(xmpmeta)));
		assertEquals("Dublin Core description", xmp.getDublinCoreSchema().getDescription());		
	}
	
	public void testDescriptionFromNodeText() throws IOException {
		// From a jpeg, valid according to http://www.w3.org/RDF/Validator/
		String xmpmeta = "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"XMP Core 4.4.0\">\n" + 
				"   <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n" + 
				"      <rdf:Description rdf:about=\"\"\n" + 
				"            xmlns:exif=\"http://ns.adobe.com/exif/1.0/\">\n" + 
				"         <exif:UserComment>exif</exif:UserComment>\n" + 
				"      </rdf:Description>\n" + 
				"      <rdf:Description rdf:about=\"\"\n" + 
				"            xmlns:dc=\"http://purl.org/dc/elements/1.1/\">\n" + 
				"         <dc:description>Dublin Core description</dc:description>\n" + 
				"      </rdf:Description>\n" + 
				"   </rdf:RDF>\n" + 
				"</x:xmpmeta>";
		XMPMetadata xmp = XMPMetadata.load(new InputSource(new StringReader(xmpmeta)));
		assertEquals("Dublin Core description", xmp.getDublinCoreSchema().getDescription());
	}
        
    public void testPDFBOX3257() throws IOException
    {
        // taken from file test-landscape2.pdf
        String xmpmeta = "<?xpacket begin=\"ï»¿\" id=\"W5M0MpCehiHzreSzNTczkc9d\"?>\n"
                + "<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 4.0-c316 44.253921, Sun Oct 01 2006 17:14:39\">\n"
                + "   <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n"
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:xap=\"http://ns.adobe.com/xap/1.0/\">\n"
                + "         <xap:CreatorTool>Acrobat PDFMaker 8.1 für Word</xap:CreatorTool>\n"
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
                + "      <rdf:Description rdf:about=\"\"\n"
                + "            xmlns:pdfx=\"http://ns.adobe.com/pdfx/1.3/\">\n"
                + "         <pdfx:Company>RWE</pdfx:Company>\n"
                + "         <pdfx:SourceModified>D:20081112142931</pdfx:SourceModified>\n"
                + "      </rdf:Description>\n"
                + "   </rdf:RDF>\n"
                + "</x:xmpmeta>\n"
                + "<?xpacket end=\"w\"?>";
        XMPMetadata xmp = XMPMetadata.load(new InputSource(new StringReader(xmpmeta)));
        XMPSchemaBasic basicSchema = xmp.getBasicSchema();
        Calendar createDate1 = basicSchema.getCreateDate();
        basicSchema.setCreateDate(new GregorianCalendar());
        Calendar createDate2 = basicSchema.getCreateDate();
        assertFalse("CreateDate has not been set", createDate1.equals(createDate2));
    }

}
