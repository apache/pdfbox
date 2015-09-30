/*
 * Copyright 2015 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 *
 * @author Khyrul Bashar
 * @author Tilman Hausherr
 *
 * Base class.
 */
abstract class MenuBase
{
    private JMenu menu = null;

    protected void setMenu(JMenu menu)
    {
        this.menu = menu;
    }
    
    /**
     * Provide the JMenu instance of the ZoomMenu.
     *
     * @return JMenu instance.
     */
    public JMenu getMenu()
    {
        return this.menu;
    }

    /**
     * Set if the menu should be enabled or disabled.
     *
     * @param isEnable boolean instance.
     */
    public void setEnableMenu(boolean isEnable)
    {
        menu.setEnabled(isEnable);
    }

    /**
     * Add the ActionListener for the menuitems.
     *
     * @param listener ActionListener.
     */
    public void addMenuListeners(ActionListener listener)
    {
        for (Component comp : menu.getMenuComponents())
        {
            JMenuItem menuItem = (JMenuItem) comp;
            removeActionListeners(menuItem);
            menuItem.addActionListener(listener);
        }
    }

    private void removeActionListeners(JMenuItem menuItem)
    {
        for (ActionListener listener : menuItem.getActionListeners())
        {
            menuItem.removeActionListener(listener);
        }
    }

}
