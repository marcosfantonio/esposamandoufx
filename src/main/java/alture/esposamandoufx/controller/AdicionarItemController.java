package alture.esposamandoufx.controller;

import alture.esposamandoufx.model.Produto;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class AdicionarItemController {
    @FXML
    private TextField itemNameField;

    @FXML
    private TextField quantityField;

    @FXML
    public void initialize() {
        itemNameField.requestFocus();
    }

    public Produto getProduto() {
        String itemName = itemNameField.getText().trim();
        String quantity = quantityField.getText().trim();

        if (itemName.isEmpty()) {
            return null;
        }

        return new Produto(itemName, quantity);
    }

    public void setValoresIniciais(String itemName, String quantity) {
        itemNameField.setText(itemName);
        quantityField.setText(quantity);
    }
}