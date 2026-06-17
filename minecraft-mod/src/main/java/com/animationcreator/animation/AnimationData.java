package com.animationcreator.animation;

import com.google.gson.*;
import java.util.*;

public class AnimationData {
    public String id;
    public String name;
    public String description;
    public String prompt;
    public float durationTicks;
    public boolean looping;
    public String loopMode; // "hold", "loop", "ping_pong"
    public List<AnimationTimeline> timelines;
    public long createdAt;
    public String author;
    public List<String> tags;

    public AnimationData() {
        this.id = UUID.randomUUID().toString();
        this.timelines = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.looping = true;
        this.loopMode = "loop";
        this.durationTicks = 20f;
        this.createdAt = System.currentTimeMillis();
    }

    public AnimationData(String name, String prompt) {
        this();
        this.name = name;
        this.prompt = prompt;
    }

    public static AnimationData fromJson(JsonObject obj) {
        AnimationData data = new AnimationData();
        if (obj.has("id")) data.id = obj.get("id").getAsString();
        if (obj.has("name")) data.name = obj.get("name").getAsString();
        if (obj.has("description")) data.description = obj.get("description").getAsString();
        if (obj.has("prompt")) data.prompt = obj.get("prompt").getAsString();
        if (obj.has("durationTicks")) data.durationTicks = obj.get("durationTicks").getAsFloat();
        if (obj.has("looping")) data.looping = obj.get("looping").getAsBoolean();
        if (obj.has("loopMode")) data.loopMode = obj.get("loopMode").getAsString();
        if (obj.has("createdAt")) data.createdAt = obj.get("createdAt").getAsLong();
        if (obj.has("author")) data.author = obj.get("author").getAsString();
        if (obj.has("tags")) {
            JsonArray tags = obj.getAsJsonArray("tags");
            for (JsonElement tag : tags) data.tags.add(tag.getAsString());
        }
        if (obj.has("timelines")) {
            JsonArray tls = obj.getAsJsonArray("timelines");
            for (JsonElement tl : tls) data.timelines.add(AnimationTimeline.fromJson(tl.getAsJsonObject()));
        }
        return data;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("name", name != null ? name : "Unnamed");
        obj.addProperty("description", description != null ? description : "");
        obj.addProperty("prompt", prompt != null ? prompt : "");
        obj.addProperty("durationTicks", durationTicks);
        obj.addProperty("looping", looping);
        obj.addProperty("loopMode", loopMode);
        obj.addProperty("createdAt", createdAt);
        obj.addProperty("author", author != null ? author : "");
        JsonArray tagsArr = new JsonArray();
        for (String tag : tags) tagsArr.add(tag);
        obj.add("tags", tagsArr);
        JsonArray tlArr = new JsonArray();
        for (AnimationTimeline tl : timelines) tlArr.add(tl.toJson());
        obj.add("timelines", tlArr);
        return obj;
    }

    public float getDurationSeconds() {
        return durationTicks / 20f;
    }

    public AnimationTimeline getOrCreateTimeline(String boneName) {
        for (AnimationTimeline tl : timelines) {
            if (tl.boneName.equals(boneName)) return tl;
        }
        AnimationTimeline tl = new AnimationTimeline(boneName);
        timelines.add(tl);
        return tl;
    }

    public AnimationData copy() {
        AnimationData copy = AnimationData.fromJson(this.toJson());
        copy.id = UUID.randomUUID().toString();
        copy.name = this.name + " (Copy)";
        return copy;
    }
}
