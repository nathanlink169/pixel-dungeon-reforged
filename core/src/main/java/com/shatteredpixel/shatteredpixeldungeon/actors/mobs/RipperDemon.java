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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bleeding;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.combat.AttackContext;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatModifier;
import com.shatteredpixel.shatteredpixeldungeon.combat.CombatResolver;
import com.shatteredpixel.shatteredpixeldungeon.combat.DamageType;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.TargetedCell;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.GameMath;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

public class RipperDemon extends Mob implements CombatModifier.AccuracyModifier, CombatModifier.OnDamageEffect {

	{
		HUNTING = new Hunting();
	}

	// TODO: This is hacky
	public boolean isLeaping = false;

	@Override
	public Constants.mobs.mobsBase GetConstants() { return Constants.mobs.ripperdemon; }

	@Override
	public float spawningWeight() {
		return 0;
	}

	@Override
	public int minDamage(AttackContext.AttackType type) {
		return super.minDamage(type) / (getRandomizerEnabled(RandomTraits.DULL_CLAWS) ? 3 : 1);
	}

	@Override
	public int maxDamage(AttackContext.AttackType type) {
		return (int) (super.maxDamage(type) * (getRandomizerEnabled(RandomTraits.DULL_CLAWS) ? 0.8f : 1.0f));
	}

	private static final String LAST_ENEMY_POS = "last_enemy_pos";
	private static final String LEAP_POS = "leap_pos";
	private static final String LEAP_CD = "leap_cd";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(LAST_ENEMY_POS, lastEnemyPos);
		bundle.put(LEAP_POS, leapPos);
		bundle.put(LEAP_CD, leapCooldown);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		lastEnemyPos = bundle.getInt(LAST_ENEMY_POS);
		leapPos = bundle.getInt(LEAP_POS);
		leapCooldown = bundle.getFloat(LEAP_CD);
	}

	private int lastEnemyPos = -1;

	@Override
	protected boolean act() {
		if (state == WANDERING){
			leapPos = -1;
		}

		AiState lastState = state;
		boolean result = super.act();
		if (paralysed <= 0) leapCooldown --;

		//if state changed from wandering to hunting, we haven't acted yet, don't update.
		if (!(lastState == WANDERING && state == HUNTING)) {
			if (enemy != null) {
				lastEnemyPos = enemy.pos;
			} else {
				lastEnemyPos = Dungeon.hero.pos;
			}
		}

		return result;
	}

	private int leapPos = -1;
	private float leapCooldown = 0;

	@Override
	public float modifyAccuracy(AttackContext context, float currentAccuracy) {
		return isLeaping ? Char.INFINITE_ACCURACY : currentAccuracy;
	}

	@Override
	public void onDamage(AttackContext context, int damageDealt) {
		if (Random.Int( 4 ) == 0) {
			Buff.affect( enemy, Bleeding.class ).set( damageDealt );
		}
		if (getRandomizerEnabled(RandomTraits.HEMORRHAGE)) {
			if (Random.Int(4) != 0) { // 75% chance
				int duration = Random.IntRange(10, 20);
				Buff.affect(enemy, Bleeding.class).set(duration);
			}
		}
		if (getRandomizerEnabled(RandomTraits.TOXIC_CLAWS)) {
			if (Random.Int(3) == 0) { // 33% chance
				int duration = Random.IntRange(10, 20);
				Buff.affect(enemy, Poison.class).set(duration);
			}
		}
	}

	@Override
	public int priority() {
		return isLeaping ? Priority.HIGHEST : Priority.NORMAL;
	}

	@Override
	public boolean appliesTo(AttackContext context) {
		return context.attacker == this;
	}

	public static class Hunting extends Mob.Hunting {

		@Override
		public boolean act(Mob mob, boolean enemyInFOV, boolean justAlerted ) {
			RipperDemon rd = (RipperDemon) mob;
			if (rd.leapPos != -1){

				rd.leapCooldown = Random.NormalIntRange(2, 4);

				if (rd.rooted){
					rd.leapPos = -1;
					return true;
				}

				Ballistica b = new Ballistica(rd.pos, rd.leapPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
				rd.leapPos = b.collisionPos;

				final Char leapVictim = Actor.findChar(rd.leapPos);
				final int endPos;

				//ensure there is somewhere to land after leaping
				if (leapVictim != null){
					int bouncepos = -1;
					//attempt to bounce in free passable space
					for (int i : PathFinder.NEIGHBOURS8){
						if ((bouncepos == -1 || Dungeon.level.trueDistance(rd.pos, rd.leapPos+i) < Dungeon.level.trueDistance(rd.pos, bouncepos))
								&& Actor.findChar(rd.leapPos+i) == null && Dungeon.level.passable[rd.leapPos+i]){
							bouncepos = rd.leapPos+i;
						}
					}
					//try again, allowing a bounce into any non-solid terrain
					if (bouncepos == -1){
						for (int i : PathFinder.NEIGHBOURS8){
							if ((bouncepos == -1 || Dungeon.level.trueDistance(rd.pos, rd.leapPos+i) < Dungeon.level.trueDistance(rd.pos, bouncepos))
									&& Actor.findChar(rd.leapPos+i) == null && !Dungeon.level.solid[rd.leapPos+i]){
								bouncepos = rd.leapPos+i;
							}
						}
					}
					//if no valid position, cancel the leap
					if (bouncepos == -1) {
						rd.leapPos = -1;
						return true;
					} else {
						endPos = bouncepos;
					}
				} else {
					endPos = rd.leapPos;
				}

				//do leap
				rd.sprite.visible = Dungeon.level.heroFOV[rd.pos] || Dungeon.level.heroFOV[rd.leapPos] || Dungeon.level.heroFOV[endPos];
				rd.sprite.jump(rd.pos, rd.leapPos, new Callback() {
					@Override
					public void call() {

						if (leapVictim != null && rd.alignment != leapVictim.alignment) {
							rd.isLeaping = true;
							AttackContext context = new AttackContext.Builder(rd, leapVictim)
									.attackType(AttackContext.AttackType.RANGED)
									.build();

							if (CombatResolver.checkHit(context)) {
								Buff.affect(leapVictim, Bleeding.class).set(0.75f * rd.damageRoll(AttackContext.AttackType.MELEE, false));
								leapVictim.sprite.flash();
								Sample.INSTANCE.play(Assets.Sounds.HIT);
							} else {
								leapVictim.sprite.showStatus( CharSprite.NEUTRAL, leapVictim.defenseVerb() );
								Sample.INSTANCE.play(Assets.Sounds.MISS);
							}
							rd.isLeaping = false;
						}

						if (endPos != rd.leapPos){
							Actor.add(new Pushing(rd, rd.leapPos, endPos));
						}

						rd.pos = endPos;
						rd.leapPos = -1;
						rd.sprite.idle();
						Dungeon.level.occupyCell(rd);
						rd.next();
					}
				});
				return false;
			}

			rd.m_EnemySeen.Set(enemyInFOV);
			if (enemyInFOV && !rd.isCharmedBy( rd.enemy ) && rd.canAttack( rd.enemy )) {

				rd.recentlyAttackedBy.clear();
				rd.m_Target.Set(rd.enemy.pos);
				return rd.doAttack( rd.enemy );

			} else {

				if (enemyInFOV) {
					rd.m_Target.Set(rd.enemy.pos);
				} else if (rd.enemy == null) {
					rd.state = rd.WANDERING;
					rd.m_Target.Set(Dungeon.level.randomDestination( rd ));
					return true;
				}

				if (rd.leapCooldown <= 0 && enemyInFOV && !rd.rooted
						&& Dungeon.level.distance(rd.pos, rd.enemy.pos) >= 3) {

					int targetPos = rd.enemy.pos;
					if (rd.lastEnemyPos != rd.enemy.pos){
						int closestIdx = 0;
						for (int i = 1; i < PathFinder.CIRCLE8.length; i++){
							if (Dungeon.level.trueDistance(rd.lastEnemyPos, rd.enemy.pos+PathFinder.CIRCLE8[i])
									< Dungeon.level.trueDistance(rd.lastEnemyPos, rd.enemy.pos+PathFinder.CIRCLE8[closestIdx])){
								closestIdx = i;
							}
						}
						targetPos = rd.enemy.pos + PathFinder.CIRCLE8[(closestIdx+4)%8];
					}

					Ballistica b = new Ballistica(rd.pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					//try aiming directly at hero if aiming near them doesn't work
					if (b.collisionPos != targetPos && targetPos != rd.enemy.pos){
						targetPos = rd.enemy.pos;
						b = new Ballistica(rd.pos, targetPos, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID);
					}
					if (b.collisionPos == targetPos){
						//get ready to leap
						rd.leapPos = targetPos;
						//don't want to overly punish players with slow move or attack speed
						if (getRandomizerEnabled(RandomTraits.SLUGGISH_LEAP)) {
							rd.spend(GameMath.gate(rd.attackDelay() * 2, (int)Math.ceil(rd.enemy.cooldown()) * 2, 6*rd.attackDelay()));
						} else {
							rd.spend(GameMath.gate(rd.attackDelay(), (int) Math.ceil(rd.enemy.cooldown()), 3 * rd.attackDelay()));
						}
						if (Dungeon.level.heroFOV[rd.pos] || Dungeon.level.heroFOV[rd.leapPos]){
							GLog.w(Messages.get(rd, "leap"));
							rd.sprite.parent.addToBack(new TargetedCell(rd.leapPos, 0xFF0000));
							((RipperSprite)rd.sprite).leapPrep( rd.leapPos );
							Dungeon.hero.interrupt();
						}
						return true;
					}
				}

				int oldPos = rd.pos;
				if (rd.m_Target.Get() != -1 && rd.getCloser( rd.m_Target.Get() )) {

					rd.spend( 1 / rd.speed() );
					return rd.moveSprite( oldPos,  rd.pos );

				} else {
					rd.spend( TICK );
					if (!enemyInFOV) {
						rd.sprite.showLost();
						rd.state = rd.WANDERING;
						rd.m_Target.Set(Dungeon.level.randomDestination( rd ));
					}
					return true;
				}
			}
		}

	}

	public enum RandomTraits {
		RAPID_DEPLOYMENT, HEMORRHAGE, TOXIC_CLAWS, SLUGGISH_LEAP, DULL_CLAWS, LAZY_SPAWNERS
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case RAPID_DEPLOYMENT: return Randomizer.getCreatureBuff(RipperDemon.class) == 1;
			case HEMORRHAGE: return Randomizer.getCreatureBuff(RipperDemon.class) == 2;
			case TOXIC_CLAWS: return Randomizer.getCreatureBuff(RipperDemon.class) == 3;
			case SLUGGISH_LEAP: return Randomizer.getCreatureNerf(RipperDemon.class) == 1;
			case DULL_CLAWS: return Randomizer.getCreatureNerf(RipperDemon.class) == 2;
			case LAZY_SPAWNERS: return Randomizer.getCreatureNerf(RipperDemon.class) == 3;
		}
		return false;
	}
}