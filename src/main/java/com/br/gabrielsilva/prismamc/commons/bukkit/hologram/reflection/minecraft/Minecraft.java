package com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.minecraft;

import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.ConstructorResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.FieldResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.MethodResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.minecraft.NMSClassResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.resolver.minecraft.OBCClassResolver;
import com.br.gabrielsilva.prismamc.commons.bukkit.hologram.reflection.util.AccessUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to access minecraft/bukkit specific objects
 */
public class Minecraft {
	static final Pattern NUMERIC_VERSION_PATTERN = Pattern.compile("v([0-9])_([0-9]*)_R([0-9])");

	public static final Version VERSION;

	private static NMSClassResolver nmsClassResolver = new NMSClassResolver();
	private static OBCClassResolver obcClassResolver = new OBCClassResolver();
	private static Class<?> NmsEntity;
	private static Class<?> CraftEntity;

	static {
		System.out.println("[ReflectionHelper] I am loaded from package " + Minecraft.class.getPackage().getName());
		VERSION = Version.getVersion();
		System.out.println("[ReflectionHelper] Version is " + VERSION);

		try {
			NmsEntity = nmsClassResolver.resolve("Entity");
			CraftEntity = obcClassResolver.resolve("entity.CraftEntity");
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return the current NMS/OBC version (format <code>&lt;version&gt;.</code>
	 */
	public static String getVersion() {
		return VERSION.name() + ".";
	}

	public static Object getHandle(Object object) throws ReflectiveOperationException {
		Method method;
		try {
			method = AccessUtil.setAccessible(object.getClass().getDeclaredMethod("getHandle"));
		} catch (ReflectiveOperationException e) {
			method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle"));
		}
		return method.invoke(object);
	}

	public static Entity getBukkitEntity(Object object) throws ReflectiveOperationException {
		Method method;
		try {
			method = AccessUtil.setAccessible(NmsEntity.getDeclaredMethod("getBukkitEntity"));
		} catch (ReflectiveOperationException e) {
			method = AccessUtil.setAccessible(CraftEntity.getDeclaredMethod("getHandle"));
		}
		return (Entity) method.invoke(object);
	}

	public static Object getHandleSilent(Object object) {
		try {
			return getHandle(object);
		} catch (Exception e) {
		}
		return null;
	}

	public enum Version {
		UNKNOWN(-1) {
			@Override
			public boolean matchesPackageName(String packageName) {
				return false;
			}
		},

		v1_7_R1(10701),
		v1_7_R2(10702),
		v1_7_R3(10703),
		v1_7_R4(10704),

		v1_8_R1(10801),
		v1_8_R2(10802),
		v1_8_R3(10803),
		//Does this even exists?
		v1_8_R4(10804),

		v1_9_R1(10901),
		v1_9_R2(10902),

		v1_10_R1(11001),

		v1_11_R1(11101),

		v1_12_R1(11201),

		v1_13_R1(11301),
		v1_13_R2(11302),

		v1_14_R1(11401);

		private int version;

		Version(int version) {
			this.version = version;
		}

		/**
		 * @return the version-number
		 */
		public int version() {
			return version;
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is older than the specified version
		 */
		public boolean olderThan(Version version) {
			return version() < version.version();
		}

		/**
		 * @param version the version to check
		 * @return <code>true</code> if this version is newer than the specified version
		 */
		public boolean newerThan(Version version) {
			return version() >= version.version();
		}

		/**
		 * @param oldVersion The older version to check
		 * @param newVersion The newer version to check
		 * @return <code>true</code> if this version is newer than the oldVersion and older that the newVersion
		 */
		public boolean inRange(Version oldVersion, Version newVersion) {
			return newerThan(oldVersion) && olderThan(newVersion);
		}

		public boolean matchesPackageName(String packageName) {
			return packageName.toLowerCase().contains(name().toLowerCase());
		}

		public static Version getVersion() {
			String name = Bukkit.getServer().getClass().getPackage().getName();
			String versionPackage = name.substring(name.lastIndexOf('.') + 1) + ".";
			for (Version version : values()) {
				if (version.matchesPackageName(versionPackage)) {
					return version;
				}
			}
			System.err.println("[ReflectionHelper] Failed to find version enum for '" + name + "'/'" + versionPackage + "'");

			System.out.println("[ReflectionHelper] Generating dynamic constant...");
			Matcher matcher = NUMERIC_VERSION_PATTERN.matcher(versionPackage);
			while (matcher.find()) {
				if (matcher.groupCount() < 3) {
					continue;
				}

				String majorString = matcher.group(1);
				String minorString = matcher.group(2);
				if (minorString.length() == 1) {
					minorString = "0" + minorString;
				}
				String patchString = matcher.group(3);
				if (patchString.length() == 1) {
					patchString = "0" + patchString;
				}

				String numVersionString = majorString + minorString + patchString;
				int numVersion = Integer.parseInt(numVersionString);
				String packge = versionPackage.substring(0, versionPackage.length() - 1);

				return UNKNOWN;
			}
			return UNKNOWN;
		}
	}
}
