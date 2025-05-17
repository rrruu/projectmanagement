package com.example.projectmanagement.model;


import java.time.LocalDate;

public class ScheduleModel {

    private String id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String content;


    public ScheduleModel(String id, String title, LocalDate startDate, LocalDate endDate, String content) {
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
