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

    @PropertyType(type = Types.Text, card = Cardinality.Simple)
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
        return getPropertyAs(ANCESTORID, URIType.class);
    }

    public String getAncestorID()
    {
        TextType tt = getAncestorIDProperty();
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
        return getPropertyAs(AUTHORS_POSITION, TextType.class);
    }

    public String getAuthorsPosition()
    {
        TextType tt = getAuthorsPositionProperty();
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
        return getPropertyAs(CAPTION_WRITER, TextType.class);
    }

    public String getCaptionWriter()
    {
        TextType tt = getCaptionWriterProperty();
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
        return getPropertyAs(CATEGORY, TextType.class);
    }

    public String getCategory()
    {
        TextType tt = getCategoryProperty();
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
        return getPropertyAs(CITY, TextType.class);
    }

    public String getCity()
    {
        TextType tt = getCityProperty();
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
        return getPropertyAs(COLOR_MODE, IntegerType.class);
    }

    public Integer getColorMode()
    {
        IntegerType tt = getColorModeProperty();
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
        return getPropertyAs(COUNTRY, TextType.class);
    }

    public String getCountry()
    {
        TextType tt = getCountryProperty();
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
        return getPropertyAs(CREDIT, TextType.class);
    }

    public String getCredit()
    {
        TextType tt = getCreditProperty();
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
        return getPropertyAs(DATE_CREATED, DateType.class);
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
        return getPropertyAs(DOCUMENT_ANCESTORS, ArrayProperty.class);
    }

    public List<String> getDocumentAncestors()
    {
        return getUnqualifiedBagValueList(DOCUMENT_ANCESTORS);
    }

    public TextType getHeadlineProperty()
    {
        return getPropertyAs(HEADLINE, TextType.class);
    }

    public String getHeadline()
    {
        TextType tt = getHeadlineProperty();
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
        return getPropertyAs(HISTORY, TextType.class);
    }

    public String getHistory()
    {
        TextType tt = getHistoryProperty();
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
        return getPropertyAs(ICC_PROFILE, TextType.class);
    }

    public String getICCProfile()
    {
        TextType tt = getICCProfileProperty();
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
        return getPropertyAs(INSTRUCTIONS, TextType.class);
    }

    public String getInstructions()
    {
        TextType tt = getInstructionsProperty();
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
        return getPropertyAs(SOURCE, TextType.class);
    }

    public String getSource()
    {
        TextType tt = getSourceProperty();
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
        return getPropertyAs(STATE, TextType.class);
    }

    public String getState()
    {
        TextType tt = getStateProperty();
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

    public TextType getSupplementalCategoriesProperty()
    {
        return getPropertyAs(SUPPLEMENTAL_CATEGORIES, TextType.class);
    }

    public String getSupplementalCategories()
    {
        TextType tt = getSupplementalCategoriesProperty();
        return tt == null ? null : tt.getStringValue();
    }

    public void setSupplementalCategories(String text)
    {
        TextType tt = (TextType) instanciateSimple(SUPPLEMENTAL_CATEGORIES, text);
        setSupplementalCategoriesProperty(tt);
    }

    public void setSupplementalCategoriesProperty(TextType text)
    {
        addProperty(text);
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
            List<LayerType> layers = new ArrayList<>();
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
        return getPropertyAs(TRANSMISSION_REFERENCE, TextType.class);
    }

    public String getTransmissionReference()
    {
        TextType tt = getTransmissionReferenceProperty();
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
        return getPropertyAs(URGENCY, IntegerType.class);
    }

    public Integer getUrgency()
    {
        IntegerType tt = getUrgencyProperty();
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
