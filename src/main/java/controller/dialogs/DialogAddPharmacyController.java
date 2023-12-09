package controller.dialogs;

import controller.BDController;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import bdclient.ClientPostgreSQL;
import util.MyAlerts;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DialogAddPharmacyController implements Initializable {
    public VBox inputVBox;
    public RadioButton anyGenderRB;
    public Button idBottomAdd;
    private List<String> columnNames;
    private String selectedTable;
    public DialogAddPharmacyController(List<String> columnNames, String selectedTable) {
        this.columnNames = columnNames;
        this.selectedTable = selectedTable;
    }


    // метод, вызываемый при нажатии на кнопку добавления
    public void onActionBottomAdd(ActionEvent actionEvent) {
        ObservableList<Node> list = inputVBox.getChildren();
        String sqlAdd = "INSERT INTO аптеки (адрес, телефон, электронная_почта, рабочие_часы) VALUES (";

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i) instanceof TextField) {

                if (i + 1 == list.size()) {

                    sqlAdd += "'" + ((TextField) list.get(i)).getText() + "');";
                    continue;
                }
                sqlAdd += "'" + ((TextField) list.get(i)).getText() + "',";


            }
        }

        ClientPostgreSQL.getInstance().simpleQuery(selectedTable, sqlAdd);

        MyAlerts.showInfoAlert("Запись добавлена успешно!");
        showTable();
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // ...
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

    public void onCancelBtn(ActionEvent actionEvent) {
        showTable();
    }

}