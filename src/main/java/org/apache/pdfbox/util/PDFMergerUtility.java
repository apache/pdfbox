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
package org.apache.pdfbox.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSInteger;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.cos.COSObject;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.COSArrayList;
import org.apache.pdfbox.pdmodel.common.COSObjectable;
import org.apache.pdfbox.pdmodel.common.PDStream;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDFieldFactory;

/**
 * This class will take a list of pdf documents and merge them, saving the result
 * in a new document.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.3 $
 */
public class PDFMergerUtility
{

    private List sources;
    private String destinationFileName;

    /**
     * Instantiate a new PDFMergerUtility.
     */
    public PDFMergerUtility()
    {
        sources = new ArrayList();
    }

    /**
     * Get the name of the destination file.
     * @return Returns the destination.
     */
    public String getDestinationFileName()
    {
        return destinationFileName;
    }

    /**
     * Set the name of the destination file.
     * @param destination
     *            The destination to set.
     */
    public void setDestinationFileName(String destination)
    {
        this.destinationFileName = destination;
    }

    /**
     * Add a source file to the list of files to merge.
     *
     * @param source Full path and file name of source document.
     */
    public void addSource(String source)
    {
        sources.add(new File(source));
    }

    /**
     * Add a source file to the list of files to mere.
     *
     * @param source File representing source document
     */
    public void addSource(File source)
    {
        sources.add(source);
    }

    /**
     * Merge the list of source documents, saving the result in the destination file.
     *
     * @throws IOException If there is an error saving the document.
     * @throws COSVisitorException If an error occurs while saving the destination file.
     */
    public void mergeDocuments() throws IOException, COSVisitorException
    {
        PDDocument destination = null;
        File sourceFile;
        PDDocument source;
        if (sources != null && sources.size() > 0)
        {
            try
            {
                Iterator sit = sources.iterator();
                sourceFile = (File) sit.next();
                destination = PDDocument.load(sourceFile);
                while (sit.hasNext())
                {
                    sourceFile = (File) sit.next();
                    source = PDDocument.load(sourceFile);
                    try
                    {
                        appendDocument(destination, source);
                    }
                    finally
                    {
                        if (source != null)
                        {
                            source.close();
                        }
                    }
                }
                destination.save(destinationFileName);
            }
            finally
            {
                if (destination != null)
                {
                    destination.close();
                }
            }
        }
    }


    /**
     * append all pages from source to destination.
     *
     * @param destination the document to receive the pages
     * @param source the document originating the new pages
     *
     * @throws IOException If there is an error accessing data from either document.
     */
    public void appendDocument(PDDocument destination, PDDocument source) throws IOException
    {
        if( destination.isEncrypted() )
        {
            throw new IOException( "Error: destination PDF is encrypted, can't append encrypted PDF documents." );
        }
        if( source.isEncrypted() )
        {
            throw new IOException( "Error: source PDF is encrypted, can't append encrypted PDF documents." );
        }
        PDDocumentInformation destInfo = destination.getDocumentInformation();
        PDDocumentInformation srcInfo = source.getDocumentInformation();
        destInfo.getDictionary().mergeInto( srcInfo.getDictionary() );

        PDDocumentCatalog destCatalog = destination.getDocumentCatalog();
        PDDocumentCatalog srcCatalog = source.getDocumentCatalog();

        if( destCatalog.getOpenAction() == null )
        {
            destCatalog.setOpenAction( srcCatalog.getOpenAction() );
        }

        PDAcroForm destAcroForm = destCatalog.getAcroForm();
        PDAcroForm srcAcroForm = srcCatalog.getAcroForm();
        if( destAcroForm == null )
        {
            cloneForNewDocument( destination, srcAcroForm );
            destCatalog.setAcroForm( srcAcroForm );
        }
        else
        {
            if( srcAcroForm != null )
            {
                mergeAcroForm(destination, destAcroForm, srcAcroForm);
            }
        }

        COSArray destThreads = (COSArray)destCatalog.getCOSDictionary().getDictionaryObject(
                COSName.getPDFName( "Threads" ));
        COSArray srcThreads = (COSArray)cloneForNewDocument(
                destination,
                destCatalog.getCOSDictionary().getDictionaryObject( COSName.getPDFName( "Threads" )));
        if( destThreads == null )
        {
            destCatalog.getCOSDictionary().setItem( COSName.getPDFName( "Threads" ), srcThreads );
        }
        else
        {
            destThreads.addAll( srcThreads );
        }

        COSName names = COSName.getPDFName( "Names" );
        PDDocumentNameDictionary destNames = destCatalog.getNames();
        PDDocumentNameDictionary srcNames = srcCatalog.getNames();
        if( srcNames != null )
        {
            if( destNames == null )
            {
                destCatalog.getCOSDictionary().setItem( names, cloneForNewDocument( destination, srcNames ) );
            }
            else
            {
                cloneMerge(destination, srcNames, destNames);
            }   
                
        }

        PDDocumentOutline destOutline = destCatalog.getDocumentOutline();
        PDDocumentOutline srcOutline = srcCatalog.getDocumentOutline();
        if( srcOutline != null )
        {
            if( destOutline == null )
            {
                PDDocumentOutline cloned =
                    new PDDocumentOutline( (COSDictionary)cloneForNewDocument( destination, srcOutline ) );
                destCatalog.setDocumentOutline( cloned );
            }
            else
            {
                PDOutlineItem first = srcOutline.getFirstChild();
                PDOutlineItem clonedFirst = new PDOutlineItem( (COSDictionary)cloneForNewDocument(
                        destination, first ));
                destOutline.appendChild( clonedFirst );
            }
        }

        String destPageMode = destCatalog.getPageMode();
        String srcPageMode = srcCatalog.getPageMode();
        if( destPageMode == null )
        {
            destCatalog.setPageMode( srcPageMode );
        }

        COSName pageLabels = COSName.getPDFName( "PageLabels" );
        COSDictionary destLabels = (COSDictionary)destCatalog.getCOSDictionary().getDictionaryObject( pageLabels );
        COSDictionary srcLabels = (COSDictionary)srcCatalog.getCOSDictionary().getDictionaryObject( pageLabels );
        if( srcLabels != null )
        {
            int destPageCount = destination.getNumberOfPages();
            COSArray destNums = null;
            if( destLabels == null )
            {
                destLabels = new COSDictionary();
                destNums = new COSArray();
                destLabels.setItem( COSName.getPDFName( "Nums" ), destNums );
                destCatalog.getCOSDictionary().setItem( pageLabels, destLabels );
            }
            else
            {
                destNums = (COSArray)destLabels.getDictionaryObject( COSName.getPDFName( "Nums" ) );
            }
            COSArray srcNums = (COSArray)srcLabels.getDictionaryObject( COSName.getPDFName( "Nums" ) );
            if (srcNums != null)
            {
                for( int i=0; i<srcNums.size(); i+=2 )
                {
                    COSNumber labelIndex = (COSNumber)srcNums.getObject( i );
                    long labelIndexValue = labelIndex.intValue();
                    destNums.add( new COSInteger( labelIndexValue + destPageCount ) );
                    destNums.add( cloneForNewDocument( destination, srcNums.getObject( i+1 ) ) );
                }
            }
        }

        COSName metadata = COSName.getPDFName( "Metadata" );
        COSStream destMetadata = (COSStream)destCatalog.getCOSDictionary().getDictionaryObject( metadata );
        COSStream srcMetadata = (COSStream)srcCatalog.getCOSDictionary().getDictionaryObject( metadata );
        if( destMetadata == null && srcMetadata != null )
        {
            PDStream newStream = new PDStream( destination, srcMetadata.getUnfilteredStream(), false );
            newStream.getStream().mergeInto( srcMetadata );
            newStream.addCompression();
            destCatalog.getCOSDictionary().setItem( metadata, newStream );
        }

        //finally append the pages
        List pages = source.getDocumentCatalog().getAllPages();
        Iterator pageIter = pages.iterator();
        while( pageIter.hasNext() )
        {
            PDPage page = (PDPage)pageIter.next();
            PDPage newPage =
                new PDPage( (COSDictionary)cloneForNewDocument( destination, page.getCOSDictionary() ) );
            newPage.setCropBox( page.findCropBox() );
            newPage.setMediaBox( page.findMediaBox() );
            newPage.setRotation( page.findRotation() );
            destination.addPage( newPage );
        }
    }
    Map clonedVersion = new HashMap();


  /**
   * 
   * @param destination
   * @param base
   * @return
   * @throws IOException
   */
    private COSBase cloneForNewDocument( PDDocument destination, Object base ) throws IOException
    {
        if( base == null )
        {
            return null;
        }
        COSBase retval = (COSBase)clonedVersion.get( base );
        if( retval != null )
        {
            //we are done, it has already been converted.
        }
        else if( base instanceof List )
        {
            COSArray array = new COSArray();
            List list = (List)base;
            for( int i=0; i<list.size(); i++ )
            {
                array.add( cloneForNewDocument( destination, list.get( i ) ) );
            }
            retval = array;
        }
        else if( base instanceof COSObjectable && !(base instanceof COSBase) )
        {
            retval = cloneForNewDocument( destination, ((COSObjectable)base).getCOSObject() );
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSObject )
        {
            COSObject object = (COSObject)base;
            retval = cloneForNewDocument( destination, object.getObject() );
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSArray )
        {
            COSArray newArray = new COSArray();
            COSArray array = (COSArray)base;
            for( int i=0; i<array.size(); i++ )
            {
                newArray.add( cloneForNewDocument( destination, array.get( i ) ) );
            }
            retval = newArray;
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSStream )
        {
            COSStream originalStream = (COSStream)base;
            List keys = originalStream.keyList();
            PDStream stream = new PDStream( destination, originalStream.getFilteredStream(), true );
            clonedVersion.put( base, stream.getStream() );
            for( int i=0; i<keys.size(); i++ )
            {
                COSName key = (COSName)keys.get( i );
                stream.getStream().setItem( key, cloneForNewDocument(destination,originalStream.getItem(key)));
            }
            retval = stream.getStream();
        }
        else if( base instanceof COSDictionary )
        {
            COSDictionary dic = (COSDictionary)base;
            List keys = dic.keyList();
            retval = new COSDictionary();
            clonedVersion.put( base, retval );
            for( int i=0; i<keys.size(); i++ )
            {
                COSName key = (COSName)keys.get( i );
                ((COSDictionary)retval).setItem( key, cloneForNewDocument(destination,dic.getItem(key)));
            }
        }
        else
        {
            retval = (COSBase)base;
        }
        clonedVersion.put( base, retval );
        return retval;
    }


    /**
     * Deep clone and Merge from Base to Target.<br/>
     * base and target must be instances of the same class.
     * @param destination
     * @param base
     * @param target
     * @throws IOException
     */
    private void cloneMerge( PDDocument destination, COSObjectable base, COSObjectable target) throws IOException
    {
        if( base == null )
        {
            return;
        }
        COSBase retval = (COSBase)clonedVersion.get( base );
        if( retval != null )
        {
          return;
          //we are done, it has already been converted. // ### Is that correct for cloneMerge???
        }
        else if( base instanceof List )
        {
            COSArray array = new COSArray();
            List list = (List)base;
            for( int i=0; i<list.size(); i++ )
            {
                array.add( cloneForNewDocument( destination, list.get( i ) ) );
            }
            ((List)target).add(array);
        }
        else if( base instanceof COSObjectable && !(base instanceof COSBase) )
        {
            cloneMerge(destination, ((COSObjectable)base).getCOSObject(), ((COSObjectable)target).getCOSObject() );
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSObject )
        {
            cloneMerge(destination, ((COSObject) base).getObject(),((COSObject) target).getObject() );
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSArray )
        {
            COSArray array = (COSArray)base;
            for( int i=0; i<array.size(); i++ )
            {
              ((COSArray)target).add( cloneForNewDocument( destination, array.get( i ) ) );
            }
            clonedVersion.put( base, retval );
        }
        else if( base instanceof COSStream )
        {
          // does that make sense???
            COSStream originalStream = (COSStream)base;
            List keys = originalStream.keyList();
            PDStream stream = new PDStream( destination, originalStream.getFilteredStream(), true );
            clonedVersion.put( base, stream.getStream() );
            for( int i=0; i<keys.size(); i++ )
            {
                COSName key = (COSName)keys.get( i );
                stream.getStream().setItem( key, cloneForNewDocument(destination,originalStream.getItem(key)));
            }
            retval = stream.getStream(); 
            target = retval;
        }
        else if( base instanceof COSDictionary )
        {
            COSDictionary dic = (COSDictionary)base;
            List keys = dic.keyList();
            clonedVersion.put( base, retval );
            for( int i=0; i<keys.size(); i++ )
            {
                COSName key = (COSName)keys.get( i );
                if (((COSDictionary)target).getItem(key)!=null)
                {
                   cloneMerge(destination, dic.getItem(key),((COSDictionary)target).getItem(key));
                } 
                else 
                {
                  ((COSDictionary)target).setItem( key, cloneForNewDocument(destination,dic.getItem(key)));
                }
            }
        }
        else
        {
            retval = (COSBase)base;
        }
        clonedVersion.put( base, retval );
        
    }

    
    
    private int nextFieldNum = 1;

    /**
     * Merge the contents of the source form into the destination form
     * for the destination file.
     *
     * @param destination the destination document
     * @param destAcroForm the destination form
     * @param srcAcroForm the source form
     * @throws IOException If an error occurs while adding the field.
     */
    private void mergeAcroForm(PDDocument destination, PDAcroForm destAcroForm, PDAcroForm srcAcroForm)
        throws IOException
    {
        List destFields = destAcroForm.getFields();
        List srcFields = srcAcroForm.getFields();
        if( srcFields != null )
        {
            if( destFields == null )
            {
                destFields = new COSArrayList();
                destAcroForm.setFields( destFields );
            }
            Iterator srcFieldsIterator = srcFields.iterator();
            while (srcFieldsIterator.hasNext())
            {
                PDField srcField = (PDField)srcFieldsIterator.next();
                PDField destField =
                    PDFieldFactory.createField(
                        destAcroForm,
                        (COSDictionary)cloneForNewDocument(destination, srcField.getDictionary() ));
                // if the form already has a field with this name then we need to rename this field
                // to prevent merge conflicts.
                if ( destAcroForm.getField(destField.getFullyQualifiedName()) != null )
                {
                    destField.setPartialName("dummyFieldName"+(nextFieldNum++));
                }
                destFields.add(destField);
            }
        }
    }
    

}
