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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.prefs.Preferences;

/**
 * A class to save windows position and size in preference using java Preference API.
 *
 * @author Tilman Hausherr
 */
public class WindowPrefs
{
    private static final String KEY = "window_prefs_";
    private final Preferences pref;

    public WindowPrefs(Class className)
    {
        this.pref = Preferences.userNodeForPackage(className);
    }

    public void setBounds(Rectangle rect)
    {
        Preferences node = pref.node(KEY);
        node.putInt("X", rect.x);
        node.putInt("Y", rect.y);
        node.putInt("W", rect.width);
        node.putInt("H", rect.height);
    }

    public Rectangle getBounds()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Preferences node = pref.node(KEY);
        int x = node.getInt("X", screenSize.width / 4);
        int y = node.getInt("Y", screenSize.height / 4);
        int w = node.getInt("W", screenSize.width / 2);
        int h = node.getInt("H", screenSize.height / 2);
        return new Rectangle(x, y, w, h);
    }

    public void setDividerLocation(int divider)
    {
        Preferences node = pref.node(KEY);
        node.putInt("DIV", divider);
    }

    public int getDividerLocation()
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Preferences node = pref.node(KEY);
        return node.getInt("DIV", screenSize.width / 8);
    }

    public void setExtendedState(int extendedState)
    {
        Preferences node = pref.node(KEY);
        node.putInt("EXTSTATE", extendedState);
    }

    public int getExtendedState()
    {
        Preferences node = pref.node(KEY);
        return node.getInt("EXTSTATE", Frame.NORMAL);
    }
}
