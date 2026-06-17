package com.animationcreator.network;

import com.animationcreator.animation.*;
import com.google.gson.*;
import net.fabricmc.fabric.api.networking.v1.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public class NetworkHandler {

    // ── Payload: client → server: play animation on a target ────────────────

    public record PlayAnimationC2S(String animationId, String targetUuid)
            implements CustomPayload {
        public static final CustomPayload.Id<PlayAnimationC2S> ID =
                new CustomPayload.Id<>(Identifier.of("animationcreator", "play_c2s"));
        public static final PacketCodec<PacketByteBuf, PlayAnimationC2S> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, PlayAnimationC2S::animationId,
                        PacketCodecs.STRING, PlayAnimationC2S::targetUuid,
                        PlayAnimationC2S::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ── Payload: server → client: broadcast animation state ─────────────────

    public record AnimationSyncS2C(String targetUuid, String animationJson)
            implements CustomPayload {
        public static final CustomPayload.Id<AnimationSyncS2C> ID =
                new CustomPayload.Id<>(Identifier.of("animationcreator", "sync_s2c"));
        public static final PacketCodec<PacketByteBuf, AnimationSyncS2C> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, AnimationSyncS2C::targetUuid,
                        PacketCodecs.STRING, AnimationSyncS2C::animationJson,
                        AnimationSyncS2C::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ── Payload: server → client: full library sync ──────────────────────────

    public record LibrarySyncS2C(String libraryJson) implements CustomPayload {
        public static final CustomPayload.Id<LibrarySyncS2C> ID =
                new CustomPayload.Id<>(Identifier.of("animationcreator", "library_s2c"));
        public static final PacketCodec<PacketByteBuf, LibrarySyncS2C> CODEC =
                PacketCodec.tuple(
                        PacketCodecs.STRING, LibrarySyncS2C::libraryJson,
                        LibrarySyncS2C::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ── Registration (server-side) ───────────────────────────────────────────

    public static void registerServer() {
        PayloadTypeRegistry.playC2S().register(PlayAnimationC2S.ID, PlayAnimationC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(AnimationSyncS2C.ID, AnimationSyncS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(LibrarySyncS2C.ID, LibrarySyncS2C.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PlayAnimationC2S.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            context.server().execute(() -> {
                AnimationManager mgr = AnimationManager.getInstance();
                mgr.playAnimation(payload.targetUuid(), payload.animationId());

                // Broadcast to all players in the same world
                AnimationData data = mgr.getAnimation(payload.animationId());
                if (data != null) {
                    String json = data.toJson().toString();
                    AnimationSyncS2C sync = new AnimationSyncS2C(payload.targetUuid(), json);
                    player.getServerWorld().getPlayers().forEach(p ->
                            ServerPlayNetworking.send(p, sync));
                }
            });
        });
    }

    // ── Registration (client-side, called from AnimationCreatorClient) ───────

    public static void registerClient() {
        ClientPlayNetworking.registerGlobalReceiver(AnimationSyncS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    JsonObject obj = JsonParser.parseString(payload.animationJson()).getAsJsonObject();
                    AnimationData data = AnimationData.fromJson(obj);
                    AnimationManager.getInstance().addAnimation(data);
                    AnimationManager.getInstance().playAnimation(payload.targetUuid(), data.id);
                } catch (Exception ignored) {}
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(LibrarySyncS2C.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    JsonArray arr = JsonParser.parseString(payload.libraryJson()).getAsJsonArray();
                    for (JsonElement el : arr) {
                        AnimationData data = AnimationData.fromJson(el.getAsJsonObject());
                        AnimationManager.getInstance().addAnimation(data);
                    }
                } catch (Exception ignored) {}
            });
        });
    }

    // ── Utilities ────────────────────────────────────────────────────────────

    public static void sendLibrary(ServerPlayerEntity player) {
        List<AnimationData> all = new java.util.ArrayList<>(
                AnimationManager.getInstance().getAllAnimations());
        JsonArray arr = new JsonArray();
        for (AnimationData a : all) arr.add(a.toJson());
        ServerPlayNetworking.send(player, new LibrarySyncS2C(arr.toString()));
    }
}
