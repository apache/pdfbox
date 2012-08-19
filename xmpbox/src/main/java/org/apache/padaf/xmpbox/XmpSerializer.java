 /*****************************************************************************
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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.padaf.xmpbox.parser.XMLUtil;
import org.apache.padaf.xmpbox.schema.XMPSchema;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.AbstractSimpleProperty;
import org.apache.padaf.xmpbox.type.AbstractStructuredType;
import org.apache.padaf.xmpbox.type.ArrayProperty;
import org.apache.padaf.xmpbox.type.Attribute;
import org.apache.padaf.xmpbox.type.ComplexPropertyContainer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ProcessingInstruction;

public class XmpSerializer {

	private DocumentBuilder documentBuilder = null;
	
	public XmpSerializer () throws TransformException {
		// xml init
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        try {
			documentBuilder = builderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new TransformException("Failed to init XmpSerializer", e);
		}
		
	}
	
	
	public void serialize(XMPMetadata metadata, OutputStream os, boolean withXpacket) throws TransformException {
		// TODO rewrite serialization
		try {
			Document doc = documentBuilder.newDocument();
			// fill document
			Element rdf = createRdfElement(doc,metadata, withXpacket);
			for (XMPSchema schema : metadata.getAllSchemas()) {
				rdf.appendChild(createSchemaElement(doc, schema));
			}
			// save
			XMLUtil.save(doc, os, "UTF-8");
		} catch (Exception e) {
			// TODO supprimer
			throw new TransformException(
					"Failed to create Document to contain Schema representation ",
					e);
		}

	}

	protected Element createSchemaElement (Document doc, XMPSchema schema) {
		// prepare schema
		Element selem = doc.createElement("rdf:Description");
		selem.setAttribute("rdf:about", schema.getAboutValue()); 
		selem.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:"+schema.getPrefix(), schema.getNamespaceValue());
		// the other attributes
//		List<Attribute> attributes = schema.getAllAttributes();
		fillElementWithAttributes(selem, schema.getAllAttributes());
//		for (Attribute attribute : attributes) {
//			selem.setAttribute(attribute.getQualifiedName(), attribute.getValue());
//		}
//		selem.setAttribute("rdf:about", schema.getClass().getName()); // TODO remove already done 6 lines before
		// the content
		List<AbstractField> fields = schema.getAllProperties();
		xxxxxxx(doc, selem, fields);
		// return created schema
		return selem;
	}

	public void xxxxxxx (Document doc, Element parent, List<AbstractField> fields) {
		for (AbstractField field : fields) {
			if (field instanceof AbstractSimpleProperty) {
				AbstractSimpleProperty simple = (AbstractSimpleProperty)field;
				Element esimple = doc.createElement(simple.getPrefix()+":"+simple.getPropertyName());
				esimple.setTextContent(simple.getStringValue());
				parent.appendChild(esimple);
			} else if (field instanceof ArrayProperty) {
				ArrayProperty array = (ArrayProperty)field;
				Element asimple = doc.createElement(array.getPrefix()+":"+array.getPropertyName());
				parent.appendChild(asimple);
				// attributes
				fillElementWithAttributes(asimple, array.getAllAttributes());
				// the array definition
				Element econtainer = doc.createElement("rdf"+":"+array.getArrayType()); 
				asimple.appendChild(econtainer);
				// for each element of the array
				List<AbstractField> innerFields = array.getAllProperties();
				xxxxxxx(doc, econtainer, innerFields);
			} else if (field instanceof AbstractStructuredType) {
				AbstractStructuredType structured = (AbstractStructuredType)field;
				// element li
				Element estructured = doc.createElement(structured.getPrefix()+":"+structured.getPropertyName());
				parent.appendChild(estructured);
				// element description
				Element econtainer = doc.createElement("rdf"+":"+"Description");
				estructured.appendChild(econtainer);
				// all properties
				List<AbstractField> innerFields = structured.getAllProperties();
				xxxxxxx(doc, econtainer, innerFields);
			} else {
				System.err.println(">> TODO >> "+field.getClass());
			}
		}
		
	}
	
	protected void fillElementWithAttributes (Element target, List<Attribute> attributes) {
		for (Attribute attribute : attributes) {
			target.setAttribute(attribute.getQualifiedName(), attribute.getValue());
		}
	}
	
	protected Element createRdfElement (Document doc, XMPMetadata metadata, boolean withXpacket) {
		// starting xpacket
		if (withXpacket) {
			ProcessingInstruction beginXPacket = doc
					.createProcessingInstruction("xpacket", "begin=\""
							+ metadata.getXpacketBegin() + "\" id=\""
							+ metadata.getXpacketId() + "\"");
			doc.appendChild(beginXPacket);
		}
		// meta element
		Element xmpmeta = doc.createElementNS("adobe:ns:meta/", "x:xmpmeta");
		xmpmeta.setAttributeNS(XMPSchema.NS_NAMESPACE, "xmlns:x","adobe:ns:meta/");
		doc.appendChild(xmpmeta);
		// ending xpacket
		if (withXpacket) {
			ProcessingInstruction endXPacket = doc
					.createProcessingInstruction("xpacket", metadata
							.getEndXPacket());
			doc.appendChild(endXPacket);
		}
		// rdf element
		Element rdf = doc.createElementNS(XmpConstants.RDF_NAMESPACE, "rdf:RDF");
//		rdf.setAttributeNS(XMPSchema.NS_NAMESPACE, qualifiedName, value)
		xmpmeta.appendChild(rdf);
		// return the rdf element where all will be put
		return rdf;
	}
  
	
}
