package de.brod.tools.picture.curator;

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

		observedImageItems = FXCollections.observableArrayList();
		imageItems.setItems(observedImageItems);

		observedYearItems = FXCollections.observableArrayList();
		yearItems.setItems(observedYearItems);
		yearItems.setMaxWidth(Double.MAX_VALUE);

		reloadImages();
	}

	private void initFolderItems() {
		observedFolderItems = FXCollections.observableArrayList();
		folderItems.setItems(observedFolderItems);
		MultipleSelectionModel<String> selectionModel = folderItems.getSelectionModel();
		selectionModel.setSelectionMode(SelectionMode.SINGLE);
		selectionModel.selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// Your action here
				observedYearItems.clear();
				loadYearList(newValue);
			}
		});
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
		loadImageList();
		loadFolderList();
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
		// String[] array = FileHandler.getImages();
		// observedImageItems.addAll(array);
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
