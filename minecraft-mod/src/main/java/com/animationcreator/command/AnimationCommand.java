package com.animationcreator.command;

import com.animationcreator.animation.*;
import com.animationcreator.network.NetworkHandler;
import com.animationcreator.storage.AnimationStorage;
import com.animationcreator.world.CityHubFeature;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.*;

public class AnimationCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("animation")
                // /animation list
                .then(literal("list").executes(ctx -> {
                    ServerCommandSource src = ctx.getSource();
                    AnimationManager mgr = AnimationManager.getInstance();
                    if (mgr.size() == 0) {
                        src.sendMessage(Text.literal("No animations loaded.").formatted(Formatting.YELLOW));
                    } else {
                        src.sendMessage(Text.literal("=== Animation Library (" + mgr.size() + ") ===")
                                .formatted(Formatting.GOLD));
                        for (AnimationData a : mgr.getAllAnimations()) {
                            src.sendMessage(Text.literal("  [" + a.id.substring(0,8) + "] " + a.name
                                    + " (" + a.durationTicks + "t" + (a.looping ? ", loop" : "") + ")")
                                    .formatted(Formatting.WHITE));
                        }
                    }
                    return mgr.size();
                }))

                // /animation play <animationId> [playerName]
                .then(literal("play")
                    .then(argument("animationId", StringArgumentType.word())
                        .executes(ctx -> {
                            String id = StringArgumentType.getString(ctx, "animationId");
                            ServerCommandSource src = ctx.getSource();
                            ServerPlayerEntity player = src.getPlayer();
                            if (player == null) return 0;
                            AnimationData anim = AnimationManager.getInstance().getAnimation(id);
                            if (anim == null) {
                                src.sendError(Text.literal("Animation not found: " + id));
                                return 0;
                            }
                            AnimationManager.getInstance().playAnimation(player.getUuidAsString(), id);
                            NetworkHandler.sendLibrary(player);
                            src.sendMessage(Text.literal("Playing: " + anim.name).formatted(Formatting.GREEN));
                            return 1;
                        })
                        .then(argument("target", StringArgumentType.word())
                            .executes(ctx -> {
                                String id = StringArgumentType.getString(ctx, "animationId");
                                String target = StringArgumentType.getString(ctx, "target");
                                ServerCommandSource src = ctx.getSource();
                                ServerPlayerEntity targetPlayer = src.getServer().getPlayerManager().getPlayer(target);
                                if (targetPlayer == null) {
                                    src.sendError(Text.literal("Player not found: " + target));
                                    return 0;
                                }
                                AnimationData anim = AnimationManager.getInstance().getAnimation(id);
                                if (anim == null) {
                                    src.sendError(Text.literal("Animation not found: " + id));
                                    return 0;
                                }
                                AnimationManager.getInstance().playAnimation(targetPlayer.getUuidAsString(), id);
                                src.sendMessage(Text.literal("Playing " + anim.name + " on " + target)
                                        .formatted(Formatting.GREEN));
                                return 1;
                            }))))

                // /animation stop [playerName]
                .then(literal("stop").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player != null) {
                        AnimationManager.getInstance().stopAnimation(player.getUuidAsString());
                        ctx.getSource().sendMessage(Text.literal("Animation stopped.").formatted(Formatting.YELLOW));
                    }
                    return 1;
                }))

                // /animation reload  – reload library from disk
                .then(literal("reload").executes(ctx -> {
                    AnimationManager mgr = AnimationManager.getInstance();
                    mgr.clearAll();
                    java.util.List<AnimationData> loaded = AnimationStorage.loadAll();
                    mgr.loadAll(loaded);
                    ctx.getSource().sendMessage(
                            Text.literal("Reloaded " + loaded.size() + " animations from disk.")
                                    .formatted(Formatting.GREEN));
                    return loaded.size();
                }))

                // /animation hub  – generate the City Hub near the player
                .then(literal("hub").executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayer();
                    if (player == null) return 0;
                    ctx.getSource().sendMessage(
                            Text.literal("Generating Animation City Hub...").formatted(Formatting.AQUA));
                    CityHubFeature.generate(player.getServerWorld(),
                            player.getBlockPos().add(0, 0, 10));
                    ctx.getSource().sendMessage(
                            Text.literal("City Hub generated! Walk forward ~10 blocks.")
                                    .formatted(Formatting.GREEN));
                    return 1;
                }))

                // /animation info <animationId>
                .then(literal("info")
                    .then(argument("animationId", StringArgumentType.word()).executes(ctx -> {
                        String id = StringArgumentType.getString(ctx, "animationId");
                        AnimationData anim = AnimationManager.getInstance().getAnimation(id);
                        if (anim == null) {
                            ctx.getSource().sendError(Text.literal("Not found: " + id));
                            return 0;
                        }
                        ctx.getSource().sendMessage(
                                Text.literal("=== " + anim.name + " ===").formatted(Formatting.GOLD));
                        ctx.getSource().sendMessage(
                                Text.literal("ID: " + anim.id).formatted(Formatting.GRAY));
                        ctx.getSource().sendMessage(
                                Text.literal("Duration: " + anim.durationTicks + " ticks ("
                                        + anim.getDurationSeconds() + "s)").formatted(Formatting.WHITE));
                        ctx.getSource().sendMessage(
                                Text.literal("Loop: " + anim.loopMode).formatted(Formatting.WHITE));
                        ctx.getSource().sendMessage(
                                Text.literal("Bones: " + anim.timelines.size()).formatted(Formatting.WHITE));
                        ctx.getSource().sendMessage(
                                Text.literal("Tags: " + String.join(", ", anim.tags)).formatted(Formatting.AQUA));
                        return 1;
                    })))
            )
        );
    }
}
