package org.apache.fontbox.ttf.gsub;

import java.util.Comparator;

public class CompoundWordSorter implements Comparator<String>
{

    @Override
    public int compare(String first, String second)
    {
        if (first.length() < second.length())
        {
            return 1;
        }
        else if (first.length() > second.length())
        {
            return -1;
        }
        else
        {
            return first.compareTo(second);
        }
    }

}
