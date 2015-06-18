# CostOnCommand
Minecraft plugin source to add costs to commands

This minecraft plugin adds costs to commands that can differ by a player's group. /back from essentials
could default cost 100$, $50 for vip1, and $25 for vip2.

This is my first plugin so there may be a bug or two.. but the core idea is working. There is no jar on bukkit/oher sites.
I don't intend to update the plugin past 1.7.10 as this plugin is for the server that I play on, direwolf.goreacraft.com

This plugin is for 1.7.10 and requires Vault to use.

To work with permissions, after you add a group to the config, you must add in the permission "costoncommand.group" to the matching group

For Example-

If I have this in config.yml

Commands:
  back:
    default: 100
	vip1: 50
	vip2: 25

Then in the group management plug-in (PermissionsEx, Group Manager, etc.), you have to add costoncommand.vip1 to the VIP group and
costoncommand.vip2 to the VIP 2 group.
	
	
Here's a list of things I want to implement
- Check if the command could fire (don't want a player charged for a command they couldn't even perform)
- Add an option for Ops (currently they bypass all costs)
- Better handling for commands with multiple arguments (the plugin only takes the first word, anything afterward is discarded)
