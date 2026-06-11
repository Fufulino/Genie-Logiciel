package com.charitymap.gui;

import com.charitymap.geometry.Triangle;
import com.charitymap.geometry.VoronoiCell;
import com.charitymap.model.AidType;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Helper class handling all the map rendering/drawing logic.
 */
public class MapPainter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MapPainter() {}

    /**
     * Returns the styling color for a given aid type.
     *
     * @param type the aid type to color
     * @return the JavaFX Color associated with the aid type
     */
    public static Color getColor(AidType type) {
        if (type == null) return Color.GRAY;
        switch (type) {
            case FOOD: return Color.web("#10b981");
            case CARE: return Color.web("#f59e0b");
            case CLOTHING: return Color.web("#8b5cf6");
            default: return Color.GRAY;
        }
    }

    /**
     * Draws the Voronoi cells on the canvas.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawVoronoi(GraphicsContext gc, MapCanvas canvas) {
        List<VoronoiCell> cells = canvas.charityMap.getAllCells();
        for (VoronoiCell cell : cells) {
            if (canvas.filterType != null && cell.getCenter().getAidType() != canvas.filterType) continue;
            List<Point> corners = cell.getCorners();
            if (corners.size() < 3) continue;
            
            double[] xPoints = new double[corners.size()];
            double[] yPoints = new double[corners.size()];
            for (int i = 0; i < corners.size(); i++) {
                double[] screen = canvas.worldToScreen(corners.get(i).getX(), corners.get(i).getY());
                xPoints[i] = screen[0];
                yPoints[i] = screen[1];
            }
            
            Color baseColor = getColor(cell.getCenter().getAidType());
            gc.setFill(Color.color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.15));
            gc.fillPolygon(xPoints, yPoints, corners.size());
            
            gc.setStroke(baseColor.darker());
            gc.setLineWidth(1);
            gc.strokePolygon(xPoints, yPoints, corners.size());
        }
    }

    /**
     * Draws the Delaunay triangulation lines.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawDelaunay(GraphicsContext gc, MapCanvas canvas) {
        List<Triangle> triangles = (canvas.filterType != null) ? canvas.charityMap.getTriangles(canvas.filterType) : canvas.charityMap.getAllTriangles();
        gc.setStroke(Color.web("#3b82f6"));
        gc.setLineWidth(1.5);
        gc.setLineDashes(5);
        
        for (Triangle t : triangles) {
            double[] pA = canvas.worldToScreen(t.getA().getX(), t.getA().getY());
            double[] pB = canvas.worldToScreen(t.getB().getX(), t.getB().getY());
            double[] pC = canvas.worldToScreen(t.getC().getX(), t.getC().getY());
            
            gc.strokeLine(pA[0], pA[1], pB[0], pB[1]);
            gc.strokeLine(pB[0], pB[1], pC[0], pC[1]);
            gc.strokeLine(pC[0], pC[1], pA[0], pA[1]);
        }
        gc.setLineDashes(null);
    }

    /**
     * Draws links between beneficiaries and their assigned centers.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawLinks(GraphicsContext gc, MapCanvas canvas) {
        gc.setStroke(Color.web("#94a3b8"));
        gc.setLineWidth(1);
        for (Beneficiary b : canvas.charityMap.getBeneficiaries()) {
            if (canvas.filterType != null && b.getNeed() != canvas.filterType) continue;
            if (b.getAssignedCenter() != null) {
                double[] from = canvas.worldToScreen(b.getPosition().getX(), b.getPosition().getY());
                double[] to = canvas.worldToScreen(b.getAssignedCenter().getPosition().getX(), b.getAssignedCenter().getPosition().getY());
                gc.strokeLine(from[0], from[1], to[0], to[1]);
            }
        }
    }

    /**
     * Draws the circumscribed circles of the Delaunay triangles.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawCircumcircles(GraphicsContext gc, MapCanvas canvas) {
        List<Triangle> triangles = (canvas.filterType != null) ? canvas.charityMap.getTriangles(canvas.filterType) : canvas.charityMap.getAllTriangles();
        gc.setStroke(Color.web("#be185d"));
        gc.setLineWidth(1.2);
        gc.setLineDashes(4);
        
        for (Triangle t : triangles) {
            Point center = t.getCircumcenter();
            if (center != null) {
                double radius = center.distanceTo(t.getA());
                double[] screenCenter = canvas.worldToScreen(center.getX(), center.getY());
                double screenRadius = radius * canvas.scale;
                gc.strokeOval(screenCenter[0] - screenRadius, screenCenter[1] - screenRadius, screenRadius * 2, screenRadius * 2);
            }
        }
        gc.setLineDashes(null);
    }

    /**
     * Draws the distribution centers on the map.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawCenters(GraphicsContext gc, MapCanvas canvas) {
        double radius = 8;
        for (DistributionCenter c : canvas.charityMap.getCenters()) {
            if (canvas.filterType != null && c.getAidType() != canvas.filterType) continue;
            double[] screen = canvas.worldToScreen(c.getPosition().getX(), c.getPosition().getY());
            gc.setFill(getColor(c.getAidType()));
            gc.fillOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
        }
    }

    /**
     * Draws the beneficiaries on the map.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawBeneficiaries(GraphicsContext gc, MapCanvas canvas) {
        double radius = 4;
        for (Beneficiary b : canvas.charityMap.getBeneficiaries()) {
            if (canvas.filterType != null && b.getNeed() != canvas.filterType) continue;
            double[] screen = canvas.worldToScreen(b.getPosition().getX(), b.getPosition().getY());
            gc.setFill(getColor(b.getNeed()));
            gc.fillOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1);
            gc.strokeOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
        }
    }

    /**
     * Draws highlights and hover rings for selected/hovered elements.
     *
     * @param gc     the graphics context to draw on
     * @param canvas the canvas state provider
     */
    public static void drawHighlights(GraphicsContext gc, MapCanvas canvas) {
        if (canvas.hoveredElement != null) {
            gc.setStroke(Color.web("#60a5fa"));
            gc.setLineWidth(2);
            Point p = null;
            double radius = 0;
            if (canvas.hoveredElement instanceof DistributionCenter) {
                p = ((DistributionCenter) canvas.hoveredElement).getPosition();
                radius = 11;
            } else if (canvas.hoveredElement instanceof Beneficiary) {
                p = ((Beneficiary) canvas.hoveredElement).getPosition();
                radius = 7;
            }
            if (p != null) {
                double[] screen = canvas.worldToScreen(p.getX(), p.getY());
                gc.strokeOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
            }
        }
        
        if (canvas.selectedCenter != null || canvas.selectedBeneficiary != null) {
            gc.setStroke(Color.web("#2563eb"));
            gc.setLineWidth(3);
            Point p = null;
            double radius = 0;
            if (canvas.selectedCenter != null) {
                p = canvas.selectedCenter.getPosition();
                radius = 12;
            } else if (canvas.selectedBeneficiary != null) {
                p = canvas.selectedBeneficiary.getPosition();
                radius = 8;
            }
            if (p != null) {
                double[] screen = canvas.worldToScreen(p.getX(), p.getY());
                gc.strokeOval(screen[0] - radius, screen[1] - radius, radius * 2, radius * 2);
            }
            
            // Highlight zone neighbors (other beneficiaries assigned to the same center)
            if (canvas.selectedBeneficiary != null) {
                DistributionCenter centerOfSelected = canvas.selectedBeneficiary.getAssignedCenter();
                if (centerOfSelected != null) {
                    gc.setStroke(Color.web("#60a5fa"));
                    gc.setLineWidth(1.5);
                    gc.setLineDashes(3);
                    for (Beneficiary b : canvas.charityMap.getBeneficiaries()) {
                        if (b != canvas.selectedBeneficiary && b.getAssignedCenter() == centerOfSelected) {
                            if (canvas.filterType != null && b.getNeed() != canvas.filterType) continue;
                            double[] screen = canvas.worldToScreen(b.getPosition().getX(), b.getPosition().getY());
                            gc.strokeOval(screen[0] - 6, screen[1] - 6, 12, 12);
                        }
                    }
                    gc.setLineDashes(null);
                }
            }
        }
        
        if (canvas.selectedTriangle != null) {
            gc.setStroke(Color.web("#2563eb"));
            gc.setLineWidth(2.5);
            double[] pA = canvas.worldToScreen(canvas.selectedTriangle.getA().getX(), canvas.selectedTriangle.getA().getY());
            double[] pB = canvas.worldToScreen(canvas.selectedTriangle.getB().getX(), canvas.selectedTriangle.getB().getY());
            double[] pC = canvas.worldToScreen(canvas.selectedTriangle.getC().getX(), canvas.selectedTriangle.getC().getY());
            gc.strokeLine(pA[0], pA[1], pB[0], pB[1]);
            gc.strokeLine(pB[0], pB[1], pC[0], pC[1]);
            gc.strokeLine(pC[0], pC[1], pA[0], pA[1]);
            
            Point cc = canvas.selectedTriangle.getCircumcenter();
            if (cc != null) {
                double[] screenCC = canvas.worldToScreen(cc.getX(), cc.getY());
                gc.strokeOval(screenCC[0] - 8, screenCC[1] - 8, 16, 16);
            }
        }
    }
}
