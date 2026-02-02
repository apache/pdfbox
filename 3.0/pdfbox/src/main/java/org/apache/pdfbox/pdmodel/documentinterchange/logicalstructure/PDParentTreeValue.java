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
package org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.common.COSObjectable;

/**
 * This is described in the PDF specification in the <b>ParentTree</b> segment of the table "Entries
 * in the structure tree root". This is either a dictionary or an array. It's a dictionary for
 * individual objects like an annotation or an XObject, and an array for a page object or a content
 * stream containing marked-content sequences identified by an MCID. The index in the array is
 * identical to the MCID. You can pass null if the MCIDs are not continous.
 *
 * @author Tilman Hausherr
 */
public class PDParentTreeValue implements COSObjectable
{
    final COSObjectable obj;

    public PDParentTreeValue(COSArray obj)
    {
        this.obj = obj;
    }

    public PDParentTreeValue(COSDictionary obj)
    {
        this.obj = obj;
    }

    @Override
    public COSBase getCOSObject()
    {
        return obj.getCOSObject();
    }

    @Override
    public String toString()
    {
        return obj.toString();
    }
}
