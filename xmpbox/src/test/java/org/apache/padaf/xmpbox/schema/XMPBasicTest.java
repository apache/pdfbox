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
import java.util.Calendar;
import java.util.Collection;
import java.util.List;


import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.parser.XMPDocumentBuilder;
import org.apache.padaf.xmpbox.schema.XMPBasicSchema;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class XMPBasicTest extends AbstractXMPSchemaTest {

	public XMPBasicTest(String prop, String type, Object val) {
		super(prop, type, val);
	}

	@Before
	public void initTempMetaData() throws Exception {
		metadata = new XMPDocumentBuilder().createXMPMetadata();
		schema = metadata.createAndAddXMPBasicSchema();
		schemaClass = XMPBasicSchema.class;
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
		List<Object[]> data = new ArrayList<Object[]>();

		data.add(wrapProperty("Advisory", "bag Xpath", new String[] { "xpath1",
				"xpath2" }));
//		data.add(wrapProperty("BaseURL", "URL", "URL"));
		data.add(wrapProperty("CreateDate", "Date", Calendar.getInstance()));
//		data.add(wrapProperty("CreatorTool", "Text", "CreatorTool"));
		data.add(wrapProperty("Identifier", "bag Text", new String[] { "id1",
				"id2" }));
		data.add(wrapProperty("Label", "Text", "label"));
		data.add(wrapProperty("MetadataDate", "Date", Calendar.getInstance()));
		data.add(wrapProperty("ModifyDate", "Date", Calendar.getInstance()));
		data.add(wrapProperty("Nickname", "Text", "nick name"));
		data.add(wrapProperty("Rating", "Integer", 7));

		// TODO TEST test Thumbnail when implemented in the XMPBasicSchema
		data.add(wrapProperty("Thumbnails", "Alt Thumbnail", null));

		return data;
	}


}
