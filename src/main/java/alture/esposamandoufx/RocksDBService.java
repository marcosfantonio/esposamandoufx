package alture.esposamandoufx;

import org.rocksdb.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RocksDBService {
    private static RocksDBService instance;
    private RocksDB database;
    private Options options;
    private String dbPath;
    private boolean isOpen;

    static {
        RocksDB.loadLibrary();
    }

    private RocksDBService() {
        this.database = null;
        this.options = null;
        this.isOpen = false;
    }

    public static synchronized RocksDBService getInstance() {
        if (instance == null) {
            instance = new RocksDBService();
        }
        return instance;
    }

    public void open(String dbPath) {
        if (isOpen) {
            throw new RuntimeException("Database is already open");
        }

        try {
            File dbDir = new File(dbPath);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            this.options = new Options();
            this.options.setCreateIfMissing(true);
            this.options.setErrorIfExists(false);

            this.database = RocksDB.open(options, dbPath);
            this.dbPath = dbPath;
            this.isOpen = true;

            System.out.println("Database opened at: " + dbPath);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to open RocksDB: " + e.getMessage(), e);
        }
    }

    public void close() {
        if (isOpen) {
            if (database != null) {
                database.close();
                database = null;
            }

            if (options != null) {
                options.close();
                options = null;
            }

            isOpen = false;
            System.out.println("Database closed");
        }
    }

    public void put(String key, String value) {
        checkIfOpen();
        try {
            database.put(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to put data: " + e.getMessage(), e);
        }
    }

    public String get(String key) {
        checkIfOpen();
        try {
            byte[] valueBytes = database.get(key.getBytes(StandardCharsets.UTF_8));
            if (valueBytes == null) {
                return null;
            }
            return new String(valueBytes, StandardCharsets.UTF_8);
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to get data: " + e.getMessage(), e);
        }
    }

    public void delete(String key) {
        checkIfOpen();
        try {
            database.delete(key.getBytes(StandardCharsets.UTF_8));
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to delete data: " + e.getMessage(), e);
        }
    }

    public boolean containsKey(String key) {
        checkIfOpen();
        try {
            byte[] value = database.get(key.getBytes(StandardCharsets.UTF_8));
            return value != null;
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to check key: " + e.getMessage(), e);
        }
    }

    public Iterable<String> getAllKeys() {
        checkIfOpen();
        List<String> keys = new ArrayList<>();

        try (RocksIterator iterator = database.newIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                keys.add(new String(iterator.key(), StandardCharsets.UTF_8));
                iterator.next();
            }
        }

        return keys;
    }

    public void clear() {
        checkIfOpen();

        try (RocksIterator iterator = database.newIterator()) {
            iterator.seekToFirst();
            while (iterator.isValid()) {
                database.delete(iterator.key());
                iterator.next();
            }
        } catch (RocksDBException e) {
            throw new RuntimeException("Failed to clear database: " + e.getMessage(), e);
        }
    }

    private void checkIfOpen() {
        if (!isOpen) {
            throw new RuntimeException("Database is not open");
        }
    }
}
