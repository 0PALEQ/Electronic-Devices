package com.cookiecraftmods.edm.media;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

public final class MediaPermissions {
	public static final String MANAGE_OTHERS = "electronic_device_mod.media.manage.others";
	public static final String ASSIGN_URL = "electronic_device_mod.media.url";

	private MediaPermissions() {
	}

	public static boolean canManageOthers(ServerPlayerEntity player) {
		return Permissions.check(player, MANAGE_OTHERS, 2);
	}

	public static boolean canAssignUrl(ServerPlayerEntity player) {
		return Permissions.check(player, ASSIGN_URL, 0);
	}
}
