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
package org.apache.pdfbox.util;

import java.awt.image.BufferedImage;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.Element;

/**
 *
 * @author Tilman Hausherr
 */
class JPEGUtil
{
    static final String JPEG_NATIVE_FORMAT = "javax_imageio_jpeg_image_1.0";

    static void updateMetadata(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException
    {
        // https://svn.apache.org/repos/asf/xmlgraphics/commons/trunk/src/java/org/apache/xmlgraphics/image/writer/imageio/ImageIOJPEGImageWriter.java
        // http://docs.oracle.com/javase/6/docs/api/javax/imageio/metadata/doc-files/jpeg_metadata.html

        Element root = (Element) metadata.getAsTree(JPEG_NATIVE_FORMAT);
        Element child = (Element) root.getElementsByTagName("app0JFIF").item(0);
        child.setAttribute("resUnits", "1"); // inch
        child.setAttribute("Xdensity", Integer.toString(dpi));
        child.setAttribute("Ydensity", Integer.toString(dpi));
        metadata.mergeTree(JPEG_NATIVE_FORMAT, root);
    }
}
