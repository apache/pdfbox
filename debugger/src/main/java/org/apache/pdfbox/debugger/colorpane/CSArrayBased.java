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

package org.apache.pdfbox.debugger.colorpane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.color.ColorSpace;
import java.io.IOException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDICCBased;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;

/**
 *
 * @author Tilman Hausherr
 *
 * Simple pane to show a title and detail on the color spaces that have no visual "magic".
 */
public class CSArrayBased
{
    private JPanel panel;
    private PDColorSpace colorSpace = null;
    private int numberOfComponents = 0;
    private String errmsg = "";

    public CSArrayBased(COSArray array)
    {
        try
        {
            colorSpace = PDColorSpace.create(array);
            if (!(colorSpace instanceof PDPattern))
            {
                numberOfComponents = colorSpace.getNumberOfComponents();
            }
        }
        catch (IOException ex)
        {
            errmsg = ex.getMessage();
        }
        initUI();
    }

    private void initUI()
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 500));

        if (colorSpace == null)
        {
            JLabel error = new JLabel(errmsg);
            error.setAlignmentX(Component.CENTER_ALIGNMENT);
            error.setFont(new Font(Font.MONOSPACED, Font.BOLD, 15));
            panel.add(error);
            return;
        }
        
        JLabel colorSpaceLabel = new JLabel(colorSpace.getName() + " colorspace");
        colorSpaceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        colorSpaceLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        panel.add(colorSpaceLabel);

        if (numberOfComponents > 0)
        {
            JLabel colorCountLabel = new JLabel("Component Count: " + numberOfComponents);
            colorCountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            colorCountLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            panel.add(colorCountLabel);
        }

        if (colorSpace instanceof PDICCBased)
        {
            PDICCBased icc = (PDICCBased) colorSpace;
            int colorSpaceType = icc.getColorSpaceType();
            String cs;
            switch (colorSpaceType)
            {
                case ColorSpace.CS_LINEAR_RGB:
                    cs = "linear RGB";
                    break;
                case ColorSpace.CS_CIEXYZ:
                    cs = "CIEXYZ";
                    break;
                case ColorSpace.CS_GRAY:
                    cs = "linear gray";
                    break;
                case ColorSpace.CS_sRGB:
                    cs = "sRGB";
                    break;
                case ColorSpace.TYPE_RGB:
                    cs = "RGB";
                    break;
                case ColorSpace.TYPE_GRAY:
                    cs = "gray";
                    break;
                case ColorSpace.TYPE_CMYK:
                    cs = "CMYK";
                    break;
                default:
                    cs = "type " + colorSpaceType;
                    break;
            }
            JLabel otherLabel = new JLabel("Colorspace type: " + cs);
            otherLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            otherLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 20));
            panel.add(otherLabel);
        }
    }

    /**
     * return the main panel that hold all the UI elements.
     *
     * @return JPanel instance
     */
    public Component getPanel()
    {
        return panel;
    }

}
