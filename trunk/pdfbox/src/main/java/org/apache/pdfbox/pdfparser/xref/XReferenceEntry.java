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
 * An implementing class represents an entry, as it can be found in a PDF's crossreference stream
 * ({@link PDFXRefStream}). Such an entry shall locate a PDF object/resource in a PDF document.
 * 
 * @author Christian Appl
 */
public interface XReferenceEntry extends Comparable<XReferenceEntry>
{

    /**
     * Returns the {@link XReferenceType} of this crossreference stream entry.
     *
     * @return The {@link XReferenceType} of this crossreference stream entry.
     */
    XReferenceType getType();

    /**
     * Returns the {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     *
     * @return The {@link COSObjectKey} of the object, that is described by this crossreference stream entry.
     */
    COSObjectKey getReferencedKey();

    /**
     * Returns the value for the first column of the crossreference stream entry. (The numeric representation of this
     * entry's {@link XReferenceType}.)
     *
     * @return The value for the first column of the crossreference stream entry.
     */
    long getFirstColumnValue();

    /**
     * Returns the value for the second column of the crossreference stream entry. (It's meaning depends on the
     * {@link XReferenceType} of this entry.)
     *
     * @return The value for the second column of the crossreference stream entry.
     */
    long getSecondColumnValue();

    /**
     * Returns the value for the third column of the crossreference stream entry. (It's meaning depends on the
     * {@link XReferenceType} of this entry.)
     *
     * @return The value for the third column of the crossreference stream entry.
     */
    long getThirdColumnValue();
}
