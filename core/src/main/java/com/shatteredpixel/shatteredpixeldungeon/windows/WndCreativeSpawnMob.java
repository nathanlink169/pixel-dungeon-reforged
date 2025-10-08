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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.EnemyRegistry;
import com.shatteredpixel.shatteredpixeldungeon.ItemRegistry;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.RatUsurper;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class WndCreativeSpawnMob extends Window {
	private static final int BTN_HEIGHT = 12;
	private static final int GAP = 2;
	private static final int FONT_SIZE = 5;

	public static final int WIDTH_P     = 126;
	public static final int HEIGHT_P    = 180;

	public static final int WIDTH_L     = 216;
	public static final int HEIGHT_L    = 130;

	private ArrayList<RedButton> buttons;

	public WndCreativeSpawnMob() {
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
		IconTitle title = new IconTitle(Icons.get(Icons.SKULL), Messages.get(this, "title"));
		title.setRect(0, 0, w, 0);
		title.setPos(0, 0);
		content.add(title);

		float pos = (int) title.bottom() + GAP;
		float btnWidth = (w - GAP) / 2f;

		for (int i = 0; i < EnemyRegistry.ALL_ENEMIES.length; i++) {
			final Class<?> enemyClass = EnemyRegistry.ALL_ENEMIES[i];

			String itemName = Messages.titleCase(Messages.get(enemyClass, "name"));

			RedButton btn = new RedButton(itemName, FONT_SIZE) {
				@Override
				protected void onClick() {
					super.onClick();
					try {
						Sample.INSTANCE.play( Assets.Sounds.CLICK );
						Mob mob = (Mob) Reflection.newInstance(enemyClass);
						if (mob != null) {
							spawnAround(mob, Dungeon.hero.pos);
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

	public static void spawnAround(Mob mob, int pos) {
		int[] neighbours = GetRandomNeighbours();

		for (int n : neighbours) {
			int cell = pos + n;
			if (Dungeon.level.passable[cell] && Actor.findChar(cell) == null) {
				mob.pos = cell;
				mob.state = mob.HUNTING;
				GameScene.add( mob );
				Dungeon.level.occupyCell(mob);
				GLog.n("Spawned " + mob.name(true) + "!");
				return;
			}
		}

		GLog.n("No space to spawn mob.");
	}

	private static int[] GetRandomNeighbours() {
		int[] neighbours = PathFinder.NEIGHBOURS8;
		int index;
		for (int i = neighbours.length - 1; i > 0; i--)
		{
			index = Random.Int(i + 1);
			if (index != i)
			{
				neighbours[index] ^= neighbours[i];
				neighbours[i] ^= neighbours[index];
				neighbours[index] ^= neighbours[i];
			}
		}
		return neighbours;
	}
}