package com.charitymap.gui;

import com.charitymap.geometry.GeometryUtils;
import com.charitymap.geometry.Triangle;
import com.charitymap.geometry.VoronoiCell;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;

/**
 * Right-side details display panel.
 * <p>
 * This panel provides descriptive context, statistics, and analysis recommendations 
 * whenever a user interacts with map elements like beneficiaries, distribution centers, 
 * or Delaunay triangles.
 * </p>
 * * @author CharityMap team
 */
public class DetailsPanel extends VBox {

    /** Label displaying the section title or selected entity name. */
    private final Label titleLabel;

    /** Label containing detailed formatted information about the selected entity. */
    private final Label detailsLabel;

    /** Label at the bottom of the panel showcasing global map statistics. */
    private final Label statsLabel;

    /**
     * Creates a new DetailsPanel with labels and borders.
     */
    public DetailsPanel() {
        setPrefWidth(260);
        setMinWidth(260);
        setMaxWidth(260);
        setSpacing(10);
        setPadding(new Insets(15));
        setStyle("-fx-background-color: #f8fafc; -fx-border-color: #cbd5e1; -fx-border-width: 0 0 0 1px;");

        titleLabel = new Label("Properties");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        detailsLabel = new Label();
        detailsLabel.setWrapText(true);
        detailsLabel.setStyle("-fx-font-size: 12px;");

        statsLabel = new Label();
        statsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #475569;");

        getChildren().addAll(titleLabel, new Separator(), detailsLabel, new Separator(), statsLabel);
        clearDetails();
    }

    /**
     * Formats internal map coordinates into a readable GPS string.
     *
     * @param x the X coordinate in the map system
     * @param y the Y coordinate in the map system
     * @return a formatted representation of the GPS coordinates
     */
    private static String formatGps(double x, double y) {
        return GeoProjection.formatGps(x, y);
    }

    /**
     * Formats a raw distance value into a localized readable string (e.g., meters or kilometers).
     *
     * @param distance the distance value to format
     * @return a formatted distance string with its unit
     */
    private static String formatDistance(double distance) {
        return GeoProjection.formatDistance(distance);
    }

    /**
     * Resets details to a default placeholder message.
     */
    public void clearDetails() {
        titleLabel.setText("Details");
        detailsLabel.setText("Click on an element on the map.");
    }

    /**
     * Displays details of a selected distribution center.
     * <p>
     * Includes association information, type of aid, linked beneficiaries count, and 
     * associated Voronoi cell geometric metrics if available.
     * </p>
     *
     * @param center the selected distribution center
     * @param map    the global map container
     */
    public void showCenterDetails(DistributionCenter center, CharityMap map) {
        titleLabel.setText("Center #" + center.getId());
        String info = "Association: " + center.getAssociation().getName() + "\n"
                    + "Aid: " + center.getAidType() + "\n"
                    + "GPS: " + formatGps(center.getPosition().getX(), center.getPosition().getY()) + "\n"
                    + "Affected beneficiaries: " + center.getLinkedBeneficiaries().size() + "\n";

        VoronoiCell cell = null;
        for (VoronoiCell c : map.getAllCells()) {
            if (c.getCenter() == center) {
                cell = c;
                break;
            }
        }

        if (cell != null) {
            double areaM2 = cell.getArea() * 32.49;
            String areaStr = areaM2 >= 1_000_000 
                ? String.format("%.2f km²", areaM2 / 1_000_000.0) 
                : String.format("%.1f ha", areaM2 / 10000.0);
                
            info += "\n[Voronoi Statistics]\n"
                  + "Coverage: " + areaStr + "\n"
                  + "Average dist.: " + formatDistance(cell.getAverageTravelDistance()) + "\n"
                  + "Max dist.: " + formatDistance(cell.getMaxTravelDistance());
        }
        detailsLabel.setText(info);
    }

    /**
     * Displays details of a selected beneficiary.
     * <p>
     * Shows their specific need, position, and information regarding their assigned 
     * distribution center if one has been resolved.
     * </p>
     *
     * @param b the selected beneficiary
     */
    public void showBeneficiaryDetails(Beneficiary b) {
        titleLabel.setText("Beneficiary #" + b.getId());
        String info = "Need: " + b.getNeed() + "\n"
                    + "GPS: " + formatGps(b.getPosition().getX(), b.getPosition().getY()) + "\n";
        
        if (b.getAssignedCenter() != null) {
            info += "\nAssigned center:\n"
                  + b.getAssignedCenter().getAssociation().getName() + " #" + b.getAssignedCenter().getId() + "\n"
                  + "Distance: " + formatDistance(b.distanceToAssignedCenter());
        } else {
            info += "\nAssigned center: None";
        }
        detailsLabel.setText(info);
    }

    /**
     * Displays analysis details of a selected Delaunay triangle.
     * <p>
     * Computes real-world areas, lengths, load balance imbalances between the three vertices 
     * (distribution centers), and returns automated network optimization recommendations.
     * </p>
     *
     * @param t   the selected Delaunay triangle
     * @param map the global map container
     */
    public void showTriangleDetails(Triangle t, CharityMap map) {
        titleLabel.setText("Delaunay Triangle");
        
        DistributionCenter centerA = null, centerB = null, centerC = null;
        for (DistributionCenter c : map.getCenters()) {
            if (c.getPosition().distanceTo(t.getA()) < 0.01) centerA = c;
            if (c.getPosition().distanceTo(t.getB()) < 0.01) centerB = c;
            if (c.getPosition().distanceTo(t.getC()) < 0.01) centerC = c;
        }
        
        int bA = (centerA != null) ? centerA.getLinkedBeneficiaries().size() : 0;
        int bB = (centerB != null) ? centerB.getLinkedBeneficiaries().size() : 0;
        int bC = (centerC != null) ? centerC.getLinkedBeneficiaries().size() : 0;
        
        int maxB = Math.max(bA, Math.max(bB, bC));
        int minB = Math.min(bA, Math.min(bB, bC));
        int diff = maxB - minB;
        
        double areaPixels = GeometryUtils.polygonArea(List.of(t.getA(), t.getB(), t.getC()));
        double areaM2 = areaPixels * 32.49;
        String areaStr = areaM2 >= 1_000_000 
            ? String.format("%.2f km²", areaM2 / 1_000_000.0) 
            : String.format("%.1f ha", areaM2 / 10000.0);
            
        double dAB = t.getA().distanceTo(t.getB()) * 5.7;
        double dBC = t.getB().distanceTo(t.getC()) * 5.7;
        double dCA = t.getC().distanceTo(t.getA()) * 5.7;
        
        String density = (areaPixels < 20000) ? "Dense Zone" : "Deserted/Dispersed Zone";
        String recommendation = (areaPixels > 80000) 
            ? "Imbalance: large distances. Suggests adding a new center near the circumcenter to optimize coverage."
            : "Coverage is optimal for this area.";
            
        String info = "Vertices (Centers):\n"
                    + "- Center A (ID " + (centerA != null ? centerA.getId() : "?") + ") : " + bA + " beneficiaries\n"
                    + "- Center B (ID " + (centerB != null ? centerB.getId() : "?") + ") : " + bB + " beneficiaries\n"
                    + "- Center C (ID " + (centerC != null ? centerC.getId() : "?") + ") : " + bC + " beneficiaries\n\n"
                    + "Imbalance (Max - Min): " + diff + " beneficiaries\n"
                    + "Zone density: " + density + "\n"
                    + "Area: " + areaStr + "\n"
                    + "Side lengths:\n"
                    + "  A-B: " + String.format("%.0f m", dAB) + "\n"
                    + "  B-C: " + String.format("%.0f m", dBC) + "\n"
                    + "  C-A: " + String.format("%.0f m", dCA) + "\n\n"
                    + "Recommendation:\n" + recommendation;
                    
        detailsLabel.setText(info);
    }

    /**
     * Updates the global statistics label in the bottom of the panel.
     *
     * @param map the global map container
     */
    public void updateStatistics(CharityMap map) {
        statsLabel.setText(String.format("Global Stats:\n- %d Centers\n- %d Beneficiaries",
                map.getCenters().size(), map.getBeneficiaries().size()));
    }
}
