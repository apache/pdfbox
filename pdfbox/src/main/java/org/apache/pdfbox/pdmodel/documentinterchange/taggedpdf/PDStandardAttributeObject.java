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
package org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDAttributeObject;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;

/**
 * A standard attribute object.
 * 
 * @author Johannes Koch
 */
public abstract class PDStandardAttributeObject extends PDAttributeObject
{
    /**
     * An "unspecified" default float value.
     */
    protected static final float UNSPECIFIED = -1.f;

    /**
     * Default constructor.
     */
    public PDStandardAttributeObject()
    {
    }

    /**
     * Creates a new standard attribute object with a given dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDStandardAttributeObject(final COSDictionary dictionary)
    {
        super(dictionary);
    }


    /**
     * Is the attribute with the given name specified in this attribute object?
     * 
     * @param name the attribute name
     * @return <code>true</code> if the attribute is specified,
     * <code>false</code> otherwise
     */
    public boolean isSpecified(final String name)
    {
        return this.getCOSObject().getDictionaryObject(name) != null;
    }


    /**
     * Gets a string attribute value.
     * 
     * @param name the attribute name
     * @return the string attribute value
     */
    protected String getString(final String name)
    {
        return this.getCOSObject().getString(name);
    }

    /**
     * Sets a string attribute value.
     * 
     * @param name the attribute name
     * @param value the string attribute value
     */
    protected void setString(final String name, final String value)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setString(name, value);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Gets an array of strings.
     * 
     * @param name the attribute name
     * @return the array of strings
     */
    protected String[] getArrayOfString(final String name)
    {
        final COSBase v = this.getCOSObject().getDictionaryObject(name);
        if (v instanceof COSArray)
        {
            final COSArray array = (COSArray) v;
            final String[] strings = new String[array.size()];
            for (int i = 0; i < array.size(); i++)
            {
                strings[i] = ((COSName) array.getObject(i)).getName();
            }
            return strings;
        }
        return null;
    }

    /**
     * Sets an array of strings.
     * 
     * @param name the attribute name
     * @param values the array of strings
     */
    protected void setArrayOfString(final String name, final String[] values)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        final COSArray array = new COSArray();
        for (final String value : values)
        {
            array.add(new COSString(value));
        }
        this.getCOSObject().setItem(name, array);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Gets a name value.
     * 
     * @param name the attribute name
     * @return the name value
     */
    protected String getName(final String name)
    {
        return this.getCOSObject().getNameAsString(name);
    }

    /**
     * Gets a name value.
     * 
     * @param name the attribute name
     * @param defaultValue the default value
     * @return the name value
     */
    protected String getName(final String name, final String defaultValue)
    {
        return this.getCOSObject().getNameAsString(name, defaultValue);
    }

    /**
     * Gets a name value or array of name values.
     * 
     * @param name the attribute name
     * @param defaultValue the default value
     * @return a String or array of Strings
     */
    protected Object getNameOrArrayOfName(final String name, final String defaultValue)
    {
        final COSBase v = this.getCOSObject().getDictionaryObject(name);
        if (v instanceof COSArray)
        {
            final COSArray array = (COSArray) v;
            final String[] names = new String[array.size()];
            for (int i = 0; i < array.size(); i++)
            {
                final COSBase item = array.getObject(i);
                if (item instanceof COSName)
                {
                    names[i] = ((COSName) item).getName();
                }
            }
            return names;
        }
        if (v instanceof COSName)
        {
            return ((COSName) v).getName();
        }
        return defaultValue;
    }

    /**
     * Sets a name value.
     * 
     * @param name the attribute name
     * @param value the name value
     */
    protected void setName(final String name, final String value)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setName(name, value);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Sets an array of name values.
     * 
     * @param name the attribute name
     * @param values the array of name values
     */
    protected void setArrayOfName(final String name, final String[] values)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        final COSArray array = new COSArray();
        for (final String value : values)
        {
            array.add(COSName.getPDFName(value));
        }
        this.getCOSObject().setItem(name, array);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Gets a number or a name value.
     * 
     * @param name the attribute name
     * @param defaultValue the default name
     * @return a Float or a String
     */
    protected Object getNumberOrName(final String name, final String defaultValue)
    {
        final COSBase value = this.getCOSObject().getDictionaryObject(name);
        if (value instanceof COSNumber)
        {
            return ((COSNumber) value).floatValue();
        }
        if (value instanceof COSName)
        {
            return ((COSName) value).getName();
        }
        return defaultValue;
    }

    /**
     * Gets an integer.
     * 
     * @param name the attribute name
     * @param defaultValue the default value
     * @return the integer
     */
    protected int getInteger(final String name, final int defaultValue)
    {
        return this.getCOSObject().getInt(name, defaultValue);
    }

    /**
     * Sets an integer.
     * 
     * @param name the attribute name
     * @param value the integer
     */
    protected void setInteger(final String name, final int value)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setInt(name, value);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Gets a number value.
     * 
     * @param name the attribute name
     * @param defaultValue the default value
     * @return the number value
     */
    protected float getNumber(final String name, final float defaultValue)
    {
        return this.getCOSObject().getFloat(name, defaultValue);
    }

    /**
     * Gets a number value.
     * 
     * @param name the attribute name
     * @return the number value
     */
    protected float getNumber(final String name)
    {
        return this.getCOSObject().getFloat(name);
    }

    /**
     * Gets a number or an array of numbers.
     * 
     * @param name the attribute name
     * @param defaultValue the default value
     * @return a Float or an array of floats
     */
    protected Object getNumberOrArrayOfNumber(final String name, final float defaultValue)
    {
        final COSBase v = this.getCOSObject().getDictionaryObject(name);
        if (v instanceof COSArray)
        {
            final COSArray array = (COSArray) v;
            final float[] values = new float[array.size()];
            for (int i = 0; i < array.size(); i++)
            {
                final COSBase item = array.getObject(i);
                if (item instanceof COSNumber)
                {
                    values[i] = ((COSNumber) item).floatValue();
                }
            }
            return values;
        }
        if (v instanceof COSNumber)
        {
            return ((COSNumber) v).floatValue();
        }
        if (Float.compare(defaultValue, UNSPECIFIED) == 0)
        {
            return null;
        }
        return defaultValue;
    }

    /**
     * Sets a float number.
     * 
     * @param name the attribute name
     * @param value the float number
     */
    protected void setNumber(final String name, final float value)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setFloat(name, value);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Sets an integer number.
     * 
     * @param name the attribute name
     * @param value the integer number
     */
    protected void setNumber(final String name, final int value)
    {
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setInt(name, value);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Sets an array of float numbers.
     * 
     * @param name the attribute name
     * @param values the float numbers
     */
    protected void setArrayOfNumber(final String name, final float[] values)
    {
        final COSArray array = new COSArray();
        for (final float value : values)
        {
            array.add(new COSFloat(value));
        }
        final COSBase oldBase = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setItem(name, array);
        final COSBase newBase = this.getCOSObject().getDictionaryObject(name);
        this.potentiallyNotifyChanged(oldBase, newBase);
    }

    /**
     * Gets a colour.
     * 
     * @param name the attribute name
     * @return the colour
     */
    protected PDGamma getColor(final String name)
    {
        final COSArray c = (COSArray) this.getCOSObject().getDictionaryObject(name);
        if (c != null)
        {
            return new PDGamma(c);
        }
        return null;
    }

    /**
     * Gets a single colour or four colours.
     * 
     * @param name the attribute name
     * @return the single ({@link PDGamma}) or a ({@link PDFourColours})
     */
    protected Object getColorOrFourColors(final String name)
    {
        final COSArray array =
            (COSArray) this.getCOSObject().getDictionaryObject(name);
        if (array == null)
        {
            return null;
        }
        if (array.size() == 3)
        {
            // only one colour
            return new PDGamma(array);
        }
        else if (array.size() == 4)
        {
            return new PDFourColours(array);
        }
        return null;
    }

    /**
     * Sets a colour.
     * 
     * @param name the attribute name
     * @param value the colour
     */
    protected void setColor(final String name, final PDGamma value)
    {
        final COSBase oldValue = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setItem(name, value);
        final COSBase newValue = value == null ? null : value.getCOSObject();
        this.potentiallyNotifyChanged(oldValue, newValue);
    }

    /**
     * Sets four colours.
     * 
     * @param name the attribute name
     * @param value the four colours
     */
    protected void setFourColors(final String name, final PDFourColours value)
    {
        final COSBase oldValue = this.getCOSObject().getDictionaryObject(name);
        this.getCOSObject().setItem(name, value);
        final COSBase newValue = value == null ? null : value.getCOSObject();
        this.potentiallyNotifyChanged(oldValue, newValue);
    }

}
