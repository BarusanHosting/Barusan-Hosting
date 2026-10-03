# Spear Attribute Swapper

Minimal client-side Fabric mod for Minecraft Java Edition 1.21.11.

## Function

Press the configurable **Swap to Next Spear** key to select the next vanilla spear
already present in the player's hotbar.

The mod never edits, replaces, or creates an ItemStack. A spear keeps all of its
existing enchantments and components.

## Keybind

Default key: **G**

Change it in Options -> Controls -> Key Binds.

## Server behavior

The implementation performs only a normal selected-hotbar-slot state change and
sends the corresponding vanilla selected-slot packet.

It does not move inventory items, fabricate ItemStacks, edit item components,
spoof movement, send attack packets, manipulate combat timing, obfuscate packets,
or attempt to bypass anti-cheat systems.

## Build

Requirements: Java 21 and Gradle 9.2.1 or newer.

Run `gradle build`. The remapped JAR is written to `build/libs/`.
