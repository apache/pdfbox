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

package org.apache.xmpbox.type;

import java.io.IOException;
import java.util.Calendar;

import org.apache.xmpbox.DateConverter;
import org.apache.xmpbox.XMPMetadata;

/**
 * Object representation of a Date XMP type
 * 
 * @author a183132
 * 
 */
public class DateType extends AbstractSimpleProperty
{

    private Calendar dateValue;

    /**
     * Property Date type constructor (namespaceURI is given)
     * 
     * @param metadata
     *            The metadata to attach to this property
     * @param namespaceURI
     *            the namespace URI to associate to this property
     * @param prefix
     *            The prefix to set for this property
     * @param propertyName
     *            The local Name of this property
     * @param value
     *            The value to set for this property
     */
    public DateType(final XMPMetadata metadata, final String namespaceURI, final String prefix, final String propertyName, final Object value)
    {
        super(metadata, namespaceURI, prefix, propertyName, value);
    }

    /**
     * Set property value
     *
     * @param value the new Calendar element value
     */
    private void setValueFromCalendar(final Calendar value)
    {
        dateValue = value;
    }

    /**
     * return the property value
     * 
     * @return boolean
     */
    @Override
    public Calendar getValue()
    {
        return dateValue;
    }

    /**
     * Check if the value has a type which can be understood
     * 
     * @param value
     *            Object value to check
     * @return True if types are compatibles
     */
    private boolean isGoodType(final Object value)
    {
        if (value instanceof Calendar)
        {
            return true;
        }
        else if (value instanceof String)
        {
            try
            {
                DateConverter.toCalendar((String) value);
                return true;
            }
            catch (IOException e)
            {
                return false;
            }
        }
        return false;
    }

    /**
     * Set value of this property
     * 
     * @param value
     *            The value to set
     */
    @Override
    public void setValue(final Object value)
    {
        if (!isGoodType(value))
        {
            if (value == null)
            {
                throw new IllegalArgumentException(
                        "Value null is not allowed for the Date type");
            }
            throw new IllegalArgumentException(
                    "Value given is not allowed for the Date type: " 
                            + value.getClass() + ", value: " + value);
        }
        else
        {
            // if string object
            if (value instanceof String)
            {
                setValueFromString((String) value);
            }
            else
            {
                // if Calendar
                setValueFromCalendar((Calendar) value);
            }

        }

    }

    @Override
    public String getStringValue()
    {
        return DateConverter.toISO8601(dateValue);
    }

    /**
     * Set the property value with a String
     * 
     * @param value
     *            The String value
     */
    private void setValueFromString(final String value)
    {
        try
        {
            setValueFromCalendar(DateConverter.toCalendar(value));
        }
        catch (IOException e)
        {
            // SHOULD NEVER HAPPEN
            // STRING HAS BEEN CHECKED BEFORE
            throw new IllegalArgumentException(e);
        }

    }

}
