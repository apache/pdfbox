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

package org.apache.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Takes in the given text having compound-glyphs to substitute, and splits it into chunks consisting of parts that
 * should be substituted and the ones that can be processed normally.
 * 
 * @author Palash Ray
 * 
 */
public class CompoundCharacterTokenizer
{
    private static final String GLYPH_ID_SEPARATOR = "_";
    private final Pattern regexExpression;

    /**
     * Constructor. Calls getRegexFromTokens which returns strings like
     * (_79_99_)|(_80_99_)|(_92_99_) and creates a regexp assigned to regexExpression. See the code
     * in GlyphArraySplitterRegexImpl on how these strings were created.
     * <p>
     * It is assumed the compound words are sorted in descending order of length.
     *
     * @param compoundWords A set of strings like _79_99_, _80_99_ or _92_99_ .
     */
    public CompoundCharacterTokenizer(Set<String> compoundWords)
    {
        validateCompoundWords(compoundWords);
        regexExpression = Pattern.compile(getRegexFromTokens(compoundWords));
    }

    public CompoundCharacterTokenizer(Pattern pattern)
    {
        regexExpression = pattern;
    }

    /**
     * Validate the compound words. They should not be null or empty and should start and end with
     * the GLYPH_ID_SEPARATOR
     */
    private void validateCompoundWords(Set<String> compoundWords)
    {
        if (compoundWords == null || compoundWords.isEmpty())
        {
            throw new IllegalArgumentException("Compound words cannot be null or empty");
        }

        // Ensure all word are starting and ending with the GLYPH_ID_SEPARATOR
        compoundWords.forEach(word ->
        {
            if (!word.startsWith(GLYPH_ID_SEPARATOR) || !word.endsWith(GLYPH_ID_SEPARATOR))
            {
                throw new IllegalArgumentException(
                        "Compound words should start and end with " + GLYPH_ID_SEPARATOR);
            }
        });
    }

    /**
     * Tokenize a string into tokens.
     *
     * @param text A string like "_66_71_71_74_79_70_"
     * @return A list of tokens like "_66_", "_71_71_", "74_79_70_". The "_" is sometimes missing at
     * the beginning or end, this has to be cleaned by the caller.
     */
    public List<String> tokenize(String text)
    {
        List<String> tokens = new ArrayList<>();

        Matcher regexMatcher = regexExpression.matcher(text);

        int lastIndexOfPrevMatch = 0;

        while (regexMatcher.find(lastIndexOfPrevMatch)) // this is where the magic happens:
                                    // the regexp is used to find a matching pattern for substitution
        {
            int beginIndexOfNextMatch = regexMatcher.start();

            String prevToken = text.substring(lastIndexOfPrevMatch, beginIndexOfNextMatch);

            if (!prevToken.isEmpty())
            {
                tokens.add(prevToken);
            }

            String currentMatch = regexMatcher.group();

            tokens.add(currentMatch);

            lastIndexOfPrevMatch = regexMatcher.end();
            if (lastIndexOfPrevMatch < text.length() && text.charAt(lastIndexOfPrevMatch) != '_')
            {
                // beause it is sometimes positioned after the "_", but it should be positioned
                // before the "_"
                --lastIndexOfPrevMatch;
            }
        }

        String tail = text.substring(lastIndexOfPrevMatch);

        if (!tail.isEmpty())
        {
            tokens.add(tail);
        }

        return tokens;
    }

    private String getRegexFromTokens(Set<String> compoundWords)
    {
        StringJoiner sj = new StringJoiner(")|(", "(", ")");
        compoundWords.forEach(sj::add);
        return sj.toString();
    }

}
