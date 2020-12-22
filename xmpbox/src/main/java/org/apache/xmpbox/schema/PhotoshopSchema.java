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

    public PhotoshopSchema(final XMPMetadata metadata)
    {
        super(metadata);
    }

    public PhotoshopSchema(final XMPMetadata metadata, final String ownPrefix)
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
        return (URIType) getProperty(ANCESTORID);
    }

    public String getAncestorID()
    {
        final TextType tt = ((TextType) getProperty(ANCESTORID));
        return tt == null ? null : tt.getStringValue();
    }

    public void setAncestorID(final String text)
    {
        final URIType tt = (URIType) instanciateSimple(ANCESTORID, text);
        setAncestorIDProperty(tt);
    }

    public void setAncestorIDProperty(final URIType text)
    {
        addProperty(text);
    }

    public TextType getAuthorsPositionProperty()
    {
        return (TextType) getProperty(AUTHORS_POSITION);
    }

    public String getAuthorsPosition()
    {
        final TextType tt = ((TextType) getProperty(AUTHORS_POSITION));
        return tt == null ? null : tt.getStringValue();
    }

    public void setAuthorsPosition(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(AUTHORS_POSITION, text);
        setAuthorsPositionProperty(tt);
    }

    public void setAuthorsPositionProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getCaptionWriterProperty()
    {
        return (TextType) getProperty(CAPTION_WRITER);
    }

    public String getCaptionWriter()
    {
        final TextType tt = ((TextType) getProperty(CAPTION_WRITER));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCaptionWriter(final String text)
    {
        final ProperNameType tt = (ProperNameType) instanciateSimple(CAPTION_WRITER, text);
        setCaptionWriterProperty(tt);
    }

    public void setCaptionWriterProperty(final ProperNameType text)
    {
        addProperty(text);
    }

    public TextType getCategoryProperty()
    {
        return (TextType) getProperty(CATEGORY);
    }

    public String getCategory()
    {
        final TextType tt = ((TextType) getProperty(CATEGORY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCategory(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(CATEGORY, text);
        setCategoryProperty(tt);
    }

    public void setCategoryProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getCityProperty()
    {
        return (TextType) getProperty(CITY);
    }

    public String getCity()
    {
        final TextType tt = ((TextType) getProperty(CITY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCity(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(CITY, text);
        setCityProperty(tt);
    }

    public void setCityProperty(final TextType text)
    {
        addProperty(text);
    }

    public IntegerType getColorModeProperty()
    {
        return (IntegerType) getProperty(COLOR_MODE);
    }

    public Integer getColorMode()
    {
        final IntegerType tt = ((IntegerType) getProperty(COLOR_MODE));
        return tt == null ? null : tt.getValue();
    }

    public void setColorMode(final String text)
    {
        final IntegerType tt = (IntegerType) instanciateSimple(COLOR_MODE, text);
        setColorModeProperty(tt);
    }

    public void setColorModeProperty(final IntegerType text)
    {
        addProperty(text);
    }

    public TextType getCountryProperty()
    {
        return (TextType) getProperty(COUNTRY);
    }

    public String getCountry()
    {
        final TextType tt = ((TextType) getProperty(COUNTRY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCountry(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(COUNTRY, text);
        setCountryProperty(tt);
    }

    public void setCountryProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getCreditProperty()
    {
        return (TextType) getProperty(CREDIT);
    }

    public String getCredit()
    {
        final TextType tt = ((TextType) getProperty(CREDIT));
        return tt == null ? null : tt.getStringValue();
    }

    public void setCredit(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(CREDIT, text);
        setCreditProperty(tt);
    }

    public void setCreditProperty(final TextType text)
    {
        addProperty(text);
    }

    public DateType getDateCreatedProperty()
    {
        return (DateType) getProperty(DATE_CREATED);
    }

    public String getDateCreated()
    {
        final TextType tt = ((TextType) getProperty(DATE_CREATED));
        return tt == null ? null : tt.getStringValue();
    }

    public void setDateCreated(final String text)
    {
        final DateType tt = (DateType) instanciateSimple(DATE_CREATED, text);
        setDateCreatedProperty(tt);
    }

    public void setDateCreatedProperty(final DateType text)
    {
        addProperty(text);
    }

    public void addDocumentAncestors(final String text)
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
        final TextType tt = ((TextType) getProperty(HEADLINE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setHeadline(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(HEADLINE, text);
        setHeadlineProperty(tt);
    }

    public void setHeadlineProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getHistoryProperty()
    {
        return (TextType) getProperty(HISTORY);
    }

    public String getHistory()
    {
        final TextType tt = ((TextType) getProperty(HISTORY));
        return tt == null ? null : tt.getStringValue();
    }

    public void setHistory(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(HISTORY, text);
        setHistoryProperty(tt);
    }

    public void setHistoryProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getICCProfileProperty()
    {
        return (TextType) getProperty(ICC_PROFILE);
    }

    public String getICCProfile()
    {
        final TextType tt = ((TextType) getProperty(ICC_PROFILE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setICCProfile(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(ICC_PROFILE, text);
        setICCProfileProperty(tt);
    }

    public void setICCProfileProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getInstructionsProperty()
    {
        return (TextType) getProperty(INSTRUCTIONS);
    }

    public String getInstructions()
    {
        final TextType tt = ((TextType) getProperty(INSTRUCTIONS));
        return tt == null ? null : tt.getStringValue();
    }

    public void setInstructions(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(INSTRUCTIONS, text);
        setInstructionsProperty(tt);

    }

    public void setInstructionsProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getSourceProperty()
    {
        return (TextType) getProperty(SOURCE);
    }

    public String getSource()
    {
        final TextType tt = ((TextType) getProperty(SOURCE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setSource(final String text)
    {
        final TextType source = (TextType) instanciateSimple(SOURCE, text);
        setSourceProperty(source);
    }

    public void setSourceProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getStateProperty()
    {
        return (TextType) getProperty(STATE);
    }

    public String getState()
    {
        final TextType tt = ((TextType) getProperty(STATE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setState(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(STATE, text);
        setStateProperty(tt);
    }

    public void setStateProperty(final TextType text)
    {
        addProperty(text);
    }

    public TextType getSupplementalCategoriesProperty()
    {
        return (TextType) getProperty(SUPPLEMENTAL_CATEGORIES);
    }

    public String getSupplementalCategories()
    {
        final TextType tt = ((TextType) getProperty(SUPPLEMENTAL_CATEGORIES));
        return tt == null ? null : tt.getStringValue();
    }

    public void setSupplementalCategories(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(SUPPLEMENTAL_CATEGORIES, text);
        setSupplementalCategoriesProperty(tt);
    }

    public void setSupplementalCategoriesProperty(final TextType text)
    {
        addProperty(text);
    }

    public void addTextLayers(final String layerName, final String layerText)
    {
        if (seqLayer == null)
        {
            seqLayer = createArrayProperty(TEXT_LAYERS, Cardinality.Seq);
            addProperty(seqLayer);
        }
        final LayerType layer = new LayerType(getMetadata());
        layer.setLayerName(layerName);
        layer.setLayerText(layerText);
        seqLayer.getContainer().addProperty(layer);
    }

    public List<LayerType> getTextLayers() throws BadFieldValueException
    {
        final List<AbstractField> tmp = getUnqualifiedArrayList(TEXT_LAYERS);
        if (tmp != null)
        {
            final List<LayerType> layers = new ArrayList<>();
            for (final AbstractField abstractField : tmp)
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
        final TextType tt = ((TextType) getProperty(TRANSMISSION_REFERENCE));
        return tt == null ? null : tt.getStringValue();
    }

    public void setTransmissionReference(final String text)
    {
        final TextType tt = (TextType) instanciateSimple(TRANSMISSION_REFERENCE, text);
        setTransmissionReferenceProperty(tt);
    }

    public void setTransmissionReferenceProperty(final TextType text)
    {
        addProperty(text);
    }

    public IntegerType getUrgencyProperty()
    {
        return (IntegerType) getProperty(URGENCY);
    }

    public Integer getUrgency()
    {
        final IntegerType tt = ((IntegerType) getProperty(URGENCY));
        return tt == null ? null : tt.getValue();
    }

    public void setUrgency(final String s)
    {
        final IntegerType tt = (IntegerType) instanciateSimple(URGENCY, s);
        setUrgencyProperty(tt);
    }

    public void setUrgency(final Integer s)
    {
        final IntegerType tt = (IntegerType) instanciateSimple(URGENCY, s);
        setUrgencyProperty(tt);
    }

    public void setUrgencyProperty(final IntegerType text)
    {
        addProperty(text);
    }

}
