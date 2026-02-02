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

package org.apache.pdfbox.debugger.ui;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * @author Khyrul Bashar
 *
 * A Customized class that helps to open and save file. It uses JFileChooser to operate.
 */
public class FileOpenSaveDialog
{
    private final Component mainUI;

    private static final JFileChooser FILE_CHOOSER = new JFileChooser()
    {
        @Override
        public void approveSelection()
        {
            File selectedFile = getSelectedFile();
            if (selectedFile.exists() && getDialogType() == JFileChooser.SAVE_DIALOG)
            {
                int result = JOptionPane.showConfirmDialog(this,
                    "Do you want to overwrite?",
                    "File already exists",
                    JOptionPane.YES_NO_OPTION);
                if (result != JOptionPane.YES_OPTION)
                {
                    cancelSelection();
                    return;
                }
            }
            super.approveSelection();
        }
    };

    /**
     * Constructor.
     * @param parentUI the main UI (JFrame) on top of which File open/save dialog should open.
     * @param fileFilter file Filter, null is allowed when no filter is applicable.
     */
    public FileOpenSaveDialog(Component parentUI, FileFilter fileFilter)
    {
        mainUI = parentUI;
        FILE_CHOOSER.resetChoosableFileFilters();
        FILE_CHOOSER.setFileFilter(fileFilter);
    }

    /**
     * Saves data into a file after the user is prompted to choose the destination.
     *
     * @param bytes byte array to be saved in a file.
     * @param extension file extension.
     * @return true if the file is saved successfully or false if failed.
     * @throws IOException if there is an error in creation of the file.
     */
    public boolean saveFile(byte[] bytes, String extension) throws IOException
    {
        int result = FILE_CHOOSER.showSaveDialog(mainUI);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            String filename = FILE_CHOOSER.getSelectedFile().getAbsolutePath();
            if (extension != null && !filename.endsWith(extension))
            {
                filename += "." + extension;
            }
            Files.write(Paths.get(filename), bytes);
            return true;
        }
        return false;
    }

    /**
     * Saves document into a .pdf file after the user is prompted to choose the destination.
     *
     * @param document document to be saved in a .pdf file.
     * @param extension file extension.
     * @return true if the file is saved successfully or false if failed.
     * @throws IOException if there is an error in creation of the file.
     */
    public boolean saveDocument(PDDocument document, String extension) throws IOException
    {
        int result = FILE_CHOOSER.showSaveDialog(mainUI);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            String filename = FILE_CHOOSER.getSelectedFile().getAbsolutePath();
            if (!filename.endsWith(extension))
            {
                filename += "." + extension;
            }
            document.setAllSecurityToBeRemoved(true);
            document.save(filename);
            return true;
        }
        return false;
    }

    /**
     * open a file prompting user to select the file.
     * @return the file opened.
     * @throws IOException if there is error in opening the file.
     */
    public File openFile() throws IOException
    {
        int result = FILE_CHOOSER.showOpenDialog(mainUI);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            return FILE_CHOOSER.getSelectedFile();
        }
        return null;
    }
}
