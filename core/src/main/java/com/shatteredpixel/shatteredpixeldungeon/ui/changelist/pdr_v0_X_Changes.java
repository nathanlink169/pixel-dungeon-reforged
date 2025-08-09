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
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.levels.traps.ToxicTrap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.ChangesScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.BallistaSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.DemonGooSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.FiendSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.GhostSprite;
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
		add_v0_1_1_Changes(changeInfos);
		add_v0_1_Changes(changeInfos);
	}

	public static void add_Coming_Soon( ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("What's next?", true, "");
		changes.hardlight(0xCCCCCC);
		changeInfos.add(changes);

		changes.addButton( new ChangeButton(Icons.get(Icons.PDR), "Overview",
				"The next minor update will be addressing balancing feedback. I'm sure some things are overtuned or undertuned, especially with the Artificer and the new boss. The minor update will be bug fixes and balancing those two in particular.\n" +
				"\n" +
				"After that, I have a few ideas in mind. The other features explained in the coming soon section will be the next major update. After that, the ultimate goal is to create an \"endless mode\" as an alternative to the ascension challenge. On top of that, I want to create an alternate boss for each region.\n" +
				"\n" +
				"I plan to keep this branched away from Shattered Pixel Dungeon. I'll bring things in if I feel they'll really improve the experience, in the same way I've brought over some features from Darkest Pixel Dungeon, Chancel Pixel Dungeon, etc.\n\nFor those mods, the goal is to celebrate those features, not outright steal them. A lot of the games I've pulled features from are no longer receiving updates, but if any of the creators have a problem with me using their content, let me know and I will remedy that."));

		changes.addButton( new ChangeButton(new Image(new SkeletonSprite()), "New Prison Boss",
				"One of the things I want to do is different sized dynamic bosses. I know we technically have the crystal in the troll quest, but that is very static.\n" +
				"\n" +
				"The current plan is to bring in a very large skeleton (name pending.) The only way to destroy it is to crush its skull, but you'll have to break down its legs to get there. This large, dynamic, multi-stage boss is something that I think will add more life to the existing bosses."));

		changes.addButton( new ChangeButton(Icons.get(Icons.PREFS), "Behind the Scenes Changes",
				"One of the main things I'd like to do is some behind the scenes changes to how data is laid out. Everything is very hard-coded (i.e. the numbers such as health, damage, etc. are put directly in the code rather than another file,) which can make it a little difficult to change what you need quickly. I'd like to create some tools allow faster development on certain things."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.MAGE, 1), "Hero Visual Customization",
				"I'd like to add some visual customization for heroes. I won't be able to change the hero splash art because I am not good at art, but I would like to be able to change the sprite: skin colour, hair colour, hairstyle, gender presentation, etc."));

		changes.addButton( new ChangeButton(HeroSprite.avatar(HeroClass.DUELIST, 1), "Level Up Changes",
				"While I do enjoy the usual level up of Shattered Pixel Dungeon, I'd like to add another game mode: Multiclassing.\n\nIn my mind, it's inspired by Dungeons and Dragons where different classes have different health, defense bonus, attack bonus, and features, etc. however, each section would cost a different amount of XP to level, meaning it's easy to level something you haven't put much effort into yet.\n\nThis would not replace the typical hero progression and would be a separate gamemode."));

		changes.addButton( new ChangeButton(Icons.get(Icons.JOURNAL), "Journal Upgrade",
				"The current journal is alright, but I'd like to upgrade it a little bit. Every time you kill an enemy, they will have a chance to drop a journal page which has much more detailed information, including their damage range, damage reduction, accuracy and evasion, etc."));

		changes.addButton( new ChangeButton(Icons.get(Icons.CHANGES), "New Run Revamp",
				"Given that I am trying to add a few new features and ways to play, I want to create a completely new UI for the start of a run. This is partly to accommodate all the new features, and it is partly to accommodate that I cannot make splash art for the life of me."));

		changes.addButton(new ChangeButton(Icons.get(Icons.KEYBOARD), "Creative Mode",
				"I don't want to add a whole dungeon editor to the game, but a sandbox mode were you can spawn creatures, give yourself items, set your own stats, etc. so you can test strategies (and so I can test the game) would be nice."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.SPIRIT_BOW), "Thrown Weapons Rework",
				"Version 3.2 of Shattered Pixel Dungeon made a change to thrown weapons. I have my own way I'd like to take it, with every character having a melee weapon and a ranged weapon, and being able to find each as they go through the dungeon. In addition, most thrown weapons will be transferred into ammunition for these ranged weapons, with a quiver slot available."));

		changes.addButton(new ChangeButton(new ItemSprite(ItemSpriteSheet.ARMOR_LEATHER), "Armour Rework",
				"The current armour system in the game is very basic, with only one armour for each tier. I'd like to introduce some variety. For example, armour that protects for more but makes you slow, armour that increases evasion but gives 0 protection, etc."));
	}

	public static void add_v0_1_1_Changes(ArrayList<ChangeInfo> changeInfos) {

		ChangeInfo changes = new ChangeInfo("v0.1.1", true, "");
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

		changes.addButton(new ChangeButton(new Image(Assets.Sprites.SPINNER, 144, 0, 16, 16), Messages.get(ChangesScene.class, "bugfixes"),
				"Fixed the following bugs:\n\n" +
						"**-**Quick Callibration was not reducing eating time.\n" +
						"**-**Duelist would crash when using certain weapon abilities."));
	}

	public static void add_v0_1_Changes(ArrayList<ChangeInfo> changeInfos ) {

		ChangeInfo changes = new ChangeInfo("v0.1", true, "");
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
