package org.dolicoli.golfz;

import com.google.appengine.api.datastore.Entity;

public class Common {
	public static final int MAX_PLAYER_COUNT = 6;

	public static int getIntProperty(Entity entity, String key) {
		return getIntProperty(entity, key, 0);
	}

	public static int getIntProperty(Entity entity, String key, int defaultValue) {
		if (!entity.hasProperty(key)) {
			return defaultValue;
		}
		return ((Long) entity.getProperty(key)).intValue();
	}

	public static long getLongProperty(Entity entity, String key) {
		return getLongProperty(entity, key, 0L);
	}

	public static long getLongProperty(Entity entity, String key,
			long defaultValue) {
		if (!entity.hasProperty(key)) {
			return defaultValue;
		}
		return ((Long) entity.getProperty(key)).longValue();
	}
}
