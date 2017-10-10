package ru.mail.polis.dao;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.NoSuchElementException;

/**
 * Created by germanium on 10.10.17.
 */
public class MyFileDAO implements MyDAO{

    @NotNull
    private final File dir;

    public MyFileDAO(@NotNull final File dir){
        this.dir=dir;
    }

    @NotNull
    private File getFile(@NotNull final String key){
        if(key.isEmpty()) {
            throw new IllegalArgumentException("Empty key");
        }
        return new File(dir,key);
    }

    @NotNull
    @Override
    public byte[] get(@NotNull final String key) throws NoSuchElementException, IllegalArgumentException, IOException {

        final File file = getFile(key);
        if(!file.exists()){
            throw new NoSuchElementException("File doesn't exist");
        }

        try(InputStream is = new FileInputStream(file)){
            final byte[] value = new byte[(int) file.length()];
            if(is.read(value) != value.length){
                throw new IOException("CAn't read file in one go!");
            }
            return value;
        }
    }

    @NotNull
    @Override
    public void upsert(@NotNull final String key, @NotNull final byte[] value) throws IllegalArgumentException, IOException {
        try(OutputStream os = new FileOutputStream(getFile(key))){
            os.write(value);
        }
    }

    @NotNull
    @Override
    public void delete(@NotNull final String key) throws IllegalArgumentException, IOException {
            getFile(key).delete();

    }
}
