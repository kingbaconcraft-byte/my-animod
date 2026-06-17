package com.animationcreator.animation;

import com.google.gson.JsonObject;

public class AnimationKeyframe implements Comparable<AnimationKeyframe> {
    public float tick;
    public BoneTransform transform;
    public String easing; // "linear", "ease_in", "ease_out", "ease_in_out"

    public AnimationKeyframe() {
        this.transform = new BoneTransform();
        this.easing = "linear";
    }

    public AnimationKeyframe(float tick, BoneTransform transform) {
        this();
        this.tick = tick;
        this.transform = transform;
    }

    public AnimationKeyframe(float tick, BoneTransform transform, String easing) {
        this.tick = tick;
        this.transform = transform;
        this.easing = easing;
    }

    public static AnimationKeyframe fromJson(JsonObject obj) {
        AnimationKeyframe kf = new AnimationKeyframe();
        if (obj.has("tick")) kf.tick = obj.get("tick").getAsFloat();
        if (obj.has("easing")) kf.easing = obj.get("easing").getAsString();
        if (obj.has("transform")) kf.transform = BoneTransform.fromJson(obj.getAsJsonObject("transform"));
        return kf;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("tick", tick);
        obj.addProperty("easing", easing);
        obj.add("transform", transform.toJson());
        return obj;
    }

    public float applyEasing(float t) {
        return switch (easing) {
            case "ease_in"     -> t * t;
            case "ease_out"    -> 1f - (1f - t) * (1f - t);
            case "ease_in_out" -> t < 0.5f ? 2f * t * t : 1f - (float) Math.pow(-2f * t + 2f, 2) / 2f;
            default            -> t; // linear
        };
    }

    @Override
    public int compareTo(AnimationKeyframe other) {
        return Float.compare(this.tick, other.tick);
    }
}
