package com.example.demo2;
import com.example.demo2.dataModel.BookData;
import com.example.demo2.dataModel.LibrData;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Controller {

    private List<BookData> bookDataList;

    @FXML
    private ListView<BookData> bookDataListView;

    @FXML
    private TextArea itemDetailsTextArea;

    @FXML
    private Label deadlineLabel;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private ContextMenu listContextMenu;

    @FXML
    private ToggleButton filterToggleButton;

    private FilteredList<BookData> filteredList;

    private Predicate<BookData> wantAllItems;
    private Predicate<BookData> wantTodaysItems;


    public void initialize() {

        listContextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                BookData item = bookDataListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });

        listContextMenu.getItems().addAll(deleteMenuItem);
        bookDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<BookData>() {


            @Override
            public void changed(ObservableValue<? extends BookData> observable, BookData oldValue, BookData newValue) {
                if(newValue != null) {
                   BookData item = bookDataListView.getSelectionModel().getSelectedItem();
                    itemDetailsTextArea.setText(item.getDetails());
                    DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d, yyyy");
                    deadlineLabel.setText(df.format(item.getDeadline()));
                }
            }
        });

        wantAllItems = new Predicate<BookData>() {
            @Override
            public boolean test(BookData todoItem) {
                return true;
            }
        };


        wantTodaysItems = new Predicate<BookData>() {
            @Override
            public boolean test(BookData bookItem) {
                return (bookItem.getDeadline().equals(LocalDate.now()));
            }
        };
        filteredList = new FilteredList<BookData>(LibrData.getInstance().getBookDataObservableList(), wantAllItems);

        SortedList<BookData> sortedList = new SortedList<BookData>(filteredList,
                new Comparator<BookData>() {
                    @Override
                    public int compare(BookData o1, BookData o2) {
                        return o1.getDeadline().compareTo(o2.getDeadline());
                    }
                });

//        todoListView.setItems(TodoData.getInstance().getTodoItems());
        bookDataListView.setItems(sortedList);
        bookDataListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        bookDataListView.getSelectionModel().selectFirst();

        bookDataListView.setCellFactory(new Callback<ListView<BookData>, ListCell<BookData>>() {
            @Override
            public ListCell<BookData> call(ListView<BookData> param) {
                ListCell<BookData> cell = new ListCell<BookData>() {

                    @Override
                    protected void updateItem(BookData book, boolean empty) {
                        super.updateItem(book, empty);
                        if(empty) {
                            setText(null);
                        } else {
                            setText(book.getShortDescription());
                            if(book.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            } else if(book.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if(isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listContextMenu);
                            }

                        });

                return cell;
            }
        });
    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Use this dialog to add a book");
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("bookDialog.fxml"));
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());

        } catch(IOException e) {
            System.out.println("Couldn't load the dialog");
            e.printStackTrace();
            return;
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK) {
            BookDialog controller = fxmlLoader.getController();
            BookData newItem = controller.processResults();
            bookDataListView.getSelectionModel().select(newItem);
        }


    }

    @FXML
    public void handleKeyPressed(KeyEvent keyEvent) {
        BookData selectedItem = bookDataListView.getSelectionModel().getSelectedItem();
        if(selectedItem != null) {
            if(keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleClickListView() {
        BookData item = bookDataListView.getSelectionModel().getSelectedItem();
        itemDetailsTextArea.setText(item.getDetails());
        deadlineLabel.setText(item.getDeadline().toString());
    }

    public void deleteItem(BookData item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete a book");
        alert.setHeaderText("Delete Book: " + item.getShortDescription());
        alert.setContentText("Are you sure?  Press OK to confirm, or cancel to Back out.");
        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && (result.get() == ButtonType.OK)) {
            LibrData.getInstance().deleteBook(item);
        }

    }

    @FXML
    public void handleFilterButton() {
        BookData selectedItem = bookDataListView.getSelectionModel().getSelectedItem();
        if(filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodaysItems);
            if(filteredList.isEmpty()) {
                itemDetailsTextArea.clear();
                deadlineLabel.setText("");
            } else if(filteredList.contains((selectedItem))) {
                bookDataListView.getSelectionModel().select(selectedItem);
            } else {
                bookDataListView.getSelectionModel().selectFirst();
            }
        } else {
            filteredList.setPredicate(wantAllItems);
            bookDataListView.getSelectionModel().select(selectedItem);
        }
    }

    @FXML
    public void handleExit() {
        Platform.exit();

    }
}
