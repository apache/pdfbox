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
package org.apache.pdfbox.cos;

import java.io.IOException;

import org.apache.pdfbox.io.RandomAccessReadView;

public interface ICOSParser
{

    /**
     * Dereference the COSBase object which is referenced by the given COSObject.
     * 
     * @param obj the COSObject which references the COSBase object to be dereferenced.
     * @return the referenced object
     * @throws IOException if something went wrong when dereferencing the COSBase object
     */
    COSBase dereferenceCOSObject(COSObject obj) throws IOException;

    /**
     * Creates a random access read view starting at the given position with the given length.
     * 
     * @param startPosition start position within the underlying random access read
     * @param streamLength stream length
     * @return the random access read view
     * @throws IOException if something went wrong when creating the view for the RandomAccessRead
     */
    RandomAccessReadView createRandomAccessReadView(long startPosition, long streamLength)
            throws IOException;

}
