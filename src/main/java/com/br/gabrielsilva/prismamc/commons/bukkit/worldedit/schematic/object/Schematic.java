package com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.object;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.scheduler.BukkitTask;

import com.br.gabrielsilva.prismamc.commons.bukkit.BukkitMain;
import com.br.gabrielsilva.prismamc.commons.bukkit.custom.events.SchematicSpawnedEvent;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.utils.Region;
import com.br.gabrielsilva.prismamc.commons.bukkit.worldedit.schematic.utils.Vector;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.IBlockData;
import net.minecraft.server.v1_8_R3.NBTCompressedStreamTools;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;

public class Schematic {

    private final List<SchematicBlock> blockList = new ArrayList<>();
    private final List<SchematicLocation> locationList = new ArrayList<>();
    
    private final Region region;

    private BukkitTask task;

    public Schematic(Region region) {
    	this.region = region;

        World world = region.getWorld();
        Vector min = region.getMinLocation();
        Vector max = region.getMaxLocation();

        //blocks =========================================================================================
        for (int x = min.getBlockX(); x <= max.getBlockX(); x++) {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++) {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Vector location = new Vector(x, y, z).subtract(min);
                    SchematicBlock schematicBlock = new SchematicBlock(location, block.getTypeId(), block.getData());
                    blockList.add(schematicBlock);
                }
            }
        }
    }

    public Schematic(String nome, File file) throws IOException {
    	FileInputStream stream = new FileInputStream(file);

        NBTTagCompound nbt = NBTCompressedStreamTools.a(stream);

        stream.close();

        short width = nbt.getShort("Width");
        short height = nbt.getShort("Height");
        short length = nbt.getShort("Length");

        int offsetX = nbt.getInt("WEOffsetX");
        int offsetY = nbt.getInt("WEOffsetY");
        int offsetZ = nbt.getInt("WEOffsetZ");
        Vector offset = new Vector(offsetX, offsetY, offsetZ);

        int originX = nbt.getInt("WEOriginX");
        int originY = nbt.getInt("WEOriginY");
        int originZ = nbt.getInt("WEOriginZ");
        Vector origin = new Vector(originX, originY, originZ);

        region = new Region(origin, offset, width, height, length);

        //blocks =========================================================================================
        byte[] blockId = nbt.getByteArray("Blocks");
        byte[] blockData = nbt.getByteArray("Data");

        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length];

        if (nbt.hasKey("AddBlocks")) {
            addId = nbt.getByteArray("AddBlocks");
        }

        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) {
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                     int index = y * width * length + z * width + x;
                     SchematicBlock block = new SchematicBlock(new Vector(x, y, z), blocks[index], blockData[index]);
                     blockList.add(block);
                }
            }
        }
        
        //locations =========================================================================================
        NBTTagList locations = nbt.getList("Locations", 8);
        for (int i = 0; i < locations.size(); i++) {
            SchematicLocation location = new SchematicLocation(locations.getString(i));
            locationList.add(location);
        }
    }

    public Region getRegion() {
        return region;
    }

    public void addLocation(SchematicLocation location) {
        locationList.add(location);
    }

    public List<Location> getConvertedLocation(String key, Location pasteLocation) {
        List<Location> result = new ArrayList<>();
        for (SchematicLocation locations : locationList) {
            if (locations.getKey().equals(key)) {
                Vector vector = locations.getLocation().clone().add(new Vector(pasteLocation).add(region.getOffset()).subtract(region.getMinLocation()));
                result.add(vector.toLocation(pasteLocation.getWorld()));
            }
        }
        return result;
    }

    public File save(File file) throws IOException {
        NBTTagCompound nbt = new NBTTagCompound();

        short width = (short) region.getWidth();
        short height = (short) region.getHeight();
        short length = (short) region.getLength();

        nbt.setShort("Width", width);
        nbt.setShort("Height", height);
        nbt.setShort("Length", length);

        nbt.setInt("WEOffsetX", region.getOffset().getBlockX());
        nbt.setInt("WEOffsetY", region.getOffset().getBlockY());
        nbt.setInt("WEOffsetZ", region.getOffset().getBlockZ());

        nbt.setInt("WEOriginX", region.getMinLocation().getBlockX());
        nbt.setInt("WEOriginY", region.getMinLocation().getBlockY());
        nbt.setInt("WEOriginZ", region.getMinLocation().getBlockZ());

        //blocks =========================================================================================
        byte[] blocks = new byte[width * height * length];
        byte[] blockData = new byte[width * height * length];
        byte[] addBlocks = null;
        for (SchematicBlock block : blockList) {
            Vector location = block.getLocation();
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            int index = y * width * length + z * width + x;

            if (block.getID() > 255) {
                if (addBlocks == null) {
                    addBlocks = new byte[(blocks.length >> 1) + 1];
                }
                addBlocks[index >> 1] = (byte) (((index & 1) == 0)
                        ? addBlocks[index >> 1] & 0xF0 | (block.getID() >> 8) & 0xF
                        : addBlocks[index >> 1] & 0xF | ((block.getID() >> 8) & 0xF) << 4);
            }

            blocks[index] = (byte) block.getID();
            blockData[index] = block.getData();

        }

        nbt.setByteArray("Blocks", blocks);
        nbt.setByteArray("Data", blockData);
        if (addBlocks != null) {
            nbt.setByteArray("AddBlocks", addBlocks);
        }

        //locations =========================================================================================
        NBTTagList locations = new NBTTagList();
        for (SchematicLocation location : locationList) {
            locations.add(new NBTTagString(location.toString()));
        }
        
        if (locations != null) {
            nbt.set("Locations", locations);
        }

        FileOutputStream stream = new FileOutputStream(file);
        NBTCompressedStreamTools.a(nbt, stream);
        stream.close();

        return file;
    }

    public void paste(String nome, Location location, int bpt) {
    	Vector finalLocation = new Vector(location).add(region.getOffset());
        World world = location.getWorld();
        
        pasteBlocks(nome, world, finalLocation, bpt, false);
    }
    
    public void paste(String nome, Location location, int bpt, boolean force) {
    	Vector finalLocation = new Vector(location).add(region.getOffset());
        World world = location.getWorld();
        
        pasteBlocks(nome, world, finalLocation, bpt, force);
    }
    
    //blocks =========================================================================================
    private void pasteBlocks(String nome, World world, Vector pasteLocation, int bpt, boolean force) {
    	if (force) {
    		Iterator<SchematicBlock> iterator = blockList.iterator();

    		while (iterator.hasNext()) {
    			SchematicBlock schemBlock = iterator.next();
                Vector finalLocation = schemBlock.getLocation().clone().add(pasteLocation);
                setAsyncBlock(world, new Location(world, finalLocation.getBlockX(), finalLocation.getBlockY(), finalLocation.getBlockZ()),
                		schemBlock.getID());
                iterator.remove();
    		}
            return;
    	}
    	
        task = Bukkit.getScheduler().runTaskTimer(BukkitMain.getInstance(), () -> {
        	
        	Iterator<SchematicBlock> iterator = blockList.iterator();

            for (int i = 0; i < bpt; i++) {
            	if (!iterator.hasNext()) {
            		Bukkit.getServer().getPluginManager().callEvent(new SchematicSpawnedEvent(nome, force, this));
                    task.cancel();
                    return;
                }

                SchematicBlock schemBlock = iterator.next();
                Vector finalLocation = schemBlock.getLocation().clone().add(pasteLocation);
                setAsyncBlock(world, new Location(world, finalLocation.getBlockX(), finalLocation.getBlockY(), finalLocation.getBlockZ()),
                		schemBlock.getID());
                iterator.remove();
            }
        }, 0L, 1L);
    }
    
	public void setAsyncBlock(World world, Location location, int blockId) {
		setAsyncBlock(world, location, blockId, (byte) 0);
	}
	
	public void setAsyncBlock(World world, Location location, int blockId, byte data) {
		setAsyncBlock(world, location.getBlockX(), location.getBlockY(), location.getBlockZ(), blockId, data);
	}
	
	public void setAsyncBlock(World world, int x, int y, int z, int blockId, byte data) {
		net.minecraft.server.v1_8_R3.World w = ((CraftWorld) world).getHandle();
		net.minecraft.server.v1_8_R3.Chunk chunk = w.getChunkAt(x >> 4, z >> 4);
		BlockPosition bp = new BlockPosition(x, y, z);
		int i = blockId + (data << 12);
		IBlockData ibd = net.minecraft.server.v1_8_R3.Block.getByCombinedId(i);
		chunk.a(bp, ibd);
		w.notify(bp);
	}
}