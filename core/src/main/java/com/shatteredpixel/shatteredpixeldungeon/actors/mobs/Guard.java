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
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Bless;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ChampionEnemy;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cripple;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Cursed;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Daze;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hex;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Chains;
import com.shatteredpixel.shatteredpixeldungeon.effects.Effects;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.FerretTuft;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GuardSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public class Guard extends Mob {

	//they can only use their chains once
	private boolean chainsUsed = false;

	{
		HP = HT = 40;
		defenseSkill = 10;

		EXP = 7;
		maxLvl = 14;

		loot = Generator.Category.ARMOR;
		lootChance = 0.2f; //by default, see lootChance()

		properties.add(Property.UNDEAD);
		
		HUNTING = new Hunting();
	}
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return GuardSprite.class;
	}

	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 12;
		return Random.NormalIntRange(4, 12);
	}

	private boolean chain(int target){
		if ((chainsUsed && !getRandomizerEnabled(RandomTraits.CHAIN_MASTER)) || enemy.properties().contains(Property.IMMOVABLE))
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
				this.target = newPos;

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
							boolean[] tuftDodged = { false };
							if (getRandomizerEnabled(RandomTraits.RUSTY_AIM)) {
								didHit = checkRandomChainHit(enemy, tuftDodged);
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
									if (tuftDodged[0]){
										//dooking is a playful sound Ferrets can make, like low pitched chirping
										// I doubt this will translate, so it's only in English
										if (Messages.lang() == Languages.ENGLISH && Random.Int(10) == 0) {
											enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, "dooked", FloatingText.TUFT);
										} else {
											enemy.sprite.showStatusWithIcon(CharSprite.NEUTRAL, enemy.defenseVerb(), FloatingText.TUFT);
										}
									} else {
										enemy.sprite.showStatus(CharSprite.NEUTRAL, enemy.defenseVerb());
									}
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
		chainsUsed = true;
		return true;
	}

	private boolean checkRandomChainHit(Char ch, boolean[] tuftDodged) {
		float acuStat = this.attackSkill( ch );
		float defStat = ch.defenseSkill( this );

		acuStat *= 1.5f; // All attacks are 50% more accurate in reforged.

		if (ch.buff(Talent.ArtificerFoodEvasionBonus.class) != null) {
			if (((Hero)ch).pointsInTalent(Talent.QUICK_CALIBRATION) == 2 || Random.Int(4) == 0) {
				defStat = INFINITE_EVASION;
			}
		}

		//if accuracy or evasion are large enough, treat them as infinite.
		//note that infinite evasion beats infinite accuracy
		if (defStat >= INFINITE_EVASION){
			return false;
		} else if (acuStat >= INFINITE_ACCURACY){
			return true;
		}

		float acuRoll = Random.Float( acuStat );
		if (this.buff(Bless.class) != null) acuRoll *= 1.25f;
		if (this.buff(   Hex.class) != null) acuRoll *= 0.8f;
		if (this.buff(  Daze.class) != null) acuRoll *= 0.5f;
		if (this.buff(Cursed.class) != null) acuRoll *= 0.9f;
		for (ChampionEnemy buff : this.buffs(ChampionEnemy.class)){
			acuRoll *= buff.evasionAndAccuracyFactor();
		}
		acuRoll *= AscensionChallenge.statModifier(this);
		float defRoll = Random.Float( defStat );
		if (ch.buff(Bless.class) != null) defRoll *= 1.25f;
		if (ch.buff(   Hex.class) != null) defRoll *= 0.8f;
		if (ch.buff(  Daze.class) != null) defRoll *= 0.5f;
		if (ch.buff(Cursed.class) != null) defRoll *= 0.7f;
		for (ChampionEnemy buff : ch.buffs(ChampionEnemy.class)){
			defRoll *= buff.evasionAndAccuracyFactor();
		}
		defRoll *= AscensionChallenge.statModifier(ch);
		if (Dungeon.hero.heroClass != HeroClass.CLERIC
				&& Dungeon.hero.hasTalent(Talent.BLESS)
				&& ch.alignment == Alignment.ALLY){
			// + 3%/5%
			defRoll *= 1.01f + 0.02f*Dungeon.hero.pointsInTalent(Talent.BLESS);
		}

		if (defRoll < acuRoll && (defRoll* FerretTuft.evasionMultiplier()) >= acuRoll){
			tuftDodged[0] = true;
		}
		defRoll *= FerretTuft.evasionMultiplier();

		return acuRoll >= defRoll;
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
	public int attackSkill( Char target ) {
		return 12;
	}

	@Override
	public int drRoll() {
		return super.drRoll() + Random.NormalIntRange(0, 7);
	}

	@Override
	public float lootChance() {
		//each drop makes future drops 1/3 as likely
		// so loot chance looks like: 1/5, 1/15, 1/45, 1/135, etc.
		return super.lootChance() * (float)Math.pow(1/3f, Dungeon.LimitedDrops.GUARD_ARM.count);
	}

	@Override
	public Item createLoot() {
		Dungeon.LimitedDrops.GUARD_ARM.count++;
		return super.createLoot();
	}

	private final String CHAINSUSED = "chainsused";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(CHAINSUSED, chainsUsed);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		chainsUsed = bundle.getBoolean(CHAINSUSED);
	}
	
	private class Hunting extends Mob.Hunting{
		@Override
		public boolean act( boolean enemyInFOV, boolean justAlerted ) {
			enemySeen = enemyInFOV;

			int maxDistance = 5;
			if (getRandomizerEnabled(RandomTraits.SHORT_LEASH)) maxDistance = 3;
			
			if ((!chainsUsed || getRandomizerEnabled(RandomTraits.CHAIN_MASTER))
					&& enemyInFOV
					&& !isCharmedBy( enemy )
					&& !canAttack( enemy )
					&& Dungeon.level.distance( pos, enemy.pos ) < maxDistance

					
					&& chain(enemy.pos)){
				return !(sprite.visible || enemy.sprite.visible);
			} else {
				return super.act( enemyInFOV, justAlerted );
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