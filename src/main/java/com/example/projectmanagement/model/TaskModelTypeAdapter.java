package com.example.projectmanagement.model;

import com.google.gson.*;
import javafx.beans.property.*;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class TaskModelTypeAdapter implements JsonSerializer<TaskModel>, JsonDeserializer<TaskModel> {

    @Override
    public JsonElement serialize(TaskModel task, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("taskName", task.getTaskName());
        obj.addProperty("id", task.getId());
        obj.add("startDate", context.serialize(task.getStartDate()));
        obj.add("endDate", context.serialize(task.getEndDate()));
        obj.addProperty("progress", task.getProgress());
        obj.addProperty("leader", task.getLeader());
        obj.addProperty("comment", task.getComment());


        JsonArray resources = new JsonArray();
        for (ResourceModel res : task.getAssignedResources()){
            resources.add(res.getId());//使用资源ID关联避免循环引用
        }
        obj.add("resources",resources);
        return obj;

    }

    @Override
    public TaskModel deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {

        JsonObject obj = json.getAsJsonObject();
        TaskModel task = new TaskModel();



        // 先设置日期字段，跳过临时验证
        LocalDate startDate = context.deserialize(obj.get("startDate"), LocalDate.class);
        LocalDate endDate = context.deserialize(obj.get("endDate"), LocalDate.class);

        // 直接设置属性，不触发监听器
        task.setStartDate(startDate);
        task.setEndDate(endDate);


//        // 确保必填字段存在并处理日期
//        JsonElement startDateElem = obj.get("startDate");
//        JsonElement endDateElem = obj.get("endDate");
//        if (startDateElem == null || endDateElem == null) {
//            throw new JsonParseException("Task must have startDate and endDate");
//        }
//        LocalDate startDate = context.deserialize(startDateElem, LocalDate.class);
//        LocalDate endDate = context.deserialize(endDateElem, LocalDate.class);
//        if (startDate == null || endDate == null) {
//            throw new JsonParseException("Invalid date format");
//        }

        //设置任务属性
        task.setTaskName(obj.get("taskName").getAsString());
        task.setId(obj.get("id").getAsString());
//        task.setStartDate(startDate);
//        task.setEndDate(endDate);
        task.setProgress(obj.get("progress").getAsDouble());
        task.setLeader(obj.get("leader").getAsString());
        task.setComment(obj.get("comment").getAsString());


        // 手动触发验证
        if (startDate.isAfter(endDate)) {
            throw new JsonParseException("开始日期不能晚于结束日期");
        }


        // 处理关联资源（仅存储ID，后续重建）
        JsonArray resources = obj.getAsJsonArray("resources");
        resources.forEach(elem -> {
            String resId = elem.getAsString();
            // 创建临时资源对象仅存储ID
            ResourceModel tempRes = new ResourceModel();
            tempRes.setId(resId);
            task.getAssignedResources().add(tempRes);
        });

        // 手动触发验证和监听器初始化
        task.initListenersAfterDeserialization(); // 新增此行
        return task;
    }
}
