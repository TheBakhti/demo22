package com.example.demo2.dataModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;


public class LibrData {
    private static LibrData instance = new LibrData();
    private static String filename = "TodoListItems.txt";

    private ObservableList<BookData> bookDataObservableList;
    private DateTimeFormatter formatter;

    public static LibrData getInstance() {
        return instance;
    }

    private LibrData() {
        formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    }

    public ObservableList<BookData> getBookDataObservableList() {
        return bookDataObservableList;
    }

    public void addTodoItem(BookData book) {
        bookDataObservableList.add(book);
    }

    public void loadBooks() throws IOException {

        bookDataObservableList = FXCollections.observableArrayList();
        Path path = Paths.get(filename);
        BufferedReader br = Files.newBufferedReader(path);

        String input;

        try {
            while ((input = br.readLine()) != null) {
                String[] itemPieces = input.split("\t");

                String shortDescription = itemPieces[0];
                String details = itemPieces[1];
                String dateString = itemPieces[2];

                LocalDate date = LocalDate.parse(dateString, formatter);
                BookData todoItem = new BookData(shortDescription, details, date);
                bookDataObservableList.add(todoItem);
            }

        } finally {
            if(br != null) {
                br.close();
            }
        }
    }

    public void storeBooks() throws IOException {

        Path path = Paths.get(filename);
        BufferedWriter bw = Files.newBufferedWriter(path);
        try {
            Iterator<BookData> iter = bookDataObservableList.iterator();
            while(iter.hasNext()) {
                BookData item = iter.next();
                bw.write(String.format("%s\t%s\t%s",
                        item.getShortDescription(),
                        item.getDetails(),
                        item.getDeadline().format(formatter)));
                bw.newLine();
            }

        } finally {
            if(bw != null) {
                bw.close();
            }
        }
    }

    public void deleteBook(BookData book) {
        bookDataObservableList.remove(book);
    }

}



