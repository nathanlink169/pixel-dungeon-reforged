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

import static com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene.defaultZoom;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;

import java.util.ArrayList;

public class WndChallenges extends Window {
	private static final int WIDTH		= 120;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP        = 1;

	private boolean editable;
	private ArrayList<CheckBox> boxes;
	private ArrayList<IconButton> infoButtons;
	private ArrayList<Integer> masks = new ArrayList<>();

	public WndChallenges( int checked, boolean editable ) {

		super();

		Challenges.load();

		RectF insets = Game.platform.getSafeInsets(PlatformSupport.INSET_BLK).scale(1f/defaultZoom);
		float h = (Camera.main.height - insets.top - insets.bottom) * 0.8f;

		resize(WIDTH, (int) h);

		this.editable = editable;

		boxes = new ArrayList<>();
		infoButtons = new ArrayList<>();

		ScrollPane pane = new ScrollPane( new Component() ){
			@Override
			public void onClick( float x, float y ) {
				if (WndChallenges.this.editable) {
					for (int i = 0; i < boxes.size(); i++) {
						if (boxes.get(i).onClick(x, y)) {
							break;
						}
					}
				}
				for (int i = 0; i < infoButtons.size(); i++) {
					if (infoButtons.get(i).onClick(x, y)) {
						break;
					}
				}
			}
		};
		add(pane);
		pane.setRect(0, 0, WIDTH, h);

		Component content = pane.content();
		IconTitle title = new IconTitle(Icons.CHALLENGE_COLOR.get(), Messages.get(this, "title"));
		title.setRect(0, 0, WIDTH, 0);
		title.setPos(0, 0);
		content.add(title);

		int lockedCount = 0;

		float pos = (int)title.bottom() + GAP * 3;
		for (int i=0; i < Challenges.NAME_IDS.length; i++) {
			boolean isLocked = !Challenges.IsChallengeUnlocked(Challenges.MASKS[i]);// && !DeviceCompat.isDebug();
			if (isLocked) {
				++lockedCount;
				continue;
			}

			masks.add(Challenges.MASKS[i]);

			final String challenge = isLocked ? "locked" : Challenges.NAME_IDS[i];
			
			CheckBox cb = new CheckBox( Messages.titleCase(Messages.get(Challenges.class, challenge)) ) {
				@Override
				protected void onClick() {
					if (!isLocked) {
						super.onClick();
					}

				}
			};
			cb.checked( (checked & Challenges.MASKS[i]) != 0 );
			cb.active = false;

			if (i > 0) {
				pos += GAP;
			}
			cb.setRect( 0, pos, WIDTH-16, BTN_HEIGHT );

			pane.content().add( cb );
			boxes.add( cb );
			
			IconButton info = new IconButton(Icons.get(Icons.INFO)){
				@Override
				protected void onClick() {
					super.onClick();
					ShatteredPixelDungeon.scene().add(
							new WndMessage(Messages.get(Challenges.class, challenge+"_desc"))
					);
				}
			};
			info.setRect(cb.right(), pos, 16, BTN_HEIGHT);
			info.active = false;
			infoButtons.add(info);
			pane.content().add(info);
			
			pos = cb.bottom();
		}

		if (lockedCount > 0) {
			pos += GAP * 2;
			RenderedTextBlock info = PixelScene.renderTextBlock(Messages.get(this, "locked", lockedCount), 6);
			info.setRect( 0, pos, WIDTH, BTN_HEIGHT );
			info.maxWidth((int) WIDTH);
			add(info);
			pos = info.bottom() + GAP;
		}

		resize((int) WIDTH, (int) Math.min(h, pos));

		content.setRect(0, 0, WIDTH, pos);
		pane.setRect(insets.left, insets.top, WIDTH, h);
	}

	@Override
	public void onBackPressed() {

		if (editable) {
			int value = 0;
			for (int i=0; i < boxes.size(); i++) {
				if (boxes.get( i ).checked()) {
					value |= masks.get(i);
				}
			}
			SPDSettings.challenges( value );
		}

		super.onBackPressed();
	}
}