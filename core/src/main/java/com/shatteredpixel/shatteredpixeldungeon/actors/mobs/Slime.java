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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Slow;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.quest.GooBlob;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SlimeSprite;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class Slime extends Mob {
	
	{
		HP = HT = 20;
		defenseSkill = 5;
		
		EXP = 4;
		maxLvl = 9;
		
		lootChance = 0.2f; //by default, see lootChance()
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return SlimeSprite.class;
	}

	private boolean stealthy = false;

	private static final String STEALTHY= "stealthy";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( STEALTHY, stealthy );
	}

	@SuppressWarnings("unchecked")
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		stealthy = bundle.getBoolean(STEALTHY);
	}

	@Override
	protected void onAdd(){
		boolean previousFirstAdded = firstAdded;
		super.onAdd();
		if (previousFirstAdded) {
			stealthy = getRandomizerEnabled(RandomTraits.CHAMELEON_OOZE);
		}
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 5;
		return Random.NormalIntRange( 2, 5 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int attackProc( Char enemy, int damage ) {
		if (getRandomizerEnabled(RandomTraits.STICKY_COATING)) {
			if (Random.Int(2) == 0) {
				Buff.affect(enemy, Slow.class);
			}
		}
		if (stealthy()) {
			stealthy = false;
			((SlimeSprite) sprite).unhide();
		}

		return super.attackProc( enemy, damage );
	}
	
	@Override
	public void damage(int dmg, Object src, int damageType) {
		float scaleFactor = AscensionChallenge.statModifier(this);
		int scaledDmg = Math.round(dmg/scaleFactor);
		int damageThreshold = 6;
		if (getRandomizerEnabled(RandomTraits.ENHANCED_RESILIENCE)) damageThreshold = 4;
		if (getRandomizerEnabled(RandomTraits.SOFTENED_MEMBRANE)) damageThreshold = 10;

		if (scaledDmg >= damageThreshold - 1){
			//takes 5/6/7/8/9/10 dmg at 5/7/10/14/19/25 incoming dmg
			scaledDmg = (damageThreshold - 2) + (int)(Math.sqrt((2 * (damageThreshold - 2))*(scaledDmg - (damageThreshold - 2)) + 1) - 1)/2;
		}
		dmg = (int)(scaledDmg*AscensionChallenge.statModifier(this));

		if (getRandomizerEnabled(RandomTraits.BLADE_WEAKNESS) && DamageType.getIsDamageType(damageType, DamageType.SLASHING)) {
			dmg *= 3; // happens after scaling damage
		}
		super.damage(dmg, src, damageType);
	}

	@Override
	public float lootChance(){
		//each drop makes future drops 1/4 as likely
		// so loot chance looks like: 1/5, 1/20, 1/80, 1/320, etc.
		return super.lootChance() * (float)Math.pow(1/4f, Dungeon.LimitedDrops.SLIME_WEP.count);
	}
	
	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.SLIME_WEP.count++;
		Generator.Category c = Generator.Category.WEP_T2;
		MeleeWeapon w = (MeleeWeapon)Generator.randomUsingDefaults(Generator.Category.WEP_T2);
		w.level(0);
		return w;
	}

	// as mimic
	public boolean stealthy(){
		return stealthy;
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		int damageThreshold = 6;
		if (getRandomizerEnabled(RandomTraits.ENHANCED_RESILIENCE)) damageThreshold = 4;
		if (getRandomizerEnabled(RandomTraits.SOFTENED_MEMBRANE)) damageThreshold = 10;


		String desc = Messages.get(this, "desc", damageThreshold);
		if (Dungeon.isChallenged(Challenges.MONSTER_UNKNOWN)) {
			desc = Messages.get(this, "desc_unknown");
		}
		return desc;
	}

	@Override
	public CharSprite sprite() {
		SlimeSprite sprite = (SlimeSprite) super.sprite();
		if (stealthy()) sprite.hide(this);
		return sprite;
	}

	public enum RandomTraits {
		ENHANCED_RESILIENCE, STICKY_COATING, CHAMELEON_OOZE, CAUSTIC_CARRIERS, SOFTENED_MEMBRANE, BLADE_WEAKNESS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case ENHANCED_RESILIENCE: return Randomizer.getCreatureBuff(Slime.class) == 1;
			case STICKY_COATING: return Randomizer.getCreatureBuff(Slime.class) == 2;
			case CHAMELEON_OOZE: return Randomizer.getCreatureBuff(Slime.class) == 3;
			case CAUSTIC_CARRIERS: return Randomizer.getCreatureNerf(Slime.class) == 1;
			case SOFTENED_MEMBRANE: return Randomizer.getCreatureNerf(Slime.class) == 2;
			case BLADE_WEAKNESS: return Randomizer.getCreatureNerf(Slime.class) == 3;
		}
		return false;
	}

	@Override
	public void rollToDropLoot() {
		if (!getRandomizerEnabled(RandomTraits.CAUSTIC_CARRIERS)) {
			super.rollToDropLoot();
			return;
		}

		if (Dungeon.hero.lvl > maxLvl + 2) return;

		super.rollToDropLoot();

		int ofs;
		do {
			ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
		Dungeon.level.drop( new GooBlob(), pos + ofs ).sprite.drop( pos );
	}
}