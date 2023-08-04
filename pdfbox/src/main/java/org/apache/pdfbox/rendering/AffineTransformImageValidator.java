package org.apache.pdfbox.rendering;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



final class AffineTransformImageValidator {

    private static final Log LOG = LogFactory.getLog(AffineTransformImageValidator.class);

    /**
     * For a drawing application the initial user space
     * resolution is 72dpi.
     */
    private static final int DEFAULT_USER_RES = 72;

    private AffineTransformImageValidator() { }

    private static Class<?> pathGraphicsClass;

    static {
        try {
            pathGraphicsClass = Class.forName("sun.print.PathGraphics");
        } catch (ClassNotFoundException e) {
            LOG.debug("sun.print.PathGraphics class not found");
            pathGraphicsClass = null;
        }
    }

    /**
     * Potential "NegativeArraySizeException" in "sun.awt.windows.WPathGraphics" when scaling images with an AffineTransform.
     * Occurs during printing of a PDF document using a custom "Printable" object with scaling.
     * Root cause: Improper handling of AffineTransform values leading to zero width or height for the image region.
     * The subsequent calculation of "minDpi" results in division by zero, causing the exception.
     *
     * This function essentially mirrors the behavior of the "drawImageToPlatform" method in "sun.awt.windows.WPathGraphics".
     * It returns false if the inclusion of the given AffineTransform results in width or height of the given image being 0.
     *
     * @param graphics
     * @param img
     * @param xform
     * @return true if the image should be drawn to the graphics object. Otherwise false.
     */
    static boolean isAffineTransformValid(Graphics2D graphics, Image img, AffineTransform xform) {
        if (pathGraphicsClass == null || !pathGraphicsClass.isInstance(graphics)) {
            return true;
        }

        PrinterJob wPrinterJob = getPrinterJob(graphics);
        if (wPrinterJob == null) {
            return true;
        }

        int srcX = 0;
        int srcY = 0;
        int srcWidth = img.getWidth(null);
        int srcHeight = img.getHeight(null);

        /* The full transform to be applied to the image is the
         * caller's transform concatenated on to the transform
         * from user space to device space. If the caller didn't
         * supply a transform then we just act as if they passed
         * in the identify transform.
         */
        AffineTransform fullTransform = graphics.getTransform();
        if (xform == null) {
            xform = new AffineTransform();
        }
        fullTransform.concatenate(xform);

        /* Split the full transform into a pair of
         * transforms. The first transform holds effects
         * that GDI (under Win95) can not perform such
         * as rotation and shearing. The second transform
         * is setup to hold only the scaling effects.
         * These transforms are created such that a point,
         * p, in user space, when transformed by 'fullTransform'
         * lands in the same place as when it is transformed
         * by 'rotTransform' and then 'scaleTransform'.
         *
         * The entire image transformation is not in Java in order
         * to minimize the amount of memory needed in the VM. By
         * dividing the transform in two, we rotate and shear
         * the source image in its own space and only go to
         * the, usually, larger, device space when we ask
         * GDI to perform the final scaling.
         * Clamp this to the device scale for better quality printing.
         */
        double[] fullMatrix = new double[6];
        fullTransform.getMatrix(fullMatrix);

        /* Calculate the amount of scaling in the x
         * and y directions. This scaling is computed by
         * transforming a unit vector along each axis
         * and computing the resulting magnitude.
         * The computed values 'scaleX' and 'scaleY'
         * represent the amount of scaling GDI will be asked
         * to perform.
         */
        Point2D.Float unitVectorX = new Point2D.Float(1, 0);
        Point2D.Float unitVectorY = new Point2D.Float(0, 1);
        fullTransform.deltaTransform(unitVectorX, unitVectorX);
        fullTransform.deltaTransform(unitVectorY, unitVectorY);

        Point2D.Float origin = new Point2D.Float(0, 0);
        double scaleX = unitVectorX.distance(origin);
        double scaleY = unitVectorY.distance(origin);

        final double[] xyRes = getXYRes(wPrinterJob);
        if (xyRes.length == 0) {
            return true;
        }

        double devResX = xyRes[0];
        double devResY = xyRes[1];
        double devScaleX = devResX / DEFAULT_USER_RES;
        double devScaleY = devResY / DEFAULT_USER_RES;

        /* check if rotated or sheared */
        int transformType = fullTransform.getType();
        boolean clampScale = ((transformType &
                               (AffineTransform.TYPE_GENERAL_ROTATION |
                                AffineTransform.TYPE_GENERAL_TRANSFORM)) != 0);
        if (clampScale) {
            if (scaleX > devScaleX) scaleX = devScaleX;
            if (scaleY > devScaleY) scaleY = devScaleY;
        }

        /* We do not need to draw anything if either scaling
         * factor is zero.
         */
        if (scaleX != 0 && scaleY != 0) {

            /* Here's the transformation we will do with Java2D,
            */
            AffineTransform rotTransform = new AffineTransform(
                                        fullMatrix[0] / scaleX,  //m00
                                        fullMatrix[1] / scaleY,  //m10
                                        fullMatrix[2] / scaleX,  //m01
                                        fullMatrix[3] / scaleY,  //m11
                                        fullMatrix[4] / scaleX,  //m02
                                        fullMatrix[5] / scaleY); //m12

            /* The scale transform is not used directly: we instead
             * directly multiply by scaleX and scaleY.
             *
             * Conceptually here is what the scaleTransform is:
             *
             * AffineTransform scaleTransform = new AffineTransform(
             *                      scaleX,                     //m00
             *                      0,                          //m10
             *                      0,                          //m01
             *                      scaleY,                     //m11
             *                      0,                          //m02
             *                      0);                         //m12
             */

            /* Convert the image source's rectangle into the rotated
             * and sheared space. Once there, we calculate a rectangle
             * that encloses the resulting shape. It is this rectangle
             * which defines the size of the BufferedImage we need to
             * create to hold the transformed image.
             */
            Rectangle2D.Float srcRect = new Rectangle2D.Float(srcX, srcY,
                                                              srcWidth,
                                                              srcHeight);

            Shape rotShape = rotTransform.createTransformedShape(srcRect);
            Rectangle2D rotBounds = rotShape.getBounds2D();

            /* add a fudge factor as some fp precision problems have
             * been observed which caused pixels to be rounded down and
             * out of the image.
             */
            rotBounds.setRect(rotBounds.getX(), rotBounds.getY(),
                              rotBounds.getWidth()+0.001,
                              rotBounds.getHeight()+0.001);

            int boundsWidth = (int) rotBounds.getWidth();
            int boundsHeight = (int) rotBounds.getHeight();

            if (boundsWidth > 0 && boundsHeight > 0) {

                /* If the image has transparent or semi-transparent
                 * pixels then we'll have the application re-render
                 * the portion of the page covered by the image.
                 * The BufferedImage will be at the image's resolution
                 * to avoid wasting memory. By re-rendering this portion
                 * of a page all compositing is done by Java2D into
                 * the BufferedImage and then that image is copied to
                 * GDI.
                 * However several special cases can be handled otherwise:
                 * - bitmask transparency with a solid background colour
                 * - images which have transparency color models but no
                 * transparent pixels
                 * - images with bitmask transparency and an IndexColorModel
                 * (the common transparent GIF case) can be handled by
                 * rendering just the opaque pixels.
                 */
                boolean drawOpaque = true;
                if (img instanceof BufferedImage && hasTransparentPixels(graphics, (BufferedImage)img)) {
                    drawOpaque = false;
                    if (isBitmaskTransparency(graphics, (BufferedImage)img)) {
                        return true;
                    }
                    if (!canDoRedraws(graphics)) {
                        drawOpaque = true;
                    }
                }
                // if src region extends beyond the image, the "opaque" path
                // may blit b/g colour (including white) where it shoudn't.
                if ((srcX+srcWidth > img.getWidth(null) ||
                     srcY+srcHeight > img.getHeight(null))
                    && canDoRedraws(graphics)) {
                    drawOpaque = false;
                }
                if (drawOpaque == false) {

                    fullTransform.getMatrix(fullMatrix);

                    Rectangle2D.Float rect =
                        new Rectangle2D.Float(srcX, srcY, srcWidth, srcHeight);

                    Shape shape = fullTransform.createTransformedShape(rect);
                    // Region isn't user space because its potentially
                    // been rotated for landscape.
                    Rectangle2D region = shape.getBounds2D();

                    region.setRect(region.getX(), region.getY(),
                                   region.getWidth()+0.001,
                                   region.getHeight()+0.001);

                    // Try to limit the amount of memory used to 8Mb, so
                    // if at device resolution this exceeds a certain
                    // image size then scale down the region to fit in
                    // that memory, but never to less than 72 dpi.

                    int w = (int)region.getWidth();
                    int h = (int)region.getHeight();
                    if (w == 0 || h == 0) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private static boolean canDoRedraws(Graphics2D graphics) {
        try {
            return (Boolean) pathGraphicsClass.getMethod("canDoRedraws").invoke(graphics);
        } catch (Exception e) {
            LOG.error("canDoRedraws invocation failed", e);
        }
        return false;
    }

    private static boolean isBitmaskTransparency(Graphics2D graphics, BufferedImage img) {
        try {
            Method method = pathGraphicsClass.getDeclaredMethod("isBitmaskTransparency", BufferedImage.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(graphics, img);
        } catch (Exception e) {
            LOG.error("isBitmaskTransparency invocation failed", e);
        }
        return false;
    }

    private static boolean hasTransparentPixels(Graphics2D graphics, BufferedImage img) {
        try {
            Method method = pathGraphicsClass.getDeclaredMethod("hasTransparentPixels", BufferedImage.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(graphics, img);
        } catch (Exception e) {
            LOG.error("hasTransparentPixels invocation failed", e);
        }
        return false;
    }

    private static PrinterJob getPrinterJob(Graphics2D graphics) {
        try {
            return (PrinterJob) pathGraphicsClass.getMethod("getPrinterJob").invoke(graphics);
        } catch (Exception e) {
            LOG.error("getPrinterJob invocation failed", e);
            return null;
        }
    }

    private static double[] getXYRes(Object wPrinterJob) {
        try {
            Method getXResMethod = wPrinterJob.getClass().getDeclaredMethod("getXRes");
            Method getYResMethod = wPrinterJob.getClass().getDeclaredMethod("getYRes");
            getXResMethod.setAccessible(true);
            getYResMethod.setAccessible(true);
            return new double[] {
                    (Double) getXResMethod.invoke(wPrinterJob),
                    (Double) getYResMethod.invoke(wPrinterJob)};
        } catch (Exception e) {
            LOG.error("getXYRes invocation failed", e);
            return new double[] {};
        }
    }
}
