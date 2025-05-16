package com.example.projectmanagement.model;

import com.google.gson.annotations.SerializedName;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 任务数据模型
 */
public class TaskModel {

    private final SimpleStringProperty taskName;
    private final SimpleStringProperty id;
    private final SimpleObjectProperty<LocalDate> startDate;
    private final SimpleObjectProperty<LocalDate> endDate;
    private final ReadOnlyIntegerWrapper duration;
    private final SimpleDoubleProperty progress;
    private final SimpleStringProperty leader;
    private final SimpleStringProperty comment;
    private final ReadOnlyDoubleWrapper cost;

    //新增资源列表属性
    private final ObservableList<ResourceModel> assignedResources = FXCollections.observableArrayList();



    // 改进点1：优化监听器初始化
    private void initListeners() {
        // 日期变化监听（同时影响工期和成本）
        startDate.addListener((obs, oldVal, newVal) -> {
            updateDuration();
            calculateCost();
        });

        endDate.addListener((obs, oldVal, newVal) -> {
            updateDuration();
            calculateCost();
        });

        // 资源列表变化监听
        assignedResources.addListener((ListChangeListener<ResourceModel>) c -> {
            calculateCost();
            // 同时监听资源单价变化
            while (c.next()) {
                c.getAddedSubList().forEach(res ->
                        res.dailyRateProperty().addListener((obs, o, n) -> calculateCost())
                );
                c.getRemoved().forEach(res ->
                        res.dailyRateProperty().removeListener((obs, o, n) -> calculateCost())
                );
            }
        });
    }




    // 添加无参构造函数
    public TaskModel() {
        this.taskName = new SimpleStringProperty();
        this.id = new SimpleStringProperty();
        this.startDate = new SimpleObjectProperty<>();
        this.endDate = new SimpleObjectProperty<>();
        this.progress = new SimpleDoubleProperty();
        this.leader = new SimpleStringProperty();
        this.comment = new SimpleStringProperty();
        this.duration = new ReadOnlyIntegerWrapper(0);
        this.cost = new ReadOnlyDoubleWrapper(0);

//        initListeners(); // 初始化监听器
        calculateCost(); // 初始计算成本
    }

    //有参构造函数
    public TaskModel(String taskName, String id, LocalDate startDate, LocalDate endDate,
                     double progress, String leader,String comment) {

        //校验开始日期和结束日期正确性
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }

        this.taskName = new SimpleStringProperty(taskName);
        this.id = new SimpleStringProperty(id);
        this.startDate = new SimpleObjectProperty<>(startDate);
        this.endDate = new SimpleObjectProperty<>(endDate);
        this.progress = new SimpleDoubleProperty(progress);
        this.leader = new SimpleStringProperty(leader);
        this.comment = new SimpleStringProperty(comment);

        // 自动计算工期
        this.duration = new ReadOnlyIntegerWrapper(
                (int) ChronoUnit.DAYS.between(startDate, endDate) + 1
        );

        this.cost = new ReadOnlyDoubleWrapper(0);


        initListeners(); // 初始化监听器
        calculateCost(); // 初始计算成本

    }

    private void updateDuration() {
        duration.set((int) ChronoUnit.DAYS.between(startDate.get(), endDate.get()) + 1);
    }

    @SerializedName("taskName")
    public String getTaskName() {
        return taskName.get();
    }
    public SimpleStringProperty taskNameProperty() {
        return taskName;
    }
    public void setTaskName(String taskName) {
        this.taskName.set(taskName);
    }

    @SerializedName("id")
    public String getId() {
        return id.get();
    }
    public SimpleStringProperty idProperty() {
        return id;
    }
    public void setId(String id) {
        this.id.set(id);
    }

    @SerializedName("startDate")
    public LocalDate getStartDate() {
        return startDate.get();
    }
    public SimpleObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }
    public void setStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("开始日期不能为空");
        }
        if (endDate.get() != null && startDate.isAfter(endDate.get())) {
            throw new IllegalArgumentException("开始日期不能晚于结束日期");
        }
        this.startDate.set(startDate);
    }


    @SerializedName("endDate")
    public LocalDate getEndDate() {
        return endDate.get();
    }
    public SimpleObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }
    public void setEndDate(LocalDate endDate) {
        if (endDate == null) {
            throw new IllegalArgumentException("结束日期不能为空");
        }
        if (startDate.get() != null && endDate.isBefore(startDate.get())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
        this.endDate.set(endDate);
    }

    @SerializedName("duration")
    public int getDuration() {
        return duration.get();
    }
    public ReadOnlyIntegerProperty durationProperty(){
        return duration.getReadOnlyProperty();
    }
    public void setDuration(int duration) {
        this.duration.set(duration);
    }


    @SerializedName("progress")
    public double getProgress() {
        return progress.get();
    }
    public SimpleDoubleProperty progressProperty() {
        return progress;
    }
    public void setProgress(double progress) {
        this.progress.set(progress);
    }


    @SerializedName("leader")
    public String getLeader() {
        return leader.get();
    }
    public SimpleStringProperty leaderProperty() {
        return leader;
    }
    public void setLeader(String leader) {
        this.leader.set(leader);
    }


    @SerializedName("comment")
    public String getComment() {
        return comment.get();
    }
    public SimpleStringProperty commentProperty() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment.set(comment);
    }


    @SerializedName("assignedResources")
    public ObservableList<ResourceModel> getAssignedResources() {
        return assignedResources;
    }


    public ReadOnlyDoubleProperty costProperty() {
        return cost.getReadOnlyProperty();
    }

    @SerializedName("cost")
    public double getCost(){
        return  cost.get();
    }

    // 关联资源展示方法
    public String getAssignedResourcesInfo() {
        return assignedResources.stream()
                .map(res -> res.getName() + "(" + res.getId() + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    private void calculateCost() {
        try {
            double total = getAssignedResources().stream()
                    .mapToDouble(ResourceModel::getDailyRate)
                    .sum() * getDuration();
            cost.set(total);
        } catch (Exception e) {
            // 异常处理（例如资源未正确初始化时）
            cost.set(0);
        }
    }

//    //任务成本计算
//    public double getCost() {
//        return getAssignedResources().stream()
//                .mapToDouble(ResourceModel::getDailyRate)
//                .sum() * getDuration();
//    }

    // 新增方法：手动初始化监听器（供反序列化后调用）
    public void initListenersAfterDeserialization() {
        initListeners();
        calculateCost(); // 重新计算
    }

}