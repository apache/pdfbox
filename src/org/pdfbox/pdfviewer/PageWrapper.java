/**
 * Copyright (c) 2003-2006, www.pdfbox.org
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of pdfbox; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://www.pdfbox.org
 *
 */
package org.pdfbox.pdfviewer;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.io.IOException;

import javax.swing.JPanel;

import org.pdfbox.PDFReader;
import org.pdfbox.pdmodel.PDPage;

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