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

import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.HeroSelectScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIcon;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

public class WndInfoBuff extends Window {

	private static final float GAP	= 2;

	private static final int WIDTH = 120;

	private Buff m_Buff;

	private Window m_sourceWindow;

	public WndInfoBuff(Buff buff, Window sourceWindow){
		super();

		m_Buff = buff;
		m_sourceWindow = sourceWindow;

		IconTitle titlebar = new IconTitle();

		Image buffIcon = new BuffIcon( buff, true );

		titlebar.icon( buffIcon );
		titlebar.label( Messages.titleCase(buff.name()), Window.TITLE_COLOR );
		titlebar.setRect( 0, 0, WIDTH, 0 );
		add( titlebar );

		RenderedTextBlock txtInfo = PixelScene.renderTextBlock(buff.desc(), 6);
		txtInfo.maxWidth(WIDTH);
		txtInfo.setPos(titlebar.left(), titlebar.bottom() + 2*GAP);
		add( txtInfo );

		if (SPDSettings.creative()) {
			IconButton creativeButton = new IconButton(Icons.get(Icons.CREATIVE)) {
				@Override
				protected void onClick() {
					super.onClick();
					WndTwoOptions removeWindow = new WndTwoOptions(Icons.get(Icons.CHANGES), "Remove", "This will instantly remove the buff. Are you sure?", "Yes", "No") {
						@Override
						// 0 is first option, 1 is second
						public void onSelect(int option) {
							super.onSelect(option);

							if (option == 0) {
								WndInfoBuff.this.hide();
								boolean isBoss = m_Buff.target.properties().contains(Char.Property.BOSS);
								m_Buff.detach();

								if (m_sourceWindow != null) {
									if (m_sourceWindow instanceof WndHero) {
										((WndHero) m_sourceWindow).Reset();
									}
									else if (m_sourceWindow instanceof WndInfoMob) {
										((WndInfoMob) m_sourceWindow).hide();
									}
								}

								if (isBoss && BuffIndicator.hasBossInstance()) {
									BuffIndicator.refreshBoss();
								}
							}
						}
					};
					ShatteredPixelDungeon.scene().addToFront(removeWindow);
				}

				@Override
				protected String hoverText() {
					return Messages.titleCase(Messages.get(WndKeyBindings.class, "creative"));
				}
			};
			creativeButton.setRect(WIDTH - buffIcon.width, buffIcon.y, 16, 16);
			add(creativeButton);
		}

		resize( WIDTH, (int)txtInfo.bottom() + 2 );
	}
}
