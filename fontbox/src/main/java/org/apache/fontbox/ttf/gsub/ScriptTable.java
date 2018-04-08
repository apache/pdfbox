package org.apache.fontbox.ttf.gsub;

import java.util.LinkedHashMap;

class ScriptTable
{
    LangSysTable defaultLangSysTable;
    LinkedHashMap<String, LangSysTable> langSysTables;

    @Override
    public String toString()
    {
        return String.format("ScriptTable[hasDefault=%s,langSysRecordsCount=%d]",
                defaultLangSysTable != null, langSysTables.size());
    }
}