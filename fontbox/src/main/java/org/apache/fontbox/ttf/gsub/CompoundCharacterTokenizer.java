package org.apache.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompoundCharacterTokenizer
{

    private final Pattern regexExpression;

    public CompoundCharacterTokenizer(Set<String> compoundWords)
    {
        regexExpression = Pattern.compile(getRegexFromTokens(compoundWords));
    }

    public List<String> tokenize(String text)
    {
        List<String> tokens = new ArrayList<String>();

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
