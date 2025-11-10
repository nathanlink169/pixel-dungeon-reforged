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
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.EnumSet;

public class GnollSapper extends Mob {
	{
		//always acts after guards, makes it easier to kite them into attacks
		actPriority = Actor.MOB_PRIO-1;
		HUNTING = new Hunting();
		WANDERING = new Wandering();
		state = SLEEPING;
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.gnollsapper; }


	public void linkPartner(Char c){
		losePartner();
		m_PartnerID.Set(c.id());
		if (c instanceof GnollGuard) {
			((GnollGuard) c).linkSapper(this);
		} else if (c instanceof GnollGeomancer){
			((GnollGeomancer) c).linkSapper(this);
		}
	}

	public void losePartner(){
		if (m_PartnerID.Get() != -1){
			if (Actor.findById(m_PartnerID.Get()) instanceof GnollGuard) {
				((GnollGuard) Actor.findById(m_PartnerID.Get())).loseSapper();
			} else if (Actor.findById(m_PartnerID.Get()) instanceof GnollGeomancer) {
				((GnollGeomancer) Actor.findById(m_PartnerID.Get())).loseSapper();
			}
			m_PartnerID.Set(-1);
		}
	}

	public Actor getPartner(){
		return Actor.findById(m_PartnerID.Get());
	}

	@Override
	public void die(Object cause) {
		super.die(cause);
		losePartner();
	}

	@Override
	public int Damage(int dmg, Object src, EnumSet<DamageType> damageType) {
		m_AbilityCooldown.Subtract((int) (dmg/10f));
		return super.Damage(dmg, src, damageType);
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	protected boolean act() {
		if (m_SpawnPosition.Get() == -1) {
			m_SpawnPosition.Set(pos);
		}

		if (m_ThrowingRocksFromPosition.Get() != -1){

			boolean attacked = Dungeon.level.map[m_ThrowingRocksFromPosition.Get()] == Terrain.MINE_BOULDER;

			if (attacked) {
				GnollGeomancer.doRockThrowAttack(this, m_ThrowingRocksFromPosition.Get(), m_ThrowingRocksToPosition.Get());
			}

			m_ThrowingRocksFromPosition.Set(-1);
			m_ThrowingRocksToPosition.Set(-1);

			spend(TICK);
			return !attacked;
		} else {
			return super.act();
		}

	}

	public static class Hunting extends Mob.Hunting {
		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			GnollSapper g = (GnollSapper)mob;
			if (!enemyInFOV) {
				if (Dungeon.level.distance(g.m_SpawnPosition.Get(), g.m_Target.Get()) > 3){
					//don't chase something more than a few tiles out of spawning position
					g.m_Target.Set(g.pos);
				}
				return super.act(g, enemyInFOV, justAlerted);
			} else {
				g.m_EnemySeen.Set(true);

				if (g.getPartner() != null
						&& g.getPartner() instanceof Mob
						&& ((Mob) g.getPartner()).alignment != g.alignment){
					g.losePartner();
				}

				if (Actor.findById(g.m_PartnerID.Get()) != null
						&& Dungeon.level.distance(g.pos, g.enemy.pos) <= 3){
					Mob partner = (Mob) Actor.findById(g.m_PartnerID.Get());
					if (partner.state == partner.SLEEPING){
						partner.notice();
					}
					if (g.enemy != partner) {
						partner.m_Target.Set(g.enemy.pos);
						partner.aggro(g.enemy);
					}
				}

				g.m_AbilityCooldown.Decrement();
				if (g.m_AbilityCooldown.Get() <= 0){
					boolean targetNextToBarricade = false;
					for (int i : PathFinder.NEIGHBOURS8){
						if (Dungeon.level.map[g.enemy.pos+i] == Terrain.BARRICADE
							|| Dungeon.level.map[g.enemy.pos+i] == Terrain.ENTRANCE){
							targetNextToBarricade = true;
							break;
						}
					}

					// 50/50 to either throw a rock or do rockfall, but never do rockfall twice
					// unless target is next to a barricade, then always try to throw
					// unless nothing to throw, then always rockfall
					Ballistica aim = GnollGeomancer.prepRockThrowAttack(g.enemy, g);
					if (aim != null && (targetNextToBarricade || g.m_LastAbilityWasRockfall.Get() || Random.Int(2) == 0)) {

						g.m_LastAbilityWasRockfall.Set(false);
						g.m_ThrowingRocksFromPosition.Set(aim.sourcePos);
						g.m_ThrowingRocksToPosition.Set(aim.collisionPos);

						Ballistica warnPath = new Ballistica(aim.sourcePos, aim.collisionPos, Ballistica.STOP_SOLID);
						for (int i : warnPath.subPath(0, warnPath.dist)){
							g.sprite.parent.add(new TargetedCell(i, 0xFF0000));
						}

						Dungeon.hero.interrupt();
						g.m_AbilityCooldown.Set(Random.NormalIntRange(4, 6));
						g.spend(GameMath.gate(TICK, (int)Math.ceil(g.enemy.cooldown()), 3*TICK));
						return true;
					} else if (GnollGeomancer.prepRockFallAttack(g.enemy, g, 2, true)) {
						g.m_LastAbilityWasRockfall.Set(true);
						Dungeon.hero.interrupt();
						g.spend(GameMath.gate(TICK, (int)Math.ceil(g.enemy.cooldown()), 3*TICK));
						g.m_AbilityCooldown.Set(Random.NormalIntRange(4, 6));
						return true;
					}
				}

				//does not approach an enemy it can see, but does melee if in range
				if (g.canAttack(g.enemy)){
					return super.act(g, enemyInFOV, justAlerted);
				} else {
					g.spend(TICK);
					return true;
				}
			}
		}
	}

	public static class Wandering extends Mob.Wandering {
		@Override
		protected int randomDestination(Mob mob) {
			return ((GnollSapper)mob).m_SpawnPosition.Get();
		}
	}

	public int SpawnPosition() {
		return m_SpawnPosition.Get();
	}

	public void SetSpawnPosition(int pos) {
		m_SpawnPosition.Set(pos);
	}

	public int ThrowingRocksFromPosition() {
		return m_ThrowingRocksFromPosition.Get();
	}

	private BundleableProperty.Int m_SpawnPosition = new BundleableProperty.Int("spawn_pos", -1);
	private BundleableProperty.Int m_PartnerID = new BundleableProperty.Int("partner_id", -1);
	private BundleableProperty.Int m_AbilityCooldown = new BundleableProperty.Int("ability_cooldown", Random.NormalIntRange(4, 6));
	private BundleableProperty.Bool m_LastAbilityWasRockfall = new BundleableProperty.Bool("last_ability_was_rockfall", false);
	private BundleableProperty.Int m_ThrowingRocksFromPosition = new BundleableProperty.Int("rock_from_pos", -1);
	private BundleableProperty.Int m_ThrowingRocksToPosition = new BundleableProperty.Int("rock_to_pos", -1);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_SpawnPosition.Store(bundle);
		m_PartnerID.Store(bundle);
		m_AbilityCooldown.Store(bundle);
		m_LastAbilityWasRockfall.Store(bundle);
		m_ThrowingRocksFromPosition.Store(bundle);
		m_ThrowingRocksToPosition.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_SpawnPosition.Restore(bundle);
		m_PartnerID.Restore(bundle);
		m_AbilityCooldown.Restore(bundle);
		m_LastAbilityWasRockfall.Restore(bundle);
		m_ThrowingRocksFromPosition.Restore(bundle);
		m_ThrowingRocksToPosition.Restore(bundle);
	}
}
