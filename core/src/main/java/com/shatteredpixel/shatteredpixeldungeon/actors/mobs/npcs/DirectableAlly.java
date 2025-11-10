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

package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.watabou.utils.Bundle;

public abstract class DirectableAlly extends NPC {

	{
		alignment = Char.Alignment.ALLY;
		intelligentAlly = true;
		WANDERING = new Wandering();
		HUNTING = new Hunting();
		state = WANDERING;

		//before other mobs
		actPriority = MOB_PRIO + 1;

	}

	protected boolean attacksAutomatically = true;

	protected int defendingPos = -1;
	protected boolean movingToDefendPos = false;

	public void defendPos( int cell ){
		defendingPos = cell;
		movingToDefendPos = true;
		aggro(null);
		state = WANDERING;
	}

	public void clearDefensingPos(){
		defendingPos = -1;
		movingToDefendPos = false;
	}

	public void followHero(){
		defendingPos = -1;
		movingToDefendPos = false;
		aggro(null);
		state = WANDERING;
	}

	public void targetChar( Char ch ){
		defendingPos = -1;
		movingToDefendPos = false;
		aggro(ch);
		m_Target.Set(ch.pos);
	}

	@Override
	public void aggro(Char ch) {
		enemy = ch;
		if (!movingToDefendPos && state != PASSIVE){
			state = HUNTING;
		}
	}

	public void directTocell( int cell ){
		if (!Dungeon.level.heroFOV[cell]
				|| Actor.findChar(cell) == null
				|| (Actor.findChar(cell) != Dungeon.hero && Actor.findChar(cell).alignment != Char.Alignment.ENEMY)){
			defendPos( cell );
			return;
		}

		if (Actor.findChar(cell) == Dungeon.hero){
			followHero();

		} else if (Actor.findChar(cell).alignment == Char.Alignment.ENEMY){
			targetChar(Actor.findChar(cell));

		}
	}

	private static final String DEFEND_POS = "defend_pos";
	private static final String MOVING_TO_DEFEND = "moving_to_defend";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(DEFEND_POS, defendingPos);
		bundle.put(MOVING_TO_DEFEND, movingToDefendPos);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		if (bundle.contains(DEFEND_POS)) defendingPos = bundle.getInt(DEFEND_POS);
		movingToDefendPos = bundle.getBoolean(MOVING_TO_DEFEND);
	}

	private static class Wandering extends Mob.Wandering {

		@Override
		public boolean act( Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			DirectableAlly da = (DirectableAlly) mob;
			if ( enemyInFOV
					&& da.attacksAutomatically
					&& !da.movingToDefendPos
					&& (da.defendingPos == -1 || !Dungeon.level.heroFOV[da.defendingPos] || da.canAttack(da.enemy))) {

				da.m_EnemySeen.Set(true);

				da.notice();
				da.alerted = true;
				da.state = da.HUNTING;
				da.m_Target.Set(da.enemy.pos);

			} else {

				da.m_EnemySeen.Set(false);

				int oldPos = da.pos;
				da.m_Target.Set(da.defendingPos != -1 ? da.defendingPos : Dungeon.hero.pos);
				//always move towards the hero when wandering
				if (da.getCloser( da.m_Target.Get() )) {
					da.spend( 1 / da.speed() );
					if (da.pos == da.defendingPos) da.movingToDefendPos = false;
					return da.moveSprite( oldPos, da.pos );
				} else {
					//if it can't move closer to defending pos, then give up and defend current position
					if (da.movingToDefendPos){
						da.defendingPos = da.pos;
						da.movingToDefendPos = false;
					}
					da.spend( TICK );
				}

			}
			return true;
		}

	}

	private static class Hunting extends Mob.Hunting {

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			DirectableAlly da = (DirectableAlly) mob;
			if (enemyInFOV && da.defendingPos != -1 && Dungeon.level.heroFOV[da.defendingPos] && !da.canAttack(da.enemy)){
				da.m_Target.Set(da.defendingPos);
				da.state = da.WANDERING;
				return true;
			}
			return super.act(mob, enemyInFOV, justAlerted);
		}

	}

}
