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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.apache.pdfbox.debugger.ui.RotationMenu;
import org.apache.pdfbox.debugger.ui.ZoomMenu;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.debugger.PDFDebugger;
import org.apache.pdfbox.debugger.ui.HighResolutionImageIcon;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDNamedDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;

/**
 * Display the page number and a page rendering.
 * 
 * @author Tilman Hausherr
 * @author John Hewson
 */
public class PagePane implements ActionListener, AncestorListener, MouseMotionListener, MouseListener
{
    private static final Log LOG = LogFactory.getLog(PagePane.class);
    private JPanel panel;
    private int pageIndex = -1;
    private final PDDocument document;
    private JLabel label;
    private ZoomMenu zoomMenu;
    private RotationMenu rotationMenu;
    private final JLabel statuslabel;
    private final PDPage page;
    private String labelText = "";
    private final Map<PDRectangle, String> rectMap = new HashMap<PDRectangle, String>();
    private final AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();

    public PagePane(PDDocument document, COSDictionary pageDict, JLabel statuslabel)
    {
        page = new PDPage(pageDict);
        pageIndex = document.getPages().indexOf(page);
        this.document = document;
        this.statuslabel = statuslabel;
        initUI();
        initRectMap();
    }

    private void initRectMap()
    {
        try
        {
            collectFieldLocations();
            collectLinkLocations();
        }
        catch (IOException ex)
        {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void collectLinkLocations() throws IOException
    {
        for (PDAnnotation annotation : page.getAnnotations())
        {
            if (annotation instanceof PDAnnotationLink)
            {
                collectLinkLocation((PDAnnotationLink) annotation);
            }
        }
    }

    private void collectLinkLocation(PDAnnotationLink linkAnnotation) throws IOException
    {
        PDAction action = linkAnnotation.getAction();
        if (action instanceof PDActionURI)
        {
            PDActionURI uriAction = (PDActionURI) action;
            rectMap.put(linkAnnotation.getRectangle(), "URI: " + uriAction.getURI());
            return;
        }
        PDDestination destination;
        if (action instanceof PDActionGoTo)
        {
            PDActionGoTo goToAction = (PDActionGoTo) action;
            destination = goToAction.getDestination();
        }
        else
        {
            destination = linkAnnotation.getDestination();
        }
        if (destination instanceof PDNamedDestination)
        {
            destination = document.getDocumentCatalog().
                    findNamedDestinationPage((PDNamedDestination) destination);
        }
        if (destination instanceof PDPageDestination)
        {
            PDPageDestination pageDestination = (PDPageDestination) destination;
            int pageNum = pageDestination.retrievePageNumber();
            if (pageNum != -1)
            {
                rectMap.put(linkAnnotation.getRectangle(), "Page destination: " + (pageNum + 1));
            }
        }
    }

    private void collectFieldLocations() throws IOException
    {
        PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
        if (acroForm == null)
        {
            return;
        }
        Set<COSDictionary> dictionarySet = new HashSet<COSDictionary>();
        for (PDAnnotation annotation : page.getAnnotations())
        {
            dictionarySet.add(annotation.getCOSObject());
        }
        for (PDField field : acroForm.getFieldTree())
        {
            for (PDAnnotationWidget widget : field.getWidgets())
            {
                // check if the annotation widget is on this page
                // (checking widget.getPage() also works, but it is sometimes null)
                if (dictionarySet.contains(widget.getCOSObject()))
                {
                    rectMap.put(widget.getRectangle(), "Field name: " + field.getFullyQualifiedName());
                }
            }
        }
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
        startRendering();
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
        if (ZoomMenu.isZoomMenu(actionCommand) ||
            RotationMenu.isRotationMenu(actionCommand) ||
            actionEvent.getSource() == PDFDebugger.allowSubsampling)
        {
            startRendering();
            zoomMenu.setPageZoomScale(ZoomMenu.getZoomScale());
        }
    }

    private void startRendering()
    {
        // render in a background thread: rendering is read-only, so this should be ok, despite
        // the fact that PDDocument is not officially thread safe
        new RenderWorker(ZoomMenu.getZoomScale(),
                RotationMenu.getRotationDegrees(),
                PDFDebugger.allowSubsampling.isSelected()
        ).execute();
        zoomMenu.setPageZoomScale(ZoomMenu.getZoomScale());
    }

    @Override
    public void ancestorAdded(AncestorEvent ancestorEvent)
    {
        zoomMenu.addMenuListeners(this);
        zoomMenu.setEnableMenu(true);
        
        rotationMenu = RotationMenu.getInstance();
        rotationMenu.addMenuListeners(this);
        rotationMenu.setEnableMenu(true);
        
        PDFDebugger.allowSubsampling.setEnabled(true);
        PDFDebugger.allowSubsampling.addActionListener(this);
    }

    @Override
    public void ancestorRemoved(AncestorEvent ancestorEvent)
    {
        zoomMenu.setEnableMenu(false);
        rotationMenu.setEnableMenu(false);

        PDFDebugger.allowSubsampling.setEnabled(false);
        PDFDebugger.allowSubsampling.removeActionListener(this);
    }

    @Override
    public void ancestorMoved(AncestorEvent ancestorEvent)
    {
        // do nothing
    }

    @Override
    public void mouseDragged(MouseEvent e)
    {
        // do nothing
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
        float x = e.getX() / zoomScale * (float) defaultTransform.getScaleX();
        float y = e.getY() / zoomScale * (float) defaultTransform.getScaleY();
        int x1;
        int y1;
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
        String text = "x: " + x1 + ", y: " + y1;

        // are we in a field widget?
        for (Map.Entry<PDRectangle, String> entry : rectMap.entrySet())
        {
            if (entry.getKey().contains(x1, y1))
            {
                text += ", " + rectMap.get(entry.getKey());
                break;
            }
        }

        statuslabel.setText(text);
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseReleased(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e)
    {
        statuslabel.setText(labelText);
    }

    /**
     * Note that PDDocument is not officially thread safe, caution advised.
     */
    private final class RenderWorker extends SwingWorker<BufferedImage, Integer>
    {
        private final float scale;
        private final int rotation;
        private final boolean allowSubsampling;

        private RenderWorker(float scale, int rotation, boolean allowSubsampling)
        {
            this.scale = scale;
            this.rotation = rotation;
            this.allowSubsampling = allowSubsampling;
        }

        @Override
        protected BufferedImage doInBackground() throws IOException
        {
            label.setIcon(null);
            labelText = "Rendering...";
            label.setText(labelText);
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(allowSubsampling);
            long t0 = System.currentTimeMillis();
            statuslabel.setText(labelText);
            BufferedImage bim = renderer.renderImage(pageIndex, scale);
            float t = (System.currentTimeMillis() - t0) / 1000f;
            labelText = "Rendered in " + t + " second" + (t > 1 ? "s" : "");
            statuslabel.setText(labelText);
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
                label.setSize((int) Math.ceil(image.getWidth() / defaultTransform.getScaleX()), 
                              (int) Math.ceil(image.getHeight() / defaultTransform.getScaleY()));
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
