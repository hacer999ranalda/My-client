package com.example.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

public class ClientCheatModule {
    // Настройки функций (true - включено, false - выключено)
    public static boolean aimbotEnabled = true;
    public static boolean killauraEnabled = true;
    public static boolean flyEnabled = true;
    public static boolean noclipEnabled = true;
    public static boolean hitboxExpandEnabled = true;
    public static boolean customCrosshair = true;
    public static boolean customHandEnabled = true;

    // Параметры функций
    public static double aimbotRange = 6.0;
    public static double auraRange = 3.8;
    public static double hitboxSizeMultiplier = 0.5; // На сколько блоков расширять хитбокс
}

// 1. МИКСИН ДЛЯ ОБНОВЛЕНИЯ ФУНКЦИЙ КАЖДЫЙ ТИК (AimBot, KillAura, Fly, NoClip)
@Mixin(PlayerEntity.class)
class PlayerEntityMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void onTick(CallbackInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        // --- ФУНКЦИЯ FLY (ПОЛЕТ) ---
        if (ClientCheatModule.flyEnabled) {
            mc.player.abilities.flying = true;
            if (mc.options.keyJump.isPressed()) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.5, mc.player.getVelocity().z);
            }
        }

        // --- ФУНКЦИЯ NOCLIP (СКВОЗЬ СТЕНЫ) ---
        if (ClientCheatModule.noclipEnabled) {
            mc.player.noClip = true;
        }

        // ПОИСК БЛИЖАЙШЕЙ ЦЕЛИ ДЛЯ АИМБОТА И КИЛЛАУРЫ
        PlayerEntity targetPlayer = null;
        double closestDist = ClientCheatModule.aimbotRange;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player || !entity.isAlive()) continue;
            double dist = mc.player.distanceTo(entity);
            if (dist = 1.0f) {
            List<Entity> targets = mc.world.getEntitiesByClass(Entity.class, 
                mc.player.getBoundingBox().expand(ClientCheatModule.auraRange), 
                entity -> entity != mc.player && entity.isAlive());
            
            if (!targets.isEmpty()) {
                Entity target = targets.get(0);
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(Hand.MAIN_HAND);
            }
        }
    }
}

// 2. МИКСИН НА УВЕЛИЧЕНИЕ ХИТБОКСОВ (Hitbox Extender)
@Mixin(Entity.class)
class EntityMixin {
    @Inject(at = @At("RETURN"), method = "getBoundingBox", cancellable = true)
    private void onGetBoundingBox(CallbackInfoReturnable<Box> info) {
        if (ClientCheatModule.hitboxExpandEnabled) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if ((Object) this instanceof PlayerEntity && (Object) this != mc.player) {
                Box originalBox = info.getReturnValue();
                double m = ClientCheatModule.hitboxSizeMultiplier;
                info.setReturnValue(originalBox.expand(m, 0.0, m));
            }
        }
    }
}

// 3. МИКСИН НА КАСТОМНЫЙ ПРИЦЕЛ И МЕНЮ ОТОБРАЖЕНИЯ (HUD в стиле Котлована)
@Mixin(InGameHud.class)
class InGameHudMixin {
    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.options.hudHidden) return;

        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        // Кастомный прицел а-ля Impact (Точка в круге)
        if (ClientCheatModule.customCrosshair) {
            int centerX = width / 2;
            int centerY = height / 2;
            mc.textRenderer.drawWithShadow(matrices, "•", centerX - 2, centerY - 4, 0x00FFCC);
            mc.textRenderer.drawWithShadow(matrices, "○", centerX - 4, centerY - 5, 0x00FFCC);
        }

        // Отрисовка активных хаков на экране (ХУД Котлована)
        mc.textRenderer.drawWithShadow(matrices, "§b[Catlavan Lite]§r v1.0", 10, 10, 0xFFFFFF);
        int yOffset = 25;
        if (ClientCheatModule.aimbotEnabled) { mc.textRenderer.drawWithShadow(matrices, "AimBot", 10, yOffset, 0xFFCC00); yOffset += 10; }
        if (ClientCheatModule.killauraEnabled) { mc.textRenderer.drawWithShadow(matrices, "KillAura", 10, yOffset, 0xFF5555); yOffset += 10; }
        if (ClientCheatModule.flyEnabled) { mc.textRenderer.drawWithShadow(matrices, "Fly", 10, yOffset, 0x55FF55); yOffset += 10; }
        if (ClientCheatModule.noclipEnabled) { mc.textRenderer.drawWithShadow(matrices, "NoClip", 10, yOffset, 0x5555FF); yOffset += 10; }
        if (ClientCheatModule.hitboxExpandEnabled) { mc.textRenderer.drawWithShadow(matrices, "HitboxExpand", 10, yOffset, 0xAA00FF); yOffset += 10; }
    }
}
	
