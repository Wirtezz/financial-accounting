package com.example.finance;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.InputStream;

public class FinanceFXApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/finance.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1200, 700);
        try {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        } catch (Exception e) {
            System.out.println("CSS не загружен");
        }

        // Иконка главного окна
        try {
            InputStream iconStream = getClass().getResourceAsStream("/images/иконка финансов.png");
            if (iconStream != null) {
                Image icon = new Image(iconStream);
                primaryStage.getIcons().add(icon);
                System.out.println("Иконка главного окна загружена");
            } else {
                System.err.println("Иконка не найдена: /images/иконка финансов.png");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки иконки: " + e.getMessage());
        }

        primaryStage.setTitle("Система учёта финансов");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(650);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}