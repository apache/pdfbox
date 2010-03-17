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
package org.apache.pdfbox.pdfviewer;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JPanel;

import org.apache.pdfbox.PDFReader;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 * A class to handle some prettyness around a single PDF page.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.5 $
 */
public class PageWrapper implements MouseMotionListener
{
    private JPanel pageWrapper = new JPanel();
    private PDFPagePanel pagePanel = null;
    private PDFReader reader = null;

    private static final int SPACE_AROUND_DOCUMENT = 20;

    /**
     * Constructor.
     *
     * @param aReader The reader application that holds this page.
     *
     * @throws IOException If there is an error creating the page drawing objects.
     */
    public PageWrapper( PDFReader aReader ) throws IOException
    {
        reader = aReader;
        pagePanel = new PDFPagePanel();
        pageWrapper.setLayout( null );
        pageWrapper.add( pagePanel );
        pagePanel.setLocation( SPACE_AROUND_DOCUMENT, SPACE_AROUND_DOCUMENT );
        pageWrapper.setBorder( javax.swing.border.LineBorder.createBlackLineBorder() );
        pagePanel.addMouseMotionListener( this );
    }

    /**
     * This will display the PDF page in this component.
     *
     * @param page The PDF page to display.
     */
    public void displayPage( PDPage page )
    {
        pagePanel.setPage( page );
        pagePanel.setPreferredSize( pagePanel.getSize() );
        Dimension d = pagePanel.getSize();
        d.width+=(SPACE_AROUND_DOCUMENT*2);
        d.height+=(SPACE_AROUND_DOCUMENT*2);

        pageWrapper.setPreferredSize( d );
        pageWrapper.validate();
    }

    /**
     * This will get the JPanel that can be displayed.
     *
     * @return The panel with the displayed PDF page.
     */
    public JPanel getPanel()
    {
        return pageWrapper;
    }

    /**
     * {@inheritDoc}
     */
    public void mouseDragged(MouseEvent e)
    {
        //do nothing when mouse moves.
    }

    /**
     * {@inheritDoc}
     */
    public void mouseMoved(MouseEvent e)
    {
        //reader.getBottomStatusPanel().getStatusLabel().setText( e.getX() + "," + (pagePanel.getHeight() - e.getY()) );
        reader.getBottomStatusPanel().getStatusLabel().setText( e.getX() + "," + e.getY() );
    }
}
