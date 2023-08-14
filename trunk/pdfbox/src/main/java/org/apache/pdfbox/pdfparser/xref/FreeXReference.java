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

import org.apache.pdfbox.cos.COSObjectKey;
import org.apache.pdfbox.pdfparser.PDFXRefStream;

/**
 * A class representing a free reference in a PDF's crossreference stream ({@link PDFXRefStream}).
 * 
 * @author Christian Appl
 */
public class FreeXReference extends AbstractXReference
{

    public static final FreeXReference NULL_ENTRY = //
            new FreeXReference(new COSObjectKey(0, 65535), 0);
    private final COSObjectKey key;
    private final long nextFreeObject;

    /**
     * Sets the given {@link COSObjectKey} as a free reference in a PDF's crossreference stream ({@link PDFXRefStream}).
     *
     * @param key The key, that shall be set as the free reference of the document.
     * @param nextFreeObject The object number of the next free object.
     */
    public FreeXReference(COSObjectKey key, long nextFreeObject)
    {
        super(XReferenceType.FREE);
        this.key = key;
        this.nextFreeObject = nextFreeObject;
    }

    /**
     * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     *
     * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     */
    @Override
    public COSObjectKey getReferencedKey()
    {
        return key;
    }

    /**
     * Returns the value for the second column of the crossreference stream entry. (This is the object number of the set
     * next free {@link COSObjectKey} - for entries of this type.)
     *
     * @return The value for the second column of the crossreference stream entry.
     */
    @Override
    public long getSecondColumnValue()
    {
        return nextFreeObject;
    }

    /**
     * Returns the value for the third column of the crossreference stream entry. (This is the generation number of the
     * set next free {@link COSObjectKey} - for entries of this type.)
     *
     * @return The value for the third column of the crossreference stream entry.
     */
    @Override
    public long getThirdColumnValue()
    {
        return getReferencedKey().getGeneration();
    }

    /**
     * Returns a string representation of this crossreference stream entry.
     *
     * @return A string representation of this crossreference stream entry.
     */
    @Override
    public String toString()
    {
        return "FreeReference{" + "key=" + key + ", nextFreeObject=" + nextFreeObject + ", type="
                + getType().getNumericValue() + " }";
    }
}
