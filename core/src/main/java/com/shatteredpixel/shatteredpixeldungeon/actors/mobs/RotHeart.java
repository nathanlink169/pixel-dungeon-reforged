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
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.ToxicGas;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.journal.Bestiary;
import com.shatteredpixel.shatteredpixeldungeon.plants.Rotberry;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;

import java.util.EnumSet;

public class RotHeart extends Mob implements CombatModifier.OnDamageEffect {

	{
		state = PASSIVE;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.rotheart; }

	@Override
	protected boolean act() {
		alerted = false;
		return super.act();
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType) {
		if (src instanceof Burning || damageType.contains(DamageType.FIRE)) {
			int hp = HP;
			destroy();
			sprite.die();
			return hp;
		} else {
			return super.Damage(dmg, src, damageType);
		}
	}

	@Override
	public void beckon(int cell) {
		//do nothing
	}

	@Override
    public boolean getCloser(int target) {
		return false;
	}

	@Override
	public void destroy() {
		super.destroy();
		Bestiary.skipCountingEncounters = true;
		for (Mob mob : Dungeon.level.mobs.toArray(new Mob[Dungeon.level.mobs.size()])){
			if (mob instanceof RotLasher){
				mob.die(null);
			}
		}
		Bestiary.skipCountingEncounters = false;
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		Dungeon.level.drop( new Rotberry.RotberrySeed(), pos ).sprite.drop();
		//assign score here as player may choose to keep the rotberry seed
		Statistics.questScores[1] += 2000;
	}

	@Override
	public boolean reset() {
		return true;
	}
	
	{
		immunities.add( ToxicGas.class );
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		//rot heart spreads less gas in enclosed spaces
		int openNearby = 0;
		for (int i : PathFinder.NEIGHBOURS8){
			if (!Dungeon.level.solid[pos+i]){
				openNearby++;
			}
		}

		GameScene.add(Blob.seed(pos, 5 + 3*openNearby, ToxicGas.class));
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == this;
	}
}
