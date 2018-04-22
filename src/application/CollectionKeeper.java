package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;


public class CollectionKeeper extends Application {

	//Use for master list
	public static ObservableList<ObservableList<Object>> database = FXCollections.observableArrayList();
	//not hooked up yet
	public static FilteredList<ObservableList<Object>> searchDatabase = new FilteredList<>(database, p -> true);
	//Use for column headers
	public static ObservableList<String> categoryList = FXCollections.observableArrayList();
	public static TableView<ObservableList<Object>> mainTable = new TableView();

	private DatabaseHandler databaseManager = new DatabaseHandler();
	private EntryHandler entryHandler = new EntryHandler();

	@Override
	public void start(Stage primaryStage) {
		try {
			/*
			 * Begin setting up primaryStage.
			 */
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,900,600);
			primaryStage.setTitle("Collection Keeper");

			//Causes all windows to close if primaryStage is closed
			primaryStage.setOnHidden(e -> Platform.exit());

			/*
			 * Creation of menuBar and buttons begins here.
			 */

			//Create menuBar and controllers
			MenuBar menuBar = new MenuBar();
			EventHandler<ActionEvent> menuController = MenuBarController();
			EventHandler<ActionEvent> buttonController = ButtonController();
			menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

			//File Menu
			Menu menuFile = new Menu("File");
			MenuItem menuItemCreateDatabase = new MenuItem("Create");
			MenuItem menuItemLoadDatabase = new MenuItem("Load");
			MenuItem menuItemSaveDatabase = new MenuItem("Save");
			MenuItem menuItemExit = new MenuItem("Exit");
			//Bind EventHandler to menu items
			menuItemCreateDatabase.setOnAction(menuController);
			menuItemLoadDatabase.setOnAction(menuController);
			menuItemSaveDatabase.setOnAction(menuController);
			menuItemExit.setOnAction(menuController);
			//Bind MenuItems to Menu
			menuFile.getItems().addAll(menuItemCreateDatabase, menuItemLoadDatabase, menuItemSaveDatabase, menuItemExit);

			//Edit Menu
			Button buttonEdit = new Button("Edit");
			buttonEdit.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			buttonEdit.setOnAction(buttonController);
			buttonEdit.setAlignment(Pos.TOP_RIGHT);

			//Delete Item
			Button buttonDelete = new Button("Delete");
			buttonDelete.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			buttonDelete.setOnAction(buttonController);
			buttonDelete.setAlignment(Pos.TOP_RIGHT);

			//Add New Item
			Button buttonAddNew = new Button("Add New");
			buttonAddNew.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			buttonAddNew.setOnAction(buttonController);
			buttonAddNew.setAlignment(Pos.TOP_RIGHT);

			//Increase count
			Button buttonPlusOne = new Button("+1");
			buttonPlusOne.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			buttonPlusOne.setOnAction(buttonController);
			buttonPlusOne.setAlignment(Pos.TOP_RIGHT);

			//Decrease count
			Button buttonMinusOne = new Button("-1");
			buttonMinusOne.setMinSize(Button.USE_PREF_SIZE, Button.USE_PREF_SIZE);
			buttonMinusOne.setOnAction(buttonController);
			buttonPlusOne.setAlignment(Pos.TOP_RIGHT);

			/*
			 * Setup of mainTable filtering begins here.
			 */

			//Set up FilterField
			TextField searchBar = new TextField();
			searchBar.setMinSize(200, TextField.USE_PREF_SIZE);
			searchBar.setAlignment(Pos.TOP_CENTER);
			searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
				searchDatabase.setPredicate(Object -> {
					//If Filter text is empty, display all items
					if (newValue == null || newValue.isEmpty()) {
						System.out.println("now empty");
						return true;
					}
					String filterText = String.valueOf(searchBar.getText()).toLowerCase();
					//compare filtered text to all entries
					for (ObservableList<Object> entry : database) {
						if(String.valueOf(entry).toLowerCase().contains(filterText)) {
							return true; // Filter matches
						}
					}
					return false;
				});
			});

			/*
			 * Finishing configuration of primaryStage begins here.
			 */

			//Add all items to Top of BorderPane
			HBox hBox = new HBox(menuBar, searchBar, buttonEdit, buttonDelete, buttonAddNew, buttonPlusOne, buttonMinusOne);
			hBox.setHgrow(menuBar, Priority.ALWAYS);
			hBox.setHgrow(searchBar, Priority.ALWAYS);
			hBox.setHgrow(buttonEdit, Priority.NEVER);
			hBox.setHgrow(buttonDelete, Priority.NEVER);
			hBox.setHgrow(buttonAddNew, Priority.NEVER);
			hBox.setHgrow(buttonPlusOne, Priority.NEVER);
			hBox.setHgrow(buttonMinusOne, Priority.NEVER);
			root.setTop(hBox);
			menuBar.getMenus().addAll(menuFile);

			root.setCenter(mainTable);

			primaryStage.getIcons().add(new Image("mahira.jpg"));
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		//Creates a new Database Directory if one does not exist.
		File saveDir = new File("Databases/");
		boolean exists = saveDir.exists();
		if (!exists) {
			saveDir.mkdir();
		}
		//Begins creating main window.
		launch(args);
	}

	//Handler for MenuBar
	private EventHandler<ActionEvent> MenuBarController()  {
		return new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				MenuItem item = (MenuItem) event.getSource(); // get name of item selected
				String text = item.getText();

				//Handle event
				if ("create".equalsIgnoreCase(text)) {
					databaseManager.createNewDatabase();
				} else if ("load".equalsIgnoreCase(text)) {
					//load database method
					ObservableList<ObservableList<Object>> tempDatabase = DatabaseHandler.loadDatabase();

					//determines whether to rebuild database.
					if (tempDatabase != null) {

						//refreshes working database and categories to empty
						database.clear();
						mainTable.getItems().clear();
						mainTable.getColumns().clear();
						database = tempDatabase;

						for (int i = 0; i < database.get(0).size(); i++) {
							final int colCount = i;
							final TableColumn<ObservableList<Object>, Object> column = new TableColumn<>(String.valueOf(categoryList.get(i)));
							column.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue().get(colCount)));
							mainTable.getColumns().add(column);

						}
						mainTable.setItems(database);

						/*for (int i = 0; i < database.size(); i++) {
							mainTable.getItems().add(database.get(i));
						}
						*/

					}
				} else if ("save".equalsIgnoreCase(text)) {
					try {
						databaseManager.saveDatabase();
					} catch (IOException e) {
						//pass
					}
				} else if ("exit".equalsIgnoreCase(text)) {
					Platform.exit();
				}
			}
		};
	}

	//Println stubs need to be implemented
	//Handler for Buttons
	private EventHandler<ActionEvent> ButtonController() {
		return new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				Button item = (Button) event.getSource(); // get name of item selected
				String text = item.getText();

				//Handle event
				if ("edit".equalsIgnoreCase(text)) {
					try {
						int index = database.indexOf(mainTable.getSelectionModel().getSelectedItem());
						entryHandler.editEntry(database.get(index));
					} catch (ArrayIndexOutOfBoundsException e) {
						//pass
					}
				} else if ("delete".equalsIgnoreCase(text)) {
					try {
						entryHandler.deleteEntry();
					} catch (ArrayIndexOutOfBoundsException e) {
						//pass
					}
				} else if ("add new".equalsIgnoreCase(text)) {
					entryHandler.addEntry();
				} else if ("+1".equalsIgnoreCase(text)) {
					try {
						int index = database.indexOf(mainTable.getSelectionModel().getSelectedItem());
						int count = (int) database.get(index).get(0) + 1;
						database.get(index).set(0, count);
						mainTable.refresh();
					} catch (ArrayIndexOutOfBoundsException e) {
						//pass
					}
				} else if ("-1".equalsIgnoreCase(text)) {
					try {
						int index = database.indexOf(mainTable.getSelectionModel().getSelectedItem());
						int count = (int) database.get(index).get(0) - 1;
						database.get(index).set(0, count);
						mainTable.refresh();
					} catch (ArrayIndexOutOfBoundsException e) {
						//pass
					}
				}
			}
		};
	}
}
