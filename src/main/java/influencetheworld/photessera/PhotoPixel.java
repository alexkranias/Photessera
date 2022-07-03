package influencetheworld.photessera;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Class of the PhotoPixel object. A PhotoPixel is a picture rescaled
 * to replace a designated area of pixels. This object morphs select
 * photos to become their designated pixel-sized emulations. Instances
 * of this class are used as the pictures which compose.
 */
public class PhotoPixel {

    public BufferedImage image;
    public int AVG_RED, AVG_GREEN, AVG_BLUE, BRIGHTNESS;
    public int position;
    private Color[][] pixels;

    private static int NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL = 2;

    /**
     * Constructor for the PhotoPixel Object.
     * @param img BufferedImage composed of the new rescaled and cropped image.
     */
    public PhotoPixel(BufferedImage img) {

        if (img != null) {
            this.image = img;
            this.pixels = new Color[img.getHeight()][img.getWidth()];
            for (int j = 0; j < img.getHeight(); j++) {
                for (int i = (j % NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL); i < img.getWidth(); i += NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL) {
                    this.pixels[i][j] = new Color(img.getRGB(i, j));
                    AVG_RED += this.pixels[i][j].getRed();
                    AVG_GREEN += this.pixels[i][j].getGreen();
                    AVG_BLUE += this.pixels[i][j].getBlue();
                }
            }
            AVG_RED /= ((img.getWidth() * img.getHeight()) / NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL);
            AVG_GREEN /= ((img.getWidth() * img.getHeight()) / NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL);
            AVG_BLUE /= ((img.getWidth() * img.getHeight()) / NUMBER_OF_PIXELS_PER_ANALYZED_PIXEL);
            BRIGHTNESS = (AVG_RED + AVG_GREEN + AVG_BLUE) / 3;
        } else {
            this.image = null;
        }
    }

    public static boolean samePhotoPixel(PhotoPixel a, PhotoPixel b) {
        return (Math.abs(a.AVG_RED - b.AVG_RED) < 5 && Math.abs(a.AVG_GREEN - b.AVG_GREEN) < 5 && Math.abs(a.AVG_BLUE - b.AVG_BLUE) < 5);
    }
}
