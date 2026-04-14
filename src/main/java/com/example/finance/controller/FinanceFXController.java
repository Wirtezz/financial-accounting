package com.example.finance.controller;

import com.example.finance.model.Transaction;
import com.example.finance.repository.TransactionRepositoryImpl;
import com.example.finance.service.TransactionServiceImpl;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.flywaydb.core.Flyway;

import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FinanceFXController implements Initializable {

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> colNumber;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, BigDecimal> colAmount;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colDate;

    @FXML private Label lblTotalIncome, lblTotalExpense, lblBalance;
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtDescription, txtCategory, txtAmount, txtDay, txtMonth, txtYear;
    @FXML private Button btnAdd;

    @FXML private TextField txtStartDay, txtStartMonth, txtStartYear, txtEndDay, txtEndMonth, txtEndYear;
    @FXML private Button btnShowReport;
    @FXML private TextArea reportArea;

    @FXML private TextField txtDeleteId;
    @FXML private Button btnDelete;
    @FXML private Label lblDeleteStatus;
    @FXML private Button btnTheme;

    private TransactionController transactionController;
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static String currentTheme = "Светлая";

    private Image appIcon; // иконка, загруженная один раз

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Загружаем иконку
        try {
            InputStream is = getClass().getResourceAsStream("/images/иконка финансов.png");
            if (is != null) {
                appIcon = new Image(is);
                System.out.println("Иконка загружена из /images/иконка финансов.png");
            } else {
                System.err.println("Иконка не найдена по пути /images/иконка финансов.png");
            }
        } catch (Exception e) {
            System.err.println("Ошибка загрузки иконки: " + e.getMessage());
        }

        showLoginAndWait();
        initDatabase();
        setupTable();
        setupThemeButton();
        cmbType.getItems().addAll("ДОХОД", "РАСХОД");
        cmbType.setValue("ДОХОД");
        btnAdd.setOnAction(e -> addTransaction());
        btnDelete.setOnAction(e -> deleteTransactionByNumber());
        btnShowReport.setOnAction(e -> showReport());
        refreshData();
        applyTheme();

        // Цветная нижняя строка
        lblTotalIncome.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        lblTotalExpense.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        lblBalance.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
    }

    // Установка иконки на окно (Stage)
    private void setIcon(Stage stage) {
        if (appIcon != null && stage != null) {
            stage.getIcons().add(appIcon);
        }
    }

    // Установка иконки на диалог (Alert, ChoiceDialog)
    private void setIconToDialog(Dialog<?> dialog) {
        if (appIcon != null && dialog.getDialogPane().getScene() != null) {
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(appIcon);
        }
    }

    private void showLoginAndWait() {
        Stage loginStage = new Stage();
        loginStage.initModality(Modality.APPLICATION_MODAL);
        loginStage.setTitle("Вход в систему");
        setIcon(loginStage); // иконка для окна входа

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        // Фоновая картинка
        try {
            InputStream is = getClass().getResourceAsStream("/images/img.png");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage bg = new BackgroundImage(
                        bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                root.setBackground(new Background(bg));
            } else {
                root.setStyle("-fx-background-color: #2c3e50;");
            }
        } catch (Exception e) {
            root.setStyle("-fx-background-color: #2c3e50;");
        }

        Label title = new Label("Система учёта финансов");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(5, Color.BLACK));
        title.setBackground(new Background(new BackgroundFill(
                Color.rgb(0, 0, 0, 0.5), new CornerRadii(10), Insets.EMPTY
        )));
        title.setPadding(new Insets(10, 20, 10, 20));

        Button btnLogin = new Button("Зайти");
        Button btnExit = new Button("Выход");
        btnLogin.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 30;");
        btnExit.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 30;");
        btnLogin.setOnAction(e -> loginStage.close());
        btnExit.setOnAction(e -> System.exit(0));

        root.getChildren().addAll(title, btnLogin, btnExit);
        Scene scene = new Scene(root, 500, 400);
        loginStage.setScene(scene);
        loginStage.showAndWait();
    }

    private void setupThemeButton() {
        btnTheme.setOnAction(e -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(currentTheme,
                    "Светлая", "Синяя", "Зелёная", "Красная", "Жёлтая");
            dialog.setTitle("Настройки темы");
            dialog.setHeaderText("Выберите цветовую гамму");
            dialog.setContentText("Тема:");
            setIconToDialog(dialog);
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(theme -> {
                currentTheme = theme;
                applyTheme();
            });
        });
    }

    private void applyTheme() {
        Scene scene = transactionsTable.getScene();
        if (scene == null) return;

        String textColor, baseStyle, fieldStyle;
        switch (currentTheme) {
            case "Синяя":
                baseStyle = "-fx-base: #1e3a5f; -fx-background: #d0e4f5; -fx-control-inner-background: #e6f2ff;";
                textColor = "black";
                fieldStyle = "-fx-text-fill: black;";
                break;
            case "Зелёная":
                baseStyle = "-fx-base: #2e6b2e; -fx-background: #d9f0d9; -fx-control-inner-background: #e8f5e8;";
                textColor = "black";
                fieldStyle = "-fx-text-fill: black;";
                break;
            case "Красная":
                baseStyle = "-fx-base: #8b0000; -fx-background: #ffe6e6; -fx-control-inner-background: #fff0f0;";
                textColor = "black";
                fieldStyle = "-fx-text-fill: black;";
                break;
            case "Жёлтая":
                baseStyle = "-fx-base: #b8860b; -fx-background: #fffacd; -fx-control-inner-background: #ffffe0;";
                textColor = "black";
                fieldStyle = "-fx-text-fill: black;";
                break;
            default:
                baseStyle = "-fx-base: #ececec; -fx-background: #f4f7fb; -fx-control-inner-background: white;";
                textColor = "black";
                fieldStyle = "-fx-text-fill: black;";
        }

        scene.getRoot().setStyle(baseStyle);
        txtDescription.setStyle(fieldStyle);
        txtCategory.setStyle(fieldStyle);
        txtAmount.setStyle(fieldStyle);
        txtDay.setStyle(fieldStyle);
        txtMonth.setStyle(fieldStyle);
        txtYear.setStyle(fieldStyle);
        txtStartDay.setStyle(fieldStyle);
        txtStartMonth.setStyle(fieldStyle);
        txtStartYear.setStyle(fieldStyle);
        txtEndDay.setStyle(fieldStyle);
        txtEndMonth.setStyle(fieldStyle);
        txtEndYear.setStyle(fieldStyle);
        txtDeleteId.setStyle(fieldStyle);
        reportArea.setStyle(fieldStyle);

        cmbType.setStyle(fieldStyle);
        cmbType.lookupAll(".list-cell").forEach(node -> node.setStyle(fieldStyle));
        cmbType.lookupAll(".combo-box-base").forEach(node -> node.setStyle(fieldStyle));

        transactionsTable.setStyle("-fx-text-fill: " + textColor + ";");
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.styleProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then("")
                    .otherwise("-fx-text-fill: " + textColor + ";"));
            return row;
        });
        transactionsTable.lookupAll(".column-header .label").forEach(node ->
                node.setStyle("-fx-text-fill: " + textColor + ";")
        );
    }

    private void initDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/finance_db";
            String user = "root";
            String password = "Leon63088.";
            Flyway flyway = Flyway.configure().dataSource(url, user, password).locations("classpath:db/migration").load();
            flyway.migrate();
            Connection conn = DriverManager.getConnection(url, user, password);
            TransactionRepositoryImpl repo = new TransactionRepositoryImpl(conn);
            TransactionServiceImpl service = new TransactionServiceImpl(repo);
            transactionController = new TransactionController(service);
        } catch (Exception e) {
            showAlert("Ошибка БД", "Не удалось подключиться: " + e.getMessage());
        }
    }

    private void setupTable() {
        colNumber.setCellValueFactory(cellData -> {
            int index = transactionsTable.getItems().indexOf(cellData.getValue()) + 1;
            return new javafx.beans.property.SimpleIntegerProperty(index).asObject();
        });
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colDate.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getTransactionDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date.format(dateFormatter) : "");
        });

        colCategory.setCellFactory(tc -> {
            TableCell<Transaction, String> cell = new TableCell<>();
            javafx.scene.text.Text text = new javafx.scene.text.Text();
            cell.setGraphic(text);
            text.wrappingWidthProperty().bind(cell.widthProperty().subtract(10));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
        colDescription.setCellFactory(tc -> {
            TableCell<Transaction, String> cell = new TableCell<>();
            javafx.scene.text.Text text = new javafx.scene.text.Text();
            cell.setGraphic(text);
            text.wrappingWidthProperty().bind(cell.widthProperty().subtract(10));
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.setPrefHeight(35);
            return row;
        });
        transactionsTable.setItems(transactionList);
        transactionsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void refreshData() {
        if (transactionController == null) return;
        transactionList.setAll(transactionController.getService().getAllTransactions());
        BigDecimal totalIncome = transactionController.getService().getTotalIncome();
        BigDecimal totalExpense = transactionController.getService().getTotalExpense();
        BigDecimal balance = totalIncome.subtract(totalExpense);
        lblTotalIncome.setText(String.format("%.2f руб.", totalIncome));
        lblTotalExpense.setText(String.format("%.2f руб.", totalExpense));
        lblBalance.setText(String.format("%.2f руб.", balance));
    }

    private void addTransaction() {
        try {
            String type = cmbType.getValue();
            String description = txtDescription.getText().trim();
            String category = txtCategory.getText().trim();
            String amountStr = txtAmount.getText().trim().replace(',', '.');
            if (description.isEmpty() || category.isEmpty() || amountStr.isEmpty()) {
                showAlert("Ошибка", "Заполните описание, категорию и сумму");
                return;
            }
            BigDecimal amount = new BigDecimal(amountStr);
            int day = Integer.parseInt(txtDay.getText().trim());
            int month = Integer.parseInt(txtMonth.getText().trim());
            int year = Integer.parseInt(txtYear.getText().trim());
            LocalDate date = LocalDate.of(year, month, day);
            if (type.equals("ДОХОД")) {
                transactionController.addIncome(description, category, amount, date);
            } else {
                transactionController.addExpense(description, category, amount, date);
            }
            txtDescription.clear(); txtCategory.clear(); txtAmount.clear(); txtDay.clear(); txtMonth.clear(); txtYear.clear();
            refreshData();
            showAlert("Успех", "Операция добавлена");
        } catch (Exception e) {
            showAlert("Ошибка", "Неверные данные: " + e.getMessage());
        }
    }

    private void deleteTransactionByNumber() {
        try {
            int rowNumber = Integer.parseInt(txtDeleteId.getText().trim());
            if (rowNumber < 1 || rowNumber > transactionList.size()) {
                showAlert("Ошибка", "Нет операции с номером " + rowNumber);
                return;
            }
            Transaction t = transactionList.get(rowNumber - 1);
            int realId = t.getId();
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Подтверждение удаления");
            confirm.setHeaderText("Удалить операцию №" + rowNumber + "?");
            confirm.setContentText("Тип: " + t.getType() + "\nСумма: " + t.getAmount() + " руб.\nКатегория: " + t.getCategory());
            ButtonType yes = new ButtonType("Да", ButtonBar.ButtonData.YES);
            ButtonType no = new ButtonType("Нет", ButtonBar.ButtonData.NO);
            confirm.getButtonTypes().setAll(yes, no);
            setIconToDialog(confirm);
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == yes) {
                transactionController.deleteTransaction(realId);
                refreshData();
                txtDeleteId.clear();
                lblDeleteStatus.setText("Операция №" + rowNumber + " удалена");
                showAlert("Успех", "Операция удалена");
            }
        } catch (NumberFormatException e) {
            showAlert("Ошибка", "Введите номер строки (цифру)");
        }
    }

    private void showReport() {
        try {
            int startDay = Integer.parseInt(txtStartDay.getText().trim());
            int startMonth = Integer.parseInt(txtStartMonth.getText().trim());
            int startYear = Integer.parseInt(txtStartYear.getText().trim());
            int endDay = Integer.parseInt(txtEndDay.getText().trim());
            int endMonth = Integer.parseInt(txtEndMonth.getText().trim());
            int endYear = Integer.parseInt(txtEndYear.getText().trim());
            LocalDate startDate = LocalDate.of(startYear, startMonth, startDay);
            LocalDate endDate = LocalDate.of(endYear, endMonth, endDay);
            BigDecimal totalIncome = transactionController.getService().getTotalIncomeByPeriod(startDate, endDate);
            BigDecimal totalExpense = transactionController.getService().getTotalExpenseByPeriod(startDate, endDate);
            BigDecimal balance = totalIncome.subtract(totalExpense);
            List<Transaction> transactions = transactionController.getService().getTransactionsByPeriod(startDate, endDate);
            StringBuilder sb = new StringBuilder();
            sb.append("ОТЧЕТ ЗА ПЕРИОД: ").append(startDate.format(dateFormatter)).append(" - ").append(endDate.format(dateFormatter)).append("\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("ДОХОДЫ: %.2f руб.\n", totalIncome));
            sb.append(String.format("РАСХОДЫ: %.2f руб.\n", totalExpense));
            sb.append(String.format("БАЛАНС: %.2f руб.\n", balance));
            sb.append("--------------------------------------------------\n");
            sb.append("ДЕТАЛИ ОПЕРАЦИЙ:\n");
            for (Transaction t : transactions) {
                sb.append(String.format("%s | %s | %.2f руб. | %s\n",
                        t.getTransactionDate().format(dateFormatter), t.getType(), t.getAmount(), t.getDescription()));
            }
            reportArea.setText(sb.toString());
        } catch (Exception e) {
            showAlert("Ошибка", "Неверный формат даты: " + e.getMessage());
        }
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        setIconToDialog(alert);
        alert.showAndWait();
    }
}