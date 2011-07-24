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

package org.apache.padaf.xmpbox;


import org.apache.padaf.xmpbox.BuildPDFExtensionSchemaHelper;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.DateConverter;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.SchemaDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BuildPDFAExtensionSchemaDescriptionTest {

	protected XMPMetadata metadata;

	@Before
	public void init() throws Exception {
		metadata = new XMPMetadata();

	}

	@Test
	public void test() throws Exception {
		try {
			BirthCertificateSchemaWithXMLDescriptions schema = new BirthCertificateSchemaWithXMLDescriptions(
					metadata);
			schema.setFirstname("Bailleul");
			schema.addLastname("Guillaume");
			schema.addLastname("Bertin");
			schema.setBirthCountry("France");
			schema.setBirthPlace("Bethune");
			schema.setBirthDate(DateConverter.toCalendar("1976-09-16"));
			schema.setMailaddr("name", "domain");
			// birth-certificate PDF/A Extension Schema definition building
			BuildPDFExtensionSchemaHelper.includePDFAExtensionDefinition(
					metadata, schema);

			// Values checking is made which what is expected with Schema
			// definition and xml files
			PDFAExtensionSchema ext = metadata.getPDFExtensionSchema();
			Assert.assertEquals(1, ext.getDescriptionSchema().size());
			SchemaDescription schemaDescription = ext.getDescriptionSchema()
					.get(0);
			Assert.assertEquals(schema.getNamespaceValue(), schemaDescription
					.getNameSpaceURI());
			Assert.assertEquals(schema.getPrefix(), schemaDescription
					.getPrefix());
			Assert.assertEquals("Birth-Certificate Schema", schemaDescription
					.getSchema());

			Assert.assertEquals(6, schemaDescription.getProperties().size());
			Assert.assertEquals(1, schemaDescription.getValueTypes().size());
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}

	}

}
