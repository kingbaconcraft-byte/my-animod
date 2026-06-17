package com.animationcreator.client.gui;

import com.animationcreator.animation.*;
import com.animationcreator.storage.AnimationStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.*;

@Environment(EnvType.CLIENT)
public class AnimationEditorScreen extends Screen {

    private final Screen parent;
    private AnimationData animation;

    // UI state
    private int selectedBoneIdx   = 0;
    private int selectedKfIdx     = 0;
    private float playheadTick    = 0f;
    private boolean playing        = false;
    private long lastTickTime      = 0;
    private String statusMsg       = "";

    // Widgets
    private TextFieldWidget nameField;
    private TextFieldWidget durationField;
    private ButtonWidget playBtn, stopBtn, loopBtn, saveBtn;

    private static final int PANEL_W  = 420;
    private static final int PANEL_H  = 300;
    private static final int TL_X_OFF = 10;
    private static final int TL_Y_OFF = 110;
    private static final int TL_W     = PANEL_W - 20;
    private static final int TL_H     = 60;

    public AnimationEditorScreen(Screen parent, AnimationData animation) {
        super(Text.translatable("screen.animationcreator.editor"));
        this.parent = parent;
        this.animation = animation != null ? animation : new AnimationData("New Animation", "");
    }

    @Override
    protected void init() {
        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;

        // Name field
        nameField = new TextFieldWidget(textRenderer, px + 60, py + 12, 180, 16,
                Text.literal("Name"));
        nameField.setText(animation.name != null ? animation.name : "");
        nameField.setMaxLength(64);
        addDrawableChild(nameField);

        // Duration field
        durationField = new TextFieldWidget(textRenderer, px + PANEL_W - 80, py + 12, 60, 16,
                Text.literal("Duration"));
        durationField.setText(String.valueOf((int) animation.durationTicks));
        durationField.setMaxLength(6);
        addDrawableChild(durationField);

        // Bone navigation
        addDrawableChild(ButtonWidget.builder(Text.literal("◀"), btn -> prevBone())
                .dimensions(px + 10, py + 34, 20, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▶"), btn -> nextBone())
                .dimensions(px + 95, py + 34, 20, 16).build());

        // Keyframe controls
        addDrawableChild(ButtonWidget.builder(Text.literal("+ KF"), btn -> addKeyframe())
                .dimensions(px + 130, py + 34, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("- KF"), btn -> removeKeyframe())
                .dimensions(px + 185, py + 34, 50, 16).build());

        // Easing selector
        addDrawableChild(ButtonWidget.builder(Text.literal("Easing"), btn -> cycleEasing())
                .dimensions(px + 245, py + 34, 60, 16).build());

        // Loop toggle
        loopBtn = ButtonWidget.builder(
                Text.literal("Loop: " + animation.loopMode), btn -> cycleLoop())
                .dimensions(px + 315, py + 34, 95, 16).build();
        addDrawableChild(loopBtn);

        // Playback
        playBtn = ButtonWidget.builder(Text.literal("▶ Play"), btn -> startPlay())
                .dimensions(px + 10, py + 56, 70, 18).build();
        stopBtn = ButtonWidget.builder(Text.literal("■ Stop"), btn -> stopPlay())
                .dimensions(px + 85, py + 56, 70, 18).build();
        saveBtn = ButtonWidget.builder(Text.literal("💾 Save"), btn -> save())
                .dimensions(px + PANEL_W - 90, py + 56, 80, 18).build();
        addDrawableChild(playBtn);
        addDrawableChild(stopBtn);
        addDrawableChild(saveBtn);

        // Transform edit buttons (for selected keyframe)
        int kfy = py + TL_Y_OFF + TL_H + 10;
        addDrawableChild(ButtonWidget.builder(Text.literal("RotX+"), btn -> adjustKf("rotX", 5))
                .dimensions(px + 10, kfy, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("RotX-"), btn -> adjustKf("rotX", -5))
                .dimensions(px + 65, kfy, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("RotY+"), btn -> adjustKf("rotY", 5))
                .dimensions(px + 125, kfy, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("RotY-"), btn -> adjustKf("rotY", -5))
                .dimensions(px + 180, kfy, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("RotZ+"), btn -> adjustKf("rotZ", 5))
                .dimensions(px + 240, kfy, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("RotZ-"), btn -> adjustKf("rotZ", -5))
                .dimensions(px + 295, kfy, 50, 16).build());

        int kfy2 = kfy + 20;
        addDrawableChild(ButtonWidget.builder(Text.literal("PosX+"), btn -> adjustKf("posX", 0.1f))
                .dimensions(px + 10, kfy2, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("PosX-"), btn -> adjustKf("posX", -0.1f))
                .dimensions(px + 65, kfy2, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("PosY+"), btn -> adjustKf("posY", 0.1f))
                .dimensions(px + 125, kfy2, 50, 16).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("PosY-"), btn -> adjustKf("posY", -0.1f))
                .dimensions(px + 180, kfy2, 50, 16).build());

        // Back button
        addDrawableChild(ButtonWidget.builder(Text.literal("← Back"), btn -> close())
                .dimensions(px + 10, py + PANEL_H - 24, 80, 18).build());

        // Duplicate
        addDrawableChild(ButtonWidget.builder(Text.literal("⎘ Duplicate"), btn -> duplicate())
                .dimensions(px + 100, py + PANEL_H - 24, 100, 18).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;

        // Background panel
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, 0xE0101820);
        ctx.fill(px, py, px + PANEL_W, py + 2, 0xFF33AAFF);
        ctx.drawBorder(px, py, PANEL_W, PANEL_H, 0xFF334466);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("✏ Animation Editor").formatted(Formatting.AQUA),
                cx, py - 10, 0xFFFFFF);

        // Name label
        ctx.drawText(textRenderer, Text.literal("Name:").formatted(Formatting.GRAY),
                px + 10, py + 16, 0xAAAAAA, false);
        ctx.drawText(textRenderer, Text.literal("Dur:").formatted(Formatting.GRAY),
                px + PANEL_W - 95, py + 16, 0xAAAAAA, false);

        // Current bone label
        String boneName = getBoneName();
        ctx.drawText(textRenderer,
                Text.literal("Bone: " + boneName + "  (" + getBoneIndex() + "/" + animation.timelines.size() + ")")
                        .formatted(Formatting.YELLOW),
                px + 33, py + 38, 0xFFFF55, false);

        // Playhead position
        ctx.drawText(textRenderer,
                Text.literal(String.format("%.1f / %.1f ticks", playheadTick, animation.durationTicks))
                        .formatted(Formatting.GRAY),
                px + 200, py + 60, 0xAAAAAA, false);

        // Timeline visualisation
        drawTimeline(ctx, px + TL_X_OFF, py + TL_Y_OFF, TL_W, TL_H);

        // Selected KF info
        AnimationKeyframe kf = getSelectedKeyframe();
        if (kf != null) {
            int ix = px + 10, iy = py + TL_Y_OFF + TL_H + 8;
            ctx.drawText(textRenderer,
                    Text.literal(String.format("KF@%.1ft  R(%.0f,%.0f,%.0f)  P(%.2f,%.2f,%.2f)  [%s]",
                            kf.tick, kf.transform.rotX, kf.transform.rotY, kf.transform.rotZ,
                            kf.transform.posX, kf.transform.posY, kf.transform.posZ, kf.easing))
                            .formatted(Formatting.GRAY),
                    ix, iy, 0x999999, false);
        }

        // Status
        if (!statusMsg.isEmpty()) {
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(statusMsg), cx, py + PANEL_H - 38, 0x55FF55);
        }

        // Advance playhead
        if (playing) {
            long now = System.currentTimeMillis();
            if (lastTickTime != 0) {
                float dtTicks = (now - lastTickTime) / 50f;
                playheadTick += dtTicks;
                if (playheadTick > animation.durationTicks) {
                    playheadTick = animation.looping ? playheadTick % animation.durationTicks : animation.durationTicks;
                    if (!animation.looping) playing = false;
                }
            }
            lastTickTime = now;
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawTimeline(DrawContext ctx, int x, int y, int w, int h) {
        // Background
        ctx.fill(x, y, x + w, y + h, 0x80000000);
        ctx.drawBorder(x, y, w, h, 0xFF445566);

        if (animation.durationTicks <= 0) return;

        // Grid lines (every 5 ticks)
        for (int t = 0; t <= (int) animation.durationTicks; t += 5) {
            int gx = x + (int) (t / animation.durationTicks * w);
            ctx.fill(gx, y, gx + 1, y + h, 0x40FFFFFF);
            ctx.drawText(textRenderer, Text.literal(String.valueOf(t)), gx + 2, y + 2, 0x555555, false);
        }

        // Keyframes for current bone
        AnimationTimeline tl = getCurrentTimeline();
        if (tl != null) {
            for (int i = 0; i < tl.keyframes.size(); i++) {
                AnimationKeyframe kf = tl.keyframes.get(i);
                int kx = x + (int) (kf.tick / animation.durationTicks * w);
                boolean sel = i == selectedKfIdx;
                ctx.fill(kx - 3, y + 5, kx + 3, y + h - 5, sel ? 0xFFFFAA00 : 0xFFAAAAAA);
            }
        }

        // Playhead
        int phx = x + (int) (playheadTick / animation.durationTicks * w);
        ctx.fill(phx, y, phx + 1, y + h, 0xFFFF4444);
    }

    // ── Timeline click handler ───────────────────────────────────────────────

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;
        int tx = px + TL_X_OFF, ty = py + TL_Y_OFF;

        if (mx >= tx && mx <= tx + TL_W && my >= ty && my <= ty + TL_H && animation.durationTicks > 0) {
            float tick = (float) ((mx - tx) / TL_W) * animation.durationTicks;
            playheadTick = Math.max(0, Math.min(tick, animation.durationTicks));

            // Select nearest keyframe
            AnimationTimeline tl = getCurrentTimeline();
            if (tl != null) {
                for (int i = 0; i < tl.keyframes.size(); i++) {
                    if (Math.abs(tl.keyframes.get(i).tick - playheadTick) < 2f) {
                        selectedKfIdx = i;
                        break;
                    }
                }
            }
            return true;
        }
        return super.mouseClicked(mx, my, btn);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void addKeyframe() {
        AnimationTimeline tl = getOrCreateTimeline();
        BoneTransform t = new BoneTransform();
        tl.addKeyframe(new AnimationKeyframe(playheadTick, t, "ease_in_out"));
        selectedKfIdx = tl.keyframes.size() - 1;
    }

    private void removeKeyframe() {
        AnimationTimeline tl = getCurrentTimeline();
        if (tl != null && selectedKfIdx >= 0 && selectedKfIdx < tl.keyframes.size()) {
            tl.keyframes.remove(selectedKfIdx);
            selectedKfIdx = Math.max(0, selectedKfIdx - 1);
        }
    }

    private void adjustKf(String axis, float delta) {
        AnimationKeyframe kf = getSelectedKeyframe();
        if (kf == null) {
            // Create a keyframe at the playhead
            addKeyframe();
            kf = getSelectedKeyframe();
            if (kf == null) return;
        }
        switch (axis) {
            case "rotX" -> kf.transform.rotX += delta;
            case "rotY" -> kf.transform.rotY += delta;
            case "rotZ" -> kf.transform.rotZ += delta;
            case "posX" -> kf.transform.posX += delta;
            case "posY" -> kf.transform.posY += delta;
            case "posZ" -> kf.transform.posZ += delta;
        }
    }

    private void cycleEasing() {
        AnimationKeyframe kf = getSelectedKeyframe();
        if (kf == null) return;
        kf.easing = switch (kf.easing) {
            case "linear"       -> "ease_in";
            case "ease_in"      -> "ease_out";
            case "ease_out"     -> "ease_in_out";
            default             -> "linear";
        };
    }

    private void cycleLoop() {
        animation.loopMode = switch (animation.loopMode) {
            case "loop"      -> "hold";
            case "hold"      -> "ping_pong";
            default          -> "loop";
        };
        animation.looping = !animation.loopMode.equals("hold");
        loopBtn.setMessage(Text.literal("Loop: " + animation.loopMode));
    }

    private void startPlay() {
        playing = true;
        playheadTick = 0;
        lastTickTime = System.currentTimeMillis();
        statusMsg = "Playing…";
    }

    private void stopPlay() {
        playing = false;
        playheadTick = 0;
        statusMsg = "";
    }

    private void save() {
        animation.name = nameField.getText().trim();
        try {
            animation.durationTicks = Float.parseFloat(durationField.getText().trim());
        } catch (NumberFormatException ignored) {}

        AnimationManager.getInstance().addAnimation(animation);
        AnimationStorage.save(animation);
        statusMsg = "✓ Saved!";
    }

    private void duplicate() {
        AnimationData copy = animation.copy();
        AnimationManager.getInstance().addAnimation(copy);
        AnimationStorage.save(copy);
        animation = copy;
        nameField.setText(copy.name);
        statusMsg = "✓ Duplicated!";
    }

    private void prevBone() {
        if (animation.timelines.isEmpty()) return;
        selectedBoneIdx = (selectedBoneIdx - 1 + animation.timelines.size()) % animation.timelines.size();
        selectedKfIdx = 0;
    }

    private void nextBone() {
        if (animation.timelines.isEmpty()) return;
        selectedBoneIdx = (selectedBoneIdx + 1) % animation.timelines.size();
        selectedKfIdx = 0;
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AnimationTimeline getCurrentTimeline() {
        if (animation.timelines.isEmpty()) return null;
        if (selectedBoneIdx >= animation.timelines.size()) selectedBoneIdx = 0;
        return animation.timelines.get(selectedBoneIdx);
    }

    private AnimationTimeline getOrCreateTimeline() {
        if (animation.timelines.isEmpty()) return animation.getOrCreateTimeline("body");
        return getCurrentTimeline();
    }

    private AnimationKeyframe getSelectedKeyframe() {
        AnimationTimeline tl = getCurrentTimeline();
        if (tl == null || tl.keyframes.isEmpty()) return null;
        if (selectedKfIdx >= tl.keyframes.size()) selectedKfIdx = tl.keyframes.size() - 1;
        return tl.keyframes.get(selectedKfIdx);
    }

    private String getBoneName() {
        AnimationTimeline tl = getCurrentTimeline();
        return tl != null ? tl.boneName : "—";
    }

    private int getBoneIndex() {
        return animation.timelines.isEmpty() ? 0 : selectedBoneIdx + 1;
    }
}
