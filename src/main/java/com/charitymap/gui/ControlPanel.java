package com.charitymap.gui;

import com.charitymap.model.AidType;
import com.charitymap.model.Association;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;
import com.charitymap.service.CsvImporter;
import com.charitymap.service.MapIO;
import com.charitymap.service.RandomGenerator;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Left-side control panel.
 */
public class ControlPanel extends VBox {

    private final CharityMap charityMap;
    private final MapCanvas mapCanvas;
    private final DetailsPanel detailsPanel;

    /**
     * Creates a new ControlPanel with buttons, filters, and file actions.
     *
     * @param charityMap   the central map data model
     * @param mapCanvas    the map drawing canvas
     * @param detailsPanel the right property details panel
     */
    public ControlPanel(CharityMap charityMap, MapCanvas mapCanvas, DetailsPanel detailsPanel) {
        this.charityMap = charityMap;
        this.mapCanvas = mapCanvas;
        this.detailsPanel = detailsPanel;

        setPrefWidth(240);
        setMinWidth(240);
        setMaxWidth(240);
        setSpacing(8);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f1f5f9; -fx-border-color: #cbd5e1; -fx-border-width: 0 1px 0 0;");

        Label title = new Label("Controls");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        getChildren().addAll(
                title,
                new Separator(),
                createSectionLabel("Distribution Centers"),
                createButton("Add a Center", this::handleAddCenter),
                createButton("Delete Selected", this::handleRemoveCenter),
                new Separator(),
                createSectionLabel("Beneficiaries"),
                createButton("Add a Beneficiary", this::handleAddBeneficiary),
                createButton("Generate 20 Random (GPS)", this::handleAddRandom),
                createButton("Delete Selected", this::handleRemoveBeneficiary),
                new Separator(),
                createSectionLabel("Display"),
                createDisplayToggles(),
                new Separator(),
                createSectionLabel("Filter"),
                createFilters(),
                new Separator(),
                createSectionLabel("File Actions"),
                createButton("Import CSV", this::handleImportCsv),
                createButton("Save", this::handleSaveBinary),
                createButton("Load", this::handleLoadBinary)
        );
    }

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        return label;
    }

    private Button createButton(String text, Runnable action) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(e -> action.run());
        return button;
    }

    private VBox createDisplayToggles() {
        VBox box = new VBox(4);
        CheckBox chkVoronoi = new CheckBox("Voronoi Cells");
        chkVoronoi.setSelected(true);
        chkVoronoi.setOnAction(e -> mapCanvas.setShowVoronoi(chkVoronoi.isSelected()));

        CheckBox chkDelaunay = new CheckBox("Delaunay (Dashed)");
        chkDelaunay.setOnAction(e -> mapCanvas.setShowDelaunay(chkDelaunay.isSelected()));

        CheckBox chkLinks = new CheckBox("Beneficiary Links");
        chkLinks.setSelected(true);
        chkLinks.setOnAction(e -> mapCanvas.setShowLinks(chkLinks.isSelected()));

        CheckBox chkCircumcircles = new CheckBox("Circumscribed Circles");
        chkCircumcircles.setOnAction(e -> mapCanvas.setShowCircumcircles(chkCircumcircles.isSelected()));

        box.getChildren().addAll(chkVoronoi, chkDelaunay, chkLinks, chkCircumcircles);
        return box;
    }

    private VBox createFilters() {
        VBox box = new VBox(4);
        ToggleGroup group = new ToggleGroup();

        RadioButton rbAll = new RadioButton("All Aid Types");
        rbAll.setToggleGroup(group);
        rbAll.setSelected(true);
        rbAll.setOnAction(e -> mapCanvas.setFilterType(null));

        RadioButton rbFood = new RadioButton("Food (FOOD)");
        rbFood.setToggleGroup(group);
        rbFood.setOnAction(e -> mapCanvas.setFilterType(AidType.FOOD));

        RadioButton rbCare = new RadioButton("Care (CARE)");
        rbCare.setToggleGroup(group);
        rbCare.setOnAction(e -> mapCanvas.setFilterType(AidType.CARE));

        RadioButton rbClothing = new RadioButton("Clothing (CLOTHING)");
        rbClothing.setToggleGroup(group);
        rbClothing.setOnAction(e -> mapCanvas.setFilterType(AidType.CLOTHING));

        box.getChildren().addAll(rbAll, rbFood, rbCare, rbClothing);
        return box;
    }

    private void updateAfterChange() {
        charityMap.recompute();
        mapCanvas.refreshMap();
        detailsPanel.updateStatistics(charityMap);
        detailsPanel.clearDetails();
    }

    private void handleAddCenter() {
        try {
            TextInputDialog d = new TextInputDialog("Restos du Coeur, FOOD, 49.040, 2.050");
            d.setTitle("Add a Center");
            d.setHeaderText("Format: Name, Type (FOOD/CARE/CLOTHING), Latitude, Longitude");
            d.showAndWait().ifPresent(val -> {
                String[] parts = val.split(",");
                if (parts.length < 4) throw new IllegalArgumentException("Invalid format.");
                String name = parts[0].trim();
                AidType type;
                try {
                    type = AidType.valueOf(parts[1].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid aid type '" + parts[1].trim() + "'. Please use CLOTHING, FOOD, or CARE.");
                }
                double lat = Double.parseDouble(parts[2].trim().replace(',', '.'));
                double lon = Double.parseDouble(parts[3].trim().replace(',', '.'));

                if (lat < GeoProjection.LAT_MIN || lat > GeoProjection.LAT_MAX ||
                    lon < GeoProjection.LON_MIN || lon > GeoProjection.LON_MAX) {
                    throw new IllegalArgumentException("Out of Cergy bounds.");
                }
                Point p = GeoProjection.toPixel(lat, lon);
                charityMap.addCenter(new DistributionCenter(p, new Association(name, type)));
                updateAfterChange();
            });
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    private void handleRemoveCenter() {
        DistributionCenter sel = mapCanvas.getSelectedCenter();
        if (sel != null) {
            charityMap.removeCenter(sel);
            mapCanvas.setSelectedCenter(null);
            updateAfterChange();
        } else {
            showError("Error", "Please select a center on the map.");
        }
    }

    private void handleAddBeneficiary() {
        try {
            TextInputDialog d = new TextInputDialog("FOOD, 49.040, 2.050");
            d.setTitle("Add a Beneficiary");
            d.setHeaderText("Format: Need (FOOD/CARE/CLOTHING), Latitude, Longitude");
            d.showAndWait().ifPresent(val -> {
                String[] parts = val.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Invalid format.");
                AidType need;
                try {
                    need = AidType.valueOf(parts[0].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid aid type '" + parts[0].trim() + "'. Please use CLOTHING, FOOD, or CARE.");
                }
                double lat = Double.parseDouble(parts[1].trim().replace(',', '.'));
                double lon = Double.parseDouble(parts[2].trim().replace(',', '.'));

                if (lat < GeoProjection.LAT_MIN || lat > GeoProjection.LAT_MAX ||
                    lon < GeoProjection.LON_MIN || lon > GeoProjection.LON_MAX) {
                    throw new IllegalArgumentException("Out of Cergy bounds.");
                }
                Point p = GeoProjection.toPixel(lat, lon);
                charityMap.addBeneficiary(new Beneficiary(p, need));
                updateAfterChange();
            });
        } catch (Exception e) {
            showError("Error", e.getMessage());
        }
    }

    private void handleAddRandom() {
        RandomGenerator gen = new RandomGenerator(800, 600);
        double marginLat = (GeoProjection.LAT_MAX - GeoProjection.LAT_MIN) * 0.02;
        double marginLon = (GeoProjection.LON_MAX - GeoProjection.LON_MIN) * 0.02;
        gen.addRandomBeneficiariesInBounds(charityMap, 20, 
            GeoProjection.LON_MIN + marginLon, GeoProjection.LON_MAX - marginLon, 
            GeoProjection.LAT_MIN + marginLat, GeoProjection.LAT_MAX - marginLat,
            GeoProjection::toPixel);
        updateAfterChange();
    }

    private void handleRemoveBeneficiary() {
        Beneficiary sel = mapCanvas.getSelectedBeneficiary();
        if (sel != null) {
            charityMap.removeBeneficiary(sel);
            mapCanvas.setSelectedBeneficiary(null);
            updateAfterChange();
        } else {
            showError("Error", "Please select a beneficiary.");
        }
    }

    private void handleImportCsv() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                int count = new CsvImporter().importCenters(charityMap, file.getAbsolutePath(), GeoProjection::toPixel);
                updateAfterChange();
                showInfo("Success", count + " centers imported.");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        }
    }

    private void handleSaveBinary() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map Files", "*.map"));
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                new MapIO().save(charityMap, file.getAbsolutePath());
                showInfo("Success", "Map saved.");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        }
    }

    private void handleLoadBinary() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Map Files", "*.map"));
        File file = chooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                CharityMap loaded = new MapIO().load(file.getAbsolutePath());
                charityMap.clear();
                loaded.getCenters().forEach(charityMap::addCenter);
                loaded.getBeneficiaries().forEach(charityMap::addBeneficiary);
                updateAfterChange();
                showInfo("Success", "Map loaded.");
            } catch (Exception e) {
                showError("Error", e.getMessage());
            }
        }
    }

    private void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
    
    private void showInfo(String title, String content) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}
