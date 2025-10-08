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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.ArmorAbility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.abilities.cleric.Trinity;
import com.shatteredpixel.shatteredpixeldungeon.effects.Enchanting;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.TalentsPane;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.audio.Sample;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class WndHeroCreative extends Window {

	private static int WIDTH = 120;
	private static final int TTL_HEIGHT = 16;
	private static final int STAT_HEIGHT = 10;
	private static final int BTN_HEIGHT = 16;
	private static final int GAP = 4;

	public WndHeroCreative(){

		RenderedTextBlock title = PixelScene.renderTextBlock( Messages.get(this, "title"), 12 );
		title.hardlight( TITLE_COLOR );
		title.setPos(
				(WIDTH - title.width()) / 2,
				(TTL_HEIGHT - title.height()) / 2
		);
		PixelScene.align(title);
		add( title );

		float pos = TTL_HEIGHT + BTN_HEIGHT;

		///
		/// STRENGTH
		///
		RenderedTextBlock strengthLabel = PixelScene.renderTextBlock( Messages.get(this, "strength"), STAT_HEIGHT );
		strengthLabel.setPos(0, pos);
		PixelScene.align(strengthLabel);
		add( strengthLabel );
		pos = strengthLabel.bottom() + BTN_HEIGHT;

		RedButton strengthButtonPlus = new RedButton("+1"){
			@Override
			protected void onClick() {
				Dungeon.hero.STR++;
				Dungeon.hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, "1", FloatingText.STRENGTH);

				GLog.p( Messages.get(PotionOfStrength.class, "msg", Dungeon.hero.STR()) );
			}
		};
		strengthButtonPlus.setRect(WIDTH - (float) WIDTH / 8, strengthLabel.top() + (strengthLabel.height() - BTN_HEIGHT) / 2, (float) WIDTH / 8, BTN_HEIGHT);
		add(strengthButtonPlus);

		RedButton strengthButtonMinus = new RedButton("-1"){
			@Override
			protected void onClick() {
				if (Dungeon.hero.STR > 0) {
					Dungeon.hero.STR--;
					Dungeon.hero.sprite.showStatusWithIcon(CharSprite.NEGATIVE, "1", FloatingText.STRENGTH);

					GLog.n(Messages.get(PotionOfStrength.class, "msg", Dungeon.hero.STR()));
				}
			}
		};
		strengthButtonMinus.setRect(strengthButtonPlus.left() - strengthButtonPlus.width() - GAP, strengthButtonPlus.top(), strengthButtonPlus.width(), strengthButtonPlus.height());
		add(strengthButtonMinus);

		///
		/// HEALTH
		///
		RenderedTextBlock healthLabel = PixelScene.renderTextBlock( Messages.get(this, "health"), STAT_HEIGHT );
		healthLabel.setPos(0, pos);
		PixelScene.align(healthLabel);
		add( healthLabel );
		pos = healthLabel.bottom() + BTN_HEIGHT;

		RedButton healthButtonPlusTen = new RedButton("+10"){
			@Override
			protected void onClick() {
				Dungeon.hero.HP = Math.min(Dungeon.hero.HP + 10, Dungeon.hero.GetMaxHP());
			}
		};
		healthButtonPlusTen.setRect(WIDTH - (float) WIDTH / 8, healthLabel.top() + (healthLabel.height() - BTN_HEIGHT) / 2, (float) WIDTH / 8, BTN_HEIGHT);
		add(healthButtonPlusTen);

		RedButton healthButtonPlus = new RedButton("+1"){
			@Override
			protected void onClick() {
				Dungeon.hero.HP = Math.min(Dungeon.hero.HP + 1, Dungeon.hero.GetMaxHP());
			}
		};
		healthButtonPlus.setRect(healthButtonPlusTen.left() - healthButtonPlusTen.width() - GAP, healthButtonPlusTen.top(), healthButtonPlusTen.width(), healthButtonPlusTen.height());
		add(healthButtonPlus);

		RedButton healthButtonMinus = new RedButton("-1"){
			@Override
			protected void onClick() {
				Dungeon.hero.HP = Math.max(Dungeon.hero.HP - 1, 1);
			}
		};
		healthButtonMinus.setRect(healthButtonPlus.left() - healthButtonPlus.width() - GAP, healthButtonPlus.top(), healthButtonPlus.width(), healthButtonPlus.height());
		add(healthButtonMinus);

		RedButton healthButtonMinusTen = new RedButton("-10"){
			@Override
			protected void onClick() {
				Dungeon.hero.HP = Math.max(Dungeon.hero.HP - 10, 1);
			}
		};
		healthButtonMinusTen.setRect(healthButtonMinus.left() - healthButtonMinus.width() - GAP, healthButtonMinus.top(), healthButtonMinus.width(), healthButtonMinus.height());
		add(healthButtonMinusTen);

		///
		/// SHIELD
		///
		RenderedTextBlock shieldLabel = PixelScene.renderTextBlock( Messages.get(this, "shield"), STAT_HEIGHT );
		shieldLabel.setPos(0, pos);
		PixelScene.align(shieldLabel);
		add( shieldLabel );
		pos = shieldLabel.bottom() + BTN_HEIGHT;

		RedButton shieldButtonPlusTen = new RedButton("+10"){
			@Override
			protected void onClick() {
				Buff.affect(Dungeon.hero, Barrier.class).incShield(10);
			}
		};
		shieldButtonPlusTen.setRect(WIDTH - (float) WIDTH / 8, shieldLabel.top() + (shieldLabel.height() - BTN_HEIGHT) / 2, (float) WIDTH / 8, BTN_HEIGHT);
		add(shieldButtonPlusTen);

		RedButton shieldButtonPlus = new RedButton("+1"){
			@Override
			protected void onClick() {
				Buff.affect(Dungeon.hero, Barrier.class).incShield(1);
			}
		};
		shieldButtonPlus.setRect(shieldButtonPlusTen.left() - shieldButtonPlusTen.width() - GAP, shieldButtonPlusTen.top(), shieldButtonPlusTen.width(), shieldButtonPlusTen.height());
		add(shieldButtonPlus);

		RedButton shieldButtonMinus = new RedButton("-1"){
			@Override
			protected void onClick() {
				Buff.affect(Dungeon.hero, Barrier.class).decShield(1);
			}
		};
		shieldButtonMinus.setRect(shieldButtonPlus.left() - shieldButtonPlus.width() - GAP, shieldButtonPlus.top(), shieldButtonPlus.width(), shieldButtonPlus.height());
		add(shieldButtonMinus);

		RedButton shieldButtonMinusTen = new RedButton("-10"){
			@Override
			protected void onClick() {
				Buff.affect(Dungeon.hero, Barrier.class).decShield(10);
			}
		};
		shieldButtonMinusTen.setRect(shieldButtonMinus.left() - shieldButtonMinus.width() - GAP, shieldButtonMinus.top(), shieldButtonMinus.width(), shieldButtonMinus.height());
		add(shieldButtonMinusTen);

		///
		/// EXPERIENCE
		///
		RenderedTextBlock experienceLabel = PixelScene.renderTextBlock( Messages.get(this, "experience"), STAT_HEIGHT );
		experienceLabel.setPos(0, pos);
		PixelScene.align(experienceLabel);
		add( experienceLabel );
		pos = experienceLabel.bottom() + BTN_HEIGHT;

		RedButton experienceButtonPlusTen = new RedButton("+25"){
			@Override
			protected void onClick() {
				Dungeon.hero.earnExp(25, WndHeroCreative.class);
			}
		};
		experienceButtonPlusTen.setRect(WIDTH - (float) WIDTH / 8, experienceLabel.top() + (experienceLabel.height() - BTN_HEIGHT) / 2, (float) WIDTH / 8, BTN_HEIGHT);
		add(experienceButtonPlusTen);

		RedButton experienceButtonPlus = new RedButton("+1"){
			@Override
			protected void onClick() {
				Dungeon.hero.earnExp(1, WndHeroCreative.class);
			}
		};
		experienceButtonPlus.setRect(experienceButtonPlusTen.left() - experienceButtonPlusTen.width() - GAP, experienceButtonPlusTen.top(), experienceButtonPlusTen.width(), experienceButtonPlusTen.height());
		add(experienceButtonPlus);

		RedButton experienceButtonMinus = new RedButton("-1"){
			@Override
			protected void onClick() {
				Dungeon.hero.removeExp(1);
			}
		};
		experienceButtonMinus.setRect(experienceButtonPlus.left() - experienceButtonPlus.width() - GAP, experienceButtonPlus.top(), experienceButtonPlus.width(), experienceButtonPlus.height());
		add(experienceButtonMinus);

		RedButton experienceButtonMinusTen = new RedButton("-25"){
			@Override
			protected void onClick() {
				Dungeon.hero.removeExp(25);
			}
		};
		experienceButtonMinusTen.setRect(experienceButtonMinus.left() - experienceButtonMinus.width() - GAP, experienceButtonMinus.top(), experienceButtonMinus.width(), experienceButtonMinus.height());
		add(experienceButtonMinusTen);

		///
		/// RESPEC
		///
		RedButton respecButton = new RedButton(Messages.get(this, "respec")){
			@Override
			protected void onClick() {
				Dungeon.hero.respecTalents();
			}
		};
		respecButton.setRect(0, pos, WIDTH, BTN_HEIGHT);
		add(respecButton);
		pos = respecButton.bottom() + BTN_HEIGHT;

		resize(WIDTH, (int) pos);
	}

	@Override
	public void hide() {
		super.hide();
		GameScene.show( new WndHero() );
	}
}
