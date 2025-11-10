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
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.spells.ClericSpell;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Ghost;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.EnumSet;

public class GreatCrab extends Crab {

	{
		WANDERING = new Wandering();
		state = WANDERING;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.greatcrab; }

	private int moving = 0;

	@Override
    public boolean getCloser(int target) {
		//this is used so that the crab remains slower, but still detects the player at the expected rate.
		moving++;
		if (moving < 3) {
			return super.getCloser( target );
		} else {
			moving = 0;
			return true;
		}

	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType ){
		//crab blocks all wand damage from the hero if it sees them.
		//Direct damage is negated, but add-on effects and environmental effects go through as normal.
		if (m_EnemySeen.Get()
				&& state != SLEEPING
				&& paralysed == 0
				&& (src instanceof Wand || src instanceof ClericSpell)
				&& enemy == Dungeon.hero
				&& enemy.invisible == 0){
			GLog.n( Messages.get(this, "noticed") );
			sprite.showStatus( CharSprite.NEUTRAL, Messages.get(this, "def_verb") );
			Sample.INSTANCE.play( Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
			Statistics.questScores[0] -= 50;
			return 0;
		} else {
			return super.Damage( dmg, src, damageType );
		}
	}

	@Override
	public int defenseSkill() {
		//crab blocks all melee attacks from its current target
		if (m_EnemySeen.Get()
				&& state != SLEEPING
				&& paralysed == 0
				&& enemy.invisible == 0){
			if (sprite != null && sprite.visible) {
				Sample.INSTANCE.play(Assets.Sounds.HIT_PARRY, 1, Random.Float(0.96f, 1.05f));
				GLog.n( Messages.get(this, "noticed") );
			}
			if (enemy == Dungeon.hero){
				Statistics.questScores[0] -= 50;
			}
			return INFINITE_EVASION;
		}
		return super.defenseSkill();
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );

		Ghost.Quest.process();
	}

	protected class Wandering extends Mob.Wandering{
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
}
