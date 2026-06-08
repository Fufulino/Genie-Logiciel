package com.charitymap.service;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.charitymap.model.CharityMap;

/**
 * Saves and loads a whole {@link CharityMap} to and from a binary file,
 * using Java serialization. The specification explicitly requires the
 * save format to be a binary file.
 *
 * @author CharityMap team
 */
public class MapIO {
    /**
     * Saves a map to a binary file.
     *
     * @param map  the map to save
     * @param path the destination file path
     * @throws IOException when the file cannot be written
     */
    public void save(CharityMap map, String path) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new FileOutputStream(path))) {
            out.writeObject(map);
        }
    }

    /**
     * Loads a map from a binary file.
     *
     * @param path the source file path
     * @return the loaded map, fully recomputed and ready to use
     * @throws IOException            when the file cannot be read
     * @throws ClassNotFoundException when the file content does not
     *                                match the expected classes
     */
    public CharityMap load(String path)
            throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(
                new FileInputStream(path))) {
            Object content = in.readObject();
            if (!(content instanceof CharityMap)) {
                throw new IOException(
                        "This file does not contain a charity map.");
            }
            CharityMap map = (CharityMap) content;
            // The Voronoi structures are transient, so rebuild them.
            map.recompute();
            return map;
        }
    }
}
