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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Sleep;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.HashSet;

public class Ballista extends Mob implements CombatModifier.OnHitEffect, CombatModifier.OnMissEffect {
	{
		HUNTING = new Hunting();
	}

	public static final HashSet<Class> RESISTS = new HashSet<>();
	static {
		RESISTS.add( Amok.class );
		RESISTS.add( Terror.class );
		RESISTS.add( Sleep.class );
		RESISTS.add( Bleeding.class );
	}

	{
		immunities.addAll(RESISTS);
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.ballista; }

	@Override
	protected boolean canAttack( Char enemy ) {
		return !Dungeon.level.adjacent( pos, enemy.pos ) && m_Ammo.Get() > 0
				&& (super.canAttack(enemy) || new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos);
	}

	protected int ammoCapacity() {
		return 1;
	}
	private void reload() {
		m_Ammo.Set(ammoCapacity());
		if (Dungeon.hero.getVisibleEnemies().contains(this)) {
			Sample.INSTANCE.play(Assets.Sounds.BALLISTA_RELOAD);
			yell(Messages.get(this, "loaded"));
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
	public void onHit(AttackContext context, int finalDamage) {
		m_Ammo.Set(m_Ammo.Get() - 1);
		if (Random.Int( 4 ) == 0) {
			int oppositeAdjacent = context.defenderPosition + (context.defenderPosition - context.attackerPosition);
			Ballistica trajectory = new Ballistica(context.defenderPosition, oppositeAdjacent, Ballistica.MAGIC_BOLT);
			WandOfBlastWave.throwChar(context.defender, trajectory, 2, false, false, this);
		}
	}

	@Override
	public void onMiss(AttackContext context) {
		m_Ammo.Set(m_Ammo.Get() - 1);
	}

	@Override
	public int priority() {
		return Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	private static class Hunting extends Mob.Hunting {

		@Override
		public boolean act( Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			Ballista b = (Ballista) mob;
			if (b.m_Ammo.Get() <= 0) {
				b.reload();
				b.spend( b.attackDelay() );
				return true;
			}
			return super.act(b, enemyInFOV, justAlerted);
		}
	}

	private BundleableProperty.Int m_Ammo = new BundleableProperty.Int("ammo", ammoCapacity());
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_Ammo.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_Ammo.Restore(bundle);
	}
}
