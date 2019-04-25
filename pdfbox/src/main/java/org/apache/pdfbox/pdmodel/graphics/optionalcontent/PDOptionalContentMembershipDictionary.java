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
import java.util.List;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.documentinterchange.markedcontent.PDPropertyList;

/**
 * An optional content membership dictionary (OCMD).
 *
 * @author Tilman Hausherr
 */
public class PDOptionalContentMembershipDictionary extends PDPropertyList
{
    /**
     * Creates a new optional content membership dictionary (OCMD).
     */
    public PDOptionalContentMembershipDictionary()
    {
        this.dict.setItem(COSName.TYPE, COSName.OCMD);
    }

    /**
     * Creates a new instance based on a given {@link COSDictionary}.
     * @param dict the dictionary
     */
    public PDOptionalContentMembershipDictionary(COSDictionary dict)
    {
        super(dict);
        if (!dict.getItem(COSName.TYPE).equals(COSName.OCMD))
        {
            throw new IllegalArgumentException(
                    "Provided dictionary is not of type '" + COSName.OCMD + "'");
        }
    }

    /**
     * Get a list of optional content groups.
     * 
     * @return List of optional content groups, never null.
     */
    public List<PDPropertyList> getOCGs()
    {
        List<PDPropertyList> list = new ArrayList<PDPropertyList>();
        COSBase base = dict.getDictionaryObject(COSName.OCGS);
        if (base instanceof COSDictionary)
        {
            list.add(PDPropertyList.create((COSDictionary) base));
        }
        else if (base instanceof COSArray)
        {
            COSArray ar = (COSArray) base;
            for (int i = 0; i < ar.size(); ++i)
            {
                COSBase elem = ar.getObject(i);
                if (elem instanceof COSDictionary)
                {
                    list.add(PDPropertyList.create((COSDictionary) elem));
                }
            }
        }
        return list;
    }

    /**
     * Set optional content groups as a list.
     * 
     * @param ocgs list of optional content groups to set.
     */
    public void setOCGs(List<PDPropertyList> ocgs)
    {
        COSArray ar = new COSArray();
        for (PDPropertyList prop : ocgs)
        {
            ar.add(prop);
        }
        dict.setItem(COSName.OCGS, ar);
    }

    /**
     * Get the visibility policy name. Valid names are AllOff, AllOn, AnyOff, AnyOn (default).
     *
     * @return the visibility policy, never null.
     */
    public COSName getVisibilityPolicy()
    {
        return dict.getCOSName(COSName.P, COSName.ANY_ON);
    }

    /**
     * Sets the visibility policy name. Valid names are AllOff, AllOn, AnyOff, AnyOn (default).
     * @param visibilityPolicy 
     */
    public void setVisibilityPolicy(COSName visibilityPolicy)
    {
        dict.setItem(COSName.P, visibilityPolicy);
    }
    
    //TODO support /VE some day
}
