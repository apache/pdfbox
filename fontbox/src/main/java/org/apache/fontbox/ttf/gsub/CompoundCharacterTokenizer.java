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

    private final Pattern regexExpression;

    public CompoundCharacterTokenizer(Set<String> compoundWords)
    {
        regexExpression = Pattern.compile(getRegexFromTokens(compoundWords));
    }

    public CompoundCharacterTokenizer(String singleRegex)
    {
        regexExpression = Pattern.compile(singleRegex);
    }

    public List<String> tokenize(String text)
    {
        List<String> tokens = new ArrayList<>();

        Matcher regexMatcher = regexExpression.matcher(text);

        int lastIndexOfPrevMatch = 0;

        while (regexMatcher.find())
        {

            int beginIndexOfNextMatch = regexMatcher.start();

            String prevToken = text.substring(lastIndexOfPrevMatch, beginIndexOfNextMatch);

            if (prevToken.length() > 0)
            {
                tokens.add(prevToken);
            }

            String currentMatch = regexMatcher.group();

            tokens.add(currentMatch);

            lastIndexOfPrevMatch = regexMatcher.end();

        }

        String tail = text.substring(lastIndexOfPrevMatch, text.length());

        if (tail.length() > 0)
        {
            tokens.add(tail);
        }

        return tokens;
    }

    private String getRegexFromTokens(Set<String> compoundWords)
    {
        StringBuilder sb = new StringBuilder();

        for (String compoundWord : compoundWords)
        {
            sb.append("(");
            sb.append(compoundWord);
            sb.append(")|");
        }

        sb.setLength(sb.length() - 1);

        return sb.toString();
    }

}
