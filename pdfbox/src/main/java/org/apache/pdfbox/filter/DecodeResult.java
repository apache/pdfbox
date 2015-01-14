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
package org.apache.pdfbox.filter;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.graphics.color.PDJPXColorSpace;

/**
 * The result of a filter decode operation. Allows information such as color space to be
 * extracted from image streams, and for stream parameters to be repaired during reading.
 *
 * @author John Hewson
 */
public final class DecodeResult
{
    /** Default decode result. */
    public static final DecodeResult DEFAULT = new DecodeResult(new COSDictionary());

    private final COSDictionary parameters;
    private PDJPXColorSpace colorSpace;

    DecodeResult(COSDictionary parameters)
    {
        this.parameters = parameters;
    }

    DecodeResult(COSDictionary parameters, PDJPXColorSpace colorSpace)
    {
        this.parameters = parameters;
        this.colorSpace = colorSpace;
    }

    /**
     * Returns the stream parameters, repaired using the embedded stream data.
     * @return the repaired stream parameters, or an empty dictionary
     */
    public COSDictionary getParameters()
    {
        return parameters;
    }

    /**
     * Returns the embedded JPX color space, if any.
     * @return the the embedded JPX color space, or null if there is none.
     */
    public PDJPXColorSpace getJPXColorSpace()
    {
        return colorSpace;
    }

    // Sets the JPX color space
    void setColorSpace(PDJPXColorSpace colorSpace)
    {
        this.colorSpace = colorSpace;
    }
}
