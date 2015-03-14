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

import org.apache.xmpbox.XMPMetadata;

import java.util.List;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.ProperNameType;
import org.apache.xmpbox.type.PropertyType;
import org.apache.xmpbox.type.StructuredType;
import org.apache.xmpbox.type.Types;

@StructuredType(preferedPrefix = "tiff", namespace = "http://ns.adobe.com/tiff/1.0/")
public class TiffSchema extends XMPSchema
{

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String IMAGE_DESCRIPTION = "ImageDescription";

    @PropertyType(type = Types.LangAlt, card = Cardinality.Simple)
    public static final String COPYRIGHT = "Copyright";

    @PropertyType(type = Types.ProperName, card = Cardinality.Simple)
    public static final String ARTIST = "Artist";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String IMAGE_WIDTH = "ImageWidth";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String IMAGE_LENGHT = "ImageLength";

    @PropertyType(type = Types.Integer, card = Cardinality.Seq)
    public static final String  BITS_PER_SAMPLE= "BitsPerSample";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String COMPRESSION = "Compression";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String PHOTOMETRIC_INTERPRETATION = "PhotometricInterpretation";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String ORIENTATION = "Orientation";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String SAMPLES_PER_PIXEL = "SamplesPerPixel";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String PLANAR_CONFIGURATION = "PlanarConfiguration";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String YCB_CR_SUB_SAMPLING = "YCbCrSubSampling";

    @PropertyType(type = Types.Integer, card = Cardinality.Seq)
    public static final String YCB_CR_POSITIONING = "YCbCrPositioning";

    @PropertyType(type = Types.Rational, card = Cardinality.Simple)
    public static final String XRESOLUTION = "XResolution";

    @PropertyType(type = Types.Rational, card = Cardinality.Simple)
    public static final String YRESOLUTION = "YResolution";

    @PropertyType(type = Types.Integer, card = Cardinality.Simple)
    public static final String RESOLUTION_UNIT = "ResolutionUnit";

    @PropertyType(type = Types.Integer, card = Cardinality.Seq)
    public static final String TRANSFER_FUNCTION = "TransferFunction";

    @PropertyType(type = Types.Rational, card = Cardinality.Seq)
    public static final String WHITE_POINT = "WhitePoint";

    @PropertyType(type = Types.Rational, card = Cardinality.Seq)
    public static final String PRIMARY_CHROMATICITIES = "PrimaryChromaticities";

    @PropertyType(type = Types.Rational, card = Cardinality.Seq)
    public static final String YCB_CR_COEFFICIENTS = "YCbCrCoefficients";

    @PropertyType(type = Types.Rational, card = Cardinality.Seq)
    public static final String REFERENCE_BLACK_WHITE = "ReferenceBlackWhite";

    @PropertyType(type = Types.Date, card = Cardinality.Simple)
    public static final String DATE_TIME = "DateTime";

    @PropertyType(type = Types.AgentName, card = Cardinality.Simple)
    public static final String SOFTWARE = "Software";

    @PropertyType(type = Types.ProperName, card = Cardinality.Simple)
    public static final String MAKE = "Make";

    @PropertyType(type = Types.ProperName, card = Cardinality.Simple)
    public static final String MODEL = "Model";

    public TiffSchema(XMPMetadata metadata)
    {
        super(metadata);
    }

    public TiffSchema(XMPMetadata metadata, String prefix)
    {
        super(metadata, prefix);
    }

    /**
     * Return the artist property
     * @return artist ProperNameType
     */
    public ProperNameType getArtistProperty()
    {
        return (ProperNameType) getProperty(ARTIST);
    }

    /**
     * Return the artist property as String
     * @return string
     */
    public String getArtist()
    {
        ProperNameType tt = (ProperNameType) getProperty(ARTIST);
        return tt == null ? null : tt.getStringValue();
    }

    /**
     * Set the name of the artist
     * @param text
     */
    public void setArtist(String text)
    {
        addProperty(createTextType(ARTIST, text));
    }


    /**
     * Return the image description property object
     * @return the image description property
     */
    public ArrayProperty getImageDescriptionProperty()
    {
        return (ArrayProperty) getProperty(IMAGE_DESCRIPTION);
    }

    /**
     * Return the list of language existing for image description
     * @return a list of languages
     */
    public List<String> getImageDescriptionLanguages()
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(IMAGE_DESCRIPTION);
    }

    /**
     * Return the image description value as String in expected language
     *
     * @param lang expected language
     * @return image description value
     */
    public String getImageDescription(String lang)
    {
        return getUnqualifiedLanguagePropertyValue(IMAGE_DESCRIPTION, lang);
    }

    /**
     * Return the image description as String in default language
     * @return image description value
     */
    public String getImageDescription()
    {
        return getImageDescription(null);
    }

    /**
     * Add a image description value for a specified language
     * @param lang language of the image description
     * @param value image description text
     */
    public void addImageDescription(String lang, String value)
    {
        setUnqualifiedLanguagePropertyValue(IMAGE_DESCRIPTION, lang, value);
    }

    /**
     * Return the copyright property object
     * @return the copyright property
     */
    public ArrayProperty getCopyRightProperty()
    {
        return (ArrayProperty) getProperty(COPYRIGHT);
    }

    /**
     * Return the list of language existing for copyright
     * @return a list of languages
     */
    public List<String> getCopyRightLanguages()
    {
        return getUnqualifiedLanguagePropertyLanguagesValue(COPYRIGHT);
    }

    /**
     * Return the copyright value as String in expected language
     *
     * @param lang expected language
     * @return copyright value
     */
    public String getCopyRight(String lang)
    {
        return getUnqualifiedLanguagePropertyValue(COPYRIGHT, lang);
    }

    /**
     * Return the copyright value as String in default language
     * @return copyright value
     */
    public String getCopyRight()
    {
        return getCopyRight(null);
    }

    /**
     * Add a copyright value for a specified language
     * @param lang language of the copyright
     * @param value copyright text
     */
    public void addCopyright(String lang, String value)
    {
        setUnqualifiedLanguagePropertyValue(COPYRIGHT, lang, value);
    }

}
