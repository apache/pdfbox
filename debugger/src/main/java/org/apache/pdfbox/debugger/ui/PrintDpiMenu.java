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
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

/**
 * @author Tilman Hausherr
 *
 * A singleton class that provides the print dpi menu.
 */
public final class PrintDpiMenu extends MenuBase
{
    @SuppressWarnings("squid:MaximumInheritanceDepth")
    private static class PrintDpiMenuItem extends JRadioButtonMenuItem
    {
        private static final long serialVersionUID = -475209538362541000L;
        private final int dpi;

        PrintDpiMenuItem(String text, int dpi)
        {
            super(text);
            this.dpi = dpi;
        }
    }

    private static final int[] DPIS = { 0, 100, 200, 300, 600, 1200, -1 };

    private static PrintDpiMenu instance;
    private final JMenu menu;

    /**
     * Constructor.
     */
    private PrintDpiMenu()
    {
        menu = new JMenu("Print rastering");
        ButtonGroup bg = new ButtonGroup();
        for (int dpi : DPIS)
        {
            String text;
            switch (dpi)
            {
                case 0:
                    text = "off";
                    break;
                case -1:
                    text = "printer dpi";
                    break;
                default:
                    text = dpi + " dpi";
                    break;
            }
            PrintDpiMenuItem printDpiMenuItem = new PrintDpiMenuItem(text, dpi);
            bg.add(printDpiMenuItem);
            menu.add(printDpiMenuItem);
        }
        changeDpiSelection(0);
        setMenu(menu);
    }

    /**
     * Provides the DpiMenu instance.
     *
     * @return DpiMenu instance.
     */
    public static PrintDpiMenu getInstance()
    {
        if (instance == null)
        {
            instance = new PrintDpiMenu();
        }
        return instance;
    }

    /**
     * Set the dpi selection.
     *
     * @param selection the selected DPI value
     * 
     * @throws IllegalArgumentException if the parameter doesn't belong to a dpi menu item.
     */
    public void changeDpiSelection(int selection)
    {
        for (Component comp : menu.getMenuComponents())
        {
            PrintDpiMenuItem menuItem = (PrintDpiMenuItem) comp;
            if (menuItem.dpi == selection)
            {
                menuItem.setSelected(true);
                return;
            }
        }
        throw new IllegalArgumentException("no dpi menu item found for: " + selection);
    }

    /**
     * Tell the current dpi scale.
     *
     * @return the current dpi scale.
     * @throws IllegalStateException if no dpi menu item is selected.
     */
    public static int getDpiSelection()
    {
        for (Component comp : instance.menu.getMenuComponents())
        {
            PrintDpiMenuItem menuItem = (PrintDpiMenuItem) comp;
            if (menuItem.isSelected())
            {
                return menuItem.dpi;
            }
        }
        throw new IllegalStateException("no dpi menu item is selected");
    }
}
