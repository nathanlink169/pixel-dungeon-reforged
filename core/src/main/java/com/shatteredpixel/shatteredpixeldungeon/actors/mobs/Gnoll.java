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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.AscensionChallenge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Pushing;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SparkParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Gnoll extends Mob {
	
	{
		HP = HT = 12;
		defenseSkill = 4;
		
		EXP = 2;
		maxLvl = 8;
		
		loot = Gold.class;
		lootChance = 0.5f;

		WANDERING = new Wandering();
		HUNTING = getRandomizerEnabled(RandomTraits.PACIFIST_PATROL) ? new Fleeing() : new Hunting();
	}
	protected int partnerID = -1;
	protected boolean seenPlayer = false;

	@Override
	protected void onAdd(){
		boolean previousFirstAdded = firstAdded;
		super.onAdd();
		if (previousFirstAdded && getRandomizerEnabled(RandomTraits.BATTLE_SCARRED)) {
			// 50%-100% health
			float multiplier = Random.Float(0.5f, 1.0f);
			HP = (int) (HT * multiplier);
		}
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( PARTNER_ID, partnerID );
		bundle.put( SEEN_PLAYER, seenPlayer);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		partnerID = bundle.getInt( PARTNER_ID );
		seenPlayer = bundle.getBoolean( SEEN_PLAYER );
	}

	private static final String PARTNER_ID = "partner_id";
	private static final String SEEN_PLAYER = "seen_player";
	@Override
	public Class<? extends CharSprite> GetSpriteClass() {

		return GnollSprite.class;
	}

	@Override
	protected boolean act() {
		//create a child
		if (getRandomizerEnabled(RandomTraits.PACK_HUNTERS) && partnerID == -1 && !(this instanceof GnollExile)){

			ArrayList<Integer> candidates = new ArrayList<>();

			int[] neighbours = {pos + 1, pos - 1, pos + Dungeon.level.width(), pos - Dungeon.level.width()};
			for (int n : neighbours) {
				if (Dungeon.level.passable[n]
						&& Actor.findChar( n ) == null
						&& (!Char.hasProp(this, Property.LARGE) || Dungeon.level.openSpace[n])) {
					candidates.add( n );
				}
			}

			if (!candidates.isEmpty()){
				Gnoll child = new Gnoll();
				child.partnerID = this.id();
				this.partnerID = child.id();
				if (state != SLEEPING) {
					child.state = child.WANDERING;
				}

				child.pos = Random.element( candidates );

				GameScene.add( child );
				Dungeon.level.occupyCell(child);

				if (sprite.visible) {
					Actor.add( new Pushing( child, pos, child.pos ) );
				}

				//champion buff, mainly
				for (Buff b : buffs()){
					if (b.revivePersists) {
						Buff.affect(child, b.getClass());
					}
				}

			}
		}


		return super.act();
	}

	public void notice() {
		super.notice();
		if (!seenPlayer && getRandomizerEnabled(RandomTraits.ALARM_NETWORK)) {
			if (fieldOfView[Dungeon.hero.pos] && Dungeon.hero.invisible <= 0) {
				seenPlayer = true;
				CellEmitter.center( pos ).start( Speck.factory( Speck.SCREAM ), 0.3f, 3 );
				Sample.INSTANCE.play( Assets.Sounds.ALERT );

				for (Mob mob : Dungeon.level.mobs) {
					mob.beckon( pos );
				}
			}
		}
	}
	
	@Override
	public int damageRoll(boolean isMaxDamage) {
		if (isMaxDamage) return 6;
		return Random.NormalIntRange( 1, 6 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 10;
	}
	
	@Override
	public int drRoll() {
		if (getRandomizerEnabled(RandomTraits.BRITTLE_ARMOUR)) {
			return super.drRoll() - 1;
		}
		return super.drRoll() + Random.NormalIntRange(0, 2);
	}

	private class Wandering extends Mob.Wandering {

		@Override
		protected boolean continueWandering() {
			if (!getRandomizerEnabled(RandomTraits.PACK_HUNTERS)) {
				return super.continueWandering();
			}

			enemySeen = false;

			Gnoll partner = (Gnoll) Actor.findById( partnerID );
			if (partner != null && (partner.state != partner.WANDERING || Dungeon.level.distance( pos,  partner.target) > 1)){
				target = partner.pos;
				int oldPos = pos;
				if (getCloser( target )){
					spend( 1 / speed() );
					return moveSprite( oldPos, pos );
				} else {
					spend( TICK );
					return true;
				}
			} else {
				return super.continueWandering();
			}
		}
	}

	@Override
	protected boolean canAttack( Char enemy ) {
		if (!getRandomizerEnabled(RandomTraits.SLING_MASTERS))
			return super.canAttack(enemy);

		return super.canAttack(enemy)
				|| new Ballistica( pos, enemy.pos, Ballistica.PROJECTILE).collisionPos == enemy.pos;
	}

	public enum RandomTraits {
		PACK_HUNTERS, SLING_MASTERS, ALARM_NETWORK, BATTLE_SCARRED, BRITTLE_ARMOUR, PACIFIST_PATROL
	}

	public static boolean getRandomizerEnabled(RandomTraits r) {
		switch (r) {
			case PACK_HUNTERS: return Randomizer.getCreatureBuff(Gnoll.class) == 1;
			case SLING_MASTERS: return Randomizer.getCreatureBuff(Gnoll.class) == 2;
			case ALARM_NETWORK: return Randomizer.getCreatureBuff(Gnoll.class) == 3;
			case BATTLE_SCARRED: return Randomizer.getCreatureNerf(Gnoll.class) == 1;
			case BRITTLE_ARMOUR: return Randomizer.getCreatureNerf(Gnoll.class) == 2;
			case PACIFIST_PATROL: return Randomizer.getCreatureNerf(Gnoll.class) == 3;
		}
		return false;
	}
}