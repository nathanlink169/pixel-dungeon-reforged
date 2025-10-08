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

import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;
import com.watabou.noosa.ui.Component;

public class WndTwoOptions extends Window {

	protected static final int WIDTH_MIN    = 120;
	protected static final int WIDTH_MAX    = 220;
	protected static final int GAP	= 2;
	private static final int BUTTON_HEIGHT = 16;
	private static final int MARGIN = 1;

	public WndTwoOptions(Image icon, String title, String message, String option1, String option2 ) {

		this( new IconTitle( icon, title ), message, option1, option2 );

	}

	public WndTwoOptions(Component titlebar, String message, String option1, String option2 ) {

		super();

		int width = WIDTH_MIN;

		titlebar.setRect( 0, 0, width, 0 );
		add(titlebar);

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		if (!useHighlighting()) text.setHightlighting(false);
		text.text( message, width );
		text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
		add( text );

		while (PixelScene.landscape()
				&& text.bottom() > targetHeight()
				&& width < WIDTH_MAX){
			width += 20;
			titlebar.setRect(0, 0, width, 0);
			text.setPos( titlebar.left(), titlebar.bottom() + 2*GAP );
			text.maxWidth(width);
		}

		bringToFront(titlebar);

		// resize first as the width of the buttons depends on the width of the window
		resize( width, (int)text.bottom() + 2 );

		final RedButton firstButton = new RedButton(option1) {
			@Override
			protected void onClick() {
				onSelect(0);
				hide();
			}
		};
		final RedButton secondButton = new RedButton(option2) {
			@Override
			protected void onClick() {
				onSelect(1);
				hide();
			}
		};

		float btnWidth = (width / 2.0f) - 2 * MARGIN;
		firstButton.setRect(MARGIN, text.bottom() + MARGIN * 2, btnWidth - MARGIN * 2, BUTTON_HEIGHT);
		secondButton.setRect(firstButton.right() + MARGIN * 2, firstButton.top(), firstButton.width(), BUTTON_HEIGHT);
		add(firstButton);
		add(secondButton);

		resize( width, (int)secondButton.bottom() + 2 );
	}

	// 0 is first option, 1 is second
	public void onSelect(int option) { }

	protected boolean useHighlighting(){
		return true;
	}

	protected float targetHeight() {
		return PixelScene.MIN_HEIGHT_L - 10;
	}
}
