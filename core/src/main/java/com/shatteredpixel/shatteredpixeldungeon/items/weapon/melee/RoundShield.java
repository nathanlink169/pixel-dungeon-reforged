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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class RoundShield extends MeleeWeapon {

	{
		image = ItemSpriteSheet.ROUND_SHIELD;
		hitSound = Assets.Sounds.HIT;
		hitSoundPitch = 1f;

		tier = 3;

		damageType = DamageType.of(DamageType.BLUDGEONING);
	}

	@Override
	public int max(int lvl) {
		return  Math.round(3f*(tier+1)) +   //12 base, down from 20
				lvl*(tier-1);               //+2 per level, down from +4
	}

	public int DRMax(){
		return DRMax(buffedLvl());
	}

	//4 extra defence, plus 1 per level
	public int DRMax(int lvl){
		return 4 + lvl;
	}
	
	public String statsInfo(){
		if (isIdentified()){
			return Messages.get(this, "stats_desc", 4+buffedLvl());
		} else {
			return Messages.get(this, "typical_stats_desc", 4);
		}
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		RoundShield.guardAbility(hero, 5+buffedLvl(), this);
	}

	@Override
	public String abilityInfo() {
		if (levelKnown){
			return Messages.get(this, "ability_desc", 5+buffedLvl());
		} else {
			return Messages.get(this, "typical_ability_desc", 5);
		}
	}

	@Override
	public String upgradeAbilityStat(int level) {
		return Integer.toString(5 + level);
	}

	public static void guardAbility(Hero hero, int duration, MeleeWeapon wep){
		wep.beforeAbilityUsed(hero, null);
		Buff.prolong(hero, GuardTracker.class, duration).hasBlocked = false;
		hero.sprite.operate(hero.pos);
		hero.spendAndNext(Actor.TICK);
		wep.afterAbilityUsed(hero);
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		if (context.defender.getWeapon() == this) {
			currentDamage -= Random.Int(DRMax());
			if (currentDamage < 0) {
				currentDamage = 0;
			}
		}

		return super.modifyPreArmorDamage(context, currentDamage);
	}

	public static class GuardTracker extends FlavourBuff implements CombatModifier.EvasionModifier {

		{
			announced = true;
			type = buffType.POSITIVE;
		}

		public boolean hasBlocked = false;

		@Override
		public int icon() {
			return BuffIndicator.DUEL_GUARD;
		}

		@Override
		public void tintIcon(Image icon) {
			if (hasBlocked){
				icon.tint(0x651f66, 0.5f);
			} else {
				icon.resetColor();
			}
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (5 - visualcooldown()) / 5);
		}

		private static final String BLOCKED = "blocked";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(BLOCKED, hasBlocked);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			hasBlocked = bundle.getBoolean(BLOCKED);
		}

		@Override
		public float modifyEvasion(AttackContext context, float currentEvasion) {
			return Char.INFINITE_EVASION;
		}

		@Override
		public int priority() {
			return Priority.NORMAL;
		}

		@Override
		public boolean appliesTo(AttackContext context) {
			return context.defender == target;
		}
	}
}