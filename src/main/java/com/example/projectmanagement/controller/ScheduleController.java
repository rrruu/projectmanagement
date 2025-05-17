package com.example.projectmanagement.controller;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;

public class ScheduleController {

    @FXML
    private Label monthLabel;

    @FXML
    private GridPane calendarGrid;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    private YearMonth currentYearMonth;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();

        prevMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            refreshCalendar();
        });

        nextMonthButton.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            refreshCalendar();
        });

        refreshCalendar();
    }

    private void refreshCalendar() {
        calendarGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );

        // 设置月份标签
        monthLabel.setText(currentYearMonth.getYear() + "年 " +
                currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA));

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = (firstOfMonth.getDayOfWeek().getValue() + 6) % 7; // 周一为0，周日为6

        LocalDate startDate = firstOfMonth.minusDays(firstDayOfWeek);

        for (int i = 0; i < 6 * 7; i++) {
            LocalDate date = startDate.plusDays(i);

            StackPane dayCell = createDayCell(date);
            int row = (i / 7) + 1;
            int col = i % 7;
            GridPane.setRowIndex(dayCell, row);
            GridPane.setColumnIndex(dayCell, col);
            calendarGrid.getChildren().add(dayCell);
        }
    }

    private StackPane createDayCell(LocalDate date) {
        Rectangle bg = new Rectangle(40, 40);
        bg.setStroke(Color.LIGHTGRAY);
        bg.setFill(date.getMonth().equals(currentYearMonth.getMonth()) ? Color.WHITE : Color.LIGHTGRAY);

        Text dayText = new Text(String.valueOf(date.getDayOfMonth()));
        return new StackPane(bg, dayText);
    }



}