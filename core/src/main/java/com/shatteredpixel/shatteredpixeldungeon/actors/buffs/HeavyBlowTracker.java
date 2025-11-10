/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
 *
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

package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal.WarriorShield;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.BitmapText;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;

public class HeavyBlowTracker extends Buff implements
		CombatModifier.AccuracyModifier,
		CombatModifier.PreArmorDamageModifier,
		CombatModifier.OnHitEffect {

	public int dmgBoost;
	public MeleeWeapon weapon;

	@Override
	public int priority() {
		return CombatModifier.Priority.HIGH;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		// Only applies to this specific attack
		if (context.attacker != target) return false;
		if (!(context.defender instanceof Mob)) return false;

		// Check if it's a surprise attack
		Mob mob = (Mob) context.defender;
		Hero hero = (Hero) context.attacker;

		// If NOT surprised, don't apply damage boost
		if (!mob.surprisedBy(hero)) {
			return false;
		}

		return true;
	}

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		// Always hit - return infinite accuracy
		return Char.INFINITE_ACCURACY;
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		// Add flat damage boost
		return currentDamage + dmgBoost;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		// Apply Daze if enemy survives
		if (context.defender.isAlive()) {
			Buff.affect(context.defender, Daze.class, Daze.DURATION);
		}
	}
}