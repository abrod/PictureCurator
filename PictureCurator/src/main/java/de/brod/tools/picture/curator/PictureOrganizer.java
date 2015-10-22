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
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

public class PictureOrganizer implements Initializable {

	@FXML
	ListView<String> imageItems;
	@FXML
	ListView<String> folderItems;
	@FXML
	Pane imageBox;
	@FXML
	ComboBox<String> yearItems;

	private FileHandler fileHandler = new FileHandler();
	private ObservableList<String> observedFolderItems;
	private ObservableList<String> observedImageItems;
	private ObservableList<String> observedYearItems;

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

	private void initYearItems() {
		observedYearItems = getItems(yearItems, new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				loadImageList();
			}
		});

		yearItems.setMaxWidth(Double.MAX_VALUE);
	}

	private void initImageItems() {
		observedImageItems = getItems(imageItems, SelectionMode.MULTIPLE, new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				ImageThread.openImage(fileHandler, imageBox, newValue);
			}
		});
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

	@FXML
	public void reloadImages() {
		loadFolderList();
		loadImageList();
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

	private void loadImageList() {
		observedImageItems.clear();
		//
		String folder = folderItems.getSelectionModel().getSelectedItem();
		String year = yearItems.getSelectionModel().getSelectedItem();
		if (folder != null && year != null) {
			File file = new File(FileHandler.outputFolder, folder + " " + year);
			String[] images = fileHandler.getImages(file);
			observedImageItems.addAll(images);
		}
	}

	@FXML
	public void buttonMovePressed() {
	}

	@FXML
	public void buttonDeletePressed() {
	}

	@FXML
	public void buttonExitPressed() {
		System.exit(0);
	}

	@FXML
	public void openImportPage() {
		PictureCurator.openScene(PictureCurator.IMPORTER);
	}

}
