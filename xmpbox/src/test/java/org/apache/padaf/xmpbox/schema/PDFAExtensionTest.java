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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XmpSchemaException;
import org.apache.padaf.xmpbox.schema.PDFAExtensionSchema;
import org.apache.padaf.xmpbox.schema.PDFAPropertyDescription;
import org.apache.padaf.xmpbox.schema.PDFAValueTypeDescription;
import org.apache.padaf.xmpbox.schema.SchemaDescription;
import org.apache.padaf.xmpbox.type.Attribute;
import org.junit.Before;
import org.junit.Test;

public class PDFAExtensionTest {

	protected XMPMetadata metadata;

	@Before
	public void initTempMetaData() throws Exception {
		metadata = new XMPMetadata();
	}

	@Test
	public void testNSManualDeclaration() throws Exception {
		HashMap<String, String> namespaces = new HashMap<String, String>();
		namespaces.put(PDFAExtensionSchema.PDFAEXTENSION,
				PDFAExtensionSchema.PDFAEXTENSIONURI);
		namespaces.put(PDFAExtensionSchema.PDFAFIELD,
				PDFAExtensionSchema.PDFAFIELDURI);
		namespaces.put(PDFAExtensionSchema.PDFAPROPERTY,
				PDFAExtensionSchema.PDFAPROPERTYURI);
		namespaces.put(PDFAExtensionSchema.PDFASCHEMA,
				PDFAExtensionSchema.PDFASCHEMAURI);
		namespaces.put(PDFAExtensionSchema.PDFATYPE,
				PDFAExtensionSchema.PDFATYPEURI);

		PDFAExtensionSchema schem = metadata
				.createAndAddPDFAExtensionSchemaWithNS(namespaces);
		Iterator<Attribute> att = schem.getAllAttributes().iterator();

		// PDFAExtension is removed during the building of
		// PDFAExtensionSchemaWithNS
		namespaces.put(PDFAExtensionSchema.PDFAEXTENSION,
				PDFAExtensionSchema.PDFAEXTENSIONURI);
		Attribute tmp;

		while (att.hasNext()) {
			// System.out.println(att.next().getPropertyName());
			tmp = att.next();
			if (!tmp.getLocalName().equals("about")) {
				Assert.assertTrue(namespaces.containsKey(tmp.getLocalName()));
			}
		}
	}

	@Test(expected = XmpSchemaException.class)
	public void testNoPdfExtension() throws Exception {
		Map<String, String> namespaces = new HashMap<String, String>();
		new PDFAExtensionSchema(metadata, namespaces);
	}

	@Test
	public void testSameSchemaTwice() throws Exception {
		PDFAExtensionSchema schema = new PDFAExtensionSchema(metadata);
		SchemaDescription sd1 = schema.createSchemaDescription();
		sd1.setPrefixValue("pref1");
		sd1.setNameSpaceURIValue("http://uri1");
		sd1.setSchemaValue("pref 1");
		Assert.assertNull(schema.addSchemaDescription(sd1));

		SchemaDescription sd2 = schema.createSchemaDescription();
		sd2.setPrefixValue("pref1");
		sd2.setNameSpaceURIValue("http://uri1");
		sd2.setSchemaValue("pref 1");
		Assert.assertNotNull(schema.addSchemaDescription(sd2));

		List<SchemaDescription> lsd = schema.getDescriptionSchema();
		Assert.assertEquals(1, lsd.size());
		SchemaDescription sd = lsd.get(0);
		Assert.assertEquals("pref1", sd.getPrefix());
		Assert.assertEquals("http://uri1", sd.getNameSpaceURI());
		Assert.assertEquals("pref 1", sd.getSchema());
	}

	@Test
	public void testSameNSSchemaDifferentPrefix() throws Exception {
		PDFAExtensionSchema schema = new PDFAExtensionSchema(metadata);
		SchemaDescription sd1 = schema.createSchemaDescription();
		sd1.setPrefixValue("pref1");
		sd1.setNameSpaceURIValue("http://uri1");
		sd1.setSchemaValue("pref 1");
		Assert.assertNull(schema.addSchemaDescription(sd1));

		SchemaDescription sd2 = schema.createSchemaDescription();
		sd2.setPrefixValue("pref2");
		sd2.setNameSpaceURIValue("http://uri1");
		sd2.setSchemaValue("pref 2");
		Assert.assertNull(schema.addSchemaDescription(sd2));

		List<SchemaDescription> lsd = schema.getDescriptionSchema();
		Assert.assertEquals(2, lsd.size());
		SchemaDescription sd = lsd.get(0);
		Assert.assertEquals("pref1", sd.getPrefix());
		Assert.assertEquals("http://uri1", sd.getNameSpaceURI());
		Assert.assertEquals("pref 1", sd.getSchema());
	}

	@Test
	public void testDifferentSchemaDifferentPrefix() throws Exception {
		PDFAExtensionSchema schema = new PDFAExtensionSchema(metadata);
		SchemaDescription sd1 = schema.createSchemaDescription();
		sd1.setPrefixValue("pref1");
		sd1.setNameSpaceURIValue("http://uri1");
		sd1.setSchemaValue("pref 1");
		Assert.assertNull(schema.addSchemaDescription(sd1));

		SchemaDescription sd2 = schema.createSchemaDescription();
		sd2.setPrefixValue("pref2");
		sd2.setNameSpaceURIValue("http://uri2");
		sd2.setSchemaValue("pref 2");
		Assert.assertNull(schema.addSchemaDescription(sd2));

		List<SchemaDescription> lsd = schema.getDescriptionSchema();
		Assert.assertEquals(2, lsd.size());
		SchemaDescription sd = lsd.get(0);
		Assert.assertEquals("pref1", sd.getPrefix());
		Assert.assertEquals("http://uri1", sd.getNameSpaceURI());
		Assert.assertEquals("pref 1", sd.getSchema());
	}

	@Test
	public void testPDFExt() throws Exception {

		PDFAExtensionSchema schem = metadata
				.createAndAddPDFAExtensionSchemaWithDefaultNS();

		Assert.assertEquals("pdfaExtension", schem.getPrefix());
		Assert.assertEquals("http://www.aiim.org/pdfa/ns/extension/", schem
				.getNamespaceValue());

		String schemDesc = "Schema Acte de naissance";
		String schemURI = "http://test.apache.com/xap/adn/";
		String schemPrefix = "adn";

		SchemaDescription desc = schem.createSchemaDescription();
		desc.setSchemaValue(schemDesc);
		desc.setNameSpaceURIValue(schemURI);
		desc.setPrefixValue(schemPrefix);
		schem.addSchemaDescription(desc);

		Assert.assertEquals(schemDesc, desc.getSchema());
		Assert.assertEquals(schemURI, desc.getNameSpaceURI());
		Assert.assertEquals(schemPrefix, desc.getPrefix());

		String descExpected = "nom de la personne concernée";
		desc.addProperty("nom", "Text", "external",
				"nom de la personne concernee");
		desc.addProperty("nom", "Text", "external", descExpected);
		Assert.assertEquals(1, desc.getProperties().size());
		Assert.assertEquals(descExpected, desc.getProperties().get(0)
				.getDescriptionValue());
		desc.addProperty("prénom", "Text", "external",
				"prénom de la personne concernée");

		List<PDFAPropertyDescription> list = desc.getProperties();
		Assert.assertEquals("nom", list.get(0).getNameValue());
		Assert.assertEquals("prénom", list.get(1).getNameValue());

		// Check retrieve descriptions
		Assert.assertEquals(desc, schem.getIteratorOfDescriptions().next());
		Assert.assertEquals(desc, schem.getDescriptionSchema().get(0));

		// check retrieve this schema in metadata
		Assert.assertEquals(schem, metadata.getPDFExtensionSchema());

		// Check if no problem when create 2 description and display result
		SchemaDescription desc2 = schem.createSchemaDescription();
		desc2.setSchemaValue("2eme schema de test");
		desc2.setNameSpaceURIValue("http://test.apache.com/xap/test/");
		desc2.setPrefixValue("tst");
		desc2.addProperty("TestText", "OwnType", "external",
				"just a text property");
		schem.addSchemaDescription(desc2);
		// Check value type
		String valType = "OwnType";
		String nsType = "http://test.apache.com/xap/test/";
		String prefType = "tst";
		String descType = "Test Own type";
		desc2.addValueType(valType, nsType, prefType, "must be replaced", null);
		desc2.addValueType(valType, nsType, prefType, descType, null);
		Assert.assertEquals(1, desc2.getValueTypes().size());
		PDFAValueTypeDescription value = desc2.getValueTypes().get(0);
		Assert.assertEquals(valType, value.getTypeNameValue());
		Assert.assertEquals(nsType, value.getNamespaceURIValue());
		Assert.assertEquals(prefType, value.getPrefixValue());
		Assert.assertEquals(descType, value.getDescriptionValue());

		// SaveMetadataHelper.serialize(metadata, true, System.out);

		// valuetype test

	}
}
