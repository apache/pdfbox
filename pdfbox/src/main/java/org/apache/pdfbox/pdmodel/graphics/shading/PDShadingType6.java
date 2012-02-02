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

/**
 * This represents resources for a shading type 6 (Coons Patch Meshes).
 *
 * @version $Revision: 1.0 $
 */
public class PDShadingType6 extends PDShadingType4
{
    
    /**
     * Constructor using the given shading dictionary.
     *
     * @param shadingDictionary The dictionary for this shading.
     */
    public PDShadingType6( COSDictionary shadingDictionary )
    {
        super(shadingDictionary);
    }

    /**
     * {@inheritDoc}
     */
    public int getShadingType()
    {
        return PDShadingResources.SHADING_TYPE6;
    }

}
