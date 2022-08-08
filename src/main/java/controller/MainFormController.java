package controller;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

public class MainFormController {
    public Label lblProgress;
    public Label lblSize;
    public JFXButton btnSelectFile;
    public Label lblFile;
    public JFXButton btnSelectDir;
    public Label lblFolder;
    public Rectangle pgbContainer;
    public Rectangle pgbBar;
    public JFXButton btnCopy;

    private File srcFile;
    private File destDir;

    public void initialize() {
        btnCopy.setDisable(true);
    }

    public void btnSelectFileOnAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setTitle("Select a file to copy");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files (*.*)", "*.*"));

        srcFile = fileChooser.showOpenDialog(lblFolder.getScene().getWindow());
        if (srcFile != null) {
            lblFile.setText(srcFile.getName() + ". " + (srcFile.length() / 1024.0) + "Kb");
        } else {
            lblFile.setText("No file selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }

    public void btnSelectDirOnAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select a destination folder");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        destDir = directoryChooser.showDialog(lblFolder.getScene().getWindow());

        if (destDir != null) {
            lblFolder.setText(destDir.getAbsolutePath());
        } else {
            lblFolder.setText("No folder selected");
        }

        btnCopy.setDisable(srcFile == null || destDir == null);
    }

    public void btnCopyOnAction(ActionEvent actionEvent) throws IOException {
        FileInputStream fis = new FileInputStream(srcFile);

        File destFile = new File(destDir, srcFile.getName());
        if (!destFile.exists()) {
            destFile.createNewFile();
        } else {
            Optional<ButtonType> result = new Alert(Alert.AlertType.INFORMATION,
                    "File already exists. Do you want to overwrite?",
                    ButtonType.YES, ButtonType.NO).showAndWait();
            if (result.get() == ButtonType.NO) {
                return;
            }
        }

        FileOutputStream fos = new FileOutputStream(destFile);

        int[] byteBuffer = new int[(int) srcFile.length()];
        for (int i = 0; i < byteBuffer.length; i++) {
            byteBuffer[i] = fis.read();
            fos.write(byteBuffer[i]);
        }

        fos.close();
        fis.close();

        new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").show();
        srcFile = null;
        destDir = null;
        lblFolder.setText("No folder selected");
        lblFile.setText("No file selected");
        btnCopy.setDisable(true);

    }
}
