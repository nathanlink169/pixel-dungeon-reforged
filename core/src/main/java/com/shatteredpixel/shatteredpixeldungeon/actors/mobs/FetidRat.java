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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.StenchGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Ooze;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class FetidRat extends Rat implements CombatModifier.OnDamageEffect {

	{
		WANDERING = new Wandering();
		state = WANDERING;
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.fetidrat; }

	@Override
	public void die( Object cause ) {
		super.die( cause );

		Ghost.Quest.process();
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.attacker == this) {
			if (Random.Int(3) == 0) {
				Buff.affect(context.defender, Ooze.class).set( Ooze.DURATION );
				//score loss is on-hit instead of on-attack because it's tied to ooze
				if (context.defender == Dungeon.hero && !Dungeon.level.water[context.defenderPosition]){
					Statistics.questScores[0] -= 50;
				}
			}
		} else {
			GameScene.add(Blob.seed(context.defenderPosition, 20, StenchGas.class));
		}

		if (super.appliesTo(context)) {
			super.onDamage(context, damageDealt);
		}
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this || context.defender == this;
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
	
	{
		immunities.add( StenchGas.class );
	}
}