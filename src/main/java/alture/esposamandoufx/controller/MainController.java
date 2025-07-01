package alture.esposamandoufx.controller;

import alture.esposamandoufx.model.Produto;
import alture.esposamandoufx.service.ProdutoService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainController {

    @FXML
    private TableView<Produto> shoppingListTableView;

    @FXML
    private TableColumn<Produto, String> itemNameColumn;

    @FXML
    private TableColumn<Produto, String> quantityColumn;

    @FXML
    private TableView<Produto> purchaseHistoryTableView;

    @FXML
    private TableColumn<Produto, String> historyItemNameColumn;

    @FXML
    private TableColumn<Produto, String> historyQuantityColumn;

    @FXML
    private TableColumn<Produto, String> historyDateColumn;

    @FXML
    private Label statusLabel;

    private final ProdutoService produtoService;
    private final Map<Produto, String> itemIdMap = new HashMap<>();

    public MainController() {
        // Inicia um Singleton do Produto Service
        produtoService = ProdutoService.getInstance();

        try {
            // Cria o caminho na pasta do usuario para manter a persistencia
            String userHome = System.getProperty("user.home");
            String dbPath = userHome + File.separator + "GroceryListManager" + File.separator + "database";
            // Abre o banco de dados com a persistencia
            produtoService.open(dbPath);
            System.out.println("Database opened at: " + dbPath);
        } catch (Exception e) {
            mostrarErro("Error initializing database", e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        statusLabel.setText("Lista de Produtos prontos");
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        TableColumn<Produto, Void> purchaseColumn = new TableColumn<>("Comprado");
        purchaseColumn.setPrefWidth(100);

        Callback<TableColumn<Produto, Void>, TableCell<Produto, Void>> cellFactory =
            new Callback<>() {
                @Override
                public TableCell<Produto, Void> call(final TableColumn<Produto, Void> param) {
                    return new TableCell<>() {
                        private final Button purchaseButton = new Button("Comprado");
                        {
                            purchaseButton.setOnAction(event -> {
                                Produto item = getTableView().getItems().get(getIndex());
                                confirmacaoCompra(item);
                            });
                        }

                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty) {
                                setGraphic(null);
                            } else {
                                setGraphic(purchaseButton);
                            }
                        }
                    };
                }
            };

        purchaseColumn.setCellFactory(cellFactory);
        shoppingListTableView.getColumns().add(purchaseColumn);

        // Logica do Historico
        historyItemNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        historyQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        historyDateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedPurchaseDate()));
        atualizarTabelas();
    }

    // Confirmaçao de Compra
    private void confirmacaoCompra(Produto item) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Compra");
        alert.setHeaderText("Comprando");
        alert.setContentText("Você tem certeza que já comprou " + item.getName() + "?");

        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                String id = getIdFromProduto(item);
                if (id != null) {
                    produtoService.markAsPurchased(id);
                    statusLabel.setText("Comprado: " + item.getName());

                    // Refresh the lists
                    atualizarTabelas();
                } else {
                    mostrarErro("Database Error", "Erro em achar o ID");
                }
            } catch (Exception e) {
                mostrarErro("Database Error", "Falha ao marcar item como comprado: " + e.getMessage());
            }
        }
    }

    @FXML
    protected void adicionarBotaoOnClick() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/alture/esposamandoufx/adicionar-produto-view.fxml"));
            DialogPane dialogPane = loader.load();
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("Add New Grocery Item");
            AdicionarItemController controller = loader.getController();
            dialog.showAndWait().ifPresent(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    Produto item = controller.getProduto();
                    if (item != null) {
                        try {
                            String id = produtoService.addItem(item);
                            statusLabel.setText("Adicionado: " + item.getName());

                            // Refresh the lists
                            atualizarTabelas();
                        } catch (Exception e) {
                            mostrarErro("Database Error", "Falha ao adicionar item: " + e.getMessage());
                        }
                    }
                }
            });
        } catch (Exception e) {
            mostrarErro("Dialog Error", "Falha ao abrir caixa de dialogo: " + e.getMessage());
        }
    }

    @FXML
    protected void comprarBotaoOnClick() {
        Produto produtoSelecionado = shoppingListTableView.getSelectionModel().getSelectedItem();
        if (produtoSelecionado == null) {
            mostrarErro("Selection Error", "Escolha um produto para comprar");
            return;
        }

        try {
            String id = getIdFromProduto(produtoSelecionado);
            if (id != null) {
                produtoService.markAsPurchased(id);
                statusLabel.setText("Purchased: " + produtoSelecionado.getName());

                // Refresh the lists
                atualizarTabelas();
            } else {
                mostrarErro("Database Error", "Falha ao buscar o ID do produto");
            }
        } catch (Exception e) {
            mostrarErro("Database Error", "Falha ao marcar produto como comrpado " + e.getMessage());
        }
    }

    @FXML
    protected void deletarBotaoOnClick() {
        Produto selectedItem = shoppingListTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            mostrarErro("Selection Error", "Selecione um produto para deletar");
            return;
        }

        try {
            String id = getIdFromProduto(selectedItem);
            if (id != null) {
                produtoService.deleteItem(id);
                statusLabel.setText("Deletado: " + selectedItem.getName());

                // Refresh the lists
                atualizarTabelas();
            } else {
                mostrarErro("Database Error", "Falha ao achar ID do produto");
            }
        } catch (Exception e) {
            mostrarErro("Database Error", "Falha ao deletar o produto: " + e.getMessage());
        }
    }

    @FXML
    protected void limparBotaoOnClick() {
        try {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Limpar Produtos");
            alert.setHeaderText("Limpar todos os produtos");
            alert.setContentText("Você tem certeza que quer limpar todos os produtos?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                produtoService.clearAll();
                statusLabel.setText("Todos os produtos limpados!");

                atualizarTabelas();
            }
        } catch (Exception e) {
            mostrarErro("Database Error", "Failed to clear grocery items: " + e.getMessage());
        }
    }

    @FXML
    protected void atualizarBotao() {
        atualizarTabelas();
    }

    private void atualizarTabelas() {
        try {
            itemIdMap.clear();

            List<Produto> unpurchasedItems = produtoService.getUnpurchasedItems();
            ObservableList<Produto> observableUnpurchasedItems = FXCollections.observableArrayList(unpurchasedItems);
            shoppingListTableView.setItems(observableUnpurchasedItems);

            List<Produto> purchasedItems = produtoService.getPurchasedItems();
            ObservableList<Produto> observablePurchasedItems = FXCollections.observableArrayList(purchasedItems);
            purchaseHistoryTableView.setItems(observablePurchasedItems);

            for (Produto item : unpurchasedItems) {
                String id = produtoService.getIdByName(item.getName());
                if (id != null) {
                    itemIdMap.put(item, id);
                }
            }
            for (Produto item : purchasedItems) {
                String id = produtoService.getIdByName(item.getName());
                if (id != null) {
                    itemIdMap.put(item, id);
                }
            }

            statusLabel.setText("Tabelas atualizadas: " + unpurchasedItems.size() + " produtos na lista de compras, "
                + purchasedItems.size() + "produtos na lista de compras passadas");
        } catch (Exception e) {
            mostrarErro("Database Error", "Falha ao atualizar as tabelas: " + e.getMessage());
        }
    }

    private String getIdFromProduto(Produto item) {
        return itemIdMap.get(item);
    }

    private void mostrarErro(String title, String message) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
