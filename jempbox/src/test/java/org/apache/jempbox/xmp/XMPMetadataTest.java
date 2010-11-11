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
	
}
