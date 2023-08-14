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
package org.apache.pdfbox.pdfparser.xref;

import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * An instance of this class represents a type for a {@link XReferenceEntry}, as it can be found in a PDF's
 * {@link PDFXRefStream}.
 * 
 * @author Christian Appl
 */
public enum XReferenceType
{

    FREE(0), NORMAL(1), OBJECT_STREAM_ENTRY(2);

    private final int numericValue;

    /**
     * Represents a type for a {@link XReferenceEntry}, as it can be found in a PDF's {@link PDFXRefStream}.
     *
     * @param numericValue The numeric representation of this type.
     */
    XReferenceType(int numericValue)
    {
        this.numericValue = numericValue;
    }

    /**
     * Returns the numeric representation of this type.
     *
     * @return The numeric representation of this type.
     */
    public int getNumericValue()
    {
        return numericValue;
    }
}
