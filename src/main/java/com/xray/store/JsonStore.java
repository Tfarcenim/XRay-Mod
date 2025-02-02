package com.xray.store;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.xray.XRay;
import com.xray.utils.Reference;
import com.xray.utils.BlockData;
import net.minecraftforge.common.Tags;
import org.apache.logging.log4j.Level;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JsonStore
{
    private static final String FILE = "block_store.json";
    private static final String CONFIG_DIR = XRay.mc.gameDir + "/config/";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public boolean created = false;
    private File jsonFile;

    // This should only be initialised once
    public JsonStore()
    {
        File configDir = new File(CONFIG_DIR, Reference.MOD_ID);

        if( !configDir.exists() )
            configDir.mkdirs();

        jsonFile = new File(CONFIG_DIR + Reference.MOD_ID, FILE);
        if( !jsonFile.exists() ) {
            this.created = true;

            // Create a file with nothing inside
            this.write(new ArrayList<BlockData.SerializableBlockData>());
        }
    }
    
    public void write(ArrayList<BlockData> blockData) {
        List<BlockData.SerializableBlockData> simpleBlockData = new ArrayList<>();
        blockData.forEach( e -> simpleBlockData.add(new BlockData.SerializableBlockData(e.getEntryName(), e.getBlockName(), e.getColor(), e.isDrawing(), e.getOrder())) );

        this.write(simpleBlockData);
    }

    private void write(List<BlockData.SerializableBlockData> simpleBlockData) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(jsonFile)))
        {
            gson.toJson(simpleBlockData, writer);
        }
        catch (IOException e) {
            XRay.logger.log(Level.ERROR, "Failed to write json data to " + FILE);
        }
    }

    public List<BlockData.SerializableBlockData> read() {
        if( !jsonFile.exists() )
            return new ArrayList<>();

        try
        {
            Type type = new TypeToken<List<BlockData.SerializableBlockData>>() {}.getType();
            try (BufferedReader reader = new BufferedReader(new FileReader(jsonFile)))
            {
                return gson.fromJson(reader, type);
            }
            catch (JsonSyntaxException ex) {
                XRay.logger.log(Level.ERROR, "Failed to read json data from " + FILE);
            }
        }
        catch (IOException e)
        {
            XRay.logger.log(Level.ERROR, "Failed to read json data from " + FILE);
        }

        return new ArrayList<>();
    }

    public List<BlockData.SerializableBlockData> populateDefault() {
        List<BlockData.SerializableBlockData> oresData = new ArrayList<>();
        Tags.Blocks.ORES.getAllElements().forEach(e -> {
            if( e.getRegistryName() == null )
                return;

            oresData.add(new BlockData.SerializableBlockData(e.getNameTextComponent().getFormattedText(),
                    e.getRegistryName().toString(),
                    new Color(new Random().nextInt(255), new Random().nextInt(255), new Random().nextInt(255)),
                    false,
                    0)
            );
        });

        for (int i = 0; i < oresData.size(); i++)
            oresData.get(i).setOrder(i);

        XRay.logger.info("Setting up default syncRenderList");
        this.write(oresData);
        return oresData;
    }
}
