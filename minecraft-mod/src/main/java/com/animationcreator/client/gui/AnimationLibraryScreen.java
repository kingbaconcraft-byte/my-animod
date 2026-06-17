package com.animationcreator.client.gui;

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

import java.util.*;

@Environment(EnvType.CLIENT)
public class AnimationLibraryScreen extends Screen {

    private final Screen parent;

    private TextFieldWidget searchField;
    private List<AnimationData> displayList = new ArrayList<>();
    private int selectedIndex = -1;
    private int scrollOffset = 0;

    private static final int PANEL_W   = 380;
    private static final int PANEL_H   = 280;
    private static final int ROW_H     = 18;
    private static final int LIST_ROWS = 8;

    private ButtonWidget playBtn, editBtn, deleteBtn, exportBtn, backBtn;

    public AnimationLibraryScreen(Screen parent) {
        super(Text.translatable("screen.animationcreator.library"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;

        // Search
        searchField = new TextFieldWidget(textRenderer, px + 10, py + 28, PANEL_W - 20, 18,
                Text.literal("Search…"));
        searchField.setPlaceholder(Text.literal("Search by name or tag…").formatted(Formatting.GRAY));
        searchField.setChangedListener(q -> refreshSearch(q));
        addDrawableChild(searchField);

        // Action buttons (right side)
        int bx = px + PANEL_W - 90;
        playBtn = ButtonWidget.builder(Text.literal("▶ Play"), btn -> playSelected())
                .dimensions(bx, py + 52, 80, 18).build();
        editBtn = ButtonWidget.builder(Text.literal("✏ Edit"), btn -> editSelected())
                .dimensions(bx, py + 74, 80, 18).build();
        deleteBtn = ButtonWidget.builder(Text.literal("✕ Delete"), btn -> deleteSelected())
                .dimensions(bx, py + 96, 80, 18).build();
        exportBtn = ButtonWidget.builder(Text.literal("⬇ Export"), btn -> exportSelected())
                .dimensions(bx, py + 118, 80, 18).build();
        addDrawableChild(playBtn);
        addDrawableChild(editBtn);
        addDrawableChild(deleteBtn);
        addDrawableChild(exportBtn);

        // Scroll
        addDrawableChild(ButtonWidget.builder(Text.literal("▲"), btn -> scroll(-1))
                .dimensions(bx, py + 142, 80, 14).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▼"), btn -> scroll(1))
                .dimensions(bx, py + 160, 80, 14).build());

        // Back & New
        backBtn = ButtonWidget.builder(Text.literal("← Back"), btn -> close())
                .dimensions(px + 10, py + PANEL_H - 28, 90, 20).build();
        addDrawableChild(backBtn);
        addDrawableChild(ButtonWidget.builder(Text.literal("+ New Animation"), btn -> openCreator())
                .dimensions(px + 110, py + PANEL_H - 28, 130, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("⬆ Import"), btn -> importAnimation())
                .dimensions(px + 250, py + PANEL_H - 28, 80, 20).build());

        refreshSearch("");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);

        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;
        int lx = px + 10, lw = PANEL_W - 110;

        // Panel bg
        ctx.fill(px, py, px + PANEL_W, py + PANEL_H, 0xE0101820);
        ctx.fill(px, py, px + PANEL_W, py + 2, 0xFF5555FF);
        ctx.drawBorder(px, py, PANEL_W, PANEL_H, 0xFF334466);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("📚 Animation Library").formatted(Formatting.AQUA), cx, py + 9, 0xFFFFFF);

        // Count
        ctx.drawText(textRenderer,
                Text.literal(displayList.size() + " animations").formatted(Formatting.GRAY),
                lx, py + 50, 0xAAAAAA, false);

        // List area bg
        ctx.fill(lx, py + 50, lx + lw, py + 50 + LIST_ROWS * ROW_H, 0x80000000);
        ctx.drawBorder(lx, py + 50, lw, LIST_ROWS * ROW_H, 0xFF334466);

        // Rows
        for (int i = 0; i < LIST_ROWS; i++) {
            int idx = i + scrollOffset;
            if (idx >= displayList.size()) break;
            AnimationData a = displayList.get(idx);
            int rowY = py + 50 + i * ROW_H;
            boolean sel = idx == selectedIndex;

            if (sel) ctx.fill(lx, rowY, lx + lw, rowY + ROW_H, 0x803355AA);

            String name = truncate(a.name != null ? a.name : "Unnamed", 22);
            String info = " §7" + (int) a.durationTicks + "t " + (a.looping ? "∞" : "→");
            ctx.drawText(textRenderer, Text.literal(name + info), lx + 4, rowY + 4,
                    sel ? 0xFFFFAA : 0xCCCCCC, false);
        }

        // Detail panel for selected
        if (selectedIndex >= 0 && selectedIndex < displayList.size()) {
            AnimationData a = displayList.get(selectedIndex);
            int dy = py + 50;
            ctx.drawText(textRenderer,
                    Text.literal("▸ " + truncate(a.name, 14)).formatted(Formatting.YELLOW),
                    cx - PANEL_W / 2 + lw + 16, dy, 0xFFFF55, false);
            ctx.drawText(textRenderer, Text.literal("Bones: " + a.timelines.size()),
                    cx - PANEL_W / 2 + lw + 16, dy + 12, 0xAAAAAA, false);
            ctx.drawText(textRenderer, Text.literal("Loop: " + a.loopMode),
                    cx - PANEL_W / 2 + lw + 16, dy + 22, 0xAAAAAA, false);
            if (!a.tags.isEmpty()) {
                ctx.drawText(textRenderer,
                        Text.literal(String.join(",", a.tags)).formatted(Formatting.AQUA),
                        cx - PANEL_W / 2 + lw + 16, dy + 32, 0x55FFFF, false);
            }
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int cx = width / 2, cy = height / 2;
        int px = cx - PANEL_W / 2, py = cy - PANEL_H / 2;
        int lx = px + 10, lw = PANEL_W - 110;
        int listY = py + 50;

        if (mx >= lx && mx <= lx + lw && my >= listY && my < listY + LIST_ROWS * ROW_H) {
            int row = (int) ((my - listY) / ROW_H);
            int idx = row + scrollOffset;
            if (idx < displayList.size()) selectedIndex = idx;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double hAmount, double vAmount) {
        scroll((int) -Math.signum(vAmount));
        return true;
    }

    // ── Actions ──────────────────────────────────────────────────────────────

    private void playSelected() {
        if (selectedIndex < 0 || selectedIndex >= displayList.size()) return;
        AnimationData a = displayList.get(selectedIndex);
        assert client != null;
        if (client.player != null) {
            AnimationManager.getInstance().playAnimation(client.player.getUuidAsString(), a.id);
        }
    }

    private void editSelected() {
        if (selectedIndex < 0 || selectedIndex >= displayList.size()) return;
        assert client != null;
        client.setScreen(new AnimationEditorScreen(this, displayList.get(selectedIndex)));
    }

    private void deleteSelected() {
        if (selectedIndex < 0 || selectedIndex >= displayList.size()) return;
        AnimationData a = displayList.get(selectedIndex);
        AnimationManager.getInstance().removeAnimation(a.id);
        AnimationStorage.delete(a.id);
        selectedIndex = -1;
        refreshSearch(searchField.getText());
    }

    private void exportSelected() {
        if (selectedIndex < 0 || selectedIndex >= displayList.size()) return;
        AnimationData a = displayList.get(selectedIndex);
        try {
            assert client != null;
            java.nio.file.Path dest = client.runDirectory.toPath()
                    .resolve("animation_export_" + a.id.substring(0, 8) + ".json");
            AnimationStorage.export(a, dest);
        } catch (Exception ignored) {}
    }

    private void importAnimation() {
        // Import from the game directory
        assert client != null;
        java.nio.file.Path dir = client.runDirectory.toPath();
        try (java.nio.file.DirectoryStream<java.nio.file.Path> stream =
                java.nio.file.Files.newDirectoryStream(dir, "*.json")) {
            for (java.nio.file.Path p : stream) {
                AnimationData a = AnimationStorage.importFrom(p);
                if (a != null) {
                    AnimationManager.getInstance().addAnimation(a);
                    AnimationStorage.save(a);
                }
            }
        } catch (Exception ignored) {}
        refreshSearch(searchField.getText());
    }

    private void openCreator() {
        assert client != null;
        client.setScreen(new AnimationCreatorScreen(this));
    }

    private void scroll(int delta) {
        scrollOffset = Math.max(0, Math.min(scrollOffset + delta,
                Math.max(0, displayList.size() - LIST_ROWS)));
    }

    @Override
    public void close() {
        assert client != null;
        client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    private void refreshSearch(String query) {
        displayList = AnimationManager.getInstance().searchAnimations(query);
        scrollOffset = 0;
        selectedIndex = -1;
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
