package com.example.projectmanagement.model;


import com.google.gson.*;

import java.lang.reflect.Type;

public class ResourceModelTypeAdapter implements JsonSerializer<ResourceModel>, JsonDeserializer<ResourceModel> {



    @Override
    public JsonElement serialize(ResourceModel resource, Type type, JsonSerializationContext context) {

        //序列化属性
        JsonObject obj = new JsonObject();
        obj.addProperty("name",resource.getName());
        obj.addProperty("id", resource.getId());
        obj.addProperty("phone", resource.getPhone());
        obj.addProperty("email", resource.getEmail());
        obj.addProperty("type", resource.getType());
        obj.addProperty("dailyRate", resource.getDailyRate());
        obj.addProperty("comment", resource.getComment());

        // 序列化关联任务（仅存储任务ID）
        JsonArray tasks = new JsonArray();
        for (TaskModel task : resource.getAssignedTasks()){
            tasks.add(task.getId());//使用任务ID关联
        }
        obj.add("tasks",tasks);
        return obj;


    }

    @Override
    public ResourceModel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {

        //反序列化属性
        JsonObject obj = json.getAsJsonObject();
        ResourceModel res = new ResourceModel();

        res.setName(obj.get("name").getAsString());
        res.setId(obj.get("id").getAsString());
        res.setPhone(obj.get("phone").getAsString());
        res.setEmail(obj.get("email").getAsString());
        res.setType(obj.get("type").getAsString());
        res.setDailyRate(obj.get("dailyRate").getAsDouble());
        res.setComment(obj.get("comment").getAsString());



        //仅存储任务ID，不创建任务对象
        JsonArray tasks = obj.getAsJsonArray("tasks");
        res.getAssignedTasks().clear(); // 清空临时存储
        tasks.forEach(elem -> {
            String taskId = elem.getAsString();
            // 直接存储任务名称用于后续关联
            res.getAssignedTasks().add(new TaskModel() {{
                setId(taskId);
            }});
        });
        return res;


    }


}
