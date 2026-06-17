package com.animationcreator.ai;

import com.animationcreator.animation.AnimationData;
import com.animationcreator.animation.AnimationTemplates;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Generates animation data from text prompts.
 *
 * Strategy (in order):
 * 1. Try the local API server (which calls Pollinations.ai – 100% free, no key).
 * 2. Fall back to the offline template engine built into AnimationTemplates.
 *
 * Both paths require zero API keys or paid accounts.
 */
public class AIAnimationService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    /** API server URL — routed through the shared proxy. */
    private static final String API_URL = "http://localhost/api/animation/generate";

    // ── Public API ──────────────────────────────────────────────────────────

    /**
     * Asynchronously generate an animation. Never blocks the game thread.
     * @param prompt  Natural-language description (e.g. "zombie walking animation")
     * @param onDone  Called on the main thread with the finished AnimationData
     */
    public static void generateAsync(String prompt, Consumer<AnimationData> onDone, Consumer<String> onError) {
        CompletableFuture.supplyAsync(() -> generate(prompt))
            .whenComplete((result, ex) -> {
                MinecraftClient mc = MinecraftClient.getInstance();
                if (mc == null) return;
                if (ex != null) {
                    mc.execute(() -> onError.accept("Generation failed: " + ex.getMessage()));
                } else {
                    mc.execute(() -> onDone.accept(result));
                }
            });
    }

    // ── Internal ────────────────────────────────────────────────────────────

    private static AnimationData generate(String prompt) {
        try {
            return tryApiServer(prompt);
        } catch (Exception e) {
            return AnimationTemplates.generate(prompt);
        }
    }

    private static AnimationData tryApiServer(String prompt) throws Exception {
        JsonObject body = new JsonObject();
        body.addProperty("prompt", prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("API returned " + response.statusCode());
        }

        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();

        // If the server returned a full AnimationData blob, deserialise it
        if (json.has("animation")) {
            return AnimationData.fromJson(json.getAsJsonObject("animation"));
        }

        // Fallback: use template with any name hint from server
        String name = json.has("name") ? json.get("name").getAsString() : prompt;
        AnimationData data = AnimationTemplates.generate(prompt);
        data.name = name;
        return data;
    }
}
