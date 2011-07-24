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

import java.io.IOException;
import java.io.OutputStream;

import javax.xml.transform.TransformerException;


import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.padaf.xmpbox.parser.XMLUtil;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;

/**
 * This class represents tools to save metadata and schema in serialized way
 * (RDF/XML Format)
 * 
 * @author a183132
 * 
 */
public class SaveMetadataHelper {

	/**
	 * According to check-style, Utility classes should not have a public or
	 * default constructor.
	 */
	protected SaveMetadataHelper() {
	};

	/**
	 * Prepare XMP Saving Put data necessary to make a well-formed XMP
	 * 
	 * @param metadata
	 *            metadata concerned by the serialization processing
	 * @param intoXPacket
	 *            true if Processing instruction must be embedded
	 * @return The DOM Document which will represent the serialized metadata
	 */
	protected static Document prepareSaving(XMPMetadata metadata,
			boolean intoXPacket) {
		Document newdoc = (Document) metadata.getFuturOwner().cloneNode(true);
		if (intoXPacket) {
			ProcessingInstruction beginXPacket = newdoc
					.createProcessingInstruction("xpacket", "begin=\""
							+ metadata.getXpacketBegin() + "\" id=\""
							+ metadata.getXpacketId() + "\"");
			newdoc.appendChild(beginXPacket);
		}

		Element xmpMeta = newdoc.createElementNS("adobe:ns:meta/", "x:xmpmeta");
		xmpMeta.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:x",
				"adobe:ns:meta/");

		newdoc.appendChild(xmpMeta);
		Element elem = (Element) metadata.getContainerElement().cloneNode(true);
		newdoc.adoptNode(elem);
		xmpMeta.appendChild(elem);

		if (intoXPacket) {
			ProcessingInstruction endXPacket = newdoc
					.createProcessingInstruction("xpacket", metadata
							.getEndXPacket());
			newdoc.appendChild(endXPacket);
		}
		return newdoc;
	}

	/**
	 * Serialize metadata into an output stream with XPacket PI
	 * 
	 * @param metadata
	 *            Metadata concerned by the serialization processing
	 * @param os
	 *            Stream to save serialized metadata
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static void serialize(XMPMetadata metadata, OutputStream os)
			throws TransformException {
		serialize(metadata, true, os);

	}

	/**
	 * Serialize metadata into an output stream
	 * 
	 * @param metadata
	 *            Metadata concerned by the serialization processing
	 * @param intoXPacket
	 *            True to put XPacket Processing Information
	 * @param os
	 *            Stream to save serialized metadata
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static void serialize(XMPMetadata metadata, boolean intoXPacket,
			OutputStream os) throws TransformException {
		Document doc = prepareSaving(metadata, intoXPacket);
		try {
			XMLUtil.save(doc, os, "UTF-8");
		} catch (TransformerException e) {
			throw new TransformException("Failed to parse defined XMP");
		}

	}

	/**
	 * Serialize metadata into the byte array returned
	 * 
	 * @param metadata
	 *            Metadata concerned by the serialization processing
	 * @param intoXPacket
	 *            True to put XPacket Processing Information
	 * @return ByteArray which contains serialized metadata
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static byte[] serialize(XMPMetadata metadata, boolean intoXPacket)
			throws TransformException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serialize(metadata, intoXPacket, bos);
		IOUtils.closeQuietly(bos);
		return bos.toByteArray();
	}

	/**
	 * Serialize metadata with XPacket PI into the byte array returned
	 * 
	 * @param metadata
	 *            Metadata concerned by the serialization processing
	 * @return ByteArray which contains serialized metadata
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static byte[] serialize(XMPMetadata metadata)
			throws TransformException {
		return serialize(metadata, true);
	}

	/**
	 * Serialize a schema into an Output stream
	 * 
	 * @param schema
	 *            Schema concerned by the serialization processing
	 * @param os
	 *            Stream to save serialized schema
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static void serialize(XMPSchema schema, OutputStream os)
			throws TransformException {
		try {
			Document doc = XMLUtil.newDocument();
			Element rdf = doc.createElementNS(
					"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf:RDF");
			Node schemContent = schema.getElement().cloneNode(true);
			doc.adoptNode(schemContent);
			rdf.appendChild(schemContent);
			XMLUtil.save(rdf, os, "UTF-8");
		} catch (TransformerException e) {
			throw new TransformException("Failed to parse defined XMP");
		} catch (IOException e) {
			throw new TransformException(
					"Failed to create Document to contain Schema representation ",
					e.getCause());
		}

	}

	/**
	 * Serialize a schema into a Byte Array
	 * 
	 * @param schema
	 *            Schema concerned by the serialization processing
	 * @return a ByteArray which contains serialized schema
	 * @throws TransformException
	 *             When couldn't parse data to XML/RDF
	 */
	public static byte[] serialize(XMPSchema schema) throws TransformException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		serialize(schema, bos);
		IOUtils.closeQuietly(bos);
		return bos.toByteArray();
	}

}
