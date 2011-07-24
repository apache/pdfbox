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

import javax.xml.transform.TransformerException;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAFieldDescription;
import org.apache.padaf.xmpbox.schema.PDFAValueTypeDescription;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PDFAValueTypeDescriptionTest {

	protected XMPMetadata parent;

	@Before
	public void resetDocument() throws Exception {
		parent = new XMPMetadata();
	}

	@Test
	public void testCreateOnePdfaProperty() throws TransformerException {
		PDFAValueTypeDescription pdfvalueType = new PDFAValueTypeDescription(
				parent);
		String type = "value Test";
		String namespaceURI = "Text";
		String prefix = "test";
		String description = "it's a test property";

		String fieldName1 = "field1";
		String fieldValueType1 = "Text";
		String fieldDescription1 = "Field one";

		String fieldName2 = "field2";
		String fieldValueType2 = "Text";
		String fieldDescription2 = "Field two";

		pdfvalueType.setTypeNameValue(type);
		pdfvalueType.setNamespaceURIValue(namespaceURI);
		pdfvalueType.setPrefixValue(prefix);
		pdfvalueType.setDescriptionValue(description);

		Assert.assertEquals("pdfaType:type", pdfvalueType.getTypeName()
				.getQualifiedName());
		Assert.assertEquals(type, pdfvalueType.getTypeNameValue());

		Assert.assertEquals("pdfaType:namespaceURI", pdfvalueType
				.getNamespaceURI().getQualifiedName());
		Assert.assertEquals(namespaceURI, pdfvalueType.getNamespaceURIValue());

		Assert.assertEquals("pdfaType:prefix", pdfvalueType.getPrefix()
				.getQualifiedName());
		Assert.assertEquals(prefix, pdfvalueType.getPrefixValue());

		Assert.assertEquals("pdfaType:description", pdfvalueType
				.getDescription().getQualifiedName());
		Assert.assertEquals(description, pdfvalueType.getDescriptionValue());

		pdfvalueType.addField(fieldName1, fieldValueType1, fieldDescription1);
		pdfvalueType.addField(fieldName2, fieldValueType2, fieldDescription2);

		PDFAFieldDescription field1, field2;
		Assert.assertEquals(2, pdfvalueType.getFields().size());
		field1 = pdfvalueType.getFields().get(0);
		field2 = pdfvalueType.getFields().get(1);

		Assert.assertEquals("pdfaField:name", field1.getName()
				.getQualifiedName());
		Assert.assertEquals(fieldName1, field1.getNameValue());
		Assert.assertEquals("pdfaField:valueType", field1.getValueType()
				.getQualifiedName());
		Assert.assertEquals(fieldValueType1, field1.getValueTypeValue());
		Assert.assertEquals("pdfaField:description", field1.getDescription()
				.getQualifiedName());
		Assert.assertEquals(fieldDescription1, field1.getDescriptionValue());

		Assert.assertEquals("pdfaField:name", field2.getName()
				.getQualifiedName());
		Assert.assertEquals(fieldName2, field2.getNameValue());
		Assert.assertEquals("pdfaField:valueType", field2.getValueType()
				.getQualifiedName());
		Assert.assertEquals(fieldValueType2, field2.getValueTypeValue());
		Assert.assertEquals("pdfaField:description", field2.getDescription()
				.getQualifiedName());
		Assert.assertEquals(fieldDescription2, field2.getDescriptionValue());

		// test add same field
		pdfvalueType.addField(field1);

		/*
		 * Element e=parent.getFuturOwner().createElement("test");
		 * e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rdf",
		 * "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		 * e.appendChild(pdfvalueType.getElement());
		 * parent.getFuturOwner().appendChild(e);
		 * XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8");
		 */
	}

}
