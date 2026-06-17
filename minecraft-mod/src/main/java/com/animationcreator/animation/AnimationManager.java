package com.animationcreator.animation;

import java.util.*;

public class AnimationManager {
    private static final AnimationManager INSTANCE = new AnimationManager();
    public static AnimationManager getInstance() { return INSTANCE; }

    private final Map<String, AnimationData> library = new LinkedHashMap<>();
    // entityUuid -> (animationId, startTick)
    private final Map<String, AnimationState> activeAnimations = new HashMap<>();

    public record AnimationState(String animationId, long startTime, boolean looping) {}

    // ── Library management ──────────────────────────────────────────────────

    public void addAnimation(AnimationData data) {
        library.put(data.id, data);
    }

    public void removeAnimation(String id) {
        library.remove(id);
    }

    public AnimationData getAnimation(String id) {
        return library.get(id);
    }

    public Collection<AnimationData> getAllAnimations() {
        return Collections.unmodifiableCollection(library.values());
    }

    public List<AnimationData> searchAnimations(String query) {
        if (query == null || query.isBlank()) return new ArrayList<>(library.values());
        String q = query.toLowerCase(Locale.ROOT);
        return library.values().stream()
            .filter(a -> (a.name != null && a.name.toLowerCase().contains(q))
                      || (a.description != null && a.description.toLowerCase().contains(q))
                      || a.tags.stream().anyMatch(t -> t.toLowerCase().contains(q)))
            .toList();
    }

    public void clearAll() {
        library.clear();
    }

    public void loadAll(List<AnimationData> animations) {
        for (AnimationData a : animations) library.put(a.id, a);
    }

    // ── Active-animation tracking ───────────────────────────────────────────

    public void playAnimation(String entityUuid, String animationId) {
        AnimationData data = library.get(animationId);
        if (data == null) return;
        activeAnimations.put(entityUuid, new AnimationState(animationId, System.currentTimeMillis(), data.looping));
    }

    public void stopAnimation(String entityUuid) {
        activeAnimations.remove(entityUuid);
    }

    public AnimationState getActiveState(String entityUuid) {
        return activeAnimations.get(entityUuid);
    }

    /** Returns the current transforms for all bones, given the current time. */
    public Map<String, BoneTransform> sampleAnimation(String entityUuid) {
        AnimationState state = activeAnimations.get(entityUuid);
        if (state == null) return Map.of();
        AnimationData data = library.get(state.animationId());
        if (data == null) { activeAnimations.remove(entityUuid); return Map.of(); }

        float elapsed = (System.currentTimeMillis() - state.startTime()) / 50f; // ticks

        if (!state.looping() && elapsed > data.durationTicks) {
            activeAnimations.remove(entityUuid);
            return Map.of();
        }

        Map<String, BoneTransform> result = new HashMap<>();
        for (AnimationTimeline tl : data.timelines) {
            result.put(tl.boneName, tl.sample(elapsed, data.looping, data.durationTicks));
        }
        return result;
    }

    public int size() { return library.size(); }
}
