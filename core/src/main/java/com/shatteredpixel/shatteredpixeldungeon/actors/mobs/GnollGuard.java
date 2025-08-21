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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spear;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class GnollGuard extends Mob {

	{
		WANDERING = new Wandering();
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.gnollguard; }

	private int sapperID = -1;

	public void linkSapper( GnollSapper sapper){
		this.sapperID = sapper.id();
		if (sprite instanceof GnollGuardSprite){
			((GnollGuardSprite) sprite).setupArmor();
		}
	}

	public boolean hasSapper(){
		return sapperID != -1
				&& Actor.findById(sapperID) instanceof GnollSapper
				&& ((GnollSapper)Actor.findById(sapperID)).isAlive();
	}

	public void loseSapper(){
		if (sapperID != -1){
			sapperID = -1;
			if (sprite instanceof GnollGuardSprite){
				((GnollGuardSprite) sprite).loseArmor();
			}
		}
	}

	@Override
	public void damage(int dmg, Object src, int damageType) {
		if (hasSapper()) dmg /= 4;
		super.damage(dmg, src, damageType);
	}

	@Override
	public int damageRoll(AttackType type, boolean isMaxDamage) {
		if (enemy != null && !Dungeon.level.adjacent(pos, enemy.pos) && type == AttackType.MELEE){
			return super.damageRoll(AttackType.RANGED_PHYSICAL, isMaxDamage);
		}
		return super.damageRoll(type, isMaxDamage);
	}

	@Override
	public int attackProc(Char enemy, int damage) {
		int dmg = super.attackProc(enemy, damage);
		if (enemy == Dungeon.hero && !Dungeon.level.adjacent(pos, enemy.pos) && dmg > 12){
			GLog.n(Messages.get(this, "spear_warn"));
		}
		return dmg;
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		//cannot 'curve' spear hits like the hero, requires fairly open space to hit at a distance
		return Dungeon.level.distance(enemy.pos, pos) <= 2
				&& new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos
				&& new Ballistica( enemy.pos, pos, Ballistica.PROJECTILE).collisionPos == pos;
	}

	@Override
	public String description(boolean forceNoMonsterUnknown) {
		if (hasSapper()){
			return super.description(forceNoMonsterUnknown) + "\n\n" + Messages.get(this, "desc_armor");
		} else {
			return super.description(forceNoMonsterUnknown);
		}
	}

	private static final String SAPPER_ID = "sapper_id";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(SAPPER_ID, sapperID);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		sapperID = bundle.getInt(SAPPER_ID);
	}

	public class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination() {
			if (hasSapper()){
				return ((GnollSapper)Actor.findById(sapperID)).pos;
			} else {
				return super.randomDestination();
			}
		}
	}

}
