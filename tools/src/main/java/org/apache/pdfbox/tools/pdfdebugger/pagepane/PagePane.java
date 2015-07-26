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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.pdfdebugger.ui.ZoomMenu;

/**
 * Display the page number and a page rendering.
 * 
 * @author Tilman Hausherr
 * @author John Hewson
 */
public class PagePane implements ActionListener, AncestorListener
{
    private JPanel panel;
    private int pageIndex = -1;
    private final PDDocument document;
    private JLabel label;
    private ZoomMenu zoomMenu;

    public PagePane(PDDocument document, COSDictionary page)
    {
        PDPage pdPage = new PDPage(page);
        pageIndex = document.getPages().indexOf(pdPage);
        this.document = document;
        initUI();
    }

    private void initUI()
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String pageLabelText = pageIndex < 0 ? "Page number not found" : "Page " + (pageIndex + 1);
        
        JLabel pageLabel = new JLabel(pageLabelText);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        pageLabel.setBackground(Color.GREEN);
        panel.add(pageLabel);
        
        label = new JLabel();
        label.setBackground(panel.getBackground());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setText("Loading...");
        panel.add(label);
        panel.addAncestorListener(this);

        // render in a background thread: rendering is read-only, so this should be ok, despite
        // the fact that PDDocument is not officially thread safe
        new RenderWorker(1).execute();
    }

    /**
     * Returns the main panel that hold all the UI elements.
     *
     * @return JPanel instance
     */
    public Component getPanel()
    {
        return panel;
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String actionCommand = actionEvent.getActionCommand();
        if (ZoomMenu.isZoomMenu(actionCommand))
        {
            new RenderWorker(ZoomMenu.getZoomScale(actionCommand)).execute();
        }
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        zoomMenu = ZoomMenu.getInstance().menuListeners(this);
        zoomMenu.setZoomSelection(ZoomMenu.ZOOM_100_PERCENT);
        zoomMenu.setEnableMenu(true);
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        zoomMenu.setEnableMenu(false);
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {

    }

    /**
     * Note that PDDocument is not officially thread safe, caution advised.
     */
    private final class RenderWorker extends SwingWorker<BufferedImage, Integer>
    {
        private final float scale;

        private RenderWorker(float scale)
        {
            this.scale = scale;
        }
        @Override
        protected BufferedImage doInBackground() throws IOException
        {
            PDFRenderer renderer = new PDFRenderer(document);
            return renderer.renderImage(pageIndex, scale);
        }

        @Override
        protected void done()
        {
            try
            {
                label.setIcon(new ImageIcon(get()));
                label.setText(null);
            }
            catch (InterruptedException e)
            {
                label.setText(e.getMessage());
                throw new RuntimeException(e);
            }
            catch (ExecutionException e)
            {
                label.setText(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
