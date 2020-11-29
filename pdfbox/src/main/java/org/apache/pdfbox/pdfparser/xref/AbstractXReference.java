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
 * An extending class represents an entry, as it can be found in a PDF's crossreference stream ({@link PDFXRefStream}).
 * Such an entry shall locate a PDF object/resource in a PDF document.
 * 
 * @author Christian Appl
 */
public abstract class AbstractXReference implements XReferenceEntry
{

    private final XReferenceType type;

    /**
     * Creates a crossreference stream entry of the given {@link XReferenceType}.
     *
     * @param type The {@link XReferenceType} of the crossreference stream entry.
     */
    protected AbstractXReference(XReferenceType type)
    {
        this.type = type;
    }

    /**
     * Returns the {@link XReferenceType} of this crossreference stream entry.
     *
     * @return The {@link XReferenceType} of this crossreference stream entry.
     */
    @Override
    public XReferenceType getType()
    {
        return type;
    }

    /**
     * Returns the value for the first column of the crossreference stream entry. (The numeric representation of this
     * entry's (The numeric representation of this entry's {@link XReferenceType}.)
     *
     * @return The value for the first column of the crossreference stream entry.
     */
    @Override
    public long getFirstColumnValue()
    {
        return getType().getNumericValue();
    }

    /**
     * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
     * as this object is less than, equal to, or greater than the specified object.
     *
     * @param xReferenceEntry the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than
     * the specified object.
     */
    @Override
    public int compareTo(XReferenceEntry xReferenceEntry)
    {
        if (getReferencedKey() == null)
        {
            return -1;
        }
        else if (xReferenceEntry == null || xReferenceEntry.getReferencedKey() == null)
        {
            return 1;
        }

        return getReferencedKey().compareTo(xReferenceEntry.getReferencedKey());
    }
}
