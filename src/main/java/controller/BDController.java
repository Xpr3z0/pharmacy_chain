package controller;

import controller.dialogs.DialogAddMedicinesController;
import controller.dialogs.DialogAddPharmacyController;
import controller.dialogs.DialogUpdateRecordsController;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import bdclient.ClientPostgreSQL;
import bdclient.JDBCClient;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class BDController implements Initializable {
    @FXML
    public ChoiceBox<String> tableChoice;
    public Button addTable;
//    public Button generateReportBtn;
    public TableView tableView;
    private JDBCClient jdbcClient;
    private String selectedTable = "";
    private List<String> columnNames;
    BDController() {
    }
    public BDController(String selectedTable) {
        this.selectedTable = selectedTable;
        Platform.runLater(() -> {
            updateTitles(selectedTable);
        });
    }
    private void updateTable() {
        cliningTable();
        fillingTable();
        fillingColumnsTable();
    }

    private void updateTitles(String selectedTable) {
        Stage currentStage = (Stage) tableView.getScene().getWindow();
        switch (selectedTable) {
            case "аптеки": {
                addTable.setText("Добавить новую аптеку");
//                LoginController.getBdStage().setTitle("Таблица магазинов");
                currentStage.setTitle("Таблица аптек");
                break;
            }
            case "препараты": {
                addTable.setText("Добавить препарат");
//                generateReportBtn.setVisible(true);
//                LoginController.getBdStage().setTitle("Таблица книг");
                currentStage.setTitle("Таблица препаратов");
                break;
            }
            case "учет": {
                addTable.setText("Обновить запись");
//                LoginController.getBdStage().setTitle("Таблица учёта");
                currentStage.setTitle("Таблица учёта");
                break;
            }
            default: addTable.setText("Добавить"); currentStage.setTitle("Таблица");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        jdbcClient = ClientPostgreSQL.getInstance();
        List<String> nameTablesSet = jdbcClient.getTableNames();
//        System.out.println(nameTablesSet);
        if (!selectedTable.isEmpty()) {
            updateTable();
        }
        if (nameTablesSet != null) {
            tableChoice.getItems().addAll(jdbcClient.getTableNames());
            for (int i = 0; i < tableChoice.getItems().size(); i++) {
                tableChoice.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        selectedTable = tableChoice.getValue();
                        updateTable();
                        Platform.runLater(() -> {
                            updateTitles(selectedTable);
                        });

                    }
                });
            }
            if ((selectedTable == null || selectedTable.trim().equals(""))) {
                tableChoice.setValue(tableChoice.getItems().get(0));
            } else {
                tableChoice.setValue(selectedTable);
            }
            tableView.setRowFactory(tv -> {
                TableRow<List<String>> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        openDialogUpdateRecords(row.getItem());
                    }
                });
                return row;
            });

        } else {
            new Alert(Alert.AlertType.WARNING, "Таблицы не найдены!").showAndWait();
        }
    }

    private void cliningTable() {
        tableView.getColumns().clear();
        tableView.getItems().clear();
    }
    private void fillingTable() {
        ResultSet resultSet = jdbcClient.getTable(selectedTable);
        try {
            if (resultSet != null) {
                ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                columnNames = new ArrayList<>();
                for (int i = 0; i < resultSetMetaData.getColumnCount(); ++i)
                    columnNames.add(resultSetMetaData.getColumnName(i + 1));
                ObservableList<List<String>> data = FXCollections.observableArrayList();
                while (resultSet.next()) {
                    List<String> row = new ArrayList<>();
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        row.add(resultSet.getString(i));
                    }
                    data.add(row);
                }
                tableView.setItems(data);

                tableChoice.setValue(selectedTable);
//                generateReportBtn.setVisible(false);

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void fillingColumnsTable() {
        for (int i = 0; i < columnNames.size(); ++i) {
            TableColumn column = new TableColumn(columnNames.get(i));
            final int finalI = i;
            column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<List<String>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<List<String>, String> data) {
                    return new ReadOnlyStringWrapper(data.getValue().get(finalI));
                }
            });
            column.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent>() {
                public void handle(TableColumn.CellEditEvent event) {
                    TablePosition tablePosition = event.getTablePosition();
                    String columnSearch = ((List) tableView.getItems().get(tablePosition.getRow())).get(0).toString();
                    String columnSearchName = ((TableColumn) tableView.getColumns().get(0)).getText();
                    if (!jdbcClient.updateTable(selectedTable, event.getTableColumn().getText(), event.getNewValue().toString(), columnSearchName, columnSearch)) {
                        new Alert(Alert.AlertType.WARNING, "Данные не изменены.").showAndWait();
                    }
                }
            });
            column.setCellFactory(TextFieldTableCell.forTableColumn());
            tableView.getColumns().add(column);

        }
    }
    public void onActionDelete(ActionEvent actionEvent) {
        int selectedIndex = tableView.getSelectionModel().getSelectedIndex();
        if (selectedIndex != -1) {
            String columnSearch = ((List) tableView.getItems().get(selectedIndex)).get(0).toString();
            String columnSearchName = ((TableColumn) tableView.getColumns().get(0)).getText();
            tableView.getItems().remove(selectedIndex);
            jdbcClient.deleteRowTable(selectedTable, columnSearchName, columnSearch);
        }
    }


    public void onActionReport(ActionEvent actionEvent) {
        // TODO: добавить диалоговое окошко с указанием параметров для выборки по критериям: автор, жанр,
        //  ценовая категория (интервал) 2 тект. поля: мин.цена и макс.цена;
        //  реализовать выполнение запроса и запись в txt файл; для реализации
        //  запроса использовать stringListQuery() из ClientPostgreSQL
    }


    public void onActionAdd(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            if (selectedTable.equals("аптеки")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DialogAddPharmacy.fxml"));
                DialogAddPharmacyController dialogAddPharmacyController = new DialogAddPharmacyController(columnNames, selectedTable);
                loader.setController(dialogAddPharmacyController);
                stage.setTitle("Добавление новой аптеки");
                stage.setScene(new Scene(loader.load()));

            } else if (selectedTable.equals("препараты")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DialogAddMedicines.fxml"));
                DialogAddMedicinesController dialogAddMedicinesController = new DialogAddMedicinesController(selectedTable);
                loader.setController(dialogAddMedicinesController);
                stage.setTitle("Добавление нового препарата");
                stage.setScene(new Scene(loader.load()));

            } else if (selectedTable.equals("учет")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DialogUpdateRecord.fxml"));
//                DialogAddRecordsController dialogAddRecordsController = new DialogAddRecordsController(selectedTable);
//                loader.setController(dialogAddRecordsController);
                DialogUpdateRecordsController dialogUpdateRecordsController = new DialogUpdateRecordsController(selectedTable);
                loader.setController(dialogUpdateRecordsController);
                stage.setTitle("Обновление записи");
                stage.setScene(new Scene(loader.load()));

            }

            stage.show();
            Stage stageClose = (Stage) tableView.getScene().getWindow();
            stageClose.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openDialogUpdateRecords(List<String> rowData) {
        try {
            if (!selectedTable.equals("учет")) {
//                new Alert(Alert.AlertType.WARNING, "Выберите таблицу 'учет' для редактирования записей.").showAndWait();
                return;
            }

            String medicineName = rowData.get(0); // Предположим, что id книги находится во втором столбце
            String pharmacyAddress = rowData.get(1); // Предположим, что id магазина находится в первом столбце
            int value = Integer.parseInt(rowData.get(2)); // Предположим, что id книги находится во втором столбце

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DialogUpdateRecord.fxml"));
            DialogUpdateRecordsController dialogUpdateRecordsController = new DialogUpdateRecordsController(selectedTable, pharmacyAddress, medicineName, value);
            loader.setController(dialogUpdateRecordsController);

            Stage stage = new Stage();
            stage.setTitle("Обновление записи");
            stage.setScene(new Scene(loader.load()));
            stage.show();

            Stage stageClose = (Stage) tableView.getScene().getWindow();
            stageClose.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}