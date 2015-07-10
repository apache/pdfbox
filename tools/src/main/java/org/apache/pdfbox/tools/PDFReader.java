/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pdfbox.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.AbstractAction;

import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.printing.PDFPageable;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.util.RecentFiles;
import org.apache.pdfbox.tools.gui.PageWrapper;
import org.apache.pdfbox.tools.gui.ReaderBottomPanel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

/**
 * An proof-of-concept application to read PDF documents, with very basic functionality.
 * @author Ben Litchfield
 */
public class PDFReader extends JFrame
{
    private File currentDir = new File(".");
    private JMenuItem saveAsImageMenuItem;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenuBar menuBar;
    private JMenuItem openMenuItem;
    private JMenu recentFileSubmenu;
    private JMenuItem printMenuItem;
    private JMenu viewMenu;
    private JMenuItem nextPageItem;
    private JMenuItem previousPageItem;
    private JMenuItem firstPageItem;
    private JMenuItem lastPageItem;
    private final JPanel documentPanel = new JPanel();
    private final ReaderBottomPanel bottomStatusPanel = new ReaderBottomPanel();

    private PDFRenderer renderer;
    private PDDocument document = null;
    private PDPageTree pages = null;

    private int currentPage = 0;
    private int numberOfPages = 0;
    private String currentFilename = null;
    private String currentFilePath = null;

    private static final String PASSWORD = "-password";

    private static final String VERSION = Version.getVersion();
    private static final String BASETITLE = "PDFBox " + VERSION;

    private static final String PREVIOUS_PAGE = "previous_page";
    private static final String NEXT_PAGE = "next_page";
    private static final String FIRST_PAGE = "first_page";
    private static final String LAST_PAGE = "last_page";

    private RecentFiles recentFiles;

    /**
     * Constructor.
     */
    public PDFReader()
    {
        initComponents();
    }

    // This method is called from within the consructor to initialize the form.
    private void initComponents()
    {
        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        recentFileSubmenu = new JMenu();
        openMenuItem = new JMenuItem();
        saveAsImageMenuItem = new JMenuItem();
        exitMenuItem = new JMenuItem();
        printMenuItem = new JMenuItem();
        viewMenu = new JMenu();
        nextPageItem = new JMenuItem();
        previousPageItem = new JMenuItem();
        firstPageItem = new JMenuItem();
        lastPageItem = new JMenuItem();

        setTitle(BASETITLE);
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                exitApplication();
            }
        });

        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView(documentPanel);

        getContentPane().add(documentScroller, java.awt.BorderLayout.CENTER);
        getContentPane().add(bottomStatusPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");
        openMenuItem.setText("Open");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        openMenuItem.setToolTipText("Open PDF file");
        openMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        try
        {
            recentFiles = new RecentFiles(this.getClass(), 5);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        recentFileSubmenu.setText("Open recent Files");
        recentFileSubmenu.setEnabled(false);
        addRecentFileItems();
        fileMenu.add(recentFileSubmenu);
        fileMenu.addSeparator();
        printMenuItem.setText("Print");
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P,
                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        printMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                try
                {
                    if (document != null)
                    {
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPageable(new PDFPageable(document));
                        if (job.printDialog())
                        {
                            job.print();
                        }
                    }
                }
                catch (PrinterException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });
        fileMenu.add(printMenuItem);

        saveAsImageMenuItem.setText("Save as image");
        saveAsImageMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                if (document != null)
                {
                    try
                    {
                        saveImage();
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        fileMenu.add(saveAsImageMenuItem);

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                exitApplication();
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        Action previousPageAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    previousPage();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        Action nextPageAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    nextPage();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        Action firstPageAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    firstPage();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        Action lastPageAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                try
                {
                    lastPage();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        viewMenu.setText("View");
        nextPageItem.setText("Next page");
        nextPageItem.setAccelerator(KeyStroke.getKeyStroke('+'));
        nextPageItem.addActionListener(nextPageAction);
        nextPageItem.setEnabled(false);
        viewMenu.add(nextPageItem);

        previousPageItem.setText("Previous page");
        previousPageItem.setAccelerator(KeyStroke.getKeyStroke('-'));
        previousPageItem.addActionListener(previousPageAction);
        previousPageItem.setEnabled(false);
        viewMenu.add(previousPageItem);

        viewMenu.addSeparator();
        firstPageItem.setText("First Page");
        firstPageItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_HOME,
                                     Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        firstPageItem.addActionListener(firstPageAction);
        firstPageItem.setEnabled(false);
        viewMenu.add(firstPageItem);

        lastPageItem.setText("Last Page");
        lastPageItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_END,
                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        lastPageItem.addActionListener(lastPageAction);
        lastPageItem.setEnabled(false);
        viewMenu.add(lastPageItem);

        menuBar.add(viewMenu);

        setJMenuBar(menuBar);

        InputMap documentInputMap = documentPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        documentInputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), PREVIOUS_PAGE);
        documentInputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), NEXT_PAGE);
        documentInputMap.put(KeyStroke.getKeyStroke("HOME"), FIRST_PAGE);
        documentInputMap.put(KeyStroke.getKeyStroke("END"), LAST_PAGE);

        ActionMap documentActionMap = documentPanel.getActionMap();
        documentActionMap.put(PREVIOUS_PAGE, previousPageAction);
        documentActionMap.put(NEXT_PAGE, nextPageAction);
        documentActionMap.put(FIRST_PAGE, firstPageAction);
        documentActionMap.put(LAST_PAGE, lastPageAction);

        JScrollBar verticalScroller = documentScroller.getVerticalScrollBar();
        verticalScroller.setUnitIncrement(20);
        InputMap scrollerInputMap = verticalScroller.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        scrollerInputMap.put(KeyStroke.getKeyStroke("DOWN"), "positiveUnitIncrement");
        scrollerInputMap.put(KeyStroke.getKeyStroke("UP"), "negativeUnitIncrement");

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width - 700) / 2, (screenSize.height - 600) / 2, 700, 600);
    }

    private void addRecentFileItems()
    {
        Action recentMenuAction = new AbstractAction()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                String filePath = (String) ((JComponent) actionEvent.getSource()).getClientProperty("path");
                try
                {
                    openPDFFile(filePath, "");
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };

        MouseListener mouseListener = new MouseListener()
        {
            @Override
            public void mouseClicked(MouseEvent mouseEvent)
            {
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent)
            {
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent)
            {
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent)
            {
                String filePath = (String) ((JComponent) mouseEvent.getSource()).getClientProperty("path");
                bottomStatusPanel.getStatusLabel().setText(filePath);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent)
            {
                bottomStatusPanel.getStatusLabel().setText("");
            }
        };

        if (!recentFiles.isEmpty())
        {
            recentFileSubmenu.removeAll();
            List<String> files = recentFiles.getFiles();
            for (int i = files.size() - 1; i >= 0; i--)
            {
                String path = files.get(i);
                String name = new File(path).getName();
                JMenuItem recentFileMenuItem = new JMenuItem(name);
                recentFileMenuItem.putClientProperty("path", path);
                recentFileMenuItem.addActionListener(recentMenuAction);
                recentFileMenuItem.addMouseListener(mouseListener);
                recentFileSubmenu.add(recentFileMenuItem);
            }
            recentFileSubmenu.setEnabled(true);
        }
    }

    private void updateTitle()
    {
        setTitle(BASETITLE + ": " + currentFilename + " " +
                "(" + (currentPage + 1) + "/" + numberOfPages + ")");
    }

    private void nextPage() throws IOException
    {
        if (currentPage < numberOfPages - 1)
        {
            currentPage++;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void previousPage() throws IOException
    {
        if (currentPage > 0)
        {
            currentPage--;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void firstPage() throws IOException
    {
        if (currentPage > 0)
        {
            currentPage = 0;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void lastPage() throws IOException
    {
        if (currentPage < numberOfPages - 1)
        {
            currentPage = numberOfPages - 1;
            updateTitle();
            showPage(currentPage);
        }
    }

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt)
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(currentDir);

        ExtensionFileFilter pdfFilter = new ExtensionFileFilter(new String[] {"PDF"}, "PDF Files");
        chooser.setFileFilter(pdfFilter);
        int result = chooser.showOpenDialog(PDFReader.this);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            String name = chooser.getSelectedFile().getPath();
            currentDir = new File(name).getParentFile();
            try
            {
                openPDFFile(name, "");
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    private void exitApplication()
    {
        try
        {
            if (document != null)
            {
                document.close();
                recentFiles.addFile(currentFilePath);
            }
            recentFiles.close();
        }
        catch (IOException io)
        {
            // do nothing because we are closing the application
        }
        this.setVisible(false);
        this.dispose();
    }

    /**
     * Entry point.
     * @param args the command line arguments
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        PDFReader viewer = new PDFReader();
        String password = "";
        String filename = null;
        for (int i = 0; i < args.length; i++)
        {
            if (args[i].equals(PASSWORD))
            {
                i++;
                if (i >= args.length)
                {
                    usage();
                }
                password = args[i];
            }
            else
            {
                filename = args[i];
            }
        }
        // open the pdf if present
        if (filename != null)
        {
            viewer.openPDFFile(filename, password);
        }
        viewer.setVisible(true);
    }

    private void openPDFFile(String filename, String password) throws IOException
    {
        if (document != null)
        {
            document.close();
            documentPanel.removeAll();
            recentFiles.addFile(currentFilePath);
        }

        File file = new File(filename);
        currentFilePath = file.getPath();
        recentFiles.removeFile(file.getPath());
        parseDocument(file, password);
        pages = document.getPages();
        numberOfPages = document.getNumberOfPages();
        currentFilename = file.getAbsolutePath();
        currentPage = 0;
        updateTitle();
        showPage(0);
        addRecentFileItems();
    }

    private void showPage(int pageNumber) throws IOException
    {
        PageWrapper wrapper = new PageWrapper(getBottomStatusPanel().getStatusLabel());
        wrapper.displayPage(renderer, pages.get(pageNumber), pageNumber);
        if (documentPanel.getComponentCount() > 0)
        {
            documentPanel.remove(0);
        }
        documentPanel.add(wrapper.getPanel());
        pack();
        updateViewMenu();
    }

    private void updateViewMenu()
    {
        if (currentPage == 0)
        {
            nextPageItem.setEnabled(true);
            previousPageItem.setEnabled(false);
            firstPageItem.setEnabled(false);
            lastPageItem.setEnabled(true);
        }
        else if (currentPage == numberOfPages - 1)
        {
            nextPageItem.setEnabled(false);
            previousPageItem.setEnabled(true);
            firstPageItem.setEnabled(true);
            lastPageItem.setEnabled(false);
        }
        else
        {
            nextPageItem.setEnabled(true);
            previousPageItem.setEnabled(true);
            firstPageItem.setEnabled(true);
            lastPageItem.setEnabled(true);
        }
    }

    private void saveImage() throws IOException
    {
        BufferedImage pageAsImage = renderer.renderImage(currentPage);
        String imageFilename = currentFilename;
        if (imageFilename.toLowerCase().endsWith(".pdf"))
        {
            imageFilename = imageFilename.substring(0, imageFilename.length() - 4);
        }
        imageFilename += "_" + (currentPage + 1);
        ImageIOUtil.writeImage(pageAsImage, imageFilename + ".png", 300);
    }

    private void parseDocument(File file, String password) throws IOException
    {
        document = PDDocument.load(file, password);
        renderer = new PDFRenderer(document);
    }

    /**
     * Get the bottom status panel.
     * @return The bottom status panel.
     */
    public ReaderBottomPanel getBottomStatusPanel()
    {
        return bottomStatusPanel;
    }

    private static void usage()
    {
        System.err.println(
                "usage: java -jar pdfbox-app-" + VERSION + ".jar PDFReader [OPTIONS] <input-file>\n"
                + "  -password <password>      Password to decrypt the document\n"
                + "  <input-file>              The PDF document to be loaded\n");
    }
}
