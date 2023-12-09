package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import bdclient.ClientPostgreSQL;
import bdclient.JDBCClient;
import java.io.IOException;

public class LoginController {
    public Button btnLogin;
    @FXML
    private TextField txtUsername;
    @FXML
    private PasswordField txtPassword;
    private static Stage bdStage;
    @FXML
    private void btnLoginAction(ActionEvent event) {
        String login = txtUsername.getText();
        String password = txtPassword.getText();
        try {
            if (login.isEmpty() || password.isEmpty()) {
                throw new Exception("Укажите логин или пароль!");
            }
            JDBCClient jdbcClient = ClientPostgreSQL.getInstance();
            if (jdbcClient.accessToDB(login, password)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BD.fxml"));
                BDController dialogAddController = new BDController();
                loader.setController(dialogAddController);
                bdStage = new Stage();
                bdStage.setTitle("Таблица");
                bdStage.setScene(new Scene(loader.load()));
                bdStage.show();
                ((Stage) btnLogin.getScene().getWindow()).close();
                System.out.println("Авторизация успешна прошла!");
            } else {
                new Alert(Alert.AlertType.ERROR, "Подключение не произошло.\nПроверьте логин или пароль.").showAndWait();
            }
        } catch (NullPointerException e) {
            new Alert(Alert.AlertType.ERROR, "Не найдена view BD.fxml").showAndWait();
            e.printStackTrace();
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, e.getMessage()).showAndWait();
        }
    }

    public static Stage getBdStage() {
        return bdStage;
    }
}
