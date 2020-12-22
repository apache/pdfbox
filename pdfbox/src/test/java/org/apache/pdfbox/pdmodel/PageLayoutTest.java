/*
 * Copyright 2016 The Apache Software Foundation.
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
package org.apache.pdfbox.pdmodel;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PageLayoutTest
{
    /**
     * @author Tilman Hausherr
     *
     * Test for completeness (PDFBOX-3362).
     */
    @Test
    void testValues()
    {
        final Set<PageLayout> pageLayoutSet = EnumSet.noneOf(PageLayout.class);
        final Set<String> stringSet = new HashSet<>();
        for (final PageLayout pl : PageLayout.values())
        {
            final String s = pl.stringValue();
            stringSet.add(s);
            pageLayoutSet.add(PageLayout.fromString(s));
        }
        assertEquals(PageLayout.values().length, pageLayoutSet.size());
        assertEquals(PageLayout.values().length, stringSet.size());
    }

    /**
     * @author John Bergqvist
     */
    @Test
    void fromStringInputNotNullOutputIllegalArgumentException()
    {
        assertThrows(IllegalArgumentException.class, () -> PageLayout.fromString("SinglePag"));
    }
}
