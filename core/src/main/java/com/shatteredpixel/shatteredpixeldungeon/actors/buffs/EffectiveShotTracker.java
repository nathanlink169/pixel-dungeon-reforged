package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Gun;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class EffectiveShotTracker extends Buff
        implements CombatModifier.AccuracyModifier,
        CombatModifier.PreArmorDamageModifier,
        CombatModifier.OnHitEffect,
        CombatModifier.OnMissEffect {

    {
        type = buffType.NEUTRAL;
    }

    @Override
    public int priority() {
        return CombatModifier.Priority.HIGH;
    }

    private boolean isQualifyingAttack(AttackContext context) {
        if (!(context.attacker instanceof Hero)) {
            return false;
        }

        Hero hero = (Hero) context.attacker;

        if (!hero.hasTalent(Talent.EFFECTIVE_SHOT)) {
            return false;
        }

        if (context.attackType != AttackContext.AttackType.RANGED) {
            return false;
        }

        // Artificer: Gun.Bullet only
        if (hero.heroClass == HeroClass.ARTIFICER) {
            return hero.belongings.thrownWeapon instanceof Gun.Bullet;
        }
        // Non-Artificer: MissileWeapon (but NOT Gun.Bullet)
        else {
            return hero.belongings.thrownWeapon instanceof MissileWeapon;
        }
    }

    /**
     * Empowered shot is ready when the cooldown buff doesn't exist
     */
    private boolean isEmpoweredShot() {
        return target.buff(EffectiveShotCooldown.class) == null;
    }

    @Override
    public boolean appliesTo(AttackContext context) {
        return isQualifyingAttack(context);
    }

    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        // Only grant infinite accuracy when empowered shot is ready
        if (isEmpoweredShot()) {
            return Char.INFINITE_ACCURACY;
        }
        return currentAccuracy;
    }

    @Override
    public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
        // Only grant max damage when empowered shot is ready
        if (isEmpoweredShot()) {
            Hero hero = (Hero) context.attacker;

            if (hero.belongings.thrownWeapon instanceof MissileWeapon) {
                MissileWeapon weapon = (MissileWeapon) hero.belongings.thrownWeapon;

                int maxDamage = weapon.max();
                maxDamage = weapon.augment.damageFactor(maxDamage);

                int exStr = hero.STR() - weapon.STRReq();
                if (exStr > 0) {
                    maxDamage += exStr;
                }

                if (hero.buff(Momentum.class) != null && hero.buff(Momentum.class).freerunning()) {
                    maxDamage = Math.round(maxDamage * (1f + 0.15f * hero.pointsInTalent(Talent.PROJECTILE_MOMENTUM)));
                }

                return maxDamage;
            }
        }

        return currentDamage;
    }

    @Override
    public void onHit(AttackContext context, int finalDamage) {
        updateCooldown(context);
    }

    @Override
    public void onMiss(AttackContext context) {
        updateCooldown(context);
    }

    /**
     * Manage the EffectiveShotCooldown buff after each qualifying shot
     */
    private void updateCooldown(AttackContext context) {
        Hero hero = (Hero) context.attacker;
        EffectiveShotCooldown cooldown = hero.buff(EffectiveShotCooldown.class);
        int talentLevel = hero.pointsInTalent(Talent.EFFECTIVE_SHOT);
        int cooldownDuration = 7 - talentLevel; // 6, 5, or 4

        if (cooldown == null) {
            // Just fired an empowered shot, create cooldown buff
            Buff.affect(hero, EffectiveShotCooldown.class).set(cooldownDuration);
        } else {
            cooldown.left--;
            if (cooldown.left <= 0) {
                cooldown.detach();
            }
        }
    }
}