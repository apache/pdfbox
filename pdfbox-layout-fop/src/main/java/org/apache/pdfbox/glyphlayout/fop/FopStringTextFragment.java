package org.apache.pdfbox.glyphlayout.fop;

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
import org.apache.fop.fonts.TextFragment;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Fop TextFragment created from String
 */
public class FopStringTextFragment implements TextFragment
{

    private final String s;

    public FopStringTextFragment(String s)
    {
        this.s = s;
    }

    @Override
    public CharacterIterator getIterator()
    {
        return new StringCharacterIterator(s);
    }

    @Override
    public int getBeginIndex()
    {
        return 0;
    }

    @Override
    public int getEndIndex()
    {
        return s.length() - 1;
    }

    @Override
    public String getScript()
    {
        return "auto";
    }

    @Override
    public String getLanguage()
    {
        return "none";
    }

    @Override
    public int getBidiLevel()
    {
        return 0;
    }

    @Override
    public char charAt(int i)
    {
        return s.charAt(i);
    }

    @Override
    public CharSequence subSequence(int i, int i1)
    {
        return s.subSequence(i, i1 + 1);
    }
}
