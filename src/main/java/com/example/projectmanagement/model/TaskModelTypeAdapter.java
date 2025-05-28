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

        // 设置日期属性
        task.setStartDate(startDate);
        task.setEndDate(endDate);


        //设置任务属性
        task.setTaskName(obj.get("taskName").getAsString());
        task.setId(obj.get("id").getAsString());
        task.setProgress(obj.get("progress").getAsDouble());
        task.setLeader(obj.get("leader").getAsString());
        task.setComment(obj.get("comment").getAsString());


        // 手动触发验证
        if (startDate.isAfter(endDate)) {
            throw new JsonParseException("开始日期不能晚于结束日期");
        }


        // 处理关联资源（仅存储ID，后续重建）
        JsonArray resources = obj.getAsJsonArray("resources");
        DataModel dataModel = DataModel.getInstance();
        resources.forEach(elem -> {
            String resId = elem.getAsString();
            ResourceModel realRes = dataModel.findResourceById(resId); // 关键修改：从数据模型获取
            if (realRes != null) {
                task.getAssignedResources().add(realRes);
            }
        });

        // 手动触发验证和监听器初始化
        task.initListenersAfterDeserialization(); // 新增此行
        return task;
    }
}
