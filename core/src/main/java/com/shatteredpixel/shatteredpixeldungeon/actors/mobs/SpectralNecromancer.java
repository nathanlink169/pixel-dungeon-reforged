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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpectralNecromancerSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SpectralNecromancer extends Necromancer {

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.spectralnecromancer; }

	private ArrayList<Integer> wraithIDs = new ArrayList<>();

	@Override
	protected boolean act() {
		if (m_Summoning.Get() && state != HUNTING){
			m_Summoning.Set(false);
			if (sprite instanceof SpectralNecromancerSprite) {
				((SpectralNecromancerSprite) sprite).cancelSummoning();
			}
		}
		return super.act();
	}

	@Override
	public void rollToDropLoot() {
		if (Dungeon.hero.lvl > GetMaxLevel() + 2) return;

		super.rollToDropLoot();

		int ofs;
		do {
			ofs = PathFinder.NEIGHBOURS8[Random.Int(8)];
		} while (Dungeon.level.solid[pos + ofs] && !Dungeon.level.passable[pos + ofs]);
		Dungeon.level.drop( new ScrollOfRemoveCurse(), pos + ofs ).sprite.drop( pos );
	}

	@Override
	public void die(Object cause) {
		for (int ID : wraithIDs){
			Actor a = Actor.findById(ID);
			if (a instanceof Wraith && ((Wraith) a).alignment == alignment){
				((Wraith) a).die(null);
			}
		}

		super.die(cause);
	}

	private static final String WRAITH_IDS = "wraith_ids";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		int[] wraithIDArr = new int[wraithIDs.size()];
		int i = 0; for (Integer val : wraithIDs){ wraithIDArr[i] = val; i++; }
		bundle.put(WRAITH_IDS, wraithIDArr);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		wraithIDs.clear();
		for (int i : bundle.getIntArray(WRAITH_IDS)){
			wraithIDs.add(i);
		}
	}

	@Override
	public void summonMinion() {
		if (Actor.findChar(m_SummoningPosition.Get()) != null) {

			int pushPos = pos;
			for (int c : PathFinder.NEIGHBOURS8) {
				if (Actor.findChar(m_SummoningPosition.Get() + c) == null
						&& Dungeon.level.passable[m_SummoningPosition.Get() + c]
						&& (Dungeon.level.openSpace[m_SummoningPosition.Get() + c] || !hasProp(Actor.findChar(m_SummoningPosition.Get()), Property.LARGE))
						&& Dungeon.level.trueDistance(pos, m_SummoningPosition.Get() + c) > Dungeon.level.trueDistance(pos, pushPos)) {
					pushPos = m_SummoningPosition.Get() + c;
				}
			}

			//no push if char is immovable
			if (Char.hasProp(Actor.findChar(m_SummoningPosition.Get()), Property.IMMOVABLE)){
				pushPos = pos;
			}

			//push enemy, or wait a turn if there is no valid pushing position
			if (pushPos != pos) {
				Char ch = Actor.findChar(m_SummoningPosition.Get());
				Actor.add( new Pushing( ch, ch.pos, pushPos ) );

				ch.pos = pushPos;
				Dungeon.level.occupyCell(ch );

			} else {

				Char blocker = Actor.findChar(m_SummoningPosition.Get());
				if (blocker.alignment != alignment){
					blocker.Damage( Random.NormalIntRange(2, 10), new SummoningBlockDamage(), DamageType.of(DamageType.BLUDGEONING));
					if (blocker == Dungeon.hero && !blocker.isAlive()){
						Badges.validateDeathFromEnemyMagic();
						Dungeon.fail(this);
						GLog.n( Messages.capitalize(Messages.get(Char.class, "kill", name(false))) );
					}
				}

				spend(TICK);
				return;
			}
		}

		m_FirstSummon.Set(false);
		m_Summoning.Set(false);

		Wraith wraith = Wraith.spawnAt(m_SummoningPosition.Get(), Wraith.class);
		if (wraith == null){
			spend(TICK);
			return;
		}
		Dungeon.level.occupyCell( wraith );
		((SpectralNecromancerSprite)sprite).finishSummoning();

		for (Buff b : buffs()){
			if (b.revivePersists) {
				Buff.affect(wraith, b.getClass());
			}
		}
		wraithIDs.add(wraith.id());
	}
}
