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

        Label title = new Label("Contrôles");
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        getChildren().addAll(
                title,
                new Separator(),
                createSectionLabel("Centres de distribution"),
                createButton("Ajouter un Centre", this::handleAddCenter),
                createButton("Supprimer sélection", this::handleRemoveCenter),
                new Separator(),
                createSectionLabel("Bénéficiaires"),
                createButton("Ajouter un Bénéficiaire", this::handleAddBeneficiary),
                createButton("Générer 20 aléatoires (GPS)", this::handleAddRandom),
                createButton("Supprimer sélection", this::handleRemoveBeneficiary),
                new Separator(),
                createSectionLabel("Affichage"),
                createDisplayToggles(),
                new Separator(),
                createSectionLabel("Filtre"),
                createFilters(),
                new Separator(),
                createSectionLabel("Actions Fichiers"),
                createButton("Importer CSV", this::handleImportCsv),
                createButton("Sauvegarder", this::handleSaveBinary),
                createButton("Charger", this::handleLoadBinary)
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
        CheckBox chkVoronoi = new CheckBox("Cellules Voronoi");
        chkVoronoi.setSelected(true);
        chkVoronoi.setOnAction(e -> mapCanvas.setShowVoronoi(chkVoronoi.isSelected()));

        CheckBox chkDelaunay = new CheckBox("Delaunay (Pointillés)");
        chkDelaunay.setOnAction(e -> mapCanvas.setShowDelaunay(chkDelaunay.isSelected()));

        CheckBox chkLinks = new CheckBox("Liaisons bénéficiaires");
        chkLinks.setSelected(true);
        chkLinks.setOnAction(e -> mapCanvas.setShowLinks(chkLinks.isSelected()));

        CheckBox chkCircumcircles = new CheckBox("Cercles circonscrits");
        chkCircumcircles.setOnAction(e -> mapCanvas.setShowCircumcircles(chkCircumcircles.isSelected()));

        box.getChildren().addAll(chkVoronoi, chkDelaunay, chkLinks, chkCircumcircles);
        return box;
    }

    private VBox createFilters() {
        VBox box = new VBox(4);
        ToggleGroup group = new ToggleGroup();

        RadioButton rbAll = new RadioButton("Toutes les aides");
        rbAll.setToggleGroup(group);
        rbAll.setSelected(true);
        rbAll.setOnAction(e -> mapCanvas.setFilterType(null));

        RadioButton rbFood = new RadioButton("Alimentation (FOOD)");
        rbFood.setToggleGroup(group);
        rbFood.setOnAction(e -> mapCanvas.setFilterType(AidType.FOOD));

        RadioButton rbCare = new RadioButton("Soins (CARE)");
        rbCare.setToggleGroup(group);
        rbCare.setOnAction(e -> mapCanvas.setFilterType(AidType.CARE));

        RadioButton rbClothing = new RadioButton("Vêtements (CLOTHING)");
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
            d.setTitle("Ajouter un Centre");
            d.setHeaderText("Format: Nom, Type (FOOD/CARE/CLOTHING), Latitude, Longitude");
            d.showAndWait().ifPresent(val -> {
                String[] parts = val.split(",");
                if (parts.length < 4) throw new IllegalArgumentException("Format invalide.");
                String name = parts[0].trim();
                AidType type = AidType.valueOf(parts[1].trim().toUpperCase());
                double lat = Double.parseDouble(parts[2].trim().replace(',', '.'));
                double lon = Double.parseDouble(parts[3].trim().replace(',', '.'));

                if (lat < GeoProjection.LAT_MIN || lat > GeoProjection.LAT_MAX ||
                    lon < GeoProjection.LON_MIN || lon > GeoProjection.LON_MAX) {
                    throw new IllegalArgumentException("Hors limites de Cergy.");
                }
                Point p = GeoProjection.toPixel(lat, lon);
                charityMap.addCenter(new DistributionCenter(p, new Association(name, type)));
                updateAfterChange();
            });
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void handleRemoveCenter() {
        DistributionCenter sel = mapCanvas.getSelectedCenter();
        if (sel != null) {
            charityMap.removeCenter(sel);
            mapCanvas.setSelectedCenter(null);
            updateAfterChange();
        } else {
            showError("Erreur", "Veuillez sélectionner un centre sur la carte.");
        }
    }

    private void handleAddBeneficiary() {
        try {
            TextInputDialog d = new TextInputDialog("FOOD, 49.040, 2.050");
            d.setTitle("Ajouter un Bénéficiaire");
            d.setHeaderText("Format: Besoin (FOOD/CARE/CLOTHING), Latitude, Longitude");
            d.showAndWait().ifPresent(val -> {
                String[] parts = val.split(",");
                if (parts.length < 3) throw new IllegalArgumentException("Format invalide.");
                AidType need = AidType.valueOf(parts[0].trim().toUpperCase());
                double lat = Double.parseDouble(parts[1].trim().replace(',', '.'));
                double lon = Double.parseDouble(parts[2].trim().replace(',', '.'));

                if (lat < GeoProjection.LAT_MIN || lat > GeoProjection.LAT_MAX ||
                    lon < GeoProjection.LON_MIN || lon > GeoProjection.LON_MAX) {
                    throw new IllegalArgumentException("Hors limites de Cergy.");
                }
                Point p = GeoProjection.toPixel(lat, lon);
                charityMap.addBeneficiary(new Beneficiary(p, need));
                updateAfterChange();
            });
        } catch (Exception e) {
            showError("Erreur", e.getMessage());
        }
    }

    private void handleAddRandom() {
        RandomGenerator gen = new RandomGenerator(800, 600);
        double marginLat = (GeoProjection.LAT_MAX - GeoProjection.LAT_MIN) * 0.02;
        double marginLon = (GeoProjection.LON_MAX - GeoProjection.LON_MIN) * 0.02;
        gen.addRandomBeneficiaries(charityMap, 20, 
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
            showError("Erreur", "Veuillez sélectionner un bénéficiaire.");
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
                showInfo("Succès", count + " centres importés.");
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
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
                showInfo("Succès", "Carte sauvegardée.");
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
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
                showInfo("Succès", "Carte chargée.");
            } catch (Exception e) {
                showError("Erreur", e.getMessage());
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
