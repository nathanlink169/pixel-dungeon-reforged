# BundleableProperty System Documentation

## Overview

The `BundleableProperty` system is a type-safe wrapper around Pixel Dungeon's `Bundle` serialization that reduces boilerplate, eliminates errors, and makes save/load code more maintainable.

### Key Benefits
- **Less Boilerplate**: One line of declaration vs. two for each property
- **Type Safety**: Compile-time checking prevents serialization errors
- **Automatic Optimization**: Only saves values that differ from defaults
- **Self-Documenting**: Property declaration includes key and default value
- **Helper Methods**: Built-in increment, toggle, etc. for common operations
- **Null Safety**: Explicit nullable types prevent null-related bugs

---

## The Problem with the Old System

### Example: Old Way

```java
public class Monster extends Mob {
    private boolean seenPlayer = false;
    private int aggroTurns = 0;
    private float damageMultiplier = 1.0f;
    private String lastKnownPlayerLocation = "";
    
    // String constants for keys
    private static final String SEEN_PLAYER = "seen_player";
    private static final String AGGRO_TURNS = "aggro_turns";
    private static final String DAMAGE_MULT = "damage_mult";
    private static final String LAST_LOCATION = "last_location";
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(SEEN_PLAYER, seenPlayer);
        bundle.put(AGGRO_TURNS, aggroTurns);
        bundle.put(DAMAGE_MULT, damageMultiplier);
        bundle.put(LAST_LOCATION, lastKnownPlayerLocation);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        seenPlayer = bundle.getBoolean(SEEN_PLAYER);
        aggroTurns = bundle.getInt(AGGRO_TURNS);
        damageMultiplier = bundle.getFloat(DAMAGE_MULT);
        lastKnownPlayerLocation = bundle.getString(LAST_LOCATION);
    }
}
```

### Problems with the Old Way

1. **Verbose**: 4 lines per field (declaration, constant, store, restore)
2. **Type Mismatches**: Nothing prevents `bundle.getInt()` on a float key
3. **No Default Handling**: Saves every value even if it's the default
4. **Scattered Logic**: Declaration, constant, and serialization in different places

---

## How BundleableProperty Solves It

### Example: New Way

```java
public class Monster extends Mob {
    private BundleableProperty.Bool seenPlayer = 
        new BundleableProperty.Bool("seen_player", false);
    private BundleableProperty.Int aggroTurns = 
        new BundleableProperty.Int("aggro_turns", 0);
    private BundleableProperty.Float damageMultiplier = 
        new BundleableProperty.Float("damage_mult", 1.0f);
    private BundleableProperty.Str lastKnownPlayerLocation = 
        new BundleableProperty.Str("last_location", "");
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        seenPlayer.Store(bundle);
        aggroTurns.Store(bundle);
        damageMultiplier.Store(bundle);
        lastKnownPlayerLocation.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        seenPlayer.Restore(bundle);
        aggroTurns.Restore(bundle);
        damageMultiplier.Restore(bundle);
        lastKnownPlayerLocation.Restore(bundle);
    }
}
```

### Benefits

✅ **Self-Contained**: Key and default in one place  
✅ **Type-Safe**: Can't call wrong getter/setter  
✅ **Automatic Optimization**: Only saves non-default values  
✅ **Less Code**: No constant declarations needed  

---

## Property Types Reference

### Primitive Types

#### BundleableProperty.Int
```java
// Basic integer property
private BundleableProperty.Int health = new BundleableProperty.Int("health", 100);

// With starting value different from default
private BundleableProperty.Int level = new BundleableProperty.Int("level", 1, 5);

// Usage
health.Set(50);
int currentHealth = health.Get();
health.Increment();       // health++
health.Decrement();       // health--
health.Add(10);           // health += 10
health.Subtract(5);       // health -= 5
```

#### BundleableProperty.Long
```java
private BundleableProperty.Long experiencePoints = 
    new BundleableProperty.Long("xp", 0L);

// Usage
experiencePoints.Set(1000L);
long xp = experiencePoints.Get();
experiencePoints.Increment();
experiencePoints.Add(500L);
```

#### BundleableProperty.Float
```java
private BundleableProperty.Float speed = 
    new BundleableProperty.Float("speed", 1.0f);

// Usage
speed.Set(1.5f);
float currentSpeed = speed.Get();
speed.Increment();        // speed += 1.0f
speed.Add(0.25f);         // speed += 0.25f
speed.Subtract(0.1f);     // speed -= 0.1f
```

#### BundleableProperty.Bool
```java
private BundleableProperty.Bool isActive = 
    new BundleableProperty.Bool("active", false);

// Usage
isActive.Set(true);
boolean active = isActive.Get();
isActive.Toggle();        // isActive = !isActive
```

#### BundleableProperty.Str
```java
private BundleableProperty.Str name = 
    new BundleableProperty.Str("name", "Unknown");

// Usage
name.Set("Goblin");
String currentName = name.Get();
```

#### BundleableProperty.Clazz
```java
private BundleableProperty.Clazz weaponClass = 
    new BundleableProperty.Clazz("weapon_class", null);

// Usage
weaponClass.Set(Sword.class);
Class<?> currentClass = weaponClass.Get();
```

### Enum Type

#### BundleableProperty.Enum
```java
public enum State { IDLE, HUNTING, FLEEING }

private BundleableProperty.Enum<State> currentState = 
    new BundleableProperty.Enum<>("state", State.IDLE);

// Usage
currentState.Set(State.HUNTING);
State state = currentState.Get();
```

### Object Type

#### BundleableProperty.Object
```java
// For any Bundlable object
private BundleableProperty.Object<Item> heldItem = 
    new BundleableProperty.Object<>("held_item", null);

// Usage
heldItem.Set(new Sword());
Item item = heldItem.Get();
```

### Nested Bundle

#### BundleableProperty.NestedBundle
```java
private BundleableProperty.NestedBundle customData = 
    new BundleableProperty.NestedBundle("custom_data");

// Usage
Bundle data = customData.Get();
data.put("key", "value");
```

### Array Types

#### BundleableProperty.IntArray
```java
private BundleableProperty.IntArray inventory = 
    new BundleableProperty.IntArray("inventory", new int[0]);

// Usage
inventory.Set(new int[]{1, 2, 3, 4, 5});
int[] items = inventory.Get();
```

#### BundleableProperty.LongArray
```java
private BundleableProperty.LongArray timestamps = 
    new BundleableProperty.LongArray("timestamps", new long[0]);
```

#### BundleableProperty.FloatArray
```java
private BundleableProperty.FloatArray positions = 
    new BundleableProperty.FloatArray("positions", new float[0]);
```

#### BundleableProperty.BoolArray
```java
private BundleableProperty.BoolArray flags = 
    new BundleableProperty.BoolArray("flags", new boolean[0]);
```

#### BundleableProperty.StringArray
```java
private BundleableProperty.StringArray names = 
    new BundleableProperty.StringArray("names", new String[0]);
```

#### BundleableProperty.ClassArray
```java
private BundleableProperty.ClassArray resistances = 
    new BundleableProperty.ClassArray("resistances", new Class[0]);
```

#### BundleableProperty.BundleArray
```java
private BundleableProperty.BundleArray savedStates = 
    new BundleableProperty.BundleArray("states", new Bundle[0]);
```

### Collection Type

#### BundleableProperty.BundlableCollection
```java
private BundleableProperty.BundlableCollection<Item> items = 
    new BundleableProperty.BundlableCollection<>("items");

// Usage
items.Add(new Sword());
items.Remove(sword);
items.Clear();
Collection<Item> allItems = items.Get();
```

### Nullable Types

Use these when you need to distinguish between "not set" and "set to default value".

#### BundleableProperty.NullableInt
```java
private BundleableProperty.NullableInt optionalValue = 
    new BundleableProperty.NullableInt("optional");

// Usage
optionalValue.Set(100);
optionalValue.Set(null);  // Clear value
if (optionalValue.HasValue()) {
    int value = optionalValue.Get();
}
```

#### BundleableProperty.NullableLong
```java
private BundleableProperty.NullableLong optionalLong = 
    new BundleableProperty.NullableLong("optional_long");
```

#### BundleableProperty.NullableFloat
```java
private BundleableProperty.NullableFloat optionalFloat = 
    new BundleableProperty.NullableFloat("optional_float");

// Usage
if (optionalFloat.HasValue()) {
    optionalFloat.Multiply(2.0f);
}
```

#### BundleableProperty.NullableBool
```java
private BundleableProperty.NullableBool optionalBool = 
    new BundleableProperty.NullableBool("optional_bool");
```

---

## Usage Patterns

### Basic Usage

```java
public class Example implements Bundlable {
    private BundleableProperty.Int counter = 
        new BundleableProperty.Int("counter", 0);
    
    @Override
    public void storeInBundle(Bundle bundle) {
        counter.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        counter.Restore(bundle);
    }
    
    public void incrementCounter() {
        counter.Increment();
    }
}
```

### Multiple Properties

```java
public class Character implements Bundlable {
    private BundleableProperty.Str name = 
        new BundleableProperty.Str("name", "Hero");
    private BundleableProperty.Int level = 
        new BundleableProperty.Int("level", 1);
    private BundleableProperty.Float health = 
        new BundleableProperty.Float("health", 100.0f);
    private BundleableProperty.Bool isAlive = 
        new BundleableProperty.Bool("alive", true);
    
    @Override
    public void storeInBundle(Bundle bundle) {
        name.Store(bundle);
        level.Store(bundle);
        health.Store(bundle);
        isAlive.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        name.Restore(bundle);
        level.Restore(bundle);
        health.Restore(bundle);
        isAlive.Restore(bundle);
    }
}
```

### With Inheritance

```java
public class Monster extends Mob {
    private BundleableProperty.Bool seenPlayer = 
        new BundleableProperty.Bool("seen_player", false);
    
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);  // Call parent first
        seenPlayer.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);  // Call parent first
        seenPlayer.Restore(bundle);
    }
}
```

### Complex Objects

```java
public class Inventory implements Bundlable {
    private BundleableProperty.BundlableCollection<Item> items = 
        new BundleableProperty.BundlableCollection<>("items");
    private BundleableProperty.Int gold = 
        new BundleableProperty.Int("gold", 0);
    
    @Override
    public void storeInBundle(Bundle bundle) {
        items.Store(bundle);
        gold.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        items.Restore(bundle);
        gold.Restore(bundle);
    }
    
    public void addItem(Item item) {
        items.Add(item);
    }
    
    public void addGold(int amount) {
        gold.Add(amount);
    }
}
```

### Enum Properties

```java
public class StateMachine implements Bundlable {
    public enum State { IDLE, ACTIVE, PAUSED, FINISHED }
    
    private BundleableProperty.Enum<State> currentState = 
        new BundleableProperty.Enum<>("state", State.IDLE);
    
    @Override
    public void storeInBundle(Bundle bundle) {
        currentState.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        currentState.Restore(bundle);
    }
    
    public void setState(State state) {
        currentState.Set(state);
    }
    
    public boolean isActive() {
        return currentState.Get() == State.ACTIVE;
    }
}
```

### Optional Values

```java
public class Quest implements Bundlable {
    // Use nullable for optional completion time
    private BundleableProperty.NullableLong completionTime = 
        new BundleableProperty.NullableLong("completion_time");
    
    @Override
    public void storeInBundle(Bundle bundle) {
        completionTime.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        completionTime.Restore(bundle);
    }
    
    public void complete() {
        completionTime.Set(System.currentTimeMillis());
    }
    
    public boolean isCompleted() {
        return completionTime.HasValue();
    }
    
    public long getCompletionTime() {
        return completionTime.HasValue() ? completionTime.Get() : 0L;
    }
}
```

---

## Migration Guide

### Step-by-Step Migration

#### Step 1: Identify Old Pattern

```java
// Old pattern to look for:
private SomeType fieldName = defaultValue;
private static final String FIELD_NAME = "field_name";

// In storeInBundle:
bundle.put(FIELD_NAME, fieldName);

// In restoreFromBundle:
fieldName = bundle.getSomeType(FIELD_NAME);
```

#### Step 2: Replace with BundleableProperty

```java
// New pattern:
private BundleableProperty.SomeType fieldName = 
    new BundleableProperty.SomeType("field_name", defaultValue);

// In storeInBundle:
fieldName.Store(bundle);

// In restoreFromBundle:
fieldName.Restore(bundle);
```

#### Step 3: Update All References

```java
// Old: Direct access
if (fieldName > 10) { ... }
fieldName = 20;

// New: Use Get/Set
if (fieldName.Get() > 10) { ... }
fieldName.Set(20);
```

### Migration Examples

#### Example 1: Boolean Flag

**Before:**
```java
private boolean discovered = false;
private static final String DISCOVERED = "discovered";

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(DISCOVERED, discovered);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    discovered = bundle.getBoolean(DISCOVERED);
}

public void discover() {
    discovered = true;
}

public boolean isDiscovered() {
    return discovered;
}
```

**After:**
```java
private BundleableProperty.Bool discovered = 
    new BundleableProperty.Bool("discovered", false);

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    discovered.Store(bundle);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    discovered.Restore(bundle);
}

public void discover() {
    discovered.Set(true);
    // Or: discovered.Toggle(); if previously false
}

public boolean isDiscovered() {
    return discovered.Get();
}
```

#### Example 2: Counter

**Before:**
```java
private int kills = 0;
private static final String KILLS = "kills";

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(KILLS, kills);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    kills = bundle.getInt(KILLS);
}

public void recordKill() {
    kills++;
}
```

**After:**
```java
private BundleableProperty.Int kills = 
    new BundleableProperty.Int("kills", 0);

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    kills.Store(bundle);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    kills.Restore(bundle);
}

public void recordKill() {
    kills.Increment();
}
```

#### Example 3: Multiple Related Fields

**Before:**
```java
private String playerName = "";
private int playerLevel = 1;
private float playerHealth = 100.0f;

private static final String PLAYER_NAME = "player_name";
private static final String PLAYER_LEVEL = "player_level";
private static final String PLAYER_HEALTH = "player_health";

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(PLAYER_NAME, playerName);
    bundle.put(PLAYER_LEVEL, playerLevel);
    bundle.put(PLAYER_HEALTH, playerHealth);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    playerName = bundle.getString(PLAYER_NAME);
    playerLevel = bundle.getInt(PLAYER_LEVEL);
    playerHealth = bundle.getFloat(PLAYER_HEALTH);
}
```

**After:**
```java
private BundleableProperty.Str playerName = 
    new BundleableProperty.Str("player_name", "");
private BundleableProperty.Int playerLevel = 
    new BundleableProperty.Int("player_level", 1);
private BundleableProperty.Float playerHealth = 
    new BundleableProperty.Float("player_health", 100.0f);

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    playerName.Store(bundle);
    playerLevel.Store(bundle);
    playerHealth.Store(bundle);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    playerName.Restore(bundle);
    playerLevel.Restore(bundle);
    playerHealth.Restore(bundle);
}
```

#### Example 4: Array Migration

**Before:**
```java
private int[] inventory = new int[0];
private static final String INVENTORY = "inventory";

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    bundle.put(INVENTORY, inventory);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    inventory = bundle.getIntArray(INVENTORY);
}
```

**After:**
```java
private BundleableProperty.IntArray inventory = 
    new BundleableProperty.IntArray("inventory", new int[0]);

@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    inventory.Store(bundle);
}

@Override
public void restoreFromBundle(Bundle bundle) {
    super.restoreFromBundle(bundle);
    inventory.Restore(bundle);
}
```

### Gradual Migration Strategy

You don't need to migrate everything at once. The old and new systems work together:

1. **New Code**: Use BundleableProperty for all new fields
2. **Bug Fixes**: When touching old code, convert those fields
3. **Major Refactors**: Convert entire classes during larger changes
4. **Leave Stable Code**: If it works and isn't changing, leave it

---

## Best Practices

### Do's

✅ **Use descriptive keys**: `"seen_player"` not `"sp"`

✅ **Set sensible defaults**: Default should be the "empty" or "initial" state

✅ **Use appropriate types**: Use `NullableInt` if you need to distinguish null from 0

✅ **Group related properties**: Keep related fields together in code

✅ **Use helper methods**: `Increment()` instead of `Set(Get() + 1)`

✅ **Document complex defaults**: Comment why a default is non-zero

```java
// Good: Clear key and obvious default
private BundleableProperty.Int health = 
    new BundleableProperty.Int("health", 100);

// Good: Nullable for truly optional values
private BundleableProperty.NullableInt completionTime = 
    new BundleableProperty.NullableInt("completion_time");

// Good: Starting value different from default
// Default: 1 (for new saves), Starting: 5 (for this instance)
private BundleableProperty.Int level = 
    new BundleableProperty.Int("level", 1, 5);
```

### Don'ts

❌ **Don't use cryptic keys**: `"h"` is less clear than `"health"` or even `"hp"`

❌ **Don't forget Store/Restore**: Property won't save without these

❌ **Don't access backing field directly**: Always use `Get()` and `Set()`

❌ **Don't duplicate keys**: Each key must be unique per object

```java
// Bad: Cryptic key
private BundleableProperty.Int hp = new BundleableProperty.Int("h", 100);

// Bad: Forgot to call Store/Restore
private BundleableProperty.Int counter = new BundleableProperty.Int("counter", 0);
@Override
public void storeInBundle(Bundle bundle) {
    super.storeInBundle(bundle);
    // Missing: counter.Store(bundle);
}

// Bad: Wrong type for boolean
private BundleableProperty.Int isActive = new BundleableProperty.Int("active", 0);
// Should be:
private BundleableProperty.Bool isActive = new BundleableProperty.Bool("active", false);
```

### Naming Conventions

```java
// Field name matches semantic meaning
private BundleableProperty.Bool isAlive = 
    new BundleableProperty.Bool("alive", true);

// Key can be abbreviated if meaning is clear
private BundleableProperty.Int experiencePoints = 
    new BundleableProperty.Int("xp", 0);

// Prefix with category for clarity
private BundleableProperty.Float combatSpeed = 
    new BundleableProperty.Float("combat_speed", 1.0f);
private BundleableProperty.Float movementSpeed = 
    new BundleableProperty.Float("movement_speed", 1.0f);
```

### Default Values

```java
// Zero/empty for counters and accumulators
private BundleableProperty.Int kills = 
    new BundleableProperty.Int("kills", 0);

// False for flags that start inactive
private BundleableProperty.Bool discovered = 
    new BundleableProperty.Bool("discovered", false);

// 1.0 for multipliers
private BundleableProperty.Float damageMultiplier = 
    new BundleableProperty.Float("damage_mult", 1.0f);

// Empty string for optional text
private BundleableProperty.Str customName = 
    new BundleableProperty.Str("custom_name", "");

// Null for truly optional objects
private BundleableProperty.Object<Item> heldItem = 
    new BundleableProperty.Object<>("held_item", null);
```

---

## Advanced Usage

### Custom Properties

If you need a type not provided, you can create your own:

```java
public static class Vector2 extends BundleableProperty<Point> {
    public Vector2(String key) {
        super(key, new Point(0, 0));
    }
    
    @Override
    protected void StoreValue(Bundle bundle) {
        bundle.put(key + "_x", value.x);
        bundle.put(key + "_y", value.y);
    }
    
    @Override
    protected void RestoreValue(Bundle bundle) {
        value = new Point(
            bundle.getInt(key + "_x"),
            bundle.getInt(key + "_y")
        );
    }
}

// Usage
private BundleableProperty.Vector2 position = 
    new BundleableProperty.Vector2("position");
```

### Conditional Saving

The default behavior only saves non-default values. You can override this:

```java
public static class AlwaysSaveInt extends BundleableProperty.Int {
    public AlwaysSaveInt(String key, int defaultValue) {
        super(key, defaultValue);
    }
    
    @Override
    protected boolean ShouldStore() {
        return true;  // Always save, even if default
    }
}
```

### Reset to Defaults

```java
public class Resettable implements Bundlable {
    private BundleableProperty.Int score = 
        new BundleableProperty.Int("score", 0);
    private BundleableProperty.Bool completed = 
        new BundleableProperty.Bool("completed", false);
    
    public void reset() {
        score.Reset();
        completed.Reset();
    }
    
    @Override
    public void storeInBundle(Bundle bundle) {
        score.Store(bundle);
        completed.Store(bundle);
    }
    
    @Override
    public void restoreFromBundle(Bundle bundle) {
        score.Restore(bundle);
        completed.Restore(bundle);
    }
}
```

### Querying Property State

```java
// Get the default value
int defaultHealth = health.GetDefault();

// Get the key (useful for debugging)
String key = health.GetKey();

// Check if a nullable has a value
if (optionalValue.HasValue()) {
    // ...
}
```

---

## Performance Considerations

### Memory

Each `BundleableProperty` is a small object wrapper. For classes with 100+ properties, this adds minimal overhead (a few KB). The trade-off for cleaner code is worth it in 99% of cases.

### Serialization Speed

The system is slightly faster than manual Bundle code because:
- Automatically skips default values (less I/O)
- Type-safe calls avoid runtime checks

### When to Use Manual Bundle

Stick with manual Bundle code for:
- Hot paths that serialize every frame (rare in Pixel Dungeon)
- Extremely large data structures (10,000+ items)
- Binary data that doesn't fit BundleableProperty types

For 99% of game objects, BundleableProperty is the right choice.