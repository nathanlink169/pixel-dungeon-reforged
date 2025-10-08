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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ItemRegistry;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.RectF;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class WndCreativeInventory extends Window {
	private static final int BTN_HEIGHT = 12;
	private static final int GAP = 2;
	private static final int FONT_SIZE = 5;

	public static final int WIDTH_P     = 126;
	public static final int HEIGHT_P    = 180;

	public static final int WIDTH_L     = 216;
	public static final int HEIGHT_L    = 130;

	private ArrayList<RedButton> buttons;

	public WndCreativeInventory() {
		super();

		int w = PixelScene.landscape() ? WIDTH_L : WIDTH_P;
		int h = PixelScene.landscape() ? HEIGHT_L : HEIGHT_P;

		resize((int) w, (int) h);

		buttons = new ArrayList<>();

		ScrollPane pane = new ScrollPane(new Component()) {
			@Override
			public void onClick(float x, float y) {
				for (int i = 0; i < buttons.size(); i++) {
					if (buttons.get(i).onClick(x, y)) {
						break;
					}
				}
			}
		};
		add(pane);

		Component content = pane.content();
		IconTitle title = new IconTitle(Icons.get(Icons.BACKPACK), Messages.get(this, "title"));
		title.setRect(0, 0, w, 0);
		title.setPos(0, 0);
		content.add(title);

		float pos = (int) title.bottom() + GAP;
		float btnWidth = (w - GAP) / 2f;

		for (int i = 0; i < ItemRegistry.ALL_ITEMS.length; i++) {
			final Class<?> itemClass = ItemRegistry.ALL_ITEMS[i];

			String itemName = Messages.titleCase(Messages.get(itemClass, "name"));

			RedButton btn = new RedButton(itemName, FONT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					try {
						Sample.INSTANCE.play( Assets.Sounds.CLICK );
						Item item = (Item) Reflection.newInstance(itemClass);
						if (item != null) {
							item.identify();
							if (!item.collect()) {
								Dungeon.level.drop(item, Dungeon.hero.pos);
								GLog.p("Dropped " + itemName);
							} else {
								GLog.p("Gave " + itemName);
							}
						}
					} catch (Exception e) {
						// Handle instantiation errors
					}
				}
			};

			boolean isLeftColumn = (i % 2 == 0);
			float x = isLeftColumn ? 0 : btnWidth + GAP;

			btn.setRect(x, pos, btnWidth, BTN_HEIGHT);
			btn.active = false;

			content.add(btn);
			buttons.add(btn);

			if (!isLeftColumn || i == ItemRegistry.ALL_ITEMS.length - 1) {
				pos += BTN_HEIGHT + GAP;
			}
		}

		content.setSize(w, pos);
		pane.setRect(0, 0, w, h);
	}
}