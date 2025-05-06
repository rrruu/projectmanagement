package com.example.projectmanagement.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class ResourceModel {

    private final SimpleStringProperty name = new SimpleStringProperty();
    private final SimpleStringProperty id = new SimpleStringProperty();
    private final SimpleStringProperty phone = new SimpleStringProperty();
    private final SimpleStringProperty email = new SimpleStringProperty();
    private final SimpleStringProperty type = new SimpleStringProperty("人力"); // 默认类型为人力
    private final SimpleDoubleProperty dailyRate = new SimpleDoubleProperty();
    private final SimpleStringProperty status = new SimpleStringProperty("可用"); // 新增状态属性
    private final SimpleStringProperty comment = new SimpleStringProperty();


    // 无参构造器用于JSON反序列化
    public ResourceModel() {
    }


    //有参构造
    public ResourceModel(String name,String id,String phone,String email,String type,double dailyRate,String comment){
        this.name.set(name);
        this.id.set(id);
        this.phone.set(phone);
        this.email.set(email);
        this.type.set(type);
        this.dailyRate.set(dailyRate);
        this.comment.set(comment);
    }


    public String getName() {
        return name.get();
    }
    public SimpleStringProperty nameProperty() {
        return name;
    }
    public void setName(String name) {
        this.name.set(name);
    }


    public String getId() {
        return id.get();
    }
    public SimpleStringProperty idProperty() {
        return id;
    }
    public void setId(String id) {
        this.id.set(id);
    }


    public String getPhone() {
        return phone.get();
    }
    public SimpleStringProperty phoneProperty() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone.set(phone);
    }


    public String getEmail() {
        return email.get();
    }
    public SimpleStringProperty emailProperty() {
        return email;
    }
    public void setEmail(String email) {
        this.email.set(email);
    }


    public String getType() {
        return type.get();
    }
    public SimpleStringProperty typeProperty() {
        return type;
    }
    public void setType(String type) {
        this.type.set(type);
    }


    public double getDailyRate() {
        return dailyRate.get();
    }
    public SimpleDoubleProperty dailyRateProperty() {
        return dailyRate;
    }
    public void setDailyRate(double dailyRate) {
        this.dailyRate.set(dailyRate);
    }


    public String getComment() {
        return comment.get();
    }
    public SimpleStringProperty commentProperty() {
        return comment;
    }
    public void setComment(String comment) {
        this.comment.set(comment);
    }

    //用于后续资源调度
    public String getStatus() {
        return status.get();
    }
    public SimpleStringProperty statusProperty() {
        return status;
    }
    public void setStatus(String status) {
        this.status.set(status);
    }
}
