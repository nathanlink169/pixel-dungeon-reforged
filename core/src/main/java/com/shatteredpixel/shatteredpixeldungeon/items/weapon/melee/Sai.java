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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Combo;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Sai extends MeleeWeapon {

	{
		image = ItemSpriteSheet.SAI;
		hitSound = Assets.Sounds.HIT_STAB;
		hitSoundPitch = 1.3f;

		tier = 3;

		damageType = DamageType.of(DamageType.PIERCING);
	}

	@Override
	public int max(int lvl) {
		return  Math.round(2.5f*(tier+1)) +     //10 base, down from 20
				lvl*Math.round(0.5f*(tier+1));  //+2 per level, down from +4
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(4+lvl) damage, roughly +60% base damage, +67% scaling
		int dmgBoost = augment.damageFactor(4 + buffedLvl());
		Sai.comboStrikeAbility(hero, target, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? 4 + buffedLvl() : 4;
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", augment.damageFactor(dmgBoost));
		}
	}

	public String upgradeAbilityStat(int level){
		return "+" + augment.damageFactor(4 + level);
	}

	public static void comboStrikeAbility(Hero hero, Integer target, int boostPerHit, MeleeWeapon wep) {
		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(wep, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = wep;
		if (!hero.canAttack(enemy)) {
			GLog.w(Messages.get(wep, "ability_target_range"));
			hero.belongings.abilityWeapon = null;
			return;
		}
		hero.belongings.abilityWeapon = null;

		hero.sprite.attack(enemy.pos, new Callback() {
			@Override
			public void call() {
				wep.beforeAbilityUsed(hero, enemy);
				AttackIndicator.target(enemy);

				// Get the tracker and activate it for this ability
				ComboStrikeTracker tracker = hero.buff(ComboStrikeTracker.class);
				int recentHits = 0;

				if (tracker != null) {
					recentHits = tracker.hits;
					// Activate the tracker for this attack
					tracker.activeAbility = true;
					tracker.abilityDamageBoost = boostPerHit;
				}

				// Build attack context
				AttackContext context = new AttackContext.Builder(hero, enemy)
						.attackType(AttackContext.AttackType.MELEE)
						.damageType(DamageType.of(DamageType.PIERCING))
						.build();

				// Resolve through new combat system
				AttackResult result = CombatResolver.resolve(context);

				// Clean up tracker
				if (tracker != null) {
					tracker.activeAbility = false;
					tracker.abilityDamageBoost = 0;
				}

				boolean hit = result.result == AttackResult.ResultType.HIT;

				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());

				if (recentHits >= 2 && hit) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				}

				wep.afterAbilityUsed(hero);
			}
		});
	}

	@Override
	public float timeToUse() {
		return super.timeToUse() * 0.5f;
	}

	// Refactored ComboStrikeTracker - now implements CombatModifier interfaces
	public static class ComboStrikeTracker extends Buff implements
			CombatModifier.AccuracyModifier,
			CombatModifier.PreArmorDamageModifier {

		{
			type = buffType.POSITIVE;
		}

		public static int DURATION = 5;
		private float comboTime = 0f;
		public int hits = 0;

		// NEW: Track if this is being used for an ability
		public boolean activeAbility = false;
		public int abilityDamageBoost = 0;

		@Override
		public int priority() {
			return CombatModifier.Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			// Only applies during ability usage
			if (!activeAbility) return false;
			if (context.attacker != target) return false;
			return true;
		}

		@Override
		public float modifyAccuracy(AttackContext context, float currentAccuracy) {
			// Infinite accuracy during ability
			if (activeAbility) {
				return Char.INFINITE_ACCURACY;
			}
			return currentAccuracy;
		}

		@Override
		public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
			if (activeAbility && hits > 0) {
				// Apply the ability's damage scaling
				int totalBoost = abilityDamageBoost * hits;
				return currentDamage + totalBoost;
			}
			return currentDamage;
		}

		@Override
		public int icon() {
			if (Dungeon.hero.belongings.weapon() instanceof Gloves
					|| Dungeon.hero.belongings.weapon() instanceof Sai
					|| Dungeon.hero.belongings.weapon() instanceof Gauntlet
					|| Dungeon.hero.belongings.secondWep() instanceof Gloves
					|| Dungeon.hero.belongings.secondWep() instanceof Sai
					|| Dungeon.hero.belongings.secondWep() instanceof Gauntlet) {
				return BuffIndicator.DUEL_COMBO;
			} else {
				return BuffIndicator.NONE;
			}
		}

		@Override
		public boolean act() {
			comboTime -= TICK;
			spend(TICK);
			if (comboTime <= 0) {
				detach();
			}
			return true;
		}

		// Called by normal attacks to build combo
		public void addHit() {
			hits++;
			comboTime = 5f;

			if (hits >= 2 && icon() != BuffIndicator.NONE) {
				GLog.p(Messages.get(Combo.class, "combo", hits));
			}
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (DURATION - comboTime) / DURATION);
		}

		@Override
		public String iconTextDisplay() {
			return Integer.toString((int)comboTime);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", hits, dispTurns(comboTime));
		}

		// Bundle code stays the same...
		private static final String TIME = "combo_time";
		public static String RECENT_HITS = "recent_hits";
		private static final String ACTIVE = "active_ability";
		private static final String BOOST = "ability_boost";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(TIME, comboTime);
			bundle.put(RECENT_HITS, hits);
			bundle.put(ACTIVE, activeAbility);
			bundle.put(BOOST, abilityDamageBoost);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			comboTime = bundle.getFloat(TIME);
			hits = bundle.getInt(RECENT_HITS);
			activeAbility = bundle.getBoolean(ACTIVE);
			abilityDamageBoost = bundle.getInt(BOOST);
		}
	}

}
