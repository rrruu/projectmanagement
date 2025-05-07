// DataModel.java
package com.example.projectmanagement.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DataModel {
    private static DataModel instance = new DataModel();

    private ObservableList<TaskModel> tasks = FXCollections.observableArrayList();
    private ObservableList<ResourceModel> resources = FXCollections.observableArrayList();

    private DataModel() {} // 私有构造器

    public static DataModel getInstance() {
        return instance;
    }

    public ObservableList<TaskModel> getTasks() {
        return tasks;
    }

    public ObservableList<ResourceModel> getResources() {
        return resources;
    }


    public ResourceModel findResourceById(String id){
        return resources.stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public TaskModel findTaskById(String id){
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}