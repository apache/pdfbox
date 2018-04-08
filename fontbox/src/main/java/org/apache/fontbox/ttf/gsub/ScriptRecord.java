package org.apache.fontbox.ttf.gsub;

class ScriptRecord
{
    // https://www.microsoft.com/typography/otspec/scripttags.htm
    String scriptTag;
    ScriptTable scriptTable;

    @Override
    public String toString()
    {
        return String.format("ScriptRecord[scriptTag=%s]", scriptTag);
    }
}