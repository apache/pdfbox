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

import java.awt.Component;
import java.util.Arrays;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * @author Khyrul Bashar
 *
 * A singleton class that provides zoom menu which can be used to show zoom menu in the menubar. To
 * act upon the menu item selection user of the class must add ActionListener which will check for
 * the action command and act accordingly.
 */
public final class ZoomMenu extends MenuBase
{
    private static class ZoomMenuItem extends JRadioButtonMenuItem
    {
        private final int zoom;

        ZoomMenuItem(String text, int zoom)
        {
            super(text);
            this.zoom = zoom;
        }
    }

    public static final String ZOOM_100_PERCENT = "100%";
    private static final int[] ZOOMS = new int[] { 25, 50, 100, 200, 400 };

    private static ZoomMenu instance;
    private final JMenu menu;

    /**
     * Constructor.
     */
    private ZoomMenu()
    {
        menu = new JMenu("Zoom");
        ButtonGroup bg = new ButtonGroup();
        for (int zoom : ZOOMS)
        {
            ZoomMenuItem zoomItem = new ZoomMenuItem(zoom + "%", zoom);
            bg.add(zoomItem);
            menu.add(zoomItem);
        }
        setMenu(menu);
    }

    /**
     * Provides the ZoomMenu instance.
     *
     * @return ZoomMenu instance.
     */
    public static ZoomMenu getInstance()
    {
        if (instance == null)
        {
            instance = new ZoomMenu();
        }
        return instance;
    }

    /**
     * Set the zoom selection.
     *
     * @param selection zoom menu string, e.g. "100%".
     * @throws IllegalArgumentException if the parameter doesn't belong to a zoom menu item.
     */
    public void setZoomSelection(String selection)
    {
        for (Component comp : menu.getMenuComponents())
        {
            JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) comp;
            if (menuItem.getText().equals(selection))
            {
                menuItem.setSelected(true);
                return;
            }
        }
        throw new IllegalArgumentException("no zoom menu item found for: " + selection);
    }

    /**
     * Tell whether the command belongs to the zoom menu.
     *
     * @param actionCommand a menu command string.
     * @return true if the command is a zoom menu command, e.g. "100%", false if not.
     */
    public static boolean isZoomMenu(String actionCommand)
    {
        if (!actionCommand.matches("^\\d+%$"))
        {
            return false;
        }
        int zoom = Integer.parseInt(actionCommand.substring(0, actionCommand.length() - 1));
        return Arrays.binarySearch(ZOOMS, zoom) >= 0;
    }

    /**
     * Tell the current zoom scale.
     *
     * @return the current zoom scale.
     * @throws IllegalStateException if no zoom menu item is selected.
     */
    public static float getZoomScale()
    {
        for (Component comp : instance.menu.getMenuComponents())
        {
            ZoomMenuItem menuItem = (ZoomMenuItem) comp;
            if (menuItem.isSelected())
            {
                return menuItem.zoom / 100f;
            }
        }
        throw new IllegalStateException("no zoom menu item is selected");
    }
}
