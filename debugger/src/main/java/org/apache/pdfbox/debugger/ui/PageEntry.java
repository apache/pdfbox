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

package org.apache.pdfbox.debugger.ui;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * Represents an abstract view of a page in the tree view.
 *
 * @author John Hewson
 */
public class PageEntry
{
    private final COSDictionary dict;
    private final int pageNum;
    private final String pageLabel;    

    public PageEntry(COSDictionary page, int pageNum, String pageLabel)
    {
        dict = page;
        this.pageNum = pageNum;
        this.pageLabel = pageLabel;
    }
    
    public COSDictionary getDict()
    {
        return dict;
    }

    public int getPageNum()
    {
        return pageNum;
    }
    
    @Override
    public String toString()
    {
        return "Page: " + pageNum + (pageLabel == null ? "" : " - " + pageLabel);
    }
    
    public String getPath()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Root/Pages");
        
        COSDictionary node = dict;
        while (node.containsKey(COSName.PARENT))
        {
            COSBase base = node.getDictionaryObject(COSName.PARENT);
            if (!(base instanceof COSDictionary))
            {
                return "";
            }
            COSDictionary parent = (COSDictionary) base;
            base = parent.getDictionaryObject(COSName.KIDS);
            if (!(base instanceof COSArray))
            {
                return "";
            }
            COSArray kids = (COSArray) base;
            int idx = kids.indexOfObject(node);
            sb.append("/Kids/[").append(idx).append("]");
            node = parent;
        }
        return sb.toString();
    }
}
