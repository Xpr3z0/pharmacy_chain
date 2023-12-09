package controller.dialogs;

import controller.BDController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.MyAlerts;
import bdclient.ClientPostgreSQL;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class DialogUpdateRecordsController implements Initializable {
    @FXML
    public TextField valueTF;
    @FXML
    public RadioButton sellRB;
    @FXML
    public RadioButton buyRB;
    public Button idBottomAdd;
    public ChoiceBox<String> storeAdressChoice;
    public ChoiceBox<String> bookNameChoice;
    public Label valueLabel;
    public TextField currentValueTF;
    private Connection connection = null;
    private String selectedTable;
    private ClientPostgreSQL clientPostgreSQL;
    private String pharmacyAddress;
    private String medicineName;
    private int value;
    private boolean newRecord = false;
    private boolean usingEmptyConstructor = false;

    public DialogUpdateRecordsController(String selectedTable, String pharmacyAddress, String medicineName, int value) {
        this.selectedTable = selectedTable;
        this.pharmacyAddress = pharmacyAddress;
        this.medicineName = medicineName;
        this.value = value;
        clientPostgreSQL = ClientPostgreSQL.getInstance();
    }

    public DialogUpdateRecordsController(String selectedTable) {
        this.selectedTable = selectedTable;
        this.pharmacyAddress = "";
        this.medicineName = "";
        this.value = 0;
        clientPostgreSQL = ClientPostgreSQL.getInstance();
        usingEmptyConstructor = true;
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        ArrayList<String> bookList = clientPostgreSQL.stringListQuery("SELECT название FROM препараты ORDER BY id", "название");
        ArrayList<String> storeList = clientPostgreSQL.stringListQuery("SELECT адрес FROM аптеки ORDER BY id", "адрес");

        String address = clientPostgreSQL.getInfoByColoumnQuery("SELECT адрес FROM аптеки WHERE адрес=?", pharmacyAddress);
        String medicine = clientPostgreSQL.getInfoByColoumnQuery("SELECT название FROM препараты WHERE название=?", medicineName);
        sellRB.setSelected(true);

        sellRB.setOnAction(event -> {
            valueLabel.setText("Количество книг для продажи");
        });

        buyRB.setOnAction(event -> {
            valueLabel.setText("Количество книг для поступления");
        });

        storeAdressChoice.getItems().addAll(storeList);
        bookNameChoice.getItems().addAll(bookList);

        storeAdressChoice.setOnAction(event -> {
            getValueOfBooks();
        });
        bookNameChoice.setOnAction(event -> {
            getValueOfBooks();
        });

        if (!usingEmptyConstructor) {
            storeAdressChoice.setValue(address);
            bookNameChoice.setValue(medicine);
            currentValueTF.setText(String.valueOf(value));
        }
    }

    public void getValueOfBooks() {
//        System.out.println("getValueOfBooks()");
        if (bookNameChoice.getValue() != null && storeAdressChoice != null &&
                !bookNameChoice.getValue().trim().equals("") && !storeAdressChoice.getValue().trim().equals("")) {

//            System.out.println("if-else: CHECK");

            try (Connection connection = clientPostgreSQL.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(
                         "SELECT количество_препаратов FROM учет WHERE название_препарата = ? AND адрес_аптеки = ?")) {

                medicineName = bookNameChoice.getValue();
                pharmacyAddress = storeAdressChoice.getValue();

                preparedStatement.setString(1, medicineName);
                preparedStatement.setString(2, pharmacyAddress);
//                System.out.println(preparedStatement);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int bookCount = resultSet.getInt("количество_препаратов");
                        currentValueTF.setText(String.valueOf(bookCount));
                        value = bookCount;
                        newRecord = false;
//                        System.out.println("Количество книг: " + bookCount);
                    } else {
                        currentValueTF.setText("0");
                        value = 0;
                        newRecord = true;
                        System.out.println("Запись с данным сочетанием не найдена.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void onCancelBtn(ActionEvent actionEvent) {
        showTable();
    }
    @FXML
    private void onActionBottomAdd(ActionEvent event) {
        String quantityText = valueTF.getText();
        if (!quantityText.isEmpty() && bookNameChoice.getValue() != null && storeAdressChoice.getValue() != null) {
            try {
                connection = clientPostgreSQL.getConnection();
                int quantity = Integer.parseInt(quantityText);
                if (sellRB.isSelected()) {
                    quantity = quantity * -1;
                } else if (buyRB.isSelected()) {
                    // ...
                }

                int newValue = value + quantity;
                if (newValue < 0) {
                    MyAlerts.showInfoAlert("Недостаточно препаратов в магазине");
//                    showInfoAlert("Недостаточно книг в магазине");
//                    new Alert(Alert.AlertType.WARNING, "Недостаточно книг в магазине").showAndWait();
                } else {
                    if (!newRecord) {
                        String updateQuery = "UPDATE учет SET количество_препаратов = " + newValue + " WHERE название_препарата = '" + medicineName + "' AND адрес_аптеки = '" + pharmacyAddress + "'";
//                    System.out.println("количество_книг = " + newValue + " WHERE название_книги = '" + bookName + "' AND адрес_магазина = '" + storeAddress + "'");
                        ClientPostgreSQL.getInstance().simpleQuery(selectedTable, updateQuery);

                        MyAlerts.showInfoAlert("Запись успешно обновлена!");
                        showTable();
                    } else {
                        String addQueryTemplate = "INSERT INTO учет (название_препарата, адрес_аптеки, количество_препаратов) VALUES ('%s', '%s', %s)";
                        String addQueryFinal = String.format(addQueryTemplate, bookNameChoice.getValue(), storeAdressChoice.getValue(), newValue);
                        ClientPostgreSQL.getInstance().simpleQuery(selectedTable, addQueryFinal);

                        MyAlerts.showInfoAlert("Запись успешно обновлена!");
                        showTable();
                    }
                }

            } catch (SQLException | NumberFormatException e) {
//                e.printStackTrace();
                MyAlerts.showErrorAlert("Ошибка при обновлении записи!");
            } finally {
                try {
                    clientPostgreSQL.closeConnection();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        } else {
            MyAlerts.showErrorAlert("Пожалуйста, заполните все поля!");
        }
    }

    private void showTable() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BD.fxml"));
            BDController dialogAddController = new BDController(selectedTable);
            loader.setController(dialogAddController);
            Stage stage = new Stage();
            stage.setTitle("Таблица");
            stage.setScene(new Scene(loader.load()));
            stage.show();
            ((Stage) idBottomAdd.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}