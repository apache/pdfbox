package org.apache.fontbox.ttf.gsub;

class LangSysRecord
{
    // https://www.microsoft.com/typography/otspec/languagetags.htm
    String langSysTag;
    LangSysTable langSysTable;

    @Override
    public String toString()
    {
        return String.format("LangSysRecord[langSysTag=%s]", langSysTag);
    }
}