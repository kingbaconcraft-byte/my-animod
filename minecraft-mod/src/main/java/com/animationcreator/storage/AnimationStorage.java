package com.animationcreator.storage;

import com.animationcreator.animation.AnimationData;
import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Persists animations as JSON files in <game-dir>/animation_creator/.
 * Works across all worlds because the folder is at the game root, not per-world.
 */
public class AnimationStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger("AnimationCreator/Storage");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path getStorageDir() {
        Path dir = FabricLoader.getInstance().getGameDir().resolve("animation_creator");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            LOGGER.error("Failed to create storage dir", e);
        }
        return dir;
    }

    /** Save a single animation to disk. */
    public static void save(AnimationData data) {
        Path file = getStorageDir().resolve(data.id + ".json");
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(data.toJson(), w);
        } catch (IOException e) {
            LOGGER.error("Failed to save animation {}", data.id, e);
        }
    }

    /** Load all animations from disk. */
    public static List<AnimationData> loadAll() {
        List<AnimationData> list = new ArrayList<>();
        Path dir = getStorageDir();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.json")) {
            for (Path path : stream) {
                try (Reader r = Files.newBufferedReader(path)) {
                    JsonObject obj = GSON.fromJson(r, JsonObject.class);
                    list.add(AnimationData.fromJson(obj));
                } catch (Exception e) {
                    LOGGER.warn("Skipping corrupt file {}: {}", path.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list storage dir", e);
        }
        return list;
    }

    /** Delete an animation file. */
    public static void delete(String id) {
        Path file = getStorageDir().resolve(id + ".json");
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOGGER.error("Failed to delete animation {}", id, e);
        }
    }

    /** Export a single animation to an arbitrary path. */
    public static void export(AnimationData data, Path destination) throws IOException {
        try (Writer w = Files.newBufferedWriter(destination)) {
            GSON.toJson(data.toJson(), w);
        }
    }

    /** Import an animation from a JSON file. Returns null on parse error. */
    public static AnimationData importFrom(Path source) {
        try (Reader r = Files.newBufferedReader(source)) {
            JsonObject obj = GSON.fromJson(r, JsonObject.class);
            return AnimationData.fromJson(obj);
        } catch (Exception e) {
            LOGGER.error("Failed to import {}: {}", source, e.getMessage());
            return null;
        }
    }
}
