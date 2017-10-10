package ru.mail.polis.dao;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Created by germanium on 10.10.17.
 */
public interface MyDAO {
    @NotNull
    byte[] get(@NotNull String key) throws NoSuchElementException,IllegalArgumentException,IOException;

    @NotNull
    void upsert(@NotNull String key, @NotNull byte[] value) throws IllegalArgumentException,IOException;


    @NotNull
    void delete(@NotNull String key) throws IllegalArgumentException,IOException;

}
