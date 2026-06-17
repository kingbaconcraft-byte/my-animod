package com.animationcreator.animation;

import com.google.gson.*;
import java.util.*;

public class AnimationTimeline {
    public String boneName;
    public List<AnimationKeyframe> keyframes;

    public AnimationTimeline(String boneName) {
        this.boneName = boneName;
        this.keyframes = new ArrayList<>();
    }

    public static AnimationTimeline fromJson(JsonObject obj) {
        AnimationTimeline tl = new AnimationTimeline(obj.get("boneName").getAsString());
        if (obj.has("keyframes")) {
            JsonArray arr = obj.getAsJsonArray("keyframes");
            for (JsonElement el : arr) tl.keyframes.add(AnimationKeyframe.fromJson(el.getAsJsonObject()));
        }
        Collections.sort(tl.keyframes);
        return tl;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("boneName", boneName);
        JsonArray arr = new JsonArray();
        for (AnimationKeyframe kf : keyframes) arr.add(kf.toJson());
        obj.add("keyframes", arr);
        return obj;
    }

    public void addKeyframe(AnimationKeyframe kf) {
        keyframes.removeIf(k -> Math.abs(k.tick - kf.tick) < 0.01f);
        keyframes.add(kf);
        Collections.sort(keyframes);
    }

    public void removeKeyframe(float tick) {
        keyframes.removeIf(k -> Math.abs(k.tick - tick) < 0.01f);
    }

    public BoneTransform sample(float tick, boolean looping, float duration) {
        if (keyframes.isEmpty()) return new BoneTransform();
        if (looping && duration > 0) {
            tick = tick % duration;
        }
        if (keyframes.size() == 1) return keyframes.get(0).transform;
        AnimationKeyframe prev = null, next = null;
        for (AnimationKeyframe kf : keyframes) {
            if (kf.tick <= tick) prev = kf;
            if (kf.tick >= tick && next == null) next = kf;
        }
        if (prev == null) return keyframes.get(0).transform;
        if (next == null) return keyframes.get(keyframes.size() - 1).transform;
        if (prev == next) return prev.transform;
        float range = next.tick - prev.tick;
        float t = range == 0 ? 0 : (tick - prev.tick) / range;
        t = prev.applyEasing(t);
        return BoneTransform.lerp(prev.transform, next.transform, t);
    }

    public float getLastTick() {
        if (keyframes.isEmpty()) return 0;
        return keyframes.get(keyframes.size() - 1).tick;
    }
}
