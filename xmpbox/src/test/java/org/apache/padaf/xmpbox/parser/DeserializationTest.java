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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.padaf.xmpbox.DateConverter;
import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.ThumbnailType;
import org.apache.padaf.xmpbox.xml.DomXmpParser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DeserializationTest {

	protected ByteArrayOutputStream bos;

	@Before
	public void init() {
		bos = new ByteArrayOutputStream();
	}


	
	@Test
	public void testStructuredRecursive () throws Exception {
		InputStream fis = DomXmpParser.class
				.getResourceAsStream("/org/apache/padaf/xmpbox/parser/structured_recursive.xml");

		DomXmpParser xdb = new DomXmpParser();

		xdb.parse(fis);
		
	}

	@Test
	public void testEmptyLi () throws Exception {
		InputStream fis = DomXmpParser.class
				.getResourceAsStream("/org/apache/padaf/xmpbox/parser/empty_list.xml");

		DomXmpParser xdb = new DomXmpParser();

		xdb.parse(fis);
		
	}

	
	@Test
	public void testAltBagSeq() throws Exception {
		InputStream fis = DomXmpParser.class
				.getResourceAsStream("/org/apache/padaf/xmpbox/parser/AltBagSeqTest.xml");

		DomXmpParser xdb = new DomXmpParser();

		xdb.parse(fis);
		// XMPMetadata metadata=xdb.parse(fis);
		// SaveMetadataHelper.serialize(metadata, true, System.out);
	}

	@Test
	public void testIsartorStyleWithThumbs() throws Exception {

		InputStream fis = DomXmpParser.class
				.getResourceAsStream("/org/apache/padaf/xmpbox/parser/ThumbisartorStyle.xml");


		DomXmpParser xdb = new DomXmpParser();

		XMPMetadata metadata = xdb.parse(fis);

		// <xmpMM:DocumentID>
		Assert.assertEquals("uuid:09C78666-2F91-3A9C-92AF-3691A6D594F7",
				metadata.getXMPMediaManagementSchema().getDocumentID());

		// <xmp:CreateDate>
		// <xmp:ModifyDate>
		// <xmp:MetadataDate>
		Assert.assertEquals(DateConverter
				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
				.getXMPBasicSchema().getCreateDate());
		Assert.assertEquals(DateConverter
				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
				.getXMPBasicSchema().getModifyDate());
		Assert.assertEquals(DateConverter
				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
				.getXMPBasicSchema().getMetadataDate());

		// THUMBNAILS TEST
		List<ThumbnailType> thumbs = metadata.getXMPBasicSchema()
				.getThumbnailsProperty();
		Assert.assertNotNull(thumbs);
		Assert.assertEquals(2, thumbs.size());

		ThumbnailType thumb = thumbs.get(0);
		Assert.assertEquals(new Integer(162), thumb.getHeight());
		Assert.assertEquals(new Integer(216), thumb.getWidth());
		Assert.assertEquals("JPEG", thumb.getFormat());
		Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());

		thumb = thumbs.get(1);
		Assert.assertEquals(new Integer(162), thumb.getHeight());
		Assert.assertEquals(new Integer(216), thumb.getWidth());
		Assert.assertEquals("JPEG", thumb.getFormat());
		Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());


	}

}
