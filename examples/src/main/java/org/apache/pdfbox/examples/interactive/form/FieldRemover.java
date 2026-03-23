/*
 * Copyright 2026 The Apache Software Foundation.
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
package org.apache.pdfbox.examples.interactive.form;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField;

/**
 * Remove an AcroForm field. Use this example as a starting point if you are writing your own
 * field editor.
 *
 * @author Tilman Hausherr
 */
public class FieldRemover
{
    public FieldRemover()
    {
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length != 3)
        {
            usage();
        }
        else
        {
            FieldRemover fieldRemover = new FieldRemover();
            fieldRemover.remove(args[0], args[1], args[2]);
        }
    }

    /**
     * Remove the field by going through the field tree.
     *
     * @param fields list of child fields.
     * @param field the specific field to be removed.
     * @return 
     */
    private boolean removeRecursive(List<PDField> fields, PDField field)
    {
        // search the tree
        for (PDField fieldItem : fields)
        {
            if (fieldItem instanceof PDNonTerminalField)
            {
                PDNonTerminalField ntField = (PDNonTerminalField) fieldItem;
                List<PDField> children = ntField.getChildren();
                if (children.remove(field))
                {
                    ntField.setChildren(children);
                    return true;
                }
                if (removeRecursive(children, field))
                {
                    return true;                    
                }
            }
        }
        return false;
    }

    /**
     * Remove the first field with a matching fully qualified name from the acroform field tree and
     * its widgets from the page annotations.
     *
     * @param src Source file name.
     * @param dst Destination file name.
     * @param fullyQualifiedFieldname fully qualified field name.
     * @throws IOException 
     */
    public void remove(String src, String dst, String fullyQualifiedFieldname) throws IOException
    {
        try (PDDocument doc = Loader.loadPDF(new File(src)))
        {
            Set<PDAnnotationWidget> widgetSet = new HashSet<>();
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            PDField field = acroForm.getField(fullyQualifiedFieldname);
            if (field == null)
            {
                System.out.println("field '" + fullyQualifiedFieldname + "' not found");
                return;
            }
            List<PDField> fields = acroForm.getFields(); // returns only top level
            boolean removed = fields.remove(field);
            if (!removed)
            {
                removed = removeRecursive(fields, field);
            }
            if (removed)
            {
                // find page(s)
                List<PDAnnotationWidget> widgets = field.getWidgets();
                for (PDAnnotationWidget widget : widgets)
                {
                    PDPage page = widget.getPage(); // not always set - we may need to go through all pages
                    if (page != null)
                    {
                        List<PDAnnotation> annotations = page.getAnnotations();
                        removed = annotations.remove(widget);
                        System.out.println("page widget removed? " + removed);
                    }
                    else
                    {
                        widgetSet.add(widget);
                    }
                }
            }
            if (!widgetSet.isEmpty())
            {
                for (PDPage page : doc.getPages())
                {
                    List<PDAnnotation> annotations = page.getAnnotations();
                    widgetSet.forEach(widget -> annotations.remove(widget));
                }
            }
            System.out.println("field removed? " + removed);
            if (removed)
            {
                doc.setAllSecurityToBeRemoved(true); // if encrypted
                doc.getDocumentCatalog().getCOSObject().removeItem(COSName.PERMS); // UR3 perms

                doc.save(new File(dst));
            }
        }
    }

    /**
     * This will print out a message telling how to use this example.
     */
    private static void usage()
    {
        System.err.println("usage: org.apache.pdfbox.examples.interactive.form.RemoveField <pdf-file> <saved-pdf-file> <fully-qualified-field-name>");
    }
}
