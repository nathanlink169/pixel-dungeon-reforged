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
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Randomizer;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.SupporterScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

public class WndRandomizerDisplay extends Window {

	protected static final int WIDTH_P    = 120;
	protected static final int WIDTH_L    = 200;

	public WndRandomizerDisplay(int region) {

		int width = PixelScene.landscape() ? WIDTH_L : WIDTH_P;

		IconTitle title = new IconTitle(Icons.get(Icons.RANDOMIZER), Messages.get(WndRandomizerDisplay.class, "title"));
		title.setRect( 0, 0, width, 0 );
		add(title);

		RenderedTextBlock text = PixelScene.renderTextBlock( 6 );
		text.text( getRandomizerDescriptionForRegion(region), width );
		text.setPos( title.left(), title.bottom() + 4 );
		add( text );

		RedButton close = new RedButton(Messages.get(this, "close")){
			@Override
			protected void onClick() {
				super.onClick();
				SPDSettings.supportNagged(true);
				WndRandomizerDisplay.super.hide();
			}
		};
		close.setRect(0, text.bottom() + 2, width, 18);
		add(close);

		resize(width, (int)close.bottom());

	}

	public static String getRandomizerDescriptionForRegion(int region) {
		if (!Dungeon.randomizerEnabled) return "";

		if (Dungeon.isChallenged(Challenges.MONSTER_UNKNOWN)) {
			return Messages.get(Randomizer.class, "unknown.creaturename") + " — " + Messages.get(Randomizer.class, "unknown.buff.name") + " — " + Messages.get(Randomizer.class, "unknown.buff.desc") + "\n\n" +
					Messages.get(Randomizer.class, "unknown.creaturename") + " — " + Messages.get(Randomizer.class, "unknown.nerf.name") + " — " + Messages.get(Randomizer.class, "unknown.nerf.desc");
		}

		Class<? extends Char> buffedCreature = Randomizer.getBuffedCreature(region);
		int buffNumber = Randomizer.getCreatureBuff(buffedCreature);
		String buffTextKey = buffedCreature.getSimpleName().toLowerCase() + ".buff" + buffNumber + ".";
		String buffCreatureName = Messages.get(buffedCreature, "name").toUpperCase();
		String buffName = Messages.get(Randomizer.class, buffTextKey + "name");
		String buffDesc = Messages.get(Randomizer.class, buffTextKey + "desc");
		String buffTextFinal = buffCreatureName + " — " + buffName + ": " + buffDesc;

		Class<? extends Char> nerfedCreature = Randomizer.getNerfedCreature(region);
		int nerfNumber = Randomizer.getCreatureNerf(nerfedCreature);
		String nerfTextKey = nerfedCreature.getSimpleName().toLowerCase() + ".nerf" + nerfNumber + ".";
		String nerfCreatureName = Messages.get(nerfedCreature, "name").toUpperCase();
		String nerfName = Messages.get(Randomizer.class, nerfTextKey + "name");
		String nerfDesc = Messages.get(Randomizer.class, nerfTextKey + "desc");
		String nerfTextFinal = nerfCreatureName + " — " + nerfName + ": " + nerfDesc;

		return buffTextFinal + "\n\n" + nerfTextFinal + "\n";
	}

	@Override
	public void hide() {
		//do nothing, have to close via the close button
	}
}
