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
package org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This represents an outline in a pdf document.
 *
 * @author Ben Litchfield
 */
public final class PDDocumentOutline extends PDOutlineNode
{

    /**
     * Default Constructor.
     */
    public PDDocumentOutline()
    {
        getCOSObject().setName(COSName.TYPE, COSName.OUTLINES.getName());
    }

    /**
     * Constructor for an existing document outline.
     *
     * @param dic The storage dictionary.
     */
    public PDDocumentOutline( COSDictionary dic )
    {
        super( dic );
        getCOSObject().setName(COSName.TYPE, COSName.OUTLINES.getName());
    }

    @Override
    public boolean isNodeOpen()
    {
        return true;
    }

    @Override
    public void openNode()
    {
        // The root of the outline hierarchy is not an OutlineItem and cannot be opened or closed
    }

    @Override
    public void closeNode()
    {
        // The root of the outline hierarchy is not an OutlineItem and cannot be opened or closed
    }
}
