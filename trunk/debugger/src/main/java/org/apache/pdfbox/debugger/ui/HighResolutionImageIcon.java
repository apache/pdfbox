/*
 * Copyright 2017 The Apache Software Foundation.
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
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.Icon;

public class HighResolutionImageIcon implements Icon
{
    private final Image image;
    private final int baseWidth;
    private final int baseHeight;

    public HighResolutionImageIcon(Image image, int baseWidth, int baseHeight)
    {
        this.image = image;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y)
    {
        g.drawImage(image, x, y, getIconWidth(), getIconHeight(), null);
    }

    @Override
    public int getIconWidth()
    {
        return baseWidth;
    }

    @Override
    public int getIconHeight()
    {
        return baseHeight;
    }
}
