package app.sketchit;

import influencetheworld.photessera.FrameRender;
import influencetheworld.photessera.ProgressBox;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Main class of Photessera. This consists of the JavaFX-built
 * main window that allows the user to choose the various options and settings
 * associated with the image or video they are rendering.
 */
public class SketchItApp extends Application {

    private Desktop desktop = Desktop.getDesktop();
    private FileChooser fileChooser = new FileChooser();
    private DirectoryChooser directoryChooser = new DirectoryChooser();

    final String TITLE_OF_PROGRAM = "Photessera";

    public static String tempFolderAddress;

    public static String photesseraDirectoryAddress, renderedFrameFolderAddress, rawFrameFolderAddress;

    private ArrayList<String> selectedPhotoPixelFiles = new ArrayList<String>();
    private String fileAddress, exportFolderAddress, exportFileName;
    private int width, height;
    private int scaleFactor;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) throws Exception {

        String operatingSystem = System.getProperty("os.name");
        if (operatingSystem.contains("Windows")) {
            System.out.println(operatingSystem);
            tempFolderAddress = "C:\\temp";
            photesseraDirectoryAddress = "C:\\temp\\Photessera";
            renderedFrameFolderAddress = photesseraDirectoryAddress + "\\RenderedFrames";
            rawFrameFolderAddress = photesseraDirectoryAddress + "\\RawFrames";
        } else if (operatingSystem.contains("Mac")) {
            tempFolderAddress = System.getProperty("user.home") + "/Documents";
            photesseraDirectoryAddress = tempFolderAddress + "/Photessera";
            renderedFrameFolderAddress = photesseraDirectoryAddress + "/RenderedFrames";
            rawFrameFolderAddress = photesseraDirectoryAddress + "/RawFrames";
            System.out.println(operatingSystem);
        } else {
            setProgressMessage("Operating System Incompatible\n\nOnly Windows and Mac OS Supported");
        }

        File photesseraDirectory = new File(photesseraDirectoryAddress), tempFolder = new File(tempFolderAddress), renderedFramesFolder = new File(renderedFrameFolderAddress), rawFramesFolder = new File(rawFrameFolderAddress);
        tempFolder.setExecutable(true);
        tempFolder.setWritable(true);
        tempFolder.setReadable(true);
        photesseraDirectory.setExecutable(true);
        photesseraDirectory.setWritable(true);
        photesseraDirectory.setReadable(true);
        renderedFramesFolder.setExecutable(true);
        renderedFramesFolder.setWritable(true);
        renderedFramesFolder.setReadable(true);
        rawFramesFolder.setExecutable(true);
        rawFramesFolder.setWritable(true);
        rawFramesFolder.setReadable(true);

        boolean directoryCreated = false;

        //Checks to see if directories exist for frame rendering and if they don't it creates the directories
        if (!tempFolder.exists()) {
            directoryCreated = tempFolder.mkdir();
        }
        if (!photesseraDirectory.exists()) {
            directoryCreated = photesseraDirectory.mkdir();
        }
        System.out.println("renderedFramesFolderAddress: " + renderedFramesFolder.exists());
        if (!renderedFramesFolder.exists() && photesseraDirectory.exists()) {
            directoryCreated = renderedFramesFolder.mkdir();
        }
        System.out.println("rawFrameFolderAddress: " + rawFramesFolder.exists());
        if (!rawFramesFolder.exists() && photesseraDirectory.exists()) {
            directoryCreated = rawFramesFolder.mkdir();
        }

        window.setTitle(TITLE_OF_PROGRAM);

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(12, 12, 12, 12));
        grid.setVgap(8);
        grid.setHgap(10);
        grid.setAlignment(Pos.CENTER);

        //Currently Selected File Label
        Label currentlySelectedFile = new Label("No File Selected");
        currentlySelectedFile.setMinWidth(300);
        currentlySelectedFile.setMaxWidth(300);
        GridPane.setConstraints(currentlySelectedFile, 1, 0);

        //Browse Currently Selected File Button
        Button browseCurrentlySelectedFile = new Button("Select File to Render");
        GridPane.setConstraints(browseCurrentlySelectedFile, 0, 0);
        browseCurrentlySelectedFile.setOnAction(e -> {
            configureFileChooser(fileChooser);
            File file = fileChooser.showOpenDialog(window);
            if (file != null) {
                fileAddress = file.getAbsolutePath();
                currentlySelectedFile.setText(fileAddress);
            }
        });

        //Currently Selected File Label
        Label numberOfSelectedPhotoPixels = new Label("0 Photos Selected as Pixels");
        GridPane.setConstraints(numberOfSelectedPhotoPixels, 8, 0);

        //Select Photos to be used as Pixels Button
        Button selectPhotoPixels = new Button("Select Photos");
        GridPane.setConstraints(selectPhotoPixels, 9, 0);
        selectPhotoPixels.setOnAction(e -> {
            selectedPhotoPixelFiles.clear();
            configureFileChooser(fileChooser);
            List<File> list = fileChooser.showOpenMultipleDialog(window);
            if (list != null) {
                for (File file : list) {
                    selectedPhotoPixelFiles.add(file.getAbsolutePath());
                }
            }
            numberOfSelectedPhotoPixels.setText(selectedPhotoPixelFiles.size() + " Photos Selected as Pixels");
        });

        //Export file name label
        Label resolutionLabel = new Label("    Resolution");
        resolutionLabel.setAlignment(Pos.CENTER);
        GridPane.setConstraints(resolutionLabel, 0, 14);

        //width text field
        TextField widthField = new TextField();
        widthField.setPromptText("Enter 0 for Default");
        GridPane.setConstraints(widthField, 1, 15);

        //label1
        Label label1 = new Label("    Width");
        GridPane.setConstraints(label1, 0, 15);

        //label2
        Label label2 = new Label("    Height");
        GridPane.setConstraints(label2, 0, 16);

        //height text field
        TextField heightField = new TextField();
        heightField.setPromptText("Enter 0 for Default");
        GridPane.setConstraints(heightField, 1, 16);

        //label3
        Label label3 = new Label("    Pixel Size");
        GridPane.setConstraints(label3, 0, 18);

        //scale text field
        TextField scaleFactorField = new TextField();
        scaleFactorField.setPromptText("e.g. 20");
        GridPane.setConstraints(scaleFactorField, 1, 18);

        //Export file name label
        Label exportFileNameLabel = new Label("Export Name");
        GridPane.setConstraints(exportFileNameLabel, 7, 18);

        TextField exportFileNameField = new TextField();
        exportFileNameField.setPromptText("e.g. photesseraVideo");
        GridPane.setConstraints(exportFileNameField, 8, 18);

        //Export Button
        Button exportTo = new Button("     Export     ");
        GridPane.setConstraints(exportTo, 9, 18);
        exportTo.setOnAction(e -> {
            alert("Rendering videos may take SEVERAL MINUTES. While the application MAY NOT BE RESPONDING, THIS IS EXPECTED. DO NOT CLOSE THE WINDOWS.\n\nUntil Multithreading Support is added the application will not respond while your files are being processed.");
            configureDirectoryChooser(directoryChooser);
            File file = directoryChooser.showDialog(window);
            if (file != null) {
                exportFolderAddress = file.getAbsolutePath();
            }
            width = Integer.parseInt(widthField.getText());
            height = Integer.parseInt(heightField.getText());
            scaleFactor = Integer.parseInt(scaleFactorField.getText());
            exportFileName = exportFileNameField.getText();

            FrameRender.photoPixels = null;
            FrameRender.pixelPhotoAddresses = null;

            if (selectedPhotoPixelFiles.size() > 0 && fileAddress != null && scaleFactor > 0 && !exportFileName.equals("") && exportFolderAddress != null) {
                try {
                    new FrameRender(fileAddress, scaleFactor, width, height, exportFileName, exportFolderAddress, selectedPhotoPixelFiles);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        grid.getChildren().addAll(browseCurrentlySelectedFile, currentlySelectedFile, exportTo, numberOfSelectedPhotoPixels, selectPhotoPixels,
                exportFileNameField, exportFileNameLabel, resolutionLabel, widthField, heightField, label1, label2, label3, scaleFactorField);
        Scene main = new Scene(grid, 1050, 430);

        //Label for Info Scene
        String text = "Photessera\nInfluence The World™\nVersion: 1.0.0\nRelease: 08/18/2020\nCreated by Alex Kranias\n\nPhotessera was made to give people the opportunity to explore the artisitic potential of creating photo and video composites of a selection of images. Have you ever considered creating a self-portrait constructed out of images of yourself? Well Photessera gives you that capability, and so much more. It's time to become the creator you always have been.\n\nMessage your creations to @photessera on Instagram to potentially be featured!\n\nSUPPORTED FILE TYPES    -    VIDEO: MP4, MOV    PHOTO: PNG, JPG";
        Label message = new Label(text);
        message.setTextAlignment(TextAlignment.CENTER);
        message.setWrapText(true);

        //Back Button on Info Scene
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> {
            window.setScene(main);
            setProgressMessage("back");
        });

        //Layout for Info Scene
        VBox layout = new VBox(30);
        layout.setPadding(new Insets(20, 200,20,200));
        layout.getChildren().addAll(message, backButton);
        layout.setAlignment(Pos.CENTER);

        Scene info = new Scene(layout, 1050, 430);

        //Info Button
        Button infoButton = new Button("More Info");
        GridPane.setConstraints(infoButton, 0, 1);
        infoButton.setOnAction(e -> {
            window.setScene(info);
            setProgressMessage("info");
        });
        grid.getChildren().add(infoButton);

        window.setResizable(false);
        window.setScene(main);
        window.show();

    }

    /**
     * Opens file chooser to select files
     * @param fileChooser FileChooser object
     */
    private static void configureFileChooser(final FileChooser fileChooser){
        fileChooser.setTitle("Select File(s)");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }

    /**
     * Opens DirectoryChooser to choose directory to export file to
     * @param fileChooser DirectoryChooser object
     */
    private static void configureDirectoryChooser(final DirectoryChooser fileChooser){
        fileChooser.setTitle("Select Folder");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
    }

    /*
    Must learn how to multithread
     */
    public static void setProgressMessage(String text) {
        //ProgressBox.open(text);
    }

    public static void alert(String text) {
        ProgressBox.alert(text);
    }
}