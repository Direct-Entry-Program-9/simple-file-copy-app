package controller;

import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

        /* Task class is a java fx specific thing, we can't use anywhere else */
        /* 1. First we implement Task abstract class -> Anonymous Inner Class */
        /* 2. Then we create an object from the newly implemented class */
        /* 3. Lastly we store the object memory location */
        var task = new Task<Void>() /* class Annon extends Task<Void>*/{    // <- Don't worry about this wired syntax yet
            @Override
            protected Void call() throws Exception {
                FileInputStream fis = new FileInputStream(srcFile);
                FileOutputStream fos = new FileOutputStream(destFile);

                long fileSize = srcFile.length();
                for (int i = 0; i < fileSize; i++) {
                    int readByte = fis.read();
                    fos.write(readByte);
                    updateProgress(i, fileSize);    /* updateProgress(workDone, totalWork) */
                }

                fos.close();
                fis.close();

                return null;
            }
        };

        task.workDoneProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number prevWork, Number curWork) {
                pgbBar.setWidth(pgbContainer.getWidth() / task.getTotalWork() * curWork.doubleValue());
                lblProgress.setText("Progress: " + (task.getProgress() * 100) + "%");
                lblSize.setText((task.getWorkDone() / 1024.0) + " / " + (task.getTotalWork() / 1024.0) + " Kb");
            }
        });

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent workerStateEvent) {
                pgbBar.setWidth(pgbContainer.getWidth());
                new Alert(Alert.AlertType.INFORMATION, "File has been copied successfully").showAndWait();
                lblFolder.setText("No folder selected");
                lblFile.setText("No file selected");
                btnCopy.setDisable(true);
                pgbBar.setWidth(0);
                lblProgress.setText("Progress: 0%");
                lblSize.setText("0 / 0 Kb");
                srcFile = null;
                destDir = null;
            }
        });

        new Thread(task).start();
    }
}
