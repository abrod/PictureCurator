package de.brod.tools.picture.curator;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.layout.Pane;

public class PictureOrganizer implements Initializable {

	@FXML
	ListView<String> imageItems;
	@FXML
	ListView<String> imageDeletedItems;
	@FXML
	ListView<String> folderItems;
	@FXML
	Pane imageBox;
	@FXML
	ComboBox<String> yearItems;
	@FXML
	Label countImageItems;
	@FXML
	Label countDeletedItems;

	private FileHandler fileHandler = new FileHandler();

	private ObservableList<String> observedFolderItems, observedImageItems, observedDeletedImageItems,
			observedYearItems;

	private void addItems(ObservableList<String> list, File file, Label counterLabel, ListView<String> view) {
		String[] setOfImages = fileHandler.getSetOfImages(file);
		list.addAll(setOfImages);
		int length = setOfImages.length;
		counterLabel.setText(length + " image" + (length != 1 ? "s" : ""));
		if (view != null && setOfImages.length > 0) {
			view.getSelectionModel().select(setOfImages[0]);
		}
	}

	@FXML
	public void buttonDeletePressed() {
	}

	@FXML
	public void buttonDownImage() {
		moveImages(imageItems, FileHandler.deletedFolder);
	}

	@FXML
	public void buttonExitPressed() {
		System.exit(0);
	}

	@FXML
	public void buttonMovePressed() {
	}

	@FXML
	public void buttonUpImage() {
		moveImages(imageDeletedItems, FileHandler.outputFolder);
	}

	private ObservableList<String> getItems(ComboBox<String> combo, ChangeListener<String> changeListener) {
		ObservableList<String> observedItems = getObservableList(changeListener, combo.getSelectionModel());
		combo.setItems(observedItems);
		return observedItems;
	}

	private ObservableList<String> getItems(ListView<String> listView, SelectionMode mode,
			ChangeListener<? super String> changeListener) {
		MultipleSelectionModel<String> selectionModel = listView.getSelectionModel();
		selectionModel.setSelectionMode(mode);
		ObservableList<String> observedItems = getObservableList(changeListener, selectionModel);
		listView.setItems(observedItems);
		return observedItems;
	}

	private ObservableList<String> getObservableList(ChangeListener<? super String> changeListener,
			SelectionModel<String> selectionModel) {
		ObservableList<String> observedItems = FXCollections.observableArrayList();
		selectionModel.selectedItemProperty().addListener(changeListener);
		return observedItems;
	}

	private void initFolderItems() {
		observedFolderItems = getItems(folderItems, SelectionMode.SINGLE, new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				observedYearItems.clear();
				loadYearList(newValue);
			}
		});
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		initFolderItems();
		initYearItems();
		initImageItems();
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
		observedImageItems = getItems(imageItems, SelectionMode.MULTIPLE, new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				ImageThread.openImage(fileHandler, imageBox, newValue);
			}
		});
		observedDeletedImageItems = getItems(imageDeletedItems, SelectionMode.MULTIPLE, new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				ImageThread.openImage(fileHandler, imageBox, newValue);
			}
		});

	}

	private void initYearItems() {
		observedYearItems = getItems(yearItems, new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				loadImageList(imageItems);
			}
		});

		yearItems.setMaxWidth(Double.MAX_VALUE);
	}

	private void loadFolderList() {

		String selectedItem = folderItems.getSelectionModel().getSelectedItem();

		List<String> lstFolderNames = fileHandler.getFolderNames();
		observedFolderItems.clear();
		observedFolderItems.addAll(lstFolderNames);

		if (selectedItem != null && observedFolderItems.contains(selectedItem)) {
			folderItems.getSelectionModel().select(selectedItem);
		} else {
			observedYearItems.clear();
		}
	}

	private void loadImageList(ListView<String> itemsToSelect) {
		observedImageItems.clear();
		observedDeletedImageItems.clear();
		//
		String folder = folderItems.getSelectionModel().getSelectedItem();
		String year = yearItems.getSelectionModel().getSelectedItem();
		if (folder != null && year != null) {
			File file = new File(FileHandler.outputFolder, folder + " " + year);
			File fileDeleted = new File(FileHandler.deletedFolder, folder + " " + year);
			fileHandler.initImages(file, fileDeleted);

			addItems(observedImageItems, file, countImageItems, itemsToSelect);
			addItems(observedDeletedImageItems, fileDeleted, countDeletedItems, null);

		}
	}

	protected void loadYearList(String folderName) {
		String[] years = fileHandler.getYears(folderName);
		String lastYear = yearItems.getSelectionModel().getSelectedItem();
		observedYearItems.clear();
		observedYearItems.addAll(years);
		if (years.length > 0) {
			if (lastYear == null || !observedYearItems.contains(lastYear)) {
				lastYear = years[years.length - 1];
			}
			yearItems.getSelectionModel().select(lastYear);
		}
	}

	private void moveImages(ListView<String> selectionList, File moveToFolder) {
		MultipleSelectionModel<String> selectionModel = selectionList.getSelectionModel();
		int selectedIndex = selectionModel.getSelectedIndex();
		ObservableList<String> selectedItems = selectionModel.getSelectedItems();
		for (String item : selectedItems) {
			fileHandler.renameFile(moveToFolder, folderItems.getSelectionModel().getSelectedItem(), item);
			selectionList.getItems();
		}
		loadImageList(selectionList);
		selectionModel.clearAndSelect(selectedIndex);
	}

	@FXML
	public void openImportPage() {
		PictureCurator.openScene(PictureCurator.IMPORTER);
	}

	@FXML
	public void reloadImages() {
		loadFolderList();
		loadImageList(imageItems);
	}

}
