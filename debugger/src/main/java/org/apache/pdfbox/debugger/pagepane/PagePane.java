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

package org.apache.pdfbox.debugger.pagepane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.apache.pdfbox.debugger.ui.RotationMenu;
import org.apache.pdfbox.debugger.ui.ZoomMenu;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.debugger.PDFDebugger;
import org.apache.pdfbox.debugger.ui.HighResolutionImageIcon;

/**
 * Display the page number and a page rendering.
 * 
 * @author Tilman Hausherr
 * @author John Hewson
 */
public class PagePane implements ActionListener, AncestorListener, MouseMotionListener, MouseListener
{
    private JPanel panel;
    private int pageIndex = -1;
    private final PDDocument document;
    private JLabel label;
    private ZoomMenu zoomMenu;
    private RotationMenu rotationMenu;
    private final JLabel statuslabel;
    private final PDPage page;

    public PagePane(PDDocument document, COSDictionary pageDict, JLabel statuslabel)
    {
        page = new PDPage(pageDict);
        pageIndex = document.getPages().indexOf(page);
        this.document = document;
        this.statuslabel = statuslabel;
        initUI();
    }

    private void initUI()
    {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String pageLabelText = pageIndex < 0 ? "Page number not found" : "Page " + (pageIndex + 1);

        // append PDF page label, if available
        String lbl = PDFDebugger.getPageLabel(document, pageIndex);
        if (lbl != null)
        {
            pageLabelText += " - " + lbl;
        }

        JLabel pageLabel = new JLabel(pageLabelText);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));
        pageLabel.setBackground(Color.GREEN);
        panel.add(pageLabel);
        
        label = new JLabel();
        label.addMouseMotionListener(this);
        label.addMouseListener(this);
        label.setBackground(panel.getBackground());
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(label);
        panel.addAncestorListener(this);

        zoomMenu = ZoomMenu.getInstance();
        zoomMenu.changeZoomSelection(zoomMenu.getPageZoomScale());
        // render in a background thread: rendering is read-only, so this should be ok, despite
        // the fact that PDDocument is not officially thread safe
        new RenderWorker(zoomMenu.getPageZoomScale(), 0).execute();
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
        if (ZoomMenu.isZoomMenu(actionCommand) || RotationMenu.isRotationMenu(actionCommand))
        {
            new RenderWorker(ZoomMenu.getZoomScale(), RotationMenu.getRotationDegrees()).execute();
            zoomMenu.setPageZoomScale(ZoomMenu.getZoomScale());
        }
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        zoomMenu.addMenuListeners(this);
        zoomMenu.setEnableMenu(true);
        
        rotationMenu = RotationMenu.getInstance();
        rotationMenu.addMenuListeners(this);
        rotationMenu.setRotationSelection(RotationMenu.ROTATE_0_DEGREES);
        rotationMenu.setEnableMenu(true);
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        zoomMenu.setEnableMenu(false);
        rotationMenu.setEnableMenu(false);
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
    }

    /**
     * Catch mouse event to display cursor position in PDF coordinates in the status bar.
     *
     * @param e mouse event with position
     */
    @Override
    public void mouseMoved(MouseEvent e)
    {
        float height = page.getCropBox().getHeight();
        float width  = page.getCropBox().getWidth();
        float offsetX = page.getCropBox().getLowerLeftX();
        float offsetY = page.getCropBox().getLowerLeftY();
        float zoomScale = zoomMenu.getPageZoomScale();
        float x = e.getX() / zoomScale;
        float y = e.getY() / zoomScale;
        int x1, y1;
        switch ((RotationMenu.getRotationDegrees() + page.getRotation()) % 360)
        {
            case 90:
                x1 = (int) (y + offsetX);
                y1 = (int) (x + offsetY);
                break;
            case 180:
                x1 = (int) (width - x + offsetX);
                y1 = (int) (y - offsetY);
                break;
            case 270:
                x1 = (int) (width - y + offsetX);
                y1 = (int) (height - x + offsetY);
                break;
            case 0:
            default:
                x1 = (int) (x + offsetX);
                y1 = (int) (height - y + offsetY);
                break;
        }
        statuslabel.setText(x1 + "," + y1);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        statuslabel.setText("");
    }

    /**
     * Note that PDDocument is not officially thread safe, caution advised.
     */
    private final class RenderWorker extends SwingWorker<BufferedImage, Integer>
    {
        private final float scale;
        private final int rotation;

        private RenderWorker(float scale, int rotation)
        {
            this.scale = scale;
            this.rotation = rotation;
        }

        @Override
        protected BufferedImage doInBackground() throws IOException
        {
            label.setIcon(null);
            label.setText("Rendering...");
            PDFRenderer renderer = new PDFRenderer(document);
            long t0 = System.currentTimeMillis();
            statuslabel.setText("Rendering...");
            BufferedImage bim = renderer.renderImage(pageIndex, scale);
            float t = (System.currentTimeMillis() - t0) / 1000f;
            statuslabel.setText("Rendered in " + t + " second" + (t > 1 ? "s" : ""));
            return ImageUtil.getRotatedImage(bim, rotation);
        }

        @Override
        protected void done()
        {
            try
            {
                BufferedImage image = get();

                // We cannot use "label.setIcon(new ImageIcon(get()))" here 
                // because of blurry upscaling in JDK9. Instead, the label is now created with 
                // a smaller size than the image to compensate that the
                // image is scaled up with some screen configurations (e.g. 125% on windows).
                // See PDFBOX-3665 for more sample code and discussion.
                AffineTransform tx = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
                label.setSize((int) Math.ceil(image.getWidth() / tx.getScaleX()), 
                              (int) Math.ceil(image.getHeight() / tx.getScaleY()));
                label.setIcon(new HighResolutionImageIcon(image, label.getWidth(), label.getHeight()));
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
