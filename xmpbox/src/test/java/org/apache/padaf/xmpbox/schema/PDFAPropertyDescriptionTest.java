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


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.PDFAPropertyDescription;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class PDFAPropertyDescriptionTest {

	protected XMPMetadata parent;

	@Before
	public void resetDocument() throws Exception {
		parent = new XMPMetadata();
	}

	@Test
	public void testCreateOnePdfaProperty() throws Exception {
		PDFAPropertyDescription pdfprop = new PDFAPropertyDescription(parent);
		String name = "value Test";
		String valueT = "Text";
		String cat = "external";
		String desc = "it's a test property";
		pdfprop.setNameValue(name);
		pdfprop.setValueTypeValue(valueT);
		pdfprop.setCategoryValue(cat);
		pdfprop.setDescriptionValue(desc);

		Assert.assertEquals(name, pdfprop.getNameValue());
		Assert.assertEquals(valueT, pdfprop.getValueTypeValue());
		Assert.assertEquals(cat, pdfprop.getCategoryValue());
		Assert.assertEquals(desc, pdfprop.getDescriptionValue());

		Assert.assertEquals(name, pdfprop.getName().getStringValue());
		Assert.assertEquals(valueT, pdfprop.getValueType().getStringValue());
		Assert.assertEquals(cat, pdfprop.getCategory().getStringValue());
		Assert.assertEquals(desc, pdfprop.getDescription().getStringValue());

		Element e = parent.getFuturOwner().createElement("test");
		e.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:rdf",
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		e.appendChild(pdfprop.getElement());
		parent.getFuturOwner().appendChild(e);
		// XMLUtil.save(parent.getFuturOwner(), System.out, "UTF-8");
	}

	@Test(expected = BadFieldValueException.class)
	public void testBadFieldCheckForCategory() throws Exception {
		PDFAPropertyDescription pdfprop = new PDFAPropertyDescription(parent);
		String name = "value Test";
		String valueT = "Text";
		String cat = "badvalue";
		String desc = "it's a test property";
		pdfprop.setNameValue(name);
		pdfprop.setValueTypeValue(valueT);
		pdfprop.setCategoryValue(cat);
		pdfprop.setDescriptionValue(desc);

	}

}
