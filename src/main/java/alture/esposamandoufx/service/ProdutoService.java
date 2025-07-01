package alture.esposamandoufx.service;

import alture.esposamandoufx.RocksDBService;
import alture.esposamandoufx.model.Produto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ProdutoService {
    private static ProdutoService instance;
    private final RocksDBService dbService;

    private ProdutoService() {
        dbService = RocksDBService.getInstance();
    }

    public static synchronized ProdutoService getInstance() {
        if (instance == null) {
            instance = new ProdutoService();
        }
        return instance;
    }

    public void open(String dbPath) {
        dbService.open(dbPath);
    }

    public void close() {
        dbService.close();
    }

    public String addItem(Produto item) {
        String id = generateId();
        dbService.put(id, item.toStorageString());
        return id;
    }

    public Produto getItem(String id) {
        String storageString = dbService.get(id);
        if (storageString == null) {
            return null;
        }
        return Produto.fromStorageString(storageString);
    }

    public boolean updateItem(String id, Produto item) {
        if (!dbService.containsKey(id)) {
            return false;
        }
        dbService.put(id, item.toStorageString());
        return true;
    }

    public boolean deleteItem(String id) {
        if (!dbService.containsKey(id)) {
            return false;
        }
        dbService.delete(id);
        return true;
    }

    public boolean markAsPurchased(String id) {
        Produto item = getItem(id);
        if (item == null) {
            return false;
        }
        item.setPurchased(true);
        return updateItem(id, item);
    }

    public List<Produto> getAllItems() {
        List<Produto> items = new ArrayList<>();
        for (String id : dbService.getAllKeys()) {
            Produto item = getItem(id);
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    public List<Produto> getUnpurchasedItems() {
        List<Produto> items = new ArrayList<>();
        for (String id : dbService.getAllKeys()) {
            Produto item = getItem(id);
            if (item != null && !item.isPurchased()) {
                items.add(item);
            }
        }
        return items;
    }

    public List<Produto> getPurchasedItems() {
        List<Produto> items = new ArrayList<>();
        for (String id : dbService.getAllKeys()) {
            Produto item = getItem(id);
            if (item != null && item.isPurchased()) {
                items.add(item);
            }
        }
        // Sort by purchase date (newest first)
        items.sort(Comparator.comparing(Produto::getPurchaseDate).reversed());
        return items;
    }

    public void clearAll() {
        dbService.clear();
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    public String getIdByName(String name) {
        for (String id : dbService.getAllKeys()) {
            Produto item = getItem(id);
            if (item != null && item.getName().equals(name)) {
                return id;
            }
        }
        return null;
    }
}