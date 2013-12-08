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

package org.apache.xmpbox.schema;

import java.util.ArrayList;
import java.util.List;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.type.AbstractField;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.DateType;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.LayerType;
import org.apache.xmpbox.type.ProperNameType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.Types;
import org.apache.xmpbox.type.URIType;

@StructuredType(preferedPrefix = "photoshop", namespace = "http://ns.adobe.com/photoshop/1.0/")
public class PhotoshopSchema extends XMPSchema
{

    public PhotoshopSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public PhotoshopSchema(XMPMetadata metadata, String ownPrefix)
    {
        super(metadata, ownPrefix);
    }

    @PropertyType(type = Types.URI, card = Cardinality.Simple)
    public static final String ANCESTORID = "AncestorID";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String AUTHORS_POSITION = "AuthorsPosition";

    @PropertyType(type = Types.ProperName, card = Cardinality.Simple)
    public static final String CAPTION_WRITER = "CaptionWriter";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CATEGORY = "Category";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CITY = "City";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String COLOR_MODE = "ColorMode";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String COUNTRY = "Country";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String CREDIT = "Credit";

    @PropertyType(type = Types.Date, card = Cardinality.Simple)
    public static final String DATE_CREATED = "DateCreated";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String DOCUMENT_ANCESTORS = "DocumentAncestors";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String HEADLINE = "Headline";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String HISTORY = "History";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String ICC_PROFILE = "ICCProfile";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String INSTRUCTIONS = "Instructions";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String SOURCE = "Source";

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String STATE = "State";

    @PropertyType(type = Types.Text, card = Cardinality.Bag)
    public static final String SUPPLEMENTAL_CATEGORIES = "SupplementalCategories";

    @PropertyType(type = Types.Layer, card = Cardinality.Seq)
    public static final String TEXT_LAYERS = "TextLayers";

    private ArrayProperty seqLayer;

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
    public static final String TRANSMISSION_REFERENCE = "TransmissionReference";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String URGENCY = "Urgency";

    public URIType getAncestorIDProperty()
    {
        return (URIType) getProperty(ANCESTORID);
    }

    public String getAncestorID()
    {
        TextType tt = ((TextType) getProperty(ANCESTORID));
        return tt == null ? null : tt.getStringValue();
    }

    public void setAncestorID(String text)
    {
        URIType tt = (URIType) instanciateSimple(ANCESTORID, text);
        setAncestorIDProperty(tt);
    }

    public void setAncestorIDProperty(URIType text)
    {
        addProperty(text);
    }

    public TextType getAuthorsPositionProperty()
    {
        return (TextType) getProperty(AUTHORS_POSITION);
    }

    public String getAuthorsPosition()
    {
        TextType tt = ((TextType) getProperty(AUTHORS_POSITION));
        return tt == null ? null : tt.getStringValue();
    }

    public void setAuthorsPosition(String text)
    {
        TextType tt = (TextType) instanciateSimple(AUTHORS_POSITION, text);
        setAuthorsPositionProperty(tt);
    }

    public void setAuthorsPositionProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getCaptionWriterProperty()
    {
        return (TextType) getProperty(CAPTION_WRITER);
    }

    public String getCaptionWriter()
    {
        TextType tt = ((TextType) getProperty(CAPTION_WRITER));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCaptionWriter(String text)
    {
        ProperNameType tt = (ProperNameType) instanciateSimple(CAPTION_WRITER, text);
        setCaptionWriterProperty(tt);
    }

    public void setCaptionWriterProperty(ProperNameType text)
    {
        addProperty(text);
    }

    public TextType getCategoryProperty()
    {
        return (TextType) getProperty(CATEGORY);
    }

    public String getCategory()
    {
        TextType tt = ((TextType) getProperty(CATEGORY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCategory(String text)
    {
        TextType tt = (TextType) instanciateSimple(CATEGORY, text);
        setCategoryProperty(tt);
    }

    public void setCategoryProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getCityProperty()
    {
        return (TextType) getProperty(CITY);
    }

    public String getCity()
    {
        TextType tt = ((TextType) getProperty(CITY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCity(String text)
    {
        TextType tt = (TextType) instanciateSimple(CITY, text);
        setCityProperty(tt);
    }

    public void setCityProperty(TextType text)
    {
        addProperty(text);
    }

    public IntegerType getColorModeProperty()
    {
        return (IntegerType) getProperty(COLOR_MODE);
    }

    public Integer getColorMode()
    {
        IntegerType tt = ((IntegerType) getProperty(COLOR_MODE));
        return tt == null ? null : tt.getValue();
    }

    public void setColorMode(String text)
    {
        IntegerType tt = (IntegerType) instanciateSimple(COLOR_MODE, text);
        setColorModeProperty(tt);
    }

    public void setColorModeProperty(IntegerType text)
    {
        addProperty(text);
    }

    public TextType getCountryProperty()
    {
        return (TextType) getProperty(COUNTRY);
    }

    public String getCountry()
    {
        TextType tt = ((TextType) getProperty(COUNTRY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCountry(String text)
    {
        TextType tt = (TextType) instanciateSimple(COUNTRY, text);
        setCountryProperty(tt);
    }

    public void setCountryProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getCreditProperty()
    {
        return (TextType) getProperty(CREDIT);
    }

    public String getCredit()
    {
        TextType tt = ((TextType) getProperty(CREDIT));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCredit(String text)
    {
        TextType tt = (TextType) instanciateSimple(CREDIT, text);
        setCreditProperty(tt);
    }

    public void setCreditProperty(TextType text)
    {
        addProperty(text);
    }

    public DateType getDateCreatedProperty()
    {
        return (DateType) getProperty(DATE_CREATED);
    }

    public String getDateCreated()
    {
        TextType tt = ((TextType) getProperty(DATE_CREATED));
        return tt == null ? null : tt.getStringValue();
    }

    public void setDateCreated(String text)
    {
        DateType tt = (DateType) instanciateSimple(DATE_CREATED, text);
        setDateCreatedProperty(tt);
    }

    public void setDateCreatedProperty(DateType text)
    {
        addProperty(text);
    }

    public void addDocumentAncestors(String text)
    {
        addQualifiedBagValue(DOCUMENT_ANCESTORS, text);
    }

    public ArrayProperty getDocumentAncestorsProperty()
    {
        return (ArrayProperty) getProperty(DOCUMENT_ANCESTORS);
    }

    public List<String> getDocumentAncestors()
    {
        return getUnqualifiedBagValueList(DOCUMENT_ANCESTORS);
    }

    public TextType getHeadlineProperty()
    {
        return (TextType) getProperty(HEADLINE);
    }

    public String getHeadline()
    {
        TextType tt = ((TextType) getProperty(HEADLINE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setHeadline(String text)
    {
        TextType tt = (TextType) instanciateSimple(HEADLINE, text);
        setHeadlineProperty(tt);
    }

    public void setHeadlineProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getHistoryProperty()
    {
        return (TextType) getProperty(HISTORY);
    }

    public String getHistory()
    {
        TextType tt = ((TextType) getProperty(HISTORY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setHistory(String text)
    {
        TextType tt = (TextType) instanciateSimple(HISTORY, text);
        setHistoryProperty(tt);
    }

    public void setHistoryProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getICCProfileProperty()
    {
        return (TextType) getProperty(ICC_PROFILE);
    }

    public String getICCProfile()
    {
        TextType tt = ((TextType) getProperty(ICC_PROFILE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setICCProfile(String text)
    {
        TextType tt = (TextType) instanciateSimple(ICC_PROFILE, text);
        setICCProfileProperty(tt);
    }

    public void setICCProfileProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getInstructionsProperty()
    {
        return (TextType) getProperty(INSTRUCTIONS);
    }

    public String getInstructions()
    {
        TextType tt = ((TextType) getProperty(INSTRUCTIONS));
        return tt == null ? null : tt.getStringValue();
    }

    public void setInstructions(String text)
    {
        TextType tt = (TextType) instanciateSimple(INSTRUCTIONS, text);
        setInstructionsProperty(tt);

    }

    public void setInstructionsProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getSourceProperty()
    {
        return (TextType) getProperty(SOURCE);
    }

    public String getSource()
    {
        TextType tt = ((TextType) getProperty(SOURCE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setSource(String text)
    {
        TextType source = (TextType) instanciateSimple(SOURCE, text);
        setSourceProperty(source);
    }

    public void setSourceProperty(TextType text)
    {
        addProperty(text);
    }

    public TextType getStateProperty()
    {
        return (TextType) getProperty(STATE);
    }

    public String getState()
    {
        TextType tt = ((TextType) getProperty(STATE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setState(String text)
    {
        TextType tt = (TextType) instanciateSimple(STATE, text);
        setStateProperty(tt);
    }

    public void setStateProperty(TextType text)
    {
        addProperty(text);
    }

    public void addSupplementalCategories(String text)
    {
        addQualifiedBagValue(SUPPLEMENTAL_CATEGORIES, text);
    }

    public void removeSupplementalCategories(String text)
    {
        removeUnqualifiedBagValue(SUPPLEMENTAL_CATEGORIES, text);
    }

    /**
     * Add a new supplemental category.
     * 
     * @param s
     *            The supplemental category.
     */
    public void addSupplementalCategory(String s)
    {
        addSupplementalCategories(s);
    }

    public void removeSupplementalCategory(String text)
    {
        removeSupplementalCategories(text);
    }

    public ArrayProperty getSupplementalCategoriesProperty()
    {
        return (ArrayProperty) getProperty(SUPPLEMENTAL_CATEGORIES);
    }

    public List<String> getSupplementalCategories()
    {
        return getUnqualifiedBagValueList(SUPPLEMENTAL_CATEGORIES);
    }

    public void addTextLayers(String layerName, String layerText)
    {
        if (seqLayer == null)
        {
            seqLayer = createArrayProperty(TEXT_LAYERS, Cardinality.Seq);
            addProperty(seqLayer);
        }
        LayerType layer = new LayerType(getMetadata());
        layer.setLayerName(layerName);
        layer.setLayerText(layerText);
        seqLayer.getContainer().addProperty(layer);
    }

    public List<LayerType> getTextLayers() throws BadFieldValueException
    {
        List<AbstractField> tmp = getUnqualifiedArrayList(TEXT_LAYERS);
        if (tmp != null)
        {
            List<LayerType> layers = new ArrayList<LayerType>();
            for (AbstractField abstractField : tmp)
            {
                if (abstractField instanceof LayerType)
                {
                    layers.add((LayerType) abstractField);
                }
                else
                {
                    throw new BadFieldValueException("Layer expected and " + abstractField.getClass().getName()
                            + " found.");
                }
            }
            return layers;
        }
        return null;

    }

    public TextType getTransmissionReferenceProperty()
    {
        return (TextType) getProperty(TRANSMISSION_REFERENCE);
    }

    public String getTransmissionReference()
    {
        TextType tt = ((TextType) getProperty(TRANSMISSION_REFERENCE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setTransmissionReference(String text)
    {
        TextType tt = (TextType) instanciateSimple(TRANSMISSION_REFERENCE, text);
        setTransmissionReferenceProperty(tt);
    }

    public void setTransmissionReferenceProperty(TextType text)
    {
        addProperty(text);
    }

    public IntegerType getUrgencyProperty()
    {
        return (IntegerType) getProperty(URGENCY);
    }

    public Integer getUrgency()
    {
        IntegerType tt = ((IntegerType) getProperty(URGENCY));
        return tt == null ? null : tt.getValue();
    }

    public void setUrgency(String s)
    {
        IntegerType tt = (IntegerType) instanciateSimple(URGENCY, s);
        setUrgencyProperty(tt);
    }

    public void setUrgency(Integer s)
    {
        IntegerType tt = (IntegerType) instanciateSimple(URGENCY, s);
        setUrgencyProperty(tt);
    }

    public void setUrgencyProperty(IntegerType text)
    {
        addProperty(text);
    }

}
