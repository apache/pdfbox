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

package org.apache.pdfbox.pdmodel.interactive.annotation;

import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

/**
 * An appearance stream is a form XObject, a self-contained content stream that shall be rendered inside the annotation
 * rectangle.
 *
 * @author Ben Litchfield
 * @author John Hewson
 */
public class PDAppearanceStream extends PDFormXObject
{
    /**
     * Creates a Form XObject for reading.
     * 
     * @param stream The XObject stream
     */
    public PDAppearanceStream(COSStream stream)
    {
        super(stream);
    }

    /**
     * Creates a Form Image XObject for writing, in the given document.
     * 
     * @param document The current document
     */
    public PDAppearanceStream(PDDocument document)
    {
        super(document);
    }
}
