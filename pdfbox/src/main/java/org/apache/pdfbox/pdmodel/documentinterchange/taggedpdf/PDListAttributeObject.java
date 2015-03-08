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

import org.apache.pdfbox.cos.COSDictionary;

/**
 * A List attribute object.
 * 
 * @author Johannes Koch
 */
public class PDListAttributeObject extends PDStandardAttributeObject
{

    /**
     * standard attribute owner: List
     */
    public static final String OWNER_LIST = "List";

    protected static final String LIST_NUMBERING = "ListNumbering";

    /**
     * ListNumbering: Circle: Open circular bullet
     */
    public static final String LIST_NUMBERING_CIRCLE = "Circle";
    /**
     * ListNumbering: Decimal: Decimal arabic numerals (1–9, 10–99, …)
     */
    public static final String LIST_NUMBERING_DECIMAL = "Decimal";
    /**
     * ListNumbering: Disc: Solid circular bullet
     */
    public static final String LIST_NUMBERING_DISC = "Disc";
    /**
     * ListNumbering: LowerAlpha: Lowercase letters (a, b, c, …)
     */
    public static final String LIST_NUMBERING_LOWER_ALPHA = "LowerAlpha";
    /**
     * ListNumbering: LowerRoman: Lowercase roman numerals (i, ii, iii, iv, …)
     */
    public static final String LIST_NUMBERING_LOWER_ROMAN = "LowerRoman";
    /**
     * ListNumbering: None: No autonumbering; Lbl elements (if present) contain arbitrary text
     * not subject to any numbering scheme
     */
    public static final String LIST_NUMBERING_NONE = "None";
    /**
     * ListNumbering: Square: Solid square bullet
     */
    public static final String LIST_NUMBERING_SQUARE = "Square";
    /**
     * ListNumbering: UpperAlpha: Uppercase letters (A, B, C, …)
     */
    public static final String LIST_NUMBERING_UPPER_ALPHA = "UpperAlpha";
    /**
     * ListNumbering: UpperRoman: Uppercase roman numerals (I, II, III, IV, …)
     */
    public static final String LIST_NUMBERING_UPPER_ROMAN = "UpperRoman";


    /**
     * Default constructor.
     */
    public PDListAttributeObject()
    {
        this.setOwner(OWNER_LIST);
    }

    /**
     * Creates a new List attribute object with a given dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDListAttributeObject(COSDictionary dictionary)
    {
        super(dictionary);
    }


    /**
     * Gets the list numbering (ListNumbering). The default value is
     * {@link #LIST_NUMBERING_NONE}.
     * 
     * @return the list numbering
     */
    public String getListNumbering()
    {
        return this.getName(LIST_NUMBERING, LIST_NUMBERING_NONE);
    }

    /**
     * Sets the list numbering (ListNumbering). The value shall be one of the
     * following:
     * <ul>
     *   <li>{@link #LIST_NUMBERING_NONE},</li>
     *   <li>{@link #LIST_NUMBERING_DISC},</li>
     *   <li>{@link #LIST_NUMBERING_CIRCLE},</li>
     *   <li>{@link #LIST_NUMBERING_SQUARE},</li>
     *   <li>{@link #LIST_NUMBERING_DECIMAL},</li>
     *   <li>{@link #LIST_NUMBERING_UPPER_ROMAN},</li>
     *   <li>{@link #LIST_NUMBERING_LOWER_ROMAN},</li>
     *   <li>{@link #LIST_NUMBERING_UPPER_ALPHA},</li>
     *   <li>{@link #LIST_NUMBERING_LOWER_ALPHA}.</li>
     * </ul>
     * 
     * @param listNumbering the list numbering
     */
    public void setListNumbering(String listNumbering)
    {
        this.setName(LIST_NUMBERING, listNumbering);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder().append(super.toString());
        if (this.isSpecified(LIST_NUMBERING))
        {
            sb.append(", ListNumbering=").append(this.getListNumbering());
        }
        return sb.toString();
    }


}
