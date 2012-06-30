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
import java.util.List;

import org.apache.padaf.xmpbox.XMPMetadata;
import org.apache.padaf.xmpbox.type.AbstractField;
import org.apache.padaf.xmpbox.type.BadFieldValueException;
import org.apache.padaf.xmpbox.type.ComplexProperty;
import org.apache.padaf.xmpbox.type.IntegerType;
import org.apache.padaf.xmpbox.type.LayerType;
import org.apache.padaf.xmpbox.type.TextType;

public class PhotoshopSchema extends XMPSchema {

	public PhotoshopSchema(XMPMetadata metadata) {
		super(metadata, PREFERRED_PHOTISHOP_PREFIX, PHOTOSHOPURI);
	}

	public PhotoshopSchema(XMPMetadata metadata, String ownPrefix) {
		super(metadata, ownPrefix, PHOTOSHOPURI);
	}
				
	public static final String PREFERRED_PHOTISHOP_PREFIX = "photoshop";

	public static final String PHOTOSHOPURI = "http://ns.adobe.com/photoshop/1.0/";

	@PropertyType(propertyType = "URI")
	public static final String ANCESTOR = "AncestorID";

	@PropertyType(propertyType = "Text")
	public static final String AUTHORS_POSITION = "AuthorsPosition";

	@PropertyType(propertyType = "Text")
	public static final String CAPTION_WRITER = "CaptionWriter";
	
	@PropertyType(propertyType = "Text")
	public static final String CATEGORY = "Category";

	@PropertyType(propertyType = "Text")
	public static final String CITY = "City";

	@PropertyType(propertyType = "Integer")
	public static final String COLOR_MODE = "ColorMode";

	@PropertyType(propertyType = "Text")
	public static final String COUNTRY = "Country";

	@PropertyType(propertyType = "Text")
	public static final String CREDIT = "Credit";

	@PropertyType(propertyType = "Text")
	public static final String DATE_CREATED = "DateCreated";

	@PropertyType(propertyType = "bag Text")
	public static final String DOCUMENT_ANCESTORS = "DocumentAncestors";
	
	@PropertyType(propertyType = "Text")
	public static final String HEADLINE = "Headline";

	@PropertyType(propertyType = "Text")
	public static final String HISTORY = "History";

	@PropertyType(propertyType = "Text")
	public static final String ICC_PROFILE = "ICCProfile";

	@PropertyType(propertyType = "Text")
	public static final String INSTRUCTIONS = "Instructions";

	@PropertyType(propertyType = "Text")
	public static final String SOURCE = "Source";

	@PropertyType(propertyType = "Text")
	public static final String STATE = "State";

	@PropertyType(propertyType = "bag Text")
	public static final String SUPPLEMENTAL_CATEGORIES = "SupplementalCategories";

	@PropertyType(propertyType = "seq Layer")
	public static final String TEXT_LAYERS = "TextLayers";
	protected ComplexProperty seqLayer;

	@PropertyType(propertyType = "Text")
	public static final String TRANSMISSION_REFERENCE = "TransmissionReference";

	@PropertyType(propertyType = "Integer")
	public static final String URGENCY = "Urgency";

	public TextType getAncestorProperty() {
		return (TextType) getProperty(localPrefix + ANCESTOR);
	}
	
	public String getAncestor() {
		TextType tt = ((TextType) getProperty(localPrefix + ANCESTOR));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setAncestor(String text) {
		addProperty(new TextType(metadata, localPrefix, ANCESTOR, text));
	}

	public void setAncestorProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getAuthorsPositionProperty() {
		return (TextType) getProperty(localPrefix + AUTHORS_POSITION);
	}
	
	public String getAuthorsPosition() {
		TextType tt = ((TextType) getProperty(localPrefix + AUTHORS_POSITION));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setAuthorsPosition(String text) {
		addProperty(new TextType(metadata, localPrefix, AUTHORS_POSITION, text));
	}

	public void setAuthorsPositionProperty(TextType text) {
		addProperty(text);
	}

	public TextType getCaptionWriterProperty() {
		return (TextType) getProperty(localPrefix + CAPTION_WRITER);
	}
	
	public String getCaptionWriter() {
		TextType tt = ((TextType) getProperty(localPrefix + CAPTION_WRITER));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCaptionWriter(String text) {
		addProperty(new TextType(metadata, localPrefix, CAPTION_WRITER, text));
	}

	public void setCaptionWriterProperty(TextType text) {
		addProperty(text);
	}

	public TextType getCategoryProperty() {
		return (TextType) getProperty(localPrefix + CATEGORY);
	}
	
	public String getCategory() {
		TextType tt = ((TextType) getProperty(localPrefix + CATEGORY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCategory(String text) {
		addProperty(new TextType(metadata, localPrefix, CATEGORY, text));
	}

	public void setCategoryProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getCityProperty() {
		return (TextType) getProperty(localPrefix + CITY);
	}
	
	public String getCity() {
		TextType tt = ((TextType) getProperty(localPrefix + CITY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCity(String text) {
		addProperty(new TextType(metadata, localPrefix, CITY, text));
	}

	public void setCityProperty(TextType text) {
		addProperty(text);
	}
	
	public IntegerType getColorModeProperty() {
		return (IntegerType) getProperty(localPrefix + COLOR_MODE);
	}
	
	public Integer getColorMode() {
		IntegerType tt = ((IntegerType) getProperty(localPrefix + COLOR_MODE));
		return tt == null ? null : tt.getValue();
	}
	
	public void setColorMode(String text) {
		addProperty(new IntegerType(metadata, localPrefix, COLOR_MODE, text));
	}

	public void setColorModeProperty(IntegerType text) {
		addProperty(text);
	}

	public TextType getCountryProperty() {
		return (TextType) getProperty(localPrefix + COUNTRY);
	}
	
	public String getCountry() {
		TextType tt = ((TextType) getProperty(localPrefix + COUNTRY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCountry(String text) {
		addProperty(new TextType(metadata, localPrefix, COUNTRY, text));
	}

	public void setCountryProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getCreditProperty() {
		return (TextType) getProperty(localPrefix + CREDIT);
	}
	
	public String getCredit() {
		TextType tt = ((TextType) getProperty(localPrefix + CREDIT));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCredit(String text) {
		addProperty(new TextType(metadata, localPrefix, CREDIT, text));
	}

	public void setCreditProperty(TextType text) {
		addProperty(text);
	}

	public TextType getDateCreatedProperty() {
		return (TextType) getProperty(localPrefix + DATE_CREATED);
	}
	
	public String getDateCreated() {
		TextType tt = ((TextType) getProperty(localPrefix + DATE_CREATED));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setDateCreated(String text) {
		addProperty(new TextType(metadata, localPrefix, DATE_CREATED, text));
	}

	public void setDateCreatedProperty(TextType text) {
		addProperty(text);
	}
	
	public void addDocumentAncestors(String text) {
		addBagValue(localPrefixSep + DOCUMENT_ANCESTORS, text);
	}

	public ComplexProperty getDocumentAncestorsProperty() {
		return (ComplexProperty) getProperty(localPrefixSep + DOCUMENT_ANCESTORS);
	}

	public List<String> getDocumentAncestors() {
		return getBagValueList(localPrefixSep + DOCUMENT_ANCESTORS);
	}

	public TextType getHeadlineProperty() {
		return (TextType) getProperty(localPrefix + HEADLINE);
	}
	
	public String getHeadline() {
		TextType tt = ((TextType) getProperty(localPrefix + HEADLINE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setHeadline(String text) {
		addProperty(new TextType(metadata, localPrefix, HEADLINE, text));
	}

	public void setHeadlineProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getHistoryProperty() {
		return (TextType) getProperty(localPrefix + HISTORY);
	}
	
	public String getHistory() {
		TextType tt = ((TextType) getProperty(localPrefix + HISTORY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setHistory(String text) {
		addProperty(new TextType(metadata, localPrefix, HISTORY, text));
	}

	public void setHistoryProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getIccProfileProperty() {
		return (TextType) getProperty(localPrefix + ICC_PROFILE);
	}
	
	public String getIccProfile() {
		TextType tt = ((TextType) getProperty(localPrefix + ICC_PROFILE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setIccProfile(String text) {
		addProperty(new TextType(metadata, localPrefix, ICC_PROFILE, text));
	}

	public void setIccProfileProperty(TextType text) {
		addProperty(text);
	}

	public TextType getInstructionsProperty() {
		return (TextType) getProperty(localPrefix + INSTRUCTIONS);
	}
	
	public String getInstructions() {
		TextType tt = ((TextType) getProperty(localPrefix + INSTRUCTIONS));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setInstructions(String text) {
		addProperty(new TextType(metadata, localPrefix, INSTRUCTIONS, text));
	}

	public void setInstructionsProperty(TextType text) {
		addProperty(text);
	}

	public TextType getSourceProperty() {
		return (TextType) getProperty(localPrefix + SOURCE);
	}
	
	public String getSource() {
		TextType tt = ((TextType) getProperty(localPrefix + SOURCE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setSource(String text) {
		addProperty(new TextType(metadata, localPrefix, SOURCE, text));
	}

	public void setSourceProperty(TextType text) {
		addProperty(text);
	}
	
	public TextType getStateProperty() {
		return (TextType) getProperty(localPrefix + STATE);
	}
	
	public String getState() {
		TextType tt = ((TextType) getProperty(localPrefix + STATE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setState(String text) {
		addProperty(new TextType(metadata, localPrefix, STATE, text));
	}

	public void setStateProperty(TextType text) {
		addProperty(text);
	}

	public void addSupplementalCategories(String text) {
		addBagValue(localPrefixSep + SUPPLEMENTAL_CATEGORIES, text);
	}

	public void removeSupplementalCategory(String text) {
		removeBagValue(localPrefixSep + SUPPLEMENTAL_CATEGORIES, text);
	}
	
	/**
     * Add a new supplemental category.
     * 
     * @param s The supplemental category.
     */
    public void addSupplementalCategory( String s )
    {
    	addSupplementalCategories(s);
    }


	
	public ComplexProperty getSupplementalCategoriesProperty() {
		return (ComplexProperty) getProperty(localPrefixSep + SUPPLEMENTAL_CATEGORIES);
	}

	public List<String> getSupplementalCategories() {
		return getBagValueList(localPrefixSep + SUPPLEMENTAL_CATEGORIES);
	}
	
	public void addTextLayers(String layerName, String layerText) {
		if (seqLayer == null) {
			seqLayer = new ComplexProperty(metadata, localPrefix, TEXT_LAYERS,
					ComplexProperty.ORDERED_ARRAY);
			addProperty(seqLayer);
		}
		LayerType layer = new LayerType(metadata, "rdf", "li");
		layer.setLayerName(PREFERRED_PHOTISHOP_PREFIX, "LayerName", layerName);
		layer.setLayerText(PREFERRED_PHOTISHOP_PREFIX, "LayerText", layerText);
		seqLayer.getContainer().addProperty(layer);
	}
	
	public List<LayerType> getTextLayers() throws BadFieldValueException {
		List<AbstractField> tmp = getArrayList(localPrefixSep + TEXT_LAYERS);
		if (tmp != null) {
			List<LayerType> layers = new ArrayList<LayerType>();
			for (AbstractField abstractField : tmp) {
				if (abstractField instanceof LayerType) {
					layers.add((LayerType) abstractField);
				} else {
					throw new BadFieldValueException("Layer expected and "
							+ abstractField.getClass().getName() + " found.");
				}
			}
			return layers;
		}
		return null;

	}
	
	public TextType getTransmissionReferenceProperty() {
		return (TextType) getProperty(localPrefix + TRANSMISSION_REFERENCE);
	}
	
	public String getTransmissionReference() {
		TextType tt = ((TextType) getProperty(localPrefix + TRANSMISSION_REFERENCE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setTransmissionReference(String text) {
		addProperty(new TextType(metadata, localPrefix, TRANSMISSION_REFERENCE, text));
	}

	public void setTransmissionReferenceProperty(TextType text) {
		addProperty(text);
	}

	public IntegerType getUrgencyProperty() {
		return (IntegerType) getProperty(localPrefix + URGENCY);
	}
	
	public Integer getUrgency() {
		IntegerType tt = ((IntegerType) getProperty(localPrefix + URGENCY));
		return tt == null ? null : tt.getValue();
	}
	
	public void setUrgency(String text) {
		addProperty(new IntegerType(metadata, localPrefix, URGENCY, text));
	}

    public void setUrgency( Integer s )
    {
    	// TODO should this test be done here ?
//        if( s != null )
//        {
//            if( s.intValue() < 1 || s.intValue() > 8 )
//            {
//                throw new RuntimeException( "Error: photoshop:Urgency must be between 1 and 8.  value=" + s );
//            }
//        }
		addProperty(new IntegerType(metadata, localPrefix, URGENCY, s));
    }

	
	public void setUrgencyProperty(IntegerType text) {
		addProperty(text);
	}
	
	
}
