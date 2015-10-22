package de.brod.tools.picture.curator;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Pane;

public class PictureImporter implements Initializable {
	@FXML
	private ListView<String> imageItems;

	@FXML
	private Pane imageBox;

	@FXML
	ListView<String> folderItems;

	@FXML
	Button btnMove;

	@FXML
	Button btnDelete;

	@FXML
	Button btnExit;

	@FXML
	TextField folderInput;

	private ObservableList<String> observedImageItems, observedFolderItems;

	private FileHandler fileHandler = new FileHandler();

	private void initFolderItems() {
		MultipleSelectionModel<String> selectionModel = folderItems.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				folderInput.setText(newValue);
			}
		});
		folderInput.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				checkStateOfButtons();
			}
		});

		observedFolderItems = FXCollections.observableArrayList();
		folderItems.setItems(observedFolderItems);
	}

	protected void checkStateOfButtons() {
		boolean bImageSelected = imageItems.getSelectionModel().getSelectedItem() != null;
		boolean bFolderItemSelected = folderInput.getText() != null && folderInput.getText().length() > 0;
		boolean bMove = bFolderItemSelected && bImageSelected;
		btnMove.setDisable(!bMove);
		btnDelete.setDisable(!bImageSelected);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initImageItems();
		initFolderItems();
		initImageBox();

		reloadImages();
	}

	private void initImageBox() {
		ChangeListener<Number> sizeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth,
					Number newSceneWidth) {
				ImageThread.openImage(fileHandler, imageBox, imageItems.getSelectionModel().getSelectedItem());
			}
		};
		imageBox.widthProperty().addListener(sizeListener);
		imageBox.heightProperty().addListener(sizeListener);
	}

	private void initImageItems() {
		MultipleSelectionModel<String> selectionModel = imageItems.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		selectionModel.selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				checkStateOfButtons();
				ImageThread.openImage(fileHandler, imageBox, newValue);
			}
		});
		observedImageItems = FXCollections.observableArrayList();
		imageItems.setItems(observedImageItems);

	}

	private void loadFolderList() {
		List<String> lstFolderNames = fileHandler.getFolderNames();
		observedFolderItems.clear();
		observedFolderItems.addAll(lstFolderNames);
	}

	private void loadImageList() {
		String[] array = fileHandler.getImages(FileHandler.importFolder);
		observedImageItems.clear();
		observedImageItems.addAll(array);
	}

	@FXML
	public void buttonMovePressed() {
		String moveToFolder = folderInput.getText();
		List<String> selectedItems = imageItems.getSelectionModel().getSelectedItems();

		TextInputDialog dialog = new TextInputDialog(moveToFolder);
		dialog.setTitle("Move images");
		if (selectedItems.size() != 1) {
			dialog.setHeaderText("Move the " + selectedItems.size() + " selected images");
		} else {
			dialog.setHeaderText("Move the selected image");
		}
		dialog.setGraphic(new ImageView(this.getClass().getResource("Rename.png").toString()));
		dialog.setContentText("Move to");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			moveToFolder = result.get();
			// ... user chose OK
			int iNotRenamed = 0;
			for (String string : selectedItems) {
				if (!fileHandler.renameFile(fileHandler.newFolder(), moveToFolder, string)) {
					iNotRenamed++;
				}
			}
			if (iNotRenamed > 0)
				SplashScreen.showError(
						new IOException("Could not rename " + iNotRenamed + " file" + (iNotRenamed > 1 ? "s" : "")));
			reloadImages();
		} else {
			// ... user chose CANCEL or closed the dialog
		}
	}

	@FXML
	public void buttonDeletePressed() {
	}

	@FXML
	public void buttonExitPressed() {
		System.exit(0);
	}

	@FXML
	public void reloadImages() {
		loadImageList();
		loadFolderList();
	}

	@FXML
	public void openOrganizePage() {
		PictureCurator.openScene(PictureCurator.ORGANIZER);
	}

}
