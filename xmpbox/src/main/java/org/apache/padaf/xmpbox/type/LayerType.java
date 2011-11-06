package org.apache.padaf.xmpbox.type;

import org.apache.padaf.xmpbox.XMPMetadata;

public class LayerType extends ComplexPropertyContainer {
	protected XMPMetadata metadata;
	
	public LayerType(XMPMetadata metadata, String namespaceURI, String prefix, String propertyName) {
		super(metadata, namespaceURI, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}
	
	public LayerType(XMPMetadata metadata, String prefix, String propertyName) {
		super(metadata, prefix, propertyName);
		this.metadata = metadata;
		setAttribute(new Attribute(null, "rdf", "parseType", "Resource"));
	}
	
	/**
	 * Get The LayerName data
	 * 
	 * @return the LayerName
	 */
	public String getLayerName() {
		AbstractField absProp = getFirstEquivalentProperty("LayerName",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set LayerName 
	 * 
	 * @param prefix
	 *            the prefix of LayerName property to set
	 * @param name
	 *            the name of LayerName property to set
	 * @param image
	 *            the value of LayerName property to set
	 */
	public void setLayerName(String prefix, String name, String image) {
		this.addProperty(new TextType(metadata, prefix, name, image));
	}
	
	/**
	 * Get The LayerText data
	 * 
	 * @return the LayerText
	 */
	public String getLayerText() {
		AbstractField absProp = getFirstEquivalentProperty("LayerText",
				TextType.class);
		if (absProp != null) {
			return ((TextType) absProp).getStringValue();
		}
		return null;
	}

	/**
	 * Set LayerText 
	 * 
	 * @param prefix
	 *            the prefix of LayerText property to set
	 * @param name
	 *            the name of LayerText property to set
	 * @param image
	 *            the value of LayerText property to set
	 */
	public void setLayerText(String prefix, String name, String image) {
		this.addProperty(new TextType(metadata, prefix, name, image));
	}

}
