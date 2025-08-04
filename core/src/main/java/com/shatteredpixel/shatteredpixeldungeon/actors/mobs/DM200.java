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

import static com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave.throwChar;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ConfusionGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DM200Sprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM200 extends Mob {

	{
		HP = HT = 80;
		defenseSkill = 12;

		EXP = 9;
		maxLvl = 17;

		loot = Random.oneOf(Generator.Category.WEAPON, Generator.Category.ARMOR);
		lootChance = 0.2f; //initially, see lootChance()

		baseSpeed = getRandomizerEnabled(RandomTraits.RUSTED_GEARS) ? 0.25f : 1.0f;

		properties.add(Property.INORGANIC);

		if (!getRandomizerEnabled(RandomTraits.COMPACT_DESIGN)) {
			properties.add(Property.LARGE);
		}

		HUNTING = new Hunting();
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return DM200Sprite.class;
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 25;
		return Random.NormalIntRange( 10, 25 );
	}

	@Override
	public int attackSkill( Char target ) {
		return 20;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 8);
	}

	@Override
	public float lootChance(){
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.DM200_EQUIP.count);
	}

	public Item createLoot() {
		Dungeon.LimitedDrops.DM200_EQUIP.count++;
		//uses probability tables for dwarf city
		if (loot == Generator.Category.WEAPON){
			return Generator.randomWeapon(4, true);
		} else {
			return Generator.randomArmor(4);
		}
	}

	private int ventCooldown = 0;
	public int nextWeapon = 1;

	private static final String VENT_COOLDOWN = "vent_cooldown";
	private static final String NEXT_WEAPON = "next_weapon";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VENT_COOLDOWN, ventCooldown);
		bundle.put(NEXT_WEAPON, nextWeapon);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		ventCooldown = bundle.getInt( VENT_COOLDOWN );
		nextWeapon = bundle.getInt(NEXT_WEAPON);
	}

	@Override
	protected boolean act() {
		ventCooldown--;
		return super.act();
	}

	public void onZapComplete(){
		zap();
		next();
	}

	private void zap( ){
		spend( TICK );

		if (nextWeapon == 1) {
			Ballistica trajectory = new Ballistica(pos, enemy.pos, Ballistica.STOP_TARGET);

			for (int i : trajectory.subPath(0, trajectory.dist)) {
				GameScene.add(Blob.seed(i, 20, ToxicGas.class));
				if (getRandomizerEnabled(RandomTraits.DUAL_PAYLOAD)) {
					GameScene.add(Blob.seed(i, 15, CorrosiveGas.class).setStrength(6));
				}
			}
			GameScene.add(Blob.seed(trajectory.collisionPos, 100, ToxicGas.class));
			if (getRandomizerEnabled(RandomTraits.DUAL_PAYLOAD)) {
				GameScene.add(Blob.seed(trajectory.collisionPos, 100, CorrosiveGas.class).setStrength(8));
			}
		}
		else {
			final Ballistica shot = new Ballistica( pos, target, Ballistica.PROJECTILE);
			WandOfBlastWave.BlastWave.blast(shot.collisionPos);
			Sample.INSTANCE.play( Assets.Sounds.BLAST );

			//presses all tiles in the AOE first, with the exception of tengu dart traps
			for (int i : PathFinder.NEIGHBOURS9){
				if (!(Dungeon.level.traps.get(shot.collisionPos+i) instanceof TenguDartTrap)) {
					Dungeon.level.pressCell(shot.collisionPos + i);
				}
			}

			//throws other chars around the center.
			for (int i  : PathFinder.NEIGHBOURS8){
				Char ch = Actor.findChar(shot.collisionPos + i);

				if (ch != null){
					if (ch.alignment != Char.Alignment.ALLY) ch.damage(Random.Int(2, 8), this);

					//do not push chars that are dieing over a pit, or that move due to the damage
					if ((ch.isAlive() || ch.flying || !Dungeon.level.pit[ch.pos])
							&& ch.pos == shot.collisionPos + i) {
						Ballistica trajectory = new Ballistica(ch.pos, ch.pos + i, Ballistica.MAGIC_BOLT);
						int strength = Random.Int(2, 3);
						throwChar(ch, trajectory, strength, false, true, this);
						Buff.prolong(ch, Vertigo.class, 4);
					}

				}
			}

			//throws the char at the center of the blast
			Char ch = Actor.findChar(shot.collisionPos);
			if (ch != null){
				ch.damage(Random.Int(2, 8), this, DamageType.MAGIC);

				//do not push chars that are dieing over a pit, or that move due to the damage
				if ((ch.isAlive() || ch.flying || !Dungeon.level.pit[ch.pos])
						&& shot.path.size() > shot.dist+1 && ch.pos == shot.collisionPos) {
					Ballistica trajectory = new Ballistica(ch.pos, shot.path.get(shot.dist + 1), Ballistica.MAGIC_BOLT);
					int strength = Random.Int(2, 3);
					throwChar(ch, trajectory, strength, false, true, this);
					Buff.prolong(ch, Vertigo.class, 10);
				}
			}
		}

		if (getRandomizerEnabled(RandomTraits.CONCUSSION_CANNON) && Random.Int(2) == 0) {
			ventCooldown = Random.Int(10, 20);
			nextWeapon = 2;
		} else {
			ventCooldown = Random.Int(20, 30);
			nextWeapon = 1;
		}
	}

	protected boolean canVent(int target){
		if (ventCooldown > 0) return false;
		PathFinder.buildDistanceMap(target, BArray.not(Dungeon.level.solid, null), Dungeon.level.distance(pos, target)+1);
		//vent can go around blocking terrain, but not through it
		if (PathFinder.distance[pos] == Integer.MAX_VALUE){
			return false;
		}
		if (PathFinder.distance[pos] > 5 && getRandomizerEnabled(RandomTraits.SHORT_RANGE)) {
			return false;
		}
		return true;
	}

	private class Hunting extends Mob.Hunting{

		@Override
		public boolean act(boolean enemyInFOV, boolean justAlerted) {
			if (!enemyInFOV || canAttack(enemy)) {
				return super.act(enemyInFOV, justAlerted);
			} else {
				enemySeen = true;
				target = enemy.pos;

				int oldPos = pos;

				if (distance(enemy) >= 1 && Random.Int(100/distance(enemy)) == 0 && canVent(target)){
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						zap();
						return true;
					}

				} else if (getCloser( target ) && !getRandomizerEnabled(RandomTraits.COMPACT_DESIGN)) {
					// Prioritize moving closer if we're not able to move through corridors
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else if (canVent(target)) {
					// If we can move through corridors, prioritize venting
					if (sprite != null && (sprite.visible || enemy.sprite.visible)) {
						sprite.zap( enemy.pos );
						return false;
					} else {
						zap();
						return true;
					}

				} else if (getCloser( target ) && getRandomizerEnabled(RandomTraits.COMPACT_DESIGN)) {
					spend( 1 / speed() );
					return moveSprite( oldPos,  pos );

				} else {
					spend( TICK );
					return true;
				}

			}
		}
	}

	public enum RandomTraits {
		DUAL_PAYLOAD, COMPACT_DESIGN, CONCUSSION_CANNON, RUSTED_GEARS, PRODUCTION_HALT, SHORT_RANGE
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case DUAL_PAYLOAD: return Randomizer.getCreatureBuff(DM200.class) == 1;
			case COMPACT_DESIGN: return Randomizer.getCreatureBuff(DM200.class) == 2;
			case CONCUSSION_CANNON: return Randomizer.getCreatureBuff(DM200.class) == 3;
			case RUSTED_GEARS: return Randomizer.getCreatureNerf(DM200.class) == 1;
			case PRODUCTION_HALT: return Randomizer.getCreatureNerf(DM200.class) == 2;
			case SHORT_RANGE: return Randomizer.getCreatureNerf(DM200.class) == 3;
		}
		return false;
	}
}