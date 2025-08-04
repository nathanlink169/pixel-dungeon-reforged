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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.artificer;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Awareness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.warrior.Endure;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.CheckedCell;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfHaste;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.HeroIcon;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;

public class Reflection extends ArmorAbility {

	{
		baseChargeUse = 40f;
	}

	@Override
	protected void activate(ClassArmor armor, Hero hero, Integer target) {
		if (hero.buff(ReflectionTracker.class) != null){
			hero.buff(ReflectionTracker.class).detach();
		}
		Buff.prolong(hero, ReflectionTracker.class, 10f);
		hero.sprite.operate(hero.pos);

		armor.charge -= chargeUse(hero);
		armor.updateQuickslot();

		Invisibility.dispel();
		hero.spendAndNext(Actor.TICK);
	}

	public static class ReflectionTracker extends FlavourBuff {

		{
			type = buffType.POSITIVE;
		}

		@Override
		public int icon() {
			return BuffIndicator.ARMOR;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(1, 0, 0);
		}

		@Override
		public float iconFadePercent() {
			return Math.max(0, (10f - visualcooldown()) / 10f);
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc");
		}

		// returns: the amount of damage after damage reduction
		public float handledamageTaken(Char enemy, float damage){
			if (enemy == null) return damage;

			float reflectionAmount = 0.2f + 0.1f * Dungeon.hero.pointsInTalent(Talent.POWERFUL_REFLECTION);
			enemy.damage((int) (damage * reflectionAmount), Dungeon.hero, DamageType.MAGIC);

			float damageMultiplier = 1.0f;
			switch (Dungeon.hero.pointsInTalent(Talent.ENDURANCE)) {
				case 1: damageMultiplier = 0.8f; break;
				case 2: damageMultiplier = 0.7f; break;
				case 3: damageMultiplier = 0.6f; break;
				case 4: damageMultiplier = 0.5f; break;
			}
			return damage * damageMultiplier;
		}
	}

	public static float speedMultiplier( Hero hero ) {
			return 1.0f + hero.pointsInTalent(Talent.ADRENALINE) * 0.15f;
	}

	@Override
	public int icon() {
		return HeroIcon.REFLECTION;
	}

	@Override
	public Talent[] talents() {
		return new Talent[]{Talent.ADRENALINE, Talent.ENDURANCE, Talent.POWERFUL_REFLECTION, Talent.HEROIC_ENERGY};
	}
}
