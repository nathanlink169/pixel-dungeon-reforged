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

package com.shatteredpixel.shatteredpixeldungeon.sprites;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.tweeners.AlphaTweener;
import com.watabou.noosa.tweeners.ScaleTweener;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class MobSprite extends CharSprite {

	private static final float FADE_TIME	= 3f;
	private static final float FALL_TIME	= 1f;

	public MobSprite() {
		setup();
	}

	// base setup call
	public void setup() {
		if (forcedNormalSprite) {
			setupFrames();
		} else {
			boolean inGame = false;
			Scene s = ShatteredPixelDungeon.scene();
			if (s instanceof PixelScene) {
				if (s instanceof GameScene) {
					inGame = !((GameScene) s).getJournalOpen();
				} else {
					inGame = ((PixelScene) s).getIsInGameScene();
				}
			}
			if (Dungeon.isChallenged(Challenges.MONSTER_UNKNOWN) && inGame) {
				setupFramesMonsterUnknown();
			} else {
				setupFrames();
			}
		}

		play( idle );
	}

	// setup call no monster_unknown
	protected void setupFrames() { }

	protected void setupFramesMonsterUnknown() {
		texture( Assets.Sprites.MONSTER_UNKNOWN );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 1, true );
		idle.frames( frames, 0, 0, 0, 1, 0, 0, 1, 1 );

		run = new Animation( 20, true );
		run.frames( frames, 2, 3, 4, 5, 6, 7 );

		die = new Animation( 20, false );
		die.frames( frames, 8, 9, 10, 11, 12, 11 );

		attack = new Animation( 15, false );
		attack.frames( frames, 13, 14, 15, 0 );
	}

	@Override
	public void play(Animation anim) {
		//Shouldn't interrupt the dieing animation
		if (curAnim == null || curAnim != die) {
			super.play(anim);
		}
	}

	protected boolean forcedNormalSprite = false;
	public void forceNoMonsterUnknown() {
		forcedNormalSprite = true;
		setup();
		forcedNormalSprite = false;
	}
	
	@Override
	public void update() {
		sleeping = ch != null && ch.isAlive() && ((Mob)ch).state == ((Mob)ch).SLEEPING;
		super.update();
	}
	
	@Override
	public void onComplete( Animation anim ) {
		
		super.onComplete( anim );
		
		if (anim == die && parent != null) {
			parent.add( new AlphaTweener( this, 0, FADE_TIME ) {
				@Override
				protected void onComplete() {
					MobSprite.this.killAndErase();
				}
			} );
		}
	}
	
	public void fall() {
		
		origin.set( width / 2, height - DungeonTilemap.SIZE / 2 );
		angularSpeed = Random.Int( 2 ) == 0 ? -720 : 720;
		am = 1;

		hideEmo();

		if (health != null){
			health.killAndErase();
		}
		
		if (parent != null) parent.add( new ScaleTweener( this, new PointF( 0, 0 ), FALL_TIME ) {
			@Override
			protected void onComplete() {
				MobSprite.this.killAndErase();
				parent.erase( this );
			}
			@Override
			protected void updateValues( float progress ) {
				super.updateValues( progress );
				y += 12 * Game.elapsed;
				am = 1 - progress;
			}
		} );
	}
}
