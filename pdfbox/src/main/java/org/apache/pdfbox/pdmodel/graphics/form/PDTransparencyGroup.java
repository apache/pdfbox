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

package org.apache.pdfbox.pdmodel.graphics.form;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.ResourceCache;
import org.apache.pdfbox.pdmodel.common.PDStream;

/**
 * A transparency group.
 *
 * @author John Hewson
 */
public class PDTransparencyGroup extends PDFormXObject
{
    /**
     * Creates a Transparency Group for reading.
     * @param stream The XObject stream
     */
    public PDTransparencyGroup(PDStream stream)
    {
        super(stream);
    }

    /**
     * Creates a Transparency Group for reading.
     * @param stream The XObject stream
     */
    public PDTransparencyGroup(COSStream stream, ResourceCache cache)
    {
        super(stream, cache);
    }    

    /**
     * Creates a Transparency Group for writing, in the given document.
     * @param document The current document
     */
    public PDTransparencyGroup(PDDocument document)
    {
        super(document);
        // todo: set mandatory fields
    }
}
