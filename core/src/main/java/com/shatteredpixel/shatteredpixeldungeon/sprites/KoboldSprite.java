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
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.CrystalGuardian;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;

public abstract class KoboldSprite extends MobSprite {
	@Override
	protected void setupFrames() {
		texture( Assets.Sprites.KOBOLD );

		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		int c = texOffset();

		idle = new MovieClip.Animation( 2, true );
		idle.frames( frames, 0+c, 1+c, 2+c );

		run = idle.clone(); // TODO: I'd like to only use the run animation when they're actually moving
		// run = new MovieClip.Animation( 15, true );
		// run.frames( frames, 0+c, 11+c, 0+c, 12+c );

		attack = new MovieClip.Animation( 12, false );
		attack.frames( frames, 0+c, 3+c, 4+c, 5+c, 6+c );

		die = new MovieClip.Animation( 10, false );
		die.frames( frames, 0+c, 7+c, 8+c, 9+c, 10+c );

		scale.set(0.85f);
	}

	protected abstract int texOffset();

	public static class Blue extends KoboldSprite {
		@Override
		protected int texOffset() {
			return 0;
		}
		@Override
		public int blood() {
			return 0xFF8EE3FF;
		}
	}

	public static class Red extends KoboldSprite {
		@Override
		protected int texOffset() {
			return 13;
		}
		@Override
		public int blood() {
			return 0xFFff8e8e;
		}
	}

	public static class Purple extends KoboldSprite {
		@Override
		protected int texOffset() {
			return 26;
		}
		@Override
		public int blood() {
			return 0xFFd28eff;
		}
	}

}
