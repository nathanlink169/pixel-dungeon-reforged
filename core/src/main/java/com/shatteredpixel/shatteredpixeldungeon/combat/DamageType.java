/*
 * Pixel Dungeon Reforged
 * Copyright (C) 2024-2025 Nathan Pringle
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.combat;

import static java.util.Map.entry;

import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DamageType {
    NONE, // None damage is unblockable

    // Physical
    BLUDGEONING, PIERCING, SLASHING,

    // Energy
    ACID, COLD, ELECTRICITY, EXPLOSIVE, FIRE, POISON, SONIC, WATER, POSITIVE_ENERGY, NEGATIVE_ENERGY, FORCE,

    // Non-Combat Types
    STARVATION, AMULET, BLEEDING, DEFERRED;

    public static String GetName(DamageType type) {
        switch(type) {
            case BLUDGEONING: return "Bludgeoning";
            case PIERCING: return "Piercing";
            case SLASHING: return "Slashing";
            case ACID: return "Acid";
            case COLD: return "Cold";
            case ELECTRICITY: return "Electricity";
            case EXPLOSIVE: return "Explosive";
            case FIRE: return "Fire";
            case POISON: return "Poison";
            case SONIC: return "Sonic";
            case WATER: return "Water";
            case POSITIVE_ENERGY: return "Positive Energy";
            case NEGATIVE_ENERGY: return "Negative Energy";
            case FORCE: return "Force";
            case STARVATION: return "Starvation";
            case AMULET: return "Amulet";
            case BLEEDING: return "Bleeding";
            case DEFERRED: return "Deferred";
        }
        return "";
    }

    public static Map<DamageType, Integer> ICONS = Map.ofEntries(
            entry(BLUDGEONING, FloatingText.BLUDGEONING),
            entry(PIERCING, FloatingText.PIERCING),
            entry(SLASHING, FloatingText.SLASHING),
            entry(ACID, FloatingText.ACID),
            entry(COLD, FloatingText.COLD),
            entry(ELECTRICITY, FloatingText.ELECTRICITY),
            entry(EXPLOSIVE, FloatingText.EXPLOSION),
            entry(FIRE, FloatingText.FIRE),
            entry(POISON, FloatingText.POISON),
            entry(SONIC, FloatingText.SONIC),
            entry(WATER, FloatingText.WATER),
            entry(POSITIVE_ENERGY, FloatingText.POSITIVE_ENERGY),
            entry(NEGATIVE_ENERGY, FloatingText.NEGATIVE_ENERGY),
            entry(FORCE, FloatingText.FORCE),
            entry(STARVATION, FloatingText.HUNGER),
            entry(AMULET, FloatingText.AMULET),
            entry(BLEEDING, FloatingText.BLEEDING),
            entry(DEFERRED, FloatingText.DEFERRED)
    );

    public static ArrayList<DamageType> PHYSICAL_DAMAGE_TYPES = new ArrayList<>(){{
        add(BLUDGEONING);
        add(PIERCING);
        add(SLASHING);
    }};

    public static ArrayList<DamageType> ENERGY_DAMAGE_TYPES = new ArrayList<>() {{
        add(ACID);
        add(COLD);
        add(ELECTRICITY);
        add(EXPLOSIVE);
        add(FIRE);
        add(POISON);
        add(SONIC);
        add(WATER);
        add(POSITIVE_ENERGY);
        add(NEGATIVE_ENERGY);
        add(FORCE);
    }};

    // Helper to create a set from varargs
    public static EnumSet<DamageType> of(DamageType... types) {
        if (types.length == 0) return EnumSet.of(NONE);
        return EnumSet.of(types[0], types);
    }

    public static boolean IsDamagePhysical(DamageType type) {
        return PHYSICAL_DAMAGE_TYPES.contains(type);
    }

    public static boolean IsDamageEnergy(DamageType type) {
        return ENERGY_DAMAGE_TYPES.contains(type);
    }

    /**
     * Converts a set of damage types into a grammatically correct string.
     * Examples:
     *   {FIRE} -> "fire damage"
     *   {FIRE, COLD} -> "fire or cold damage"
     *   {FIRE, COLD, ELECTRICITY} -> "fire, cold, or electricity damage"
     *
     * @param damageTypes The set of damage types to format
     * @return A formatted string describing the damage types
     */
    public static String FormatDamageTypeString(EnumSet<DamageType> damageTypes) {
        if (damageTypes == null || damageTypes.isEmpty()) {
            return ""; // Default fallback
        }

        // Remove NONE from the set if present (it's not a real damage type)
        EnumSet<DamageType> types = EnumSet.copyOf(damageTypes);
        types.remove(DamageType.NONE);
        types.remove(DamageType.STARVATION); // Also remove non-combat types
        types.remove(DamageType.AMULET);

        if (types.isEmpty()) {
            return "physical damage";
        }

        // Convert to list for easier indexing
        ArrayList<String> typeNames = new ArrayList<>();
        for (DamageType type : types) {
            String name = GetName(type);
            if (!name.isEmpty()) {
                typeNames.add(name.toLowerCase()); // Lowercase for consistency
            }
        }

        if (typeNames.isEmpty()) {
            return "physical damage";
        }

        // Build the string based on count
        StringBuilder result = new StringBuilder();

        if (typeNames.size() == 1) {
            // Single type: "fire damage"
            result.append(typeNames.get(0));
        } else if (typeNames.size() == 2) {
            // Two types: "fire or cold damage"
            result.append(typeNames.get(0))
                    .append(" or ")
                    .append(typeNames.get(1));
        } else {
            // Three or more types: "fire, cold, or electricity damage"
            for (int i = 0; i < typeNames.size(); i++) {
                result.append(typeNames.get(i));

                if (i < typeNames.size() - 2) {
                    // Not the last or second-to-last: add comma
                    result.append(", ");
                } else if (i == typeNames.size() - 2) {
                    // Second-to-last: add Oxford comma and "or"
                    result.append(", or ");
                }
                // Last item: add nothing
            }
        }

        result.append(" damage");

        return result.toString();
    }
}
