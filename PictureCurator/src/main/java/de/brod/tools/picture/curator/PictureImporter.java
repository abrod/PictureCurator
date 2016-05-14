package de.brod.tools.picture.curator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
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

	@FXML
	public void buttonDeleteNonHDR() {
		List<String> lstNonHDR = new ArrayList<>();
		List<String> selectedItems = imageItems.getItems();
		for (String item : selectedItems) {
			if (item.endsWith(FileHandler.HDR_KEY)) {
				String name = item.substring(0, item.lastIndexOf("(")).trim();
				if (selectedItems.contains(name)) {
					lstNonHDR.add(name);
				}
			}
		}
		if (lstNonHDR.size() == 0) {
			showInformation("Information", "No HDR pictures found");
		} else {
			if (showConfirmation("Delete", "Delete NON HDR files",
					"Do you really want to delete " + lstNonHDR.size() + " NON HDR files ?")) {
				moveFiles(fileHandler.deleteFolder("nonHDR"), "", lstNonHDR);
			}
		}

	}

	@FXML
	public void buttonDeletePressed() {
		List<String> selectedItems = imageItems.getSelectionModel().getSelectedItems();
		String headerText = getSelectedItems("Delete", selectedItems);

	}

	@FXML
	public void buttonExitPressed() {
		System.exit(0);
	}

	@FXML
	public void buttonMovePressed() {
		List<String> selectedItems = imageItems.getSelectionModel().getSelectedItems();
		String headerText = getSelectedItems("Move", selectedItems);

		String moveToFolder = folderInput.getText();
		Optional<String> result = showDialog(moveToFolder, "Rename.png", headerText, "Move images", "Move to");

		if (result.isPresent()) {
			moveToFolder = result.get();
			// ... user chose OK
			moveFiles(fileHandler.newFolder(), moveToFolder, selectedItems);
		} else {
			// ... user chose CANCEL or closed the dialog
		}
	}

	protected void checkStateOfButtons() {
		boolean bImageSelected = imageItems.getSelectionModel().getSelectedItem() != null;
		boolean bFolderItemSelected = folderInput.getText() != null && folderInput.getText().length() > 0;
		boolean bMove = bFolderItemSelected && bImageSelected;
		btnMove.setDisable(!bMove);
		btnDelete.setDisable(!bImageSelected);
	}

	private String getSelectedItems(String text, List<String> selectedItems) {
		String headerText;
		if (selectedItems.size() != 1) {
			headerText = text + " the " + selectedItems.size() + " selected images";
		} else {
			headerText = text + " the selected image";
		}
		return headerText;
	}

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
		fileHandler.initImages(FileHandler.importFolder);
		String[] array = fileHandler.getSetOfImages(FileHandler.importFolder);
		observedImageItems.clear();
		observedImageItems.addAll(array);
	}

	private void moveFiles(File baseFolder, String moveToFolder, List<String> selectedItems) {
		int iNotRenamed = 0;
		for (String fileName : selectedItems) {
			if (!fileHandler.renameFile(baseFolder, moveToFolder, fileName)) {
				iNotRenamed++;
			}
		}
		if (iNotRenamed > 0) {
			SplashScreen.showError(
					new IOException("Could not rename " + iNotRenamed + " file" + (iNotRenamed > 1 ? "s" : "")));
		}
		reloadImages();

	}

	@FXML
	public void openOrganizePage() {
		PictureCurator.openScene(PictureCurator.ORGANIZER);
	}

	@FXML
	public void reloadImages() {
		loadImageList();
		loadFolderList();
	}

	private boolean showConfirmation(String title, String header, String content) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		Optional<ButtonType> result = alert.showAndWait();
		return (result.get() == ButtonType.OK);
	}

	private Optional<String> showDialog(String inputText, String imageName, String headerText, String title,
			String contentText) {
		Dialog<String> dialog = new TextInputDialog(inputText);
		dialog.setTitle(title);
		dialog.setHeaderText(headerText);
		if (imageName != null) {
			dialog.setGraphic(new ImageView(this.getClass().getResource(imageName).toString()));
		}
		dialog.setContentText(contentText);
		Optional<String> result = dialog.showAndWait();
		return result;
	}

	private void showInformation(String title, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

}
