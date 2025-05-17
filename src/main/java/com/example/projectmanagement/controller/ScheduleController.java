package com.example.projectmanagement.controller;


import com.example.projectmanagement.db.ScheduleDAO;
import com.example.projectmanagement.model.ScheduleModel;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
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

    @FXML private FlowPane cardsContainer;

    private ObservableList<ScheduleModel> schedules = FXCollections.observableArrayList();

    private YearMonth currentYearMonth;

    private boolean isRefreshing = false; // 添加标志位


    // 添加currentYearMonth的属性支持
    private final ObjectProperty<YearMonth> currentYearMonthProperty = new SimpleObjectProperty<>(YearMonth.now());

    private YearMonth getCurrentYearMonth() {
        return currentYearMonthProperty.get();
    }

    private ObjectProperty<YearMonth> currentYearMonthProperty() {
        return currentYearMonthProperty;
    }

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();

//        prevMonthButton.setOnAction(e -> {
//            currentYearMonth = currentYearMonth.minusMonths(1);
//            refreshCalendar();
//        });
//
//        nextMonthButton.setOnAction(e -> {
//            currentYearMonth = currentYearMonth.plusMonths(1);
//            refreshCalendar();
//        });
//
//        refreshCalendar();


        loadSchedules();
        setupCalendarListeners();
        refreshAll();
    }

    void refreshCalendar() {
        calendarGrid.getChildren().removeIf(node ->
                GridPane.getRowIndex(node) != null && GridPane.getRowIndex(node) > 0
        );

//        // 设置月份标签
//        monthLabel.setText(currentYearMonth.getYear() + "年 " +
//                currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA));
        YearMonth yearMonth = getCurrentYearMonth(); // 使用属性获取当前年月
        monthLabel.setText(yearMonth.getYear() + "年 " +
                yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.CHINA));

        LocalDate firstOfMonth = yearMonth.atDay(1);
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



    private void setupCalendarListeners() {
        // 监听当前月份变化时刷新日程显示
        currentYearMonthProperty().addListener((obs, oldVal, newVal) -> {
            refreshAll();
        });

        // 监听日程列表变化（使用单独方法）
        schedules.addListener(this::handleScheduleListChange);
    }


    // 修改原有按钮事件处理逻辑
    @FXML
    private void handlePrevMonth() {
        currentYearMonthProperty.set(getCurrentYearMonth().minusMonths(1));
    }

    @FXML
    private void handleNextMonth() {
        currentYearMonthProperty.set(getCurrentYearMonth().plusMonths(1));
    }



    private void loadSchedules() {
        try {
            // 临时移除监听器避免触发 refreshAll
            schedules.removeListener(this::handleScheduleListChange);
            schedules.setAll(ScheduleDAO.findAll());
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            schedules.addListener(this::handleScheduleListChange);
        }
    }

    private void handleScheduleListChange(ListChangeListener.Change<? extends ScheduleModel> change) {
        if (!isRefreshing) {
            refreshAll();
        }
    }

    public void refreshAll() {
        if (isRefreshing) return; // 防止重复刷新
        isRefreshing = true;
        loadSchedules();  // 新增：每次刷新前重新加载数据库数据
        refreshCalendar();
        refreshCards();
        isRefreshing = false;
    }

    private void refreshCards() {
        cardsContainer.getChildren().clear();
        schedules.forEach(schedule -> {
            VBox card = new VBox(5);
            card.getStyleClass().add("schedule-card");
            card.setPrefSize(200, 100);

            Label title = new Label(schedule.getTitle());
            Label dates = new Label(schedule.getStartDate() + " - " + schedule.getEndDate());
            Text content = new Text(schedule.getContent());
            content.setWrappingWidth(180);

            card.getChildren().addAll(title, dates, content);
            cardsContainer.getChildren().add(card);
        });
    }

    private StackPane createDayCell(LocalDate date) {
        Rectangle bg = new Rectangle(40, 40);
        bg.setStroke(Color.LIGHTGRAY);

        // 检查是否是日程结束日期
        boolean isScheduleEnd = schedules.stream()
                .anyMatch(s -> s.getEndDate().equals(date));

        bg.setFill(isScheduleEnd ? Color.LIGHTBLUE :
                date.getMonth().equals(currentYearMonth.getMonth()) ? Color.WHITE : Color.LIGHTGRAY);

        Text dayText = new Text(String.valueOf(date.getDayOfMonth()));
        return new StackPane(bg, dayText);
    }


    // 添加打开添加窗口的逻辑
    @FXML
    private void handleAddSchedule() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/projectmanagement/scheduleadd.fxml"));
        Parent root = loader.load();

        ScheduleAddController controller = loader.getController();
        controller.setMainController(this);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("添加日程");
        stage.show();
    }


}