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

package org.apache.pdfbox.tools.pdfdebugger.pagepane;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

/**
 *
 * @author Tilman Hausherr
 */
public class PagePane
{
    private JPanel panel;
    private int pageIndex = -1;

    public PagePane(COSDictionary page)
    {
        COSBase parent = page;
        
        // copied from PDPageDestination.retrievePageNumber()
        //TODO should this become a utility method of PDPageTree?
        while (((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P) != null)
        {
            parent = ((COSDictionary) parent).getDictionaryObject(COSName.PARENT, COSName.P);
        }
        // now parent is the pages node
        PDPageTree pages = new PDPageTree((COSDictionary) parent);
        pageIndex = pages.indexOf(new PDPage(page));

        initUI();
    }

    private void initUI()
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(300, 500));

        String pageLabelText = pageIndex < 0 ? "Page number not found" : "Page " + (pageIndex + 1);
        
        JLabel pageLabel = new JLabel(pageLabelText);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        panel.add(pageLabel);
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
