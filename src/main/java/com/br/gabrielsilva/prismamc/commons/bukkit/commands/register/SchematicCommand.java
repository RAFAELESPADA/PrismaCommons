package com.br.gabrielsilva.prismamc.commons.bukkit.commands.register;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.br.gabrielsilva.prismamc.commons.core.utils.system.MachineOS;
import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.commands.BukkitCommandSender;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.object.Schematic;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.object.SchematicLocation;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.utils.Positions;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.utils.Region;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.utils.Vector;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandClass;
import com.br.gabrielsilva.prismamc.commons.core.command.CommandFramework.Command;
import com.br.gabrielsilva.prismamc.commons.core.group.Groups;
import com.br.gabrielsilva.prismamc.commons.core.utils.string.StringUtils;

public class SchematicCommand implements CommandClass {

    public static final Map<Player, Positions> POSITIONS = new HashMap<>();

	@Command(name = "schematic", aliases= {"schem"}, groupsToUse= {Groups.DONO}, runAsync=true)
	public void reports(BukkitCommandSender commandSender, String label, String[] args) {
		if (!commandSender.isPlayer()) {
			return;
		}
        Player player = commandSender.getPlayer();

        if (args.length == 3 && args[0].equalsIgnoreCase("paste")) {
            File file = new File(MachineOS.getDiretorio() + MachineOS.getSeparador() + "schematics", 
            		args[1].replace(".schematic", "") + ".schematic");
            if (!file.exists()) {
                player.sendMessage("§6§lSCHEMATIC §fSchematic não encontrada");
                return;
            }

            if (!StringUtils.isInteger(args[2])) {
            	player.sendMessage("§6§lSCHEMATIC §cComando incorreto.");
            	return;
            }
            int blocksPerTick = Integer.valueOf(args[2]);
            player.sendMessage("§6§lSCHEMATIC §fColando schematic...");
            long started = System.currentTimeMillis();
            try {
                Schematic schematic = new Schematic(args[1], file);
                schematic.paste(args[1], player.getLocation(), blocksPerTick);
            } catch (IOException ex) {
                player.sendMessage("§6§lSCHEMATIC §fOcorreu um erro ao tentar copiar...");
                ex.printStackTrace();
            }

        } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            StringBuilder builder = new StringBuilder();
            
            File dir = new File(MachineOS.getDiretorio() + MachineOS.getSeparador() + "schematics");
            
            for (File files : dir.listFiles()) {
                if (files.getName().endsWith(".schematic")) {
                    builder.append(", ").append(files.getName());
                }
            }
            player.sendMessage("§6§lSCHEMATIC §fSchematics: " + builder.toString().replaceFirst(", ", ""));

        } else if (args.length == 1 && (args[0].equalsIgnoreCase("pos1") || args[0].equalsIgnoreCase("pos2"))) {
            Location location = player.getLocation();

            Positions positions = POSITIONS.get(player);
            if (positions == null) {
                positions = new Positions();
                POSITIONS.put(player, positions);
            }

            Vector position = new Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());

            int index = Integer.parseInt(args[0].replace("pos", ""));
            if (index == 2) {
                positions.setPosition2(position);
            } else {
                positions.setPosition1(position);
            }

            player.sendMessage("§6§lSCHEMATIC §fPosição " + index + " setada");

        } else if (args.length == 2 && args[0].equalsIgnoreCase("save")) {
            Positions positions = POSITIONS.get(player);
            if (positions == null || positions.isIncomplete()) {
                player.sendMessage("§6§lSCHEMATIC §fVocê precisa setar as 2 posições.");
                return;
            }

            File file = new File(MachineOS.getDiretorio() + MachineOS.getSeparador() + "schematics", args[1] + ".schematic");
            if (file.exists()) {
                player.sendMessage("§6§lSCHEMATIC §fSchematic ja existe.");
                return;
            }

            Vector origin = new Vector(player.getLocation());
            Region region = new Region(player.getWorld(), origin, positions.getPosition1(), positions.getPosition2());
            Schematic schematic = new Schematic(region.getWithoutAir());

            for (String data : BukkitMain.getInstance().getConfig().getStringList("Locations")) {
                SchematicLocation location = new SchematicLocation(data);
                if (region.isInside(location.getLocation())) {
                    schematic.addLocation(location);
                }
            }

            try {
                file.createNewFile();
                schematic.save(file);
                player.sendMessage("§6§lSCHEMATIC §fSchematic " + args[1] + " salva. (" + schematic.getRegion().getSize() + " blocos)");
            } catch (IOException ex) {
                player.sendMessage("§6§lSCHEMATIC §fSchematic não foi salva.");
                ex.printStackTrace();
            }

        } else {
            player.sendMessage("§6§lSCHEMATIC §fUsage:");
            player.sendMessage("§6§lSCHEMATIC §f/" + label + " pos1 | pos2");
            player.sendMessage("§6§lSCHEMATIC §f/" + label + " save <name>");
            player.sendMessage("§6§lSCHEMATIC §f/" + label + " paste <schematic> <Blocos por tick>");
            player.sendMessage("§6§lSCHEMATIC §f/" + label + " list");
        }
    }
}