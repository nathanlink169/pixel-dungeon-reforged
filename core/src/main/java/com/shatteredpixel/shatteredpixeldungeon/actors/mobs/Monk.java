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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Paralysis;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Imp;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.SkeletonKey;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MonkSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Monk extends Mob {
	
	{
		HP = HT = 70;
		defenseSkill = 30;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Food.class;
		lootChance = 0.083f;

		properties.add(Property.UNDEAD);
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return MonkSprite.class;
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 25;
		return Random.NormalIntRange( 12, 25 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 30;
	}
	
	@Override
	public float attackDelay() {
		return super.attackDelay()*0.5f;
	}
	
	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}
	
	@Override
	public void rollToDropLoot() {
		Imp.Quest.process( this );
		
		super.rollToDropLoot();
	}
	
	protected float focusCooldown = 0;
	
	@Override
	protected boolean act() {
		boolean result = super.act();
		if (buff(Focus.class) == null && state == HUNTING && focusCooldown <= 0 && buff(Blindness.class) == null) {
			Buff.affect( this, Focus.class );
		}
		return result;
	}
	
	@Override
	protected void spend( float time ) {
		if (buff(Blindness.class) == null) {
			if (getRandomizerEnabled(RandomTraits.DISTRACTED_MIND)) {
				focusCooldown -= time / 2;
			} else {
				focusCooldown -= time;
			}
		}
		super.spend( time );
	}
	
	@Override
	public void move( int step, boolean travelling) {
		// moving reduces cooldown by an additional 0.67, giving a total reduction of 1.67f.
		// basically monks will become focused notably faster if you kite them.
		if (travelling) {
			focusCooldown -= 0.67f;
			if (getRandomizerEnabled(RandomTraits.RAPID_MEDITATION)) {
				focusCooldown -= 1.67f; // double the total reduction when moving
			}
		}
		super.move( step, travelling);
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		damage = super.attackProc( enemy, damage );

		if (getRandomizerEnabled(RandomTraits.STUNNING_STRIKES) && enemy.buff(Paralysis.class) == null) {
			// 1 in 20 chance, but attacks twice a turn so each attack is half a chance
			if (damage > 0 && Random.Int(40) == 0) {
				Buff.affect(enemy, Paralysis.class, 1.0f);
			}
		}

		return damage;
	}
	
	@Override
	public int defenseSkill( Char enemy ) {
		if (buff(Focus.class) != null && paralysed == 0 && state != SLEEPING) {
			if (surprisedBy(enemy) && getRandomizerEnabled(RandomTraits.UNFOCUSED_DEFENSE)) {
				return super.defenseSkill( enemy );
			}
			return INFINITE_EVASION;
		}
		return super.defenseSkill( enemy );
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
			focusCooldown = Random.NormalFloat( 6, 7 );
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
	
	private static String FOCUS_COOLDOWN = "focus_cooldown";
	
	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( FOCUS_COOLDOWN, focusCooldown );
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		focusCooldown = bundle.getInt( FOCUS_COOLDOWN );
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