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
		metadata = new XMPMetadata();
		schema = metadata.createAndAddXMPBasicSchema();
		schemaClass = XMPBasicSchema.class;
	}

	@Parameters
	public static Collection<Object[]> initializeParameters() throws Exception {
		List<Object[]> data = new ArrayList<Object[]>();

		data.add(wrapProperty("Advisory", "bag Xpath", new String[] { "xpath1",
				"xpath2" }));
		data.add(wrapProperty("BaseURL", "URL", "URL"));
		data.add(wrapProperty("CreateDate", "Date", Calendar.getInstance()));
		data.add(wrapProperty("CreatorTool", "Text", "CreatorTool"));
		data.add(wrapProperty("Identifier", "bag Text", new String[] { "id1",
				"id2" }));
		data.add(wrapProperty("Label", "Text", "label"));
		data.add(wrapProperty("MetadataDate", "Date", Calendar.getInstance()));
		data.add(wrapProperty("ModifyDate", "Date", Calendar.getInstance()));
		data.add(wrapProperty("Nickname", "Text", "nick name"));
		data.add(wrapProperty("Rating", "Integer", 7));

		// TODO test Thumbnail when implemented in the XMPBasicSchema
		data.add(wrapProperty("Thumbnails", "Alt Thumbnail", null));

		return data;
	}

	// @Test
	// public void testPDFExt() throws TransformException{
	// XMPBasicSchema schem=metadata.createAndAddXMPBasicSchema();
	//
	// String xpath1="xpath1";
	// String xpath2="xpath2";
	// String url="URL";
	// Calendar createDate=Calendar.getInstance();
	// String creatorTool="CreatorTool";
	// String identifier1="id1";
	// String identifier2="id2";
	// String label="label";
	// Calendar metaDataDate=Calendar.getInstance();
	// Calendar modifyDate=Calendar.getInstance();
	// String nickname="nickname";
	// int rate=7;
	//
	// schem.addAdvisoryValue(xpath1);
	// schem.addAdvisoryValue(xpath2);
	// schem.setBaseURLValue(url);
	// schem.setCreateDateValue(createDate);
	// schem.setCreatorToolValue(creatorTool);
	// schem.addIdentifierValue(identifier1);
	// schem.addIdentifierValue(identifier2);
	// schem.setLabelValue(label);
	// schem.setMetadataDateValue(metaDataDate);
	// schem.setModifyDateValue(modifyDate);
	// schem.setNicknameValue(nickname);
	// schem.setRatingValue(rate);
	//
	// //check retrieve this schema in metadata
	// Assert.assertEquals(schem, metadata.getXMPBasicSchema());
	//
	// //check values embedded in this schema
	// Assert.assertEquals("xmp:Advisory",
	// schem.getAdvisory().getQualifiedName());
	// Assert.assertTrue(schem.getAdvisoryValues().contains(xpath1));
	// Assert.assertTrue(schem.getAdvisoryValues().contains(xpath2));
	//
	// Assert.assertEquals("xmp:BaseURL",
	// schem.getBaseURL().getQualifiedName());
	// Assert.assertEquals(url, schem.getBaseURLValue());
	//
	// Assert.assertEquals("xmp:CreateDate",
	// schem.getCreateDate().getQualifiedName());
	// Assert.assertEquals(createDate, schem.getCreateDateValue());
	//
	// Assert.assertEquals("xmp:CreatorTool",
	// schem.getCreatorTool().getQualifiedName());
	// Assert.assertEquals(creatorTool, schem.getCreatorToolValue());
	//
	// Assert.assertEquals("xmp:Identifier",
	// schem.getIdentifier().getQualifiedName());
	// Assert.assertTrue(schem.getIdentifierValues().contains(identifier1));
	// Assert.assertTrue(schem.getIdentifierValues().contains(identifier2));
	//
	// Assert.assertEquals("xmp:Label", schem.getLabel().getQualifiedName());
	// Assert.assertEquals(label, schem.getLabelValue());
	//
	// Assert.assertEquals("xmp:MetadataDate",
	// schem.getMetadataDate().getQualifiedName());
	// Assert.assertEquals(metaDataDate, schem.getMetadataDateValue());
	//
	// Assert.assertEquals("xmp:ModifyDate",
	// schem.getModifyDate().getQualifiedName());
	// Assert.assertEquals(modifyDate, schem.getModifyDateValue());
	//
	// Assert.assertEquals("xmp:Nickname",
	// schem.getNickname().getQualifiedName());
	// Assert.assertEquals(nickname, schem.getNicknameValue());
	//
	// Assert.assertEquals("xmp:Rating", schem.getRating().getQualifiedName());
	// Assert.assertEquals(rate, schem.getRatingValue());
	//
	// //SaveMetadataHelper.serialize(metadata, true, System.out);
	//
	// //TODO test Thumbnail when implemented in the XMPBasicSchema
	// }

}
