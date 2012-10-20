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
 * A PrintField attribute object.
 * 
 * @author <a href="mailto:Johannes%20Koch%20%3Ckoch@apache.org%3E">Johannes Koch</a>
 * @version $Revision: $
 */
public class PDPrintFieldAttributeObject extends PDStandardAttributeObject
{

    /**
     * standard attribute owner: PrintField
     */
    public static final String OWNER_PRINT_FIELD = "PrintField";

    private static final String ROLE = "Role";
    private static final String CHECKED = "checked";
    private static final String DESC = "Desc";

    /**
     * role: rb: Radio button
     */
    public static final String ROLE_RB = "rb";
    /**
     * role: cb: Check box
     */
    public static final String ROLE_CB = "cb";
    /**
     * role: pb: Push button
     */
    public static final String ROLE_PB = "pb";
    /**
     * role: tv: Text-value field
     */
    public static final String ROLE_TV = "tv";
    /**
     * checked state: on
     */
    public static final String CHECKED_STATE_ON = "on";
    /**
     * checked state: off
     */
    public static final String CHECKED_STATE_OFF = "off";
    /**
     * checked state: neutral
     */
    public static final String CHECKED_STATE_NEUTRAL = "neutral";


    /**
     * Default constructor.
     */
    public PDPrintFieldAttributeObject()
    {
        this.setOwner(OWNER_PRINT_FIELD);
    }

    /**
     * Creates a new PrintField attribute object with a given dictionary.
     * 
     * @param dictionary the dictionary
     */
    public PDPrintFieldAttributeObject(COSDictionary dictionary)
    {
        super(dictionary);
    }


    /**
     * Gets the role.
     * 
     * @return the role
     */
    public String getRole()
    {
        return this.getName(ROLE);
    }

    /**
     * Sets the role. The value of Role shall be one of the following:
     * <ul>
     *   <li>{@link #ROLE_RB},</li>
     *   <li>{@link #ROLE_CB},</li>
     *   <li>{@link #ROLE_PB},</li>
     *   <li>{@link #ROLE_TV}.</li>
     * </ul>
     * 
     * @param role the role
     */
    public void setRole(String role)
    {
        this.setName(ROLE, role);
    }

    /**
     * Gets the checked state. The default value is {@link #CHECKED_STATE_OFF}.
     * 
     * @return the checked state
     */
    public String getCheckedState()
    {
        return this.getName(CHECKED, CHECKED_STATE_OFF);
    }

    /**
     * Sets the checked state. The value shall be one of:
     * <ul>
     *   <li>{@link #CHECKED_STATE_ON},</li>
     *   <li>{@link #CHECKED_STATE_OFF} (default), or</li>
     *   <li>{@link #CHECKED_STATE_NEUTRAL}.</li>
     * </ul>
     * 
     * @param checkedState the checked state
     */
    public void setCheckedState(String checkedState)
    {
        this.setName(CHECKED, checkedState);
    }

    /**
     * Gets the alternate name of the field (Desc).
     * 
     * @return the alternate name of the field
     */
    public String getAlternateName()
    {
        return this.getString(DESC);
    }

    /**
     * Sets the alternate name of the field (Desc).
     * 
     * @param alternateName the alternate name of the field
     */
    public void setAlternateName(String alternateName)
    {
        this.setString(DESC, alternateName);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder().append(super.toString());
        if (this.isSpecified(ROLE))
        {
            sb.append(", Role=").append(this.getRole());
        }
        if (this.isSpecified(CHECKED))
        {
            sb.append(", Checked=").append(this.getCheckedState());
        }
        if (this.isSpecified(DESC))
        {
            sb.append(", Desc=").append(this.getAlternateName());
        }
        return sb.toString();
    }

}
