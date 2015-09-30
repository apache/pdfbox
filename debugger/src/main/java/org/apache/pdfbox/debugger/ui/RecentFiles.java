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
package org.apache.pdfbox.debugger.ui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.prefs.Preferences;

/**
 * A class to save recent file history in preference using java Preference api.
 */
public class RecentFiles
{
    private static final String KEY = "recent_files_";
    private static final String PATH_KEY = "recent_files_%d_%d";
    private static final String PIECES_LENGTH_KEY = "recent_files_%d_length";
    private static final String HISTORY_LENGTH = "history_length";

    private final Preferences pref;
    private Queue<String> filePaths;
    private final int maximum;

    /**
     * Constructor.
     *
     * @param className the class for which this Recentfiles object is created and it will be used
     * to create preference instance.
     * @param maximumFile the number of recent files to remember.
     */
    public RecentFiles(Class className, int maximumFile)
    {
        this.maximum = maximumFile;
        this.pref = Preferences.userNodeForPackage(className);
        filePaths = readHistoryFromPref();
        if (filePaths == null)
        {
            filePaths = new ArrayDeque<String>();
        }
    }

    /**
     * Clear the previous recent file history.
     */
    public void removeAll()
    {
        filePaths.clear();
    }

    /**
     * Check if file history is empty.
     *
     * @return if history is empty return true otherwise return false.
     */
    public boolean isEmpty()
    {
        return filePaths.isEmpty();
    }

    /**
     * Add a new file in recent file history.
     *
     * @param path path to the file. this path means File#getPath() method returned String.
     */
    public void addFile(String path)
    {
        if (filePaths.size() >= maximum + 1 && path != null)
        {
            filePaths.remove();
        }

        filePaths.add(path);
    }

    /**
     * Remove a file from recent file history.
     *
     * @param path path string to the file. this path means File#getPath() method returned String.
     */
    public void removeFile(String path)
    {
        if (filePaths.contains(path))
        {
            filePaths.remove(path);
        }
    }

    /**
     * This gives the file in descending order where order is according to the time it is added.
     * This checks for file's existence in file history.
     *
     * @return return the file paths in a List.
     */
    public List<String> getFiles()
    {
        if (!isEmpty())
        {
            List<String> files = new ArrayList<String>();
            for (String path : filePaths)
            {
                File file = new File(path);
                if (file.exists())
                {
                    files.add(path);
                }
            }
            if (files.size() > maximum)
            {
                files.remove(0);
            }
            return files;
        }
        return null;
    }

    /**
     * This method save the present recent file history in the preference. To get the recent file
     * history in next session this method must be called.
     *
     * @throws IOException if saving in preference doesn't success.
     */
    public void close() throws IOException
    {
        writeHistoryToPref(filePaths);
    }

    private String[] breakString(String fullPath)
    {
        int allowedStringLength = Preferences.MAX_VALUE_LENGTH;
        List<String> pieces = new ArrayList<String>();
        int beginIndex = 0;
        int remainingLength = fullPath.length();
        int endIndex = 0;
        while (remainingLength > 0)
        {
            endIndex += remainingLength >= allowedStringLength ? allowedStringLength : remainingLength;
            pieces.add(fullPath.substring(beginIndex, endIndex));
            beginIndex = endIndex;
            remainingLength = fullPath.length() - endIndex;
        }
        return pieces.toArray(new String[pieces.size()]);
    }

    private void writeHistoryToPref(Queue<String> filePaths)
    {
        if (filePaths.size() == 0)
        {
            return;
        }
        Preferences node = pref.node(KEY);
        node.putInt(HISTORY_LENGTH, filePaths.size());
        int fileCount = 1;
        for (String path : filePaths)
        {
            String[] pieces = breakString(path);
            node.putInt(String.format(PIECES_LENGTH_KEY, fileCount), pieces.length);
            for (int i = 0; i < pieces.length; i++)
            {
                node.put(String.format(PATH_KEY, fileCount, i), pieces[i]);
            }
            fileCount++;
        }
    }

    private Queue<String> readHistoryFromPref()
    {
        Preferences node = pref.node(KEY);
        int historyLength = node.getInt(HISTORY_LENGTH, 0);
        if (historyLength == 0)
        {
            return null;
        }
        Queue<String> history = new ArrayDeque<String>();

        for (int i = 1; i <= historyLength; i++)
        {
            int totalPieces = node.getInt(String.format(PIECES_LENGTH_KEY, i), 0);
            StringBuilder stringBuilder = new StringBuilder();
            for (int j = 0; j < totalPieces; j++)
            {
                String piece = node.get(String.format(PATH_KEY, i, j), "");
                stringBuilder.append(piece);
            }
            history.add(stringBuilder.toString());
        }
        return history;
    }
}
