package org.apache.padaf.xmpbox.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.padaf.xmpbox.XmpConstants;
import org.apache.padaf.xmpbox.xml.XmpParsingException.ErrorType;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public final class DomHelper {

	private DomHelper () {}

	public static Element getUniqueElementChild (Element description) throws XmpParsingException {
		NodeList nl = description.getChildNodes();
		int pos = -1;
		for (int i=0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				if (pos>=0) {
					// invalid : found two child elements
					throw new XmpParsingException(ErrorType.Undefined,"Found two child elements in "+description);
				} else {
					pos = i;
				}
			}
		}
		return (Element)nl.item(pos);
	}

	public static Element getFirstChildElement (Element description) throws XmpParsingException {
		NodeList nl = description.getChildNodes();
		for (int i=0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				return (Element)nl.item(i);
			}
		}
		throw new XmpParsingException(ErrorType.Undefined,"Does not contain element");
	}


	public static List<Element> getElementChildren (Element description) throws XmpParsingException {
		NodeList nl = description.getChildNodes();
		List<Element> ret = new ArrayList<Element>(nl.getLength());
		for (int i=0; i < nl.getLength(); i++) {
			if (nl.item(i) instanceof Element) {
				ret.add((Element)nl.item(i));
			}
		}
		return ret;
	}

	public static QName getQName (Element element) {
		return new QName(element.getNamespaceURI(), element.getLocalName(), element.getPrefix());
	}

	public static boolean isRdfDescription (Element element) {
		return (XmpConstants.DEFAULT_RDF_PREFIX.equals(element.getPrefix()) && XmpConstants.DESCRIPTION_NAME.equals(element.getLocalName()));
	}

	public static boolean isParseTypeResource (Element element) {
		Attr parseType = element.getAttributeNodeNS(XmpConstants.RDF_NAMESPACE, XmpConstants.PARSE_TYPE);
		if (parseType!=null && XmpConstants.RESOURCE_NAME.equals(parseType.getValue())) {
			// parseType resourc
			return true;
		}
		// else
		return false;
	}


}
