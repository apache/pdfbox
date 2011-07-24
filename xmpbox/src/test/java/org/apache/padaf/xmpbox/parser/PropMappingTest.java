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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.parser.PropMapping;
import org.junit.Before;
import org.junit.Test;

public class PropMappingTest {

	protected PropMapping propMap;
	protected String nsURI = "http://www.test.org/PropMap#";

	@Before
	public void init() {
		propMap = new PropMapping(nsURI);
	}

	@Test
	public void testURI() {
		Assert.assertEquals(nsURI, propMap.getConcernedNamespace());
	}

	@Test
	public void testPropMapAdding() {
		String name = "propName";
		String type = "PropType";

		propMap.addNewProperty(name, type, null);
		Assert.assertEquals(1, propMap.getPropertiesName().size());
		Assert.assertEquals(name, propMap.getPropertiesName().get(0));
		Assert.assertNull(propMap.getPropertyAttributes(name));
		Assert.assertEquals(type, propMap.getPropertyType(name));

	}

	@Test
	public void testPropMapAttr() {
		String name = "propName";
		String type = "PropType";
		List<String> attr = new ArrayList<String>();
		String att1 = "attr1";
		String att2 = "attr2";
		attr.add(att1);
		attr.add(att2);

		propMap.addNewProperty(name, type, attr);
		Assert.assertEquals(attr, propMap.getPropertyAttributes(name));
	}
}
