package org.roux.window;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Border;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.roux.application.Application;
import org.roux.application.ApplicationLibrary;
import org.roux.utils.AutoCompleteTextField;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.roux.utils.Utils.makeGraphicButton;
import static org.roux.utils.Utils.makeVerticalSeparator;

public class MainWindow extends UndecoratedStage {

    public static final int FIELD_WIDTH = 280;
    public static final int APP_HEIGHT = FIELD_WIDTH / 12;
    public static final int BUTTON_SIZE = APP_HEIGHT;

    private final ApplicationLibrary applicationLibrary;

    private OptionWindow optionWindow;

    private AutoCompleteTextField textField;

    public MainWindow(final ApplicationLibrary applicationLibrary) {
        this.applicationLibrary = applicationLibrary;
        final Parent root = buildRoot();

        setRoot(root);
        scene.setFill(Color.TRANSPARENT);
        scene.setOnKeyPressed(ke -> {
            if(ke.getCode() == KeyCode.ESCAPE) {
                close();
                Platform.exit();
            }
        });
        setAlwaysOnTop(true);
        focusedProperty().addListener((observableValue, node, t1) -> {
            //            System.out.println("Focus changed to -> " + t1);
        });
        setOnShowing(event -> textField.requestFocus());
    }

    public void launchApplication(final String name) {
        final Application application = applicationLibrary.getApplication(name);
        if(application != null) {
            final Path path = application.getExecutablePath();
            if(path.toFile().canExecute()) {
                try {
                    final ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", path.toString());
                    processBuilder.start();
                } catch(final IOException e) {
                    e.printStackTrace();
                }
                close();
            } else {
                // error, missing executable
            }
        }
        //@todo On verra si on met un truc ici pour dire que y'a erreur
    }

    public void scan() {
        final ObservableList<Application> applications = applicationLibrary.scan();
        textField.getEntries().clear();
        textField.getEntries().addAll(applications.stream()
                                              .map(Application::getName)
                                              .collect(Collectors.toList())
        );
    }

    public Parent buildRoot() {
        textField = makeField();
        final Button updateButton = makeGraphicButton("update-icon.png", MainWindow.BUTTON_SIZE,
                                                      event -> {
                                                          scan();
                                                          System.out.println("Scanning done");
                                                          event.consume();
                                                      });
        final Button optionButton = makeGraphicButton("option-icon.png", MainWindow.BUTTON_SIZE,
                                                      event -> {
                                                          if(optionWindow == null)
                                                              optionWindow = new OptionWindow(this,
                                                                                              applicationLibrary);
                                                          optionWindow.show();
                                                          setOpacity(0);
                                                          event.consume();
                                                      });

        final HBox root = new HBox(updateButton, makeVerticalSeparator(), textField, makeVerticalSeparator(),
                                   optionButton);
        root.setPadding(new Insets(2));
        root.setBorder(Border.EMPTY);
        root.setAlignment(Pos.CENTER);

        return root;
    }

    public AutoCompleteTextField makeField() {
        final AutoCompleteTextField textField = new AutoCompleteTextField(this, applicationLibrary);
        textField.setPromptText("Find an app");
        textField.setPrefSize(FIELD_WIDTH, APP_HEIGHT);
        textField.getEntries().addAll(
                applicationLibrary.getLibrary()
                        .stream()
                        .map(Application::getName)
                        .collect(Collectors.toList())
        );
        return textField;
    }

}