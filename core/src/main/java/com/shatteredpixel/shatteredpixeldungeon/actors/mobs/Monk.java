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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Monk extends Mob implements CombatModifier.OnDamageEffect {
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.monk; }

	@Override
	public void rollToDropLoot() {
		Imp.Quest.process( this );
		
		super.rollToDropLoot();
	}
	
	@Override
	protected boolean act() {
		boolean result = super.act();
		if (buff(Focus.class) == null && state == HUNTING && m_FocusCooldown.Get() <= 0 && buff(Blindness.class) == null) {
			Buff.affect( this, Focus.class );
		}
		return result;
	}

	@Override
    public void spend(float time) {
		if (buff(Blindness.class) == null) {
			if (getRandomizerEnabled(RandomTraits.DISTRACTED_MIND)) {
				m_FocusCooldown.Subtract(time / 2.0f);
			} else {
				m_FocusCooldown.Subtract(time);
			}
		}
		super.spend( time );
	}
	
	@Override
	public void move( int step, boolean travelling) {
		// moving reduces cooldown by an additional 0.67, giving a total reduction of 1.67f.
		// basically monks will become focused notably faster if you kite them.
		if (travelling) {
			m_FocusCooldown.Subtract(0.67f);
			if (getRandomizerEnabled(RandomTraits.RAPID_MEDITATION)) {
				m_FocusCooldown.Subtract(1.67f); // double the total reduction when moving
			}
		}
		super.move( step, travelling);
	}
	
	@Override
	public int defenseSkill() {
		if (buff(Focus.class) != null && paralysed == 0 && state != SLEEPING) {
			if (surprisedBy(enemy) && getRandomizerEnabled(RandomTraits.UNFOCUSED_DEFENSE)) {
				return super.defenseSkill();
			}
			return INFINITE_EVASION;
		}
		return super.defenseSkill();
	}
	
	@Override
	public String defenseVerb() {
		Focus f = buff(Focus.class);
		if (f == null) {
			return super.defenseVerb();
		} else {
			f.detach();
			if (sprite != null && sprite.visible) {
				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			}
			m_FocusCooldown.Set(Random.NormalFloat( 6, 7 ));
			return Messages.get(this, "parried");
		}
	}

	@Override
	public void die( Object cause ) {

		super.die( cause );

		if (getRandomizerEnabled(RandomTraits.SCHOLAR_MONKS) && Random.Int(4) == 0) {
			Item toDrop = Generator.random(Generator.Category.SCROLL);
			Dungeon.level.drop(toDrop, pos).sprite.drop(pos);
		}
	}

	protected BundleableProperty.Float m_FocusCooldown = new BundleableProperty.Float("focus_cooldown", 0.0f);
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_FocusCooldown.Store(bundle);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_FocusCooldown.Restore(bundle);
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (getRandomizerEnabled(RandomTraits.STUNNING_STRIKES) && enemy.buff(Paralysis.class) == null) {
			// 1 in 20 chance, but attacks twice a turn so each attack is half a chance
			if (Random.Int(40) == 0) {
				Buff.affect(enemy, Paralysis.class, 1.0f);
			}
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	public static class Focus extends Buff {
		
		{
			type = buffType.POSITIVE;
			announced = true;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.MIND_VISION;
		}

		@Override
		public void tintIcon(Image icon) {
			icon.hardlight(0.25f, 1.5f, 1f);
		}
	}

	public enum RandomTraits {
		RAPID_MEDITATION, SENIOR_PRESENCE, STUNNING_STRIKES, DISTRACTED_MIND, UNFOCUSED_DEFENSE, SCHOLAR_MONKS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case RAPID_MEDITATION: return Randomizer.getCreatureBuff(Monk.class) == 1;
			case SENIOR_PRESENCE: return Randomizer.getCreatureBuff(Monk.class) == 2;
			case STUNNING_STRIKES: return Randomizer.getCreatureBuff(Monk.class) == 3;
			case DISTRACTED_MIND: return Randomizer.getCreatureNerf(Monk.class) == 1;
			case UNFOCUSED_DEFENSE: return Randomizer.getCreatureNerf(Monk.class) == 2;
			case SCHOLAR_MONKS: return Randomizer.getCreatureNerf(Monk.class) == 3;
		}
		return false;
	}
}