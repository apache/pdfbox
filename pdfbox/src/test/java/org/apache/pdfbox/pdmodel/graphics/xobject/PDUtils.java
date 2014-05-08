/*
 * Copyright 2014 The Apache Software Foundation.
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

package org.apache.pdfbox.pdmodel.graphics.xobject;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import static junit.framework.Assert.assertEquals;

/**
 *
 * @author Tilman Hausherr
 */
public class PDUtils
{
    static BufferedImage createInterestingImage(int type)
    {
        BufferedImage awtImage = new BufferedImage(256, 256, type);
        Graphics g = awtImage.getGraphics();
        g.setColor(Color.blue);
        g.fillRect(0, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.white);
        g.fillRect(awtImage.getWidth() / 3, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.red);
        g.fillRect(awtImage.getWidth() / 3 * 2, 0, awtImage.getWidth() / 3, awtImage.getHeight() - 1);
        g.setColor(Color.black);
        g.drawRect(0, 0, awtImage.getWidth() - 1, awtImage.getHeight() - 1);
        g.dispose();
        return awtImage;
    }

    /**
     * Check whether images are identical.
     *
     * @param expectedImage
     * @param actualImage
     */
    static void checkIdent(BufferedImage expectedImage, BufferedImage actualImage)
    {
        String errMsg = "";

        int w = expectedImage.getWidth();
        int h = expectedImage.getHeight();
        for (int y = 0; y < h; ++y)
        {
            for (int x = 0; x < w; ++x)
            {
                if ((expectedImage.getRGB(x, y) & 0xFFFFFF) != (actualImage.getRGB(x, y) & 0xFFFFFF))
                {
                    errMsg = String.format("(%d,%d) %X != %X", x, y, expectedImage.getRGB(x, y) & 0xFFFFFF, actualImage.getRGB(x, y) & 0xFFFFFF);
                }
                assertEquals(errMsg, expectedImage.getRGB(x, y) & 0xFFFFFF, actualImage.getRGB(x, y) & 0xFFFFFF);
            }
        }
    }

    static int colorCount(BufferedImage bim)
    {
        Set<Integer> colors = new HashSet<Integer>();
        int w = bim.getWidth();
        int h = bim.getHeight();
        for (int y = 0; y < h; y++)
        {
            for (int x = 0; x < w; x++)
            {
                colors.add(bim.getRGB(x, y));
            }
        }
        return colors.size();
    }
    
}
