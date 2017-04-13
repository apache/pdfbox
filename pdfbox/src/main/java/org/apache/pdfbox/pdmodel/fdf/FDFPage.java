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
package org.apache.pdfbox.pdmodel.fdf;

import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents an FDF page that is part of the FDF document.
 *
 * @author Ben Litchfield
 */
public class FDFPage implements COSObjectable
{
    private final COSDictionary page;

    /**
     * Default constructor.
     */
    public FDFPage()
    {
        page = new COSDictionary();
    }

    /**
     * Constructor.
     *
     * @param p The FDF page.
     */
    public FDFPage(COSDictionary p)
    {
        page = p;
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    @Override
    public COSDictionary getCOSObject()
    {
        return page;
    }

    /**
     * This will get a list of FDFTemplage objects that describe the named pages that serve as templates.
     *
     * @return A list of templates.
     */
    public List<FDFTemplate> getTemplates()
    {
        List<FDFTemplate> retval = null;
        COSArray array = (COSArray) page.getDictionaryObject(COSName.TEMPLATES);
        if (array != null)
        {
            List<FDFTemplate> objects = new ArrayList<>();
            for (int i = 0; i < array.size(); i++)
            {
                objects.add(new FDFTemplate((COSDictionary) array.getObject(i)));
            }
            retval = new COSArrayList<>(objects, array);
        }
        return retval;
    }

    /**
     * A list of FDFTemplate objects.
     *
     * @param templates A list of templates for this Page.
     */
    public void setTemplates(List<FDFTemplate> templates)
    {
        page.setItem(COSName.TEMPLATES, COSArrayList.converterToCOSArray(templates));
    }

    /**
     * This will get the FDF page info object.
     *
     * @return The Page info.
     */
    public FDFPageInfo getPageInfo()
    {
        FDFPageInfo retval = null;
        COSDictionary dict = (COSDictionary) page.getDictionaryObject(COSName.INFO);
        if (dict != null)
        {
            retval = new FDFPageInfo(dict);
        }
        return retval;
    }

    /**
     * This will set the page info.
     *
     * @param info The new page info dictionary.
     */
    public void setPageInfo(FDFPageInfo info)
    {
        page.setItem(COSName.INFO, info);
    }
}
