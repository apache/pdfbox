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
import org.apache.padaf.xmpbox.schema.XMPMediaManagementSchema;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class XMPMediaManagementTest extends AbstractXMPSchemaTest {

	@Before
	public void initTempMetaData() throws Exception {
		metadata = new XMPMetadata();
		schema = metadata.createAndAddXMPMediaManagementSchema();
		schemaClass = XMPMediaManagementSchema.class;
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
		List<Object[]> data = new ArrayList<Object[]>();
		data.add(wrapProperty("DocumentID", "Text",
				"uuid:FB031973-5E75-11B2-8F06-E7F5C101C07A"));
		data.add(wrapProperty("ResourceRef", "Text", "uuid:14"));
		data.add(wrapProperty("Manager", "Text", "Raoul"));
		data.add(wrapProperty("ManageTo", "Text", "uuid:36"));
		data.add(wrapProperty("ManageUI", "Text", "uuid:3635"));
		data.add(wrapProperty("ManageFrom", "Text", "uuid:36"));
		data.add(wrapProperty("InstanceID", "Text", "uuid:42"));
		data.add(wrapProperty("OriginalDocumentID", "Text", "uuid:142"));
		data.add(wrapProperty("RenditionClass", "Text", "myclass"));
		data.add(wrapProperty("RenditionParams", "Text", "my params"));
		data.add(wrapProperty("VersionID", "Text", "14"));
		data.add(wrapProperty("Versions", "seq Text", new String[] { "1", "2",
				"3" }));
		data.add(wrapProperty("History", "seq Text", new String[] { "action 1",
				"action 2", "action 3" }));
		data.add(wrapProperty("Ingredients", "bag Text", new String[] {
				"resource1", "resource2" }));
		return data;
	}

	public XMPMediaManagementTest(String property, String type, Object value) {
		super(property, type, value);
	}

}
