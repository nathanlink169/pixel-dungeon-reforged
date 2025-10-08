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

package com.shatteredpixel.shatteredpixeldungeon.actors.hero;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LostInventory;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Thief;
import com.shatteredpixel.shatteredpixeldungeon.items.Ankh;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KindofMisc;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.ClassArmor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Food;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfStrength;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfHoneyedHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs.ElixirOfMight;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfMastery;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.Spell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.Runestone;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ShardOfOblivion;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.Trinket;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.Wand;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Iterator;

public class Belongings implements Iterable<Item> {

	private Hero owner;

	public static class Backpack extends Bag {
		{
			image = ItemSpriteSheet.BACKPACK;
		}
		public int capacity(){
			int cap = super.capacity();
			for (Item item : items){
				if (item instanceof Bag){
					cap++;
				}
			}
			if (Dungeon.hero != null && Dungeon.hero.belongings.secondWep != null){
				//secondary weapons still occupy an inv. slot
				cap--;
			}
			return cap;
		}
	}

	public Backpack backpack;
	
	public Belongings( Hero owner ) {
		this.owner = owner;
		
		backpack = new Backpack();
		backpack.owner = owner;
	}

	public KindOfWeapon weapon = null;
	public Armor armor = null;
	public Artifact artifact = null;
	public KindofMisc misc = null;
	public Ring ring = null;

	//used when thrown weapons temporary become the current weapon
	public KindOfWeapon thrownWeapon = null;

	//used to ensure that the duelist always uses the weapon she's using the ability of
	public KindOfWeapon abilityWeapon = null;

	//used by the champion subclass
	public KindOfWeapon secondWep = null;

	//*** these accessor methods are so that worn items can be affected by various effects/debuffs
	// we still want to access the raw equipped items in cases where effects should be ignored though,
	// such as when equipping something, showing an interface, or dealing with items from a dead hero

	//normally the primary equipped weapon, but can also be a thrown weapon or an ability's weapon
	public KindOfWeapon attackingWeapon(){
		if (thrownWeapon != null) return thrownWeapon;
		if (abilityWeapon != null) return abilityWeapon;
		return weapon();
	}

	//we cache whether belongings are lost to avoid lots of calls to hero.buff(LostInventory.class)
	private boolean lostInvent;
	public void lostInventory( boolean val ){
		lostInvent = val;
	}

	public boolean lostInventory(){
		return lostInvent;
	}

	public KindOfWeapon weapon(){
		if (!lostInventory() || (weapon != null && weapon.keptThroughLostInventory())){
			return weapon;
		} else {
			return null;
		}
	}

	public Armor armor(){
		if (!lostInventory() || (armor != null && armor.keptThroughLostInventory())){
			return armor;
		} else {
			return null;
		}
	}

	public Artifact artifact(){
		if (!lostInventory() || (artifact != null && artifact.keptThroughLostInventory())){
			return artifact;
		} else {
			return null;
		}
	}

	public KindofMisc misc(){
		if (!lostInventory() || (misc != null && misc.keptThroughLostInventory())){
			return misc;
		} else {
			return null;
		}
	}

	public Ring ring(){
		if (!lostInventory() || (ring != null && ring.keptThroughLostInventory())){
			return ring;
		} else {
			return null;
		}
	}

	public KindOfWeapon secondWep(){
		if (!lostInventory() || (secondWep != null && secondWep.keptThroughLostInventory())){
			return secondWep;
		} else {
			return null;
		}
	}

	// ***
	
	private static final String WEAPON		= "weapon";
	private static final String ARMOR		= "armor";
	private static final String ARTIFACT   = "artifact";
	private static final String MISC       = "misc";
	private static final String RING       = "ring";

	private static final String SECOND_WEP = "second_wep";

	public void storeInBundle( Bundle bundle ) {
		
		backpack.storeInBundle( bundle );
		
		bundle.put( WEAPON, weapon );
		bundle.put( ARMOR, armor );
		bundle.put( ARTIFACT, artifact );
		bundle.put( MISC, misc );
		bundle.put( RING, ring );
		bundle.put( SECOND_WEP, secondWep );
	}
	
	public void restoreFromBundle( Bundle bundle ) {
		
		backpack.clear();
		backpack.restoreFromBundle( bundle );
		
		weapon = (KindOfWeapon) bundle.get(WEAPON);
		if (weapon() != null)       weapon().activate(owner);
		
		armor = (Armor)bundle.get( ARMOR );
		if (armor() != null)        armor().activate( owner );

		artifact = (Artifact) bundle.get(ARTIFACT);
		if (artifact() != null)     artifact().activate(owner);

		misc = (KindofMisc) bundle.get(MISC);
		if (misc() != null)         misc().activate( owner );

		ring = (Ring) bundle.get(RING);
		if (ring() != null)         ring().activate( owner );

		secondWep = (KindOfWeapon) bundle.get(SECOND_WEP);
		if (secondWep() != null)    secondWep().activate(owner);
	}
	
	public static void preview( GamesInProgress.Info info, Bundle bundle ) {
		if (bundle.contains( ARMOR )){
			Armor armor = ((Armor)bundle.get( ARMOR ));
			if (armor instanceof ClassArmor){
				info.armorTier = 6;
			} else {
				info.armorTier = armor.tier;
			}
		} else {
			info.armorTier = 0;
		}
	}

	//ignores lost inventory debuff
	public ArrayList<Bag> getBags(){
		ArrayList<Bag> result = new ArrayList<>();

		result.add(backpack);

		for (Item i : this){
			if (i instanceof Bag){
				result.add((Bag)i);
			}
		}

		return result;
	}
	
	@SuppressWarnings("unchecked")
	public<T extends Item> T getItem( Class<T> itemClass ) {

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return (T) item;
				}
			}
		}
		
		return null;
	}

	public<T extends Item> ArrayList<T> getAllItems( Class<T> itemClass ) {
		ArrayList<T> result = new ArrayList<>();

		boolean lostInvent = lostInventory();

		for (Item item : this) {
			if (itemClass.isInstance( item )) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add((T) item);
				}
			}
		}

		return result;
	}
	
	public boolean contains( Item contains ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (contains == item) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public Item getSimilar( Item similar ){

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (similar != item && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					return item;
				}
			}
		}
		
		return null;
	}
	
	public ArrayList<Item> getAllSimilar( Item similar ){
		ArrayList<Item> result = new ArrayList<>();

		boolean lostInvent = lostInventory();
		
		for (Item item : this) {
			if (item != similar && similar.isSimilar(item)) {
				if (!lostInvent || item.keptThroughLostInventory()) {
					result.add(item);
				}
			}
		}
		
		return result;
	}

	//triggers when a run ends, so ignores lost inventory effects
	public void identify() {
		for (Item item : this) {
			item.identify(false);
		}
	}
	
	public void observe() {
		if (weapon() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && weapon() instanceof Weapon){
				((Weapon) weapon()).setIDReady();
			} else {
				weapon().identify();
				Badges.validateItemLevelAquired(weapon());
			}
		}
		if (secondWep() != null){
			if (ShardOfOblivion.passiveIDDisabled() && secondWep() instanceof Weapon){
				((Weapon) secondWep()).setIDReady();
			} else {
				secondWep().identify();
				Badges.validateItemLevelAquired(secondWep());
			}
		}
		if (armor() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				armor().setIDReady();
			} else {
				armor().identify();
				Badges.validateItemLevelAquired(armor());
			}
		}
		if (artifact() != null) {
			//oblivion shard does not prevent artifact IDing
			artifact().identify();
			Badges.validateItemLevelAquired(artifact());
		}
		if (misc() != null) {
			if (ShardOfOblivion.passiveIDDisabled() && misc() instanceof Ring){
				((Ring) misc()).setIDReady();
			} else {
				misc().identify();
				Badges.validateItemLevelAquired(misc());
			}
		}
		if (ring() != null) {
			if (ShardOfOblivion.passiveIDDisabled()){
				ring().setIDReady();
			} else {
				ring().identify();
				Badges.validateItemLevelAquired(ring());
			}
		}
		if (ShardOfOblivion.passiveIDDisabled()){
			GLog.p(Messages.get(ShardOfOblivion.class, "identify_ready_worn"));
		}
		for (Item item : backpack) {
			if (item instanceof EquipableItem || item instanceof Wand) {
				item.cursedKnown = true;
			}
		}
		Item.updateQuickslot();
	}
	
	public void uncurseEquipped() {
		ScrollOfRemoveCurse.uncurse( owner, armor(), weapon(), artifact(), misc(), ring(), secondWep());
	}
	
	public Item randomUnequipped() {
		if (owner.buff(LostInventory.class) != null) return null;

		return Random.element( backpack.items );
	}

	public Item getThiefItemToSteal() {
		if (owner.buff(LostInventory.class) != null) return null;

		Item toReturn = null;

		if (Thief.getRandomizerEnabled(Thief.RandomTraits.BOLD_FINGERS)) {
			if (Random.Int(2) == 0 || true) { // 50% chance to steal something equipped
				int attempts = 0;
				do {
					int itemToUnequip = Random.Int(5);
					switch (itemToUnequip) {
						case 0: // weapon
							if (weapon() == null || weapon().unique) continue;
							toReturn = weapon();
							weapon = null;
							break;
						case 1: // armour
							if (armor() == null || armor instanceof ClassArmor || armor.checkSeal() != null || armor().unique) continue;
							toReturn = armor();
							armor = null;
							break;
						case 2: // artifact
							if (artifact() == null || artifact().unique) continue;
							toReturn = artifact();
							artifact = null;
							break;
						case 3: // misc
							if (misc() == null || misc().unique) continue;
							toReturn = misc();
							misc = null;
							break;
						case 4: // ring
							if (ring() == null || ring().unique) continue;
							toReturn = ring();
							ring = null;
							break;
					}
				} while (toReturn == null && ++attempts < 100);
				if (toReturn != null) {
					Dungeon.quickslot.clearItem(toReturn);
					toReturn.updateQuickslot();
				}

				return toReturn;
			}
			// otherwise, fallthrough to normal selection
		}
		if (Thief.getRandomizerEnabled(Thief.RandomTraits.MASTER_PICKPOCKET)) {
			ArrayList<Item> highestPriority = new ArrayList<>();
			ArrayList<Item> secondPriority = new ArrayList<>();

			for (Item item : backpack.items) {
				if (item instanceof ScrollOfUpgrade ||
				    item instanceof PotionOfStrength ||
				    item instanceof PotionOfMastery ||
					item instanceof ElixirOfMight ||
					item instanceof Ankh ||
					item.level() > 5) {
					highestPriority.add(item);
				} else
				if (item instanceof Artifact ||
					item instanceof Trinket ||
					item instanceof PotionOfHealing ||
					item instanceof ElixirOfHoneyedHealing ||
					item instanceof Honeypot ||
					item instanceof Sungrass.SungrassSeed ||
					item instanceof PotionOfExperience ||
					item instanceof ScrollOfTransmutation) {
					secondPriority.add(item);
				}
			}

			if (!highestPriority.isEmpty()) {
				return Random.element(highestPriority);
			}
			if (!secondPriority.isEmpty()) {
				return Random.element(secondPriority);
			}
			// Fall through
		}

		boolean validSteal = false;
		int attempts = 0;
		do {
			toReturn = Random.element(backpack.items);
			validSteal = toReturn != null && !toReturn.unique && toReturn.level() < 1;
		} while (!validSteal && ++attempts < 100 );
		if (!validSteal) {
			return null;
		}
		return toReturn;
	}
	
	public int charge( float charge ) {
		
		int count = 0;
		
		for (Wand.Charger charger : owner.buffs(Wand.Charger.class)){
			charger.gainCharge(charge);
			count++;
		}
		
		return count;
	}

	@Override
	public Iterator<Item> iterator() {
		return new ItemIterator();
	}
	
	private class ItemIterator implements Iterator<Item> {

		private int index = 0;
		
		private Iterator<Item> backpackIterator = backpack.iterator();
		
		private Item[] equipped = {weapon, armor, artifact, misc, ring, secondWep};
		private int backpackIndex = equipped.length;
		
		@Override
		public boolean hasNext() {
			
			for (int i=index; i < backpackIndex; i++) {
				if (equipped[i] != null) {
					return true;
				}
			}
			
			return backpackIterator.hasNext();
		}

		@Override
		public Item next() {
			
			while (index < backpackIndex) {
				Item item = equipped[index++];
				if (item != null) {
					return item;
				}
			}
			
			return backpackIterator.next();
		}

		@Override
		public void remove() {
			switch (index) {
			case 0:
				equipped[0] = weapon = null;
				break;
			case 1:
				equipped[1] = armor = null;
				break;
			case 2:
				equipped[2] = artifact = null;
				break;
			case 3:
				equipped[3] = misc = null;
				break;
			case 4:
				equipped[4] = ring = null;
				break;
			case 5:
				equipped[5] = secondWep = null;
				break;
			default:
				backpackIterator.remove();
			}
		}
	}

	public void TriggerAdaptive() {
		ArrayList<Item> items = getAllItems(Item.class);

		// detach all items first, just so that we don't drop items that don't need to
		// be dropped because we're removing later stuff
		for (Item item : items) {
			if (item.unique) {
				continue;
			}

			if (item == weapon || item == armor || item == artifact || item == ring || item == misc || item == secondWep) {
				continue;
			}

			if (item instanceof Armor) {
				// because of basic armour, we should only shuffle its glyph/curse
				continue;
			}

			item.detachAll(backpack);
		}

		for (Item item : items) {
			if (item.unique) {
				continue;
			}

			int totalCount = item.quantity();
			for (int i = 0; i < totalCount; ++i) {
				if (item instanceof MeleeWeapon) {
					ReplaceMeleeWeapon((MeleeWeapon)item);
				} else if (item instanceof MissileWeapon) {
					ReplaceMissile((MissileWeapon) item, i == 0);
				} else if (item instanceof Scroll) {
					ReplaceScroll();
				} else if (item instanceof Potion) {
					ReplacePotion();
				} else if (item instanceof Food) {
					ReplaceFood();
				} else if (item instanceof Runestone || item instanceof Plant.Seed) {
					ReplaceSeedsAndStones();
				} else if (item instanceof Artifact) {
					ReplaceArtifacts((Artifact) item);
				} else if (item instanceof Bomb) {
					ReplaceBomb();
				} else if (item instanceof Ring) {
					ReplaceRing((Ring) item);
				} else if (item instanceof Spell) {
					ReplaceSpell();
				} else if (item instanceof Trinket) {
					ReplaceTrinket();
				} else if (item instanceof Wand) {
					ReplaceWand((Wand) item);
				} else if (item instanceof Armor) {
					ShuffleArmor((Armor) item);
				} else {
					item.collect();
					break;
				}
			}
		}
	}

	private void ReplaceMeleeWeapon(MeleeWeapon wep) {
		int tier = wep.tier;
		Generator.Category c = Generator.Category.WEP_T1;
		switch (tier) {
			case 1:
				//c = Generator.Category.WEP_T1;
				break;
			case 2:
				c = Generator.Category.WEP_T2;
				break;
			case 3:
				c = Generator.Category.WEP_T3;
				break;
			case 4:
				c = Generator.Category.WEP_T4;
				break;
			case 5:
				c = Generator.Category.WEP_T5;
				break;
		}
		boolean enchanted = wep.hasGoodEnchant();
		boolean cursed = wep.cursed;
		boolean curseKnown = wep.cursedKnown;
		boolean identified = wep.isIdentified();

		MeleeWeapon w = (MeleeWeapon)Generator.random(c);
		int equipped = 0; // 0 = not equipped, 1 primary weapon, 2 secondary weapon
		if (wep == this.weapon()) {
			equipped = 1;
		} else if (wep == this.secondWep()) {
			equipped = 2;
		}
		if (equipped == 1) {
			wep.detach(backpack);
			weapon = w;
		} else if (equipped == 2) {
			wep.detach(backpack);
			secondWep = w;
		} else {
			if (!w.collect()) {
				Dungeon.level.drop( w, Dungeon.hero.pos ).sprite.drop();
			}
		}

		if (identified) {
			w.identify();
		}
		if (enchanted) {
			w.enchant();
		}
		else if (cursed) {
			w.enchant(Weapon.Enchantment.randomCurse());
			w.cursed = true;
		}
		else {
			w.enchantment = null;
			w.cursed = false;
		}
		if (curseKnown) {
			w.cursedKnown = true;
		}
		w.setLevel(wep.level());
	}

	private void ReplaceMissile(MissileWeapon missile, boolean firstInList) {
		MissileWeapon w = Generator.randomMissile();
		boolean enchanted = missile.hasGoodEnchant();
		boolean cursed = missile.cursed;
		boolean identified = missile.isIdentified();
		int upgrades = missile.level();

		if (identified) {
			w.identify();
		}
		if (enchanted) {
			w.enchant();
		}
		if (cursed) {
			w.enchant(Weapon.Enchantment.randomCurse());
			w.cursed = true;
		}
		w.upgrade(upgrades);
		w.quantity(1);
		if (firstInList) {
			float durabilityPercentage = missile.GetDurability()/ MissileWeapon.MAX_DURABILITY;
			w.SetDurability(durabilityPercentage * MissileWeapon.MAX_DURABILITY);
		}
		if (!w.collect()) {
			Dungeon.level.drop( w, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void ReplacePotion() {
		Potion p = null;

		switch (Random.Int(4)) {
			case 0: default:
				p = (Potion) Generator.random(Generator.Category.POTION);
				break;
			case 1:
				p = (Potion) Generator.random(Generator.Category.POTION);
				if (ExoticPotion.regToExo.containsKey(p.getClass())){
					p = Reflection.newInstance(ExoticPotion.regToExo.get(p.getClass()));
				}
				break;
			case 2:
				p = (Potion) Generator.random(Generator.Category.BREW);
				break;
			case 3:
				p = (Potion) Generator.random(Generator.Category.ELIXIR);
				break;
		}
		if (!p.collect()) {
			Dungeon.level.drop( p, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void ReplaceScroll() {
		Scroll s = (Scroll) Generator.random(Generator.Category.SCROLL);

		if (Random.Float() >= 0.5f && ExoticScroll.regToExo.containsKey(s.getClass())){
			s = Reflection.newInstance(ExoticScroll.regToExo.get(s.getClass()));
		}

		if (!s.collect()) {
			Dungeon.level.drop( s, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void ReplaceFood() {
		Food f = (Food) Generator.random(Generator.Category.ALL_FOOD);
		if (!f.collect()) {
			Dungeon.level.drop(f, Dungeon.hero.pos).sprite.drop();
		}
	}

	private void ReplaceSeedsAndStones() {
		Item i = null;

		switch (Random.Int(2)) {
			case 0: default:
				i = Generator.random(Generator.Category.SEED);
				break;
			case 1:
				i = Generator.random(Generator.Category.STONE);
				break;
		}
		if (!i.collect()) {
			Dungeon.level.drop( i, Dungeon.hero.pos ).sprite.drop();
		}
	}

	private void ReplaceArtifacts(Artifact artifact) {
		Artifact a = Generator.randomArtifact();

		if (a == null) { // keep this artifact
			if (!(artifact == this.artifact || artifact == this.misc)) {
				artifact.collect();
			}
		}
		else {
			Generator.readdArtifact(artifact.getClass());
			a.cursed = artifact.cursed;
			a.upgrade(artifact.level());
			a.cursedKnown = artifact.cursedKnown;

			if (artifact == this.artifact) {
				artifact.detach(backpack);
				this.artifact = a;
			} else if (artifact == this.misc) {
				artifact.detach(backpack);
				this.misc = a;
			} else {
				a.collect();
			}
		}
	}

	private void ReplaceBomb() {
		Generator.random(Generator.Category.BOMB).collect();
	}

	private void ReplaceRing(Ring ring) {
		Ring r = (Ring) Generator.random(Generator.Category.RING);
		r.cursed = ring.cursed;
		r.upgrade(ring.level());
		r.cursedKnown = ring.cursedKnown;

		if (ring == this.ring) {
			ring.detach(backpack);
			this.ring = r;
		} else if (ring == this.misc) {
			ring.detach(backpack);
			this.misc = r;
		} else {
			r.collect();
		}
	}

	private void ReplaceSpell() {
		Generator.random(Generator.Category.SPELL).collect();
	}

	private void ReplaceTrinket() {
		Generator.random(Generator.Category.TRINKET).collect();
	}

	private void ReplaceWand(Wand wand) {
		Wand w = (Wand) Generator.random(Generator.Category.WAND);
		w.cursed = wand.cursed;
		w.cursedKnown = wand.cursedKnown;
		w.upgrade(wand.level());
		w.curCharges = wand.curCharges;
		w.partialCharge = wand.partialCharge;
		if (wand.isIdentified()) {
			w.identify();
		}
	}

	private void ShuffleArmor(Armor a) {
		if (a.hasGoodGlyph()) {
			a.inscribe();
		}
		if (a.hasCurseGlyph()) {
			a.glyph = Armor.Glyph.randomCurse();
		}
	}
}
