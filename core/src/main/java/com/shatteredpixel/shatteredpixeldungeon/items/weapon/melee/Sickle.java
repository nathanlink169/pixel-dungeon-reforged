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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackResult;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.AttackIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

import java.util.ArrayList;

public class Sickle extends MeleeWeapon implements CombatModifier.AccuracyModifier {

	{
		image = ItemSpriteSheet.SICKLE;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1f;

		tier = 2;

		damageType = DamageType.of(DamageType.SLASHING);
	}

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		if (context.attacker.getWeapon() == this) {
			return currentAccuracy * 0.68f;
		}
		return currentAccuracy;
	}

	@Override
	public int max(int lvl) {
		return  Math.round(6.67f*(tier+1)) +    //20 base, up from 15
				lvl*(tier+1);                   //scaling unchanged
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//replaces damage with 15+2.5*lvl bleed, roughly 138% avg base dmg, 125% avg scaling
		int bleedAmt = augment.damageFactor(Math.round(15f + 2.5f*buffedLvl()));
		Sickle.harvestAbility(hero, target, bleedAmt, this);
	}

	@Override
	public String abilityInfo() {
		int bleedAmt = levelKnown ? Math.round(15f + 2.5f*buffedLvl()) : 15;
		if (levelKnown){
			return Messages.get(this, "ability_desc", augment.damageFactor(bleedAmt));
		} else {
			return Messages.get(this, "typical_ability_desc", bleedAmt);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(augment.damageFactor(Math.round(15f + 2.5f*level)));
	}

	public static void harvestAbility(Hero hero, Integer target, int bleedBoost, MeleeWeapon wep){

		if (target == null) {
			return;
		}

		Char enemy = Actor.findChar(target);
		if (enemy == null || enemy == hero || hero.isCharmedBy(enemy) || !Dungeon.level.heroFOV[target]) {
			GLog.w(Messages.get(wep, "ability_no_target"));
			return;
		}

		hero.belongings.abilityWeapon = wep;
		if (!hero.canAttack(enemy)){
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

				Buff.affect(enemy, HarvestBleedTracker.class, 0).dmgBoost = bleedBoost;
				// Build attack context
				AttackContext context = new AttackContext.Builder(hero, enemy)
						.attackType(AttackContext.AttackType.RANGED)
						.damageType(DamageType.of(DamageType.SLASHING))
						.build();

				// Resolve attack - this handles EVERYTHING internally
				AttackResult result = CombatResolver.resolve(context);
				if (result.result == AttackResult.ResultType.HIT) {
					Sample.INSTANCE.play(Assets.Sounds.HIT_STRONG);
				}

				Invisibility.dispel();
				hero.spendAndNext(hero.attackDelay());
				wep.afterAbilityUsed(hero);
			}
		});

	}

	public static class HarvestBleedTracker extends FlavourBuff implements
			CombatModifier.AccuracyModifier,
			CombatModifier.PostArmorDamageModifier,
			CombatModifier.OnHitEffect {

		private boolean consumed = false;
		private int bleedDamage = 0;
		public int dmgBoost = 0;

		@Override
		public int priority() {
			return Priority.LOWEST; // Get the post armour damage AFTER all the modifiers
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			// Only apply to the single attack this buff is attached for
			return !consumed && context.defender == target;
		}

		@Override
		public float modifyAccuracy(AttackContext context, float currentAccuracy) {
			// Always hit
			return Char.INFINITE_ACCURACY;
		}

		@Override
		public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
			bleedDamage = currentDamage + dmgBoost;
			return 0;
		}

		@Override
		public void onHit(AttackContext context, int finalDamage) {
			if (context.defender.isImmune(Bleeding.class)) {
				consumed = true;
				detach();
				return;
			}

			Bleeding b = context.defender.buff(Bleeding.class);
			if (b == null) {
				b = new Bleeding();
				b.attachTo(context.defender);
			}
			b.announced = false;
			b.set(bleedDamage, HarvestBleedTracker.class);
			bleedDamage = 0;

			// Show bleed status
			if (context.defender.sprite != null) {
				context.defender.sprite.showStatus(
						CharSprite.WARNING,
						Messages.titleCase(b.name()) + " " + (int)b.level()
				);
			}

			consumed = true;
			detach();
		}
	}

}
