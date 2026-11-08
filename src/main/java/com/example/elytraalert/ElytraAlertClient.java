package com.example.elytraalert;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class ElytraAlertClient implements ClientModInitializer {
    public static final int LEVEL_1_Y = 100;
    public static final int LEVEL_2_Y = 200;
    private static final int BEEP_INTERVAL_TICKS = 15;

    private final Map<UUID, Integer> levels = new HashMap<>();
    private int alarmTicksLeft = 0;
    private int beepTimer = 0;

    @Override
    public void onInitializeClient() {
        ElytraAlertConfig.load();
        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    public static boolean hasElytra(Player p) {
        ElytraAlertConfig cfg = ElytraAlertConfig.INSTANCE;
        boolean wearing = p.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);
        return wearing && (!cfg.onlyWhenGliding || p.isFallFlying());
    }

    /** Returns -1 for "no change", otherwise the glow colour (black by day, white at night). */
    public static int glowColorFor(Object entity) {
        ElytraAlertConfig cfg = ElytraAlertConfig.INSTANCE;
        if (!(cfg.enabled && cfg.colorPlayers && entity instanceof Player p)) return -1;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return -1;
        if (p == mc.player && !cfg.includeSelf) return -1;
        if (!hasElytra(p)) return -1;
        if (cfg.colorUnder500Only && p.getY() >= 500.0) return -1;
        long t = mc.level.getGameTime() % 24000L;
        boolean night = t >= 13000L && t < 23000L;
        return night ? 0xFFFFFF : 0;
    }

    private void tick(Minecraft mc) {
        if (mc.level == null || mc.player == null) {
            levels.clear();
            alarmTicksLeft = 0;
            return;
        }
        ElytraAlertConfig cfg = ElytraAlertConfig.INSTANCE;
        if (!cfg.enabled) {
            levels.clear();
            alarmTicksLeft = 0;
            beepTimer = 0;
            return;
        }
        boolean anyActive = false;
        int highest = 0;
        for (Player p : mc.level.players()) {
            if (p == mc.player && !cfg.includeSelf) continue;
            int newLevel = 0;
            if (hasElytra(p)) {
                double y = p.getY();
                if (y >= LEVEL_2_Y) newLevel = 2;
                else if (y >= LEVEL_1_Y) newLevel = 1;
            }
            if (newLevel > 0) anyActive = true;
            highest = Math.max(highest, newLevel);
            int oldLevel = levels.getOrDefault(p.getUUID(), 0);
            if (newLevel > oldLevel) {
                message(mc, p, newLevel);
                alarmTicksLeft = cfg.durationSeconds * 20;
                beepTimer = 0;
            }
            levels.put(p.getUUID(), newLevel);
        }
        Iterator<UUID> it = levels.keySet().iterator();
        while (it.hasNext()) {
            if (mc.level.getPlayerByUUID(it.next()) == null) it.remove();
        }

        boolean alarmOn;
        if (cfg.continuous) {
            alarmOn = anyActive;
            alarmTicksLeft = 0;
        } else {
            alarmOn = alarmTicksLeft > 0;
            if (alarmTicksLeft > 0) alarmTicksLeft--;
        }
        if (alarmOn) {
            if (beepTimer <= 0) {
                float pitch = highest >= 2 ? 2.0f : 1.0f;
                AlertSounds.play(cfg.sound, pitch);
                beepTimer = BEEP_INTERVAL_TICKS;
            }
            beepTimer--;
        } else {
            beepTimer = 0;
        }
    }

    private void message(Minecraft mc, Player p, int level) {
        int height = level == 2 ? 200 : 100;
        ChatFormatting color = level == 2 ? ChatFormatting.RED : ChatFormatting.GOLD;
        MutableComponent msg = Component.literal("[Elytra Alert] ").withStyle(ChatFormatting.AQUA)
            .append(Component.literal(p.getName().getString()).withStyle(color))
            .append(Component.literal(" is flying with elytra above " + height + " blocks! ("
                + (int) p.getX() + ", " + (int) p.getY() + ", " + (int) p.getZ() + ")").withStyle(ChatFormatting.WHITE));
        mc.player.displayClientMessage(msg, false);
    }
}
