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

package org.apache.padaf.xmpbox;

import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.TransformException;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.TextType;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Test XMP MetaData Transformer
 * 
 * @author a183132
 * 
 */
public class XMPMetaDataTest {

	protected XMPMetadata metadata;
	protected Document parent;
	protected XMPSchema tmp, tmp2;

	@Before
	public void init() throws Exception {
		metadata = new XMPMetadata();
		String tmpNsURI = "http://www.test.org/schem/";
		tmp = new XMPSchema(metadata, "test", tmpNsURI);
		tmp.addBagValue("test:BagContainer", "Value1");
		tmp.addBagValue("test:BagContainer", "Value2");
		tmp.addBagValue("test:BagContainer", "Value3");

		tmp.addSequenceValue("test:SeqContainer", "Value1");
		tmp.addSequenceValue("test:SeqContainer", "Value2");
		tmp.addSequenceValue("test:SeqContainer", "Value3");

		tmp
				.addProperty(new TextType(metadata, "test", "simpleProperty",
						"YEP"));

		tmp2 = new XMPSchema(metadata, "space", "http://www.space.org/schem/");
		tmp2.addSequenceValue("test:SeqSpContainer", "ValueSpace1");
		tmp2.addSequenceValue("test:SeqSpContainer", "ValueSpace2");
		tmp2.addSequenceValue("test:SeqSpContainer", "ValueSpace3");

		metadata.addSchema(tmp);

		metadata.addSchema(tmp2);

		// Check schema getting
		Assert.assertEquals(tmp, metadata.getSchema(tmpNsURI));
		Assert.assertNull(metadata.getSchema("THIS URI NOT EXISTS !"));
	}

	@Test
	public void testAddingSchem() {

		List<XMPSchema> vals = metadata.getAllSchemas();
		Assert.assertTrue(vals.contains(tmp));
		Assert.assertTrue(vals.contains(tmp2));
	}

	/*
	 * @Test public void displayResult() throws TransformException {
	 * System.out.println
	 * ("info used:\n XPacketBegin:"+metadata.getXpacketBegin()+
	 * "\n XPacketID:"+metadata.getXpacketId());
	 * SaveMetadataHelper.serialize(metadata, true, System.out);
	 * 
	 * }
	 */

	@Test(expected = org.apache.padaf.xmpbox.TransformException.class)
	public void testTransformerExceptionMessage() throws TransformException {
		throw new TransformException("TEST");
	}

	@Test(expected = org.apache.padaf.xmpbox.TransformException.class)
	public void testTransformerExceptionWithCause() throws TransformException {
		throw new TransformException("TEST", new Throwable());
	}

	@Test
	public void testInitMetaDataWithInfo() throws Exception {
		String xpacketBegin = "TESTBEG", xpacketId = "TESTID", xpacketBytes = "TESTBYTES", xpacketEncoding = "TESTENCOD";
		metadata = new XMPMetadata(xpacketBegin, xpacketId, xpacketBytes,
				xpacketEncoding);
		Assert.assertEquals(xpacketBegin, metadata.getXpacketBegin());
		Assert.assertEquals(xpacketId, metadata.getXpacketId());
		Assert.assertEquals(xpacketBytes, metadata.getXpacketBytes());
		Assert.assertEquals(xpacketEncoding, metadata.getXpacketEncoding());
	}
}
