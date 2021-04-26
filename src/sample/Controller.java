package sample;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class Controller implements Initializable {

    @FXML
    private ChoiceBox<String> choiceBox;

    @FXML
    private TextField x0TextField;

    @FXML
    private TextField y0TextField;

    @FXML
    private TextField x1TextField;

    @FXML
    private TextField y1TextField;

    @FXML
    private TextField rTextField;

    @FXML
    private Pane inputPane;

    @FXML
    private Pane lineInputPane;

    @FXML
    private Pane circleInputPane;

    @FXML
    private GridPane gridPane;

    @FXML
    private Button drawButton;

    @FXML
    private ListView<String> listView;

    private int numCols;
    private int numRows;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        numCols = (int) gridPane.getPrefWidth() / 10;
        numRows = (int) gridPane.getPrefHeight() / 10;

        createGrid(numCols, numRows);

        inputPane.setVisible(false);
        drawButton.setVisible(false);
        choiceBox.getItems().addAll(Arrays.stream(Algorithm.values()).map(Algorithm::getSimpleName).collect(Collectors.toList()));

        setupNumericTextField(x0TextField);
        setupNumericTextField(x1TextField);
        setupNumericTextField(y0TextField);
        setupNumericTextField(y1TextField);
    }

    public void onSelect() {
        inputPane.setVisible(true);
        drawButton.setVisible(true);

        String selected = choiceBox.getValue();

        if (selected.equals(Algorithm.BRESENHAM_CIRCLE.getSimpleName())) {
            lineInputPane.setVisible(false);
            circleInputPane.setVisible(true);
        } else {
            circleInputPane.setVisible(false);
            lineInputPane.setVisible(true);
        }
    }

    public void onDrawButtonClick() {
        List<String> errors = new ArrayList<>();

        if (x0TextField.getText().isEmpty()) {
            errors.add("Значение x0 должно быть задано!");
        }

        if (y0TextField.getText().isEmpty()) {
            errors.add("Значение y0 должно быть задано!");
        }

        if (choiceBox.getValue().equals(Algorithm.BRESENHAM_CIRCLE.getSimpleName())) {
            if (rTextField.getText().isEmpty()) {
                errors.add("Значение r должно быть задано!");
            }
        } else {
            if (x1TextField.getText().isEmpty()) {
                errors.add("Значение x1 должно быть задано!");
            }
            if (y1TextField.getText().isEmpty()) {
                errors.add("Значение y1 должно быть задано!");
            }
        }

        int x0 = Integer.parseInt(x0TextField.getText());
        int y0 = Integer.parseInt(y0TextField.getText());

        if (x0 >= numCols) {
            errors.add("Значение x0 должно быть < " + numCols);
        }
        if (y0 >= numRows) {
            errors.add("Значение y0 должно быть < " + numRows);
        }

        int x1 = 0;
        int y1 = 0;
        int r = 0;

        if (choiceBox.getValue().equals(Algorithm.BRESENHAM_CIRCLE.getSimpleName())) {
            r = Integer.parseInt(rTextField.getText());
            if (r > x0 || r > y0) {
                errors.add("Значение r должно быть не больше чем x0 и y0");
            }
            if (r + x0 >= numCols || r + y0 >= numRows) {
                errors.add("Круг не должен выходить за границы области!");
            }
        } else {
            x1 = Integer.parseInt(x1TextField.getText());
            y1 = Integer.parseInt(y1TextField.getText());

            if (x1 >= numCols) {
                errors.add("Значение x1 должно быть < " + numCols);
            }
            if (y1 >= numRows) {
                errors.add("Значение y1 должно быть < " + numRows);
            }
        }

        if (!errors.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, String.join("\n", errors)).showAndWait();
            return;
        }

        gridPane.getChildren().clear();
        createGrid(numCols, numRows);
        listView.getItems().clear();

        if (choiceBox.getValue().equals(Algorithm.BRESENHAM_CIRCLE.getSimpleName())) {
            drawCircle(x0, y0, r);
        } else {
            drawLine(x0, y0, x1, y1,
                    Arrays.stream(Algorithm.values())
                            .filter(algorithm -> algorithm.getSimpleName().equals(choiceBox.getValue()))
                            .findAny().orElseThrow(RuntimeException::new)
            );
        }
    }

    private void drawLine(int x0, int y0, int x1, int y1, Algorithm algorithm) {
        switch (algorithm) {
            case DDA:
                dda(x0, y0, x1, y1);
                break;
            case STEP_BY_STEP:
                stepByStep(x0, y0, x1, y1);
                break;
            case BRESENHAM:
                bresenham(x0, y0, x1, y1);
                break;
        }
    }

    private void stepByStep(int x0, int y0, int x1, int y1) {
        float k = (float) (y1 - y0) / (x1 - x0);
        float b = y0 - k * x0;

        float x = x0;
        float y = y0;
        float sign = Math.signum(x1 - x0);

        if (sign == 0) {
            gridPane.add(new Rectangle(10, 10, Color.BLACK), x0, y0);
            return;
        }

        listView.getItems().add("k = " + k);
        listView.getItems().add("b = " + b);

        while (Math.signum(x1 - x) == sign && x < numCols && y < numRows) {
            gridPane.add(new Rectangle(10, 10, Color.BLACK), Math.round(x), Math.round(y));
            x += sign * 0.1;
            y = k * x + b;
            listView.getItems().add("x = " + x);
            listView.getItems().add("y = kx + b = " + y);
        }
    }

    private void dda(int x0, int y0, int x1, int y1) {
        int l = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        float deltaX = (float) (x1 - x0) / l;
        float deltaY = (float) (y1 - y0) / l;

        listView.getItems().add("l = " + l);
        listView.getItems().add("delta x = " + deltaX);
        listView.getItems().add("delta y = " + deltaY);

        float x = x0;
        float y = y0;

        listView.getItems().add("x = " + x);
        listView.getItems().add("y = " + y);

        int i = 0;

        while (i <= l) {
            listView.getItems().add(String.format("i = %d: x = %d, y = %d", i, Math.round(x), Math.round(x)));
            gridPane.add(new Rectangle(10, 10, Color.BLACK), Math.round(x), Math.round(y));
            x = x + deltaX;
            y = y + deltaY;
            i++;
        }
    }

    private void bresenham(int x0, int y0, int x1, int y1) {
        boolean step = Math.abs(y1 - y0) > Math.abs(x1 - x0);

        listView.getItems().add("step = " + step);

        if (step) {
            int temp = x0;
            x0 = y0;
            y0 = temp;

            temp = x1;
            x1 = y1;
            y1 = temp;
        }

        if (x0 > x1) {
            int temp = x0;
            x0 = x1;
            x1 = temp;

            temp = y0;
            y0 = y1;
            y1 = temp;
        }

        int dx = x1 - x0;
        int dy = Math.abs(y1 - y0);
        int error = dx / 2;
        int yStep = y0 < y1 ? 1 : -1;
        int y = y0;

        listView.getItems().add("dx = " + dx);
        listView.getItems().add("dy = " + dy);
        listView.getItems().add("error = " + error);
        listView.getItems().add("yStep = " + yStep);
        listView.getItems().add("y = " + y);

        for (int x = x0; x <= x1; x++) {
            gridPane.add(new Rectangle(10, 10, Color.BLACK), step ? y : x, step ? x : y);
            error -= dy;
            if (error < 0) {
                y += yStep;
                error += dx;
            }
            listView.getItems().add(String.format("x = %d: y = %d, error = %d", x, y, error));
        }
    }

    private void drawCircle(int x0, int y0, int r) {
        int x = 0;
        int y = r;
        int delta = 1 - 2 * r;
        int error;

        listView.getItems().add("x = 0");
        listView.getItems().add("y = r");
        listView.getItems().add("delta = " + delta);

        while (y >= 0) {
            gridPane.add(new Rectangle(10, 10, Color.BLACK), x0 + x, y0 + y);
            gridPane.add(new Rectangle(10, 10, Color.BLACK), x0 + x, y0 - y);
            gridPane.add(new Rectangle(10, 10, Color.BLACK), x0 - x, y0 + y);
            gridPane.add(new Rectangle(10, 10, Color.BLACK), x0 - x, y0 - y);

            error = 2 * (delta + y) - 1;
            if (delta < 0 && error <= 0) {
                delta += 2 * ++x + 1;
            } else if (delta > 0 && error > 0) {
                delta -= 2 * --y + 1;
            } else {
                delta += 2 * (++x - --y);
            }

            listView.getItems().add(String.format("x = %d, y = %d, error = %d, delta = %d", x, y, error, delta));
        }
    }

    private void createGrid(int numCols, int numRows) {
        for (int i = 0; i < numCols; i++) {
            for (int j = 0; j < numRows; j++) {
                BorderStrokeStyle topStyle = BorderStrokeStyle.SOLID;
                BorderStrokeStyle rightStyle = i == numCols - 1 ? BorderStrokeStyle.SOLID : BorderStrokeStyle.NONE;
                BorderStrokeStyle bottomStyle = j == numRows - 1 ? BorderStrokeStyle.SOLID : BorderStrokeStyle.NONE;
                BorderStrokeStyle leftStyle = BorderStrokeStyle.SOLID;

                BorderStroke borderStroke = new BorderStroke(
                        Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK,
                        topStyle, rightStyle, bottomStyle, leftStyle,
                        CornerRadii.EMPTY, new BorderWidths(1), Insets.EMPTY
                );

                Region cell = new Region();
                cell.setBorder(new Border(borderStroke));
                cell.setPrefWidth(10);
                cell.setPrefHeight(10);

                gridPane.add(cell, i, j);
            }
        }
    }

    private void setupNumericTextField(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.isEmpty() || !newVal.matches("\\d*")) {
                textField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private enum Algorithm {
        STEP_BY_STEP("Пошаговый алгоритм"),
        DDA("алгоритм ЦДА"),
        BRESENHAM("алгоритм Брезенхема"),
        BRESENHAM_CIRCLE("алгоритм Брезенхема (окружность)");

        private final String simpleName;

        public String getSimpleName() {
            return simpleName;
        }

        Algorithm(String name) {
            this.simpleName = name;
        }
    }
}