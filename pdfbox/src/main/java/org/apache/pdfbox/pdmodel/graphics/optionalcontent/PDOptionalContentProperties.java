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
package org.apache.pdfbox.pdmodel.graphics.optionalcontent;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This class represents the optional content properties dictionary.
 *
 * @since PDF 1.5
 */
public class PDOptionalContentProperties implements COSObjectable
{

    /**
     * Enumeration for the BaseState dictionary entry on the "D" dictionary.
     */
    public enum BaseState
    {

        /** The "ON" value. */
        ON(COSName.ON),
        /** The "OFF" value. */
        OFF(COSName.OFF),
        /** The "Unchanged" value. */
        UNCHANGED(COSName.UNCHANGED);

        private final COSName name;

        private BaseState(COSName value)
        {
            this.name = value;
        }

        /**
         * Returns the PDF name for the state.
         * @return the name of the state
         */
        public COSName getName()
        {
            return this.name;
        }

        /**
         * Returns the base state represented by the given {@link COSName}.
         * @param state the state name
         * @return the state enum value
         */
        public static BaseState valueOf(COSName state)
        {
            if (state == null)
            {
                return BaseState.ON;
            }
            return BaseState.valueOf(state.getName().toUpperCase());
        }

    }

    private final COSDictionary dict;

    /**
     * Creates a new optional content properties dictionary.
     */
    public PDOptionalContentProperties()
    {
        this.dict = new COSDictionary();
        this.dict.setItem(COSName.OCGS, new COSArray());
        COSDictionary d = new COSDictionary();

        // Name optional but required for PDF/A-3
        d.setString(COSName.NAME, "Top");

        this.dict.setItem(COSName.D, d);
    }

    /**
     * Creates a new instance based on a given {@link COSDictionary}.
     * @param props the dictionary
     */
    public PDOptionalContentProperties(COSDictionary props)
    {
        this.dict = props;
    }

    /** {@inheritDoc} */
    @Override
    public COSBase getCOSObject()
    {
        return this.dict;
    }

    private COSArray getOCGs()
    {
        COSArray ocgs = this.dict.getCOSArray(COSName.OCGS);
        if (ocgs == null)
        {
            ocgs = new COSArray();
            this.dict.setItem(COSName.OCGS, ocgs); //OCGs is required
        }
        return ocgs;
    }

    private COSDictionary getD()
    {
        COSBase base = this.dict.getDictionaryObject(COSName.D);
        if (base instanceof COSDictionary)
        {
            return (COSDictionary) base;
        }

        COSDictionary d = new COSDictionary();

        // Name optional but required for PDF/A-3
        d.setString(COSName.NAME, "Top");

        // D is required
        this.dict.setItem(COSName.D, d);        

        return d;
    }

    /**
     * Returns the first optional content group of the given name.
     *
     * @param name the group name
     * @return the optional content group or null, if there is no such group
     */
    public PDOptionalContentGroup getGroup(String name)
    {
        COSArray ocgs = getOCGs();
        for (COSBase o : ocgs)
        {
            COSDictionary ocg = toDictionary(o);
            String groupName = ocg.getString(COSName.NAME);
            if (groupName.equals(name))
            {
                return new PDOptionalContentGroup(ocg);
            }
        }
        return null;
    }

    /**
     * Adds an optional content group (OCG).
     * @param ocg the optional content group
     */
    public void addGroup(PDOptionalContentGroup ocg)
    {
        COSArray ocgs = getOCGs();
        ocgs.add(ocg.getCOSObject());

        //By default, add new group to the "Order" entry so it appears in the user interface
        COSArray order = (COSArray)getD().getDictionaryObject(COSName.ORDER);
        if (order == null)
        {
            order = new COSArray();
            getD().setItem(COSName.ORDER, order);
        }
        order.add(ocg);
    }

    /**
     * Returns the collection of all optional content groups.
     * @return the optional content groups
     */
    public Collection<PDOptionalContentGroup> getOptionalContentGroups()
    {
        Collection<PDOptionalContentGroup> coll = new ArrayList<PDOptionalContentGroup>();
        COSArray ocgs = getOCGs();
        for (COSBase base : ocgs)
        {
            coll.add(new PDOptionalContentGroup(toDictionary(base)));
        }
        return coll;
    }

    /**
     * Returns the base state for optional content groups.
     * @return the base state
     */
    public BaseState getBaseState()
    {
        COSDictionary d = getD();
        COSName name = (COSName)d.getItem(COSName.BASE_STATE);
        return BaseState.valueOf(name);
    }

    /**
     * Sets the base state for optional content groups.
     * @param state the base state
     */
    public void setBaseState(BaseState state)
    {
        COSDictionary d = getD();
        d.setItem(COSName.BASE_STATE, state.getName());
    }

    /**
     * Lists all optional content group names.
     * @return an array of all names
     */
    public String[] getGroupNames()
    {
        COSArray ocgs = (COSArray)dict.getDictionaryObject(COSName.OCGS);
        int size = ocgs.size();
        String[] groups = new String[size];
        for (int i = 0; i < size; i++)
        {
            COSBase obj = ocgs.get(i);
            COSDictionary ocg = toDictionary(obj);
            groups[i] = ocg.getString(COSName.NAME);
        }
        return groups;
    }

    /**
     * Indicates whether a particular optional content group is found in the PDF file.
     * @param groupName the group name
     * @return true if the group exists, false otherwise
     */
    public boolean hasGroup(String groupName)
    {
        String[] layers = getGroupNames();
        for (String layer : layers)
        {
            if (layer.equals(groupName))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether <em>at least one</em> optional content group with this name is enabled.
     * There may be disabled optional content groups with this name even if this function returns
     * true.
     *
     * @param groupName the group name
     * @return true if at least one group is enabled
     */
    public boolean isGroupEnabled(String groupName)
    {
        boolean result = false;
        COSArray ocgs = getOCGs();
        for (COSBase o : ocgs)
        {
            COSDictionary ocg = toDictionary(o);
            String name = ocg.getString(COSName.NAME);
            if (groupName.equals(name) && isGroupEnabled(new PDOptionalContentGroup(ocg)))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Indicates whether an optional content group is enabled.
     * @param group the group object
     * @return true if the group is enabled
     */
    public boolean isGroupEnabled(PDOptionalContentGroup group)
    {
        //TODO handle Optional Content Configuration Dictionaries,
        //i.e. OCProperties/Configs

        PDOptionalContentProperties.BaseState baseState = getBaseState();
        boolean enabled = !baseState.equals(BaseState.OFF);
        //TODO What to do with BaseState.Unchanged?

        if (group == null)
        {
            return enabled;
        }

        COSDictionary d = getD();
        COSBase base = d.getDictionaryObject(COSName.ON);
        if (base instanceof COSArray)
        {
            for (COSBase o : (COSArray) base)
            {
                COSDictionary dictionary = toDictionary(o);
                if (dictionary == group.getCOSObject())
                {
                    return true;
                }
            }
        }

        base = d.getDictionaryObject(COSName.OFF);
        if (base instanceof COSArray)
        {
            for (COSBase o : (COSArray) base)
            {
                COSDictionary dictionary = toDictionary(o);
                if (dictionary == group.getCOSObject())
                {
                    return false;
                }
            }
        }

        return enabled;
    }

    private COSDictionary toDictionary(COSBase o)
    {
        if (o instanceof COSObject)
        {
            return (COSDictionary)((COSObject)o).getObject();
        }
        else
        {
            return (COSDictionary)o;
        }
    }

    /**
     * Enables or disables all optional content groups with the given name.
     *
     * @param groupName the group name
     * @param enable true to enable, false to disable
     * @return true if at least one group with this name already had an on or off setting, false
     * otherwise
     */
    public boolean setGroupEnabled(String groupName, boolean enable)
    {
        boolean result = false;
        COSArray ocgs = getOCGs();
        for (COSBase o : ocgs)
        {
            COSDictionary ocg = toDictionary(o);
            String name = ocg.getString(COSName.NAME);
            if (groupName.equals(name) && setGroupEnabled(new PDOptionalContentGroup(ocg), enable))
            {
                result = true;
            }
        }
        return result;
    }

    /**
     * Enables or disables an optional content group.
     * @param group the group object
     * @param enable true to enable, false to disable
     * @return true if the group already had an on or off setting, false otherwise
     */
    public boolean setGroupEnabled(PDOptionalContentGroup group, boolean enable)
    {
        COSArray on;
        COSArray off;

        COSDictionary d = getD();
        COSBase base = d.getDictionaryObject(COSName.ON);
        if (!(base instanceof COSArray))
        {
            on = new COSArray();
            d.setItem(COSName.ON, on);
        }
        else
        {
            on = (COSArray) base;
        }
        base = d.getDictionaryObject(COSName.OFF);
        if (!(base instanceof COSArray))
        {
            off = new COSArray();
            d.setItem(COSName.OFF, off);
        }
        else
        {
            off = (COSArray) base;
        }

        boolean found = false;
        if (enable)
        {
            for (COSBase o : off)
            {
                COSDictionary groupDictionary = toDictionary(o);
                if (groupDictionary == group.getCOSObject())
                {
                    //enable group
                    off.remove(o);
                    on.add(o);
                    found = true;
                    break;
                }
            }
        }
        else
        {
            for (COSBase o : on)
            {
                COSDictionary groupDictionary = toDictionary(o);
                if (groupDictionary == group.getCOSObject())
                {
                    //disable group
                    on.remove(o);
                    off.add(o);
                    found = true;
                    break;
                }
            }
        }
        if (!found)
        {
            if (enable)
            {
                on.add(group.getCOSObject());
            }
            else
            {
                off.add(group.getCOSObject());
            }
        }
        return found;
    }
}
