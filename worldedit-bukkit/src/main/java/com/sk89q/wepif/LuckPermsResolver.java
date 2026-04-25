/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.wepif;

import com.sk89q.util.yaml.YAMLProcessor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Locale;
import java.util.UUID;

public class LuckPermsResolver implements PermissionsResolver {

    private static LuckPerms luckPerms = null;

    public static PermissionsResolver factory(Server server, YAMLProcessor config) {
        if (server.getPluginManager().getPlugin("LuckPerms") == null) {
            return null;
        }
        RegisteredServiceProvider<LuckPerms> rsp = server.getServicesManager().getRegistration(LuckPerms.class);
        if (rsp == null) {
            return null;
        }
        luckPerms = rsp.getProvider();
        if (luckPerms == null) {
            return null;
        }

        return new LuckPermsResolver(server);
    }

    private final Server server;

    public LuckPermsResolver(Server server) {
        this.server = server;
    }

    @Override
    public void load() {
    }

    @Override
    public String getDetectionMessage() {
        return "LuckPerms detected! Using LuckPerms for permissions";
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasPermission(String name, String permission) {
        return hasPermission(server.getOfflinePlayer(name), permission);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(worldName, server.getOfflinePlayer(name), permission);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean inGroup(String player, String group) {
        return inGroup(server.getOfflinePlayer(player), group);
    }

    @Override
    @SuppressWarnings("deprecation")
    public String[] getGroups(String player) {
        return getGroups(server.getOfflinePlayer(player));
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return hasPermission(resolveWorld(player), player, permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        User user = loadUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        return user.getCachedData().getPermissionData(queryOptions(worldName))
                .checkPermission(permission).asBoolean();
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        User user = loadUser(player.getUniqueId());
        if (user == null) {
            return false;
        }
        return user.getInheritedGroups(queryOptions(resolveWorld(player))).stream()
                .anyMatch(g -> g.getName().equalsIgnoreCase(group));
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        User user = loadUser(player.getUniqueId());
        if (user == null) {
            return new String[0];
        }
        return user.getInheritedGroups(queryOptions(resolveWorld(player))).stream()
                .map(Group::getName)
                .toArray(String[]::new);
    }

    private static String resolveWorld(OfflinePlayer player) {
        if (player.isOnline() && player.getPlayer() != null) {
            return player.getPlayer().getWorld().getName();
        }
        return null;
    }

    private static QueryOptions queryOptions(String worldName) {
        if (worldName == null) {
            return luckPerms.getContextManager().getStaticQueryOptions();
        }
        return QueryOptions.contextual(ImmutableContextSet.of("world", worldName.toLowerCase(Locale.ROOT)));
    }

    private static User loadUser(UUID uuid) {
        UserManager userManager = luckPerms.getUserManager();
        User user = userManager.getUser(uuid);
        if (user != null) {
            return user;
        }
        try {
            return userManager.loadUser(uuid).join();
        } catch (Exception e) {
            return null;
        }
    }

}
