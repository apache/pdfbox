/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pdfbox.pdmodel.encryption;

import java.nio.CharBuffer;
import java.text.Normalizer;

/**
 * Copied from https://github.com/tombentley/saslprep/blob/master/src/main/java/SaslPrep.java on
 * 30.5.2019, commit 2e30daa.
 *
 * @author Tom Bentley
 */
class SaslPrep
{

    private SaslPrep()
    {
    }

    /**
     * Return the {@code SASLPrep}-canonicalised version of the given {@code str} for use as a query
     * string. This implements the {@code SASLPrep} algorithm defined in
     * <a href="https://tools.ietf.org/html/rfc4013">RFC 4013</a>.
     *
     * @param str The string to canonicalise.
     * @return The canonicalised string.
     * @throws IllegalArgumentException if the string contained prohibited codepoints, or broke the
     * requirements for bidirectional character handling.
     * @see <a href="https://tools.ietf.org/html/rfc3454#section-7">RFC 3454, Section 7</a> for
     * discussion of what a query string is.
     */
    static String saslPrepQuery(String str)
    {
        return saslPrep(str, true);
    }

    /**
     * Return the {@code SASLPrep}-canonicalised version of the given
     * @code str} for use as a stored string. This implements the {@code SASLPrep} algorithm defined
     * in
     * <a href="https://tools.ietf.org/html/rfc4013">RFC 4013</a>.
     *
     * @param str The string to canonicalise.
     * @return The canonicalised string.
     * @throws IllegalArgumentException if the string contained prohibited codepoints, or broke the
     * requirements for bidirectional character handling.
     * @see <a href="https://tools.ietf.org/html/rfc3454#section-7">RFC 3454, Section 7</a> for
     * discussion of what a stored string is.
     */
    static String saslPrepStored(String str)
    {
        return saslPrep(str, false);
    }

    private static String saslPrep(String str, boolean allowUnassigned)
    {
        char[] chars = str.toCharArray();

        // 1. Map
        // non-ASCII space chars mapped to space
        for (int i = 0; i < str.length(); i++)
        {
            char ch = str.charAt(i);
            if (nonAsciiSpace(ch))
            {
                chars[i] = ' ';
            }
        }

        int length = 0;
        for (int i = 0; i < str.length(); i++)
        {
            char ch = chars[i];
            if (!mappedToNothing(ch))
            {
                chars[length++] = ch;
            }
        }

        // 2. Normalize
        String normalized = Normalizer.normalize(CharBuffer.wrap(chars, 0, length), Normalizer.Form.NFKC);

        boolean containsRandALCat = false;
        boolean containsLCat = false;
        boolean initialRandALCat = false;
        int i = 0;
        while (i < normalized.length())
        {
            final int codepoint = normalized.codePointAt(i);
            // 3. Prohibit
            if (prohibited(codepoint))
            {
                throw new IllegalArgumentException("Prohibited character " +
                        codepoint + " at position " + i);
            }

            // 4. Check bidi
            final byte directionality = Character.getDirectionality(codepoint);
            final boolean isRandALcat = directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT
                    || directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
            containsRandALCat |= isRandALcat;
            containsLCat |= directionality == Character.DIRECTIONALITY_LEFT_TO_RIGHT;

            initialRandALCat |= i == 0 && isRandALcat;
            if (!allowUnassigned && !Character.isDefined(codepoint))
            {
                throw new IllegalArgumentException("Character at position " + i + " is unassigned");
            }

            i += Character.charCount(codepoint);

            if (initialRandALCat && i >= normalized.length() && !isRandALcat)
            {
                throw new IllegalArgumentException("First character is RandALCat, but last character is not");
            }
        }
        if (containsRandALCat && containsLCat)
        {
            throw new IllegalArgumentException("Contains both RandALCat characters and LCat characters");
        }
        return normalized;
    }

    /**
     * Return true if the given {@code codepoint} is a prohibited character
     * as defined by
     * <a href="https://tools.ietf.org/html/rfc4013#section-2.3">RFC 4013,
     * Section 2.3</a>.
     */
    static boolean prohibited(int codepoint)
    {
        return nonAsciiSpace((char)codepoint)
                || asciiControl((char)codepoint)
                || nonAsciiControl(codepoint)
                || privateUse(codepoint)
                || nonCharacterCodePoint(codepoint)
                || surrogateCodePoint(codepoint)
                || inappropriateForPlainText(codepoint)
                || inappropriateForCanonical(codepoint)
                || changeDisplayProperties(codepoint)
                || tagging(codepoint);
    }

    /**
     * Return true if the given {@code codepoint} is a tagging character
     * as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.9">RFC 3454,
     * Appendix C.9</a>.
     */
    private static boolean tagging(int codepoint)
    {
        return codepoint == 0xE0001
                || 0xE0020 <= codepoint && codepoint <= 0xE007F;
    }

    /**
     * Return true if the given {@code codepoint} is change display properties
     * or deprecated characters as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.8">RFC 3454,
     * Appendix C.8</a>.
     */
    private static boolean changeDisplayProperties(int codepoint)
    {
        return codepoint == 0x0340
                || codepoint == 0x0341
                || codepoint == 0x200E
                || codepoint == 0x200F
                || codepoint == 0x202A
                || codepoint == 0x202B
                || codepoint == 0x202C
                || codepoint == 0x202D
                || codepoint == 0x202E
                || codepoint == 0x206A
                || codepoint == 0x206B
                || codepoint == 0x206C
                || codepoint == 0x206D
                || codepoint == 0x206E
                || codepoint == 0x206F
                ;
    }

    /**
     * Return true if the given {@code codepoint} is inappropriate for
     * canonical representation characters as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.7">RFC 3454,
     * Appendix C.7</a>.
     */
    private static boolean inappropriateForCanonical(int codepoint)
    {
        return 0x2FF0 <= codepoint && codepoint <= 0x2FFB;
    }

    /**
     * Return true if the given {@code codepoint} is inappropriate for plain
     * text characters as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.6">RFC 3454,
     * Appendix C.6</a>.
     */
    private static boolean inappropriateForPlainText(int codepoint)
    {
        return codepoint == 0xFFF9
                || codepoint == 0xFFFA
                || codepoint == 0xFFFB
                || codepoint == 0xFFFC
                || codepoint == 0xFFFD
                ;
    }

    /**
     * Return true if the given {@code codepoint} is a surrogate
     * code point as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.5">RFC 3454,
     * Appendix C.5</a>.
     */
    private static boolean surrogateCodePoint(int codepoint)
    {
        return 0xD800 <= codepoint && codepoint <= 0xDFFF;
    }

    /**
     * Return true if the given {@code codepoint} is a non-character
     * code point as defined by
     * <a href="https://tools.ietf.org/html/rfc3454#appendix-C.4">RFC 3454,
     * Appendix C.4</a>.
     */
    private static boolean nonCharacterCodePoint(int codepoint)
    {
        return 0xFDD0 <= codepoint && codepoint <= 0xFDEF
                || 0xFFFE <= codepoint && codepoint <= 0xFFFF
                || 0x1FFFE <= codepoint && codepoint <= 0x1FFFF
                || 0x2FFFE <= codepoint && codepoint <= 0x2FFFF
                || 0x3FFFE <= codepoint && codepoint <= 0x3FFFF
                || 0x4FFFE <= codepoint && codepoint <= 0x4FFFF
                || 0x5FFFE <= codepoint && codepoint <= 0x5FFFF
                || 0x6FFFE <= codepoint && codepoint <= 0x6FFFF
                || 0x7FFFE <= codepoint && codepoint <= 0x7FFFF
                || 0x8FFFE <= codepoint && codepoint <= 0x8FFFF
                || 0x9FFFE <= codepoint && codepoint <= 0x9FFFF
                || 0xAFFFE <= codepoint && codepoint <= 0xAFFFF
                || 0xBFFFE <= codepoint && codepoint <= 0xBFFFF
                || 0xCFFFE <= codepoint && codepoint <= 0xCFFFF
                || 0xDFFFE <= codepoint && codepoint <= 0xDFFFF
                || 0xEFFFE <= codepoint && codepoint <= 0xEFFFF
                || 0xFFFFE <= codepoint && codepoint <= 0xFFFFF
                || 0x10FFFE <= codepoint && codepoint <= 0x10FFFF
                ;
    }

    /**
     * Return true if the given {@code codepoint} is a private use character
     * as defined by <a href="https://tools.ietf.org/html/rfc3454#appendix-C.3">RFC 3454,
     * Appendix C.3</a>.
     */
    private static boolean privateUse(int codepoint)
    {
        return 0xE000 <= codepoint && codepoint <= 0xF8FF
                || 0xF0000 <= codepoint && codepoint <= 0xFFFFD
                || 0x100000 <= codepoint && codepoint <= 0x10FFFD;
    }

    /**
     * Return true if the given {@code ch} is a non-ASCII control character
     * as defined by <a href="https://tools.ietf.org/html/rfc3454#appendix-C.2.2">RFC 3454,
     * Appendix C.2.2</a>.
     */
    private static boolean nonAsciiControl(int codepoint)
    {
        return 0x0080 <= codepoint && codepoint <= 0x009F
                || codepoint == 0x06DD
                || codepoint == 0x070F
                || codepoint == 0x180E
                || codepoint == 0x200C
                || codepoint == 0x200D
                || codepoint == 0x2028
                || codepoint == 0x2029
                || codepoint == 0x2060
                || codepoint == 0x2061
                || codepoint == 0x2062
                || codepoint == 0x2063
                || 0x206A <= codepoint && codepoint <= 0x206F
                || codepoint == 0xFEFF
                || 0xFFF9 <= codepoint && codepoint <= 0xFFFC
                || 0x1D173 <= codepoint && codepoint <= 0x1D17A;
    }

    /**
     * Return true if the given {@code ch} is an ASCII control character
     * as defined by <a href="https://tools.ietf.org/html/rfc3454#appendix-C.2.1">RFC 3454,
     * Appendix C.2.1</a>.
     */
    private static boolean asciiControl(char ch)
    {
        return '\u0000' <= ch && ch <= '\u001F' || ch == '\u007F';
    }

    /**
     * Return true if the given {@code ch} is a non-ASCII space character
     * as defined by <a href="https://tools.ietf.org/html/rfc3454#appendix-C.1.2">RFC 3454,
     * Appendix C.1.2</a>.
     */
    private static boolean nonAsciiSpace(char ch)
    {
        return ch == '\u00A0'
                || ch == '\u1680'
                || '\u2000' <= ch && ch <= '\u200B'
                || ch == '\u202F'
                || ch == '\u205F'
                || ch == '\u3000';
    }

    /**
     * Return true if the given {@code ch} is a "commonly mapped to nothing" character
     * as defined by <a href="https://tools.ietf.org/html/rfc3454#appendix-B.1">RFC 3454,
     * Appendix B.1</a>.
     */
    private static boolean mappedToNothing(char ch)
    {
        return ch == '\u00AD'
                || ch == '\u034F'
                || ch == '\u1806'
                || ch == '\u180B'
                || ch == '\u180C'
                || ch == '\u180D'
                || ch == '\u200B'
                || ch == '\u200C'
                || ch == '\u200D'
                || ch == '\u2060'
                || '\uFE00' <= ch && ch <= '\uFE0F'
                || ch == '\uFEFF';
    }
}