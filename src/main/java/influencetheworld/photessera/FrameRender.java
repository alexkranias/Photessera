package influencetheworld.photessera;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Class associated with taking a set of images and a seperate designated
 * image or video and render indivisual frame(s) by replacing areas of the
 * designated frame with the images which best match the RGB vakues of those
 * areas.
 */
public class FrameRender {

    private static int IMAGE_WIDTH, IMAGE_HEIGHT;
    private static ArrayList<Integer> SCALE_FACTORS_ALLOWED;

    public static final String RENDERED_FRAMES_FOLDER_ADDRESS = App.renderedFrameFolderAddress;
    public static final String RAW_FRAMES_FOLDER_ADDRESS = App.rawFrameFolderAddress;

    /*
    The scale factor effects how much larger a photo pixel
    is compared to a regular pixel. For example, a scale factor
    of 2 results in a photo pixel having a width and height of 2 pixels,
    making it have an area of 4 pixels. Scale factors will be a common
    factor of the width and height of the video/image
    */
    private int scaleFactor;
    private int numOfPhotos;

    public static ArrayList<String> pixelPhotoAddresses;

    public static ArrayList<PhotoPixel> photoPixels;

    private static BufferedImage previousFrame;
    private static BufferedImage previousRenderedFrame;
    private static PhotoPixel[][] previousFramePixels;

    private BufferedImage frame;

    /**
     * Constructor for the FrameRender which when called will
     * initiate the frame rendering process using the provided parameters.
     * @param fileAddress Absolute file path for the image or video that will be recounstructed using a set of images.
     * @param scaleFactor The side length, in pixels, of the new photos which will be reconstructing the selected file.
     * @param width Width of new file in pixels
     * @param height Height of new file in pixels
     * @param outputFileName Name of reconstructed file
     * @param exportFolderAddress Absolute path of the directory which the reconstructed file will be exported to
     * @param pixelPhotoAddresses Absolute paths of the various images which will be reconstructing the selected file
     * @throws IOException
     */
    public FrameRender(String fileAddress, int scaleFactor, int width, int height, String outputFileName, String exportFolderAddress, ArrayList<String> pixelPhotoAddresses) throws IOException {

        File file = new File(fileAddress);
        
        this.IMAGE_HEIGHT = height;
        this.IMAGE_WIDTH = width;

        System.out.println("File Extension: " + (file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1)));
        if (file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("jpg") || file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("png")) {

            this.frame = ImageIO.read(new File(fileAddress));

            if (this.IMAGE_HEIGHT == 0) {
                this.IMAGE_HEIGHT = this.frame.getHeight();
            }
            if (this.IMAGE_WIDTH == 0) {
                this.IMAGE_WIDTH = this.frame.getWidth();
            }

            SCALE_FACTORS_ALLOWED = getFactors(this.IMAGE_WIDTH, this.IMAGE_HEIGHT);

            if (SCALE_FACTORS_ALLOWED.size() <= 1) {
                this.IMAGE_HEIGHT = Math.round(this.IMAGE_HEIGHT / 10) * 10;
                this.IMAGE_WIDTH = Math.round(this.IMAGE_WIDTH / 10) * 10;
            }

            SCALE_FACTORS_ALLOWED = getFactors(this.IMAGE_WIDTH, this.IMAGE_HEIGHT);

            System.out.println("ORIGINAL IMAGE SIZE " + this.frame.getWidth() + " x " + this.frame.getHeight());

            //Calculate scale size for image to selected resolution
            double scale;
            if (IMAGE_WIDTH > this.frame.getWidth()) scale = (double) IMAGE_WIDTH / this.frame.getWidth();
            else scale = (double) IMAGE_HEIGHT / this.frame.getHeight();
            System.out.println("SCALE " + scale);

            //Paste selected frame on to a BufferedImage with the selected resoultion
            //(only do so if image is a smaller resolution than selected resolution)
            BufferedImage temp = this.frame;
            this.frame = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, 5);
            Graphics2D g = (Graphics2D) this.frame.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
            g.drawImage(temp, null, 0, 0);

            //Based on scaling take the current frame and scale it so it becomes the selected resolution
            BufferedImage before = this.frame;
            int w = before.getWidth();
            int h = before.getHeight();
            BufferedImage after = new BufferedImage(w, h, 5);
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            after = scaleOp.filter(before, after);
            this.frame = after;

            System.out.println("NEW FRAME SIZE " + this.frame.getWidth() + " x " + this.frame.getHeight());

            this.numOfPhotos = pixelPhotoAddresses.size();
            this.pixelPhotoAddresses = pixelPhotoAddresses;

            int n = 0;
            while (n < SCALE_FACTORS_ALLOWED.size() - 1 && SCALE_FACTORS_ALLOWED.get(n) < scaleFactor) {
                n++;
            }
            this.scaleFactor = SCALE_FACTORS_ALLOWED.get(n);
            System.out.println("PIXEL SCALE FACTOR: " + this.scaleFactor);

            if (photoPixels == null) {
                photoPixels = new ArrayList<PhotoPixel>();
                int total = pixelPhotoAddresses.size();
                int num = 0;
                for (String address : pixelPhotoAddresses) {
                    PhotoPixel pixel = photoToPixel(address);
                    if (pixel != null) {
                        photoPixels.add(pixel);
                        num++;
                    } else {
                        total--;
                    }
                    App.setProgressMessage(num + "/" + total + " PhotoPixels Installed");
                }
            }

            renderFrame(exportFolderAddress + "\\" + outputFileName + ".jpg");

            previousFrame = this.frame;

            //if the frame being exported is not a frame of a video
            if (!exportFolderAddress.equals(RENDERED_FRAMES_FOLDER_ADDRESS)) {
                App.alert("Rendered Image Exported to\n" + exportFolderAddress);
            }
            
        } else if (file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("mp4") || file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("MOV")) {

            int numOfFrames = VideoFrameConversion.generateFramesFromVideo(fileAddress, RAW_FRAMES_FOLDER_ADDRESS);
            for (int i = 0; i < numOfFrames; i++) {
                App.setProgressMessage("Rendering Frames...\n\n" + i + "/" + numOfFrames + " Frames Rendered");
                FrameRender f = new FrameRender(RAW_FRAMES_FOLDER_ADDRESS + "\\frame-" + i + ".jpg", scaleFactor, width, height, "frame-" + i, RENDERED_FRAMES_FOLDER_ADDRESS,  pixelPhotoAddresses);
            }
            VideoFrameConversion.convertJPGtoMovie(exportFolderAddress + "\\" + outputFileName + ".mp4", RENDERED_FRAMES_FOLDER_ADDRESS, VideoFrameConversion.getFrameRate(fileAddress), numOfFrames);
            App.alert("Rendered Video Exported to\n" + exportFolderAddress);
        
        } else {
            App.alert("Unsupported File Type Selected to Render.\n\nSupported File Types: mp4, mov," +
                    "" +
                    "" +
                    " jpg, png");
        }

    }

    /**
     * Gets the integer factors of two integers
     * @param a A number
     * @param b A number
     * @return ArrayList of the factors of the two integer arguments
     */
    private ArrayList<Integer> getFactors(int a, int b) {
        ArrayList<Integer> factors = new ArrayList<Integer>();
        for (int i = 2; i < a && i < b; i++) {
            if (a % i == 0 && b % i == 0) {
                factors.add(i);
            }
        }
        return factors;
    }

    /**
     * Takes a photo and downscales it to the designated size of a pixel to be later used as a photo pixel.
     * @param photoAddress The directory address for a photo.
     * @return A BufferedImage of the downscaled photo (now a pixel).
     * @throws IOException
     */
    private PhotoPixel photoToPixel(String photoAddress) throws IOException {
        File file = new File(photoAddress);
        if (file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("jpg") || file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("png") || file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(".") + 1).equals("jpeg")) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(file);
            } catch (IOException e) {
            }

            final int image_type = img.getType();

            int sideLength;
            if (img.getWidth() > img.getHeight()) {
                sideLength = img.getHeight();
            } else {
                sideLength = img.getWidth();
            }
            BufferedImage crop = img.getSubimage(0, 0, sideLength, sideLength);

            double scale = (double) this.scaleFactor / sideLength;

            BufferedImage before = crop;
            int w = before.getWidth();
            int h = before.getHeight();
            BufferedImage after = new BufferedImage(w, h, image_type);
            AffineTransform at = new AffineTransform();
            at.scale(scale, scale);
            AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            after = scaleOp.filter(before, after);
            after = after.getSubimage(0, 0, this.scaleFactor, this.scaleFactor);

            System.out.println(photoAddress + " Installed");

            return new PhotoPixel(after);
        }
        return null;
    }

    /**
     * Finds the PhotoPixel which most resembles the inputted section of the frame
     * @param framePixel A small, cropped area of the original frame
     * @return The PhotoPixel which most resembles the inputted framePixel
     */
    private PhotoPixel getMostSimilarPhotoPixel(PhotoPixel framePixel) {
        double minimum = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < photoPixels.size(); i++) {
            double squareDifference = Math.pow(framePixel.AVG_RED - photoPixels.get(i).AVG_RED, 2) + Math.pow(framePixel.AVG_GREEN - photoPixels.get(i).AVG_GREEN, 2) + Math.pow(framePixel.AVG_BLUE - photoPixels.get(i).AVG_BLUE, 2);
            if (squareDifference < minimum) {
                minimum = squareDifference;
                minIndex = i;
            }
        }
        return photoPixels.get(minIndex);
    }

    private PhotoPixel getMostSimilarPhotoPixel(int[] color_RGB) {
        double minimum = Double.MAX_VALUE;
        int minIndex = 0;
        for (int i = 0; i < photoPixels.size(); i++) {
            double squareDifference = Math.pow(color_RGB[0] - photoPixels.get(i).AVG_RED, 2) + Math.pow(color_RGB[1] - photoPixels.get(i).AVG_GREEN, 2) + Math.pow(color_RGB[2] - photoPixels.get(i).AVG_BLUE, 2);
            if (squareDifference < minimum) {
                minimum = squareDifference;
                minIndex = i;
            }
        }
        return photoPixels.get(minIndex);
    }

    /**
     * For any area of the frame which isn't white it randomly is
     * swapped with one of the selected pictures excluding the last
     * selected image (the last element in the array). For all areas
     * that are white they are all replaced with the last element of
     * the photoPixel array.
     * @param framePixel A small, cropped area of the original frame
     * @return A random photopixel from the selected images, except for when the frame is white in which it is always returned by the last element of the photoPixel array.
     */
    private PhotoPixel getRandomPhotoPixel(PhotoPixel framePixel) {
        int index = 0;
        if (framePixel.AVG_RED >= 250 && framePixel.AVG_RED >= 250 && framePixel.AVG_RED >= 250) return photoPixels.get(photoPixels.size() - 1);
        else return photoPixels.get((int)(Math.random() * (photoPixels.size() - 1)));
    }

    static int[] color1_RGB = {255, 255, 255}; //white
    static int[] color1_swap_RGB = {0, 0, 0}; //color to swap white with
    static int[] color2_RGB = {0, 0, 0}; //black
    static int[] color2_swap_RGB = {0, 255, 0}; //color to swap black with

    private PhotoPixel swapColor(PhotoPixel framePixel) {
        if (framePixel.AVG_RED == 0) return getMostSimilarPhotoPixel(color2_swap_RGB);
        else return getMostSimilarPhotoPixel(color1_swap_RGB);
    }

    /**
     * Renders and saves the current frame as a jpg file by reconstructing it using the images in the photoPixels array.
     * @param outputFileName Name of the jpg file which is the reconstructed frame
     * @throws IOException
     */
    public void renderFrame(String outputFileName) throws IOException {

        //if the frame being rendered isn't a frame of a video
        if ((outputFileName.length() < RENDERED_FRAMES_FOLDER_ADDRESS.length()) || ((outputFileName.length() > RENDERED_FRAMES_FOLDER_ADDRESS.length()) && !outputFileName.substring(0, RENDERED_FRAMES_FOLDER_ADDRESS.length()).equals(RENDERED_FRAMES_FOLDER_ADDRESS))) {
            App.setProgressMessage("Analyzing Image...");
        }

        BufferedImage newFrame;

        //If this is the first/only frame
        if (previousFrame == null) {
            newFrame = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, 5);
            newFrame.getGraphics().setColor(Color.white);
            newFrame.getGraphics().fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        } else {
            newFrame = previousFrame;
        }

        System.out.println("FRAME SIZE " + this.frame.getWidth() + " x " + this.frame.getHeight());

        System.out.println("Analyzing Image...");

        System.out.println("Rendering...");
        Graphics2D graphics = (Graphics2D) newFrame.getGraphics();

        PhotoPixel[][] framePixels = new PhotoPixel[IMAGE_HEIGHT / this.scaleFactor][IMAGE_WIDTH / this.scaleFactor];
        for (int j = 0; j < framePixels.length; j++) {
            for (int i = 0; i < framePixels[0].length; i++) {
                framePixels[j][i] = new PhotoPixel(this.frame.getSubimage((i * this.scaleFactor), (j * this.scaleFactor), this.scaleFactor, this.scaleFactor));

                framePixels[j][i].image = getMostSimilarPhotoPixel(framePixels[j][i]).image;

                graphics.drawImage(framePixels[j][i].image, null, i * this.scaleFactor, j * this.scaleFactor);
                framePixels[j][i].image = null;
            }
        }

        //previousRenderedFrame = newFrame;
       // previousFramePixels = framePixels;

        //if the frame being rendered isn't a frame of a video
        if ((outputFileName.length() < RENDERED_FRAMES_FOLDER_ADDRESS.length()) || ((outputFileName.length() > RENDERED_FRAMES_FOLDER_ADDRESS.length()) && !outputFileName.substring(0, RENDERED_FRAMES_FOLDER_ADDRESS.length()).equals(RENDERED_FRAMES_FOLDER_ADDRESS))) {
            App.setProgressMessage("Rendering...");
        }

        System.out.println(outputFileName + " Saved!\n");
        saveAsJPG(newFrame, outputFileName);
    }

    /**
     * Saves a BufferedImage as a JPG file at a specified file address
     * @param img An image
     * @param address Absolute path (including the file itself) of where the file will be stored.
     * @throws IOException
     */
    private static void saveAsJPG(BufferedImage img, String address) throws IOException {
        File outputfile = new File(address);
        ImageIO.write(img, "jpg", outputfile);
    }

}