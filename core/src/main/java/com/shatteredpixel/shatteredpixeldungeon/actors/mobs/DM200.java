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
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.CorrosiveGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Vertigo;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.TenguDartTrap;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class DM200 extends Mob {
	{
		HUNTING = new Hunting();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.dm200; }

	@Override
	protected void addProperty(Property p) {
		if (p == Property.LARGE && getRandomizerEnabled(RandomTraits.COMPACT_DESIGN)) {
			return;
		}
		super.addProperty(p);
	}
	@Override
	public float speed() {
		return super.speed() * (getRandomizerEnabled(RandomTraits.RUSTED_GEARS) ? 0.25f : 1.0f);
	}

	@Override
	public float GetLootChance(int slot){
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.GetLootChance(slot) * (float)Math.pow(1/3f, Dungeon.LimitedDrops.DM200_EQUIP.count);
	}

	public Item createLoot(int slot) {
		Dungeon.LimitedDrops.DM200_EQUIP.count++;
		Object loot = null;

		switch(slot) {
			case 0:
				loot = GetConstants().getLoot().getLoot1();
				break;
			case 1:
				loot = GetConstants().getLoot().getLoot2();
				break;
			case 2:
				loot = GetConstants().getLoot().getLoot3();
				break;
		}

		//uses probability tables for dwarf city
		if (loot == Generator.Category.WEAPON){
			return Generator.randomWeapon(4, true);
		} else {
			return Generator.randomArmor(4);
		}
	}

	public int GetNextWeapon() {
		return m_NextWeapon.Get();
	}

	private BundleableProperty.Int m_VentCooldown = new BundleableProperty.Int("vent_cooldown", 0);
	private BundleableProperty.Int m_NextWeapon = new BundleableProperty.Int("next_weapon", 1);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_VentCooldown.Store(bundle);
		m_NextWeapon.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_VentCooldown.Restore(bundle);
		m_NextWeapon.Restore(bundle);
	}

	@Override
	protected boolean act() {
		m_VentCooldown.Set(m_VentCooldown.Get() - 1);
		return super.act();
	}

	public void onZapComplete(){
		zap();
		next();
	}

	private void zap( ){
		spend( TICK );

		if (m_NextWeapon.Get() == 1) {
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
			final Ballistica shot = new Ballistica( pos, m_Target.Get(), Ballistica.PROJECTILE);
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
					if (ch.alignment != Char.Alignment.ALLY) ch.Damage(Random.Int(2, 8), this, DamageType.of(DamageType.EXPLOSIVE));

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
				ch.Damage(Random.Int(2, 8), this, DamageType.of(DamageType.EXPLOSIVE));

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
			m_VentCooldown.Set(Random.Int(10, 20));
			m_NextWeapon.Set(2);
		} else {
			m_VentCooldown.Set(Random.Int(20, 30));
			m_NextWeapon.Set(1);
		}
	}

	protected boolean canVent(int target){
		if (m_VentCooldown.Get() > 0) return false;
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

	private static class Hunting extends Mob.Hunting{

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			DM200 dm = (DM200) mob;
			if (!enemyInFOV || dm.canAttack(dm.enemy)) {
				return super.act(dm, enemyInFOV, justAlerted);
			} else {
				dm.m_EnemySeen.Set(true);
				dm.m_Target.Set(dm.enemy.pos);

				int oldPos = dm.pos;

				if (dm.distance(dm.enemy) >= 1 && Random.Int(100/dm.distance(dm.enemy)) == 0 && dm.canVent(dm.m_Target.Get())){
					if (dm.sprite != null && (dm.sprite.visible || dm.enemy.sprite.visible)) {
						dm.sprite.zap( dm.enemy.pos );
						return false;
					} else {
						dm.zap();
						return true;
					}

				} else if (!getRandomizerEnabled(RandomTraits.COMPACT_DESIGN) && dm.getCloser( dm.m_Target.Get() )) {
					// Prioritize moving closer if we're not able to move through corridors
					dm.spend( 1 / dm.speed() );
					return dm.moveSprite( oldPos,  dm.pos );

				} else if (dm.canVent(dm.m_Target.Get())) {
					// If we can move through corridors, prioritize venting
					if (dm.sprite != null && (dm.sprite.visible || dm.enemy.sprite.visible)) {
						dm.sprite.zap( dm.enemy.pos );
						return false;
					} else {
						dm.zap();
						return true;
					}

				} else if (getRandomizerEnabled(RandomTraits.COMPACT_DESIGN) && dm.getCloser( dm.m_Target.Get() )) {
					dm.spend( 1 / dm.speed() );
					return dm.moveSprite( oldPos,  dm.pos );

				} else {
					dm.spend( TICK );
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