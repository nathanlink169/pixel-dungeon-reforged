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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Main combat resolution engine.
 * Handles the flow of an attack from start to finish.
 */
public class CombatResolver {
    /**
     * Execute a complete attack sequence.
     */
    public static AttackResult resolve(AttackContext context) {
        // Phase 1: Determine if attack hits
        HitResult hitResult = resolveHit(context);

        if (hitResult == HitResult.MISS) {
            triggerOnMissEffects(context);
            return AttackResult.miss(context);
        }

        if (hitResult == HitResult.INVULNERABLE) {
            return AttackResult.invulnerable(context);
        }

        // Phase 2: Calculate base damage
        int damage = context.baseDamage;

        // Phase 3: Apply pre-armor modifiers
        damage = applyPreArmorModifiers(context, damage);

        // Phase 4: Apply armor
        int armor = applyArmorModifiers(context, context.baseArmour);
        damage = Math.max(0, damage - armor);

        // Phase 5: Apply post-armor modifiers
        damage = applyPostArmorModifiers(context, damage);

        // Phase 6: Trigger on-hit effects (before damage applied)
        triggerOnHitEffects(context, damage);

        // Phase 7: Apply damage
        int damageDealt = context.defender.Damage(damage, context.attacker, context.damageType);

        // Phase 8: Trigger post-damage effects
        triggerOnDamageEffects(context, damageDealt);

        return AttackResult.hit(context, damageDealt);
    }

    public static boolean checkHit(AttackContext context) {
        HitResult result = resolveHit(context);
        return result == HitResult.HIT;
    }

    /**
     * Phase 1: Hit calculation
     */
    private static HitResult resolveHit(AttackContext context) {
        // Check invulnerability first
        if (context.defender.isInvulnerable(context.attacker.getClass())) {
            return HitResult.INVULNERABLE;
        }

        float accuracy = context.baseAccuracy;
        float evasion = context.baseEvasion;

        // If surprise attack, always hit
        if (context.defender instanceof Mob && ((Mob)context.defender).surprisedBy(context.attacker)) {
            accuracy = Char.INFINITE_ACCURACY;
            evasion = 0.0f;
        }
        else {
            // Gather all modifiers
            ArrayList<CombatModifier.AccuracyModifier> accMods = gatherModifiers(context, CombatModifier.AccuracyModifier.class);
            ArrayList<CombatModifier.EvasionModifier> evaMods = gatherModifiers(context, CombatModifier.EvasionModifier.class);

            // Calculate modified accuracy
            for (CombatModifier.AccuracyModifier mod : accMods) {
                if (mod.appliesTo(context)) {
                    accuracy = mod.modifyAccuracy(context, accuracy);
                    if (accuracy >= Char.INFINITE_ACCURACY || accuracy == 0) {
                        break;
                    }
                }
            }

            // Calculate modified evasion
            for (CombatModifier.EvasionModifier mod : evaMods) {
                if (mod.appliesTo(context)) {
                    evasion = mod.modifyEvasion(context, evasion);
                    if (evasion >= Char.INFINITE_EVASION || evasion == 0) {
                        break;
                    }
                }
            }
        }

        // Handle infinite values
        if (evasion >= Char.INFINITE_EVASION) {
            return HitResult.MISS;
        }
        if (accuracy >= Char.INFINITE_ACCURACY) {
            return HitResult.HIT;
        }

        // Roll to hit
        float accRoll = Random.Float(accuracy);
        float evaRoll = Random.Float(evasion);

        return accRoll >= evaRoll ? HitResult.HIT : HitResult.MISS;
    }

    /**
     * Phase 3: Pre-armor damage modifiers
     */
    private static int applyPreArmorModifiers(AttackContext context, int baseDamage) {
        ArrayList<CombatModifier.PreArmorDamageModifier> modifiers = gatherModifiers(context, CombatModifier.PreArmorDamageModifier.class);

        int damage = baseDamage;
        for (CombatModifier.PreArmorDamageModifier mod : modifiers) {
            if (mod.appliesTo(context)) {
                damage = mod.modifyPreArmorDamage(context, damage);
            }
        }

        return damage;
    }

    /**
     * Phase 4: Armor modifiers
     */
    private static int applyArmorModifiers(AttackContext context, int baseArmor) {
        ArrayList<CombatModifier.ArmorModifier> modifiers = gatherModifiers(context, CombatModifier.ArmorModifier.class);

        int armor = baseArmor;
        for (CombatModifier.ArmorModifier mod : modifiers) {
            if (mod.appliesTo(context)) {
                armor = mod.modifyArmor(context, armor);
            }
        }

        return Math.max(0, armor);
    }

    /**
     * Phase 5: Post-armor damage modifiers
     */
    private static int applyPostArmorModifiers(AttackContext context, int damage) {
        ArrayList<CombatModifier.PostArmorDamageModifier> modifiers = gatherModifiers(context, CombatModifier.PostArmorDamageModifier.class);

        for (CombatModifier.PostArmorDamageModifier mod : modifiers) {
            if (mod.appliesTo(context)) {
                damage = mod.modifyPostArmorDamage(context, damage);
            }
        }

        return damage;
    }

    /**
     * Phase 6: On-hit effects
     */
    private static void triggerOnHitEffects(AttackContext context, int damage) {
        ArrayList<CombatModifier.OnHitEffect> effects = gatherModifiers(context, CombatModifier.OnHitEffect.class);

        for (CombatModifier.OnHitEffect effect : effects) {
            if (effect.appliesTo(context)) {
                effect.onHit(context, damage);
            }
        }
    }

    /**
     * On-miss effects
     */
    private static void triggerOnMissEffects(AttackContext context) {
        ArrayList<CombatModifier.OnMissEffect> effects = gatherModifiers(context, CombatModifier.OnMissEffect.class);

        for (CombatModifier.OnMissEffect effect : effects) {
            if (effect.appliesTo(context)) {
                effect.onMiss(context);
            }
        }
    }

    /**
     * Phase 8: Post-damage effects
     */
    private static void triggerOnDamageEffects(AttackContext context, int damageDealt) {
        ArrayList<CombatModifier.OnDamageEffect> effects = gatherModifiers(context, CombatModifier.OnDamageEffect.class);

        for (CombatModifier.OnDamageEffect effect : effects) {
            if (effect.appliesTo(context)) {
                effect.onDamage(context, damageDealt);
            }
        }
    }

    /**
     * Gather all modifiers of a given type from both attacker and defender.
     * Returns them sorted by priority.
     */
    @SuppressWarnings("unchecked")
    private static <T extends CombatModifier> ArrayList<T> gatherModifiers(
            AttackContext context,
            Class<T> modifierClass) {

        ArrayList<T> modifiers = new ArrayList<>();

        // CHECK HERO'S TALENT MANAGER (for global talent effects)
        if (modifierClass.isInstance(Dungeon.hero.GetTalentManager())) {
            modifiers.add((T) Dungeon.hero.GetTalentManager());
        }

        // Gather from attacker's buffs
        if (modifierClass.isInstance(context.attacker)) {
            modifiers.add((T) context.attacker);
        }

        for (Buff buff : context.attacker.buffs()) {
            if (modifierClass.isInstance(buff)) {
                modifiers.add((T) buff);
            }
        }

        modifiers.addAll(getEquipmentModifiers(context.attacker, modifierClass));

        // Gather from defender's buffs
        if (modifierClass.isInstance(context.defender)) {
            modifiers.add((T) context.defender);
        }

        for (Buff buff : context.defender.buffs()) {
            if (modifierClass.isInstance(buff)) {
                modifiers.add((T) buff);
            }
        }

        modifiers.addAll(getEquipmentModifiers(context.defender, modifierClass));

        // Gather from heros artifacts
        ArrayList<Artifact> artifacts = Dungeon.hero.belongings.getAllItems(Artifact.class);
        for (Artifact artifact : artifacts) {
            if (modifierClass.isInstance(artifact)) {
                modifiers.add((T) artifact);
            }
        }

        // Gather from heros trinkets
        ArrayList<Trinket> trinkets = Dungeon.hero.belongings.getAllItems(Trinket.class);
        for (Trinket trinket : trinkets) {
            if (modifierClass.isInstance(trinket)) {
                modifiers.add((T) trinket);
            }
        }

        // Sort by priority (higher priority first)
        Collections.sort(modifiers, new Comparator<T>() {
            @Override
            public int compare(T a, T b) {
                return Integer.compare(b.priority(), a.priority());
            }
        });

        return modifiers;
    }

    private static <T extends CombatModifier> ArrayList<T> getEquipmentModifiers(
            Char character,
            Class<T> modifierClass) {

        ArrayList<T> modifiers = new ArrayList<>();

        // Check weapon
        if (character.getWeapon() != null && modifierClass.isInstance(character.getWeapon())) {
            KindOfWeapon kindOfWeapon = character.getWeapon();
            modifiers.add(modifierClass.cast(kindOfWeapon));
            if (kindOfWeapon instanceof Weapon) {
                Weapon weapon = (Weapon) kindOfWeapon;
                if (modifierClass.isInstance(weapon.enchantment)) {
                    modifiers.add(modifierClass.cast(weapon.enchantment));
                }
            }
        }

        // Check armor
        if (character.getArmor() != null && modifierClass.isInstance(character.getArmor())) {
            Armor armor = character.getArmor();
            modifiers.add(modifierClass.cast(character.getArmor()));
            if (modifierClass.isInstance(armor.glyph)) {
                modifiers.add(modifierClass.cast(armor.glyph));
            }
        }

        // Check artifact
        if (character.getArtifact() != null && modifierClass.isInstance(character.getArtifact())) {
            modifiers.add(modifierClass.cast(character.getArtifact()));
        }

        // Check misc item
        if (character.getMisc() != null && modifierClass.isInstance(character.getMisc())) {
            modifiers.add(modifierClass.cast(character.getMisc()));
        }

        // Check ring
        if (character.getRing() != null && modifierClass.isInstance(character.getRing())) {
            modifiers.add(modifierClass.cast(character.getRing()));
        }

        return modifiers;
    }

    private enum HitResult {
        HIT, MISS, INVULNERABLE
    }
}
