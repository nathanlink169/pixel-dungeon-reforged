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

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class DemonSpawner extends Mob implements CombatModifier.PostArmorDamageModifier, CombatModifier.OnDamageEffect {
	{
		state = PASSIVE;
	}

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.demonspawner; }

	@Override
	public int GetMaxHP() {
		return (int) (super.GetMaxHP() * (Dungeon.isChallenged(Challenges.STRONGER_BOSSES) ? (4f/3f) : 1.0f));
	}

	@Override
	public void beckon(int cell) {
		//do nothing
	}

	@Override
	public boolean reset() {
		return true;
	}

	@Override
	protected boolean act() {
		if (!m_SpawnRecorded.Get()){
			Statistics.spawnersAlive++;
			m_SpawnRecorded.Set(true);
		}

		if (Dungeon.hero.buff(AscensionChallenge.class) != null && m_SpawnCooldown.Get() > 20){
			m_SpawnCooldown.Set(20.0f);
		}

		m_SpawnCooldown.Set(m_SpawnCooldown.Get() - 1);
		if (m_SpawnCooldown.Get() <= 0){
			SpawnDemon();
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
				SpawnDemon();
			}

			//we don't want spawners to store multiple ripper demons
			if (m_SpawnCooldown.Get() < -20){
				m_SpawnCooldown.Set(-20.0f);
			}
			m_SpawnCooldown.Set(m_SpawnCooldown.Get() + timeBetweenSpawns());
			if (Dungeon.depth > 21){
				//60/53.33/46.67/40 turns to spawn on floor 21/22/23/24
				m_SpawnCooldown.Set(m_SpawnCooldown.Get() - Math.min(20, (Dungeon.depth-21)*6.67f));
			}
			if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
				// Not quite double, but we spawn double the demons
				m_SpawnCooldown.Set(m_SpawnCooldown.Get() * 1.75f);
			}
		}
		alerted = false;
		return super.act();
	}

	private int timeBetweenSpawns() {
		if (RipperDemon.getRandomizerEnabled(RipperDemon.RandomTraits.RAPID_DEPLOYMENT)) {
			return 30;
		}
		if (RipperDemon.getRandomizerEnabled(RipperDemon.RandomTraits.LAZY_SPAWNERS)) {
			return 100;
		}
		return 60;
	}

	private void SpawnDemon() {
		ArrayList<Integer> candidates = new ArrayList<>();
		for (int n : PathFinder.NEIGHBOURS8) {
			if (Dungeon.level.passable[pos+n] && Actor.findChar( pos+n ) == null) {
				candidates.add( pos+n );
			}
		}

		if (!candidates.isEmpty()) {
			RipperDemon spawn = new RipperDemon();

			spawn.pos = Random.element(candidates);
			spawn.state = spawn.HUNTING;

			GameScene.add(spawn, 1);
			Dungeon.level.occupyCell(spawn);

			if (sprite.visible) {
				Actor.add(new Pushing(spawn, pos, spawn.pos));
			}
		}
	}

	@Override
	public Notes.Landmark landmark() {
		return Notes.Landmark.DEMON_SPAWNER;
	}

	@Override
	public void die(Object cause) {
		if (m_SpawnRecorded.Get()){
			Statistics.spawnersAlive--;
			Notes.remove(landmark());
		}
		GLog.h(Messages.get(this, "on_death"));
		super.die(cause);
	}

	public boolean GetSpawnRecorded() {
		return m_SpawnRecorded.Get();
	}

	private BundleableProperty.Float m_SpawnCooldown = new BundleableProperty.Float("spawn_cooldown", 0.0f);
	private BundleableProperty.Bool m_SpawnRecorded = new BundleableProperty.Bool("spawn_cooldown", false);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_SpawnCooldown.Store(bundle);
		m_SpawnRecorded.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_SpawnCooldown.Restore(bundle);
		m_SpawnRecorded.Restore(bundle);
	}

	@Override
	public int modifyPostArmorDamage(AttackContext context, int currentDamage) {
		if (currentDamage >= 20){
			//takes 20/21/22/23/24/25/26/27/28/29/30 dmg
			// at   20/22/25/29/34/40/47/55/64/74/85 incoming dmg
			currentDamage = 19 + (int)(Math.sqrt(8*(currentDamage - 19) + 1) - 1)/2;
		}
		return currentDamage;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		m_SpawnCooldown.Set(m_SpawnCooldown.Get() - damageDealt);
		if (Dungeon.isChallenged(Challenges.STRONGER_BOSSES)) {
			m_SpawnCooldown.Set(m_SpawnCooldown.Get() - damageDealt);
		}
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
