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
package org.apache.pdfbox.encoding;

import java.util.Iterator;

import org.apache.fontbox.afm.CharMetric;
import org.apache.fontbox.afm.FontMetric;

import org.apache.pdfbox.cos.COSBase;

/**
 * This will handle the encoding from an AFM font.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.8 $
 */
public class AFMEncoding extends Encoding
{
    private FontMetric metric = null;

    /**
     * Constructor.
     *
     * @param fontInfo The font metric information.
     */
    public AFMEncoding( FontMetric fontInfo )
    {
        metric = fontInfo;
        Iterator<CharMetric> characters = metric.getCharMetrics().iterator();
        while( characters.hasNext() )
        {
            CharMetric nextMetric = (CharMetric)characters.next();
            addCharacterEncoding( nextMetric.getCharacterCode(), nextMetric.getName() );
        }
    }

    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return null;
    }
}
