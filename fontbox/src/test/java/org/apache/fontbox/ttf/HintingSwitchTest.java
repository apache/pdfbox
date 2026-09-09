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
package org.apache.fontbox.ttf;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The switch itself: that hinting is off unless it is asked for, that
 * {@link TrueTypeFont#SYSPROP_HINTING} is what asks for it, and that the setter overrides the property.
 * <p>
 * The setting is resolved once per JVM, so every test here clears the resolved value first - otherwise
 * it would be observing whatever an earlier test class left behind rather than what a fresh JVM sees.
 */
class HintingSwitchTest
{
    private String previousProperty;

    @BeforeEach
    void rememberProperty()
    {
        previousProperty = System.getProperty(TrueTypeFont.SYSPROP_HINTING);
    }

    /** Puts the JVM back exactly as it was: the property restored and nothing resolved from it yet. */
    @AfterEach
    void restoreProperty()
    {
        if (previousProperty == null)
        {
            System.clearProperty(TrueTypeFont.SYSPROP_HINTING);
        }
        else
        {
            System.setProperty(TrueTypeFont.SYSPROP_HINTING, previousProperty);
        }
        TrueTypeFont.resetHintingSetting();
    }

    private static void resolve(String propertyValue)
    {
        if (propertyValue == null)
        {
            System.clearProperty(TrueTypeFont.SYSPROP_HINTING);
        }
        else
        {
            System.setProperty(TrueTypeFont.SYSPROP_HINTING, propertyValue);
        }
        TrueTypeFont.resetHintingSetting();
    }

    private static TrueTypeFont parse() throws IOException
    {
        try (InputStream is = HintingSwitchTest.class
                .getResourceAsStream("/ttf/LiberationSans-Regular.ttf"))
        {
            assertNotNull(is, "missing test font");
            return new TTFParser().parse(new RandomAccessReadBuffer(is));
        }
    }

    private static int gid(TrueTypeFont font) throws IOException
    {
        return font.getUnicodeCmapLookup().getGlyphId('H');
    }

    /**
     * The contract the whole feature rests on while it is opt-in: a JVM that was never told to hint
     * renders exactly what it rendered before hinting existed.
     */
    @Test
    void testHintingIsOffWhenThePropertyIsNotSet() throws IOException
    {
        resolve(null);
        assertFalse(TrueTypeFont.isHintingEnabled(), "hinting must be off by default");

        TrueTypeFont font = parse();
        assertNull(font.getHintedPath(gid(font), 16), "an unconfigured JVM must not grid-fit");
    }

    @Test
    void testPropertyTurnsHintingOn() throws IOException
    {
        resolve("true");
        assertTrue(TrueTypeFont.isHintingEnabled());

        TrueTypeFont font = parse();
        assertNotNull(font.getHintedPath(gid(font), 16), "the property should enable grid-fitting");
    }

    @Test
    void testPropertyIsCaseInsensitive()
    {
        resolve("TRUE");
        assertTrue(TrueTypeFont.isHintingEnabled(), "-D...=TRUE should work like =true");
    }

    @Test
    void testAnythingOtherThanTrueLeavesHintingOff()
    {
        resolve("false");
        assertFalse(TrueTypeFont.isHintingEnabled());

        resolve("yes");
        assertFalse(TrueTypeFont.isHintingEnabled(), "only \"true\" enables hinting");
    }

    /** The programmatic switch is documented to override the property; nothing pinned that. */
    @Test
    void testSetterOverridesTheProperty() throws IOException
    {
        resolve("false");
        TrueTypeFont.setHintingEnabled(true);
        assertTrue(TrueTypeFont.isHintingEnabled());

        TrueTypeFont font = parse();
        assertNotNull(font.getHintedPath(gid(font), 16));

        resolve("true");
        TrueTypeFont.setHintingEnabled(false);
        assertFalse(TrueTypeFont.isHintingEnabled());
    }
}
