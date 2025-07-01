package alture.esposamandoufx.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Produto {
    private String name;
    private String quantity;
    private boolean purchased;
    private LocalDateTime purchaseDate;

    public Produto(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
        this.purchased = false;
        this.purchaseDate = null;
    }

    public Produto(String name, String quantity, boolean purchased, LocalDateTime purchaseDate) {
        this.name = name;
        this.quantity = quantity;
        this.purchased = purchased;
        this.purchaseDate = purchaseDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public boolean isPurchased() {
        return purchased;
    }

    public void setPurchased(boolean purchased) {
        this.purchased = purchased;
        if (purchased && purchaseDate == null) {
            this.purchaseDate = LocalDateTime.now();
        } else if (!purchased) {
            this.purchaseDate = null;
        }
    }

    public LocalDateTime getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(LocalDateTime purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getFormattedPurchaseDate() {
        if (purchaseDate == null) {
            return "Not purchased";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return purchaseDate.format(formatter);
    }

    @Override
    public String toString() {
        return name + " (" + quantity + ")" + (purchased ? " - Purchased on " + getFormattedPurchaseDate() : "");
    }

    public String toStorageString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("|");
        sb.append(quantity).append("|");
        sb.append(purchased).append("|");
        if (purchaseDate != null) {
            sb.append(purchaseDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
        return sb.toString();
    }

    public static Produto fromStorageString(String storageString) {
        String[] parts = storageString.split("\\|");
        String name = parts[0];
        String quantity = parts[1];
        boolean purchased = Boolean.parseBoolean(parts[2]);
        LocalDateTime purchaseDate = null;
        if (parts.length > 3 && !parts[3].isEmpty()) {
            purchaseDate = LocalDateTime.parse(parts[3], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return new Produto(name, quantity, purchased, purchaseDate);
    }
}