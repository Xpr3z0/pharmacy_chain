import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Locale;


// логин: postgres пароль: root
public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Locale.setDefault(new Locale("ru"));
        Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Авторизация");
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
