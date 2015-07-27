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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreePath;
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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.tools.gui.ArrayEntry;
import org.apache.pdfbox.tools.gui.DocumentEntry;
import org.apache.pdfbox.tools.gui.MapEntry;
import org.apache.pdfbox.tools.gui.OSXAdapter;
import org.apache.pdfbox.tools.gui.PDFTreeCellRenderer;
import org.apache.pdfbox.tools.gui.PDFTreeModel;
import org.apache.pdfbox.tools.gui.PageEntry;
import org.apache.pdfbox.tools.pdfdebugger.colorpane.CSArrayBased;
import org.apache.pdfbox.tools.pdfdebugger.colorpane.CSDeviceN;
import org.apache.pdfbox.tools.pdfdebugger.colorpane.CSIndexed;
import org.apache.pdfbox.tools.pdfdebugger.colorpane.CSSeparation;
import org.apache.pdfbox.tools.pdfdebugger.flagbitspane.FlagBitsPane;
import org.apache.pdfbox.tools.pdfdebugger.fontencodingpane.FontEncodingPaneController;
import org.apache.pdfbox.tools.pdfdebugger.pagepane.PagePane;
import org.apache.pdfbox.tools.pdfdebugger.streampane.StreamPane;
import org.apache.pdfbox.tools.pdfdebugger.treestatus.TreeStatus;
import org.apache.pdfbox.tools.pdfdebugger.treestatus.TreeStatusPane;
import org.apache.pdfbox.tools.pdfdebugger.ui.Tree;
import org.apache.pdfbox.tools.pdfdebugger.ui.ZoomMenu;
import org.apache.pdfbox.tools.util.FileOpenSaveDialog;
import org.apache.pdfbox.tools.util.RecentFiles;

/**
 *
 * @author wurtz
 * @author Ben Litchfield
 */
public class PDFDebugger extends javax.swing.JFrame
{
    private TreeStatusPane statusPane;
    private RecentFiles recentFiles;
    private boolean isPageMode;

    private PDDocument document;
    private String currentFilePath;

    private static final Set<COSName> SPECIALCOLORSPACES =
            new HashSet(Arrays.asList(COSName.INDEXED, COSName.SEPARATION, COSName.DEVICEN));

    private static final Set<COSName> OTHERCOLORSPACES =
            new HashSet(Arrays.asList(COSName.ICCBASED, COSName.PATTERN, COSName.CALGRAY, COSName.CALRGB, COSName.LAB));

    private static final String PASSWORD = "-password";

    private static final int SHORCUT_KEY_MASK =
            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    private static final String OS_NAME = System.getProperty("os.name").toLowerCase();
    private static final boolean IS_MAC_OS = OS_NAME.startsWith("mac os x");
    
    /**
     * Constructor.
     */
    public PDFDebugger()
    {
        initComponents();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents()
    {
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane1 = new JScrollPane();
        tree = new Tree(this);
        jScrollPane2 = new JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new JMenu();
        openMenuItem = new JMenuItem();
        saveMenuItem = new JMenuItem();
        saveAsMenuItem = new JMenuItem();
        recentFilesMenu = new JMenu();
        exitMenuItem = new JMenuItem();
        editMenu = new JMenu();
        cutMenuItem = new JMenuItem();
        copyMenuItem = new JMenuItem();
        pasteMenuItem = new JMenuItem();
        deleteMenuItem = new JMenuItem();
        viewMenu = new JMenu();
        viewModeItem = new JMenuItem();
        helpMenu = new JMenu();
        contentsMenuItem = new JMenuItem();
        aboutMenuItem = new JMenuItem();

        tree.setCellRenderer( new PDFTreeCellRenderer() );
        tree.setModel( null );

        setTitle("PDFBox Debugger");

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
                exitForm(evt);
            }
        });
        
        jScrollPane1.setBorder(new BevelBorder(BevelBorder.RAISED));
        jScrollPane1.setPreferredSize(new Dimension(300, 500));
        tree.addTreeSelectionListener(new TreeSelectionListener()
        {
            @Override
            public void valueChanged(TreeSelectionEvent evt)
            {
                jTree1ValueChanged(evt);
            }
        });

        jScrollPane1.setViewportView(tree);

        jSplitPane1.setRightComponent(jScrollPane2);
        jSplitPane1.setDividerSize(3);
        
        jScrollPane2.setPreferredSize(new Dimension(300, 500));
        jScrollPane2.setViewportView(jTextPane1);

        jSplitPane1.setLeftComponent(jScrollPane1);

        JScrollPane documentScroller = new JScrollPane();
        documentScroller.setViewportView( documentPanel );

        statusPane = new TreeStatusPane(tree);
        statusPane.getPanel().setBorder(new BevelBorder(BevelBorder.RAISED));
        statusPane.getPanel().setPreferredSize(new Dimension(300, 25));
        getContentPane().add(statusPane.getPanel(), BorderLayout.PAGE_START);

        getContentPane().add( jSplitPane1, BorderLayout.CENTER );

        fileMenu.setText("File");
        openMenuItem.setText("Open...");
        openMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORCUT_KEY_MASK));
        openMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        saveMenuItem.setText("Save");

        saveAsMenuItem.setText("Save As ...");

        try
        {
            recentFiles = new RecentFiles(this.getClass(), 5);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        recentFilesMenu.setText("Open Recent");
        recentFilesMenu.setEnabled(false);
        addRecentFileItems();
        fileMenu.add(recentFilesMenu);

        exitMenuItem.setText("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke("alt F4"));
        exitMenuItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent evt)
            {
                exitMenuItemActionPerformed(evt);
            }
        });

        if (!IS_MAC_OS)
        {
            fileMenu.add(exitMenuItem);
        }

        menuBar.add(fileMenu);

        editMenu.setText("Edit");
        cutMenuItem.setText("Cut");
        editMenu.add(cutMenuItem);

        copyMenuItem.setText("Copy");
        editMenu.add(copyMenuItem);

        pasteMenuItem.setText("Paste");
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setText("Delete");
        editMenu.add(deleteMenuItem);

        viewMenu.setText("View");

        viewModeItem.setText("Show Pages");
        viewModeItem.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent actionEvent)
            {
                if (isPageMode)
                {
                    viewModeItem.setText("Show Pages");
                    isPageMode = false;
                }
                else
                {
                    viewModeItem.setText("Show Internal Structure");
                    isPageMode = true;
                }
                initTree();
            }
        });

        viewMenu.add(viewModeItem);

        menuBar.add(viewMenu);

        helpMenu.setText("Help");
        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);
        
        ZoomMenu zoomMenu = ZoomMenu.getInstance();
        zoomMenu.setEnableMenu(false);
        viewMenu.add(zoomMenu.getMenu());

        setJMenuBar(menuBar);

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-700)/2, (screenSize.height-600)/2, 700, 600);

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
                    return true;
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
                catch (UnsupportedFlavorException e)
                {
                    throw new RuntimeException(e);
                }
            }
        });

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
    }//GEN-END:initComponents

    /**
     * This method is called via reflection on Mac OS X.
     */
    private void osxOpenFiles(String filename)
    {
        try
        {
            readPDFFile(filename, "");
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method is called via reflection on Mac OS X.
     */
    private void osxQuit()
    {
        exitMenuItemActionPerformed(null);
    }

    private void openMenuItemActionPerformed(ActionEvent evt)
    {
        try
        {
            if (IS_MAC_OS)
            {
                FileDialog openDialog = new FileDialog(this, "Open");
                openDialog.setFilenameFilter(new FilenameFilter()
                {
                    @Override
                    public boolean accept(File file, String s)
                    {
                        return file.getName().toLowerCase().endsWith(".pdf");
                    }
                });
                openDialog.setVisible(true);
                if (openDialog.getFile() != null)
                {
                    readPDFFile(openDialog.getFile(), "");
                }
            }
            else
            {
                String[] extensions = new String[] {"pdf", "PDF"};
                FileFilter pdfFilter = new ExtensionFileFilter(extensions, "PDF Files (*.pdf)");
                FileOpenSaveDialog openDialog = new FileOpenSaveDialog(this, pdfFilter);

                File file = openDialog.openFile();
                if (file != null)
                {
                    readPDFFile(file, "");
                }
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void jTree1ValueChanged(TreeSelectionEvent evt)
    {
        TreePath path = tree.getSelectionPath();
        if (path != null)
        {
            try
            {
                Object selectedNode = path.getLastPathComponent();
                
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
                    showStream((COSStream)getUnderneathObject(selectedNode), path);
                    return;
                }
                if (isFont(selectedNode))
                {
                    showFont(selectedNode, path);
                    return;
                }
                if (!jSplitPane1.getRightComponent().equals(jScrollPane2))
                {
                    jSplitPane1.setRightComponent(jScrollPane2);
                }
                jTextPane1.setText(convertToString(selectedNode));
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }//GEN-LAST:event_jTree1ValueChanged

    private boolean isSpecialColorSpace(Object selectedNode)
    {
        selectedNode = getUnderneathObject(selectedNode);

        if (selectedNode instanceof COSArray && ((COSArray) selectedNode).size() > 0)
        {
            COSBase arrayEntry = ((COSArray)selectedNode).get(0);
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
        selectedNode = getUnderneathObject(selectedNode);

        if (selectedNode instanceof COSArray && ((COSArray) selectedNode).size() > 0)
        {
            COSBase arrayEntry = ((COSArray)selectedNode).get(0);
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
        selectedNode = getUnderneathObject(selectedNode);

        if (selectedNode instanceof COSDictionary)
        {
            COSDictionary dict = (COSDictionary) selectedNode;
            COSBase typeItem = dict.getItem(COSName.TYPE);
            if (COSName.PAGE.equals(typeItem))
            {
                return true;
            }
        }
        else if (selectedNode instanceof PageEntry)
        {
            return true;
        }
        return false;
    }

    private boolean isFlagNode(Object selectedNode, Object parentNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            Object key = ((MapEntry) selectedNode).getKey();
            return (COSName.FLAGS.equals(key) && isFontDescriptor(parentNode))
                    || (COSName.F.equals(key) && isAnnot(parentNode)) || COSName.FF.equals(key)
                    || COSName.PANOSE.equals(key);
        }
        return false;
    }

    private boolean isFontDescriptor(Object obj)
    {
        Object underneathObject = getUnderneathObject(obj);
        return underneathObject instanceof COSDictionary &&
                ((COSDictionary) underneathObject).containsKey(COSName.TYPE) &&
                ((COSDictionary) underneathObject).getCOSName(COSName.TYPE).equals(COSName.FONT_DESC);
    }

    private boolean isAnnot(Object obj)
    {
        Object underneathObject = getUnderneathObject(obj);
        return underneathObject instanceof COSDictionary &&
                ((COSDictionary) underneathObject).containsKey(COSName.TYPE) &&
                ((COSDictionary) underneathObject).getCOSName(COSName.TYPE).equals(COSName.ANNOT);
    }

    private boolean isStream(Object selectedNode)
    {
        return getUnderneathObject(selectedNode) instanceof COSStream;
    }

    private boolean isFont(Object selectedNode)
    {
        selectedNode = getUnderneathObject(selectedNode);
        if (selectedNode instanceof COSDictionary)
        {
            COSDictionary dic = (COSDictionary)selectedNode;
            return  dic.containsKey(COSName.TYPE) &&
                    dic.getCOSName(COSName.TYPE).equals(COSName.FONT) &&
                    !isCIDFont(dic);
        }
        return false;
    }

    private boolean isCIDFont(COSDictionary dic)
    {
        return dic.containsKey(COSName.SUBTYPE) &&
                (dic.getCOSName(COSName.SUBTYPE).equals(COSName.CID_FONT_TYPE0)
                || dic.getCOSName(COSName.SUBTYPE).equals(COSName.CID_FONT_TYPE2));
    }

    /**
     * Show a Panel describing color spaces in more detail and interactive way.
     * @param csNode the special color space containing node.
     */
    private void showColorPane(Object csNode)
    {
        csNode = getUnderneathObject(csNode);

        if (csNode instanceof COSArray && ((COSArray) csNode).size() > 0)
        {
            COSArray array = (COSArray)csNode;
            COSBase arrayEntry = array.get(0);
            if (arrayEntry instanceof COSName)
            {
                COSName csName = (COSName) arrayEntry;
                if (csName.equals(COSName.SEPARATION))
                {
                    jSplitPane1.setRightComponent(new CSSeparation(array).getPanel());
                }
                else if (csName.equals(COSName.DEVICEN))
                {
                    jSplitPane1.setRightComponent(new CSDeviceN(array).getPanel());
                }
                else if (csName.equals(COSName.INDEXED))
                {
                    jSplitPane1.setRightComponent(new CSIndexed(array).getPanel());
                }
                else if (OTHERCOLORSPACES.contains(csName))
                {
                    jSplitPane1.setRightComponent(new CSArrayBased(array).getPanel());
                }
            }
        }
    }

    private void showPage(Object selectedNode)
    {
        selectedNode = getUnderneathObject(selectedNode);

        COSDictionary page;
        if (selectedNode instanceof COSDictionary)
        {
            page = (COSDictionary) selectedNode;
        }
        else
        {
            page = ((PageEntry) selectedNode).getDict();
        }

        COSBase typeItem = page.getItem(COSName.TYPE);
        if (COSName.PAGE.equals(typeItem))
        {
            PagePane pagePane = new PagePane(document, page);
            jSplitPane1.setRightComponent(new JScrollPane(pagePane.getPanel()));
        }
    }

    private void showFlagPane(Object parentNode, Object selectedNode)
    {
        parentNode = getUnderneathObject(parentNode);
        if (parentNode instanceof COSDictionary)
        {
            selectedNode = ((MapEntry)selectedNode).getKey();
            selectedNode = getUnderneathObject(selectedNode);
            FlagBitsPane flagBitsPane = new FlagBitsPane((COSDictionary) parentNode, (COSName) selectedNode);
            jSplitPane1.setRightComponent(flagBitsPane.getPane());
        }
    }

    private void showStream(COSStream stream, TreePath path)
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
            resourcesDic = (COSDictionary) page.getDictionaryObject(COSName.RESOURCES);
            isContentStream = true;
        }
        else if (COSName.CONTENTS.equals(parentKey) || COSName.CHAR_PROCS.equals(parentKey))
        {
            Object pageObj = path.getParentPath().getParentPath().getLastPathComponent();
            COSDictionary page = (COSDictionary) getUnderneathObject(pageObj);
            resourcesDic = (COSDictionary) page.getDictionaryObject(COSName.RESOURCES);
            isContentStream = true;
        }
        else if (COSName.FORM.equals(stream.getCOSName(COSName.SUBTYPE)) ||
                COSName.PATTERN.equals(stream.getCOSName(COSName.TYPE)))
        {
            if (stream.containsKey(COSName.RESOURCES))
            {
                resourcesDic = (COSDictionary) stream.getDictionaryObject(COSName.RESOURCES);
            }
            isContentStream = true;
        }
        else if (COSName.IMAGE.equals((stream).getCOSName(COSName.SUBTYPE)))
        {
            Object resourcesObj = path.getParentPath().getParentPath().getLastPathComponent();
            resourcesDic = (COSDictionary) getUnderneathObject(resourcesObj);
        }
        else if (COSName.THUMB.equals(key))
        {
            resourcesDic = null;
            isThumb = true;
        }
        StreamPane streamPane = new StreamPane(stream, isContentStream, isThumb, resourcesDic);
        jSplitPane1.setRightComponent(streamPane.getPanel());
    }

    private void showFont(Object selectedNode, TreePath path)
    {
        COSName fontName = getNodeKey(selectedNode);
        COSDictionary resourceDic = (COSDictionary) getUnderneathObject(path.getParentPath().getParentPath().getLastPathComponent());

        FontEncodingPaneController fontEncodingPaneController = new FontEncodingPaneController(fontName, resourceDic);
        jSplitPane1.setRightComponent(fontEncodingPaneController.getPane());
    }

    private COSName getNodeKey(Object selectedNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            return ((MapEntry) selectedNode).getKey();
        }
        return null;
    }

    private Object getUnderneathObject(Object selectedNode)
    {
        if (selectedNode instanceof MapEntry)
        {
            selectedNode = ((MapEntry) selectedNode).getValue();
        }
        else if (selectedNode instanceof ArrayEntry)
        {
            selectedNode = ((ArrayEntry) selectedNode).getValue();
        }
        else if (selectedNode instanceof PageEntry)
        {
            selectedNode = ((PageEntry) selectedNode).getDict();
        }

        if (selectedNode instanceof COSObject)
        {
            selectedNode = ((COSObject) selectedNode).getObject();
        }
        return selectedNode;
    }

    private String convertToString( Object selectedNode )
    {
        String data = null;
        if(selectedNode instanceof COSBoolean)
        {
            data = "" + ((COSBoolean)selectedNode).getValue();
        }
        else if( selectedNode instanceof COSFloat )
        {
            data = "" + ((COSFloat)selectedNode).floatValue();
        }
        else if( selectedNode instanceof COSNull )
        {
            data = "null";
        }
        else if( selectedNode instanceof COSInteger )
        {
            data = "" + ((COSInteger)selectedNode).intValue();
        }
        else if( selectedNode instanceof COSName )
        {
            data = "" + ((COSName)selectedNode).getName();
        }
        else if( selectedNode instanceof COSString )
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
            data = "" + text;
        }
        else if( selectedNode instanceof COSStream )
        {
            try
            {
                COSStream stream = (COSStream)selectedNode;
                InputStream ioStream = stream.getUnfilteredStream();
                ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int amountRead;
                while( (amountRead = ioStream.read( buffer, 0, buffer.length ) ) != -1 )
                {
                    byteArray.write( buffer, 0, amountRead );
                }
                data = byteArray.toString();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        else if( selectedNode instanceof MapEntry )
        {
            data = convertToString( ((MapEntry)selectedNode).getValue() );
        }
        else if( selectedNode instanceof ArrayEntry )
        {
            data = convertToString( ((ArrayEntry)selectedNode).getValue() );
        }
        return data;
    }

    private void exitMenuItemActionPerformed(ActionEvent evt)
    {
        if( document != null )
        {
            try
            {
                document.close();
                recentFiles.addFile(currentFilePath);
                recentFiles.close();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

    /**
     * Exit the Application.
     */
    private void exitForm(WindowEvent evt)
    {
        if( document != null )
        {
            try
            {
                document.close();
                recentFiles.addFile(currentFilePath);
                recentFiles.close();
            }
            catch( IOException e )
            {
                throw new RuntimeException(e);
            }
        }
        System.exit(0);
    }

    /**
     * Entry point.
     * 
     * @param args the command line arguments
     * @throws Exception If anything goes wrong.
     */
    public static void main(String[] args) throws Exception
    {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable)
            {
                StringBuilder sb = new StringBuilder();
                sb.append(throwable.toString());
                for (StackTraceElement element : throwable.getStackTrace())
                {
                    sb.append('\n');
                    sb.append(element);
                }
                JOptionPane.showMessageDialog(null, "Error: " + sb.toString(),"Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        
        final PDFDebugger viewer = new PDFDebugger();

        
        
        // open file, if any
        String filename = null;
        String password = "";
        
        for( int i = 0; i < args.length; i++ )
        {
            if( args[i].equals( PASSWORD ) )
            {
                i++;
                if( i >= args.length )
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
        
        if (filename != null)
        {
            File file = new File(filename);
            if (file.exists())
            {
                viewer.readPDFFile( filename, password );
            }
        }
        viewer.setVisible(true);
    }

    private void readPDFFile(String filePath, String password) throws IOException
    {
        File file = new File(filePath);
        readPDFFile(file, password);
    }
    
    private void readPDFFile(File file, String password) throws IOException
    {
        if( document != null )
        {
            document.close();
            recentFiles.addFile(currentFilePath);
        }
        currentFilePath = file.getPath();
        recentFiles.removeFile(file.getPath());
        parseDocument( file, password );
        
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
    
    private void initTree()
    {
        TreeStatus treeStatus = new TreeStatus(document.getDocument().getTrailer());
        statusPane.updateTreeStatus(treeStatus);
        
        if (isPageMode)
        {
            File file = new File(currentFilePath);
            DocumentEntry documentEntry = new DocumentEntry(document, file.getName());
            tree.setModel(new PDFTreeModel(documentEntry));
            tree.setSelectionPath(treeStatus.getPathForString("Root/Pages/Kids/[0]"));
        }
        else
        {
            tree.setModel(new PDFTreeModel(document));
            tree.setSelectionPath(treeStatus.getPathForString("Root"));
        }
    }
    
    /**
     * This will parse a document.
     *
     * @param file The file addressing the document.
     *
     * @throws IOException If there is an error parsing the document.
     */
    private void parseDocument( File file, String password )throws IOException
    {
        document = PDDocument.load(file, password);
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
     * This will print out a message telling how to use this utility.
     */
    private static void usage()
    {
        System.err.println(
                "usage: java -jar pdfbox-app-x.y.z.jar PDFDebugger [OPTIONS] <input-file>\n" +
                        "  -password <password>      Password to decrypt the document\n" +
                        "  <input-file>              The PDF document to be loaded\n"
        );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JMenuItem aboutMenuItem;
    private JMenuItem contentsMenuItem;
    private JMenuItem copyMenuItem;
    private JMenuItem cutMenuItem;
    private JMenuItem deleteMenuItem;
    private JMenu editMenu;
    private JMenuItem exitMenuItem;
    private JMenu fileMenu;
    private JMenu helpMenu;
    private JMenu recentFilesMenu;
    private JMenu viewMenu;
    private JMenuItem viewModeItem;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextPane jTextPane1;
    private Tree tree;
    private javax.swing.JMenuBar menuBar;
    private JMenuItem openMenuItem;
    private JMenuItem pasteMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem saveMenuItem;
    private final JPanel documentPanel = new JPanel();
    // End of variables declaration//GEN-END:variables

}
