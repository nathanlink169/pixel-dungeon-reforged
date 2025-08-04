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
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Elemental;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.GnollGeomancer;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Wyrm;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;

public class WyrmSprite extends MobSprite {

	boolean isSleeping = false;

	@Override
	protected void setupFrames() {
		texture( Assets.Sprites.WYRM );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		if (ch != null && ch instanceof Mob) {
			isSleeping = ((Mob) ch).state == ((Mob) ch).SLEEPING;
		}

		if (isSleeping) {
			idle = new Animation( 1, true );
			idle.frames( frames, 0, 0, 0, 1);
		} else {
			idle = new Animation( 3, true );
			idle.frames( frames, 2, 4, 5, 2, 4, 5, 2, 4, 5, 2, 3, 4, 5);
		}

		run = new Animation( 10, true );
		run.frames( frames, 2, 4, 5 );

		attack = new Animation( 15, false );
		attack.frames( frames, 2, 6, 7);

		die = new Animation( 10, false );
		die.frames( frames, 8, 9, 10, 11 );

		scale.set(1.5f);
	}


	@Override
	public void link( Char ch ) {
		super.link( ch );

		if (ch != null && ch instanceof Mob){
			isSleeping = ((Mob) ch).state == ((Mob) ch).SLEEPING;
			setup();
		}
	}

	@Override
	public void zap( int cell ) {
		super.zap( cell );

		MagicMissile.boltFromChar( parent,
				MagicMissile.FIRE,
				this,
				cell,
				new Callback() {
					@Override
					public void call() {
						((Wyrm)ch).onZapComplete();
					}
				} );
		Sample.INSTANCE.play( Assets.Sounds.PUFF );
	}
}
