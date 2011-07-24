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
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.XMPRightsManagementSchema;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class XmpRightsSchemaTest extends AbstractXMPSchemaTest {

	public XmpRightsSchemaTest(String property, String type, Object value) {
		super(property, type, value);
	}

	@Before
	public void initTempMetaData() throws Exception {
		metadata = new XMPMetadata();
		schema = metadata.createAndAddXMPRightsManagementSchema();
		schemaClass = XMPRightsManagementSchema.class;
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(wrapProperty("Certificate", "URL",
				"http://une.url.vers.un.certificat/moncert.cer"));
		data.add(wrapProperty("Marked", "Boolean", true));
		data.add(wrapProperty("Owner", "bag ProperName",
				new String[] { "OwnerName" }));

		Map<String, String> desc = new HashMap<String, String>(2);
		desc.put("fr", "Termes d'utilisation");
		desc.put("en", "Usage Terms");
		data.add(wrapProperty("UsageTerms", "Lang Alt", desc));
		data.add(wrapProperty("WebStatement", "URL",
				"http://une.url.vers.une.page.fr/"));
		return data;
	}


}
