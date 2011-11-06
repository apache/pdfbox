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

	public TextType getAncestor() {
		return (TextType) getProperty(localPrefix + ANCESTOR);
	}
	
	public String getAncestorValue() {
		TextType tt = ((TextType) getProperty(localPrefix + ANCESTOR));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setAncestorValue(String text) {
		addProperty(new TextType(metadata, localPrefix, ANCESTOR, text));
	}

	public void setAncestor(TextType text) {
		addProperty(text);
	}
	
	public TextType getAuthorsPosition() {
		return (TextType) getProperty(localPrefix + AUTHORS_POSITION);
	}
	
	public String getAuthorsPositionValue() {
		TextType tt = ((TextType) getProperty(localPrefix + AUTHORS_POSITION));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setAuthorsPositionValue(String text) {
		addProperty(new TextType(metadata, localPrefix, AUTHORS_POSITION, text));
	}

	public void setAuthorsPosition(TextType text) {
		addProperty(text);
	}

	public TextType getCaptionWriter() {
		return (TextType) getProperty(localPrefix + CAPTION_WRITER);
	}
	
	public String getCaptionWriterValue() {
		TextType tt = ((TextType) getProperty(localPrefix + CAPTION_WRITER));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCaptionWriterValue(String text) {
		addProperty(new TextType(metadata, localPrefix, CAPTION_WRITER, text));
	}

	public void setCaptionWriter(TextType text) {
		addProperty(text);
	}

	public TextType getCategory() {
		return (TextType) getProperty(localPrefix + CATEGORY);
	}
	
	public String getCategoryValue() {
		TextType tt = ((TextType) getProperty(localPrefix + CATEGORY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCategoryValue(String text) {
		addProperty(new TextType(metadata, localPrefix, CATEGORY, text));
	}

	public void setCategory(TextType text) {
		addProperty(text);
	}
	
	public TextType getCity() {
		return (TextType) getProperty(localPrefix + CITY);
	}
	
	public String getCityValue() {
		TextType tt = ((TextType) getProperty(localPrefix + CITY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCityValue(String text) {
		addProperty(new TextType(metadata, localPrefix, CITY, text));
	}

	public void setCity(TextType text) {
		addProperty(text);
	}
	
	public IntegerType getColorMode() {
		return (IntegerType) getProperty(localPrefix + COLOR_MODE);
	}
	
	public Integer getColorModeValue() {
		IntegerType tt = ((IntegerType) getProperty(localPrefix + COLOR_MODE));
		return tt == null ? null : tt.getValue();
	}
	
	public void setColorModeValue(String text) {
		addProperty(new IntegerType(metadata, localPrefix, COLOR_MODE, text));
	}

	public void setColorMode(IntegerType text) {
		addProperty(text);
	}

	public TextType getCountry() {
		return (TextType) getProperty(localPrefix + COUNTRY);
	}
	
	public String getCountryValue() {
		TextType tt = ((TextType) getProperty(localPrefix + COUNTRY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCountryValue(String text) {
		addProperty(new TextType(metadata, localPrefix, COUNTRY, text));
	}

	public void setCountry(TextType text) {
		addProperty(text);
	}
	
	public TextType getCredit() {
		return (TextType) getProperty(localPrefix + CREDIT);
	}
	
	public String getCreditValue() {
		TextType tt = ((TextType) getProperty(localPrefix + CREDIT));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setCreditValue(String text) {
		addProperty(new TextType(metadata, localPrefix, CREDIT, text));
	}

	public void setCredit(TextType text) {
		addProperty(text);
	}

	public TextType getDateCreated() {
		return (TextType) getProperty(localPrefix + DATE_CREATED);
	}
	
	public String getDateCreatedValue() {
		TextType tt = ((TextType) getProperty(localPrefix + DATE_CREATED));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setDateCreatedValue(String text) {
		addProperty(new TextType(metadata, localPrefix, DATE_CREATED, text));
	}

	public void setDateCreated(TextType text) {
		addProperty(text);
	}
	
	public void addToDocumentAncestorsValue(String text) {
		addBagValue(localPrefixSep + DOCUMENT_ANCESTORS, text);
	}

	public ComplexProperty getDocumentAncestors() {
		return (ComplexProperty) getProperty(localPrefixSep + DOCUMENT_ANCESTORS);
	}

	public List<String> getDocumentAncestorsValue() {
		return getBagValueList(localPrefixSep + DOCUMENT_ANCESTORS);
	}

	public TextType getHeadline() {
		return (TextType) getProperty(localPrefix + HEADLINE);
	}
	
	public String getHeadlineValue() {
		TextType tt = ((TextType) getProperty(localPrefix + HEADLINE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setHeadlineValue(String text) {
		addProperty(new TextType(metadata, localPrefix, HEADLINE, text));
	}

	public void setHeadline(TextType text) {
		addProperty(text);
	}
	
	public TextType getHistory() {
		return (TextType) getProperty(localPrefix + HISTORY);
	}
	
	public String getHistoryValue() {
		TextType tt = ((TextType) getProperty(localPrefix + HISTORY));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setHistoryValue(String text) {
		addProperty(new TextType(metadata, localPrefix, HISTORY, text));
	}

	public void setHistory(TextType text) {
		addProperty(text);
	}
	
	public TextType getIccProfile() {
		return (TextType) getProperty(localPrefix + ICC_PROFILE);
	}
	
	public String getIccProfileValue() {
		TextType tt = ((TextType) getProperty(localPrefix + ICC_PROFILE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setIccProfileValue(String text) {
		addProperty(new TextType(metadata, localPrefix, ICC_PROFILE, text));
	}

	public void setIccProfile(TextType text) {
		addProperty(text);
	}

	public TextType getInstructions() {
		return (TextType) getProperty(localPrefix + INSTRUCTIONS);
	}
	
	public String getInstructionsValue() {
		TextType tt = ((TextType) getProperty(localPrefix + INSTRUCTIONS));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setInstructionsValue(String text) {
		addProperty(new TextType(metadata, localPrefix, INSTRUCTIONS, text));
	}

	public void setInstructions(TextType text) {
		addProperty(text);
	}

	public TextType getSource() {
		return (TextType) getProperty(localPrefix + SOURCE);
	}
	
	public String getSourceValue() {
		TextType tt = ((TextType) getProperty(localPrefix + SOURCE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setSourceValue(String text) {
		addProperty(new TextType(metadata, localPrefix, SOURCE, text));
	}

	public void setSource(TextType text) {
		addProperty(text);
	}
	
	public TextType getState() {
		return (TextType) getProperty(localPrefix + STATE);
	}
	
	public String getStateValue() {
		TextType tt = ((TextType) getProperty(localPrefix + STATE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setStateValue(String text) {
		addProperty(new TextType(metadata, localPrefix, STATE, text));
	}

	public void setState(TextType text) {
		addProperty(text);
	}

	public void addToSupplementalCategoriesValue(String text) {
		addBagValue(localPrefixSep + SUPPLEMENTAL_CATEGORIES, text);
	}

	public ComplexProperty getSupplementalCategories() {
		return (ComplexProperty) getProperty(localPrefixSep + SUPPLEMENTAL_CATEGORIES);
	}

	public List<String> getSupplementalCategoriesValue() {
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
	
	public TextType getTransmissionReference() {
		return (TextType) getProperty(localPrefix + TRANSMISSION_REFERENCE);
	}
	
	public String getTransmissionReferenceValue() {
		TextType tt = ((TextType) getProperty(localPrefix + TRANSMISSION_REFERENCE));
		return tt == null ? null : tt.getStringValue();
	}
	
	public void setTransmissionReferenceValue(String text) {
		addProperty(new TextType(metadata, localPrefix, TRANSMISSION_REFERENCE, text));
	}

	public void setTransmissionReference(TextType text) {
		addProperty(text);
	}

	public IntegerType getUrgency() {
		return (IntegerType) getProperty(localPrefix + URGENCY);
	}
	
	public Integer getUrgencyValue() {
		IntegerType tt = ((IntegerType) getProperty(localPrefix + URGENCY));
		return tt == null ? null : tt.getValue();
	}
	
	public void setUrgencyValue(String text) {
		addProperty(new IntegerType(metadata, localPrefix, URGENCY, text));
	}

	public void setUrgency(IntegerType text) {
		addProperty(text);
	}
	
	
}
