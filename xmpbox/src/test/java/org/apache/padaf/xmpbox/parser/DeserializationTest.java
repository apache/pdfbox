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

import org.junit.Before;
import org.junit.Test;

public class DeserializationTest {

	protected ByteArrayOutputStream bos;

	@Before
	public void init() {
		bos = new ByteArrayOutputStream();
	}

	@Test
	public void testIsartorStyle() throws Exception {
//
//		InputStream fis = XMPDocumentBuilder.class
//				.getResourceAsStream("isartorStyleXMPOK.xml");
//
//		XMPDocumentBuilder xdb = new XMPDocumentBuilder();
//
//		XMPMetadata metadata = xdb.parse(fis);
//		// <xmpMM:DocumentID>
//		Assert.assertEquals("uuid:09C78666-2F91-3A9C-92AF-3691A6D594F7",
//				metadata.getXMPMediaManagementSchema().getDocumentID());
//
//		// <xmp:CreateDate>
//		// <xmp:ModifyDate>
//		// <xmp:MetadataDate>
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getCreateDate());
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getModifyDate());
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getMetadataDate());
//
//		// PDFA Extension
//		// Check numbers of schema descriptions
//		Assert.assertTrue(metadata.getPDFExtensionSchema()
//				.getDescriptionSchema().size() == 1);
//		SchemaDescription desc = metadata.getPDFExtensionSchema()
//				.getDescriptionSchema().get(0);
//
//		// Check description content
//		Assert.assertEquals("ACME E-Mail Schema", desc.getSchema());
//		Assert.assertEquals("http://www.acme.com/ns/email/1/", desc
//				.getNameSpaceURI());
//		Assert.assertEquals("acmeemail", desc.getPrefix());
//
//		// Check property definition
//		List<PDFAPropertyDescription> properties = desc.getProperties();
//		Assert.assertTrue(properties.size() == 2);
//
//		// Check properties content
//		PDFAPropertyDescription tmpProp = properties.get(0);
//		Assert.assertEquals("Delivery-Date", tmpProp.getNameValue());
//		Assert.assertEquals("Date", tmpProp.getValueTypeValue());
//		Assert.assertEquals("internal", tmpProp.getCategoryValue());
//		Assert.assertEquals("date of email delivery", tmpProp
//				.getDescriptionValue());
//
//		tmpProp = properties.get(1);
//		Assert.assertEquals("From", tmpProp.getNameValue());
//		Assert.assertEquals("mailaddress", tmpProp.getValueTypeValue());
//		Assert.assertEquals("internal", tmpProp.getCategoryValue());
//		Assert.assertEquals("sender email address", tmpProp
//				.getDescriptionValue());
//
//		// Check valuetype
//		// Check numbers of valuetype defined
//		Assert.assertTrue(desc.getValueTypes().size() == 1);
//		PDFAValueTypeDescription tmpValType = desc.getValueTypes().get(0);
//		Assert.assertEquals("mailaddress", tmpValType.getTypeNameValue());
//		Assert.assertEquals("http://www.acme.com/ns/email/1/mailaddress/",
//				tmpValType.getNamespaceURIValue());
//		Assert.assertEquals("mailaddress value", tmpValType
//				.getDescriptionValue());
//
//		// Check fields associated to this value type
//		Assert.assertTrue(tmpValType.getFields().size() == 2);
//
//		PDFAFieldDescription tmpField = tmpValType.getFields().get(0);
//		Assert.assertEquals("name", tmpField.getNameValue());
//		Assert.assertEquals("Text", tmpField.getValueTypeValue());
//		Assert.assertEquals("plaintext name", tmpField.getDescriptionValue());
//
//		tmpField = tmpValType.getFields().get(1);
//		Assert.assertEquals("mailto", tmpField.getNameValue());
//		Assert.assertEquals("Text", tmpField.getValueTypeValue());
//		Assert.assertEquals("email address", tmpField.getDescriptionValue());
//
//		// Check PDFA Conformance schema
//		Assert.assertEquals("1", metadata.getPDFIdentificationSchema()
//				.getPartProperty().getStringValue());
//		Assert.assertEquals("B", metadata.getPDFIdentificationSchema()
//				.getConformance());
//		Assert.assertEquals("1:2005", metadata.getPDFIdentificationSchema()
//				.getAmdProperty().getStringValue());
//
//		// Check ADOBE PDF Schema
//		Assert.assertEquals("PDFlib Personalization Server 7.0.2p5 (Win32)",
//				metadata.getAdobePDFSchema().getProducer());
//
//		// Check Defined Schema
//		XMPSchema schem = metadata.getSchema("http://www.acme.com/ns/email/1/");
//		Assert.assertEquals(DateConverter
//				.toCalendar("2007-11-09T09:55:36+01:00"), schem
//				.getDatePropertyValue("acmeemail:Delivery-Date"));
//		Assert.assertNotNull(schem.getAbstractProperty(("acmeemail:From")));
//
//		// SaveMetadataHelper.serialize(metadata, true, System.out);
//
	}

	
	@Test
	public void testStructuredRecursive () throws Exception {
		InputStream fis = XMPDocumentBuilder.class
				.getResourceAsStream("structured_recursive.xml");

		XMPDocumentBuilder xdb = new XMPDocumentBuilder();

		xdb.parse(fis);
		
	}
	
	@Test
	public void testAltBagSeq() throws Exception {
		InputStream fis = XMPDocumentBuilder.class
				.getResourceAsStream("AltBagSeqTest.xml");

		XMPDocumentBuilder xdb = new XMPDocumentBuilder();

		xdb.parse(fis);
		// XMPMetadata metadata=xdb.parse(fis);
		// SaveMetadataHelper.serialize(metadata, true, System.out);
	}

	@Test
	public void testIsartorStyleWithThumbs() throws Exception {
//
//		InputStream fis = XMPDocumentBuilder.class
//				.getResourceAsStream("ThumbisartorStyle.xml");
//
//		XMPDocumentBuilder xdb = new XMPDocumentBuilder();
//
//		XMPMetadata metadata = xdb.parse(fis);
//
//		// <xmpMM:DocumentID>
//		Assert.assertEquals("uuid:09C78666-2F91-3A9C-92AF-3691A6D594F7",
//				metadata.getXMPMediaManagementSchema().getDocumentID());
//
//		// <xmp:CreateDate>
//		// <xmp:ModifyDate>
//		// <xmp:MetadataDate>
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getCreateDate());
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getModifyDate());
//		Assert.assertEquals(DateConverter
//				.toCalendar("2008-01-18T16:59:54+01:00"), metadata
//				.getXMPBasicSchema().getMetadataDate());
//
//		// THUMBNAILS TEST
//		List<ThumbnailType> thumbs = metadata.getXMPBasicSchema()
//				.getThumbnailsProperty();
//		Assert.assertNotNull(thumbs);
//		Assert.assertEquals(2, thumbs.size());
//
//		ThumbnailType thumb = thumbs.get(0);
//		Assert.assertEquals(new Integer(162), thumb.getHeight());
//		Assert.assertEquals(new Integer(216), thumb.getWidth());
//		Assert.assertEquals("JPEG", thumb.getFormat());
//		Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());
//
//		thumb = thumbs.get(1);
//		Assert.assertEquals(new Integer(162), thumb.getHeight());
//		Assert.assertEquals(new Integer(216), thumb.getWidth());
//		Assert.assertEquals("JPEG", thumb.getFormat());
//		Assert.assertEquals("/9j/4AAQSkZJRgABAgEASABIAAD", thumb.getImage());
//
//		// PDFA Extension
//		// Check numbers of schema descriptions
//		Assert.assertTrue(metadata.getPDFExtensionSchema()
//				.getDescriptionSchema().size() == 1);
//		SchemaDescription desc = metadata.getPDFExtensionSchema()
//				.getDescriptionSchema().get(0);
//
//		// Check description content
//		Assert.assertEquals("ACME E-Mail Schema", desc.getSchema());
//		Assert.assertEquals("http://www.acme.com/ns/email/1/", desc
//				.getNameSpaceURI());
//		Assert.assertEquals("acmeemail", desc.getPrefix());
//
//		// Check property definition
//		List<PDFAPropertyDescription> properties = desc.getProperties();
//		Assert.assertTrue(properties.size() == 2);
//
//		// Check properties content
//		PDFAPropertyDescription tmpProp = properties.get(0);
//		Assert.assertEquals("Delivery-Date", tmpProp.getNameValue());
//		Assert.assertEquals("Date", tmpProp.getValueTypeValue());
//		Assert.assertEquals("internal", tmpProp.getCategoryValue());
//		Assert.assertEquals("date of email delivery", tmpProp
//				.getDescriptionValue());
//
//		tmpProp = properties.get(1);
//		Assert.assertEquals("From", tmpProp.getNameValue());
//		Assert.assertEquals("mailaddress", tmpProp.getValueTypeValue());
//		Assert.assertEquals("internal", tmpProp.getCategoryValue());
//		Assert.assertEquals("sender email address", tmpProp
//				.getDescriptionValue());
//
//		// Check valuetype
//		// Check numbers of valuetype defined
//		Assert.assertTrue(desc.getValueTypes().size() == 1);
//		PDFAValueTypeDescription tmpValType = desc.getValueTypes().get(0);
//		Assert.assertEquals("mailaddress", tmpValType.getTypeNameValue());
//		Assert.assertEquals("http://www.acme.com/ns/email/1/mailaddress/",
//				tmpValType.getNamespaceURIValue());
//		Assert.assertEquals("mailaddress value", tmpValType
//				.getDescriptionValue());
//
//		// Check fields associated to this value type
//		Assert.assertTrue(tmpValType.getFields().size() == 2);
//
//		PDFAFieldDescription tmpField = tmpValType.getFields().get(0);
//		Assert.assertEquals("name", tmpField.getNameValue());
//		Assert.assertEquals("Text", tmpField.getValueTypeValue());
//		Assert.assertEquals("plaintext name", tmpField.getDescriptionValue());
//
//		tmpField = tmpValType.getFields().get(1);
//		Assert.assertEquals("mailto", tmpField.getNameValue());
//		Assert.assertEquals("Text", tmpField.getValueTypeValue());
//		Assert.assertEquals("email address", tmpField.getDescriptionValue());
//
//		// Check PDFA Conformance schema
//		Assert.assertEquals("1", metadata.getPDFIdentificationSchema()
//				.getPartProperty().getStringValue());
//		Assert.assertEquals("B", metadata.getPDFIdentificationSchema()
//				.getConformance());
//		Assert.assertEquals("1:2005", metadata.getPDFIdentificationSchema()
//				.getAmd());
//
//		// Check ADOBE PDF Schema
//		Assert.assertEquals("PDFlib Personalization Server 7.0.2p5 (Win32)",
//				metadata.getAdobePDFSchema().getProducer());
//
//		// Check Defined Schema
//		XMPSchema schem = metadata.getSchema("http://www.acme.com/ns/email/1/");
//		Assert.assertEquals(DateConverter
//				.toCalendar("2007-11-09T09:55:36+01:00"), schem
//				.getDatePropertyValue("acmeemail:Delivery-Date"));
//		Assert.assertNotNull(schem.getAbstractProperty(("acmeemail:From")));
//
//		// SaveMetadataHelper.serialize(metadata, true, System.out);
//
	}

}
