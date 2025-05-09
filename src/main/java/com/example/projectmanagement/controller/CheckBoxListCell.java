package com.example.projectmanagement.controller;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

public class CheckBoxListCell<T> extends ListCell<T> {
    private final CheckBox checkBox = new CheckBox();

    public CheckBoxListCell() {
        checkBox.setOnAction(event -> {
            if (getItem() != null) {
                getListView().getSelectionModel().select(getItem());
            }
        });
    }

    @Override
    protected void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            setGraphic(checkBox);
            setText(item.toString());
            checkBox.setSelected(getListView().getSelectionModel().getSelectedItems().contains(item));
        }
    }
}