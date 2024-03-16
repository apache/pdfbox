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
package org.apache.pdfbox.debugger;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.imageio.spi.IIORegistry;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSBoolean;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSFloat;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNull;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.cos.COSString;
import org.apache.pdfbox.debugger.colorpane.CSArrayBased;
import org.apache.pdfbox.debugger.colorpane.CSDeviceN;
import org.apache.pdfbox.debugger.colorpane.CSIndexed;
import org.apache.pdfbox.debugger.colorpane.CSSeparation;
import org.apache.pdfbox.debugger.flagbitspane.FlagBitsPane;
import org.apache.pdfbox.debugger.fontencodingpane.FontEncodingPaneController;
import org.apache.pdfbox.debugger.pagepane.PagePane;
import org.apache.pdfbox.debugger.streampane.StreamPane;
import org.apache.pdfbox.debugger.stringpane.StringPane;
import org.apache.pdfbox.debugger.treestatus.TreeStatus;
import org.apache.pdfbox.debugger.treestatus.TreeStatusPane;
import org.apache.pdfbox.debugger.ui.ArrayEntry;
import org.apache.pdfbox.debugger.ui.DocumentEntry;
import org.apache.pdfbox.debugger.ui.ErrorDialog;
import org.apache.pdfbox.debugger.ui.FileOpenSaveDialog;
import org.apache.pdfbox.debugger.ui.ImageTypeMenu;
import org.apache.pdfbox.debugger.ui.LogDialog;
import org.apache.pdfbox.debugger.ui.MapEntry;
import org.apache.pdfbox.debugger.ui.OSXAdapter;
import org.apache.pdfbox.debugger.ui.PDFTreeCellRenderer;
import org.apache.pdfbox.debugger.ui.PDFTreeModel;
import org.apache.pdfbox.debugger.ui.PageEntry;
import org.apache.pdfbox.debugger.ui.PrintDpiMenu;
import org.apache.pdfbox.debugger.ui.ReaderBottomPanel;
import org.apache.pdfbox.debugger.ui.RecentFiles;
import org.apache.pdfbox.debugger.ui.RenderDestinationMenu;
import org.apache.pdfbox.debugger.ui.RotationMenu;
import org.apache.pdfbox.debugger.ui.TextDialog;
import org.apache.pdfbox.debugger.ui.Tree;
import org.apache.pdfbox.debugger.ui.TreeViewMenu;
import org.apache.pdfbox.debugger.ui.ViewMenu;
import org.apache.pdfbox.debugger.ui.WindowPrefs;
import org.apache.pdfbox.debugger.ui.XrefEntries;
import org.apache.pdfbox.debugger.ui.XrefEntry;
import org.apache.pdfbox.debugger.ui.ZoomMenu;
import org.apache.pdfbox.filter.FilterFactory;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences;
import org.apache.pdfbox.printing.Orientation;
import org.apache.pdfbox.printing.PDFPageable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

/**
 * PDF Debugger.
 * 
 * @author wurtz
 * @author Ben Litchfield
 * @author Khyrul Bashar
 */
@SuppressWarnings({ "serial", "squid:MaximumInheritanceDepth", "squid:S1948" })
@Command(name = "pdfdebugger", description = "Analyzes and inspects the internal structure of a PDF document")
public class PDFDebugger extends JFrame implements Callable<Integer>, HyperlinkListener
{
    private static Log LOG; // needs late initialization

    private static final Set<COSName> SPECIALCOLORSPACES = new HashSet<>(
            Arrays.asList(COSName.INDEXED, COSName.SEPARATION, COSName.DEVICEN));

    private static final Set<COSName> OTHERCOLORSPACES = new HashSet<>(
            Arrays.asList(COSName.ICCBASED, COSName.PATTERN, COSName.CALGRAY, COSName.CALRGB, COSName.LAB));

    private static final FileFilter PDF_FILTER = new FileNameExtensionFilter("PDF Files (*.pdf)", "pdf", "PDF");

    private int shortcutKeyMask;
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_MAC_OS = OS_NAME.startsWith("mac os x");

    private final JPanel documentPanel = new JPanel();
    private TreeStatusPane statusPane;
    private RecentFiles recentFiles;
    private WindowPrefs windowPrefs;
    private PDDocument document;
    private String currentFilePath;
    private JScrollPane jScrollPaneRight;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTextPane jTextPane;
    private ReaderBottomPanel statusBar;
    private Tree tree;
    // file menu
    private JMenuItem saveAsMenuItem;
    private JMenu recentFilesMenu;
    private JMenuItem printMenuItem;
    private JMenu printDpiMenu;
    private JMenuItem reopenMenuItem;

    // edit > find menu
    private JMenu findMenu;
    private JMenuItem findMenuItem;
    private JMenuItem findNextMenuItem;
    private JMenuItem findPreviousMenuItem;

    // current view mode of the tree
    private String currentTreeViewMode = TreeViewMenu.VIEW_PAGES;

    // cli options
    // Expected for CLI app to write to System.out/System.err
    @SuppressWarnings("squid:S106")
    private final PrintStream SYSERR;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;

    @Option(names = "-password", description = "password to decrypt the document", arity = "0..1", interactive = true)
    private String password;

    @Option(names = "-viewstructure", description = "activate structure mode on startup")
    private boolean viewstructure = false;

    @Parameters(paramLabel = "inputfile", arity="0..1", description = "the PDF file to be loaded")
    private File infile;

    // configuration
    public static final Properties configuration = new Properties();

    @Spec CommandSpec spec;

    /**
     * Constructor.
     */
    public PDFDebugger()
    {
        SYSERR = System.err;
        if (viewstructure)
        {
            currentTreeViewMode = TreeViewMenu.VIEW_STRUCTURE;
        }
    }

    /**
     * Constructor.
     *
     * @param initialViewMode initial view mode for the tree view on the left hand side.
     * 
     */
    public PDFDebugger(String initialViewMode)
    {
        SYSERR = System.err;
        if (TreeViewMenu.isValidViewMode(initialViewMode))
        {
            currentTreeViewMode = initialViewMode;
        }
        else
        {
            SYSERR.println("Onknown view mode " + initialViewMode);
        }
    }

    /**
     * Entry point.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        int exitCode = new CommandLine(new PDFDebugger()).execute(args);
        if (exitCode > 0)
        {
            System.exit(exitCode);
        }
    }

    @Override
    public Integer call()
    {
        try
        {
            // can't be initialized earlier because it's an awt call which would fail when
            // PDFBox.main runs on a headless system
            shortcutKeyMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (System.getProperty("apple.laf.useScreenMenuBar") == null)
            {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
            }
    
            // handle uncaught exceptions
            Thread.setDefaultUncaughtExceptionHandler(
                    (thread, throwable) -> new ErrorDialog(throwable).setVisible(true));
    
            loadConfiguration();
            initComponents();

            // use our custom logger
            // this works only if there is no earlier "LogFactory.getLog()" in this class,
            // and if there are no methods that call logging, even invisible
            // use reduced file from PDFBOX-3653 to see logging
            LogDialog.init(this,statusBar.getLogLabel());
            System.setProperty("org.apache.commons.logging.Log", "org.apache.pdfbox.debugger.ui.DebugLog");
            LOG = LogFactory.getLog(PDFDebugger.class);

            TextDialog.init(this);

            // trigger premature initializations for more accurate rendering benchmarks
            // See discussion in PDFBOX-3988
            PDDeviceCMYK.INSTANCE.toRGB(new float[] { 0, 0, 0, 0 });
            PDDeviceRGB.INSTANCE.toRGB(new float[] { 0, 0, 0 });
            IIORegistry.getDefaultInstance();
            FilterFactory.INSTANCE.getFilter(COSName.FLATE_DECODE);

            if (infile != null && infile.exists())
            {
                readPDFFile(infile, password);
            }

            setVisible(true);
        }
        catch (Exception ex)
        {
            SYSERR.println( "Error viewing document: " + ex.getMessage());
            return 4;
        }
        return 0;
    }

    /**
     * Provide the current view mode of the tree view. see {@link TreeViewMenu} for valid values
     */
    public String getTreeViewMode()
    {
        return currentTreeViewMode;
    }

    /**
     * Set the current view mode of the tree view. see {@link TreeViewMenu} for valid values
     * 
     * @param viewMode the view mode to be set
     * 
     */
    public void setTreeViewMode(String viewMode)
    {
        if (TreeViewMenu.isValidViewMode(viewMode))
        {
            currentTreeViewMode = viewMode;
        }
    }

    public boolean hasDocument()
    {
        return document != null;
    }
    
    /**
     * Loads the local configuration file, if any.
     */
    private void loadConfiguration()
    {
        File file = new File("config.properties");
        if (file.exists())
        {
            try
            {
                try (InputStream is = new FileInputStream(file))
                {
                    configuration.load(is);
                }
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents()
    {
        jSplitPane = new javax.swing.JSplitPane();
        JScrollPane jScrollPaneLeft = new JScrollPane();
        tree = new Tree();
        jScrollPaneRight = new JScrollPane();
        jTextPane = new javax.swing.JTextPane();
        
        tree.setCellRenderer(new PDFTreeCellRenderer());
        tree.setModel(null);

        setTitle("Apache PDFBox Debugger");

        addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowOpened(WindowEvent windowEvent)
            {
                tree.requestFocusInWindow();
                super.windowOpened(windowEvent);
            }

            @Override
            public void windowClosing(WindowEvent evt)
            {
                exitMenuItemActionPerformed(null);
            }
        });

        windowPrefs = new WindowPrefs(this.getClass());

        jScrollPaneLeft.setBorder(new BevelBorder(BevelBorder.RAISED));
        jSplitPane.setDividerLocation(windowPrefs.getDividerLocation());
        tree.addTreeSelectionListener(this::jTree1ValueChanged);

        jScrollPaneLeft.setViewportView(tree);

        jSplitPane.setRightComponent(jScrollPaneRight);
        jSplitPane.setDividerSize(3);

        jScrollPaneRight.setViewportView(jTextPane);

        jSplitPane.setLeftComponent(jScrollPaneLeft);

        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView(documentPanel);

        statusPane = new TreeStatusPane(tree);
        statusPane.getPanel().setBorder(new BevelBorder(BevelBorder.RAISED));
        Dimension preferredTreePathSize = statusPane.getPanel().getPreferredSize();
        int treePathHeight = (int) Math.round(preferredTreePathSize.getHeight());
        treePathHeight = Integer.parseInt(
                configuration.getProperty("treePathHeight", Integer.toString(treePathHeight)));
        preferredTreePathSize.height = treePathHeight;
        statusPane.getPanel().setPreferredSize(preferredTreePathSize);
        getContentPane().add(statusPane.getPanel(), BorderLayout.PAGE_START);

        getContentPane().add(jSplitPane, BorderLayout.CENTER);

        statusBar = new ReaderBottomPanel();
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        // create menus
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        
        ViewMenu viewMenu = ViewMenu.getInstance(this);
        menuBar.add(viewMenu.getMenu());
        setJMenuBar(menuBar);

        menuBar.add(Box.createHorizontalGlue());
        JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);

        JMenuItem item = new JMenuItem("About PDFBox", KeyEvent.VK_A);
        item.setActionCommand("about");
        item.addActionListener(actionEvent ->
                textDialog("About Apache PDFBox", PDFDebugger.class.getResource("about.html")));
        help.add(item);

        menuBar.add(help);

        setExtendedState(windowPrefs.getExtendedState());
        setBounds(windowPrefs.getBounds());

        // drag and drop to open files
        setTransferHandler(new TransferHandler()
        {
            @Override
            public boolean canImport(TransferSupport transferSupport)
            {
                return transferSupport.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
            }

            @Override
            @SuppressWarnings("unchecked")
            public boolean importData(TransferSupport transferSupport)
            {
                try
                {
                    Transferable transferable = transferSupport.getTransferable();
                    List<File> files = (List<File>) transferable.getTransferData(
                            DataFlavor.javaFileListFlavor);
                    readPDFFile(files.get(0), "");
                }
                catch (UnsupportedFlavorException e)
                {
                    new ErrorDialog(e).setVisible(true);
                    return false;
                }
                catch (Exception e)
                {
                    new ErrorDialog(e).setVisible(true);
                }
                return true;
            }
        });

        initGlobalEventHandlers();

    }

    /**
     * Initialize application global event handlers.
     * Protected to allow subclasses to override this method if they
     * don't want the global event handler overridden.
     */
    @SuppressWarnings("WeakerAccess")
    protected void initGlobalEventHandlers()
    {
        // Mac OS X file open/quit handler
        if (IS_MAC_OS)
        {
            try
            {
                Method osxOpenFiles = getClass().getDeclaredMethod("osxOpenFiles", String.class);
                osxOpenFiles.setAccessible(true);
                OSXAdapter.setFileHandler(this, osxOpenFiles);

                Method osxQuit = getClass().getDeclaredMethod("osxQuit");
                osxQuit.setAccessible(true);
                OSXAdapter.setQuitHandler(this, osxQuit);
            }
            catch (NoSuchMethodException e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    private JMenu createFileMenu()
    {
        JMenuItem openMenuItem = new JMenuItem("Open...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, shortcutKeyMask));
        openMenuItem.addActionListener(this::openMenuItemActionPerformed);

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(openMenuItem);
        fileMenu.setMnemonic('F');

        JMenuItem openUrlMenuItem = new JMenuItem("Open URL...");
        openUrlMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, shortcutKeyMask));
        openUrlMenuItem.addActionListener(evt ->
        {
            String urlString = JOptionPane.showInputDialog("Enter an URL");
            if (urlString == null || urlString.isEmpty())
            {
                return;
            }
            try
            {
                readPDFurl(urlString, "");
            }
            catch (IOException | URISyntaxException e)
            {
                throw new RuntimeException(e);
            }
        });
        fileMenu.add(openUrlMenuItem);
        
        reopenMenuItem = new JMenuItem("Reopen");
        reopenMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, shortcutKeyMask));
        reopenMenuItem.addActionListener(evt ->
        {
            try
            {
                if (currentFilePath.startsWith("http") || currentFilePath.startsWith("file:"))
                {
                    readPDFurl(currentFilePath, "");
                }
                else
                {
                    readPDFFile(currentFilePath, "");
                }
            }
            catch (IOException | URISyntaxException e)
            {
                new ErrorDialog(e).setVisible(true);
            }
        });
        reopenMenuItem.setEnabled(false);
        fileMenu.add(reopenMenuItem);

        recentFiles = new RecentFiles(this.getClass(), 5);
        recentFilesMenu = new JMenu("Open Recent");
        recentFilesMenu.setEnabled(false);
        addRecentFileItems();
        fileMenu.add(recentFilesMenu);

        saveAsMenuItem = new JMenuItem("Save as...");
        saveAsMenuItem.addActionListener(this::saveAsMenuItemActionPerformed);
        saveAsMenuItem.setEnabled(false);        
        fileMenu.add(saveAsMenuItem);

        printMenuItem = new JMenuItem("Print");
        printMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, shortcutKeyMask));
        printMenuItem.setEnabled(false);
        printMenuItem.addActionListener(this::printMenuItemActionPerformed);

        fileMenu.addSeparator();
        fileMenu.add(printMenuItem);

        printDpiMenu = PrintDpiMenu.getInstance().getMenu();
        printDpiMenu.setEnabled(false);
        fileMenu.add(printDpiMenu);

        if (!IS_MAC_OS)
        {
            JMenuItem exitMenuItem = new JMenuItem("Exit");
            exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
            exitMenuItem.addActionListener(this::exitMenuItemActionPerformed);

            fileMenu.addSeparator();
            fileMenu.add(exitMenuItem);
        }
        
        return fileMenu;
    }
    
    private JMenu createEditMenu()
    {
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.setEnabled(false);
        editMenu.add(cutMenuItem);

        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.setEnabled(false);
        editMenu.add(copyMenuItem);

        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.setEnabled(false);
        editMenu.add(pasteMenuItem);

        JMenuItem deleteMenuItem = new JMenuItem("Delete");
        deleteMenuItem.setEnabled(false);
        editMenu.add(deleteMenuItem);
        editMenu.addSeparator();
        editMenu.add(createFindMenu());
        
        return editMenu;
    }

    private JMenu createFindMenu()
    {
        findMenu = new JMenu("Find");
        findMenu.setEnabled(false);
        
        findMenuItem = new JMenuItem("Find...");
        findMenuItem.setActionCommand("find");
        findMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, shortcutKeyMask));
        
        findNextMenuItem = new JMenuItem("Find Next");
        if (IS_MAC_OS)
        {
            findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, shortcutKeyMask));
        }
        else
        {
            findNextMenuItem.setAccelerator(KeyStroke.getKeyStroke("F3"));
        }

        findPreviousMenuItem = new JMenuItem("Find Previous");
        if (IS_MAC_OS)
        {
            findPreviousMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_G, shortcutKeyMask | InputEvent.SHIFT_DOWN_MASK));
        }
        else
        {
            findPreviousMenuItem.setAccelerator(KeyStroke.getKeyStroke(
                    KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK));
        }
        
        findMenu.add(findMenuItem);
        findMenu.add(findNextMenuItem);
        findMenu.add(findPreviousMenuItem);
        
        return findMenu;
    }

    /**
     * Returns the File menu.
     * 
     * @return the File menu
     */
    public JMenu getFindMenu()
    {
        return findMenu;
    }

    /**
     * Returns the Edit &gt; Find &gt; Find menu item.
     * 
     * @return the Find menu
     */
    public JMenuItem getFindMenuItem()
    {
        return findMenuItem;
    }
    
    /**
     * Returns the Edit &gt; Find &gt; Find Next menu item.
     * 
     * @return the FindNext menu
     */
    public JMenuItem getFindNextMenuItem()
    {
        return findNextMenuItem;
    }

    /**
     * Returns the Edit &gt; Find &gt; Find Previous menu item.
     * 
     * @return the FindPrevious menu
     */
    public JMenuItem getFindPreviousMenuItem()
    {
        return findPreviousMenuItem;
    }

    /**
     * This method is called via reflection on Mac OS X.
     */
    private void osxOpenFiles(String filename)
    {
        try
        {
            readPDFFile(filename, "");
        }
        catch (IOException | URISyntaxException e)
        {
            new ErrorDialog(e).setVisible(true);
        }
    }

    /**
     * This method is called via reflection on Mac OS X.
     */
    private void osxQuit()
    {
        exitMenuItemActionPerformed(null);
    }

    private void saveAsMenuItemActionPerformed(ActionEvent evt)
    {
        try
        {
            if (IS_MAC_OS)
            {
                FileDialog openDialog = new FileDialog(this, "Save", FileDialog.SAVE);
                openDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                openDialog.setVisible(true);
                String file = openDialog.getFile();
                if (file != null)
                {
                    document.setAllSecurityToBeRemoved(true);
                    document.save(file);
                }
            }
            else
            {
                FileOpenSaveDialog saveAsDialog = new FileOpenSaveDialog(this, PDF_FILTER);
                saveAsDialog.saveDocument(document, "pdf");
            }
        }
        catch (IOException e)
        {
            new ErrorDialog(e).setVisible(true);
        }
    }

    private void openMenuItemActionPerformed(ActionEvent evt)
    {
        try
        {
            if (IS_MAC_OS)
            {
                FileDialog openDialog = new FileDialog(this, "Open");
                openDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".pdf"));
                openDialog.setVisible(true);
                if (openDialog.getFile() != null)
                {
                    readPDFFile(new File(openDialog.getDirectory(),openDialog.getFile()), "");
                }
            }
            else
            {
                FileOpenSaveDialog openDialog = new FileOpenSaveDialog(this, PDF_FILTER);

                File file = openDialog.openFile();
                if (file != null)
                {
                    readPDFFile(file, "");
                }
            }
        }
        catch (IOException | URISyntaxException e)
        {
            new ErrorDialog(e).setVisible(true);
        }
    }

    private void jTree1ValueChanged(TreeSelectionEvent evt)
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            try
            {
                Object selectedNode = path.getLastPathComponent();
                
                statusBar.getStatusLabel().setText("");
                
                if (selectedNode instanceof XrefEntry)
                {
                    if (jSplitPane.getRightComponent() == null
                            || !jSplitPane.getRightComponent().equals(jScrollPaneRight))
                    {
                        replaceRightComponent(jScrollPaneRight);
                    }
                    jTextPane.setText(convertToString(selectedNode));
                    return;
                }

                if (isPage(selectedNode))
                {
                    showPage(selectedNode);
                    return;
                }
                
                if (isSpecialColorSpace(selectedNode) || isOtherColorSpace(selectedNode))
                {
                    showColorPane(selectedNode);
                    return;
                }
                if (path.getParentPath() != null
                        && isFlagNode(selectedNode, path.getParentPath().getLastPathComponent()))
                {
                    Object parentNode = path.getParentPath().getLastPathComponent();
                    showFlagPane(parentNode, selectedNode);
                    return;
                }
                if (isStream(selectedNode))
                {
                    showStream((COSStream) getUnderneathObject(selectedNode), path);
                    return;
                }
                if (isFont(selectedNode))
                {
                    showFont(selectedNode, path);
                    return;
                }
                if (isString(selectedNode))
                {
                    showString(selectedNode);
                    return;
                }
                if (jSplitPane.getRightComponent() == null
                        || !jSplitPane.getRightComponent().equals(jScrollPaneRight))
                {
                    replaceRightComponent(jScrollPaneRight);
                }
                jTextPane.setText(convertToString(selectedNode));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isSpecialColorSpace(Object selectedNode)
    {
        COSBase underneathObject = getUnderneathObject(selectedNode);

        if (underneathObject instanceof COSArray && ((COSArray) underneathObject).size() > 0)
        {
            COSBase arrayEntry = ((COSArray) underneathObject).get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName name = (COSName) arrayEntry;
                return SPECIALCOLORSPACES.contains(name);
            }
        }
        return false;
    }

    private boolean isOtherColorSpace(Object selectedNode)
    {
        COSBase underneathObject = getUnderneathObject(selectedNode);

        if (underneathObject instanceof COSArray && ((COSArray) underneathObject).size() > 0)
        {
            COSBase arrayEntry = ((COSArray) underneathObject).get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName name = (COSName) arrayEntry;
                return OTHERCOLORSPACES.contains(name);
            }
        }
        return false;
    }

    private boolean isPage(Object selectedNode)
    {
        COSBase underneathObject = getUnderneathObject(selectedNode);

        if (underneathObject instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary) underneathObject;
            COSBase typeItem = dict.getItem(COSName.TYPE);
            if (COSName.PAGE.equals(typeItem))
            {
                return true;
            }
        }
        return false;
    }

    private boolean isFlagNode(Object selectedNode, Object parentNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            Object key = ((MapEntry) selectedNode).getKey();
            return (COSName.FLAGS.equals(key) && isFontDescriptor(parentNode)) || 
                    (COSName.F.equals(key) && isAnnot(parentNode)) || 
                    COSName.FF.equals(key) || 
                    COSName.PANOSE.equals(key) ||
                    COSName.SIG_FLAGS.equals(key) ||
                    (COSName.P.equals(key) && isEncrypt(parentNode));
        }
        return false;
    }

    private boolean isEncrypt(Object obj)
    {
        if (obj instanceof MapEntry)
        {
            MapEntry entry = (MapEntry) obj;
            return COSName.ENCRYPT.equals(entry.getKey()) && entry.getValue() instanceof COSDictionary;
        }
        return false;
    }

    private boolean isFontDescriptor(Object obj)
    {
        COSBase underneathObject = getUnderneathObject(obj);
        return underneathObject instanceof COSDictionary &&
                COSName.FONT_DESC.equals(((COSDictionary) underneathObject).getCOSName(COSName.TYPE));
    }

    private boolean isAnnot(Object obj)
    {
        COSBase underneathObject = getUnderneathObject(obj);
        return underneathObject instanceof COSDictionary &&
                COSName.ANNOT.equals(((COSDictionary) underneathObject).getCOSName(COSName.TYPE));
    }

    private boolean isStream(Object selectedNode)
    {
        return getUnderneathObject(selectedNode) instanceof COSStream;
    }

    private boolean isString(Object selectedNode)
    {
        return getUnderneathObject(selectedNode) instanceof COSString;
    }

    private boolean isFont(Object selectedNode)
    {
        COSBase underneathObject = getUnderneathObject(selectedNode);
        if (underneathObject instanceof COSDictionary)
        {
            COSDictionary dic = (COSDictionary) underneathObject;
            return COSName.FONT.equals(dic.getCOSName(COSName.TYPE)) && !isCIDFont(dic);
        }
        return false;
    }

    private boolean isCIDFont(COSDictionary dic)
    {
        return COSName.CID_FONT_TYPE0.equals(dic.getCOSName(COSName.SUBTYPE)) ||
               COSName.CID_FONT_TYPE2.equals(dic.getCOSName(COSName.SUBTYPE));
    }

    /**
     * Show a Panel describing color spaces in more detail and interactive way.
     * @param csNode the special color space containing node.
     */
    private void showColorPane(Object csNode) throws IOException
    {
        COSBase underneathObject = getUnderneathObject(csNode);

        if (underneathObject instanceof COSArray && ((COSArray) underneathObject).size() > 0)
        {
            COSArray array = (COSArray) underneathObject;
            COSBase arrayEntry = array.get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName csName = (COSName) arrayEntry;
                if (csName.equals(COSName.SEPARATION))
                {
                    replaceRightComponent(new CSSeparation(array).getPanel());
                }
                else if (csName.equals(COSName.DEVICEN))
                {
                    replaceRightComponent(new CSDeviceN(array).getPanel());
                }
                else if (csName.equals(COSName.INDEXED))
                {
                    replaceRightComponent(new CSIndexed(array).getPanel());
                }
                else if (OTHERCOLORSPACES.contains(csName))
                {
                    replaceRightComponent(new CSArrayBased(array).getPanel());
                }
            }
        }
    }

    private void showPage(Object selectedNode)
    {
        COSBase underneathObject = getUnderneathObject(selectedNode);
        if (underneathObject instanceof COSDictionary)
        {
            COSDictionary page = (COSDictionary) underneathObject;
            COSBase typeItem = page.getItem(COSName.TYPE);
            if (COSName.PAGE.equals(typeItem))
            {
                PagePane pagePane = new PagePane(document, page, statusBar.getStatusLabel());
                replaceRightComponent(new JScrollPane(pagePane.getPanel()));
            }
        }
    }

    private void showFlagPane(Object parentNode, Object selectedNode)
    {
        COSBase underneathParentObject = getUnderneathObject(parentNode);
        if (underneathParentObject instanceof COSDictionary)
        {
            COSName selectedNodeName = ((MapEntry) selectedNode).getKey();
            FlagBitsPane flagBitsPane = new FlagBitsPane(document,
                    (COSDictionary) underneathParentObject,
                    (COSName) selectedNodeName);
            replaceRightComponent(flagBitsPane.getPane());
        }
    }

    private void showStream(COSStream stream, TreePath path) throws IOException
    {
        boolean isContentStream = false;
        boolean isThumb = false;

        COSName key = getNodeKey(path.getLastPathComponent());
        COSName parentKey = getNodeKey(path.getParentPath().getLastPathComponent());
        COSDictionary resourcesDic = null;

        if (COSName.CONTENTS.equals(key))
        {
            Object pageObj = path.getParentPath().getLastPathComponent();
            COSDictionary page = (COSDictionary) getUnderneathObject(pageObj);
            if (page != null)
            {
                resourcesDic = page.getCOSDictionary(COSName.RESOURCES);
            }
            isContentStream = true;
        }
        else if (COSName.CONTENTS.equals(parentKey) || COSName.CHAR_PROCS.equals(parentKey))
        {
            Object pageObj = path.getParentPath().getParentPath().getLastPathComponent();
            COSDictionary page = (COSDictionary) getUnderneathObject(pageObj);
            if (page != null)
            {
                resourcesDic = page.getCOSDictionary(COSName.RESOURCES);
            }
            isContentStream = true;
        }
        else if (COSName.FORM.equals(stream.getCOSName(COSName.SUBTYPE)) ||
                COSName.PATTERN.equals(stream.getCOSName(COSName.TYPE)) ||
                stream.getInt(COSName.PATTERN_TYPE) == 1)
        {
            if (stream.containsKey(COSName.RESOURCES))
            {
                resourcesDic = stream.getCOSDictionary(COSName.RESOURCES);
            }
            isContentStream = true;
        }
        else if (COSName.THUMB.equals(key))
        {
            isThumb = true;
        }
        else if (COSName.IMAGE.equals((stream).getCOSName(COSName.SUBTYPE)))
        {
            // not to be used for /Thumb, even if it contains /Subtype /Image
            Object resourcesObj = path.getParentPath().getParentPath().getLastPathComponent();
            // resources may be unreachable if the selected node is on the first level of a cross reference table
            if (!(resourcesObj instanceof XrefEntries))
            {
                resourcesDic = (COSDictionary) getUnderneathObject(resourcesObj);
            }
        }
        StreamPane streamPane = new StreamPane(stream, isContentStream, isThumb, resourcesDic);
        replaceRightComponent(streamPane.getPanel());
    }

    private void showFont(Object selectedNode, TreePath path)
    {
        JPanel pane = null;
        COSName fontName = getNodeKey(selectedNode);
        // may be null if the selected node is on the first level of a cross reference table
        if (fontName != null)
        {
            COSDictionary resourceDic = (COSDictionary) getUnderneathObject(
                    path.getParentPath().getParentPath().getLastPathComponent());

            FontEncodingPaneController fontEncodingPaneController = new FontEncodingPaneController(
                    fontName, resourceDic);
            pane = fontEncodingPaneController.getPane();
        }
        if (pane == null)
        {
            // unsupported font type
            replaceRightComponent(jScrollPaneRight);
            return;
        }
        replaceRightComponent(pane);
    }

    // replace the right component while keeping divider position
    private void replaceRightComponent(Component pane)
    {
        int div = jSplitPane.getDividerLocation();
        jSplitPane.setRightComponent(pane);
        jSplitPane.setDividerLocation(div);
    }

    private void showString(Object selectedNode)
    {
        COSString string = (COSString)getUnderneathObject(selectedNode);
        replaceRightComponent(new StringPane(string).getPane());
    }

    private COSName getNodeKey(Object selectedNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            return ((MapEntry) selectedNode).getKey();
        }
        return null;
    }

    private COSBase getUnderneathObject(Object selectedNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            return ((MapEntry) selectedNode).getValue();
        }
        else if (selectedNode instanceof ArrayEntry)
        {
            return ((ArrayEntry) selectedNode).getValue();
        }
        else if (selectedNode instanceof PageEntry)
        {
            return ((PageEntry) selectedNode).getDict();
        }
        else if (selectedNode instanceof XrefEntry)
        {
            return ((XrefEntry) selectedNode).getCOSObject();
        }
        if (selectedNode instanceof COSObject)
        {
            return ((COSObject) selectedNode).getObject();
        }
        return null;
    }

    private String convertToString( Object selectedNode )
    {
        if(selectedNode instanceof COSBoolean)
        {
            return Boolean.toString(((COSBoolean) selectedNode).getValue());
        }
        if (selectedNode instanceof COSFloat)
        {
            return Float.toString(((COSFloat) selectedNode).floatValue());
        }
        if (selectedNode instanceof COSNull)
        {
            return "null";
        }
        if (selectedNode instanceof COSInteger)
        {
            return Integer.toString(((COSInteger) selectedNode).intValue());
        }
        if (selectedNode instanceof COSName)
        {
            return ((COSName) selectedNode).getName();
        }
        if (selectedNode instanceof COSString)
        {
            String text = ((COSString) selectedNode).getString();
            // display unprintable strings as hex
            for (char c : text.toCharArray())
            {
                if (Character.isISOControl(c))
                {
                    text = "<" + ((COSString) selectedNode).toHexString() + ">";
                    break;
                }
            }
            return text;
        }
        if (selectedNode instanceof COSStream)
        {
            try
            {
                COSStream stream = (COSStream) selectedNode;
                try (InputStream in = stream.createInputStream())
                {
                    return new String(IOUtils.toByteArray(in));
                }
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        if (selectedNode instanceof COSDictionary)
        {
            // just a placeholder, the values are shown within the tree on the left hand side
            return "COSDictionary";
        }
        if (selectedNode instanceof COSArray)
        {
            // just a placeholder, the values are shown within the tree on the left hand side
            return "COSArray";
        }
        if (selectedNode instanceof MapEntry)
        {
            return convertToString(((MapEntry) selectedNode).getValue());
        }
        if (selectedNode instanceof ArrayEntry)
        {
            return convertToString(((ArrayEntry) selectedNode).getValue());
        }
        if (selectedNode instanceof XrefEntry)
        {
            return selectedNode.toString();
        }
        return null;
    }
    
    private void exitMenuItemActionPerformed(ActionEvent ignored)
    {
        if( document != null )
        {
            try
            {
                document.close();
                if (!currentFilePath.startsWith("http"))
                {
                    recentFiles.addFile(currentFilePath);
                }
                recentFiles.close();
            }
            catch( IOException e )
            {
                // no dialogbox, don't interfere with exit wish
                e.printStackTrace();
            }
        }
        windowPrefs.setExtendedState(getExtendedState());
        this.setExtendedState(Frame.NORMAL);
        windowPrefs.setBounds(getBounds());
        windowPrefs.setDividerLocation(jSplitPane.getDividerLocation());
        performApplicationExit();
    }

    /**
     * Exit the application after the window is closed. This is protected to
     * let subclasses override the behavior.
     */
    @SuppressWarnings("WeakerAccess")
    protected void performApplicationExit()
    {
        System.exit(0);
    }

    private void printMenuItemActionPerformed(ActionEvent evt)
    {
        if (document == null)
        {
            return;
        }
        AccessPermission ap = document.getCurrentAccessPermission();
        if (!ap.canPrint())
        {
            JOptionPane.showMessageDialog(this, "You do not have permission to print");
            return;
        }

        try
        {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPageable(new PDFPageable(document, Orientation.AUTO, false, PrintDpiMenu.getDpiSelection()));
            PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
            PDViewerPreferences vp = document.getDocumentCatalog().getViewerPreferences();
            if (vp != null && vp.getDuplex() != null)
            {
                String dp = vp.getDuplex();
                if (PDViewerPreferences.DUPLEX.DuplexFlipLongEdge.toString().equals(dp))
                {
                    pras.add(Sides.TWO_SIDED_LONG_EDGE);
                }
                else if (PDViewerPreferences.DUPLEX.DuplexFlipShortEdge.toString().equals(dp))
                {
                    pras.add(Sides.TWO_SIDED_SHORT_EDGE);
                }
                else if (PDViewerPreferences.DUPLEX.Simplex.toString().equals(dp))
                {
                    pras.add(Sides.ONE_SIDED);
                }
            }
            if (job.printDialog(pras))
            {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                try
                {
                    long t0 = System.nanoTime();
                    job.print(pras);
                    long t1 = System.nanoTime();
                    long ms = TimeUnit.MILLISECONDS.convert(t1 - t0, TimeUnit.NANOSECONDS);
                    LOG.info("Printed in " + ms + " ms");
                }
                finally
                {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        catch (PrinterException e)
        {
            new ErrorDialog(e).setVisible(true);
        }
    }

    private void readPDFFile(String filePath, String password) throws IOException, URISyntaxException
    {
        File file = new File(filePath);
        readPDFFile(file, password);
    }
    
    private void readPDFFile(final File file, String password) throws IOException, URISyntaxException
    {
        if( document != null )
        {
            document.close();
            if (!currentFilePath.startsWith("http"))
            {
                recentFiles.addFile(currentFilePath);
            }
        }
        currentFilePath = file.getPath();
        recentFiles.removeFile(file.getPath());
        LogDialog.instance().clear();
        TextDialog.instance().setVisible(false);
        
        DocumentOpener documentOpener = new DocumentOpener(password)
        {
            @Override
            PDDocument open() throws IOException
            {
                long t0 = System.nanoTime();
                PDDocument doc = Loader.loadPDF(file, password);
                long t1 = System.nanoTime();
                long ms = TimeUnit.MILLISECONDS.convert(t1 - t0, TimeUnit.NANOSECONDS);
                LOG.info("Parsed in " + ms + " ms");
                return doc;
            }
        };
        document = documentOpener.parse();
        printMenuItem.setEnabled(true);
        printDpiMenu.setEnabled(true);
        reopenMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);
        
        initTree();
        
        if (IS_MAC_OS)
        {
            setTitle(file.getName());
            getRootPane().putClientProperty("Window.documentFile", file);
        }
        else
        {
            setTitle("PDF Debugger - " + file.getAbsolutePath());
        }
        addRecentFileItems();
    }

    private void readPDFurl(final String urlString, String password)
            throws IOException, URISyntaxException
    {
        if (document != null)
        {
            document.close();
            if (!currentFilePath.startsWith("http"))
            {
                recentFiles.addFile(currentFilePath);
            }
        }
        currentFilePath = urlString;
        LogDialog.instance().clear();
        TextDialog.instance().setVisible(false);

        DocumentOpener documentOpener = new DocumentOpener(password)
        {
            @Override
            PDDocument open() throws IOException, URISyntaxException
            {
                long t0 = System.nanoTime();
                PDDocument doc = Loader.loadPDF(RandomAccessReadBuffer
                        .createBufferFromStream(new URI(urlString).toURL().openStream()), password);
                long t1 = System.nanoTime();
                long ms = TimeUnit.MILLISECONDS.convert(t1 - t0, TimeUnit.NANOSECONDS);
                LOG.info("Parsed in " + ms + " ms");
                return doc;
            }
        };
        document = documentOpener.parse();
        printMenuItem.setEnabled(true);
        printDpiMenu.setEnabled(true);
        reopenMenuItem.setEnabled(true);
        saveAsMenuItem.setEnabled(true);

        initTree();

        if (IS_MAC_OS)
        {
            setTitle(urlString);
        }
        else
        {
            setTitle("PDF Debugger - " + urlString);
        }
        addRecentFileItems();
    }

    public void initTree()
    {
        TreeStatus treeStatus = new TreeStatus(document.getDocument().getTrailer());
        statusPane.updateTreeStatus(treeStatus);
        
        String treeViewMode = TreeViewMenu.getInstance().getTreeViewSelection();
        if (TreeViewMenu.VIEW_PAGES.equals(treeViewMode))
        {
            File file = new File(currentFilePath);
            DocumentEntry documentEntry = new DocumentEntry(document, file.getName());
            ZoomMenu.getInstance().resetZoom();
            RotationMenu.getInstance().setRotationSelection(RotationMenu.ROTATE_0_DEGREES);
            ImageTypeMenu.getInstance().setImageTypeSelection(ImageTypeMenu.IMAGETYPE_RGB);
            RenderDestinationMenu.getInstance()
                    .setRenderDestinationSelection(RenderDestinationMenu.RENDER_DESTINATION_EXPORT);
            tree.setModel(new PDFTreeModel(documentEntry));
            // Root/Pages/Kids/[0] is not always the first page, so use the first row instead:
            tree.setSelectionPath(tree.getPathForRow(1));
        }
        else if (TreeViewMenu.VIEW_STRUCTURE.equals(treeViewMode))
        {
            tree.setModel(new PDFTreeModel(document));
            tree.setSelectionPath(treeStatus.getPathForString("Root"));
            tree.setSelectionPath(tree.getPathForRow(1));
        }
        else if (TreeViewMenu.VIEW_CROSS_REF_TABLE.equals(treeViewMode))
        {
            tree.setModel(new PDFTreeModel(new XrefEntries(document)));
            tree.setSelectionPath(treeStatus.getPathForString("CRT"));
            tree.setSelectionPath(tree.getPathForRow(1));
        }
    }

    /**
     * Internal class to avoid double code in password entry loop.
     */
    abstract static class DocumentOpener
    {
        String password;

        DocumentOpener(String password)
        {
            this.password = password;
        }

        /**
         * Override to load the actual input type (File, URL, stream), don't call it directly!
         * 
         * @return the PDDocument instance
         * @throws IOException Cannot read document
         * @throws URISyntaxException
         */
        abstract PDDocument open() throws IOException, URISyntaxException;

        /**
         * Call this!
         * 
         * @return the PDDocument instance
         * @throws IOException Cannot read document
         */
        final PDDocument parse() throws IOException, URISyntaxException 
        {
            while (true)
            {
                try
                {
                    return open();
                }
                catch (InvalidPasswordException ipe)
                {
                    // https://stackoverflow.com/questions/8881213/joptionpane-to-get-password
                    JPanel panel = new JPanel();
                    JLabel label = new JLabel("Password:");
                    JPasswordField pass = new JPasswordField(10);
                    panel.add(label);
                    panel.add(pass);
                    String[] options = new String[]
                    {
                        "OK", "Cancel"
                    };
                    int option = JOptionPane.showOptionDialog(null, panel, "Enter password",
                            JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE,
                            null, options, "");
                    if (option == 0)
                    {
                        password = new String(pass.getPassword());
                        continue;
                    }
                    throw ipe;
                }
            }
        }
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
                    readPDFFile(filePath, "");
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        if (!recentFiles.isEmpty())
        {
            recentFilesMenu.removeAll();
            List<String> files = recentFiles.getFiles();
            for (int i = files.size() - 1; i >= 0; i--)
            {
                String path = files.get(i);
                String name = new File(path).getName();
                JMenuItem recentFileMenuItem = new JMenuItem(name);
                recentFileMenuItem.putClientProperty("path", path);
                recentFileMenuItem.addActionListener(recentMenuAction);
                recentFilesMenu.add(recentFileMenuItem);
            }
            recentFilesMenu.setEnabled(true);
        }
    }

    /**
     * Convenience method to get the page label if available.
     * 
     * @param document the current document
     * @param pageIndex 0-based page number.
     * @return a page label or null if not available.
     */
    public static String getPageLabel(PDDocument document, int pageIndex)
    {
        PDPageLabels pageLabels;
        try
        {
            pageLabels = document.getDocumentCatalog().getPageLabels();
        }
        catch (IOException ex)
        {
            return ex.getMessage();
        }
        if (pageLabels != null)
        {
            String[] labels = pageLabels.getLabelsByPageIndices();
            if (labels[pageIndex] != null)
            {
                return labels[pageIndex];
            }
        }
        return null;
    }

    private void textDialog(String title, URL resource)
    {
        try
        {
            JDialog dialog = new JDialog(this, title, true);
            JEditorPane editor = new JEditorPane(resource);
            editor.setContentType("text/html");
            editor.setEditable(false);
            editor.setBackground(Color.WHITE);
            editor.setPreferredSize(new Dimension(400, 250));

            // put it in the middle of the parent, but not outside of the screen
            // GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
            // doesn't work give the numbers we need
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            double screenWidth = screenSize.getWidth();
            double screenHeight = screenSize.getHeight();
            Rectangle parentBounds = this.getBounds();
            editor.addHyperlinkListener(this);
            dialog.add(editor);
            dialog.pack();

            int x = (int) (parentBounds.getX() + (parentBounds.getWidth() - editor.getWidth()) / 2);
            int y = (int) (parentBounds.getY() + (parentBounds.getHeight() - editor.getHeight()) / 2);
            x = (int) Math.min(x, screenWidth * 3 / 4);
            y = (int) Math.min(y, screenHeight * 3 / 4);
            x = Math.max(0, x);
            y = Math.max(0, y);
            dialog.setLocation(x, y);

            dialog.setVisible(true);
        }
        catch (IOException ex)
        {
            new ErrorDialog(ex).setVisible(true);
        }
    }
    
    @Override
    public void hyperlinkUpdate(HyperlinkEvent he)
    {
        if (he.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            try
            {
                URL url = he.getURL();
                try (InputStream stream = url.openStream())
                {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.copy(stream,baos);
                    JEditorPane editor
                            = new JEditorPane("text/plain", baos.toString(StandardCharsets.UTF_8.name()));
                    editor.setEditable(false);
                    editor.setBackground(Color.WHITE);
                    editor.setCaretPosition(0);
                    editor.setPreferredSize(new Dimension(600, 400));

                    String name = url.toString();
                    name = name.substring(name.lastIndexOf('/') + 1);

                    JDialog dialog = new JDialog(this, "Apache PDFBox: " + name, true);
                    dialog.add(new JScrollPane(editor));
                    dialog.pack();
                    dialog.setVisible(true);
                }
            }
            catch (IOException ex)
            {
                new ErrorDialog(ex).setVisible(true);
            }
        }
    }
}
