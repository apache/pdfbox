/*
 * Copyright 2014 The Apache Software Foundation.
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
package org.apache.pdfbox.tools.imageio;

import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tilman Hausherr
 */
final class JPEGUtil
{
    private JPEGUtil()
    {
    }
    
    /**
     * Set dpi in a JPEG file
     *
     * @param metadata the meta data
     * @param dpi the dpi
     *
     * @throws IIOInvalidTreeException if something goes wrong
     */
    static void updateMetadata(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException
    {
        MetaUtil.debugLogMetadata(metadata, MetaUtil.JPEG_NATIVE_FORMAT);

        // https://svn.apache.org/viewvc/xmlgraphics/commons/trunk/src/java/org/apache/xmlgraphics/image/writer/imageio/ImageIOJPEGImageWriter.java
        // http://docs.oracle.com/javase/6/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html
        Element root = (Element) metadata.getAsTree(MetaUtil.JPEG_NATIVE_FORMAT);
        NodeList jvarNodeList = root.getElementsByTagName("JPEGvariety");
        Element jvarChild;
        if (jvarNodeList.getLength() == 0)
        {
            jvarChild = new IIOMetadataNode("JPEGvariety");
            root.appendChild(jvarChild);
        }
        else
        {
            jvarChild = (Element) jvarNodeList.item(0);
        }

        NodeList jfifNodeList = jvarChild.getElementsByTagName("app0JFIF");
        Element jfifChild;
        if (jfifNodeList.getLength() == 0)
        {
            jfifChild = new IIOMetadataNode("app0JFIF");
            jvarChild.appendChild(jfifChild);
        }
        else
        {
            jfifChild = (Element) jfifNodeList.item(0);
        }
        if (jfifChild.getAttribute("majorVersion").isEmpty())
        {
            jfifChild.setAttribute("majorVersion", "1");
        }
        if (jfifChild.getAttribute("minorVersion").isEmpty())
        {
            jfifChild.setAttribute("minorVersion", "2");
        }
        jfifChild.setAttribute("resUnits", "1"); // inch
        jfifChild.setAttribute("Xdensity", Integer.toString(dpi));
        jfifChild.setAttribute("Ydensity", Integer.toString(dpi));
        if (jfifChild.getAttribute("thumbWidth").isEmpty())
        {
            jfifChild.setAttribute("thumbWidth", "0");
        }
        if (jfifChild.getAttribute("thumbHeight").isEmpty())
        {
            jfifChild.setAttribute("thumbHeight", "0");
        }
        
        // mergeTree doesn't work for ARGB
        metadata.setFromTree(MetaUtil.JPEG_NATIVE_FORMAT, root); 
    }
}
