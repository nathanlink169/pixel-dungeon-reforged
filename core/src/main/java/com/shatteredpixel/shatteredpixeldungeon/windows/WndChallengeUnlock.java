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

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SupporterScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndChallengeUnlock extends Window {

	private static final int WIDTH_MIN = 120;
	private static final int WIDTH_MAX = 220;
	private static final int MARGIN = 4;

	public WndChallengeUnlock(int challengeToShow){

		super();

		int width = WIDTH_MIN;

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				MARGIN,
				MARGIN
		);
		PixelScene.align(title);
		add( title );

		RenderedTextBlock info = PixelScene.renderTextBlock( Messages.get(this, "desc"), 6 );
		info.maxWidth(width - MARGIN * 2);
		info.setPos(MARGIN, title.bottom() + MARGIN);
		add( info );

		while (PixelScene.landscape()
				&& info.height() > 120
				&& width < WIDTH_MAX){
			width += 20;
			info.maxWidth(width - MARGIN * 2);
		}

		final String challenge = Challenges.NAME_IDS[challengeToShow];
		RenderedTextBlock challengeInfo = PixelScene.renderTextBlock("_" + Messages.get(Challenges.class, challenge) + "_\n\n" + Messages.get(Challenges.class, challenge+"_desc"), 6 );
		challengeInfo.maxWidth(width - MARGIN * 2);
		challengeInfo.setPos(MARGIN, info.bottom() + MARGIN * 4);
		add( challengeInfo );

		resize(
				(int)info.width() + MARGIN * 2,
				(int)challengeInfo.bottom() + MARGIN * 2 );

	}
}
