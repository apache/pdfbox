package org.apache.fontbox.ttf.gsub;

import java.util.Comparator;

public class CompoundWordSorter implements Comparator<String>
{

    @Override
    public int compare(String first, String second)
    {
        return second.length() - first.length();
    }

}
