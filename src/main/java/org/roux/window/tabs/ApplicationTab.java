package org.roux.window.tabs;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.fxmisc.easybind.EasyBind;
import org.roux.application.Application;
import org.roux.utils.Utils;
import org.roux.window.EditApplicationWindow;

import static org.roux.utils.Utils.makeTextButton;
import static org.roux.utils.Utils.makeVerticalSeparator;

public class ApplicationTab extends CustomTab {

    private final ObservableList<Application> applications;
    private final ObservableList<String> blacklist;
    private final EditApplicationWindow editApplicationWindow;

    private boolean seeBlacklisted = false;

    private TableView<Application> applicationView;

    public ApplicationTab(final Stage sourceWindow, final String name,
                          final ObservableList<Application> applications,
                          final ObservableList<String> blacklist) {
        super(sourceWindow, name);
        this.applications = applications;
        this.blacklist = blacklist;
        editApplicationWindow = new EditApplicationWindow(sourceWindow);
        editApplicationWindow.setOnHidden(event -> applicationView.refresh());

        applicationView = buildApplicationView();
        final HBox applicationButtons = buildApplicationButtons();

        final VBox root = new VBox(applicationView, applicationButtons);
        root.setAlignment(Pos.CENTER);
        root.setSpacing(5);
        setRoot(sourceWindow, root);
    }

    public TableView<Application> buildApplicationView() {
        final TableView<Application> table = new TableView<>();
        table.getItems().addAll(applications);
        table.setEditable(false);
        table.setStyle("-fx-font-size: 12");
        table.setRowFactory(tv -> {
            final TableRow<Application> row = new TableRow<>();
            EasyBind.select(row.itemProperty())
                    .selectObject(Application::isBlacklistedProperty)
                    .addListener((observable, oldValue, newValue) -> {
                        if(newValue == null) return;
                        if(newValue) {
                            // To ensure only one subsist
                            row.getStyleClass().remove("table-row-blacklisted");
                            row.getStyleClass().add("table-row-blacklisted");
                        } else {
                            row.getStyleClass().remove("table-row-blacklisted");
                        }
                    });
            row.setOnMouseClicked(event -> {
                if(event.getClickCount() == 2 && !row.isEmpty() && row.getItem() != null) {
                    final Application application = row.getItem();
                    editApplicationWindow.edit(application);
                }
            });

            return row;
        });
        table.getItems().addListener((Observable observable) -> Utils.autoResizeColumns(table));
        applications.addListener((Observable observable) -> {
            Utils.autoResizeColumns(table);
            table.refresh();
        });
        blacklist.addListener((Observable observable) -> {
            if(!seeBlacklisted) {
                applicationView.setItems(applications.filtered(item -> !item.isBlacklisted()));
            }
        });

        final TableColumn<Application, String> name = buildNameColumn();
        final TableColumn<Application, String> keywords = buildKeywordsColumn();
        table.getColumns().setAll(name, keywords);
        Utils.autoResizeColumns(table);
        return table;
    }

    public TableColumn<Application, String> buildNameColumn() {
        final TableColumn<Application, String> column = new TableColumn<>("Name");
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));

        return column;
    }

    public TableColumn<Application, String> buildKeywordsColumn() {
        final TableColumn<Application, String> column = new TableColumn<>("Keywords");
        column.setCellFactory(TextFieldTableCell.forTableColumn());
        column.setCellValueFactory(
                data -> new ReadOnlyStringWrapper(data.getValue().getKeywords().toString()));

        return column;
    }

    public HBox buildApplicationButtons() {
        final Button edit = makeTextButton("Edit", event -> {
            final Application application = applicationView.getSelectionModel().getSelectedItem();
            if(application != null) {
                editApplicationWindow.edit(application);
            }
        });

        final Button remove = makeTextButton("Remove", event -> {
            final Application application = applicationView.getSelectionModel().getSelectedItem();
            if(application != null) {
                applications.remove(application);
            }
        });

        final Button addBlacklist = makeTextButton("Add to blacklist", event -> {
            final Application application = applicationView.getSelectionModel().getSelectedItem();
            if(application != null) {
                application.setBlacklisted(true);
                blacklist.add(application.getExecutablePath().toString());
            }
        });

        final Button removeBlacklist = makeTextButton("Remove from blacklist", event -> {
            final Application application = applicationView.getSelectionModel().getSelectedItem();
            if(application != null) {
                application.setBlacklisted(false);
                blacklist.remove(application.getExecutablePath().toString());
            }
        });

        final CheckBox checkBox = new CheckBox();
        checkBox.setSelected(false);
        checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) seeBlacklisted = newValue;
        });

        final HBox buttons = new HBox(edit, makeVerticalSeparator(),
                                      remove, makeVerticalSeparator(),
                                      addBlacklist, removeBlacklist, checkBox);
        buttons.setAlignment(Pos.CENTER);
        buttons.setSpacing(10);
        buttons.setPadding(new Insets(10));
        return buttons;
    }
}