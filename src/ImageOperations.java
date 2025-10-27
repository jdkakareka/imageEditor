import java.awt.*;
import java.awt.image.BufferedImage;

class ImageOperations {

    /**
     * sets red value of every pixel in the image to zero
     *
     * @param img Buffered Image to edit
     * @return new BufferedImage
     */
    static BufferedImage zeroRed(BufferedImage img) {
        BufferedImage newImg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        for(int w = 0;w < img.getWidth();w++){
            for(int h = 0;h < img.getHeight();h++){
                Color newColor = ColorOperations.getColorAtPos(img,w,h);
                newColor = new Color(0,newColor.getGreen(),newColor.getBlue());
                newImg.setRGB(w,h,newColor.getRGB());
            }
        }
        return newImg;
    }

    /**
     * sets a ppm image to grayscale
     *
     * @param img image to change
     * @return new BufferedImage
     */
    static BufferedImage grayscale(BufferedImage img) {
        BufferedImage newImg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        for(int w = 0;w < img.getWidth();w++){
            for(int h = 0;h < img.getHeight();h++){
                Color newColor = ColorOperations.getColorAtPos(img,w,h);
                int gray = (int)(0.299 * newColor.getRed() + 0.587 * newColor.getGreen() + 0.114 * newColor.getBlue());
                int newRGB = (gray << 16) | (gray << 8) | gray;
                newImg.setRGB(w,h,newRGB);
            }
        }
        return newImg;
    }

    /**
     * Inverts the colors of an image
     * @param img image to edit
     * @return new BufferedImage with inverted colors
     */
    static BufferedImage invert(BufferedImage img) {
        BufferedImage newImg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        for(int w = 0;w < img.getWidth();w++){
            for(int h = 0;h < img.getHeight();h++){
                Color newColor = ColorOperations.getColorAtPos(img,w,h);
                newColor = new Color(255-newColor.getRed(),255-newColor.getGreen(),255-newColor.getBlue());
                newImg.setRGB(w,h,newColor.getRGB());
            }
        }
        return newImg;
    }

    /**
     * mirror the given image
     *
     * @param img image to change
     * @param dir direction to mirror it
     * @return new BufferedImage that is mirrored
     */
    static BufferedImage mirror(BufferedImage img, MirrorMenuItem.MirrorDirection dir) {
        BufferedImage newImg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        for(int w = 0;w < img.getWidth();w++){
            for(int h = 0;h < img.getHeight();h++){
                newImg.setRGB(w,h,img.getRGB(w,h));
            }
        }

        if (dir == MirrorMenuItem.MirrorDirection.VERTICAL) {
            for(int w = 0;w < img.getWidth()/2;w++){
                for(int h = 0;h < img.getHeight();h++){
                    newImg.setRGB(img.getWidth()/2+w,h,img.getRGB(img.getWidth()/2-1-w,h));
                }
            }
        } else {
            for(int w = 0;w < img.getWidth();w++){
                for(int h = 0;h < img.getHeight()/2;h++){
                    newImg.setRGB(w,img.getHeight()/2+h,img.getRGB(w,img.getHeight()/2-1-h));
                }
            }
        }
        return newImg;
    }

    /**
     * rotate a given image
     *
     * @param img image to rotate
     * @param dir direction to rotate the image
     * @return new rotated BufferedImage
     */
    static BufferedImage rotate(BufferedImage img, RotateMenuItem.RotateDirection dir) {
        BufferedImage newImg = new BufferedImage(img.getWidth(),img.getHeight(),BufferedImage.TYPE_INT_RGB);
        if (dir == RotateMenuItem.RotateDirection.CLOCKWISE) {
            for(int h = 0;h < img.getHeight();h++){
                for(int w = 0;w < img.getWidth();w++){
                    newImg.setRGB(img.getHeight()-h-1,w,img.getRGB(w,h));
                }
            }
        } else {
            for(int h = 0;h < img.getHeight();h++){
                for(int w = 0;w < img.getWidth();w++){
                    newImg.setRGB(h,img.getWidth()-w-1,img.getRGB(w,h));
                }
            }
        }
        return newImg;
    }

    /**
     * repeats an image side by side specified number of times
     *
     * @param img image to repeat
     * @param n   number of times to repeat image
     * @param dir direction to add new images in
     * @return new edited BufferedImage
     */
    static BufferedImage repeat(BufferedImage img, int n, RepeatMenuItem.RepeatDirection dir) {
        BufferedImage newImg = null;
        // newImg must be instantiated in both branches with the correct dimensions.
        if (dir == RepeatMenuItem.RepeatDirection.HORIZONTAL) {
            newImg = new BufferedImage(img.getWidth()*n,img.getHeight(),BufferedImage.TYPE_INT_RGB);
            for(int i = 0;i < n;i++){
                for(int w = 0;w < img.getWidth();w++){
                    for(int h = 0;h < img.getHeight();h++){
                        newImg.setRGB(w+i*500,h,img.getRGB(w,h));
                    }
                }
            }
        } else {
            newImg = new BufferedImage(img.getWidth(),img.getHeight()*n,BufferedImage.TYPE_INT_RGB);
            for(int i = 0;i < n;i++){
                for(int w = 0;w < img.getWidth();w++){
                    for(int h = 0;h < img.getHeight();h++){
                        newImg.setRGB(w,h+i*500,img.getRGB(w,h));
                    }
                }
            }
        }
        return newImg;
    }

    /**
     * Zooms in on the image. The zoom factor increases in multiplicatives of 10% and
     * decreases in multiplicatives of 10%.
     *
     * @param img        the original image to zoom in on. The image cannot be already zoomed in
     *                   or out because then the image will be distorted.
     * @param zoomFactor The factor to zoom in by.
     * @return the zoomed in image.
     */
    static BufferedImage zoom(BufferedImage img, double zoomFactor) {
        int newImageWidth = (int) (img.getWidth() * zoomFactor);
        int newImageHeight = (int) (img.getHeight() * zoomFactor);
        BufferedImage newImg = new BufferedImage(newImageWidth, newImageHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, newImageWidth, newImageHeight, null);
        g2d.dispose();
        return newImg;
    }
}
