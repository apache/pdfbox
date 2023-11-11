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

import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSObjectKey;

/**
 * Represents an abstract view of the cross references of a pdf.
 *
 */
public class XrefEntry
{
    private final int index;
    private final COSObjectKey key;
    private final long offset;
    private final COSObject cosObject;
    
    public XrefEntry(int index, COSObjectKey key, long offset, COSObject cosObject)
    {
        this.index = index;
        this.key = key;
        this.offset = offset;
        this.cosObject = cosObject;
    }
    
    public COSObjectKey getKey()
    {
        return key;
    }

    public int getIndex()
    {
        return index;
    }

    public COSObject getCOSObject()
    {
        return cosObject;
    }

    public COSBase getObject()
    {
        return cosObject != null ? cosObject.getObject() : null;
    }

    public String getPath()
    {
        return XrefEntries.PATH + "/" + toString();
    }

    @Override
    public String toString()
    {
        if (key == null)
        {
            return "(null)";
        }
        return offset >= 0 ? //
                "Offset: " + offset + " [" + key + "]" : //
                "Compressed object stream: " + (-offset) + " [" + key + "]";
    }
}
