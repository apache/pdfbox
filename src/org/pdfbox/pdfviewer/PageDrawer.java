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
package org.pdfbox.pdfviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.pdfbox.util.Matrix;

import org.pdfbox.pdmodel.PDPage;
import org.pdfbox.pdmodel.PDResources;

import org.pdfbox.pdmodel.common.PDRectangle;
import org.pdfbox.pdmodel.font.PDFont;
import org.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;
import org.pdfbox.pdmodel.interactive.annotation.PDAppearanceStream;
import org.pdfbox.pdmodel.text.PDTextState;

import org.pdfbox.util.PDFStreamEngine;
import org.pdfbox.util.ResourceLoader;
import org.pdfbox.util.TextPosition;

/**
 * This will paint a page in a PDF document to a graphics context.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 1.22 $
 */
public class PageDrawer extends PDFStreamEngine
{

    private Graphics2D graphics;
    protected Dimension pageSize;
    protected PDPage page;

    private List lineSubPaths = new ArrayList();
    private GeneralPath linePath = new GeneralPath();

    /**
     * Default constructor, loads properties from file.
     *
     * @throws IOException If there is an error loading properties from the file.
     */
    public PageDrawer() throws IOException
    {
        super( ResourceLoader.loadProperties( "Resources/PageDrawer.properties", true ) );
    }

    /**
     * This will draw the page to the requested context.
     *
     * @param g The graphics context to draw onto.
     * @param p The page to draw.
     * @param pageDimension The size of the page to draw.
     *
     * @throws IOException If there is an IO error while drawing the page.
     */
    public void drawPage( Graphics g, PDPage p, Dimension pageDimension ) throws IOException
    {
        graphics = (Graphics2D)g;
        page = p;
        pageSize = pageDimension;

        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        PDResources resources = page.findResources();
        processStream( page, resources, page.getContents().getStream() );
        List annotations = page.getAnnotations();
        for( int i=0; i<annotations.size(); i++ )
        {
            PDAnnotation annot = (PDAnnotation)annotations.get( i );
            PDRectangle rect = annot.getRectangle();
            String appearanceName = annot.getAppearanceStream();
            PDAppearanceDictionary appearDictionary = annot.getAppearance();
            if( appearDictionary != null )
            {
                if( appearanceName == null )
                {
                    appearanceName = "default";
                }
                Map appearanceMap = appearDictionary.getNormalAppearance();
                PDAppearanceStream appearance =
                    (PDAppearanceStream)appearanceMap.get( appearanceName );
                if( appearance != null )
                {
                    g.translate( (int)rect.getLowerLeftX(), (int)-rect.getLowerLeftY()  );
                    //g.translate( 20, -20 );
                    processSubStream( page, appearance.getResources(), appearance.getStream() );
                    g.translate( (int)-rect.getLowerLeftX(), (int)+rect.getLowerLeftY()  );
                }
            }
        }
        // Transformations should be done in order
        // 1 - Translate
        // 2 - Rotate
        // 3 - Scale
        // Refer to PDFReference p176 (or 188 in xpdf)
        /*AffineTransform transform = graphics.getTransform();
        transform.setToTranslation( 0, page.findMediaBox().getHeight()/2 );
        transform.setToRotation((double)p.getRotation());
        transform.setTransform( 1, 0, 0, 1, 0, 0 );
        transform.setToScale( 1, 1 );

        AffineTransform rotation = graphics.getTransform();
        rotation.rotate( (page.findRotation() * Math.PI) / 180d );
        graphics.setTransform( rotation );*/

    }

    /**
     * You should override this method if you want to perform an action when a
     * string is being shown.
     *
     * @param text The string to display.
     */
    protected void showCharacter( TextPosition text )
    {
        //should use colorspaces for the font color but for now assume that
        //the font color is black
        try
        {
            if( this.getGraphicsState().getTextState().getRenderingMode() == PDTextState.RENDERING_MODE_FILL_TEXT )
            {
                graphics.setColor( this.getGraphicsState().getNonStrokingColorSpace().createColor() );
            }
            else if( this.getGraphicsState().getTextState().getRenderingMode() == PDTextState.RENDERING_MODE_STROKE_TEXT )
            {
                graphics.setColor( this.getGraphicsState().getStrokingColorSpace().createColor() );
            }
            else
            {
                //need to implement....
            }
            PDFont font = text.getFont();
            font.drawString( text.getCharacter(), graphics, text.getFontSize(), text.getXScale(), text.getYScale(),
                             text.getX(), text.getY() );
        }
        catch( IOException io )
        {
            io.printStackTrace();
        }
    }

    /**
     * Get the graphics that we are currently drawing on.
     *
     * @return The graphics we are drawing on.
     */
    public Graphics2D getGraphics()
    {
        return graphics;
    }

    /**
     * Get the page that is currently being drawn.
     *
     * @return The page that is being drawn.
     */
    public PDPage getPage()
    {
        return page;
    }

    /**
     * Get the size of the page that is currently being drawn.
     *
     * @return The size of the page that is being drawn.
     */
    public Dimension getPageSize()
    {
        return pageSize;
    }

    /**
     * Fix the y coordinate based on page rotation.
     *
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The updated y coordinate.
     */
    public double fixY( double x, double y )
    {
    	return pageSize.getHeight() - y;
    }

    /**
     * Get the current line path to be drawn.
     *
     * @return The current line path to be drawn.
     */
    public GeneralPath getLinePath()
    {
        return linePath;
    }

    /**
     * Set the line path to draw.
     *
     * @param newLinePath Set the line path to draw.
     */
    public void setLinePath(GeneralPath newLinePath)
    {
        if (linePath == null || linePath.getCurrentPoint() == null){
            linePath = newLinePath;
        }else{
            linePath.append (newLinePath, false);
        }
    }

    /**
     * Get the current list of line paths to be drawn.
     *
     * @return The current list of line paths to be drawn.
     */
    public List getLineSubPaths()
    {
        return lineSubPaths;
    }

    /**
     * Set the list of line paths to draw.
     *
     * @param newLineSubPaths Set the list of line paths to draw.
     */
    public void setLineSubPaths(List newLineSubPaths)
    {
        lineSubPaths = newLineSubPaths;
    }


    /**
     *
     * Fill the path
     *
     * @param windingRule The winding rule this path will use.
     */
    public void fillPath(int windingRule) throws IOException{

    	graphics.setColor( getGraphicsState().getNonStrokingColorSpace().createColor() );

        //logger().info("Filling the path with rule: " + windingRule);

    	getLinePath().setWindingRule(windingRule);

    	graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
        List subPaths = getLineSubPaths();
        for( int i=0; i<subPaths.size(); i++ )
        {
            GeneralPath subPath = (GeneralPath)subPaths.get( i );
            if (subPath.getCurrentPoint() != null){ //Sector9's suggestion in bug 1672556
                subPath.closePath();
            }
            graphics.fill( subPath );
        }

            graphics.fill( getLinePath() );
            getLinePath().reset();
    }


    public void setStroke(BasicStroke newStroke){
    	getGraphics().setStroke( newStroke );
    }

    public void StrokePath() throws IOException{
    	graphics.setColor( getGraphicsState().getStrokingColorSpace().createColor() ); //per Ben's 11/15 change in StrokePath.java
        List subPaths = getLineSubPaths();
        for( int i=0; i<subPaths.size(); i++ )
        {
            GeneralPath subPath = (GeneralPath)subPaths.get( i );
            graphics.draw( subPath );
        }
        subPaths.clear();
        GeneralPath path = getLinePath();
        graphics.draw( path );
        path.reset();
    }

    //If you need to do anything when a color changes, do it here ... or in an override of this function
    public void ColorChanged(Boolean bStroking) throws IOException{
        logger().info("changing " + (bStroking ? "" : "non") + "stroking color");
    }

    //This code generalizes the code Jim Lynch wrote for AppendRectangleToPath
    public java.awt.geom.Point2D.Double TransformedPoint (double x, double y){

        double scaleX = 0.0;
        double scaleY = 0.0;
        double transX = 0.0;
        double transY = 0.0;

        double finalX = x;
        double finalY = y;

        //Get the transformation matrix
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();

    	scaleX = at.getScaleX();
        scaleY = at.getScaleY();
        transX = at.getTranslateX();
        transY = at.getTranslateY();

        Point2D Pscale = ScaledPoint (finalX, finalY, scaleX, scaleY);
        finalX = Pscale.getX();
        finalY = Pscale.getY();

        finalX += transX;
      	finalY += transY;

        finalY = fixY( finalX, finalY );
        finalY -= .6;

        return new java.awt.geom.Point2D.Double(finalX, finalY);
    }

    //Use ScaledPoint rather than TransformedPoint in situations where most of the translation
    //need not be repeated.
    //Consider, for example, the second coordinate of a rectangle.
    public java.awt.geom.Point2D.Double ScaledPoint (double x, double y, double scaleX, double scaleY){

        double finalX = 0.0;
        double finalY = 0.0;

        if(scaleX > 0)
    	{
	    	finalX = x * scaleX;
    	}
        if(scaleY > 0)
        {
        	finalY = y * scaleY;
    	}

        return new java.awt.geom.Point2D.Double(finalX, finalY);
    }

    public java.awt.geom.Point2D.Double ScaledPoint (double x, double y){

        double scaleX = 0.0;
        double scaleY = 0.0;

        //Get the transformation matrix
        Matrix ctm = getGraphicsState().getCurrentTransformationMatrix();
        AffineTransform at = ctm.createAffineTransform();
       	scaleX = at.getScaleX();
        scaleY = at.getScaleY();
        return ScaledPoint(x, y, scaleX, scaleY);
    }
}
