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

import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class GnollTrickster extends Gnoll implements CombatModifier.OnHitEffect {

	{
		WANDERING = new Wandering();
		state = WANDERING;
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.gnolltrickster; }

	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( pos, enemy.pos )
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}

	@Override
    public boolean getCloser(int target) {
		m_Combo.Set(0); //if he's moving, he isn't attacking, reset combo.
		if (state == HUNTING) {
			return m_EnemySeen.Get() && getFurther( target );
		} else {
			return super.getCloser( target );
		}
	}

	@Override
	public void aggro(Char ch) {
		//cannot be aggroed to something it can't see
		//skip this check if FOV isn't initialized
		if (ch == null || fieldOfView == null
				|| fieldOfView.length != Dungeon.level.length() || fieldOfView[ch.pos]) {
			super.aggro(ch);
		}
	}
	
	@Override
	public Item createLoot(int itemSlot) {
		MissileWeapon drop = (MissileWeapon)super.createLoot(itemSlot);
		//half quantity, rounded up
		drop.quantity((drop.quantity()+1)/2);
		return drop;
	}
	
	@Override
	public void die( Object cause ) {
		super.die( cause );

		Ghost.Quest.process();
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		if (m_Combo.Get() >= 1){
			//score loss is on-hit instead of on-attack as it's tied to combo
			Statistics.questScores[0] -= 50;
		}

		//The gnoll's attacks get more severe the more the player lets it hit them
		m_Combo.Increment();
		int effect = Random.Int(4)+m_Combo.Get();

		if (effect > 2) {

			if (effect >=6 && enemy.buff(Burning.class) == null){

				if (Dungeon.level.flamable[enemy.pos]) {
					GameScene.add(Blob.seed(enemy.pos, 4, Fire.class));
				}
				Buff.affect(enemy, Burning.class).reignite( enemy );

			} else {
				Buff.affect(enemy, Poison.class).set((effect - 2));
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

	protected static class Wandering extends Mob.Wandering{
		@Override
		protected int randomDestination(Mob mob) {
			//of two potential wander positions, picks the one closest to the hero
			int pos1 = super.randomDestination(mob);
			int pos2 = super.randomDestination(mob);
			PathFinder.buildDistanceMap(Dungeon.hero.pos, Dungeon.level.passable);
			if (PathFinder.distance[pos2] < PathFinder.distance[pos1]){
				return pos2;
			} else {
				return pos1;
			}
		}
	}

	private BundleableProperty.Int m_Combo = new BundleableProperty.Int("combo", 0);

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
		m_Combo.Store(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Combo.Restore(bundle);
	}

}
