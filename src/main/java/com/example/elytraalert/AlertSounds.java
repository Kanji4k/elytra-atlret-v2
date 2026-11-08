package com.example.elytraalert;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public final class AlertSounds {
    public static final String[] NAMES = {
        "Pling", "Bell", "Chime", "Bit", "Xylophone", "Banjo", "Harp",
        "XP Orb", "Level Up", "Anvil", "Bell Block", "Dragon Growl", "Wither Spawn", "Beacon", "Thunder"
    };

    private AlertSounds() {}

    // Object[] on purpose: note-block sounds are Holders, the rest are plain SoundEvents.
    private static Object[] raw() {
        return new Object[] {
            SoundEvents.NOTE_BLOCK_PLING, SoundEvents.NOTE_BLOCK_BELL, SoundEvents.NOTE_BLOCK_CHIME,
            SoundEvents.NOTE_BLOCK_BIT, SoundEvents.NOTE_BLOCK_XYLOPHONE, SoundEvents.NOTE_BLOCK_BANJO,
            SoundEvents.NOTE_BLOCK_HARP,
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundEvents.PLAYER_LEVELUP, SoundEvents.ANVIL_LAND,
            SoundEvents.BELL_BLOCK, SoundEvents.ENDER_DRAGON_GROWL, SoundEvents.WITHER_SPAWN,
            SoundEvents.BEACON_ACTIVATE, SoundEvents.LIGHTNING_BOLT_THUNDER
        };
    }

    public static SoundEvent get(int index) {
        Object[] a = raw();
        Object o = a[Math.max(0, Math.min(a.length - 1, index))];
        if (o instanceof Holder<?> h) return (SoundEvent) h.value();
        return (SoundEvent) o;
    }

    /**
     * Minecraft clamps ONE sound instance to 100% volume, so a plain volume > 1.0 does nothing.
     * To reach up to 1000% we start several copies of the same sound at once (10 copies = 1000%).
     * Works the same for every sound in NAMES.
     */
    public static void play(int index, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        SoundEvent ev = get(index);
        int pct = Math.max(0, Math.min(1000, ElytraAlertConfig.INSTANCE.volume));
        int full = pct / 100;
        float rest = (pct % 100) / 100f;
        for (int i = 0; i < full; i++) mc.getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, 1.0f));
        if (rest > 0f) mc.getSoundManager().play(SimpleSoundInstance.forUI(ev, pitch, rest));
    }
}
