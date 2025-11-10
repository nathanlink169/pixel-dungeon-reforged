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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Web;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Dread;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Roots;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Terror;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.EnumSet;

public class Spinner extends Mob implements CombatModifier.OnDamageEffect {
	{
		HUNTING = new Hunting();
		FLEEING = new Fleeing();
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.spinner; }

	@Override
	public int GetMaxHP() {
		return super.GetMaxHP() / (getRandomizerEnabled(RandomTraits.FRAGILE_CHITIN) ? 2 : 1);
	}

	@Override
	public int drRoll(EnumSet<DamageType> damageType) {
		if (getRandomizerEnabled(RandomTraits.FRAGILE_CHITIN)) {
			return 0;
		}
		return super.drRoll(damageType);
	}

	private BundleableProperty.Int m_WebCooldown = new BundleableProperty.Int("web_cooldown", 0);
	private BundleableProperty.Int m_LastEnemyPosition = new BundleableProperty.Int("last_enemy_pos", -1);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_WebCooldown.Store(bundle);
		m_LastEnemyPosition.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_WebCooldown.Restore(bundle);
		m_LastEnemyPosition.Restore(bundle);
	}
	
	@Override
	protected boolean act() {
		if (state == HUNTING || state == FLEEING){
			m_WebCooldown.Decrement();
		}

		if (getRandomizerEnabled(RandomTraits.WEBLESS)) {
			m_WebCooldown.Set(500);
		}

		AiState lastState = state;
		boolean result = super.act();

		//We only want to update target position once per turn, so if switched from wandering, wait for a moment
		//Also want to avoid updating when we visually shot a web this turn (don't want to change the position)
		if (!(lastState == WANDERING && state == HUNTING)) {
			if (!shotWebVisually){
				if (enemy != null && m_EnemySeen.Get()) {
					m_LastEnemyPosition.Set(enemy.pos);
				} else {
					m_LastEnemyPosition.Set(Dungeon.hero.pos);
				}
			}
			shotWebVisually = false;
		}
		
		return result;
	}
	
	private boolean shotWebVisually = false;

	public int webPos(){
		if (getRandomizerEnabled(RandomTraits.WEBLESS)) {
			return -1;
		}

		Char enemy = this.enemy;
		if (enemy == null) return -1;

		//don't web a non-moving enemy that we're going to attack
		if (state != FLEEING && enemy.pos == m_LastEnemyPosition.Get() && canAttack(enemy)){
			return -1;
		}

		int webPos;
		if (getRandomizerEnabled(RandomTraits.DIRECT_SHOT)) {
			webPos = enemy.pos;
		}
		else {
			Ballistica b;
			//aims web in direction enemy is moving, or between self and enemy if they aren't moving
			if (m_LastEnemyPosition.Get() == enemy.pos) {
				b = new Ballistica(enemy.pos, pos, Ballistica.WONT_STOP);
			} else {
				b = new Ballistica(m_LastEnemyPosition.Get(), enemy.pos, Ballistica.WONT_STOP);
			}

			int collisionIndex = 0;
			for (int i = 0; i < b.path.size(); i++) {
				if (b.path.get(i) == enemy.pos) {
					collisionIndex = i;
					break;
				}
			}

			//in case target is at the edge of the map and there are no more cells in the path
			if (b.path.size() <= collisionIndex + 1) {
				return -1;
			}

			webPos = b.path.get(collisionIndex + 1);
		}

		//ensure we aren't shooting the web through walls
		int projectilePos = new Ballistica( pos, webPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID).collisionPos;
		
		if ((webPos != enemy.pos || getRandomizerEnabled(RandomTraits.DIRECT_SHOT)) && projectilePos == webPos && Dungeon.level.passable[webPos]){
			return webPos;
		} else {
			return -1;
		}
		
	}
	
	public void shootWeb(){
		int webPos = webPos();
		if (webPos != -1){

			if (!getRandomizerEnabled(RandomTraits.DIRECT_SHOT)) {
				int i;
				for (i = 0; i < PathFinder.CIRCLE8.length; i++) {
					if ((enemy.pos + PathFinder.CIRCLE8[i]) == webPos) {
						break;
					}
				}

				//spread to the tile hero was moving towards and the two adjacent ones
				int leftPos = enemy.pos + PathFinder.CIRCLE8[left(i)];
				int rightPos = enemy.pos + PathFinder.CIRCLE8[right(i)];

				if (Dungeon.level.passable[leftPos]) applyWebToCell(leftPos);
				if (Dungeon.level.passable[webPos] && !getRandomizerEnabled(RandomTraits.DIRECT_SHOT))
					applyWebToCell(webPos);
				if (Dungeon.level.passable[rightPos]) applyWebToCell(rightPos);
			} else {
				for (int offset : PathFinder.NEIGHBOURS4) {
					if (Dungeon.level.passable[webPos + offset]) applyWebToCell(webPos + offset);
				}
			}
			
			m_WebCooldown.Set(10);

			if (Dungeon.level.heroFOV[enemy.pos]){
				Dungeon.hero.interrupt();
			}

			if (getRandomizerEnabled(RandomTraits.DIRECT_SHOT)) {
				Char e = Dungeon.level.findMob(webPos);
				if (e == null && Dungeon.hero.pos == webPos) {
					e = Dungeon.hero;
				}

				if (e != null) {
					Buff.prolong(e, Roots.class, Roots.DURATION);
				}
			}
		}
		next();
	}

	protected void applyWebToCell(int cell){
		GameScene.add(Blob.seed(cell, 20, Web.class));
	}
	
	private int left(int direction){
		return direction <= 0 ? 7 : direction-1;
	}
	
	private int right(int direction){
		return direction >= 7 ? 0 : direction+1;
	}

	{
		resistances.add(Poison.class);
	}
	
	{
		immunities.add(Web.class);
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (Random.Int(2) == 0) {
			int duration = Random.IntRange(7, 8);
			if (getRandomizerEnabled(RandomTraits.POTENT_VENOM)) {
				duration += 5;
			}
			if (getRandomizerEnabled(RandomTraits.WEAK_VENOM)) {
				duration -= 5;
			}
			//we only use half the ascension modifier here as total poison dmg doesn't scale linearly
			duration = Math.round(duration * (AscensionChallenge.statModifier(this)/2f + 0.5f));
			Buff.affect(enemy, Poison.class).set(duration);
			m_WebCooldown.Set(0);
			state = FLEEING;
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

	private static class Hunting extends Mob.Hunting {

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			Spinner s = (Spinner)mob;
			if (enemyInFOV && s.m_WebCooldown.Get() <= 0 && s.m_LastEnemyPosition.Get() != -1){
				if (s.webPos() != -1){
					if (s.sprite != null && (s.sprite.visible || s.enemy.sprite.visible)) {
						s.sprite.zap( s.webPos() );
						s.shotWebVisually = true;
						return false;
					} else {
						s.shootWeb();
						return true;
					}
				}
			}

			return super.act(s, enemyInFOV, justAlerted);
		}
	}

	private static class Fleeing extends Mob.Fleeing {

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted) {
			Spinner s = (Spinner)mob;
			if (s.buff( Terror.class ) == null && s.buff( Dread.class ) == null &&
					enemyInFOV && s.enemy.buff( Poison.class ) == null){
				s.state = s.HUNTING;
				return true;
			}

			if (enemyInFOV && s.m_WebCooldown.Get() <= 0 && s.m_LastEnemyPosition.Get() != -1){
				if (s.webPos() != -1){
					if (s.sprite != null && (s.sprite.visible || s.enemy.sprite.visible)) {
						s.sprite.zap( s.webPos() );
						s.shotWebVisually = true;
						return false;
					} else {
						s.shootWeb();
						return true;
					}
				}
			}
			return super.act(s, enemyInFOV, justAlerted);
		}

	}

	public enum RandomTraits {
		LASTING_WEBS, DIRECT_SHOT, POTENT_VENOM, WEBLESS, WEAK_VENOM, FRAGILE_CHITIN
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case LASTING_WEBS: return Randomizer.getCreatureBuff(Spinner.class) == 1;
			case DIRECT_SHOT: return Randomizer.getCreatureBuff(Spinner.class) == 2;
			case POTENT_VENOM: return Randomizer.getCreatureBuff(Spinner.class) == 3;
			case WEBLESS: return Randomizer.getCreatureNerf(Spinner.class) == 1;
			case WEAK_VENOM: return Randomizer.getCreatureNerf(Spinner.class) == 2;
			case FRAGILE_CHITIN: return Randomizer.getCreatureNerf(Spinner.class) == 3;
		}
		return false;
	}
}