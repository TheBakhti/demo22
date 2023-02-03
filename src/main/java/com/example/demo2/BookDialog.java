package com.example.demo2;

import com.example.demo2.dataModel.BookData;
import com.example.demo2.dataModel.LibrData;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;


public class BookDialog {

    @FXML
    private TextField shortDescriptionField;

    @FXML
    private TextArea details;

    @FXML
    private DatePicker deadlinePicker;

    public BookData processResults() {
        String shortDescription = shortDescriptionField.getText().trim();
        String details = this.details.getText().trim();
        LocalDate deadlineValue = deadlinePicker.getValue();

        BookData newItem = new BookData(shortDescription, details, deadlineValue);
        LibrData.getInstance().addTodoItem(newItem);
        return newItem;
    }
}
