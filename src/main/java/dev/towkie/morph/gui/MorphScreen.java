package dev.towkie.morph.gui;

import dev.towkie.morph.MorphStore;
import dev.towkie.morph.Scale;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** A simple in-game editor: choose any online player on the left, sizes on right. */
public final class MorphScreen extends Screen {
    private UUID selected;
    private String selectedName = "";
    private int page = 0;
    private int rows = 5;
    private int left = 0;
    private int top = 0;
    private List<PlayerListEntry> players = List.of();

    public MorphScreen() {
        super(Text.literal("Towkie Morphs"));
    }

    @Override
    protected void init() {
        if (client == null || client.getNetworkHandler() == null) return;
        players = new ArrayList<>(client.getNetworkHandler().getPlayerList());
        players.sort(Comparator.comparing(p -> p.getProfile().getName(), String.CASE_INSENSITIVE_ORDER));
        rows = Math.max(3, Math.min(7, (height - 105) / 23));
        int panelWidth = Math.min(435, width - 16);
        left = (width - panelWidth) / 2;
        top = Math.max(8, (height - Math.min(height - 12, 246)) / 2);

        // On opening: select own player if present, else first player.
        if (selected == null && client.player != null) {
            selected = client.player.getUuid();
        }
        boolean stillOnline = false;
        for (PlayerListEntry p : players) {
            if (p.getProfile().getId().equals(selected)) {
                selectedName = p.getProfile().getName();
                stillOnline = true;
                break;
            }
        }
        if (!stillOnline) {
            selected = players.isEmpty() ? null : players.get(0).getProfile().getId();
            selectedName = players.isEmpty() ? "" : players.get(0).getProfile().getName();
        }

        page = Math.max(0, Math.min(page, Math.max(0, (players.size() - 1) / rows)));
        int listWidth = Math.min(146, Math.max(105, panelWidth / 3));
        int sx = left + listWidth + 17;
        int sliderWidth = Math.max(112, panelWidth - listWidth - 30);
        int listY = top + 36;
        for (int i = page * rows; i < Math.min(players.size(), (page + 1) * rows); i++) {
            PlayerListEntry entry = players.get(i);
            String name = entry.getProfile().getName();
            int y = listY + (i - page * rows) * 23;
            String label = entry.getProfile().getId().equals(selected) ? "§b▶ " + name : name;
            addDrawableChild(ButtonWidget.builder(Text.literal(label), button -> {
                selected = entry.getProfile().getId();
                selectedName = name;
                clearAndInit();
            }).dimensions(left + 7, y, listWidth - 7, 20).build());
        }
        int navY = top + 38 + rows * 23;
        addDrawableChild(ButtonWidget.builder(Text.literal("◀"), b -> {
            if (page > 0) { page--; clearAndInit(); }
        }).dimensions(left + 7, navY, 39, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> {
            if ((page + 1) * rows < players.size()) { page++; clearAndInit(); }
        }).dimensions(left + listWidth - 40, navY, 39, 20).build());

        if (selected != null) {
            Scale profile = MorphStore.edit(selected);
            for (int i = 0; i < Scale.KEYS.length; i++) {
                int sliderY = top + 32 + i * 23;
                addDrawableChild(new SizeSlider(sx, sliderY, sliderWidth, 20, selected, i, profile.get(i)));
            }
            addDrawableChild(ButtonWidget.builder(Text.literal("Сбросить размеры"), b -> {
                MorphStore.reset(selected);
                clearAndInit();
            }).dimensions(sx, height - top - 29, sliderWidth / 2 - 3, 20).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Готово"), b -> close())
                .dimensions(sx + sliderWidth / 2 + 3, height - top - 29, sliderWidth / 2 - 3, 20).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal("Закрыть"), b -> close())
                .dimensions(sx, height - top - 29, sliderWidth, 20).build());
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        int panelWidth = Math.min(435, width - 16);
        int panelHeight = height - 2 * top;
        context.fill(left - 4, top - 5, left + panelWidth + 4, top + panelHeight + 5, 0xDD0C1529);
        context.fill(left - 4, top - 5, left + panelWidth + 4, top - 3, 0xFF42BCD8);
        context.drawCenteredTextWithShadow(textRenderer, "TOWKIE MORPHS", width / 2, top + 6, 0xFF79E5FF);
        context.drawTextWithShadow(textRenderer, "Игроки онлайн: " + players.size(), left + 7, top + 23, 0xFFDAE5F6);
        if (selected != null) {
            int sx = left + Math.min(146, Math.max(105, panelWidth / 3)) + 17;
            context.drawTextWithShadow(textRenderer, selectedName, sx, top + 20, 0xFFFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer, "Нет игроков на сервере", width / 2, height / 2, 0xFFE0E0E0);
        }
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, "Только визуально · Видно только тебе", width / 2, height - top - 7, 0xFF9CC5D9);
    }

    @Override
    public void close() {
        MorphStore.save();
        super.close();
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class SizeSlider extends SliderWidget {
        private final UUID player;
        private final int property;

        SizeSlider(int x, int y, int width, int height, UUID player, int property, float scale) {
            super(x, y, width, height, Text.empty(), normalized(scale, property));
            this.player = player;
            this.property = property;
            updateMessage();
        }

        private static float min(int property) { return property == 0 ? 0.25f : 0.5f; }
        private static float max(int property) { return property == 0 ? 4f : 3f; }
        private static double normalized(float scale, int property) {
            return Math.max(0, Math.min(1, (scale - min(property)) / (max(property) - min(property))));
        }
        private float actual() { return (float) (min(property) + value * (max(property) - min(property))); }

        @Override
        protected void updateMessage() {
            if (property >= 0 && property < Scale.LABELS.length) {
                setMessage(Text.literal(Scale.LABELS[property] + ": " + String.format(Locale.ROOT, "%.2fx", actual())));
            }
        }

        @Override
        protected void applyValue() {
            if (player != null) {
                MorphStore.edit(player).set(property, actual());
            }
        }
    }
}
