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
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollGuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

public class GnollGuard extends Mob implements CombatModifier.PreArmorDamageModifier, CombatModifier.OnDamageEffect {

	{
		WANDERING = new Wandering();
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.gnollguard; }

	public void linkSapper( GnollSapper sapper){
		this.m_SapperID.Set(sapper.id());
		if (sprite instanceof GnollGuardSprite){
			((GnollGuardSprite) sprite).setupArmor();
		}
	}

	public boolean hasSapper(){
		return m_SapperID.Get() != -1
				&& Actor.findById(m_SapperID.Get()) instanceof GnollSapper
				&& ((GnollSapper)Actor.findById(m_SapperID.Get())).isAlive();
	}

	public void loseSapper(){
		if (m_SapperID.Get() != -1){
			m_SapperID.Set(-1);
			if (sprite instanceof GnollGuardSprite){
				((GnollGuardSprite) sprite).loseArmor();
			}
		}
	}

	@Override
	public int damageRoll(AttackContext.AttackType type, boolean isMaxDamage) {
		if (enemy != null && !Dungeon.level.adjacent(pos, enemy.pos) && type == AttackContext.AttackType.MELEE){
			return super.damageRoll(AttackContext.AttackType.RANGED, isMaxDamage);
		}
		return super.damageRoll(type, isMaxDamage);
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

	private BundleableProperty.Int m_SapperID = new BundleableProperty.Int("sapper_id", -1);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_SapperID.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_SapperID.Restore(bundle);
	}

	@Override
	public int modifyPreArmorDamage(AttackContext context, int currentDamage) {
		if (context.defender == this && hasSapper()) {
			return currentDamage / 4;
		}
		return currentDamage;
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.defender == this || context.attacker == this;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (context.attacker == this) {
			if (context.defender == Dungeon.hero && context.distance > 1 && damageDealt > 12){
				GLog.n(Messages.get(this, "spear_warn"));
			}
		}
	}

	public static class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination(Mob mob) {
			GnollGuard g = (GnollGuard) mob;
			if (g.hasSapper()){
				return ((GnollSapper)Actor.findById(g.m_SapperID.Get())).pos;
			} else {
				return super.randomDestination(g);
			}
		}
	}

}
