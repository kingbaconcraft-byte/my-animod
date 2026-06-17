package com.animationcreator.client.gui;

import com.animationcreator.ai.AIAnimationService;
import com.animationcreator.animation.AnimationData;
import com.animationcreator.animation.AnimationManager;
import com.animationcreator.storage.AnimationStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

@Environment(EnvType.CLIENT)
public class AnimationCreatorScreen extends Screen {

    private final Screen parent;

    private TextFieldWidget promptField;
    private ButtonWidget generateBtn;
    private ButtonWidget libraryBtn;
    private ButtonWidget editorBtn;
    private ButtonWidget closeBtn;

    private String statusMessage = "";
    private boolean generating = false;
    private List<AnimationData> recentAnimations;

    // Panel dimensions
    private static final int PANEL_W = 360;
    private static final int PANEL_H = 260;

    public AnimationCreatorScreen(Screen parent) {
        super(Text.translatable("screen.animationcreator.creator"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int cy = height / 2;
        int px = cx - PANEL_W / 2;
        int py = cy - PANEL_H / 2;

        // Prompt input
        promptField = new TextFieldWidget(textRenderer,
                px + 10, py + 40, PANEL_W - 20, 20,
                Text.literal("Enter animation prompt…"));
        promptField.setMaxLength(200);
        promptField.setPlaceholder(Text.literal("e.g. zombie walking animation").formatted(Formatting.GRAY));
        addDrawableChild(promptField);
        setInitialFocus(promptField);

        // Generate button
        generateBtn = ButtonWidget.builder(Text.literal("✦ Generate Animation"), btn -> onGenerate())
                .dimensions(px + 10, py + 68, PANEL_W - 20, 20)
                .build();
        addDrawableChild(generateBtn);

        // Quick-prompt buttons
        addDrawableChild(ButtonWidget.builder(Text.literal("Walking"), btn -> quickPrompt("walking animation"))
                .dimensions(px + 10, py + 96, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Running"), btn -> quickPrompt("running sprint animation"))
                .dimensions(px + 95, py + 96, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Wave"), btn -> quickPrompt("happy waving emote"))
                .dimensions(px + 180, py + 96, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Attack"), btn -> quickPrompt("sword attack animation"))
                .dimensions(px + 265, py + 96, PANEL_W - 265, 18).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Dance"), btn -> quickPrompt("dance emote"))
                .dimensions(px + 10, py + 118, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Jump"), btn -> quickPrompt("jump animation"))
                .dimensions(px + 95, py + 118, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Idle"), btn -> quickPrompt("idle breathing animation"))
                .dimensions(px + 180, py + 118, 78, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Death"), btn -> quickPrompt("death fall animation"))
                .dimensions(px + 265, py + 118, PANEL_W - 265, 18).build());

        // Navigation
        libraryBtn = ButtonWidget.builder(Text.literal("📚 Library"), btn -> openLibrary())
                .dimensions(px + 10, py + PANEL_H - 32, (PANEL_W - 30) / 3, 22)
                .build();
        editorBtn = ButtonWidget.builder(Text.literal("✏ Editor"), btn -> openEditor())
                .dimensions(px + 10 + (PANEL_W - 30) / 3 + 5, py + PANEL_H - 32, (PANEL_W - 30) / 3, 22)
                .build();
        closeBtn = ButtonWidget.builder(Text.literal("✕ Close"), btn -> close())
                .dimensions(px + PANEL_W - 10 - (PANEL_W - 30) / 3, py + PANEL_H - 32, (PANEL_W - 30) / 3, 22)
                .build();
        addDrawableChild(libraryBtn);
        addDrawableChild(editorBtn);
        addDrawableChild(closeBtn);

        // Load recent animations
        refreshRecent();
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;

        // Panel background
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, 0xE0101820);
        ctx.fill(px, py, px + PANEL_W, py + 2,       0xFF5555FF);  // accent top bar
        ctx.drawBorder(px, py, PANEL_W, PANEL_H,      0xFF334466);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("✦ Animation Creator ✦").formatted(Formatting.AQUA),
                cx, py + 10, 0xFFFFFF);

        // Subtitle
        ctx.drawText(textRenderer,
                Text.literal("Describe an animation in plain English:").formatted(Formatting.GRAY),
                px + 10, py + 30, 0xAAAAAA, false);

        // Status message
        if (!statusMessage.isEmpty()) {
            int colour = generating ? 0xFFAA00 : statusMessage.startsWith("✓") ? 0x55FF55 : 0xFF5555;
            ctx.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(statusMessage), cx, py + 142, colour);
        }

        // Recent animations header
        ctx.drawText(textRenderer,
                Text.literal("Recent Animations:").formatted(Formatting.YELLOW),
                px + 10, py + 162, 0xFFFFFF, false);

        // Recent animation rows
        if (recentAnimations != null && !recentAnimations.isEmpty()) {
            for (int i = 0; i < Math.min(3, recentAnimations.size()); i++) {
                AnimationData a = recentAnimations.get(i);
                String label = (i + 1) + ". " + truncate(a.name, 28)
                        + " §7(" + a.durationTicks + "t" + (a.looping ? ", loop" : "") + ")";
                ctx.drawText(textRenderer, Text.literal(label), px + 10, py + 174 + i * 11, 0xCCCCCC, false);
            }
        } else {
            ctx.drawText(textRenderer,
                    Text.literal("No animations yet — generate your first!").formatted(Formatting.DARK_GRAY),
                    px + 10, py + 174, 0x666666, false);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void onGenerate() {
        String prompt = promptField.getText().trim();
        if (prompt.isEmpty()) {
            statusMessage = "✗ Please enter a prompt first!";
            return;
        }
        generating = true;
        statusMessage = "⏳ Generating animation…";
        generateBtn.active = false;

        AIAnimationService.generateAsync(
            prompt,
            this::onAnimationGenerated,
            error -> {
                statusMessage = "✗ " + error;
                generating = false;
                generateBtn.active = true;
            }
        );
    }

    private void onAnimationGenerated(AnimationData data) {
        AnimationManager.getInstance().addAnimation(data);
        AnimationStorage.save(data);
        refreshRecent();
        statusMessage = "✓ Created: " + truncate(data.name, 30);
        generating = false;
        generateBtn.active = true;
    }

    private void quickPrompt(String prompt) {
        promptField.setText(prompt);
        onGenerate();
    }

    private void openLibrary() {
        assert client != null;
        client.setScreen(new AnimationLibraryScreen(this));
    }

    private void openEditor() {
        assert client != null;
        List<AnimationData> all = new java.util.ArrayList<>(AnimationManager.getInstance().getAllAnimations());
        AnimationData selected = all.isEmpty() ? null : all.get(all.size() - 1);
        client.setScreen(new AnimationEditorScreen(this, selected));
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    private void refreshRecent() {
        List<AnimationData> all = new java.util.ArrayList<>(AnimationManager.getInstance().getAllAnimations());
        recentAnimations = all.subList(Math.max(0, all.size() - 3), all.size());
        java.util.Collections.reverse(new java.util.ArrayList<>(recentAnimations));
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
