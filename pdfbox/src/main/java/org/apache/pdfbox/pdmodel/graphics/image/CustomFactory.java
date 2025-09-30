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
package org.apache.pdfbox.pdmodel.graphics.image;

import java.io.IOException;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * A functional interface that allows users to define a custom strategy for converting image data as
 * a byte array into a {@link PDImageXObject}.
 */
@FunctionalInterface
public interface CustomFactory
{

    /**
     * Creates a {@link PDImageXObject} from the given image byte array and document context.
     *
     * @param document the document that shall use this PDImageXObject.
     * @param byteArray the image data as a byte array
     * @return a PDImageXObject.
     * @throws IOException if there is an error when creating the PDImageXObject.
     */
    PDImageXObject createFromByteArray(PDDocument document, byte[] byteArray) throws IOException;
}
