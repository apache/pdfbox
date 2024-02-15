/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.pdmodel.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSFloat;
import org.junit.jupiter.api.Test;

class TestPDRange {
    
    @Test
    void testNullRange()
    {
        PDRange pdRange1 = new PDRange( null );
        assertEquals( pdRange1.getMin(), 0.0f );
        assertEquals( pdRange1.getMax(), 1.0f );

        PDRange pdRange2 = new PDRange( null, 0 );
        assertEquals( pdRange2.getMin(), 0.0f );
        assertEquals( pdRange2.getMax(), 1.0f );
    }

    @Test
    void testInvalidIndex()
    {
        COSArray cosArray = new COSArray();
        cosArray.add( new COSFloat( 0.0f ) );
        cosArray.add( new COSFloat( 1.0f ) );
        cosArray.add( new COSFloat( 2.0f ) );

        assertThrows(IllegalArgumentException.class, () -> {
            new PDRange( cosArray, -1 );
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new PDRange( cosArray, 1 );
        });
        assertThrows(IllegalArgumentException.class, () -> {
            new PDRange( cosArray, 2 );
        });
    }
}
