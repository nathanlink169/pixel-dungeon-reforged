package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class Cursed extends FlavourBuff implements CombatModifier.AccuracyModifier, CombatModifier.EvasionModifier {

    public static final float DURATION = 90f;

    {
        type = buffType.NEGATIVE;
        announced = true;
    }

    @Override
    public int icon() {
        return BuffIndicator.SACRIFICE;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }

    @Override
    public float modifyAccuracy(AttackContext context, float currentAccuracy) {
        if (context.attacker == target) {
            return currentAccuracy * 0.9f;
        }
        return currentAccuracy;
    }

    @Override
    public int priority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean appliesTo(AttackContext context) {
        return context.attacker == target || context.defender == target;
    }

    @Override
    public float modifyEvasion(AttackContext context, float currentEvasion) {
        if (context.defender == target) {
            return currentEvasion * 0.7f;
        }
        return currentEvasion;
    }
}
