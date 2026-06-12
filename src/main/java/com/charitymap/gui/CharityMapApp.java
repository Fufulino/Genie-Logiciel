package com.charitymap.gui;

import com.charitymap.geometry.Triangle;
import com.charitymap.model.AidType;
import com.charitymap.model.Association;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;
import com.charitymap.service.RandomGenerator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main entry point for the JavaFX graphical application.
 */
public class CharityMapApp extends Application {

    /**
     * Creates a new CharityMapApp instance.
     */
    public CharityMapApp() {
        super();
    }

    @Override
    public void start(Stage primaryStage) {
        CharityMap charityMap = new CharityMap();
        loadDemoData(charityMap);

        MapCanvas mapCanvas = new MapCanvas(charityMap);
        DetailsPanel detailsPanel = new DetailsPanel();
        ControlPanel controlPanel = new ControlPanel(charityMap, mapCanvas, detailsPanel);

        mapCanvas.setOnSelectionChanged(selected -> {
            if (selected == null) {
                detailsPanel.clearDetails();
            } else if (selected instanceof DistributionCenter) {
                detailsPanel.showCenterDetails((DistributionCenter) selected, charityMap);
            } else if (selected instanceof Beneficiary) {
                detailsPanel.showBeneficiaryDetails((Beneficiary) selected);
            } else if (selected instanceof Triangle) {
                detailsPanel.showTriangleDetails((Triangle) selected, charityMap);
            }
        });

        BorderPane root = new BorderPane();
        root.setLeft(controlPanel);
        root.setCenter(mapCanvas);
        root.setRight(detailsPanel);

        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("CharityMap - Version Simplifiée");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(500);
        primaryStage.setScene(scene);
        primaryStage.show();

        mapCanvas.refreshMap();
        detailsPanel.updateStatistics(charityMap);
    }

    private void loadDemoData(CharityMap map) {
        Association restos = new Association("Restos du Coeur", AidType.FOOD);
        Association croix = new Association("Croix Rouge", AidType.CARE);
        Association vestiaire = new Association("Vestiaire Solidaire", AidType.CLOTHING);

        map.addCenter(new DistributionCenter(new Point(200, 300), restos));
        map.addCenter(new DistributionCenter(new Point(600, 250), restos));
        map.addCenter(new DistributionCenter(new Point(400, 150), restos));
        map.addCenter(new DistributionCenter(new Point(150, 500), restos));
        map.addCenter(new DistributionCenter(new Point(650, 550), restos));
        
        map.addCenter(new DistributionCenter(new Point(400, 600), croix));
        map.addCenter(new DistributionCenter(new Point(750, 700), croix));
        map.addCenter(new DistributionCenter(new Point(500, 400), croix));
        map.addCenter(new DistributionCenter(new Point(250, 650), croix));
        map.addCenter(new DistributionCenter(new Point(600, 100), croix));
        
        map.addCenter(new DistributionCenter(new Point(300, 500), vestiaire));
        map.addCenter(new DistributionCenter(new Point(100, 200), vestiaire));
        map.addCenter(new DistributionCenter(new Point(700, 350), vestiaire));
        map.addCenter(new DistributionCenter(new Point(450, 250), vestiaire));
        map.addCenter(new DistributionCenter(new Point(350, 750), vestiaire));

        RandomGenerator gen = new RandomGenerator(800, 600);
        double marginLat = (GeoProjection.LAT_MAX - GeoProjection.LAT_MIN) * 0.02;
        double marginLon = (GeoProjection.LON_MAX - GeoProjection.LON_MIN) * 0.02;
        gen.addRandomBeneficiariesInBounds(map, 80, 
            GeoProjection.LON_MIN + marginLon, GeoProjection.LON_MAX - marginLon, 
            GeoProjection.LAT_MIN + marginLat, GeoProjection.LAT_MAX - marginLat,
            GeoProjection::toPixel);

        map.recompute();
    }

    /**
     * Entry point of the JVM to launch the application.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}

