package com.example.projectmanagement.controller;

import com.example.projectmanagement.model.DataModel;
import com.example.projectmanagement.model.ResourceModel;
import com.example.projectmanagement.model.TaskModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
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
//        // 设置默认日期（确保不为空）
//        if (startDatePicker.getValue() == null) {
//            startDatePicker.setValue(LocalDate.of(2025, 01, 01));
//        }
//        if (endDatePicker.getValue() == null) {
//            endDatePicker.setValue(LocalDate.of(2025, 12, 31));
//        }

        // 从DataModel读取日期
        startDatePicker.setValue(dataModel.getAnalysisStartDate());
        endDatePicker.setValue(dataModel.getAnalysisEndDate());


        // 添加日期变更监听
        startDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dataModel.setAnalysisStartDate(newVal);
        });

        endDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            dataModel.setAnalysisEndDate(newVal);
        });

//        startDatePicker.setValue(LocalDate.of(2025, 01, 01));
//
//        endDatePicker.setValue(LocalDate.of(2025, 12, 31));


        // 初始化双层饼图
        outerPieChart = new PieChart();
        innerPieChart = new PieChart();
        innerPieChart.setLabelsVisible(true); // 显示内层标签
        innerPieChart.setMaxSize(250, 250);    // 调整内层大小
//        innerPieChart.setTranslateX(25);      // X轴偏移
//        innerPieChart.setTranslateY(25);      // Y轴偏移
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

        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        // 有效性检查（包含三种无效情况）
        if (start == null || end == null) {
            showAlert("日期未选择", "请先选择开始日期和结束日期");
            return;
        }
        if (start.isAfter(end)) {
            showAlert("日期顺序错误", "结束日期不能早于开始日期");
            return;
        }

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
                    .filter(task -> task.getStartDate() != null && task.getEndDate() != null)
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


        long periodDays = ChronoUnit.DAYS.between(start, end) + 1;
        periodDays = periodDays > 0 ? periodDays : 1; // 强制最小1天

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        long finalPeriodDays = periodDays;
        dataModel.getResources().forEach(res -> {
            long usedDays = calculateUsedDays(res, start, end);
            double usageRate = (usedDays * 100.0) / finalPeriodDays;
            usageRate = Math.max(0, usageRate); // 强制归零负值
            series.getData().add(new XYChart.Data<>(res.getName(), usageRate));
        });
        usageRateChart.getData().setAll(series);
    }

    // 图表4：类型分布
    private void loadTypeDistributionChart() {


        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

//        long periodDays = ChronoUnit.DAYS.between(start, end) + 1;

        Map<String, Integer> typeCount = new HashMap<>();
        Map<String, Double> typeUsageSum = new HashMap<>();

        // 统计类型数据
        dataModel.getResources().forEach(res -> {

            // 检查资源是否在时间段内有任务
            boolean hasTaskInPeriod = res.getAssignedTasks().stream()
                    .anyMatch(task -> isTaskInPeriod(task, start, end));
            if (!hasTaskInPeriod) return;


            String type = res.getType();
            // 统计类型数量
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);


            // 计算该资源的使用率
            long usedDays = calculateUsedDays(res, start, end);
//            double usageRate = (usedDays * 100.0) / periodDays;
            long periodDays = ChronoUnit.DAYS.between(start, end) + 1;
            double usageRate = (usedDays * 100.0) / periodDays;


            // 累加类型使用率
            typeUsageSum.put(type, typeUsageSum.getOrDefault(type, 0.0) + usageRate);

//            typeUsage.put(type, typeUsage.getOrDefault(type, 0.0) + usageRate);

        });



        // 构建外层饼图（类型占比）
        ObservableList<PieChart.Data> outerData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) -> {
            outerData.add(new PieChart.Data(type + " (" + count + ")", count));
        });
        //外层饼图数据
        outerPieChart.setData(outerData);

        // 构建内层饼图（平均使用率）
        ObservableList<PieChart.Data> innerData = FXCollections.observableArrayList();
        typeCount.forEach((type, count) -> {
            //计算平均使用率
            double avgUsage = typeUsageSum.getOrDefault(type, 0.0) / count;
//            innerData.add(new PieChart.Data(type, avgUsage));

            PieChart.Data data = new PieChart.Data(type, avgUsage);

            // 绑定标签显示格式 (示例："Human\n25.0%")
            data.nameProperty().bind(
                    Bindings.concat(
                            type, "\n",
                            String.format("%.1f", avgUsage), "%"
                    )
            );
            innerData.add(data);
        });

        // 应用内层数据并设置标签样式
        innerPieChart.setData(innerData);
        innerPieChart.getData().forEach(data ->
                data.getNode().getStyleClass().add("inner-pie-label")
        );


        // 第四阶段：调整图表布局
        outerPieChart.setLegendVisible(true);  // 隐藏图例避免重复
        innerPieChart.setLabelsVisible(true);   // 强制显示标签

//        // 设置内层标签偏移（避免与外层重叠）
//        innerPieChart.setTranslateX(25);
//        innerPieChart.setTranslateY(25);
    }

    // 计算资源在时间段内的使用天数
    private long calculateUsedDays(ResourceModel res, LocalDate start, LocalDate end) {
        return res.getAssignedTasks().stream()
                .filter(task -> isTaskInPeriod(task, start, end)) // 过滤无效任务
                .mapToLong(t -> {
                    LocalDate taskStart = t.getStartDate().isBefore(start) ? start : t.getStartDate();
                    LocalDate taskEnd = t.getEndDate().isAfter(end) ? end : t.getEndDate();
                    // 双重保护：确保天数非负
                    long daysBetween = ChronoUnit.DAYS.between(taskStart, taskEnd);
                    return daysBetween >= 0 ? daysBetween + 1 : 0;
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


    // 新增提示框方法
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("输入验证");
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}