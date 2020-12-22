/*
 * Copyright 2018 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;

/**
 *
 * @author Tilman Hausherr
 */
public final class PDFormContentStream extends PDAbstractContentStream
{
    /**
     * Create a new form XObject content stream.
     *
     * @param form The form XObject stream to write to.
     * 
     * @throws IOException If there is an error writing to the form contents.
     */
    public PDFormContentStream(final PDFormXObject form) throws IOException
    {
        super(null, form.getContentStream().createOutputStream(), form.getResources());
    }
}
