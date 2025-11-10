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

package com.shatteredpixel.shatteredpixeldungeon.utils;

import com.watabou.utils.Bundle;
import com.watabou.utils.Bundlable;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Type-safe wrappers for Bundle serialization.
 */
public abstract class BundleableProperty<T> {

	protected final String key;
	protected final T defaultValue;
	protected T value;

	public BundleableProperty(String key, T defaultValue) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	public BundleableProperty(String key, T defaultValue, T startingValue) {
		this.key = key;
		this.defaultValue = defaultValue;
		this.value = startingValue;
	}

	public T Get() {
		return value;
	}

	public T GetDefault() {
		return defaultValue;
	}

	public void Set(T value) {
		this.value = value;
	}

	public void Reset() {
		this.value = this.defaultValue;
	}

	public String GetKey() {
		return key;
	}

	public void Store(Bundle bundle) {
		if (ShouldStore()) {
			StoreValue(bundle);
		}
	}

	public void Restore(Bundle bundle) {
		if (bundle.contains(key)) {
			RestoreValue(bundle);
		} else {
			value = defaultValue;
		}
	}

	protected boolean ShouldStore() {
		if (value == null && defaultValue == null) return false;
		if (value == null || defaultValue == null) return true;
		return !value.equals(defaultValue);
	}

	// Subclasses just need to use bundle.put(key, value) directly
	protected abstract void StoreValue(Bundle bundle);
	protected abstract void RestoreValue(Bundle bundle);

	// ==================== PRIMITIVE TYPES ====================

	public static class Int extends BundleableProperty<Integer> {

		public Int(String key) {
			this(key, 0);
		}

		public Int(String key, int defaultValue) {
			super(key, defaultValue);
		}

		public Int(String key, int defaultValue, int startingValue) {
			super(key, defaultValue, startingValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getInt(key);
		}

		public void Increment() { value++; }
		public void Decrement() { value--; }
		public void Add(int amount) { value += amount; }
		public void Subtract(int amount) { value -= amount; }
	}

	public static class Long extends BundleableProperty<java.lang.Long> {

		public Long(String key) {
			this(key, 0L);
		}

		public Long(String key, long defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getLong(key);
		}

		public void Increment() { value++; }
		public void Decrement() { value--; }
		public void Add(long amount) { value += amount; }
	}

	public static class Float extends BundleableProperty<java.lang.Float> {

		public Float(String key) {
			this(key, 0f);
		}

		public Float(String key, float defaultValue) {
			super(key, defaultValue);
		}

		public void Increment() { value += 1.0f; }
		public void Decrement() { value -= 1.0f; }

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getFloat(key);
		}

		public void Add(float amount) { value += amount; }
		public void Subtract(float amount) { value -= amount; }
	}

	public static class Bool extends BundleableProperty<Boolean> {

		public Bool(String key) {
			this(key, false);
		}

		public Bool(String key, boolean defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getBoolean(key);
		}

		public void Toggle() { value = !value; }
	}

	public static class Str extends BundleableProperty<String> {

		public Str(String key) {
			this(key, "");
		}

		public Str(String key, String defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getString(key);
		}
	}

	public static class Clazz extends BundleableProperty<Class> {

		public Clazz(String key) {
			this(key, null);
		}

		public Clazz(String key, Class defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getClass(key);
		}
	}

	// ==================== ENUM TYPE ====================

	public static class Enum<E extends java.lang.Enum<E>> extends BundleableProperty<E> {

		private final Class<E> enumClass;

		public Enum(String key, E defaultValue) {
			super(key, defaultValue);
			this.enumClass = (Class<E>) defaultValue.getClass();
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getEnum(key, enumClass);
		}
	}

	// ==================== BUNDLABLE OBJECT ====================

	public static class Object<T extends Bundlable> extends BundleableProperty<T> {

		public Object(String key) {
			this(key, null);
		}

		public Object(String key, T defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = (T) bundle.get(key);
		}
	}

	// ==================== NESTED BUNDLE ====================

	public static class NestedBundle extends BundleableProperty<Bundle> {

		public NestedBundle(String key) {
			this(key, new Bundle());
		}

		public NestedBundle(String key, Bundle defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getBundle(key);
		}
	}

	// ==================== ARRAY TYPES ====================

	public static class IntArray extends BundleableProperty<int[]> {

		public IntArray(String key) {
			this(key, new int[0]);
		}

		public IntArray(String key, int[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getIntArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class LongArray extends BundleableProperty<long[]> {

		public LongArray(String key) {
			this(key, new long[0]);
		}

		public LongArray(String key, long[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getLongArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class FloatArray extends BundleableProperty<float[]> {

		public FloatArray(String key) {
			this(key, new float[0]);
		}

		public FloatArray(String key, float[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getFloatArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class BoolArray extends BundleableProperty<boolean[]> {

		public BoolArray(String key) {
			this(key, new boolean[0]);
		}

		public BoolArray(String key, boolean[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getBooleanArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class StringArray extends BundleableProperty<String[]> {

		public StringArray(String key) {
			this(key, new String[0]);
		}

		public StringArray(String key, String[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getStringArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class ClassArray extends BundleableProperty<Class[]> {

		public ClassArray(String key) {
			this(key, new Class[0]);
		}

		public ClassArray(String key, Class[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getClassArray(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	public static class BundleArray extends BundleableProperty<Bundle[]> {

		public BundleArray(String key) {
			this(key, new Bundle[0]);
		}

		public BundleArray(String key, Bundle[] defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key + "_count", value.length);
			for (int i = 0; i < value.length; i++) {
				bundle.put(key + "_" + i, value[i]);
			}
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			int count = bundle.getInt(key + "_count");
			value = new Bundle[count];
			for (int i = 0; i < count; i++) {
				value[i] = bundle.getBundle(key + "_" + i);
			}
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && value.length > 0;
		}
	}

	// ==================== COLLECTION TYPE ====================

	public static class BundlableCollection<T extends Bundlable> extends BundleableProperty<Collection<T>> {

		public BundlableCollection(String key) {
			this(key, new ArrayList<>());
		}

		public BundlableCollection(String key, Collection<T> defaultValue) {
			super(key, defaultValue);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = (Collection<T>) bundle.getCollection(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null && !value.isEmpty();
		}

		public void Add(T item) {
			if (value == null) value = new ArrayList<>();
			value.add(item);
		}

		public void Remove(T item) {
			if (value != null) value.remove(item);
		}

		public void Clear() {
			if (value != null) value.clear();
		}
	}

	// ==================== NULLABLE/OPTIONAL TYPES ====================

	public static class NullableInt extends BundleableProperty<Integer> {

		public NullableInt(String key) {
			super(key, null);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getInt(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null;
		}

		public boolean HasValue() { return value != null; }
		public void Increment() { if (value != null) value++; }
		public void Decrement() { if (value != null) value--; }
		public void Add(int amount) { if (value != null) value += amount; }
	}

	public static class NullableLong extends BundleableProperty<java.lang.Long> {

		public NullableLong(String key) {
			super(key, null);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getLong(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null;
		}

		public boolean HasValue() { return value != null; }
		public void Increment() { if (value != null) value++; }
		public void Decrement() { if (value != null) value--; }
	}

	public static class NullableFloat extends BundleableProperty<java.lang.Float> {

		public NullableFloat(String key) {
			super(key, null);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getFloat(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null;
		}

		public boolean HasValue() { return value != null; }
		public void Add(float amount) { if (value != null) value += amount; }
		public void Multiply(float factor) { if (value != null) value *= factor; }
	}

	public static class NullableBool extends BundleableProperty<Boolean> {

		public NullableBool(String key) {
			super(key, null);
		}

		@Override
		protected void StoreValue(Bundle bundle) {
			bundle.put(key, value);
		}

		@Override
		protected void RestoreValue(Bundle bundle) {
			value = bundle.getBoolean(key);
		}

		@Override
		protected boolean ShouldStore() {
			return value != null;
		}

		public boolean HasValue() { return value != null; }
		public void Toggle() { if (value != null) value = !value; }
	}
}