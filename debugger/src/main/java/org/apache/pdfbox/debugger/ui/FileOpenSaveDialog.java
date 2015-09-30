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

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * @author Khyrul Bashar
 *
 * A Customized class that helps to open and save file. It uses JFileChooser to operate.
 */
public class FileOpenSaveDialog
{
    private final Component mainUI;

    private static final JFileChooser fileChooser = new JFileChooser() 
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
        fileChooser.setFileFilter(fileFilter);
    }

    /**
     * Saves data into a file after the user is prompted to choose the destination.
     *
     * @param bytes byte array to be saved in a file.
     * @return true if the file is saved successfully or false if failed.
     * @throws IOException if there is an error in creation of the file.
     */
    public boolean saveFile(byte[] bytes) throws IOException
    {
        int result = fileChooser.showSaveDialog(mainUI);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            File selectedFile = fileChooser.getSelectedFile();
            FileOutputStream outputStream = null;
            try
            {
                outputStream = new FileOutputStream(selectedFile);
                outputStream.write(bytes);
            }
            finally
            {
                if (outputStream != null)
                {
                    outputStream.close();
                }
            }
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
        int result = fileChooser.showOpenDialog(mainUI);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            return fileChooser.getSelectedFile();
        }
        return null;
    }
}
