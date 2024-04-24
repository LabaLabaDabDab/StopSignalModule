package nsu.fit.khomchenko.stopsignalmodule.utils;

import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

import java.util.Arrays;
import java.util.Optional;

import static nsu.fit.khomchenko.stopsignalmodule.controllers.MainController.showAlert;

public class InputDialogHelper {
    public static Optional<String> promptTestPersonName() {
        TextInputDialog tableNameDialog = new TextInputDialog();
        tableNameDialog.setTitle("Испытуемый");
        tableNameDialog.setHeaderText("Введите имя испытуемого:");

        Optional<String> testName;
        do {
            testName = tableNameDialog.showAndWait().map(String::trim);
            if (testName.isPresent() && testName.get().contains("_")) {
                showAlert("Имя испытуемого не должно содержать символ '_'.");
                testName = Optional.empty();
            } else if (testName.isPresent() && testName.get().contains(" ")) {
                showAlert("Имя испытуемого не должно содержать пробелы.");
                testName = Optional.empty();
            }
        } while (!testName.isPresent());

        return testName;
    }

    public static Optional<String> promptGender() {
        String[] choices = {"М", "Ж"};
        ChoiceDialog<String> genderDialog = new ChoiceDialog<>("М", Arrays.asList(choices));
        genderDialog.setTitle("Пол");
        genderDialog.setHeaderText("Выберите пол (М/Ж):");
        genderDialog.setContentText("Пол:");

        return genderDialog.showAndWait();
    }

    public static Optional<Integer> promptAge() {
        Optional<Integer> age = Optional.empty();
        do {
            TextInputDialog ageDialog = new TextInputDialog();
            ageDialog.setTitle("Возраст");
            ageDialog.setHeaderText("Введите возраст (0-120):");
            ageDialog.setContentText("Возраст:");

            Optional<String> input = ageDialog.showAndWait();
            if (input.isPresent()) {
                age = parseAge(input.get());
                if (!age.isPresent()) {
                    showAlert("Введите корректное целочисленное значение для возраста.");
                } else if (age.get() < 0 || age.get() > 120) {
                    showAlert("Возраст должен быть в интервале от 0 до 120.");
                    age = Optional.empty();
                }
            } else {
                break;
            }
        } while (!age.isPresent());

        return age;
    }


    private static Optional<Integer> parseAge(String input) {
        try {
            int age = Integer.parseInt(input);
            return Optional.of(age);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}