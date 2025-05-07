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
        task.setTaskName(obj.get("taskName").getAsString());
        task.setId(obj.get("id").getAsString());
        task.setStartDate(context.deserialize(obj.get("startDate"), LocalDate.class));
        task.setEndDate(context.deserialize(obj.get("endDate"), LocalDate.class));
        task.setProgress(obj.get("progress").getAsDouble());
        task.setLeader(obj.get("leader").getAsString());
        task.setComment(obj.get("comment").getAsString());

        JsonArray resources = obj.getAsJsonArray("resources");
        for (JsonElement elem : resources){
            String resId = elem.getAsString();
            ResourceModel res = DataModel.getInstance().findResourceById(resId);
            if(res != null){
                task.getAssignedResources().add(res);
            }
        }


        return task;
    }
}
