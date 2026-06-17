package com.animationcreator.animation;

import com.google.gson.JsonObject;

public class BoneTransform {
    public float rotX, rotY, rotZ;
    public float posX, posY, posZ;
    public float scaleX, scaleY, scaleZ;

    public BoneTransform() {
        this.scaleX = 1f;
        this.scaleY = 1f;
        this.scaleZ = 1f;
    }

    public BoneTransform(float rotX, float rotY, float rotZ, float posX, float posY, float posZ) {
        this.rotX = rotX;
        this.rotY = rotY;
        this.rotZ = rotZ;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.scaleX = 1f;
        this.scaleY = 1f;
        this.scaleZ = 1f;
    }

    public static BoneTransform fromJson(JsonObject obj) {
        BoneTransform bt = new BoneTransform();
        if (obj.has("rotX")) bt.rotX = obj.get("rotX").getAsFloat();
        if (obj.has("rotY")) bt.rotY = obj.get("rotY").getAsFloat();
        if (obj.has("rotZ")) bt.rotZ = obj.get("rotZ").getAsFloat();
        if (obj.has("posX")) bt.posX = obj.get("posX").getAsFloat();
        if (obj.has("posY")) bt.posY = obj.get("posY").getAsFloat();
        if (obj.has("posZ")) bt.posZ = obj.get("posZ").getAsFloat();
        if (obj.has("scaleX")) bt.scaleX = obj.get("scaleX").getAsFloat();
        if (obj.has("scaleY")) bt.scaleY = obj.get("scaleY").getAsFloat();
        if (obj.has("scaleZ")) bt.scaleZ = obj.get("scaleZ").getAsFloat();
        return bt;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("rotX", rotX);
        obj.addProperty("rotY", rotY);
        obj.addProperty("rotZ", rotZ);
        obj.addProperty("posX", posX);
        obj.addProperty("posY", posY);
        obj.addProperty("posZ", posZ);
        obj.addProperty("scaleX", scaleX);
        obj.addProperty("scaleY", scaleY);
        obj.addProperty("scaleZ", scaleZ);
        return obj;
    }

    public static BoneTransform lerp(BoneTransform a, BoneTransform b, float t) {
        BoneTransform result = new BoneTransform();
        result.rotX = a.rotX + (b.rotX - a.rotX) * t;
        result.rotY = a.rotY + (b.rotY - a.rotY) * t;
        result.rotZ = a.rotZ + (b.rotZ - a.rotZ) * t;
        result.posX = a.posX + (b.posX - a.posX) * t;
        result.posY = a.posY + (b.posY - a.posY) * t;
        result.posZ = a.posZ + (b.posZ - a.posZ) * t;
        result.scaleX = a.scaleX + (b.scaleX - a.scaleX) * t;
        result.scaleY = a.scaleY + (b.scaleY - a.scaleY) * t;
        result.scaleZ = a.scaleZ + (b.scaleZ - a.scaleZ) * t;
        return result;
    }
}
