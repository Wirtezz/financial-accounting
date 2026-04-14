package com.example.finance.controller;

import com.example.finance.model.RecurringTransaction;
import com.example.finance.model.Transaction;
import com.example.finance.model.TransactionType;
import com.example.finance.repository.RecurringTransactionRepositoryImpl;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
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
import java.util.*;
import java.util.stream.Collectors;

public class FinanceFXController implements Initializable {

    // ---------- Основная таблица ----------
    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, Integer> colNumber;
    @FXML private TableColumn<Transaction, String> colType;
    @FXML private TableColumn<Transaction, BigDecimal> colAmount;
    @FXML private TableColumn<Transaction, String> colCategory;
    @FXML private TableColumn<Transaction, String> colDescription;
    @FXML private TableColumn<Transaction, String> colDate;

    // Статистика
    @FXML private Label lblTotalIncome, lblTotalExpense, lblBalance;

    // Добавление операции
    @FXML private ComboBox<String> cmbType;
    @FXML private TextField txtDescription, txtCategory, txtAmount, txtDay, txtMonth, txtYear;
    @FXML private Button btnAdd;

    // Редактирование операции
    @FXML private TextField editId;
    @FXML private ComboBox<String> editType;
    @FXML private TextField editDescription, editCategory, editAmount, editDay, editMonth, editYear;
    @FXML private Button btnUpdate;

    // Отчёты
    @FXML private TextField txtStartDay, txtStartMonth, txtStartYear, txtEndDay, txtEndMonth, txtEndYear;
    @FXML private Button btnShowReport;
    @FXML private TextArea reportArea;

    // Удаление по номеру строки
    @FXML private TextField txtDeleteId;
    @FXML private Button btnDelete;
    @FXML private Label lblDeleteStatus;

    // Тема
    @FXML private Button btnTheme;

    // График
    @FXML private TextField chartStartDay, chartStartMonth, chartStartYear;
    @FXML private TextField chartEndDay, chartEndMonth, chartEndYear;
    @FXML private Button btnBuildChart;
    @FXML private LineChart<String, Number> financeChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    // ---------- Планировщик (регулярные операции) ----------
    @FXML private ComboBox<String> schedType;
    @FXML private TextField schedCategory, schedDescription, schedAmount, schedDay;
    @FXML private Button btnAddScheduled;
    @FXML private TableView<RecurringTransaction> scheduledTable;
    @FXML private TableColumn<RecurringTransaction, Integer> colSchedId;
    @FXML private TableColumn<RecurringTransaction, String> colSchedType;
    @FXML private TableColumn<RecurringTransaction, String> colSchedCategory;
    @FXML private TableColumn<RecurringTransaction, String> colSchedDescription;
    @FXML private TableColumn<RecurringTransaction, BigDecimal> colSchedAmount;
    @FXML private TableColumn<RecurringTransaction, Integer> colSchedDay;
    @FXML private TableColumn<RecurringTransaction, String> colSchedLast;
    @FXML private TableColumn<RecurringTransaction, Void> colSchedDelete;
    @FXML private Label schedInfo;

    // ---------- Внутренние поля ----------
    private TransactionController transactionController;
    private TransactionRepositoryImpl repository;
    private RecurringTransactionRepositoryImpl recurringRepo;
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private ObservableList<RecurringTransaction> scheduledList = FXCollections.observableArrayList();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static String currentTheme = "Светлая";
    private Image appIcon;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Иконка
        try {
            InputStream is = getClass().getResourceAsStream("/images/иконка финансов.png");
            if (is != null) {
                appIcon = new Image(is);
                System.out.println("Иконка загружена");
            } else {
                System.err.println("Иконка не найдена");
            }
        } catch (Exception e) {
            System.err.println("Ошибка иконки: " + e.getMessage());
        }

        showLoginAndWait();
        initDatabase();
        setupTable();
        setupThemeButton();

        // Добавление операции
        cmbType.getItems().addAll("ДОХОД", "РАСХОД");
        cmbType.setValue("ДОХОД");
        btnAdd.setOnAction(e -> addTransaction());

        // Редактирование
        editType.getItems().addAll("ДОХОД", "РАСХОД");
        editType.setValue("ДОХОД");
        btnUpdate.setOnAction(e -> updateTransaction());

        // Удаление
        btnDelete.setOnAction(e -> deleteTransactionByNumber());

        // Отчёты
        btnShowReport.setOnAction(e -> showReport());

        // График
        btnBuildChart.setOnAction(e -> buildChart());

        // Клик по таблице -> форма редактирования
        transactionsTable.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            if (newVal != null) loadTransactionToEditForm(newVal);
        });

        // ---------- Планировщик ----------
        schedType.getItems().addAll("ДОХОД", "РАСХОД");
        schedType.setValue("ДОХОД");
        btnAddScheduled.setOnAction(e -> addScheduledTransaction());
        setupScheduledTable();
        refreshScheduledTable();

        // Обновляем основную таблицу
        refreshData();
        applyTheme();

        // Цветная нижняя строка
        lblTotalIncome.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        lblTotalExpense.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        lblBalance.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");

        // Автоматическая проверка и добавление пропущенных регулярных операций
        processMissedRecurringTransactions();
    }

    // ------------------------------------------------------------------
    // Иконки, окно входа, тема
    // ------------------------------------------------------------------
    private void setIcon(Stage stage) {
        if (appIcon != null && stage != null) stage.getIcons().add(appIcon);
    }

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
        setIcon(loginStage);
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        try {
            InputStream is = getClass().getResourceAsStream("/images/img.png");
            if (is != null) {
                Image bgImage = new Image(is);
                BackgroundImage bg = new BackgroundImage(bgImage,
                        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true));
                root.setBackground(new Background(bg));
            } else root.setStyle("-fx-background-color: #2c3e50;");
        } catch (Exception e) { root.setStyle("-fx-background-color: #2c3e50;"); }
        Label title = new Label("Система учёта финансов");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setTextFill(Color.WHITE);
        title.setEffect(new DropShadow(5, Color.BLACK));
        title.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0,0.5), new CornerRadii(10), Insets.EMPTY)));
        title.setPadding(new Insets(10,20,10,20));
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
            result.ifPresent(theme -> { currentTheme = theme; applyTheme(); });
        });
    }

    private void applyTheme() {
        Scene scene = transactionsTable.getScene();
        if (scene == null) return;
        String textColor, baseStyle, fieldStyle;
        switch (currentTheme) {
            case "Синяя":
                baseStyle = "-fx-base: #1e3a5f; -fx-background: #d0e4f5; -fx-control-inner-background: #e6f2ff;";
                textColor = "black"; fieldStyle = "-fx-text-fill: black;"; break;
            case "Зелёная":
                baseStyle = "-fx-base: #2e6b2e; -fx-background: #d9f0d9; -fx-control-inner-background: #e8f5e8;";
                textColor = "black"; fieldStyle = "-fx-text-fill: black;"; break;
            case "Красная":
                baseStyle = "-fx-base: #8b0000; -fx-background: #ffe6e6; -fx-control-inner-background: #fff0f0;";
                textColor = "black"; fieldStyle = "-fx-text-fill: black;"; break;
            case "Жёлтая":
                baseStyle = "-fx-base: #b8860b; -fx-background: #fffacd; -fx-control-inner-background: #ffffe0;";
                textColor = "black"; fieldStyle = "-fx-text-fill: black;"; break;
            default:
                baseStyle = "-fx-base: #ececec; -fx-background: #f4f7fb; -fx-control-inner-background: white;";
                textColor = "black"; fieldStyle = "-fx-text-fill: black;";
        }
        scene.getRoot().setStyle(baseStyle);
        // текстовые поля основной вкладки
        txtDescription.setStyle(fieldStyle); txtCategory.setStyle(fieldStyle); txtAmount.setStyle(fieldStyle);
        txtDay.setStyle(fieldStyle); txtMonth.setStyle(fieldStyle); txtYear.setStyle(fieldStyle);
        editDescription.setStyle(fieldStyle); editCategory.setStyle(fieldStyle); editAmount.setStyle(fieldStyle);
        editDay.setStyle(fieldStyle); editMonth.setStyle(fieldStyle); editYear.setStyle(fieldStyle); editId.setStyle(fieldStyle);
        txtStartDay.setStyle(fieldStyle); txtStartMonth.setStyle(fieldStyle); txtStartYear.setStyle(fieldStyle);
        txtEndDay.setStyle(fieldStyle); txtEndMonth.setStyle(fieldStyle); txtEndYear.setStyle(fieldStyle);
        txtDeleteId.setStyle(fieldStyle); reportArea.setStyle(fieldStyle);
        // планировщик
        schedCategory.setStyle(fieldStyle); schedDescription.setStyle(fieldStyle); schedAmount.setStyle(fieldStyle); schedDay.setStyle(fieldStyle);
        cmbType.setStyle(fieldStyle); cmbType.lookupAll(".list-cell").forEach(node -> node.setStyle(fieldStyle));
        editType.setStyle(fieldStyle); editType.lookupAll(".list-cell").forEach(node -> node.setStyle(fieldStyle));
        schedType.setStyle(fieldStyle); schedType.lookupAll(".list-cell").forEach(node -> node.setStyle(fieldStyle));
        // таблицы
        transactionsTable.setStyle("-fx-text-fill: " + textColor + ";");
        transactionsTable.setRowFactory(tv -> {
            TableRow<Transaction> row = new TableRow<>();
            row.styleProperty().bind(javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then("").otherwise("-fx-text-fill: " + textColor + ";"));
            return row;
        });
        transactionsTable.lookupAll(".column-header .label").forEach(node -> node.setStyle("-fx-text-fill: " + textColor + ";"));
        scheduledTable.setStyle("-fx-text-fill: " + textColor + ";");
        scheduledTable.lookupAll(".column-header .label").forEach(node -> node.setStyle("-fx-text-fill: " + textColor + ";"));
    }

    // ------------------------------------------------------------------
    // Инициализация БД
    // ------------------------------------------------------------------
    private void initDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/finance_db";
            String user = "root";
            String password = "Leon63088.";
            Flyway flyway = Flyway.configure().dataSource(url, user, password).locations("classpath:db/migration").load();
            flyway.migrate();
            Connection conn = DriverManager.getConnection(url, user, password);
            repository = new TransactionRepositoryImpl(conn);
            recurringRepo = new RecurringTransactionRepositoryImpl(conn);
            TransactionServiceImpl service = new TransactionServiceImpl(repository);
            transactionController = new TransactionController(service);
        } catch (Exception e) {
            showAlert("Ошибка БД", "Не удалось подключиться: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Основные операции (таблица, добавление, редактирование, удаление, отчёты, график)
    // ------------------------------------------------------------------
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
        transactionsTable.setRowFactory(tv -> { TableRow<Transaction> row = new TableRow<>(); row.setPrefHeight(35); return row; });
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
            if (type.equals("ДОХОД")) transactionController.addIncome(description, category, amount, date);
            else transactionController.addExpense(description, category, amount, date);
            txtDescription.clear(); txtCategory.clear(); txtAmount.clear(); txtDay.clear(); txtMonth.clear(); txtYear.clear();
            refreshData();
            showAlert("Успех", "Операция добавлена");
        } catch (Exception e) { showAlert("Ошибка", "Неверные данные: " + e.getMessage()); }
    }

    private void loadTransactionToEditForm(Transaction t) {
        editId.setText(String.valueOf(t.getId()));
        editType.setValue(t.getType().toString());
        editDescription.setText(t.getDescription());
        editCategory.setText(t.getCategory());
        editAmount.setText(t.getAmount().toString());
        LocalDate date = t.getTransactionDate();
        editDay.setText(String.valueOf(date.getDayOfMonth()));
        editMonth.setText(String.valueOf(date.getMonthValue()));
        editYear.setText(String.valueOf(date.getYear()));
    }

    private void updateTransaction() {
        try {
            int id = Integer.parseInt(editId.getText().trim());
            String type = editType.getValue();
            String description = editDescription.getText().trim();
            String category = editCategory.getText().trim();
            String amountStr = editAmount.getText().trim().replace(',', '.');
            if (description.isEmpty() || category.isEmpty() || amountStr.isEmpty()) {
                showAlert("Ошибка", "Заполните описание, категорию и сумму");
                return;
            }
            BigDecimal amount = new BigDecimal(amountStr);
            int day = Integer.parseInt(editDay.getText().trim());
            int month = Integer.parseInt(editMonth.getText().trim());
            int year = Integer.parseInt(editYear.getText().trim());
            LocalDate date = LocalDate.of(year, month, day);
            TransactionType transactionType = type.equals("ДОХОД") ? TransactionType.INCOME : TransactionType.EXPENSE;
            Transaction updated = new Transaction(amount, transactionType, description, category, date);
            updated.setId(id);
            repository.update(updated);
            refreshData();
            showAlert("Успех", "Операция обновлена");
            editId.clear(); editDescription.clear(); editCategory.clear(); editAmount.clear();
            editDay.clear(); editMonth.clear(); editYear.clear();
        } catch (Exception e) { showAlert("Ошибка", "Не удалось обновить: " + e.getMessage()); }
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
        } catch (NumberFormatException e) { showAlert("Ошибка", "Введите номер строки (цифру)"); }
    }

    private void showReport() {
        try {
            LocalDate start = LocalDate.of(Integer.parseInt(txtStartYear.getText()), Integer.parseInt(txtStartMonth.getText()), Integer.parseInt(txtStartDay.getText()));
            LocalDate end = LocalDate.of(Integer.parseInt(txtEndYear.getText()), Integer.parseInt(txtEndMonth.getText()), Integer.parseInt(txtEndDay.getText()));
            BigDecimal totalIncome = transactionController.getService().getTotalIncomeByPeriod(start, end);
            BigDecimal totalExpense = transactionController.getService().getTotalExpenseByPeriod(start, end);
            BigDecimal balance = totalIncome.subtract(totalExpense);
            List<Transaction> transactions = transactionController.getService().getTransactionsByPeriod(start, end);
            StringBuilder sb = new StringBuilder();
            sb.append("ОТЧЕТ ЗА ПЕРИОД: ").append(start.format(dateFormatter)).append(" - ").append(end.format(dateFormatter)).append("\n");
            sb.append("--------------------------------------------------\n");
            sb.append(String.format("ДОХОДЫ: %.2f руб.\n", totalIncome));
            sb.append(String.format("РАСХОДЫ: %.2f руб.\n", totalExpense));
            sb.append(String.format("БАЛАНС: %.2f руб.\n", balance));
            sb.append("--------------------------------------------------\n");
            sb.append("ДЕТАЛИ ОПЕРАЦИЙ:\n");
            for (Transaction t : transactions) {
                sb.append(String.format("%s | %s | %.2f руб. | Категория: %s | %s\n",
                        t.getTransactionDate().format(dateFormatter), t.getType(), t.getAmount(), t.getCategory(), t.getDescription()));
            }
            reportArea.setText(sb.toString());
        } catch (Exception e) { showAlert("Ошибка", "Неверный формат даты: " + e.getMessage()); }
    }

    private void buildChart() {
        try {
            LocalDate start = LocalDate.of(Integer.parseInt(chartStartYear.getText()), Integer.parseInt(chartStartMonth.getText()), Integer.parseInt(chartStartDay.getText()));
            LocalDate end = LocalDate.of(Integer.parseInt(chartEndYear.getText()), Integer.parseInt(chartEndMonth.getText()), Integer.parseInt(chartEndDay.getText()));
            List<Transaction> transactions = transactionController.getService().getTransactionsByPeriod(start, end);
            Map<LocalDate, List<Transaction>> byDate = transactions.stream().collect(Collectors.groupingBy(Transaction::getTransactionDate));
            List<LocalDate> sortedDates = byDate.keySet().stream().sorted().collect(Collectors.toList());
            if (sortedDates.isEmpty()) { showAlert("Нет данных", "За выбранный период нет операций"); return; }
            XYChart.Series<String, Number> balanceSeries = new XYChart.Series<>();
            balanceSeries.setName("Накопленный баланс");
            BigDecimal runningBalance = BigDecimal.ZERO;
            for (LocalDate date : sortedDates) {
                String label = date.format(dateFormatter);
                BigDecimal dayIncome = byDate.get(date).stream().filter(t -> t.getType().toString().equals("INCOME")).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal dayExpense = byDate.get(date).stream().filter(t -> t.getType().toString().equals("EXPENSE")).map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                runningBalance = runningBalance.add(dayIncome.subtract(dayExpense));
                balanceSeries.getData().add(new XYChart.Data<>(label, runningBalance));
            }
            financeChart.getData().clear();
            financeChart.getData().add(balanceSeries);
            balanceSeries.getNode().setStyle("-fx-stroke: #1565c0; -fx-stroke-width: 3px;");
            financeChart.setTitle("Динамика баланса (накопленный итог)");
            xAxis.setLabel("Дата");
            yAxis.setLabel("Баланс (руб.)");
        } catch (Exception e) { showAlert("Ошибка", "Неверный формат даты для графика: " + e.getMessage()); }
    }

    // ------------------------------------------------------------------
    // Планировщик
    // ------------------------------------------------------------------
    private void setupScheduledTable() {
        colSchedId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSchedType.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getType().toString()));
        colSchedCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colSchedDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        colSchedAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colSchedDay.setCellValueFactory(new PropertyValueFactory<>("dayOfMonth"));
        colSchedLast.setCellValueFactory(cellData -> {
            LocalDate d = cellData.getValue().getLastExecuted();
            return new javafx.beans.property.SimpleStringProperty(d != null ? d.format(dateFormatter) : "");
        });
        colSchedDelete.setCellFactory(col -> new TableCell<>() {
            private final Button delBtn = new Button("Удалить");
            {
                delBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                delBtn.setOnAction(e -> {
                    RecurringTransaction rt = getTableView().getItems().get(getIndex());
                    deleteScheduled(rt.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : delBtn);
            }
        });
        scheduledTable.setItems(scheduledList);
    }

    private void refreshScheduledTable() {
        scheduledList.setAll(recurringRepo.findAll());
    }

    private void addScheduledTransaction() {
        try {
            String type = schedType.getValue();
            String category = schedCategory.getText().trim();
            String description = schedDescription.getText().trim();
            String amountStr = schedAmount.getText().trim().replace(',', '.');
            int day = Integer.parseInt(schedDay.getText().trim());
            if (category.isEmpty() || description.isEmpty() || amountStr.isEmpty()) {
                showAlert("Ошибка", "Заполните категорию, описание и сумму");
                return;
            }
            if (day < 1 || day > 31) {
                showAlert("Ошибка", "День месяца должен быть от 1 до 31");
                return;
            }
            BigDecimal amount = new BigDecimal(amountStr);
            TransactionType ttype = type.equals("ДОХОД") ? TransactionType.INCOME : TransactionType.EXPENSE;
            RecurringTransaction rt = new RecurringTransaction(ttype, category, description, amount, day, null);
            recurringRepo.save(rt);
            refreshScheduledTable();
            schedCategory.clear(); schedDescription.clear(); schedAmount.clear(); schedDay.clear();
            showAlert("Успех", "Регулярная операция добавлена");
        } catch (Exception e) {
            showAlert("Ошибка", "Неверные данные: " + e.getMessage());
        }
    }

    private void deleteScheduled(int id) {
        recurringRepo.delete(id);
        refreshScheduledTable();
        showAlert("Успех", "Регулярная операция удалена");
    }

    private void processMissedRecurringTransactions() {
        List<RecurringTransaction> list = recurringRepo.findAll();
        if (list.isEmpty()) return;
        LocalDate today = LocalDate.now();
        boolean anyAdded = false;
        for (RecurringTransaction rt : list) {
            LocalDate last = rt.getLastExecuted();
            // Вычисляем следующую дату выполнения
            LocalDate nextDate;
            if (last == null) {
                // Если никогда не выполнялась: берём текущий месяц, но если число больше сегодняшнего, то следующий месяц
                try {
                    nextDate = LocalDate.of(today.getYear(), today.getMonth(), rt.getDayOfMonth());
                    if (nextDate.isAfter(today)) {
                        nextDate = nextDate.minusMonths(1); // перейдём на прошлый месяц, потом цикл добавит
                    }
                } catch (Exception e) {
                    // неверная дата (например 31 февраля) – пропускаем
                    continue;
                }
            } else {
                nextDate = last.plusMonths(1).withDayOfMonth(rt.getDayOfMonth());
            }

            while (!nextDate.isAfter(today)) {
                // Добавляем транзакцию
                Transaction newTx = new Transaction(rt.getAmount(), rt.getType(), rt.getDescription(), rt.getCategory(), nextDate);
                transactionController.addTransaction(newTx);
                recurringRepo.updateLastExecuted(rt.getId(), nextDate);
                anyAdded = true;
                // Переходим к следующему месяцу
                nextDate = nextDate.plusMonths(1).withDayOfMonth(rt.getDayOfMonth());
            }
        }
        if (anyAdded) {
            refreshData();
            refreshScheduledTable();
            showAlert("Планировщик", "Добавлены пропущенные регулярные операции");
        }
    }

    // ------------------------------------------------------------------
    // Общие утилиты
    // ------------------------------------------------------------------
    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        setIconToDialog(alert);
        alert.showAndWait();
    }
}