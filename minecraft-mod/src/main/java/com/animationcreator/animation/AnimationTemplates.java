package com.animationcreator.animation;

import java.util.*;

/**
 * Offline keyword-based animation generator.
 * Analyses the prompt and builds animation keyframes from pre-built templates.
 * Works 100% offline – no API key required.
 */
public class AnimationTemplates {

    public static AnimationData generate(String prompt) {
        String lower = prompt.toLowerCase(Locale.ROOT);
        AnimationData anim = new AnimationData();
        anim.prompt = prompt;

        if (contains(lower, "walk", "walking", "stride", "march")) {
            return applyWalking(anim, prompt);
        } else if (contains(lower, "run", "running", "sprint", "sprint")) {
            return applyRunning(anim, prompt);
        } else if (contains(lower, "wave", "waving", "hello", "greet", "hi")) {
            return applyWave(anim, prompt);
        } else if (contains(lower, "jump", "jumping", "leap", "hop")) {
            return applyJump(anim, prompt);
        } else if (contains(lower, "attack", "strike", "hit", "slash", "punch")) {
            return applyAttack(anim, prompt);
        } else if (contains(lower, "dance", "dancing", "boogie", "groove")) {
            return applyDance(anim, prompt);
        } else if (contains(lower, "idle", "stand", "rest", "breathe", "breathing")) {
            return applyIdle(anim, prompt);
        } else if (contains(lower, "swim", "swimming", "float")) {
            return applySwimming(anim, prompt);
        } else if (contains(lower, "fly", "flying", "soar", "flap")) {
            return applyFly(anim, prompt);
        } else if (contains(lower, "death", "die", "dying", "fall")) {
            return applyDeath(anim, prompt);
        } else if (contains(lower, "sit", "sitting", "crouch", "crouching")) {
            return applySit(anim, prompt);
        } else if (contains(lower, "emote", "celebrate", "cheer", "victory")) {
            return applyEmote(anim, prompt);
        } else {
            return applyIdle(anim, prompt);
        }
    }

    private static boolean contains(String text, String... keywords) {
        for (String kw : keywords) if (text.contains(kw)) return true;
        return false;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Template definitions
    // ──────────────────────────────────────────────────────────────────────────

    private static AnimationData applyWalking(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Walking");
        a.description = "Bipedal walking cycle";
        a.durationTicks = 20;
        a.looping = true;

        // Right leg forward / back
        AnimationTimeline rightLeg = a.getOrCreateTimeline("right_leg");
        rightLeg.addKeyframe(kf(0,  new BoneTransform(30,0,0, 0,0,0)));
        rightLeg.addKeyframe(kf(5,  new BoneTransform(0,0,0,  0,0,0)));
        rightLeg.addKeyframe(kf(10, new BoneTransform(-30,0,0,0,0,0)));
        rightLeg.addKeyframe(kf(15, new BoneTransform(0,0,0,  0,0,0)));
        rightLeg.addKeyframe(kf(20, new BoneTransform(30,0,0, 0,0,0)));

        // Left leg opposite phase
        AnimationTimeline leftLeg = a.getOrCreateTimeline("left_leg");
        leftLeg.addKeyframe(kf(0,  new BoneTransform(-30,0,0,0,0,0)));
        leftLeg.addKeyframe(kf(5,  new BoneTransform(0,0,0,  0,0,0)));
        leftLeg.addKeyframe(kf(10, new BoneTransform(30,0,0, 0,0,0)));
        leftLeg.addKeyframe(kf(15, new BoneTransform(0,0,0,  0,0,0)));
        leftLeg.addKeyframe(kf(20, new BoneTransform(-30,0,0,0,0,0)));

        // Arms swing opposite to legs
        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(-30,0,0,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(30,0,0, 0,0,0)));
        rightArm.addKeyframe(kf(20, new BoneTransform(-30,0,0,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0,  new BoneTransform(30,0,0, 0,0,0)));
        leftArm.addKeyframe(kf(10, new BoneTransform(-30,0,0,0,0,0)));
        leftArm.addKeyframe(kf(20, new BoneTransform(30,0,0, 0,0,0)));

        // Head bobs slightly
        AnimationTimeline head = a.getOrCreateTimeline("head");
        head.addKeyframe(kf(0,  new BoneTransform(0,0,0, 0,0,0)));
        head.addKeyframe(kf(10, new BoneTransform(0,0,0, 0,0.1f,0)));
        head.addKeyframe(kf(20, new BoneTransform(0,0,0, 0,0,0)));

        addTags(a, "walk", "locomotion", "bipedal");
        return a;
    }

    private static AnimationData applyRunning(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Running");
        a.description = "Fast running sprint cycle";
        a.durationTicks = 12;
        a.looping = true;

        AnimationTimeline rightLeg = a.getOrCreateTimeline("right_leg");
        rightLeg.addKeyframe(kf(0, new BoneTransform(50,0,0,0,0,0)));
        rightLeg.addKeyframe(kf(6, new BoneTransform(-50,0,0,0,0,0)));
        rightLeg.addKeyframe(kf(12,new BoneTransform(50,0,0,0,0,0)));

        AnimationTimeline leftLeg = a.getOrCreateTimeline("left_leg");
        leftLeg.addKeyframe(kf(0, new BoneTransform(-50,0,0,0,0,0)));
        leftLeg.addKeyframe(kf(6, new BoneTransform(50,0,0,0,0,0)));
        leftLeg.addKeyframe(kf(12,new BoneTransform(-50,0,0,0,0,0)));

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0, new BoneTransform(-60,0,20,0,0,0)));
        rightArm.addKeyframe(kf(6, new BoneTransform(60,0,-20,0,0,0)));
        rightArm.addKeyframe(kf(12,new BoneTransform(-60,0,20,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0, new BoneTransform(60,0,-20,0,0,0)));
        leftArm.addKeyframe(kf(6, new BoneTransform(-60,0,20,0,0,0)));
        leftArm.addKeyframe(kf(12,new BoneTransform(60,0,-20,0,0,0)));

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0, new BoneTransform(-10,0,0,0,0,0)));
        body.addKeyframe(kf(6, new BoneTransform(-10,0,0,0,0.1f,0)));
        body.addKeyframe(kf(12,new BoneTransform(-10,0,0,0,0,0)));

        addTags(a, "run", "sprint", "locomotion");
        return a;
    }

    private static AnimationData applyWave(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Waving");
        a.description = "Friendly hand wave emote";
        a.durationTicks = 30;
        a.looping = true;

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(-90,0,30,0,0,0)));
        rightArm.addKeyframe(kf(5,  new BoneTransform(-90,0,50,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(-90,0,20,0,0,0)));
        rightArm.addKeyframe(kf(15, new BoneTransform(-90,0,50,0,0,0)));
        rightArm.addKeyframe(kf(20, new BoneTransform(-90,0,20,0,0,0)));
        rightArm.addKeyframe(kf(25, new BoneTransform(-90,0,40,0,0,0)));
        rightArm.addKeyframe(kf(30, new BoneTransform(-90,0,30,0,0,0)));

        AnimationTimeline head = a.getOrCreateTimeline("head");
        head.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        head.addKeyframe(kf(15, new BoneTransform(5,15,0,0,0,0)));
        head.addKeyframe(kf(30, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "wave", "emote", "greeting");
        return a;
    }

    private static AnimationData applyJump(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Jump");
        a.description = "Jump and land cycle";
        a.durationTicks = 20;
        a.looping = false;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(3,  new BoneTransform(10,0,0,0,-0.3f,0)));
        body.addKeyframe(kf(7,  new BoneTransform(-5,0,0,0,0.5f,0)));
        body.addKeyframe(kf(13, new BoneTransform(-5,0,0,0,0.5f,0)));
        body.addKeyframe(kf(17, new BoneTransform(10,0,0,0,0,0)));
        body.addKeyframe(kf(20, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline legs = a.getOrCreateTimeline("right_leg");
        legs.addKeyframe(kf(3,  new BoneTransform(-10,0,0,0,0,0)));
        legs.addKeyframe(kf(7,  new BoneTransform(20,0,0,0,0,0)));
        legs.addKeyframe(kf(13, new BoneTransform(20,0,0,0,0,0)));
        legs.addKeyframe(kf(17, new BoneTransform(-10,0,0,0,0,0)));
        legs.addKeyframe(kf(20, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline arms = a.getOrCreateTimeline("right_arm");
        arms.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        arms.addKeyframe(kf(7,  new BoneTransform(-150,0,0,0,0,0)));
        arms.addKeyframe(kf(13, new BoneTransform(-150,0,0,0,0,0)));
        arms.addKeyframe(kf(20, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "jump", "leap", "action");
        return a;
    }

    private static AnimationData applyAttack(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Attack");
        a.description = "Melee attack swing";
        a.durationTicks = 15;
        a.looping = false;

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        rightArm.addKeyframe(kf(3,  new BoneTransform(-120,0,-30,0,0,0)));
        rightArm.addKeyframe(kf(7,  new BoneTransform(30,0,30,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(0,0,0,0,0,0)));
        rightArm.addKeyframe(kf(15, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(3,  new BoneTransform(0,-30,0,0,0,0)));
        body.addKeyframe(kf(7,  new BoneTransform(0,30,0,0,0,0)));
        body.addKeyframe(kf(15, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "attack", "combat", "melee");
        return a;
    }

    private static AnimationData applyDance(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Dance");
        a.description = "Funky dance emote";
        a.durationTicks = 40;
        a.looping = true;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(5,  new BoneTransform(0,20,5,0,0.1f,0)));
        body.addKeyframe(kf(10, new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(15, new BoneTransform(0,-20,-5,0,0.1f,0)));
        body.addKeyframe(kf(20, new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(25, new BoneTransform(0,20,5,0,0.1f,0)));
        body.addKeyframe(kf(30, new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(35, new BoneTransform(0,-20,-5,0,0.1f,0)));
        body.addKeyframe(kf(40, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(-60,0,30,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(-90,30,60,0,0,0)));
        rightArm.addKeyframe(kf(20, new BoneTransform(-60,0,30,0,0,0)));
        rightArm.addKeyframe(kf(30, new BoneTransform(-90,-30,0,0,0,0)));
        rightArm.addKeyframe(kf(40, new BoneTransform(-60,0,30,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0,  new BoneTransform(-60,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(10, new BoneTransform(-90,-30,-60,0,0,0)));
        leftArm.addKeyframe(kf(20, new BoneTransform(-60,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(30, new BoneTransform(-90,30,0,0,0,0)));
        leftArm.addKeyframe(kf(40, new BoneTransform(-60,0,-30,0,0,0)));

        addTags(a, "dance", "emote", "fun");
        return a;
    }

    private static AnimationData applyIdle(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Idle");
        a.description = "Peaceful breathing idle";
        a.durationTicks = 60;
        a.looping = true;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(30, new BoneTransform(2,0,0,0,0.05f,0)));
        body.addKeyframe(kf(60, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline head = a.getOrCreateTimeline("head");
        head.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        head.addKeyframe(kf(20, new BoneTransform(3,5,0,0,0,0)));
        head.addKeyframe(kf(40, new BoneTransform(0,0,0,0,0,0)));
        head.addKeyframe(kf(60, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "idle", "breathing", "passive");
        return a;
    }

    private static AnimationData applySwimming(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Swimming");
        a.description = "Smooth swimming stroke";
        a.durationTicks = 20;
        a.looping = true;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(-80,0,0,0,0,0)));
        body.addKeyframe(kf(10, new BoneTransform(-80,0,0,0,0.2f,0)));
        body.addKeyframe(kf(20, new BoneTransform(-80,0,0,0,0,0)));

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(-180,0,0,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(-90,0,30,0,0,0)));
        rightArm.addKeyframe(kf(20, new BoneTransform(-180,0,0,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0,  new BoneTransform(-90,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(10, new BoneTransform(-180,0,0,0,0,0)));
        leftArm.addKeyframe(kf(20, new BoneTransform(-90,0,-30,0,0,0)));

        addTags(a, "swim", "water", "locomotion");
        return a;
    }

    private static AnimationData applyFly(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Flying");
        a.description = "Wing flap / gliding animation";
        a.durationTicks = 16;
        a.looping = true;

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0, new BoneTransform(0,0,90,0,0,0)));
        rightArm.addKeyframe(kf(4, new BoneTransform(-30,0,60,0,0,0)));
        rightArm.addKeyframe(kf(8, new BoneTransform(0,0,90,0,0,0)));
        rightArm.addKeyframe(kf(12,new BoneTransform(20,0,110,0,0,0)));
        rightArm.addKeyframe(kf(16,new BoneTransform(0,0,90,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0, new BoneTransform(0,0,-90,0,0,0)));
        leftArm.addKeyframe(kf(4, new BoneTransform(-30,0,-60,0,0,0)));
        leftArm.addKeyframe(kf(8, new BoneTransform(0,0,-90,0,0,0)));
        leftArm.addKeyframe(kf(12,new BoneTransform(20,0,-110,0,0,0)));
        leftArm.addKeyframe(kf(16,new BoneTransform(0,0,-90,0,0,0)));

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0, new BoneTransform(-20,0,0,0,0,0)));
        body.addKeyframe(kf(8, new BoneTransform(-20,0,0,0,0.1f,0)));
        body.addKeyframe(kf(16,new BoneTransform(-20,0,0,0,0,0)));

        addTags(a, "fly", "aerial", "locomotion");
        return a;
    }

    private static AnimationData applyDeath(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Death");
        a.description = "Death fall animation";
        a.durationTicks = 20;
        a.looping = false;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0), "ease_in"));
        body.addKeyframe(kf(10, new BoneTransform(45,0,0,0,0,0)));
        body.addKeyframe(kf(20, new BoneTransform(90,0,0,0,-0.9f,0)));

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        rightArm.addKeyframe(kf(15, new BoneTransform(-30,0,70,0,0,0)));
        rightArm.addKeyframe(kf(20, new BoneTransform(-30,0,90,0,0,0)));

        addTags(a, "death", "fall", "action");
        return a;
    }

    private static AnimationData applySit(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Sitting");
        a.description = "Seated resting pose";
        a.durationTicks = 40;
        a.looping = true;

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0, new BoneTransform(0,0,0,0,-0.5f,0)));

        AnimationTimeline rightLeg = a.getOrCreateTimeline("right_leg");
        rightLeg.addKeyframe(kf(0, new BoneTransform(-90,0,0,0,0,0)));

        AnimationTimeline leftLeg = a.getOrCreateTimeline("left_leg");
        leftLeg.addKeyframe(kf(0, new BoneTransform(-90,0,0,0,0,0)));

        AnimationTimeline head = a.getOrCreateTimeline("head");
        head.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        head.addKeyframe(kf(20, new BoneTransform(5,10,0,0,0,0)));
        head.addKeyframe(kf(40, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "sit", "rest", "emote");
        return a;
    }

    private static AnimationData applyEmote(AnimationData a, String prompt) {
        a.name = deriveName(prompt, "Victory");
        a.description = "Celebration victory emote";
        a.durationTicks = 30;
        a.looping = false;

        AnimationTimeline rightArm = a.getOrCreateTimeline("right_arm");
        rightArm.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        rightArm.addKeyframe(kf(5,  new BoneTransform(-160,0,30,0,0,0)));
        rightArm.addKeyframe(kf(10, new BoneTransform(-140,0,20,0,0,0)));
        rightArm.addKeyframe(kf(15, new BoneTransform(-160,0,30,0,0,0)));
        rightArm.addKeyframe(kf(25, new BoneTransform(-160,0,30,0,0,0)));
        rightArm.addKeyframe(kf(30, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline leftArm = a.getOrCreateTimeline("left_arm");
        leftArm.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        leftArm.addKeyframe(kf(5,  new BoneTransform(-160,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(10, new BoneTransform(-140,0,-20,0,0,0)));
        leftArm.addKeyframe(kf(15, new BoneTransform(-160,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(25, new BoneTransform(-160,0,-30,0,0,0)));
        leftArm.addKeyframe(kf(30, new BoneTransform(0,0,0,0,0,0)));

        AnimationTimeline body = a.getOrCreateTimeline("body");
        body.addKeyframe(kf(0,  new BoneTransform(0,0,0,0,0,0)));
        body.addKeyframe(kf(5,  new BoneTransform(-10,0,0,0,0.2f,0)));
        body.addKeyframe(kf(25, new BoneTransform(-10,0,0,0,0.2f,0)));
        body.addKeyframe(kf(30, new BoneTransform(0,0,0,0,0,0)));

        addTags(a, "celebrate", "victory", "emote");
        return a;
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Helpers
    // ──────────────────────────────────────────────────────────────────────────

    private static AnimationKeyframe kf(float tick, BoneTransform t) {
        return new AnimationKeyframe(tick, t, "ease_in_out");
    }

    private static AnimationKeyframe kf(float tick, BoneTransform t, String easing) {
        return new AnimationKeyframe(tick, t, easing);
    }

    private static String deriveName(String prompt, String fallback) {
        if (prompt == null || prompt.isBlank()) return fallback;
        String cleaned = prompt.trim();
        if (cleaned.length() > 32) cleaned = cleaned.substring(0, 32) + "…";
        return cleaned.substring(0, 1).toUpperCase() + cleaned.substring(1);
    }

    private static void addTags(AnimationData a, String... tags) {
        for (String tag : tags) a.tags.add(tag);
    }
}
