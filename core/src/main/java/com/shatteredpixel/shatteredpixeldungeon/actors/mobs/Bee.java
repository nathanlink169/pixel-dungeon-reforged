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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AllyBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Amok;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

//FIXME the AI for these things is becoming a complete mess, should refactor
public class Bee extends Mob implements CombatModifier.OnHitEffect {
	
	{
		state = WANDERING;
		
		//only applicable when the bee is charmed with elixir of honeyed healing
		intelligentAlly = true;
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.bee; }

	private static final String ALIGMNENT   = "alignment";

	private BundleableProperty.Int m_Level = new BundleableProperty.Int("level", 0);
	private BundleableProperty.Int m_PotPosition = new BundleableProperty.Int("potpos", -1); //-1 refers to a pot that has gone missing.
	private BundleableProperty.Int m_PotHolder = new BundleableProperty.Int("potholder", -1); //-1 for no owner

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		m_Level.Store(bundle);
		m_PotPosition.Store(bundle);
		m_PotHolder.Store(bundle);
		bundle.put( ALIGMNENT, alignment);
	}
	
	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		m_Level.Restore(bundle);
		m_PotPosition.Restore(bundle);
		m_PotHolder.Restore(bundle);
		if (bundle.contains(ALIGMNENT)) alignment = bundle.getEnum( ALIGMNENT, Alignment.class);
	}

	@Override
	public void die(Object cause) {
		flying = false;
		super.die(cause);
	}

	@Override
	public int GetMaxHP() {
		return (2 + m_Level.Get()) * 4;
	}

	@Override
	public int defenseSkill() {
		return 9 + m_Level.Get();
	}
	
	public void spawn( int level ) {
		m_Level.Set(level);
	}

	public void setPotInfo(int potPos, Char potHolder){
		m_PotPosition.Set(potPos);
		m_PotHolder.Set(potHolder != null ? potHolder.id() : -1);
	}
	
	public int potPos(){
		return m_PotPosition.Get();
	}
	
	public int potHolderID(){
		return m_PotHolder.Get();
	}
	
	@Override
	public int attackSkill(  ) {
		return defenseSkill();
	}
	
	@Override
	public int damageRoll(AttackContext context) {
		if (context.isMaxDamage) return GetMaxHP()/4;
		return Random.NormalIntRange( GetMaxHP() / 10, GetMaxHP() / 4 );
	}

	@Override
	public int priority() {
		return CombatModifier.Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		// Only apply when the bee is the attacker
		return context.attacker == this;
	}

	@Override
	public void onHit(AttackContext context, int finalDamage) {
		// Make enemy mobs aggro onto the bee to protect the honeypot
		if (context.defender instanceof Mob) {
			((Mob)context.defender).aggro(this);
		}
	}

	@Override
	public boolean add(Buff buff) {
		if (super.add(buff)) {
			//TODO maybe handle honeyed bees with their own ally buff?
			if (buff instanceof AllyBuff) {
				intelligentAlly = false;
				setPotInfo(-1, null);
			}
			return true;
		}
		return false;
	}

	@Override
	protected Char chooseEnemy() {
		//if the pot is no longer present, default to regular AI behaviour
		if (alignment == Alignment.ALLY || (m_PotHolder.Get() == -1 && m_PotPosition.Get() == -1)){
			return super.chooseEnemy();
		
		//if something is holding the pot, target that
		}else if (Actor.findById(m_PotHolder.Get()) != null){
			return (Char) Actor.findById(m_PotHolder.Get());
			
		//if the pot is on the ground
		}else {
			
			//try to find a new enemy in these circumstances
			if (enemy == null || !enemy.isAlive() || !Actor.chars().contains(enemy) || state == WANDERING
					|| Dungeon.level.distance(enemy.pos, m_PotPosition.Get()) > 3
					|| (alignment == Alignment.ALLY && enemy.alignment == Alignment.ALLY)
					|| (buff( Amok.class ) == null && enemy.isInvulnerable(getClass()))){
				
				//target closest potential enemy near the pot
				Char closest = null;
				for (Mob mob : Dungeon.level.mobs) {
					if (!(mob == this)
							&& Dungeon.level.distance(mob.pos, m_PotPosition.Get()) <= 3
							&& mob.alignment != Alignment.NEUTRAL
							&& !mob.isInvulnerable(getClass())
							&& !(alignment == Alignment.ALLY && mob.alignment == Alignment.ALLY)) {
						if (closest == null || Dungeon.level.distance(closest.pos, pos) > Dungeon.level.distance(mob.pos, pos)){
							closest = mob;
						}
					}
				}
				
				if (closest != null){
					return closest;
				} else {
					if (alignment != Alignment.ALLY && Dungeon.level.distance(Dungeon.hero.pos, m_PotPosition.Get()) <= 3){
						return Dungeon.hero;
					} else {
						return null;
					}
				}
				
			} else {
				return enemy;
			}

			
		}
	}

	@Override
    public boolean getCloser(int target) {
		if (alignment == Alignment.ALLY && enemy == null && buffs(AllyBuff.class).isEmpty()) {
			target = Dungeon.hero.pos;
		} else if (enemy != null && Actor.findById(m_PotHolder.Get()) == enemy) {
			target = enemy.pos;
		} else if (m_PotPosition.Get() != -1 && (state == WANDERING || Dungeon.level.distance(target, m_PotPosition.Get()) > 3)) {
			if (!Dungeon.level.insideMap(m_PotPosition.Get())){
				m_PotPosition.Set(-1);
			} else {
				target = m_PotPosition.Get();
				m_Target.Set(m_PotPosition.Get());
			}
		}
		return super.getCloser( target );
	}
	
	@Override
	public String description(boolean forceNoMonsterUnknown) {
		if (alignment == Alignment.ALLY && buffs(AllyBuff.class).isEmpty()){
			return Messages.get(this, "desc_honey");
		} else {
			return super.description(forceNoMonsterUnknown);
		}
	}
}