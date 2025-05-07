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
        return task;
    }
}
