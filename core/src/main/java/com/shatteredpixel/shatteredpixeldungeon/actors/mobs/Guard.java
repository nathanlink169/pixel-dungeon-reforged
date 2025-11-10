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
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.BundleableProperty;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;

public class Guard extends Mob {

	{
		HUNTING = new Hunting();
	}
	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.guard; }

	private boolean chain(int target){
		if ((m_ChainsUsed.Get() && !getRandomizerEnabled(RandomTraits.CHAIN_MASTER)) || enemy.properties().contains(Property.IMMOVABLE))
			return false;

		Ballistica chain = new Ballistica(pos, target, Ballistica.PROJECTILE);

		if (chain.collisionPos != enemy.pos
				|| chain.path.size() < 2
				|| (Dungeon.level.pit[chain.path.get(1)] && !getRandomizerEnabled(RandomTraits.PIT_PULLER)))
			return false;
		else {
			int newPos = -1;
			for (int i : chain.subPath(1, chain.dist)){
				if (!Dungeon.level.solid[i] && Actor.findChar(i) == null){
					newPos = i;
					break;
				}
			}

			if (newPos == -1){
				return false;
			} else {
				final int newPosFinal = newPos;
				this.m_Target.Set(newPos);

				if (sprite.visible || enemy.sprite.visible) {
					yell(Messages.get(this, "scorpion"));
					new Item().throwSound();
					Sample.INSTANCE.play(Assets.Sounds.CHAINS);
					sprite.parent.add(new Chains(sprite.center(),
							enemy.sprite.destinationCenter(),
							Effects.Type.CHAIN,
							new Callback() {
						public void call() {
							boolean didHit = true;
							if (getRandomizerEnabled(RandomTraits.RUSTY_AIM)) {
								didHit = checkRandomChainHit(enemy);
							}

							if (didHit) {
								Actor.add(new Pushing(enemy, enemy.pos, newPosFinal, new Callback() {
									public void call() {
										pullEnemy(enemy, newPosFinal);
									}
								}));
							}
							else {
								Sample.INSTANCE.play(Assets.Sounds.MISS);
								spend(1.0f);

								if (enemy.sprite != null){
									enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
								}
							}
							next();
						}
					}));
				} else {
					pullEnemy(enemy, newPos);
				}
			}
		}
		m_ChainsUsed.Set(true);
		return true;
	}

	private boolean checkRandomChainHit(Char ch) {
		AttackContext context = new AttackContext.Builder(this, ch)
				.attackType(AttackContext.AttackType.RANGED)
				.build();

		return CombatResolver.checkHit(context);
	}

	private void pullEnemy( Char enemy, int pullPos ){
		enemy.pos = pullPos;
		enemy.sprite.place(pullPos);
		Dungeon.level.occupyCell(enemy);
		if (!getRandomizerEnabled(RandomTraits.GENTLE_CHAIN)) {
			if (getRandomizerEnabled(RandomTraits.EXTENDED_CRIPPLE)) {
				Cripple.prolong(enemy, Cripple.class, 12f);
			} else {
				Cripple.prolong(enemy, Cripple.class, 4f);
			}
		}
		if (enemy == Dungeon.hero) {
			Dungeon.hero.interrupt();
			Dungeon.observe();
			GameScene.updateFog();
		}
	}

	@Override
	public float GetLootChance(int slot) {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.GetLootChance(slot) * (float)Math.pow(1/3f, Dungeon.LimitedDrops.GUARD_ARM.count);
	}

	@Override
	public Item createLoot(int itemSlot) {
		Dungeon.LimitedDrops.GUARD_ARM.count++;
		return super.createLoot(itemSlot);
	}

	private BundleableProperty.Bool m_ChainsUsed = new BundleableProperty.Bool("chainsused", false);

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		m_ChainsUsed.Store(bundle);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		m_ChainsUsed.Restore(bundle);
	}

	private static class Hunting extends Mob.Hunting{
		@Override
		public boolean act( Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			Guard g = (Guard)mob;
			g.m_EnemySeen.Set(enemyInFOV);

			int maxDistance = 5;
			if (getRandomizerEnabled(RandomTraits.SHORT_LEASH)) maxDistance = 3;
			
			if ((!g.m_ChainsUsed.Get() || getRandomizerEnabled(RandomTraits.CHAIN_MASTER))
					&& enemyInFOV
					&& !g.isCharmedBy( g.enemy )
					&& !g.canAttack( g.enemy )
					&& Dungeon.level.distance( g.pos, g.enemy.pos ) < maxDistance

					
					&& g.chain(g.enemy.pos)){
				return !(g.sprite.visible || g.enemy.sprite.visible);
			} else {
				return super.act( g, enemyInFOV, justAlerted );
			}
			
		}
	}

	public enum RandomTraits {
		PIT_PULLER, EXTENDED_CRIPPLE, CHAIN_MASTER, GENTLE_CHAIN, SHORT_LEASH, RUSTY_AIM
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case PIT_PULLER: return Randomizer.getCreatureBuff(Guard.class) == 1;
			case EXTENDED_CRIPPLE: return Randomizer.getCreatureBuff(Guard.class) == 2;
			case CHAIN_MASTER: return Randomizer.getCreatureBuff(Guard.class) == 3;
			case GENTLE_CHAIN: return Randomizer.getCreatureNerf(Guard.class) == 1;
			case SHORT_LEASH: return Randomizer.getCreatureNerf(Guard.class) == 2;
			case RUSTY_AIM: return Randomizer.getCreatureNerf(Guard.class) == 3;
		}
		return false;
	}
}