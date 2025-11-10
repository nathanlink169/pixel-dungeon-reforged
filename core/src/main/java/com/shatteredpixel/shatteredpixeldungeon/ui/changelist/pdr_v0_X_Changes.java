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

package com.shatteredpixel.shatteredpixeldungeon.ui.changelist;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Constants;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BallistaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DemonGooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FiendSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GnollSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HalfRipperSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.sprites.KoboldSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.RatSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SkeletonSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.SpitterSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.UnholyPriestSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.TerrainFeaturesTilemap;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.watabou.noosa.Image;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class pdr_v0_X_Changes {

	public static void addAllChanges( ArrayList<ChangeInfo> changeInfos ){
		add_Coming_Soon(changeInfos);
		add_v0_3_0_Changes(changeInfos);

		ChangeInfo changes2 = new ChangeInfo("v0.2", true, "");
		changes2.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes2);
		add_v0_2_1_Changes(changeInfos);
		add_v0_2_0_Changes(changeInfos);

		ChangeInfo changes1 = new ChangeInfo("v0.1", true, "");
		changes1.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes1);
		add_v0_1_4_Changes(changeInfos);
		add_v0_1_3_Changes(changeInfos);
		add_v0_1_2_Changes(changeInfos);
		add_v0_1_1_Changes(changeInfos);
		add_v0_1_Changes(changeInfos);
	}

	public static void add_Coming_Soon( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("Coming Soon", true, "");
		changes.hardlight(0xCCCCCC);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Overview",
				"This area is meant to go over all of my plans that I will add. That doesn't mean all of these will come in the next update, but they should come sometime!\n" +
				"\n" +
				"I plan to keep this branched away from Shattered Pixel Dungeon. I'll bring things in if I feel they'll really improve the experience, in the same way I've brought over some features from Darkest Pixel Dungeon, Chancel Pixel Dungeon, etc.\n\nFor those mods, the goal is to celebrate those features, not outright steal them. A lot of the games I've pulled features from are no longer receiving updates, but if any of the creators have a problem with me using their content, let me know and I will remedy that."));

		changes.addButton( new ChangeButton(new Image(new SkeletonSprite()), "New Prison Boss (and a new boss for each region)",
				"One of the things I want to do is different sized dynamic bosses. I know we technically have the crystal in the troll quest, but that is very static.\n" +
				"\n" +
				"The current plan is to bring in a very large skeleton (name pending.) The only way to destroy it is to crush its skull, but you'll have to break down its legs to get there. This large, dynamic, multi-stage boss is something that I think will add more life to the existing bosses."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.MAGE, 1), "Hero Visual Customization",
				"I'd like to add some visual customization for heroes. I won't be able to change the hero splash art because I am not good at art, but I would like to be able to change the sprite: skin colour, hair colour, hairstyle, gender presentation, etc."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.DUELIST, 1), "Level Up Changes",
				"While I do enjoy the usual level up of Shattered Pixel Dungeon, I'd like to add another game mode: Multiclassing.\n\nIn my mind, it's inspired by Dungeons and Dragons where different classes have different health, defense bonus, attack bonus, and features, etc. however, each section would cost a different amount of XP to level, meaning it's easy to level something you haven't put much effort into yet.\n\nThis would not replace the typical hero progression and would be a separate gamemode."));

		changes.addButton( new ChangeButton(Icons.get(Icons.JOURNAL), "Journal Upgrade",
				"The current journal is alright, but I'd like to upgrade it a little bit. Every time you kill an enemy, they will have a chance to drop a journal page which has much more detailed information, including their damage range, damage reduction, accuracy and evasion, etc."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SPIRIT_BOW), "Thrown Weapons Rework",
				"Version 3.2 of Shattered Pixel Dungeon made a change to thrown weapons. I have my own way I'd like to take it, with every character having a melee weapon and a ranged weapon, and being able to find each as they go through the dungeon. In addition, most thrown weapons will be transferred into ammunition for these ranged weapons, with a quiver slot available."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARMOR_LEATHER), "Armour Rework",
				"The current armour system in the game is very basic, with only one armour for each tier. I'd like to introduce some variety. For example, armour that protects for more but makes you slow, armour that increases evasion but gives 0 protection, etc."));
	}

	public static void add_v0_3_0_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.3.0", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Developer Commentary",
				"_-_ Released November 8th, 2025\n" +
						"\n" +
						"This update is mainly a rework of the combat system behind the scenes. As someone coming into this codebase fresh, the combat logic has a heavy learning curve because its distributed across multiple files and areas of that file. For someone who built it and knows where everything lives, it's intuitive. For me jumping in, it was difficult to comprehend.\n" +
						"\n" +
						"That is not saying the system in place in Shattered is bad! The systems work well and the game is excellent. This rework was just more geared towards my style of coding and will allow me to speed up my own personal development.\n" +
						"\n" +
						"This rework also comes with a heavier emphasis on damage types, and while there aren't many creatures that take advantage of damage types yet, more will come in the coming updates!",
						"What's next?\n\n" +
								"For the 0.4.0 update, there are two things I'd like to update: I'd like to implement the character appearance editor, as well as a mini rework for some of the Artificer's more unbalanced features. A small gun nerf, a big quickdraw nerf, an armourer rework, etc."));

		changes.addButton( new ChangeButton(new Image(new GnollSprite()), "Combat Rework",
				"This is a complete rewrite of the combat system. You'll see a fair few more damage icons in regular battle! This is because each enemy and weapon now have a specified damage type, and each damage type has its own icon. Some of the old icons are being retired as they had overlap with other damage types, and there are a few new ones.\n" +
						"**-** Bludgeoning: if you want to pummel, beat, and smash your enemies, bludgeoning damage is for you.\n" +
						"**-** Piercing: Spikes, daggers, or anything that penetrates.\n" +
						"**-** Slashing: Slice up enemies with swords or claws.\n" +
						"**-** Acid: Flesh-melting, corrosion, and ooze.\n" +
						"**-** Cold: Freeze your oponents to their core.\n" +
						"**-** Electricity: How shocking.\n" +
						"**-** Explosive: For those looking to make the loudest impact.\n" +
						"**-** Fire: We all know what fire is.\n" +
						"**-** Poison: Toxic traps and coated thorns.\n" +
						"**-** Sonic: I take it back, THIS is the loudest impact, quite literally.\n" +
						"**-** Water: Usually not very damaging, but if you're made of fire, watch out.\n" +
						"**-** Positive Energy: The radiant power of anything holy.\n" +
						"**-** Negative Energy: The flipside of Positive Energy, necrotic and withering.\n" +
						"**-** Force: Pure magic.",
						"^A special thank you to players for helping to test this version early. In particular, thank you to discord users baddreams0862, born_killer, elgransersuperior999, and miaomix2688.^"));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RATION), "Hunger Buff",
				"Hunger was a little too forgiving, so I'm partially reverting the changing from version 1.1.\n\n" +
						"**-** Time to go hungry: 450 -> 350\n" +
						"**-** Time to start starving: 200 -> 175\n" +
						"**-** Removing the extra ration on floors 4, 9, 14, 19, and 24."));

		changes.addButton( new ChangeButton(Icons.DISPLAY_PORT.get(), "Version Update",
				"In order to use some of the newer language features for this Combat Rework, I've had to update Pixel Dungeon Reforged to a newer SDK version. This means that some of the lowest devices will no longer be supported. I do not plan to upgrade this ever again, barring anything outside of my control.\n" +
						"**-** Pixel Dungeon Reforged now requires Android 8.0+, up from 5.0+."));

		changes.addButton( new ChangeButton(Icons.CONTROLLER.get(), "Other Behind the Scenes Reworks",
				"I didn't talk much about these parts because ideally, the player will literally never see them. The way the game saved behind the scenes has had a small rework, mainly to make it safer and less likely to accidentally break it on my end. In addition, the way that AI works has had a mini rework to reduce the amount of memory allocations."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +

						"**-** Railgun will no longer pierce through the entire dungeon\n" +
						"**-** Before that bug, the railgun would stop at the tile aimed at. This will no longer occur, and it will continue until it hits something\n" +
						"**-** Railgun will no longer damage allies\n" +
						"**-** Firing railgun will remove timestop and invisibility\n" +
						"**-** Ballista will no longer knock back if you dodge its attack\n" +
						"**-** Modifier cups will now unlock a modifier rather than claiming they are all unlocked\n" +
						"**-** Bestiary will no longer crash the game\n" +
						"**-** Kobold traps will no longer appear outside of the kobold quest\n" +
						"**-** Unstable spellbook will no longer trigger the volatile salvage text\n" +
						"**-** Evil Eyes now drop loot\n" +
						"**-** Falling into the sewers boss level should properly drop you in the starting room\n" +
						"**-** Quickdraw now requires 65% energy (up from 50%). This is a temporary solution until Quickdraw can be reworked.\n" +
						"**-** Ringbox should now apply ring bonuses correctly\n" +
						"**-** Gotcha! quest now has proper description\n" +
						"**-** Badges will now correctly hide the older badge as necessary\n" +
						"**-** Badges will no longer constantly appear when swapping between creative mode runs\n" +
						"**-** Creative mode now properly resets between runs\n" +
						"**-** Modifier window will no longer enable the incorrect modifier behind the scenes\n" +
						"**-** Localization fixes"));
	}

	public static void add_v0_2_1_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.2.1", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.KEYBOARD.get(), "Creative Mode",
				"When selecting difficulty, you can now select creative mode. This allows you to change things on the fly in game.\n" +
						"**-** Give yourself any item.\n" +
						"**-** Infinite gold and energy.\n" +
						"**-** Edit your experience, health, shielding, and strength.\n" +
						"**-** Remove any status from yourself or other creatures.\n" +
						"**-** Spawn any non-boss enemy on the fly.\n\n" +
						"There are four areas to find the creative mode icon. In the hero window to edit stats, in the backpack to give yourself items, in the status info window to remove a status effect, and in the pause menu to spawn an enemy.\n\n" +
						"Your score will automatically set as 0 at the end of a creative mode run. You cannot unlock modifiers using creative mode."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +

						"**-** Ballista would not consume ammo upon shooting\n" +
						"**-** Enemies with physical projectiles would not deal any damage\n" +
						"**-** Demon Halls quest would not reset properly between runs\n" +
						"**-** Demon goo would drop items too often\n" +
						"**-** Elixir of Might could be duplicated by Volatile Salvage\n" +
						"**-** Modifier rooms would not spawn sometimes\n" +
						"**-** Some modifier rooms were sometimes impossible to complete due to level generation\n" +
						"**-** Construct would always have the cripple ability regardless of the Construct Lethality talent level"));
	}

	public static void add_v0_2_0_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.2.0", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Developer Commentary",
				"_-_ Released September 20th, 2025\n" +
						"\n" +
						"Here's the next update, with some technical modernizations, a constructor rework, new run flow redesign, a new challenge unlock system, and a new challenge!\n" +
						"\n" +
						"I wanted to put off the library update as long as possible, but unfortunately the game wouldn't build without the update, so I couldn't put it off any longer. Please let me know if there are any weird changes as a consequence of these updates. Changing libraries is easily my least-favourite task of game development because the errors tend to be little more than \"I just do not _vibe_ with this update\" so I may have missed some things."));

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "What's Next?",
				"After this update (and the usual round of bugfixes,) I plan to implement the Hero Visual Customization, and maybe also the Creative Mode."));

		changes.addButton( new ChangeButton(Icons.DISPLAY_PORT.get(), "Mobile Layout Changes",
				"**-** Much like Shattered Pixel Dungeon, Pixel Dungeon Reforged now renders in true edge-to-edge fullscreen on Android 9+!\n" +
						  "**-** All in-game interfaces should have been adjusted to better handle true mobile fullscreen and insets. Please let me know if you run into any issues on your device.\n" +
						  "**-** Pixel Dungeon reforged now requires Android 5.0+, up from 4.0+.\n" +
						  "**-** Updated various Google Play libraries."));

		changes.addButton( new ChangeButton(new Image(Assets.Sprites.CONSTRUCT, 0, 0, 16, 16), "Constructor Rework",
				"While the constructor wasn't bad, lots of their talents were just \"look at the number go up\". These upgrades should be a little more interesting.\n\n" +
						"**-** Construct Hardening has been changed to Construct Vision.\n" +
						"  **-** Previously, Construct Hardening would upgrade the Constructs Max Health from 15% of your Max Health to 25%, 35%, then 50% per point. Now, the _Constructs Max Health is always 50%._\n" +
						"  **-** Previously, Construct Hardening would upgrade the Constructs Defense Skill from 15% of your Defense Skill to 25%, 35%, then 50% per point. Now, the _Constructs Defense Skill is always 50%._\n" +
						"  **-** Previously, Construct Hardening would upgrade the Constructs Damage Reduction from 15% of your Damage Reduction to 25%, 35%, then 50% per point. Now, the _Constructs Damage Reduction is always 80%._\n" +
						"  **-** Construct Vision Range has decreased from _8 -> 6._\n" +
						"  **-** The first point of Construct Vision allows you to _always see the Constructs vision range_ (in the same way you can see a wards vision or the spirit hawks vision.) The second and third points increase the vision range by _2 each._",
						"**-** Construct Mobility has been left unchanged, as I think speed and flying is interesting enough on its own.\n" +
						"**-** Construct Lethality has been adjusted.\n" +
						"  **-** The Constructs passive healing has been decreased from 1 hp every 1 turn to _1 hp every 5 turns._\n" +
						"  **-** The first point of Construct Lethality had its damage increase as if it was a Tier 3 weapon (instead of a Tier 2 weapon.) It still does this, but now it also gives a chance to _cripple_ the target.\n" +
						"  **-** The second point of Construct Lethality had its damage increase as if it was a Tier 4 weapon. It still does this, but now it also increases the Constructs passive healing from 1 hp every 5 turns to _1 hp every 2 turns._\n" +
						"  **-** The third point of Construct Lethality had its damage increase as if it was a Tier 5 weapon. It still does this, but now it also _reflects 20% of all melee damage_, and _explodes upon death, dealing damage to all nearby enemies._",
						"**-** Also sneaking in a small Armorer buff too, shh. Point 2 of infinite falling now _reduces 50% of fall damage_ (instead of 33%), and Point 3 _reduces it by 90%_ (instead of 75%.)"));

		changes.addButton( new ChangeButton(new Image(Icons.get(Icons.CHALLENGE_COLOR)), "Modifiers!",
				"Challenges have been renamed to Modifiers! This lets me make Modifiers that are more just... fun, rather than simply harder. I already experimented a little with this with Trinket Madness, but now we're going all in!\n" +
						"**-** Modifiers are no longer unlocked by finishing a run.\n" +
						"**-** Every modifier will now have to be unlocked individually.\n" +
						"  **-** You have a chance of finding a special room every run that requires you to complete a challenge. At the end of this challenge is a _Modifier Cup!_ Each Modifier Cup will unlock a random Modifier that you can apply to your next run.\n" +
						"  **-** Just finding the room isn't enough. The rooms themselves are meant to drain some of your resources as well, so make sure you can actually get them!\n" +
						"  **-** There are four different rooms that could unlock a Modifier. More will likely be on the way.\n" +
						"  **-** _The Randomizer_ has been moved to be a Modifier, rather than its own separate feature.",
						"**-** _New Modifier: Adaptive!_ Every time an enemy dies, all of your equipment is replaced with something of the same type. Your weapon, your armour, your throwables, your potions, your scrolls, etc. Anything unique (spirit bow, homemade railgun, waterskin, potions of strength, etc.) will not be replaced."));

		changes.addButton( new ChangeButton(new Image(Icons.get(Icons.STAIRS)), "New Run Flow",
				"Given my previously mentioned artistic deficit, as well as some of the new features I'm cramming in, the old UI for a new run doesn't make too much sense anymore. To be clear, the Shattered Pixel Dungeon new run UI is definitely better for Shattered Pixel Dungeon, given that Shattered has proper artists, and a specific amount of features that the UI is designed around.\n\n" +
						"This new UI gives me room to let each feature breathe, and also be obvious to players used to Shattered who may not dig through menus to find new stuff. Please let me know what you think, and if you have any suggestions!"));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +

						"**Mobs:**\n" +
						"**-** Construct not being orderable when reloading a save\n" +
						"**-** Statues could not be sneak attacked\n\n" +
						"**Items:**\n" +
						"**-** Barrels of water don't create a water tile when destroyed with the railgun"));
	}

	public static void add_v0_1_4_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.1.4", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.CHANGES.get(), "Update Checker Improvement",
				"Update Checker now checks for hotfixes."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +

						"**Mobs:**\n" +
						"**-** Scorpios crashing the game on desktop\n" +
						"**-** Ballista sprites not disappearing after death\n" +
						"**-** Piranhas not dropping meat\n" +
						"**-** Mimics immune to sneak attacks\n" +
						"**-** Rare issue where game crashes when loading a save on a dark level\n" +
						"**-** Rotting fist dying but not disappearing"));
	}

	public static void add_v0_1_3_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.1.3", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Developer Commentary",
				"_-_ Released August 28th, 2025\n" +
						"\n" +
						"Thank you to everyone for reporting issues and sticking through the buggy releases!\n" +
						"\n" +
						"The next update, barring any silly mistakes, should be v0.2.0. Here are the plans for that specific version:\n" +
						"**-**Constructor rework: While I don't dislike the current constructor, the current talents of \"number go up\" isn't terribly interesting. I want to rework these talents to make the construct itself more interesting to upgrade.\n" +
						"**-**New run flow redesign: The current UI doesn't fully work for where I want to take the game, as it will start getting very crowded soon. Between character appearance customization, difficulty selection, challenges (soon renamed to \"modifiers\"), seed, etc. the current layout struggles a bit.\n" +
						"**-**Likely another challenge (modifier) or two, some ones that may make the game a bit easier, but are a bit of fun to play around with. One that I have in mind is the ability for the pickaxe to work on every level."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.GUN), "Railgun",
				"Another Railgun buff!\n\n" +
						"**-** Railgun reload time: 4 turns -> 6 turns (the only nerf, I promise)\n" +
						"**-** Homemade Railgun no longer destroys items on the ground\n" +
						"**-** Homemade Railgun damage increased:\n" +
						"   **-** Base damage: 4-8 -> 4-8\n" +
						"   **-** +1 damage: 6-13 -> 8-16\n" +
						"   **-** +2 damage: 8-18 -> 12-24\n" +
						"   **-** +3 damage: 10-23 -> 16-32\n" +
						"   **-** +4 damage: 12-28 -> 20-40\n" +
						"   **-** +5 damage: 14-33 -> 24-48\n"));

		changes.addButton( new ChangeButton(Icons.CHANGES.get(), "Auto Updater",
				"Now, the game should automatically tell you when the game has an update, rather than me posting in Discord and Reddit."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RATION), "Food Changes",
				"A few small food changes:\n" +
						"**-** Quick Callibration now works with Horn of Plenty\n" +
						"**-** One more ration spawns per region\n" +
						"**-** On Diet now has food be 40% as effective (previously 33%)"));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n" +
						"**Items:**\n" +
						"**-** Homemade Railgun not affecting tiles that had water on them\n" +
						"**-** Homemade Railgun crashing the game when aiming outside of level bounds\n" +
						"**-** Leeching Curse taking too much health\n" +
						"**-** Trinket Madness spawning with both Mossy Blump and Trap Mechanism",

				"**Mobs:**\n" +
						"**-** Elementals spawning 3,600 HP\n" +
						"**-** NPC unable to be interacted with\n" +
						"**-** Rot Lashers not dropping seeds\n" +
						"**-** Many enemies don't drop items they're supposed to\n" +
						"**-** Tengu spawning with full HP in second phase\n" +
						"**-** Bees appears and using the stats of bats\n" +
						"**-** Rats giving 0xp\n" +
						"**-** Monks not dropping food rations\n" +
						"**-** Newborn Fire Elemental crashing the game on attacks\n" +
						"**-** Thieves not stealing items\n" +
						"**-** Flies not dropping potions\n" +
						"**-** Demon goos unable to be killed\n" +
						"**-** Wraiths unable to be sneak attacked"));
	}
	public static void add_v0_1_2_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.1.2", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.GUN), "Railgun",
				"Friendship ended with \"Gun,\" now \"Homemade Railgun\" is my new best friend.\n\n" +
						"**-** Gun has been renamed to Homemade Railgun\n" +
						"**-** Homemade Railgun can pierce infinite enemies\n" +
						"**-** Every creature hit generates a small explosion around the enemy, dealing half damage to all creatures within the area\n" +
						"**-** Explosions can destroy any destroyable items (like potions or scrolls) so be careful!"));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.ARTIFICER, 1), "Adaptive Minefield",
				"Due to the gun changes, adaptive minefield has been reworked. Instead of disabling traps with the gun, the artificer now has a bonus chance to passively find traps, due to her intimate knowledge of trap creation and deployment."));

		changes.addButton( new ChangeButton(Icons.CHALLENGE_COLOR.get(), "Trinket Madness",
				"Another new challenge, Trinket Madness! Start with three random fully upgraded trinkets. You cannot get rid of the trinkets, transmute them, throw them, or anything like that. You will also not find a magical catalyst in the dungeon.\n\n" +
						"To save on space, during this challenge, trinkets will go into your Velvet Pouch."));

		changes.addButton( new ChangeButton(new Image(new KoboldSprite.Red()), "Kobold Quest Boss Buff",
				"The boss of the kobold quest had a few issues with being easily defeated. Now it will approach anyone attacking at range!"));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.ARTIFICER, 6), "Artificer Armor Abilities Nerfed",
				"**The Artificers armour abilities have been nerfed, as they were a massive power spike.**\n" +
						"\n" +
						"**Quickdraws** base cost has been upped from 35% to 50%, and the amount of shots it shoots at base has been reduced from 5 to 2 (given the gun rework, I think this should still work fine)\n" +
						"\n" +
						"**Truesight** base cost has been upped from 20% to 40%, and bright light's chance has been pulled from 20% change per point to 10%\n" +
						"\n" +
						"**Reflection** has been left unchanged for now."));

		changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Behind the Scenes Change",
				"Enemy stats are no longer hardcoded, instead generated from .csv files. This means the .java files should be smaller and easier to navigate. There will be more overhauls like this. Ultimately, you shouldn't notice anything, but it will make it easier for me to navigate the codebase."));
	}
	public static void add_v0_1_1_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.1.1", false, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.GUN), "Gun Buff",
				"The damage of the gun was a little underwhelming. Its damage suggested it was meant to be used multiple times, when in reality this is meant to be a powerful one-time use per fight item. In future, I want to add a more unique mechanic, either AoE or pierce (hitting multiple enemies in a straight line), but until I work out the numbers, I'm making the numbers more consistent. Taking a little off the top, but adding a _lot_ to the bottom!\n\n" +
						"**-** The gun now indicates if it's loaded in the quickbar.\n" +
						"**-** Base damage: 1-12 -> 4-8\n" +
						"**-** +1 damage: 2-16 -> 6-13\n" +
						"**-** +2 damage: 3-20 -> 8-18\n" +
						"**-** +3 damage: 4-24 -> 10-23\n" +
						"**-** +4 damage: 5-28 -> 12-28\n" +
						"**-** +5 damage: 6-32 -> 14-33"));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_JERA), "Scroll of EHWAZ Replacement",
				"The scroll of EHWAZ looked just like an Arial M. Unfortunately there was no way to make it look closer to the actual rune in the space the sprite provides, so we have replaced it. Goodbye EHWAZ, hello JERA!"));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RATION), "Hunger Buff",
				"Hunger was a little too punishing. This should help prevent a mad scramble for food.\n\n" +
						"**-** Time to go hungry: 300 -> 450\n" +
						"**-** Time to start starving: 150 -> 200"));

		changes.addButton( new ChangeButton(new Image(new RatSprite()), "New Boss Minion Fix",
				"The new sewers boss's minions would be able to proc Ring of Wealth. This has been fixed.\n\nTheir damage has been reduced from 4-12 to 4-10, as their damage was just a touch too high."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.GREATAXE), "Accuracy Reversion",
				"The previous accuracy buff has been reverted. It had way too big of an impact on the balance of the game. I may tweak it again later, but for now I want to let other changes settle."));

		changes.addButton(new ChangeButton(new Image(new FiendSprite()), "Fiend Death Damage Nerf",
				"The death damage of the fiend was a little too high, considering its main feature is the miasma effect. This is going from 24-36 down to 14-20."));

		changes.addButton(new ChangeButton(new Image(new GhostSprite()), "Sad Ghost reward reversion",
				"Two artifacts was a little too good. It's being reverted to a weapon and an armor, however each is guaranteed to be useful in the early-game or mid-game."));

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n\n" +
						"**-**Quick Callibration was not reducing eating time.\n" +
						"**-**Duelist would crash when using certain weapon abilities.\n" +
						"**-**Mimic melee attacks would softlock the game.\n" +
						"**-**It was possible to duplicate stones of intuition.\n" +
						"**-**Fixed dark gold not generating enough during new blacksmith quest.\n" +
						"**-**Ringbox would delete rings when closing the game.\n" +
						"**-**Gun would not reload if done through the gun menu (not the quickbar.)\n" +
						"**-**Miasma would not indicate turns remaining.\n" +
						"**-**Constructor would spawn in weird in places and the icon would not disappear properly.\n" +
						"**-**Volatile salvage would not proc properly.\n" +
						"**-**Quickdraw would take too much time to activate."));
	}

	public static void add_v0_1_Changes(ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("Initial Release", true, "");
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "new"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Developer Commentary",
				"_-_ Released August 4th, 2025\n" +
						"\n" +
						"Thank you for taking a look at Pixel Dungeon Reforged. I want to go a little into why I made this and what some of the goals were.\n" +
						"\n" +
						"In general, I approached this project as enhancement rather than replacement. Shattered Pixel Dungeon is already a fantastic game, and my goal was to preserve the core loop and feel while just adding... more. I am very conscious that this was built on Evan's work and I don't want to take away from that at all: I'm not trying to \"fix\" Shattered Pixel Dungeon, because it's not broken. This is meant to just add options rather than correct flaws.\n" +
						"\n" +
						"This initial version is mainly just stuff I wanted to see, either because I had an idea for something useful, or because I had an idea for something I thought was fun. There's not a narrow focus or theme here yet, right now it's moreso a variety pack that adds a touch to most areas of the game.\n" +
						"\n" +
						"... also I'm really sorry, I'm a programmer, not an artist. I did my best, I swear."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.ARTIFICER, 1), "The Artificer!",
				"**Pixel Dungeon Reforged introduces a new hero, for a total of seven!**\n" +
				"\n" +
				"The Artificer comes with her homemade gun. It plays similarly to the Huntress's Spirit Bow, but it is more powerful. Unfortunately, it takes many turns to reload, so it cannot be shot every turn like the Spirit Bow can.\n" +
				"\n" +
				"A low level Artificer starts experimenting with things she finds in the dungeon, namely scrolls and potions. A chance to avoid consuming a potion or scroll on use, identifying all potion types on pickup, or creating a shockwave when drinking a potion."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.ARTIFICER, 4), "Artificer Subclasses",
				"**At higher levels, the Artificer starts creating her own inventions. An artificer can either look outwards or inwards!**\n" +
				"\n" +
				"**The Constructor** creates a permanent upgradable companion that can be ordered around.\n" +
				"\n" +
				"**The Armorer** focuses on upgrading her own armor. She passively increases the blocking on all armor, passively creating light, reducing fall damage, and more."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.ARTIFICER, 6), "Artificer Armor Abilities",
				"**The Artificer has three lategame armor abilities as well!**\n" +
				"\n" +
				"**Quickdraw** allows the Artificer to fire off rapid shots at enemies all in a single turn.\n" +
				"\n" +
				"**Truesight** ensures the Artificer always has a grip on their surroundings.\n" +
				"\n" +
				"**Reflection** causes any enemy that deals damage to the Artificer to take some damage back.\n"));

		changes.addButton( new ChangeButton(Icons.SKULL.get(), "Difficulty Selection",
				"Difficulty? In MY pixel dungeon?!\n" +
				"\n" +
				"The normal difficulty (medium) is still the intended way to play. The other difficulties are there just so you can play the game you want to! (... or for bragging rights.)"));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SHORTSWORD), "New Weapon Curse: Leech",
				"Your weapon is very violent... even for a weapon. If you do not provide it the blood it desires, it will take your own instead."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.WAND_DISPLACEMENT), "Wand of Displacement",
				"Have you ever thought \"I'd like to be over there, but that guy is instead!\" Well aren't you lucky, we've got the wand for you!"));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.SCROLL_JERA), "Two new scrolls!",
				"One making their way over from Chancel Pixel Dungeon, and the other being an obvious inversion:\n\n" +
						"Scroll of Decay: Kills life, nourishes the dead.\n" +
						"Scroll of Growth: Re-kills the dead, nourishes life.\n\n" +
						"This also comes with the stone of blight, which has a weaker effect than the scroll of decay in a smaller area."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.ELIXIR_CLAWS), "Elixir of Arcane Claws",
				"With a new boss (see the mobs section) comes new drops and a new elixir! The elixir of arcane claws guarantees damage every turn against all nearby enemies."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_RINGBOX), "New Artifact: Ringbox",
				"The ringbox is a new artifact! It has slots for rings inside of it, and those rings will transfer their power to you, albeit less efficiently.\n\n" +
						"The ringbox has two slots for rings to begin with, but that increases to three slots as the ringbox levels up."));

		changes.addButton( new ChangeButton(Icons.CHALLENGE_COLOR.get(), "New Challenges",
				"I have added a couple of challenges, mainly to give people a couple more options when going for their 6 challenge runs.\n\n" +
						"**-**Horde: Twice as many enemies\n" +
						"**-**Monster Unknown: No one really needs to be able to tell mobs apart, after all"));

		changes.addButton( new ChangeButton(new Image(new HalfRipperSprite()), "Demon Halls Quest",
				"An odd Ripper Demon is located near the bottom-most floor. What do they want? We can't be sure, but that bag of upgraded equipment including weapons, armor, an artifact, a ring, and scrolls of transmutation sure looks interesting."));

		changes.addButton(new ChangeButton(Icons.RANDOMIZER.get(), "Randomizer",
				"Ever felt like Shattered Pixel Dungeon was too well balanced? The randomizer will fix that! This will select two mob types from each region. It will randomly buff one and nerf the other. Each mobs has three possible buffs and three possible nerfs it can receive. None of these are generic, all six are made with that specific mob in mind.\n\n" +
						"To unlock the randomizer, you must first finish a run. To enable the randomizer, you can find it in the difficulty selection window."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "mobs"), false, null);
		changes.hardlight(Window.TITLE_COLOR);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(new Image(new KoboldSprite.Red()), "Kobold Quest",
				"The blacksmith has a new problem. On top of gnolls and crystals, kobolds have moved in! They seem to be protecting something... dangerous.\n\nGather the gold, but beware. The creature they are defending might be upset at having its gold taken.\n\nThis quest ends with a one-on-one boss fight with a dangerous creature, as all kobolds will flee the area. It takes advantage of the mining mechanic to provide a bit more of a stealth-based battle, as approaching it from afar will end up with you taking a ton of damage, but the creature is weaker up close."));

		changes.addButton( new ChangeButton(new Image(new SpitterSprite()), "Sewer Mob: Spitter",
				"Meant to be an early game introduction to ranged enemies, the spitter acts once every two turns, and doesn't deal much damage. However, they can attack from afar!"));

		changes.addButton( new ChangeButton(new Image(new UnholyPriestSprite()), "Prison Mob: Unholy Priest",
				"When faith is all one has, it can twist your mind, your body, and your spirit. These ranged attacks deal magic damage and inflict a new status effect: Cursed. It hurts many stats mildly, but can be resisted by becoming blessed."));

		changes.addButton( new ChangeButton(new Image(new BallistaSprite()), "Caves Mob: Ballista",
				"Coming over from Darkest Pixel Dungeon, the ballista will shoot you from afar and hit like a truck. However, it needs a bit of time to reload itself."));

		changes.addButton( new ChangeButton(new Image(new FiendSprite()), "City Mob: Fiend",
				"Coming over from Yet Another Pixel Dungeon, the fiend is what happens when devotion to royalty turns into fanaticism. A being made a pure shadow and evil, its attack may be laughable, but you will not be laughing upon its death."));

		changes.addButton( new ChangeButton(new Image(new DemonGooSprite()), "Demon Halls Mob: Demon Goo",
				"Coming over from Sprouted Pixel Dungeon, the demon goo is not terribly special... until you hit it. Say, this effect seems kinda familiar...\n\nMainly I liked this effect so I wanted to use it outside of the ONE place it's currently found."));

		changes.addButton( new ChangeButton(new Image(new GooSprite()), "New Bosses",
				"There is a new boss in the sewers. You must complete a specific objective to unlock it, and after that, it will be randomly selected between the goo and this new boss."));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "changes"), false, null);
		changes.hardlight(CharSprite.WARNING);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new Image(new GhostSprite()), "Sad Ghost reward change",
				"Upon completing the sad ghost reward quest, he will offer you two artifacts rather than equipment."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.GREATAXE), "Accuracy Buff",
				"All attacks in the game have a higher accuracy. This is two-fold:\n\n" +
				"**-** Nerf the strategy of \"run around a pillar forever until something is dead\"\n" +
				"**-** Lower the amount of time in one-on-one fights where each character just misses each other 10 times in a row for some reason.\n\n" +
				"I believe this will indirectly buff the ferret tuft too, but that's alright. This will also make the game generally a bit more difficult, as you have ways of guaranteeing hits that the enemies don't have, so this is more of a buff to enemies than to you."));

		changes.addButton(new ChangeButton(Icons.get(Icons.PREFS), Messages.get(ChangesScene.class, "misc"),
				"**Highlights:**\n" +
						"**-** Added a new room type to add extra variety\n" +
						"**-** Vampiric enchantment rework: higher chance to heal, less health healed. Hopefully this will increase consistency without affecting its overall power too too much.\n" +
						"**-** When picking up items, you will be told the amount you have in your inventory if it's a stackable item (like gold, potions, etc.)"));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "buffs"), false, null);
		changes.hardlight(CharSprite.POSITIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARTIFACT_CHALICE3), "Chalice of Blood",
				"The chalice of blood will now tell you exactly how much health you will lose before you prick yourself. Most players would just get the wiki up if they didn't know if from heart, so this is just a quality of life buff."));

		changes.addButton(new ChangeButton(TerrainFeaturesTilemap.getTrapVisual(new ToxicTrap()), "Toxic Gas Room",
				"The toxic gas room was always a bit boring, only offering some gold at the cost of one of the most useful potions in the game, especially during challenge runs. Now, this room has a chance of spawning an artifact or a ring."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.GOLDEN_KEY), "Secret Chasm Room",
				"The secret chasm room (otherwise known at the four gold keys floaty room) was always an awkward one, because if you encountered it early enough, you'd have to come back for some... not always fantastic rewards. Now, this room has a chance to spawn 2 levitation potions on the floor instead of just one."));

		changes.addButton( new ChangeButton(new ItemSprite(ItemSpriteSheet.RING_TOPAZ), "Ring of Tenacity Rework",
				"The old ring of tenacity would reduce damage if you were on low health. This had a very niche use for Beserkers but even then, you were probably better off using something else.\n\n" +
				"The new ring of tenacity does not care about your current health. Instead, it reduces damage based on how high the incoming damage is, reducing a higher percentage of damage if the damage itself is higher.\n\n" +
				"For example, at base, if you were to take 10 damage, it wouldn't reduce anything (0%). If you were to take 25 damage, it would reduce 2 damage (8%). If you were to take 50 damage, it would reduce 6 damage (12%)\n\n" +
				"This is obviously a minor reduction at base level, but the amount it decreases as you level it up will get higher and higher."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.EXOTIC_INDIGO), "Potion of Mastery",
				"Personally I never felt the need to use a potion of mastery, so I've upped the bonus from 2 to 4. This may be a bit high, so I'll keep an eye on it, but for now I think it'll give this a little bump."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.WATERSKIN), "Waterskin: Water",
				"The waterskin now has the \"water\" action, which will take 10 drops to dump water out of the waterskin. Useful for putting yourself out or ruining traps on the ground!"));

		changes = new ChangeInfo(Messages.get(ChangesScene.class, "nerfs"), false, null);
		changes.hardlight(CharSprite.NEGATIVE);
		changeInfos.add(changes);

		changes.addButton(new ChangeButton(HeroSprite.avatar(HeroClass.HUNTRESS, 1), "Ring of Sharpshooting + Spirit Bow nerf",
				"While I'm not in the habit of nerfing powerful synergies (synergies are what makes roguelikes amazing,) this synergy was way too useful. The ring of sharpshooting is half as effective on the Spirit Bow and the Gun."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.RATION), "Hunger Rework",
				"I always found hunger in this game way more of an inconvenience rather than, y'know, actually starving.\n" +
				"\n" +
				"Now, starving increases exponentially. Food will satiate you for a bit longer, but if you ignore starvation, you will quickly find yourself going from 1 damage every other turn to 5-10 damage per turn."));

	}

}
