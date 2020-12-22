/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.fontbox.afm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.fontbox.util.BoundingBox;

/**
 * This is the outermost AFM type.  This can be created by the afmparser with a valid AFM document.
 *
 * @author Ben Litchfield
 */
public class FontMetrics
{
    /**
     * This is the version of the FontMetrics.
     */
    private float afmVersion;
    private int metricSets = 0;
    private String fontName;
    private String fullName;
    private String familyName;
    private String weight;
    private BoundingBox fontBBox;
    private String fontVersion;
    private String notice;
    private String encodingScheme;
    private int mappingScheme = 0;
    private int escChar = 0;
    private String characterSet;
    private int characters = 0;
    private boolean isBaseFont = true;
    private float[] vVector;
    private Boolean isFixedV = null;
    private float capHeight;
    private float xHeight;
    private float ascender;
    private float descender;
    private final List<String> comments = new ArrayList<>();

    private float underlinePosition;
    private float underlineThickness;
    private float italicAngle;
    private float[] charWidth;
    private boolean isFixedPitch;
    private float standardHorizontalWidth;
    private float standardVerticalWidth;

    private final List<CharMetric> charMetrics = new ArrayList<>();
    private final Map<String, CharMetric> charMetricsMap = new HashMap<>();
    private final List<TrackKern> trackKern = new ArrayList<>();
    private final List<Composite> composites = new ArrayList<>();
    private final List<KernPair> kernPairs = new ArrayList<>();
    private final List<KernPair> kernPairs0 = new ArrayList<>();
    private final List<KernPair> kernPairs1 = new ArrayList<>();

    /**
     * Constructor.
     */
    public FontMetrics()
    {
        // Default constructor
    }

    /**
     * This will get the width of a character.
     *
     * @param name The character to get the width for.
     *
     * @return The width of the character.
     */
    public float getCharacterWidth(final String name )
    {
        float result = 0;
        final CharMetric metric = charMetricsMap.get( name );
        if( metric != null )
        {
            result = metric.getWx();
        }
        return result;
    }
    
    /**
     * This will get the width of a character.
     *
     * @param name The character to get the width for.
     * @return The width of the character.
     */
    public float getCharacterHeight(final String name )
    {
        float result = 0;
        final CharMetric metric = charMetricsMap.get( name );
        if( metric != null )
        {
            result = metric.getWy(); 
            if( Float.compare(result, 0) == 0 )
            {
                result = metric.getBoundingBox().getHeight();
            }
        }
        return result;
    }


    /**
     * This will get the average width of a character.
     *
     * @return The width of the character.
     */
    public float getAverageCharacterWidth()
    {
        float average = 0;
        float totalWidths = 0;
        float characterCount = 0;
        for (final CharMetric metric : charMetrics)
        {
            if( metric.getWx() > 0 )
            {
                totalWidths += metric.getWx();
                characterCount += 1;
            }
        }
        if( totalWidths > 0 )
        {
            average = totalWidths / characterCount;
        }
        return average;
    }

    /**
     * This will add a new comment.
     *
     * @param comment The comment to add to this metric.
     */
    public void addComment(final String comment )
    {
        comments.add( comment );
    }

    /**
     * This will get all comments.
     *
     * @return The list of all comments.
     */
    public List<String> getComments()
    {
        return Collections.unmodifiableList(comments);
    }

    /**
     * This will get the version of the AFM document.
     *
     * @return The version of the document.
     */
    public float getAFMVersion()
    {
        return afmVersion;
    }

    /**
     * This will get the metricSets attribute.
     *
     * @return The value of the metric sets.
     */
    public int getMetricSets()
    {
        return metricSets;
    }

    /**
     * This will set the version of the AFM document.
     *
     * @param afmVersionValue The version of the document.
     */
    public void setAFMVersion(final float afmVersionValue )
    {
        afmVersion = afmVersionValue;
    }

    /**
     * This will set the metricSets attribute.  This value must be 0,1, or 2.
     *
     * @param metricSetsValue The new metric sets attribute.
     * @throws IllegalArgumentException If the metricSets attribute is not 0,1, or 2.
     */
    public void setMetricSets(final int metricSetsValue )
    {
        if( metricSetsValue < 0 || metricSetsValue > 2 )
        {
            throw new IllegalArgumentException("The metricSets attribute must be in the "
                    + "set {0,1,2} and not '" + metricSetsValue + "'");
        }
        metricSets = metricSetsValue;
    }

    /**
     * Getter for property fontName.
     *
     * @return Value of property fontName.
     */
    public String getFontName()
    {
        return fontName;
    }

    /**
     * Setter for property fontName.
     *
     * @param name New value of property fontName.
     */
    public void setFontName(final String name)
    {
        fontName = name;
    }

    /**
     * Getter for property fullName.
     *
     * @return Value of property fullName.
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Setter for property fullName.
     *
     * @param fullNameValue New value of property fullName.
     */
    public void setFullName(final String fullNameValue)
    {
        fullName = fullNameValue;
    }

    /**
     * Getter for property familyName.
     *
     * @return Value of property familyName.
     */
    public String getFamilyName()
    {
        return familyName;
    }

    /**
     * Setter for property familyName.
     *
     * @param familyNameValue New value of property familyName.
     */
    public void setFamilyName(final String familyNameValue)
    {
        familyName = familyNameValue;
    }

    /**
     * Getter for property weight.
     *
     * @return Value of property weight.
     */
    public String getWeight()
    {
        return weight;
    }

    /**
     * Setter for property weight.
     *
     * @param weightValue New value of property weight.
     */
    public void setWeight(final String weightValue)
    {
        weight = weightValue;
    }

    /**
     * Getter for property fontBBox.
     *
     * @return Value of property fontBBox.
     */
    public BoundingBox getFontBBox()
    {
        return fontBBox;
    }

    /**
     * Setter for property fontBBox.
     *
     * @param bBox New value of property fontBBox.
     */
    public void setFontBBox(final BoundingBox bBox)
    {
        this.fontBBox = bBox;
    }

    /**
     * Getter for property notice.
     *
     * @return Value of property notice.
     */
    public String getNotice()
    {
        return notice;
    }

    /**
     * Setter for property notice.
     *
     * @param noticeValue New value of property notice.
     */
    public void setNotice(final String noticeValue)
    {
        notice = noticeValue;
    }

    /**
     * Getter for property encodingScheme.
     *
     * @return Value of property encodingScheme.
     */
    public String getEncodingScheme()
    {
        return encodingScheme;
    }

    /**
     * Setter for property encodingScheme.
     *
     * @param encodingSchemeValue New value of property encodingScheme.
     */
    public void setEncodingScheme(final String encodingSchemeValue)
    {
        encodingScheme = encodingSchemeValue;
    }

    /**
     * Getter for property mappingScheme.
     *
     * @return Value of property mappingScheme.
     */
    public int getMappingScheme()
    {
        return mappingScheme;
    }

    /**
     * Setter for property mappingScheme.
     *
     * @param mappingSchemeValue New value of property mappingScheme.
     */
    public void setMappingScheme(final int mappingSchemeValue)
    {
        mappingScheme = mappingSchemeValue;
    }

    /**
     * Getter for property escChar.
     *
     * @return Value of property escChar.
     */
    public int getEscChar()
    {
        return escChar;
    }

    /**
     * Setter for property escChar.
     *
     * @param escCharValue New value of property escChar.
     */
    public void setEscChar(final int escCharValue)
    {
        escChar = escCharValue;
    }

    /**
     * Getter for property characterSet.
     *
     * @return Value of property characterSet.
     */
    public String getCharacterSet()
    {
        return characterSet;
    }

    /**
     * Setter for property characterSet.
     *
     * @param characterSetValue New value of property characterSet.
     */
    public void setCharacterSet(final String characterSetValue)
    {
        characterSet = characterSetValue;
    }

    /**
     * Getter for property characters.
     *
     * @return Value of property characters.
     */
    public int getCharacters()
    {
        return characters;
    }

    /**
     * Setter for property characters.
     *
     * @param charactersValue New value of property characters.
     */
    public void setCharacters(final int charactersValue)
    {
        characters = charactersValue;
    }

    /**
     * Getter for property isBaseFont.
     *
     * @return Value of property isBaseFont.
     */
    public boolean getIsBaseFont()
    {
        return isBaseFont;
    }

    /**
     * Setter for property isBaseFont.
     *
     * @param isBaseFontValue New value of property isBaseFont.
     */
    public void setIsBaseFont(final boolean isBaseFontValue)
    {
        isBaseFont = isBaseFontValue;
    }

    /**
     * Getter for property vVector.
     *
     * @return Value of property vVector.
     */
    public float[] getVVector()
    {
        return this.vVector;
    }

    /**
     * Setter for property vVector.
     *
     * @param vVectorValue New value of property vVector.
     */
    public void setVVector(final float[] vVectorValue)
    {
        vVector = vVectorValue;
    }

    /**
     * Getter for property isFixedV.
     *
     * @return Value of property isFixedV.
     */
    public boolean getIsFixedV()
    {
        // if not set the default value depends on the existence of vVector
        return isFixedV == null ? vVector != null : isFixedV;
    }

    /**
     * Setter for property isFixedV.
     *
     * @param isFixedVValue New value of property isFixedV.
     */
    public void setIsFixedV(final boolean isFixedVValue)
    {
        isFixedV = isFixedVValue;
    }

    /**
     * Getter for property capHeight.
     *
     * @return Value of property capHeight.
     */
    public float getCapHeight()
    {
        return capHeight;
    }

    /**
     * Setter for property capHeight.
     *
     * @param capHeightValue New value of property capHeight.
     */
    public void setCapHeight(final float capHeightValue)
    {
        capHeight = capHeightValue;
    }

    /**
     * Getter for property xHeight.
     *
     * @return Value of property xHeight.
     */
    public float getXHeight()
    {
        return xHeight;
    }

    /**
     * Setter for property xHeight.
     *
     * @param xHeightValue New value of property xHeight.
     */
    public void setXHeight(final float xHeightValue )
    {
        xHeight = xHeightValue;
    }

    /**
     * Getter for property ascender.
     *
     * @return Value of property ascender.
     */
    public float getAscender()
    {
        return ascender;
    }

    /**
     * Setter for property ascender.
     *
     * @param ascenderValue New value of property ascender.
     */
    public void setAscender(final float ascenderValue )
    {
        ascender = ascenderValue;
    }

    /**
     * Getter for property descender.
     *
     * @return Value of property descender.
     */
    public float getDescender()
    {
        return descender;
    }

    /**
     * Setter for property descender.
     *
     * @param descenderValue New value of property descender.
     */
    public void setDescender(final float descenderValue )
    {
        descender = descenderValue;
    }

    /**
     * Getter for property fontVersion.
     *
     * @return Value of property fontVersion.
     */
    public String getFontVersion()
    {
        return fontVersion;
    }

    /**
     * Setter for property fontVersion.
     *
     * @param fontVersionValue New value of property fontVersion.
     */
    public void setFontVersion(final String fontVersionValue)
    {
        fontVersion = fontVersionValue;
    }

    /**
     * Getter for property underlinePosition.
     *
     * @return Value of property underlinePosition.
     */
    public float getUnderlinePosition()
    {
        return underlinePosition;
    }

    /**
     * Setter for property underlinePosition.
     *
     * @param underlinePositionValue New value of property underlinePosition.
     */
    public void setUnderlinePosition(final float underlinePositionValue)
    {
        underlinePosition = underlinePositionValue;
    }

    /**
     * Getter for property underlineThickness.
     *
     * @return Value of property underlineThickness.
     */
    public float getUnderlineThickness()
    {
        return underlineThickness;
    }

    /**
     * Setter for property underlineThickness.
     *
     * @param underlineThicknessValue New value of property underlineThickness.
     */
    public void setUnderlineThickness(final float underlineThicknessValue)
    {
        underlineThickness = underlineThicknessValue;
    }

    /**
     * Getter for property italicAngle.
     *
     * @return Value of property italicAngle.
     */
    public float getItalicAngle()
    {
        return italicAngle;
    }

    /**
     * Setter for property italicAngle.
     *
     * @param italicAngleValue New value of property italicAngle.
     */
    public void setItalicAngle(final float italicAngleValue)
    {
        italicAngle = italicAngleValue;
    }

    /**
     * Getter for property charWidth.
     *
     * @return Value of property charWidth.
     */
    public float[] getCharWidth()
    {
        return this.charWidth;
    }

    /**
     * Setter for property charWidth.
     *
     * @param charWidthValue New value of property charWidth.
     */
    public void setCharWidth(final float[] charWidthValue)
    {
        charWidth = charWidthValue;
    }

    /**
     * Getter for property isFixedPitch.
     *
     * @return Value of property isFixedPitch.
     */
    public boolean getIsFixedPitch()
    {
        return isFixedPitch;
    }

    /**
     * Setter for property isFixedPitch.
     *
     * @param isFixedPitchValue New value of property isFixedPitch.
     */
    public void setFixedPitch(final boolean isFixedPitchValue)
    {
        isFixedPitch = isFixedPitchValue;
    }

    /** Getter for property charMetrics.
     * @return Value of property charMetrics.
     */
    public List<CharMetric> getCharMetrics()
    {
        return Collections.unmodifiableList(charMetrics);
    }

    /**
     * This will add another character metric.
     *
     * @param metric The character metric to add.
     */
    public void addCharMetric(final CharMetric metric )
    {
        charMetrics.add( metric );
        charMetricsMap.put( metric.getName(), metric );
    }

    /** Getter for property trackKern.
     * @return Value of property trackKern.
     */
    public List<TrackKern> getTrackKern()
    {
        return Collections.unmodifiableList(trackKern);
    }

    /**
     * This will add another track kern.
     *
     * @param kern The track kerning data.
     */
    public void addTrackKern(final TrackKern kern )
    {
        trackKern.add( kern );
    }

    /** Getter for property composites.
     * @return Value of property composites.
     */
    public List<Composite> getComposites()
    {
        return Collections.unmodifiableList(composites);
    }

    /**
     * This will add a single composite part to the picture.
     *
     * @param composite The composite info to add.
     */
    public void addComposite(final Composite composite )
    {
        composites.add( composite );
    }

    /** Getter for property kernPairs.
     * @return Value of property kernPairs.
     */
    public List<KernPair> getKernPairs()
    {
        return Collections.unmodifiableList(kernPairs);
    }

    /**
     * This will add a kern pair.
     *
     * @param kernPair The kern pair to add.
     */
    public void addKernPair(final KernPair kernPair )
    {
        kernPairs.add( kernPair );
    }

    /** Getter for property kernPairs0.
     * @return Value of property kernPairs0.
     */
    public List<KernPair> getKernPairs0()
    {
        return Collections.unmodifiableList(kernPairs0);
    }

    /**
     * This will add a kern pair.
     *
     * @param kernPair The kern pair to add.
     */
    public void addKernPair0(final KernPair kernPair )
    {
        kernPairs0.add( kernPair );
    }

    /** Getter for property kernPairs1.
     * @return Value of property kernPairs1.
     */
    public List<KernPair> getKernPairs1()
    {
        return Collections.unmodifiableList(kernPairs1);
    }

    /**
     * This will add a kern pair.
     *
     * @param kernPair The kern pair to add.
     */
    public void addKernPair1(final KernPair kernPair )
    {
        kernPairs1.add( kernPair );
    }

    /** Getter for property standardHorizontalWidth.
     * @return Value of property standardHorizontalWidth.
     */
    public float getStandardHorizontalWidth()
    {
        return standardHorizontalWidth;
    }

    /** Setter for property standardHorizontalWidth.
     * @param standardHorizontalWidthValue New value of property standardHorizontalWidth.
     */
    public void setStandardHorizontalWidth(final float standardHorizontalWidthValue)
    {
        standardHorizontalWidth = standardHorizontalWidthValue;
    }

    /** Getter for property standardVerticalWidth.
     * @return Value of property standardVerticalWidth.
     */
    public float getStandardVerticalWidth()
    {
        return standardVerticalWidth;
    }

    /** Setter for property standardVerticalWidth.
     * @param standardVerticalWidthValue New value of property standardVerticalWidth.
     */
    public void setStandardVerticalWidth(final float standardVerticalWidthValue)
    {
        standardVerticalWidth = standardVerticalWidthValue;
    }

}