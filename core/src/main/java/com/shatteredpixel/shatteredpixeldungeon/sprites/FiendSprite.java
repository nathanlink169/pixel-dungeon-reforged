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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Necromancer;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.particles.Emitter;

public class FiendSprite extends MobSprite {
	@Override
	protected void setupFrames() {
		texture( Assets.Sprites.FIEND );
		TextureFilm frames = new TextureFilm( texture, 16, 16 );

		idle = new Animation( 5, true );
		idle.frames(frames, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 4, 10, 11, 10, 4, 1, 1 );

		run = new Animation( 5, true );
		run.frames( frames, 0, 1, 2, 3 );

		attack = new Animation( 15, false );
		attack.frames( frames, 10, 11, 12, 13, 14, 15 );

		die = new Animation( 10, false );
		die.frames( frames, 16, 17, 18, 19, 19, 19 );
	}
}
