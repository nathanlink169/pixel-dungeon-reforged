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

package com.shatteredpixel.shatteredpixeldungeon.scenes;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.Rankings;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.journal.Journal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ActionIndicator;
import com.shatteredpixel.shatteredpixeldungeon.ui.Archs;
import com.shatteredpixel.shatteredpixeldungeon.ui.ExitButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.IconButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.StyledButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.DungeonSeed;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndChallenges;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndDifficulty;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndHeroSelection;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTextInput;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTwoOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndVictoryCongrats;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Camera;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;
import com.watabou.noosa.NinePatch;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.SkinnedBlock;
import com.watabou.noosa.tweeners.Tweener;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.GameMath;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class HeroSelectScene extends PixelScene {
	private StyledButton m_StartButton;
	private StyledButton m_DailyButton;
	private StyledButton m_ClassSelectionButton;
	private StyledButton m_CharacterCustomizationButton;
	private StyledButton m_CustomSeedButton;
	private StyledButton m_DifficultyButton;
	private StyledButton m_ModifiersButton;
	private StyledButton m_ResetButton;
	private IconButton m_ExitButton;

	private RectF insets;

	@Override
	public void create() {
		super.create();

		Dungeon.hero = null;

		Badges.loadGlobal();
		Challenges.load();
		Journal.loadGlobal();
		
		insets = Game.platform.getSafeInsets(PlatformSupport.INSET_ALL).scale(1f/defaultZoom);

		if (GamesInProgress.selectedClass == null) {
			GamesInProgress.selectedClass = HeroClass.Get(SPDSettings.lastClass());
		}

		Archs archs = new Archs();
		archs.setSize( Camera.main.width, Camera.main.height );
		add( archs );
		
		

		SetupButtons();

		if (Badges.isUnlocked(Badges.Badge.VICTORY) && !SPDSettings.victoryNagged()) {
			SPDSettings.victoryNagged(true);
			add(new WndVictoryCongrats());
		}

		fadeIn();
	}

	private void SetupButtons() {
		final int BTN_WIDTH = 80;
		final int BTN_HEIGHT = 20;
		final int GAP = 2;

		m_StartButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "start")){
			@Override
			protected void onClick() {
				super.onClick();

				if (GamesInProgress.selectedClass == null) return;

				Dungeon.hero = null;
				Dungeon.daily = Dungeon.dailyReplay = false;
				Dungeon.initSeed();
				ActionIndicator.clearAction();
				InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

				Game.switchScene( InterlevelScene.class );
			}
		};
		m_StartButton.icon(Icons.get(Icons.ENTER));
		m_StartButton.leftJustify = true;
		m_StartButton.setRect(GAP + insets.left, GAP + insets.top, BTN_WIDTH, BTN_HEIGHT);
		add(m_StartButton);

		m_DailyButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "daily")){
			private static final long SECOND = 1000;
			private static final long MINUTE = 60 * SECOND;
			private static final long HOUR = 60 * MINUTE;
			private static final long DAY = 24 * HOUR;

			@Override
			protected void onClick() {
				super.onClick();

				if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
					ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
							Icons.get(Icons.CALENDAR),
							Messages.get(HeroSelectScene.class, "daily"),
							Messages.get(HeroSelectScene.class, "daily_nowin"))
					);
					return;
				}

				long diff = (SPDSettings.lastDaily() + DAY) - Game.realTime;
				if (diff > 24*HOUR){
					ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "daily_unavailable_long", (diff / DAY)+1)));
					return;
				}

				for (GamesInProgress.Info game : GamesInProgress.checkAll()){
					if (game.daily){
						ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "daily_existing")));
						return;
					}
				}

				Image icon = Icons.get(Icons.CALENDAR);
				if (diff <= 0)  icon.hardlight(0.5f, 1f, 2f);
				else            icon.hardlight(1f, 0.5f, 2f);
				ShatteredPixelDungeon.scene().addToFront(new WndOptions(
						icon,
						Messages.get(HeroSelectScene.class, "daily"),
						diff > 0 ?
								Messages.get(HeroSelectScene.class, "daily_repeat") :
								Messages.get(HeroSelectScene.class, "daily_desc"),
						Messages.get(HeroSelectScene.class, "daily_yes"),
						Messages.get(HeroSelectScene.class, "daily_no")){
					@Override
					protected void onSelect(int index) {
						if (index == 0){
							if (diff <= 0) {
								long time = Game.realTime - (Game.realTime % DAY);

								//earliest possible daily for v3.0.1 is Mar 01 2025
								//which is 20,148 days days after Jan 1 1970
								time = Math.max(time, 20_148 * DAY);

								SPDSettings.lastDaily(time);
								Dungeon.dailyReplay = false;
							} else {
								Dungeon.dailyReplay = true;
							}

							Dungeon.hero = null;
							Dungeon.daily = true;
							Dungeon.initSeed();
							ActionIndicator.clearAction();
							InterlevelScene.mode = InterlevelScene.Mode.DESCEND;

							Game.switchScene( InterlevelScene.class );
						}
					}
				});
			}

			private long timeToUpdate = 0;

			private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.ROOT);
			{
				dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
			}

			@Override
			public void update() {
				super.update();

				if (Game.realTime > timeToUpdate && visible){
					long diff = (SPDSettings.lastDaily() + DAY) - Game.realTime;

					if (diff > 0){
						if (diff > 30*HOUR){
							text("30:00:00+");
						} else {
							text(dateFormat.format(new Date(diff)));
						}
						timeToUpdate = Game.realTime + SECOND;
					} else {
						text(Messages.get(HeroSelectScene.class, "daily"));
						timeToUpdate = Long.MAX_VALUE;
					}
				}

			}
		};
		m_DailyButton.icon(Icons.get(Icons.CALENDAR));
		m_DailyButton.leftJustify = true;
		m_DailyButton.setRect(m_StartButton.left(), m_StartButton.bottom() + GAP, BTN_WIDTH, BTN_HEIGHT);
		add(m_DailyButton);

		m_ClassSelectionButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "class_selection")){
			@Override
			protected void onClick() {
				super.onClick();

				super.onClick();
				HeroClass cls = GamesInProgress.selectedClass;
				if (cls != null) {
					Window w = new WndHeroSelection(HeroSelectScene.this);
					if (landscape()) {
						w.offset(Camera.main.width / 6, 0);
					}
					ShatteredPixelDungeon.scene().addToFront(w);
				}

			}
		};
		m_ClassSelectionButton.icon(GetHeroIconForClass(GamesInProgress.selectedClass));
		m_ClassSelectionButton.leftJustify = true;
		if (SPDSettings.landscape()) {
			m_ClassSelectionButton.setRect(m_StartButton.right() + GAP, m_StartButton.top(), BTN_WIDTH, BTN_HEIGHT);
		} else {
			m_ClassSelectionButton.setRect(m_DailyButton.left(), m_DailyButton.bottom() + GAP * 3, BTN_WIDTH, BTN_HEIGHT);
		}
		add(m_ClassSelectionButton);

		m_CharacterCustomizationButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "character_customization")){
			@Override
			protected void onClick() {
				super.onClick();

				ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
						Icons.get(Icons.CHANGES),
						"Not Available",
						"Character customization coming soon! Please check back later.")
				);
			}
		};
		m_CharacterCustomizationButton.icon(Icons.get(Icons.CHANGES));
		m_CharacterCustomizationButton.leftJustify = true;
		m_CharacterCustomizationButton.setRect(m_ClassSelectionButton.left(), m_ClassSelectionButton.bottom() + GAP, BTN_WIDTH, BTN_HEIGHT);
		add(m_CharacterCustomizationButton);

		m_CustomSeedButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "custom_seed")){
			@Override
			protected void onClick() {
				if (!Badges.isUnlocked(Badges.Badge.VICTORY) && !DeviceCompat.isDebug()){
					ShatteredPixelDungeon.scene().addToFront( new WndTitledMessage(
							Icons.get(Icons.SEED),
							Messages.get(HeroSelectScene.class, "custom_seed"),
							Messages.get(HeroSelectScene.class, "custom_seed_nowin"))
					);
					return;
				}

				String existingSeedtext = SPDSettings.customSeed();
				ShatteredPixelDungeon.scene().addToFront( new WndTextInput(Messages.get(HeroSelectScene.class, "custom_seed_title"),
						Messages.get(HeroSelectScene.class, "custom_seed_desc"),
						existingSeedtext,
						20,
						false,
						Messages.get(HeroSelectScene.class, "custom_seed_set"),
						Messages.get(HeroSelectScene.class, "custom_seed_clear")){
					@Override
					public void onSelect(boolean positive, String text) {
						text = DungeonSeed.formatText(text);
						long seed = DungeonSeed.convertFromText(text);

						if (positive && seed != -1){

							for (GamesInProgress.Info info : GamesInProgress.checkAll()){
								if (info.customSeed.isEmpty() && info.seed == seed){
									SPDSettings.customSeed("");
									icon.resetColor();
									ShatteredPixelDungeon.scene().addToFront(new WndMessage(Messages.get(HeroSelectScene.class, "custom_seed_duplicate")));
									return;
								}
							}

							SPDSettings.customSeed(text);
							icon.hardlight(1f, 1.5f, 0.67f);
						} else {
							SPDSettings.customSeed("");
							icon.resetColor();
						}
					}
				});
			}
		};
		m_CustomSeedButton.icon(Icons.get(Icons.SEED));
		m_CustomSeedButton.leftJustify = true;
		m_CustomSeedButton.setRect(m_CharacterCustomizationButton.left(), m_CharacterCustomizationButton.bottom() + GAP, BTN_WIDTH, BTN_HEIGHT);
		add(m_CustomSeedButton);

		m_DifficultyButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "difficulty")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndDifficulty(SPDSettings.difficulty(), true, HeroSelectScene.this));
			}
		};
		m_DifficultyButton.icon(GetDifficultyIcon());
		m_DifficultyButton.leftJustify = true;
		m_DifficultyButton.setRect(m_CustomSeedButton.left(), m_CustomSeedButton.bottom() + GAP, BTN_WIDTH, BTN_HEIGHT);
		add(m_DifficultyButton);

		m_ModifiersButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "modifiers")){
			@Override
			protected void onClick() {
				ShatteredPixelDungeon.scene().addToFront(new WndChallenges(SPDSettings.challenges(), true) {
					public void onBackPressed() {
						super.onBackPressed();
						icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
					}
				} );
			}
		};
		m_ModifiersButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
		m_ModifiersButton.leftJustify = true;
		m_ModifiersButton.setRect(m_DifficultyButton.left(), m_DifficultyButton.bottom() + GAP, BTN_WIDTH, BTN_HEIGHT);
		add(m_ModifiersButton);

		m_ResetButton = new StyledButton(Chrome.Type.GREY_BUTTON_TR, Messages.get(this, "reset")){
			@Override
			protected void onClick() {
				WndTwoOptions resetWindow = new WndTwoOptions(Icons.get(Icons.CHANGES), "Reset", "Are you sure?", "Yes", "No") {
					@Override
					// 0 is first option, 1 is second
					public void onSelect(int option) {
						super.onSelect(option);

						if (option == 0) {
							SPDSettings.challenges(0);
							GamesInProgress.selectedClass = HeroClass.Get(0);
							SPDSettings.customSeed("");
							SPDSettings.difficulty(2);
							HeroSelectScene.this.UpdateAfterWindowClose();
						}
					}
				};
				ShatteredPixelDungeon.scene().addToFront(resetWindow);
			}
		};
		m_ResetButton.icon(Icons.ALERT.get());
		m_ResetButton.leftJustify = false;
		if (SPDSettings.landscape()) {
			m_ResetButton.setRect(m_DailyButton.left(), m_ModifiersButton.top(), BTN_WIDTH, BTN_HEIGHT);
		} else {
			m_ResetButton.setRect(m_ModifiersButton.left(), m_ModifiersButton.bottom() + GAP * 2, BTN_WIDTH, BTN_HEIGHT);
		}
		add(m_ResetButton);

		m_ExitButton = new ExitButton();
		m_ExitButton.setPos( Camera.main.width - m_ExitButton.width() - insets.right, insets.top );
		add(m_ExitButton);
		m_ExitButton.visible = m_ExitButton.active = !SPDSettings.intro();
	}

	public void UpdateAfterWindowClose() {
		m_ClassSelectionButton.icon(GetHeroIconForClass(GamesInProgress.selectedClass));
		m_DifficultyButton.icon(GetDifficultyIcon());
		if (!Objects.equals(SPDSettings.customSeed(), "")) {
			m_CustomSeedButton.icon().hardlight(1f, 1.5f, 0.67f);
		} else {
			m_CustomSeedButton.icon().resetColor();
		}
		m_ModifiersButton.icon(Icons.get(SPDSettings.challenges() > 0 ? Icons.CHALLENGE_COLOR : Icons.CHALLENGE_GREY));
	}

	private Image GetHeroIconForClass(HeroClass c) {
		switch (c) {
			case WARRIOR:
				return new ItemSprite(ItemSpriteSheet.SEAL, null);
			case MAGE:
				return new ItemSprite(ItemSpriteSheet.MAGES_STAFF, null);
			case ROGUE:
				return new ItemSprite(ItemSpriteSheet.ARTIFACT_CLOAK, null);
			case HUNTRESS:
				return new ItemSprite(ItemSpriteSheet.SPIRIT_BOW, null);
			case DUELIST:
				return new ItemSprite(ItemSpriteSheet.RAPIER, null);
			case CLERIC:
				return new ItemSprite(ItemSpriteSheet.ARTIFACT_TOME, null);
			case ARTIFICER:
				return new ItemSprite(ItemSpriteSheet.GUN, null);
		}

		throw new RuntimeException("HeroSelectScene.GetHeroIconForClass - Could not find icon for HeroClass " + c);
	}

	private Image GetDifficultyIcon() {
		if (SPDSettings.creative()) {
			switch (SPDSettings.difficulty()) {
				case 1:
					return Icons.get(Icons.EASYCREATIVE);
				case 2:
					return Icons.get(Icons.MEDIUMCREATIVE);
				case 3:
					return Icons.get(Icons.HARDCREATIVE);
				case 4:
					return Icons.get(Icons.IMPOSSIBLECREATIVE);
			}
		} else {
			switch (SPDSettings.difficulty()) {
				case 1:
					return Icons.get(Icons.EASY);
				case 2:
					return Icons.get(Icons.MEDIUM);
				case 3:
					return Icons.get(Icons.HARD);
				case 4:
					return Icons.get(Icons.IMPOSSIBLE);
			}
		}

		return Icons.get(Icons.DIFFICULTY);
	}

	@Override
	public void update() {
		super.update();
		if (SPDSettings.intro() && Rankings.INSTANCE.totalNumber > 0){
			SPDSettings.intro(false);
		}
		m_ExitButton.visible = m_ExitButton.active = !SPDSettings.intro();
	}

	@Override
	protected void onBackPressed() {
		if (m_ExitButton.active){
			ShatteredPixelDungeon.switchScene(TitleScene.class);
		} else {
			super.onBackPressed();
		}
	}
}
