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
package org.apache.pdfbox.pdmodel.graphics.shading;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;

/**
 * This represents resources for a shading type 4 (Free-Form Gouraud-Shaded
 * Triangle Meshes).
 *
 */
public class PDShadingType4 extends PDTriangleBasedShadingType
{

    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary The dictionary for this shading.
     */
    public PDShadingType4(COSDictionary shadingDictionary)
    {
        super(shadingDictionary);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getShadingType()
    {
        return PDShadingResources.SHADING_TYPE4;
    }

    /**
     * The bits per flag of this shading. This will return -1 if one has not
     * been set.
     *
     * @return The number of bits per flag.
     */
    public int getBitsPerFlag()
    {
        return getCOSDictionary().getInt(COSName.BITS_PER_FLAG, -1);
    }

    /**
     * Set the number of bits per flag.
     *
     * @param bpf The number of bits per flag.
     */
    public void setBitsPerFlag(int bpf)
    {
        getCOSDictionary().setInt(COSName.BITS_PER_FLAG, bpf);
    }

}
