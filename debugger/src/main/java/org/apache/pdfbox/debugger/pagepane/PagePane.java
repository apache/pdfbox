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

import java.awt.Graphics2D;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.debugger.PDFDebugger;
import org.apache.pdfbox.debugger.ui.ImageUtil;
import org.apache.pdfbox.debugger.ui.RotationMenu;
import org.apache.pdfbox.debugger.ui.ViewMenu;
import org.apache.pdfbox.debugger.ui.ZoomMenu;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.debugger.ui.ErrorDialog;
import org.apache.pdfbox.debugger.ui.HighResolutionImageIcon;
import org.apache.pdfbox.debugger.ui.ImageTypeMenu;
import org.apache.pdfbox.debugger.ui.RenderDestinationMenu;
import org.apache.pdfbox.debugger.ui.TextDialog;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.fixup.AcroFormDefaultFixup;
import org.apache.pdfbox.pdmodel.fixup.PDDocumentFixup;
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
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Display the page number and a page rendering.
 * 
 * @author Tilman Hausherr
 * @author John Hewson
 */
public class PagePane implements ActionListener, AncestorListener, MouseMotionListener, MouseListener
{
    private static final Log LOG = LogFactory.getLog(PagePane.class);
    private final PDDocument document;
    private final JLabel statuslabel;
    private final PDPage page;
    private JPanel panel;
    private int pageIndex = -1;
    private JLabel label;
    private ZoomMenu zoomMenu;
    private RotationMenu rotationMenu;
    private ImageTypeMenu imageTypeMenu;
    private RenderDestinationMenu renderDestinationMenu;
    private ViewMenu viewMenu;
    private String labelText = "";
    private String currentURI = "";
    private final Map<PDRectangle,String> rectMap = new HashMap<>();
    private final AffineTransform defaultTransform = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration().getDefaultTransform();
    // more ideas:
    // https://stackoverflow.com/questions/16440159/dragging-of-shapes-on-jpanel

    public PagePane(final PDDocument document, final COSDictionary pageDict, final JLabel statuslabel)
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
        for (final PDAnnotation annotation : page.getAnnotations())
        {
            if (annotation instanceof PDAnnotationLink)
            {
                collectLinkLocation((PDAnnotationLink) annotation);
            }
        }
    }

    private void collectLinkLocation(final PDAnnotationLink linkAnnotation) throws IOException
    {
        final PDAction action = linkAnnotation.getAction();
        if (action instanceof PDActionURI)
        {
            final PDActionURI uriAction = (PDActionURI) action;
            rectMap.put(linkAnnotation.getRectangle(), "URI: " + uriAction.getURI());
            return;
        }
        PDDestination destination;
        if (action instanceof PDActionGoTo)
        {
            final PDActionGoTo goToAction = (PDActionGoTo) action;
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
            final PDPageDestination pageDestination = (PDPageDestination) destination;
            final int pageNum = pageDestination.retrievePageNumber();
            if (pageNum != -1)
            {
                rectMap.put(linkAnnotation.getRectangle(), "Page destination: " + (pageNum + 1));
            }
        }
    }

    private void collectFieldLocations() throws IOException
    {
        // get Acroform without applying fixups to enure that we get the original content
        final PDDocumentFixup fixup = ViewMenu.isRepairAcroformSelected() ? new AcroFormDefaultFixup(document) : null;
        final PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm(fixup);
        if (acroForm == null)
        {
            return;
        }
        final Set<COSDictionary> dictionarySet = new HashSet<>();
        for (final PDAnnotation annotation : page.getAnnotations())
        {
            dictionarySet.add(annotation.getCOSObject());
        }
        for (final PDField field : acroForm.getFieldTree())
        {
            for (final PDAnnotationWidget widget : field.getWidgets())
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
        final String lbl = PDFDebugger.getPageLabel(document, pageIndex);
        if (lbl != null)
        {
            pageLabelText += " - " + lbl;
        }

        final JLabel pageLabel = new JLabel(pageLabelText);
        pageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pageLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 20));
        pageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.add(pageLabel);
        
        label = new JLabel();
        label.addMouseMotionListener(this);
        label.addMouseListener(this);
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
    public void actionPerformed(final ActionEvent actionEvent)
    {
        final String actionCommand = actionEvent.getActionCommand();
        if (ViewMenu.isRepairAcroformEvent(actionEvent))
        {
            final PDDocumentFixup fixup = ViewMenu.isRepairAcroformSelected() ? new AcroFormDefaultFixup(document) : null;
            document.getDocumentCatalog().getAcroForm(fixup);
            startRendering();
        }
        else if (ZoomMenu.isZoomMenu(actionCommand) ||
            RotationMenu.isRotationMenu(actionCommand) ||
            ImageTypeMenu.isImageTypeMenu(actionCommand) ||
            RenderDestinationMenu.isRenderDestinationMenu(actionCommand) ||
            ViewMenu.isRenderingOption(actionCommand))
        {
            startRendering();
        }
        else if (ViewMenu.isExtractTextEvent(actionEvent))
        {
            startExtracting();
        }
    }

    private void startExtracting()
    {
        final GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        final int screenWidth = gd.getDisplayMode().getWidth();
        final int screenHeight = gd.getDisplayMode().getHeight();

        final TextDialog textDialog = TextDialog.instance();
        textDialog.setSize(screenWidth / 3, screenHeight / 3);
        textDialog.setVisible(true);

        // avoid that the text extraction window gets outside of the screen
        final int x = Math.min(getPanel().getLocationOnScreen().x + getPanel().getWidth() / 2, screenWidth * 3 / 4);
        final int y = Math.min(getPanel().getLocationOnScreen().y + getPanel().getHeight() / 2, screenHeight * 3 / 4);
        textDialog.setLocation(x, y);

        try
        {
            final PDFTextStripper stripper = new PDFTextStripper();
            stripper.setStartPage(pageIndex + 1);
            stripper.setEndPage(pageIndex + 1);
            textDialog.setText(stripper.getText(document));
        }
        catch (IOException ex)
        {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private void startRendering()
    {
        // render in a background thread: rendering is read-only, so this should be ok, despite
        // the fact that PDDocument is not officially thread safe
        new RenderWorker().execute();
        zoomMenu.setPageZoomScale(ZoomMenu.getZoomScale());
    }

    @Override
    public void ancestorAdded(final AncestorEvent ancestorEvent)
    {
        zoomMenu.addMenuListeners(this);
        zoomMenu.setEnableMenu(true);
        
        rotationMenu = RotationMenu.getInstance();
        rotationMenu.addMenuListeners(this);
        rotationMenu.setEnableMenu(true);

        imageTypeMenu = ImageTypeMenu.getInstance();
        imageTypeMenu.addMenuListeners(this);
        imageTypeMenu.setEnableMenu(true);

        renderDestinationMenu = RenderDestinationMenu.getInstance();
        renderDestinationMenu.addMenuListeners(this);
        renderDestinationMenu.setEnableMenu(true);

        viewMenu = ViewMenu.getInstance(null);

        final JMenu menuInstance = viewMenu.getMenu();
        final int itemCount = menuInstance.getItemCount();
        
        for (int i = 0; i< itemCount; i++)
        {
            final JMenuItem item = menuInstance.getItem(i);
            if (item != null)
            {
                item.setEnabled(true);
                item.addActionListener(this);
            }
        }
    }

    @Override
    public void ancestorRemoved(final AncestorEvent ancestorEvent)
    {
        boolean isFirstEntrySkipped = false;
        zoomMenu.setEnableMenu(false);
        rotationMenu.setEnableMenu(false);
        imageTypeMenu.setEnableMenu(false);
        renderDestinationMenu.setEnableMenu(false);

        final JMenu menuInstance = viewMenu.getMenu();
        final int itemCount = menuInstance.getItemCount();
        
        for (int i = 0; i< itemCount; i++)
        {
            final JMenuItem item = menuInstance.getItem(i);
            // skip the first JMenuItem as this shall always be shown
            if (item != null)
            {
                if (!isFirstEntrySkipped)
                {
                    isFirstEntrySkipped = true;
                }
                else
                {
                    item.setEnabled(false);
                    item.removeActionListener(this);
                }
            }
        }
    }

    @Override
    public void ancestorMoved(final AncestorEvent ancestorEvent)
    {
        // do nothing
    }

    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // do nothing
    }

    /**
     * Catch mouse event to display cursor position in PDF coordinates in the status bar.
     *
     * @param e mouse event with position
     */
    @Override
    public void mouseMoved(final MouseEvent e)
    {
        final float height = page.getCropBox().getHeight();
        final float width  = page.getCropBox().getWidth();
        final float offsetX = page.getCropBox().getLowerLeftX();
        final float offsetY = page.getCropBox().getLowerLeftY();
        final float zoomScale = zoomMenu.getPageZoomScale();
        final float x = e.getX() / zoomScale * (float) defaultTransform.getScaleX();
        final float y = e.getY() / zoomScale * (float) defaultTransform.getScaleY();
        final int x1;
        final int y1;
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

        // are we in a field widget or a link annotation?
        Cursor cursor = Cursor.getDefaultCursor();
        currentURI = "";
        for (final Entry<PDRectangle,String> entry : rectMap.entrySet())
        {
            if (entry.getKey().contains(x1, y1))
            {
                final String s = rectMap.get(entry.getKey());
                text += ", " + s;
                if (s.startsWith("URI: "))
                {
                    currentURI = s.substring(5);
                    cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                }
                break;
            }
        }
        panel.setCursor(cursor);

        statuslabel.setText(text);
    }

    @Override
    public void mouseClicked(final MouseEvent e)
    {
        if (!currentURI.isEmpty() &&
            Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                Desktop.getDesktop().browse(new URI(currentURI));
            }
            catch (URISyntaxException | IOException ex)
            {
                new ErrorDialog(ex).setVisible(true);
            }
        }
    }

    @Override
    public void mousePressed(final MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseEntered(final MouseEvent e)
    {
        // do nothing
    }

    @Override
    public void mouseExited(final MouseEvent e)
    {
        statuslabel.setText(labelText);
    }

    /**
     * Note that PDDocument is not officially thread safe, caution advised.
     */
    private final class RenderWorker extends SwingWorker<BufferedImage, Integer>
    {
        @Override
        protected BufferedImage doInBackground() throws IOException
        {
            // rendering can take a long time, so remember all options that are used later
            final float scale = ZoomMenu.getZoomScale();
            final boolean showTextStripper = ViewMenu.isShowTextStripper();
            final boolean showTextStripperBeads = ViewMenu.isShowTextStripperBeads();
            final boolean showFontBBox = ViewMenu.isShowFontBBox();
            final int rotation = RotationMenu.getRotationDegrees();

            label.setIcon(null);
            labelText = "Rendering...";
            label.setText(labelText);
            statuslabel.setText(labelText);

            final PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(ViewMenu.isAllowSubsampling());

            final long t0 = System.nanoTime();
            final BufferedImage image = renderer.renderImage(pageIndex, scale, ImageTypeMenu.getImageType(), RenderDestinationMenu.getRenderDestination());
            final long t1 = System.nanoTime();

            final long ms = TimeUnit.MILLISECONDS.convert(t1 - t0, TimeUnit.NANOSECONDS);
            labelText = "Rendered in " + ms + " ms";
            statuslabel.setText(labelText);

            // debug overlays
            final DebugTextOverlay debugText = new DebugTextOverlay(document, pageIndex, scale,
                                                              showTextStripper, showTextStripperBeads,
                                                              showFontBBox, ViewMenu.isShowGlyphBounds());
            final Graphics2D g = image.createGraphics();
            debugText.renderTo(g);
            g.dispose();
            
            return ImageUtil.getRotatedImage(image, rotation);
        }

        @Override
        protected void done()
        {
            try
            {
                final BufferedImage image = get();

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
            catch (InterruptedException | ExecutionException e)
            {
                label.setText(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
