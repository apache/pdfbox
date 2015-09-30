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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 *
 * @author Tilman Hausherr
 *
 * Utility class for images.
 */
public final class ImageUtil
{
    private ImageUtil()
    {
    }

    /**
     * Return an image rotated by a multiple of 90°.
     *
     * @param image The image to rotate.
     * @param rotation The rotation in degrees.
     * @return The rotated image.
     */
    public static BufferedImage getRotatedImage(BufferedImage image, int rotation)
    {
        int width = image.getWidth();
        int height = image.getHeight();
        double x = 0, y = 0;
        BufferedImage rotatedImage;
        switch (rotation % 360)
        {
            case 90:
                x = height;
                rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
                break;
            case 270:
                y = width;
                rotatedImage = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
                break;
            case 180:
                x = width;
                y = height;
                rotatedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                break;
            default:
                return image;
        }
        Graphics2D g = (Graphics2D) rotatedImage.getGraphics();
        g.translate(x, y);
        g.rotate(Math.toRadians(rotation));
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return rotatedImage;
    }

}
