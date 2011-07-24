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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.schema.DublinCoreSchema;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.junit.Before;
import org.junit.Test;

/**
 * Test with 2 dublinCore with different prefix (Test comportment of
 * XMPMetadata)
 * 
 * @author a183132
 * 
 */
public class DoubleSameTypeSchemaTest {

	protected XMPMetadata metadata;

	@Before
	public void testInit() throws Exception {
		metadata = new XMPMetadata();
	}

	@Test
	public void testDoubleDublinCore() throws Exception {
		DublinCoreSchema dc1 = metadata.createAndAddDublinCoreSchema();
		String ownPrefix = "test";
		DublinCoreSchema dc2 = new DublinCoreSchema(metadata, ownPrefix);
		metadata.addSchema(dc2);

		List<String> creators = new ArrayList<String>();
		creators.add("creator1");
		creators.add("creator2");

		String format = "application/pdf";
		dc1.setFormatValue(format);
		dc1.addToCreatorValue(creators.get(0));
		dc1.addToCreatorValue(creators.get(1));

		String coverage = "Coverage";
		dc2.setCoverageValue(coverage);
		dc2.addToCreatorValue(creators.get(0));
		dc2.addToCreatorValue(creators.get(1));

		// We can't use metadata.getDublinCoreSchema() due to specification of
		// XMPBox (see Javadoc of XMPMetadata)
		Assert.assertEquals(format, ((DublinCoreSchema) metadata.getSchema(
				DublinCoreSchema.PREFERRED_DC_PREFIX, DublinCoreSchema.DCURI))
				.getFormatValue());
		Assert.assertEquals(coverage, ((DublinCoreSchema) metadata.getSchema(
				ownPrefix, DublinCoreSchema.DCURI)).getCoverageValue());

		List<XMPSchema> schems = metadata.getAllSchemas();
		DublinCoreSchema dc;
		for (XMPSchema xmpSchema : schems) {
			dc = (DublinCoreSchema) xmpSchema;
			Assert.assertTrue(dc.getCreatorValue().containsAll(creators));
		}

		// SaveMetadataHelper.serialize(metadata, true, System.out);

	}
}
