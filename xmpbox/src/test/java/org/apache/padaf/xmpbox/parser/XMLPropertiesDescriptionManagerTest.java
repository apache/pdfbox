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
import org.apache.padaf.xmpbox.parser.XMLPropertiesDescriptionManager;
import org.apache.padaf.xmpbox.type.PropertyDescription;
import org.junit.Test;

public class XMLPropertiesDescriptionManagerTest {

	@Test
	public void testPropDesc() throws Exception {
		List<String> propNames = new ArrayList<String>();
		propNames.add("propName1");
		propNames.add("propName2");
		List<String> descProps = new ArrayList<String>();
		descProps.add("descProp1");
		descProps.add("descProp2");

		XMLPropertiesDescriptionManager xmlParser = new XMLPropertiesDescriptionManager();

		xmlParser.addPropertyDescription(propNames.get(0), descProps.get(0));
		xmlParser.addPropertyDescription(propNames.get(1), descProps.get(1));

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		xmlParser.toXML(bos);
		IOUtils.closeQuietly(bos);

		XMLPropertiesDescriptionManager propRetrieve = new XMLPropertiesDescriptionManager();

		InputStream is = new ByteArrayInputStream(bos.toByteArray());
		propRetrieve.loadListFromXML(is);

		List<PropertyDescription> propList = propRetrieve
				.getPropertiesDescriptionList();
		Assert.assertEquals(propNames.size(), propList.size());
		for (int i = 0; i < propList.size(); i++) {
			Assert.assertTrue(propNames.contains(propList.get(i)
					.getPropertyName()));
			Assert.assertTrue(descProps.contains(propList.get(i)
					.getDescription()));
		}

	}
}
