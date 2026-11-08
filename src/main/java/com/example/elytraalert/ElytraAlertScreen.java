package com.example.elytraalert;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ElytraAlertScreen extends Screen {
    private final Screen parent;
    private final ElytraAlertConfig cfg;
    private Button soundButton;
    private DurationSlider slider;

    public ElytraAlertScreen(Screen parent) {
        super(Component.literal("Elytra Alert Settings"));
        this.cfg = ElytraAlertConfig.INSTANCE;
        this.parent = parent;
    }

    private static Component onOff(String name, boolean v) { return Component.literal(name + ": " + (v ? "ON" : "OFF")); }

    private Component soundLabel() {
        return Component.literal("Sound " + (cfg.sound + 1) + "/" + AlertSounds.NAMES.length + ": " + AlertSounds.NAMES[cfg.sound] + " (click = test)");
    }

    private void changeSound(int delta) {
        int n = AlertSounds.NAMES.length;
        cfg.sound = ((cfg.sound + delta) % n + n) % n;
        soundButton.setMessage(soundLabel());
        AlertSounds.play(cfg.sound, 1.0f);
    }

    private Component heightLabel() { return Component.literal("Glow height: " + (cfg.colorUnder500Only ? "Under 500 blocks" : "Unlimited")); }
    private Component glideLabel() { return Component.literal("Trigger: " + (cfg.onlyWhenGliding ? "Only when gliding" : "Wearing elytra")); }

    @Override
    protected void init() {
        int x = width / 2 - 125;
        int y = Math.max(34, height / 2 - 112);
        int step = 24;

        addRenderableWidget(Button.builder(onOff("Elytra Alert mod", cfg.enabled), b -> {
            cfg.enabled = !cfg.enabled;
            b.setMessage(onOff("Elytra Alert mod", cfg.enabled));
        }).bounds(x, y, 250, 20).build());
        y += step + 6;

        addRenderableWidget(Button.builder(Component.literal("<"), b -> changeSound(-1)).bounds(x, y, 20, 20).build());
        soundButton = addRenderableWidget(Button.builder(soundLabel(), b -> AlertSounds.play(cfg.sound, 1.0f))
            .bounds(x + 22, y, 206, 20).build());
        addRenderableWidget(Button.builder(Component.literal(">"), b -> changeSound(1)).bounds(x + 230, y, 20, 20).build());
        y += step;

        addRenderableWidget(new VolumeSlider(x, y, 250, 20)); // NEW: alert volume 0..1000%
        y += step;

        slider = addRenderableWidget(new DurationSlider(x, y, 250, 20));
        slider.active = !cfg.continuous;
        y += step;

        addRenderableWidget(Button.builder(onOff("Alert until no elytra left", cfg.continuous), b -> {
            cfg.continuous = !cfg.continuous;
            b.setMessage(onOff("Alert until no elytra left", cfg.continuous));
            slider.active = !cfg.continuous;
        }).bounds(x, y, 250, 20).build());
        y += step;

        addRenderableWidget(Button.builder(onOff("Black (day) / White (night) glow", cfg.colorPlayers), b -> {
            cfg.colorPlayers = !cfg.colorPlayers;
            b.setMessage(onOff("Black (day) / White (night) glow", cfg.colorPlayers));
        }).bounds(x, y, 250, 20).build());
        y += step;

        addRenderableWidget(Button.builder(heightLabel(), b -> {
            cfg.colorUnder500Only = !cfg.colorUnder500Only;
            b.setMessage(heightLabel());
        }).bounds(x, y, 250, 20).build());
        y += step;

        addRenderableWidget(Button.builder(onOff("Include myself", cfg.includeSelf), b -> {
            cfg.includeSelf = !cfg.includeSelf;
            b.setMessage(onOff("Include myself", cfg.includeSelf));
        }).bounds(x, y, 250, 20).build());
        y += step;

        addRenderableWidget(Button.builder(glideLabel(), b -> {
            cfg.onlyWhenGliding = !cfg.onlyWhenGliding;
            b.setMessage(glideLabel());
        }).bounds(x, y, 250, 20).build());
        y += step + 6;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose()).bounds(x, y, 250, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        ElytraAlertConfig.save();
        minecraft.setScreen(parent);
    }

    /** Alert length 1..60 s (unchanged from 2.0.0). */
    private final class DurationSlider extends AbstractSliderButton {
        DurationSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), (cfg.durationSeconds - 1) / 59.0);
            updateMessage();
        }
        private int seconds() { return 1 + (int) Math.round(value * 59.0); }
        @Override protected void updateMessage() { setMessage(Component.literal("Alert length: " + seconds() + " s")); }
        @Override protected void applyValue() { cfg.durationSeconds = seconds(); }
    }

    /** Alert volume 0..1000 % in 10 % steps. */
    private final class VolumeSlider extends AbstractSliderButton {
        VolumeSlider(int x, int y, int w, int h) {
            super(x, y, w, h, Component.empty(), cfg.volume / 1000.0);
            updateMessage();
        }
        private int percent() { return (int) Math.round(value * 100.0) * 10; }
        @Override protected void updateMessage() { setMessage(Component.literal("Alert volume: " + percent() + "%")); }
        @Override protected void applyValue() { cfg.volume = percent(); }
    }
}
