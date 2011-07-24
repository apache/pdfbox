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

package org.apache.padaf.xmpbox.parser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.padaf.xmpbox.parser.XMLValueTypeDescriptionManager;
import org.apache.padaf.xmpbox.type.FieldDescription;
import org.apache.padaf.xmpbox.type.ValueTypeDescription;
import org.junit.Test;

public class XMLValueTypeDescriptionManagerTest {

	@Test
	public void testPropDesc() throws Exception {
		List<String> types = new ArrayList<String>();
		types.add("type1");
		types.add("type2");

		List<String> uris = new ArrayList<String>();
		uris.add("nsURI1");
		uris.add("nsURI2");

		List<String> prefixs = new ArrayList<String>();
		prefixs.add("pref1");
		prefixs.add("pref2");

		List<String> descProps = new ArrayList<String>();
		descProps.add("descProp1");
		descProps.add("descProp2");

		XMLValueTypeDescriptionManager xmlParser = new XMLValueTypeDescriptionManager();

		xmlParser.addValueTypeDescription(types.get(0), uris.get(0), prefixs
				.get(0), descProps.get(0));
		xmlParser.addValueTypeDescription(types.get(1), uris.get(1), prefixs
				.get(1), descProps.get(1));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		xmlParser.toXML(bos);
		IOUtils.closeQuietly(bos);

		XMLValueTypeDescriptionManager propRetrieve = new XMLValueTypeDescriptionManager();

		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		propRetrieve.loadListFromXML(is);

		List<ValueTypeDescription> vtList = propRetrieve
				.getValueTypesDescriptionList();
		Assert.assertEquals(types.size(), vtList.size());
		for (int i = 0; i < vtList.size(); i++) {
			Assert.assertTrue(types.contains(vtList.get(i).getType()));
			Assert.assertTrue(uris.contains(vtList.get(i).getNamespaceURI()));
			Assert.assertTrue(prefixs.contains(vtList.get(i).getPrefix()));
			Assert.assertTrue(descProps
					.contains(vtList.get(i).getDescription()));
			Assert.assertNull(vtList.get(i).getFields());
		}

	}

	@Test
	public void testPropDescWithField() throws Exception {
		String type = "type1";

		String uri = "nsURI1";

		String prefix = "pref1";

		String descProp = "descProp1";

		List<String> fieldNames = new ArrayList<String>();
		fieldNames.add("fieldName1");
		fieldNames.add("fieldName2");

		List<String> fieldValueTypes = new ArrayList<String>();
		fieldValueTypes.add("fieldVT1");
		fieldValueTypes.add("fieldVT2");

		List<String> fieldDescription = new ArrayList<String>();
		fieldDescription.add("FieldDesc1");
		fieldDescription.add("FieldDesc2");

		List<FieldDescription> fieldList = new ArrayList<FieldDescription>();
		fieldList.add(new FieldDescription(fieldNames.get(0), fieldValueTypes
				.get(0), fieldDescription.get(0)));
		fieldList.add(new FieldDescription(fieldNames.get(1), fieldValueTypes
				.get(1), fieldDescription.get(1)));

		XMLValueTypeDescriptionManager xmlParser = new XMLValueTypeDescriptionManager();

		xmlParser.addValueTypeDescription(type, uri, prefix, descProp,
				fieldList);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		xmlParser.toXML(bos);
		IOUtils.closeQuietly(bos);

		XMLValueTypeDescriptionManager propRetrieve = new XMLValueTypeDescriptionManager();

		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		propRetrieve.loadListFromXML(is);

		List<ValueTypeDescription> vtList = propRetrieve
				.getValueTypesDescriptionList();
		Assert.assertEquals(1, vtList.size());
		ValueTypeDescription vt = vtList.get(0);
		Assert.assertEquals(type, vt.getType());
		Assert.assertEquals(uri, vt.getNamespaceURI());
		Assert.assertEquals(prefix, vt.getPrefix());
		Assert.assertEquals(descProp, vt.getDescription());
		List<FieldDescription> fieldsFound = vt.getFields();
		Assert.assertNotNull(fieldsFound);

		for (int i = 0; i < fieldsFound.size(); i++) {
			Assert
					.assertTrue(fieldNames.contains(fieldsFound.get(i)
							.getName()));
			Assert.assertTrue(fieldValueTypes.contains(fieldsFound.get(i)
					.getValueType()));
			Assert.assertTrue(fieldDescription.contains(fieldsFound.get(i)
					.getDescription()));

		}

	}
}
