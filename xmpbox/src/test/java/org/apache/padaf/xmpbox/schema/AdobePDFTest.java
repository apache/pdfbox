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

package org.apache.padaf.xmpbox.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.AdobePDFSchema;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class AdobePDFTest extends AbstractXMPSchemaTest {

	@Before
	public void initTempMetaData() throws Exception {
		metadata = new XMPMetadata();
		schema = metadata.createAndAddAdobePDFSchema();
		schemaClass = AdobePDFSchema.class;
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(wrapProperty("Keywords", "Text", "kw1 kw2 kw3"));
		data.add(wrapProperty("PDFVersion", "Text", "1.4"));
		data.add(wrapProperty("Producer", "Text", "testcase"));

		return data;
	}

	public AdobePDFTest(String property, String type, Object value) {
		super(property, type, value);
	}

	// @Test
	// public void testPDFAIdentification() throws BadFieldValueException,
	// TransformException{
	// AdobePDFSchema schem=metadata.createAndAddAdobePDFSchema();
	//		
	// String keywords="keywords ihih";
	// String pdfVersion="1.4";
	// String producer="producer";
	//		
	// schem.setKeywordsValue(keywords);
	// schem.setPDFVersionValue(pdfVersion);
	//		
	// //Check get null if property not defined
	// Assert.assertNull(schem.getProducer());
	//		
	// schem.setProducerValue(producer);
	//		
	// Assert.assertEquals("pdf:Keywords",
	// schem.getKeywords().getQualifiedName());
	// Assert.assertEquals(keywords, schem.getKeywordsValue());
	//		
	// Assert.assertEquals("pdf:PDFVersion",
	// schem.getPDFVersion().getQualifiedName());
	// Assert.assertEquals(pdfVersion, schem.getPDFVersionValue());
	//		
	// Assert.assertEquals("pdf:Producer",
	// schem.getProducer().getQualifiedName());
	// Assert.assertEquals(producer, schem.getProducerValue());
	//		
	// //check retrieve this schema in metadata
	// Assert.assertEquals(schem, metadata.getAdobePDFSchema());
	//		
	// //SaveMetadataHelper.serialize(metadata, true, System.out);
	// }

	// @Test(expected=BadFieldValueException.class)
	// public void testBadPDFAConformanceId() throws BadFieldValueException{
	// PDFAIdentificationSchema pdfaid=
	// metadata.createAndAddPFAIdentificationSchema();
	// String conformance="kiohiohiohiohio";
	// pdfaid.setConformanceValue(conformance);
	// }
	//	
	// @Test(expected=IllegalArgumentException.class)
	// public void testBadVersionIdValueType() throws Exception {
	// PDFAIdentificationSchema pdfaid=
	// metadata.createAndAddPFAIdentificationSchema();
	// pdfaid.setVersionIdentifierValueWithString("1");
	// pdfaid.setVersionIdentifierValueWithString("ojoj");
	// }

}
