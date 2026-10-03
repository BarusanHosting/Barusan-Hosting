package com.example.spearswapper;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Switches to the next vanilla spear already present in the hotbar.
 *
 * <p>This mod stays inside ordinary vanilla inventory behavior. It does not
 * fabricate item stacks, edit item components, move inventory contents,
 * spoof movement, inject attack packets, or modify server-side combat logic.</p>
 *
 * <p>The only network message produced by a swap is the normal selected-hotbar
 * slot packet. No packet obfuscation or anti-cheat bypass logic is used.</p>
 */
public final class SpearAttributeSwapperClient implements ClientModInitializer {

    private static final String MOD_ID = "spear-attribute-swapper";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final int ACTION_COOLDOWN_TICKS = 6;

    private static final KeyMapping SWAP_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyMapping(
                    "key.spear-attribute-swapper.swap",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    KeyMapping.Category.MISC
            )
    );

    private int cooldownTicks;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (cooldownTicks > 0) {
                cooldownTicks--;
            }

            while (SWAP_KEY.consumeClick()) {
                try {
                    swapToNextSpear(client);
                } catch (RuntimeException exception) {
                    LOGGER.debug("Spear swap skipped after client state changed", exception);
                }
            }
        });
    }

    private void swapToNextSpear(Minecraft client) {
        if (cooldownTicks > 0 || client.player == null || client.getConnection() == null) {
            return;
        }

        if (client.screen != null || client.player.isDeadOrDying()) {
            return;
        }

        Inventory inventory = client.player.getInventory();
        int currentSlot = inventory.getSelectedSlot();
        int targetSlot = findNextSpear(inventory, currentSlot);

        if (targetSlot < 0 || targetSlot == currentSlot) {
            return;
        }

        inventory.setSelectedSlot(targetSlot);
        client.getConnection()
                .getConnection()
                .send(new ServerboundSetCarriedItemPacket(targetSlot));

        cooldownTicks = ACTION_COOLDOWN_TICKS;
    }

    private static int findNextSpear(Inventory inventory, int currentSlot) {
        int hotbarSize = Inventory.getHotbarSize();
        for (int offset = 1; offset <= hotbarSize; offset++) {
            int slot = (currentSlot + offset) % hotbarSize;
            if (isSpear(inventory.getItem(slot))) {
                return slot;
            }
        }
        return -1;
    }

    private static boolean isSpear(ItemStack stack) {
        Item item = stack.getItem();
        return item == Items.WOODEN_SPEAR
                || item == Items.STONE_SPEAR
                || item == Items.COPPER_SPEAR
                || item == Items.IRON_SPEAR
                || item == Items.GOLDEN_SPEAR
                || item == Items.DIAMOND_SPEAR
                || item == Items.NETHERITE_SPEAR;
    }
}
