package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

public class ResourceAnalysisController {
    @FXML private BarChart<String, Number> taskCountChart;
    @FXML private BarChart<String, Number> durationChart;
    @FXML private BarChart<String, Number> usageRateChart;
//    @FXML private PieChart typePieChart;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private StackPane pieChartContainer;

    private DataModel dataModel = DataModel.getInstance();
    private PieChart outerPieChart;
    private PieChart innerPieChart;

    @FXML
    private void initialize() {
        // 设置默认日期（确保不为空）
        if (startDatePicker.getValue() == null) {
            startDatePicker.setValue(LocalDate.of(2025, 01, 01));
        }
        if (endDatePicker.getValue() == null) {
            endDatePicker.setValue(LocalDate.of(2025, 12, 31));
        }

//        startDatePicker.setValue(LocalDate.of(2025, 01, 01));
//
//        endDatePicker.setValue(LocalDate.of(2025, 12, 31));


        // 初始化双层饼图
        outerPieChart = new PieChart();
        innerPieChart = new PieChart();
        innerPieChart.setLabelsVisible(false); // 隐藏内层标签
        innerPieChart.setMaxSize(300, 300);    // 调整内层大小
        pieChartContainer.getChildren().addAll(outerPieChart, innerPieChart);

        configureCharts();
        loadAllCharts();
    }

    private void configureCharts() {
        // 图表1配置
        CategoryAxis xAxis1 = new CategoryAxis();
        xAxis1.setLabel("资源名称");
        NumberAxis yAxis1 = new NumberAxis();
        yAxis1.setLabel("任务数量");
        taskCountChart.setTitle("资源任务数量统计");
        taskCountChart.setLegendVisible(false);

        // 图表2配置
        CategoryAxis xAxis2 = new CategoryAxis();
        xAxis2.setLabel("资源名称");
        NumberAxis yAxis2 = new NumberAxis();
        yAxis2.setLabel("总工期（天）");
        durationChart.setTitle("资源总工期统计");
        durationChart.setLegendVisible(false);

        // 图表3配置
        CategoryAxis xAxis3 = new CategoryAxis();
        xAxis3.setLabel("资源名称");
        NumberAxis yAxis3 = new NumberAxis();
        yAxis3.setLabel("使用率 (%)");
        usageRateChart.setTitle("资源使用率统计");
        usageRateChart.setLegendVisible(false);
    }

    @FXML
    private void updateUsageChart() {
        loadUsageRateChart();
        loadTypeDistributionChart();
    }

    private void loadAllCharts() {
        loadTaskCountChart();
        loadDurationChart();
        loadUsageRateChart();
        loadTypeDistributionChart();
    }

    // 图表1：任务数量统计
    private void loadTaskCountChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        dataModel.getResources().forEach(res -> {
            int taskCount = res.getAssignedTasks().size();
            series.getData().add(new XYChart.Data<>(res.getName(), taskCount));
        });
        taskCountChart.getData().setAll(series);
    }

    // 图表2：总工期统计
    private void loadDurationChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        dataModel.getResources().forEach(res -> {
            long totalDuration = res.getAssignedTasks().stream()
                    .mapToLong(t -> ChronoUnit.DAYS.between(t.getStartDate(), t.getEndDate()) + 1)
                    .sum();
            series.getData().add(new XYChart.Data<>(res.getName(), totalDuration));
        });
        durationChart.getData().setAll(series);
    }

    // 图表3：使用率统计
    private void loadUsageRateChart() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null || start.isAfter(end)) {
            return; // 处理无效日期
        }
        long periodDays = ChronoUnit.DAYS.between(start, end) + 1;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        dataModel.getResources().forEach(res -> {
            long usedDays = calculateUsedDays(res, start, end);
            double usageRate = (usedDays * 100.0) / periodDays;
            series.getData().add(new XYChart.Data<>(res.getName(), usageRate));
        });
        usageRateChart.getData().setAll(series);
    }

    // 图表4：类型分布
    private void loadTypeDistributionChart() {


        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();
        if (start == null || end == null || start.isAfter(end)) {
            return;
        }
//        long periodDays = ChronoUnit.DAYS.between(start, end) + 1;

        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Double> typeUsageSum = new HashMap<>();

        // 统计类型数据
        dataModel.getResources().forEach(res -> {
            boolean hasTaskInPeriod = res.getAssignedTasks().stream()
                    .anyMatch(task -> isTaskInPeriod(task, start, end));
            if (!hasTaskInPeriod) return;


            String type = res.getType();
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);

            long usedDays = calculateUsedDays(res, start, end);
//            double usageRate = (usedDays * 100.0) / periodDays;
            long periodDays = ChronoUnit.DAYS.between(start, end) + 1;
            double usageRate = (usedDays * 100.0) / periodDays;
            typeUsageSum.put(type, typeUsageSum.getOrDefault(type, 0.0) + usageRate);

//            typeUsage.put(type, typeUsage.getOrDefault(type, 0.0) + usageRate);

        });

//        // 创建饼图数据
//        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
//        typeCount.forEach((type, count) -> {
//            double avgUsage = typeUsage.getOrDefault(type, 0.0) / count;
//            // 在名称中直接包含统计信息
//            String name = String.format("%s (%d个)\n平均使用率:%.1f%%", type, count, avgUsage);
//            pieData.add(new PieChart.Data(name, count));
//        });
//        typePieChart.setData(pieData);
//
//        // 添加样式类便于自定义样式
//        pieData.forEach(data ->
//                data.getNode().getStyleClass().add("pie-label")
//        );



        // 更新外层饼图（类型占比）
        ObservableList<PieChart.Data> outerData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) -> {
            outerData.add(new PieChart.Data(type + " (" + count + ")", count));
        });
        outerPieChart.setData(outerData);

        // 更新内层饼图（平均使用率）
        ObservableList<PieChart.Data> innerData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) -> {
            double avgUsage = typeUsageSum.getOrDefault(type, 0.0) / count;
            innerData.add(new PieChart.Data(type, avgUsage));
        });
        innerPieChart.setData(innerData);
    }

    // 计算资源在时间段内的使用天数
    private long calculateUsedDays(ResourceModel res, LocalDate start, LocalDate end) {
        return res.getAssignedTasks().stream()
                .mapToLong(t -> {
                    LocalDate taskStart = t.getStartDate().isBefore(start) ? start : t.getStartDate();
                    LocalDate taskEnd = t.getEndDate().isAfter(end) ? end : t.getEndDate();
                    return ChronoUnit.DAYS.between(taskStart, taskEnd) + 1;
                })
                .sum();
    }


    // 新增辅助方法：判断任务是否在时间段内
    private boolean isTaskInPeriod(TaskModel task, LocalDate start, LocalDate end) {
        LocalDate taskStart = task.getStartDate();
        LocalDate taskEnd = task.getEndDate();
        return !taskStart.isAfter(end) && !taskEnd.isBefore(start);
    }

    // 计算资源总体使用率（按所有任务时间）
    private double calculateUsageRate(ResourceModel res) {
        LocalDate earliest = res.getAssignedTasks().stream()
                .map(TaskModel::getStartDate)
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());
        LocalDate latest = res.getAssignedTasks().stream()
                .map(TaskModel::getEndDate)
                .max(LocalDate::compareTo)
                .orElse(LocalDate.now());

        long totalDays = ChronoUnit.DAYS.between(earliest, latest) + 1;
        long usedDays = res.getAssignedTasks().stream()
                .mapToLong(t -> ChronoUnit.DAYS.between(t.getStartDate(), t.getEndDate()) + 1)
                .sum();

        return totalDays == 0 ? 0 : (usedDays * 100.0) / totalDays;
    }
}