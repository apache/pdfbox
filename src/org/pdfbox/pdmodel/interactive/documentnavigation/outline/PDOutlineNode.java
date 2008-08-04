/**
 * Copyright (c) 2005, www.pdfbox.org
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
package org.pdfbox.pdmodel.interactive.documentnavigation.outline;

import org.pdfbox.cos.COSBase;
import org.pdfbox.cos.COSDictionary;

import org.pdfbox.pdmodel.common.COSObjectable;

/**
 * This represents an node in an outline in a pdf document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDOutlineNode implements COSObjectable
{   
    /**
     * The dictionary for this node.
     */
    protected COSDictionary node;
    
    /**
     * Default Constructor.
     */
    public PDOutlineNode()
    {
        node = new COSDictionary();
    }
    
    /**
     * Default Constructor.
     * 
     * @param dict The dictionary storage.
     */
    public PDOutlineNode( COSDictionary dict)
    {
        node = dict;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSBase getCOSObject()
    {
        return node;
    }
    
    /**
     * Convert this standard java object to a COS object.
     *
     * @return The cos object that matches this Java object.
     */
    public COSDictionary getCOSDictionary()
    {
        return node;
    }
    
    /**
     * Get the parent of this object.  This will either be a DocumentOutline or an OutlineItem.
     * 
     * @return The parent of this object, or null if this is the document outline and there
     * is no parent.
     */
    public PDOutlineNode getParent()
    {
        PDOutlineNode retval = null;
        COSDictionary parent = (COSDictionary)node.getDictionaryObject( "Parent", "P" );
        if( parent != null )
        {
            if( parent.getDictionaryObject( "Parent", "P" ) == null )
            {
                retval = new PDDocumentOutline( parent );
            }
            else
            {
                retval = new PDOutlineItem( parent );
            }
        }
        
        return retval;
    }
    
    /**
     * Set the parent of this object, this is maintained by these objects and should not
     * be called by any clients of PDFBox code.
     * 
     * @param parent The parent of this object.
     */
    protected void setParent( PDOutlineNode parent )
    {
        node.setItem( "Parent", parent );
    }
    
    /**
     * append a child node to this node.
     * 
     * @param outlineNode The node to add.
     */
    public void appendChild( PDOutlineItem outlineNode )
    {
        outlineNode.setParent( this );
        if( getFirstChild() == null )
        {
            int currentOpenCount = getOpenCount();
            setFirstChild( outlineNode );
            //1 for the the item we are adding;
            int numberOfOpenNodesWeAreAdding = 1;
            if( outlineNode.isNodeOpen() )
            {
                numberOfOpenNodesWeAreAdding += outlineNode.getOpenCount();
            }
            if( isNodeOpen() )
            {
                setOpenCount( currentOpenCount + numberOfOpenNodesWeAreAdding );
            }
            else
            {
                setOpenCount( currentOpenCount - numberOfOpenNodesWeAreAdding );
            }
            updateParentOpenCount( numberOfOpenNodesWeAreAdding );
        }
        else
        {
            PDOutlineItem previousLastChild = getLastChild();
            previousLastChild.insertSiblingAfter( outlineNode );
        }
        setLastChild( outlineNode );
    }
    
    /**
     * Return the first child or null if there is no child.
     * 
     * @return The first child.
     */
    public PDOutlineItem getFirstChild()
    {
        PDOutlineItem last = null;
        COSDictionary lastDic = (COSDictionary)node.getDictionaryObject( "First" );
        if( lastDic != null )
        {
            last = new PDOutlineItem( lastDic );
        }
        return last;
    }
    
    /**
     * Set the first child, this will be maintained by this class.
     * 
     * @param outlineNode The new first child.
     */
    protected void setFirstChild( PDOutlineNode outlineNode )
    {
        node.setItem( "First", outlineNode );
    }
    
    /**
     * Return the last child or null if there is no child.
     * 
     * @return The last child.
     */
    public PDOutlineItem getLastChild()
    {
        PDOutlineItem last = null;
        COSDictionary lastDic = (COSDictionary)node.getDictionaryObject( "Last" );
        if( lastDic != null )
        {
            last = new PDOutlineItem( lastDic );
        }
        return last;
    }
    
    /**
     * Set the last child, this will be maintained by this class.
     * 
     * @param outlineNode The new last child.
     */
    protected void setLastChild( PDOutlineNode outlineNode )
    {
        node.setItem( "Last", outlineNode );
    }
    
    /**
     * Get the number of open nodes.  Or a negative number if this node
     * is closed.  See PDF Reference for more details.  This value
     * is updated as you append children and siblings.
     * 
     * @return The Count attribute of the outline dictionary.
     */
    public int getOpenCount()
    {
        return node.getInt( "Count", 0 );
    }
    
    /**
     * Set the open count.  This number is automatically managed for you
     * when you add items to the outline.
     * 
     * @param openCount The new open cound.
     */
    protected void setOpenCount( int openCount )
    {
        node.setInt( "Count", openCount );
    }
    
    /**
     * This will set this node to be open when it is shown in the viewer.  By default, when 
     * a new node is created it will be closed.
     * This will do nothing if the node is already open.
     */
    public void openNode()
    {
        //if the node is already open then do nothing.
        if( !isNodeOpen() )
        {
            int openChildrenCount = 0;
            PDOutlineItem currentChild = getFirstChild();
            while( currentChild != null )
            {
                //first increase by one for the current child
                openChildrenCount++;
                //then increase by the number of open nodes the child has
                if( currentChild.isNodeOpen() )
                {
                    openChildrenCount += currentChild.getOpenCount();
                }
                currentChild = currentChild.getNextSibling();
            }
            setOpenCount( openChildrenCount );
            updateParentOpenCount( openChildrenCount );
        }
    }
    
    /**
     * Close this node.
     *
     */
    public void closeNode()
    {
        //if the node is already closed then do nothing.
        if( isNodeOpen() )
        {
            int openCount = getOpenCount();
            updateParentOpenCount( -openCount );
            setOpenCount( -openCount );
        }
    }
    
    /**
     * Node is open if the open count is greater than zero.
     * @return true if this node is open.
     */
    public boolean isNodeOpen()
    {
        return getOpenCount() > 0;
    }
    
    /**
     * The count parameter needs to be updated when you add or remove elements to 
     * the outline.  When you add an element at a lower level then you need to 
     * increase all of the parents.
     * 
     * @param amount The amount to update by.
     */
    protected void updateParentOpenCount( int amount )
    {
        PDOutlineNode parent = getParent();
        if( parent != null )
        {
            int currentCount = parent.getOpenCount();
            //if the currentCount is negative or it is absent then
            //we will treat it as negative.  The default is to be negative.
            boolean negative = currentCount < 0 || 
                parent.getCOSDictionary().getDictionaryObject( "Count" ) == null;
            currentCount = Math.abs( currentCount );
            currentCount += amount;
            if( negative )
            {
                currentCount = -currentCount;
            }
            parent.setOpenCount( currentCount );
            //recursively call parent to update count, but the parents count is only
            //updated if this is an open node
            if( !negative )
            {
                parent.updateParentOpenCount( amount );
            }
        }
    }
}
