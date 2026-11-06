package com.cookiecraftmods.edm.media;

import com.cookiecraftmods.edm.ElectronicDeviceMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.events.PermissionGatherEvent;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

public final class MediaPermissions {
	public static final String MANAGE_OTHERS = "electronic_device_mod.media.manage.others";
	public static final String ASSIGN_URL = "electronic_device_mod.media.url";
	private static final PermissionNode<Boolean> MANAGE_OTHERS_NODE = new PermissionNode<>(
		ElectronicDeviceMod.MOD_ID, "media.manage.others", PermissionTypes.BOOLEAN,
		(player, playerId, contexts) -> player != null && player.hasPermissionLevel(2)
	);
	private static final PermissionNode<Boolean> ASSIGN_URL_NODE = new PermissionNode<>(
		ElectronicDeviceMod.MOD_ID, "media.url", PermissionTypes.BOOLEAN,
		(player, playerId, contexts) -> true
	);

	private MediaPermissions() {
	}

	public static boolean canManageOthers(ServerPlayerEntity player) {
		return PermissionAPI.getPermission(player, MANAGE_OTHERS_NODE);
	}

	public static boolean canAssignUrl(ServerPlayerEntity player) {
		return PermissionAPI.getPermission(player, ASSIGN_URL_NODE);
	}

	public static void registerNodes(PermissionGatherEvent.Nodes event) {
		event.addNodes(MANAGE_OTHERS_NODE, ASSIGN_URL_NODE);
	}

	public static boolean canManageWhitelist(ServerPlayerEntity player) {
		return player.hasPermissionLevel(2);
	}

	public static void checkSource(ServerPlayerEntity player, String url) {
		MediaConfig config = MediaConfig.get();
		config.mediaSources.check(url, config.maxMediaUrlLength, config.mediaSources.linkWhitelistMode || canManageWhitelist(player));
	}
}
