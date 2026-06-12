package com.charitymap.gui;

import com.charitymap.geometry.Triangle;
import com.charitymap.model.AidType;
import com.charitymap.model.Beneficiary;
import com.charitymap.model.CharityMap;
import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

import java.util.List;
import java.util.function.Consumer;

/**
 * Graphical Canvas component for rendering the map and handling mouse interactions.
 */
public class MapCanvas extends StackPane {

    final CharityMap charityMap;
    private final Canvas canvas;
    
    private Image mapImage;
    
    double offsetX = 0;
    double offsetY = 0;
    double scale = 1.0;
    private boolean userHasZoomedOrPanned = false;
    
    private double lastMouseX;
    private double lastMouseY;
    
    private double mouseWorldX = 0;
    private double mouseWorldY = 0;
    
    private boolean showVoronoi = true;
    private boolean showDelaunay = false;
    private boolean showLinks = true;
    private boolean showCircumcircles = false;
    
    AidType filterType = null;
    
    DistributionCenter selectedCenter;
    Beneficiary selectedBeneficiary;
    Triangle selectedTriangle;
    Object hoveredElement;
    
    private Consumer<Object> onSelectionChanged;

    /**
     * Creates a new MapCanvas instance and sets up listeners.
     *
     * @param charityMap the central model containing centers and beneficiaries
     */
    public MapCanvas(CharityMap charityMap) {
        this.charityMap = charityMap;
        this.canvas = new Canvas(800, 600);
        getChildren().add(canvas);
        
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener(e -> {
            adjustScaleAndOffsets();
            refreshMap();
        });
        canvas.heightProperty().addListener(e -> {
            adjustScaleAndOffsets();
            refreshMap();
        });
        
        try {
            mapImage = new Image(getClass().getResourceAsStream("/cergy_map.png"));
        } catch (Exception e) {
            System.err.println("Failed to load map image: " + e.getMessage());
        }
        
        canvas.setOnScroll(this::handleScroll);
        canvas.setOnMousePressed(this::handleMousePressed);
        canvas.setOnMouseDragged(this::handleMouseDragged);
        canvas.setOnMouseMoved(this::handleMouseMoved);
    }
    
    private double getMinScale() {
        double imgW = (mapImage != null) ? mapImage.getWidth() : 1000.0;
        double imgH = (mapImage != null) ? mapImage.getHeight() : 1000.0;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        if (canvasWidth <= 0 || canvasHeight <= 0) return 0.1;
        return Math.min(canvasWidth / imgW, canvasHeight / imgH);
    }
    
    private void handleScroll(ScrollEvent event) {
        double zoomFactor = 1.1;
        double oldScale = scale;
        double newScale = scale;
        
        if (event.getDeltaY() > 0) newScale *= zoomFactor;
        else newScale /= zoomFactor;
        
        double minScale = getMinScale();
        if (newScale < minScale) newScale = minScale;
        if (newScale > 5.0) newScale = 5.0; // Maximum zoom limit
        
        double mouseX = event.getX();
        double mouseY = event.getY();
        
        offsetX = mouseX - (mouseX - offsetX) * (newScale / oldScale);
        offsetY = mouseY - (mouseY - offsetY) * (newScale / oldScale);
        scale = newScale;
        
        userHasZoomedOrPanned = true;
        
        clampOffsets();
        refreshMap();
        event.consume();
    }
    
    private void handleMousePressed(MouseEvent event) {
        lastMouseX = event.getX();
        lastMouseY = event.getY();
        
        if (event.getClickCount() == 2) {
            userHasZoomedOrPanned = false;
            adjustScaleAndOffsets();
            refreshMap();
            event.consume();
            return;
        }
        
        if (event.getButton() == MouseButton.PRIMARY) {
            double[] worldPos = screenToWorld(event.getX(), event.getY());
            Point clickPoint = new Point(worldPos[0], worldPos[1]);
            
            DistributionCenter nearestCenter = null;
            double minCenterDist = Double.MAX_VALUE;
            
            for (DistributionCenter c : charityMap.getCenters()) {
                if (filterType != null && c.getAidType() != filterType) continue;
                double dist = clickPoint.distanceTo(c.getPosition());
                if (dist < minCenterDist) {
                    minCenterDist = dist;
                    nearestCenter = c;
                }
            }
            
            Beneficiary nearestBen = null;
            double minBenDist = Double.MAX_VALUE;
            
            for (Beneficiary b : charityMap.getBeneficiaries()) {
                if (filterType != null && b.getNeed() != filterType) continue;
                double dist = clickPoint.distanceTo(b.getPosition());
                if (dist < minBenDist) {
                    minBenDist = dist;
                    nearestBen = b;
                }
            }
            
            Triangle nearestTriangle = null;
            double minTriangleDist = Double.MAX_VALUE;
            
            List<Triangle> triangles = (filterType != null) ? charityMap.getTriangles(filterType) : charityMap.getAllTriangles();
            for (Triangle t : triangles) {
                Point cc = t.getCircumcenter();
                if (cc != null) {
                    double dist = clickPoint.distanceTo(cc);
                    if (dist < minTriangleDist) {
                        minTriangleDist = dist;
                        nearestTriangle = t;
                    }
                }
            }
            
            double selectRadius = 15 / scale;
            
            if (nearestCenter != null && minCenterDist <= selectRadius) {
                selectedCenter = nearestCenter;
                selectedBeneficiary = null;
                selectedTriangle = null;
            } else if (nearestBen != null && minBenDist <= selectRadius) {
                selectedBeneficiary = nearestBen;
                selectedCenter = null;
                selectedTriangle = null;
            } else if (nearestTriangle != null && minTriangleDist <= selectRadius) {
                selectedTriangle = nearestTriangle;
                selectedCenter = null;
                selectedBeneficiary = null;
            } else {
                selectedCenter = null;
                selectedBeneficiary = null;
                selectedTriangle = null;
            }
            notifySelection();
            refreshMap();
        }
    }
    
    private void handleMouseDragged(MouseEvent event) {
        if (event.getButton() == MouseButton.MIDDLE || event.getButton() == MouseButton.SECONDARY) {
            offsetX += event.getX() - lastMouseX;
            offsetY += event.getY() - lastMouseY;
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            userHasZoomedOrPanned = true;
            clampOffsets();
            refreshMap();
        } else if (event.getButton() == MouseButton.PRIMARY) {
            double[] worldPos = screenToWorld(event.getX(), event.getY());
            double wx = worldPos[0];
            double wy = worldPos[1];
            
            // Constrain dragging coordinates to the map boundaries [0, 1000]
            if (wx < 0) wx = 0;
            if (wx > 1000) wx = 1000;
            if (wy < 0) wy = 0;
            if (wy > 1000) wy = 1000;
            
            Point newPos = new Point(wx, wy);
            
            if (selectedCenter != null) {
                selectedCenter.setPosition(newPos);
                charityMap.recompute();
                notifySelection();
            } else if (selectedBeneficiary != null) {
                selectedBeneficiary.setPosition(newPos);
                charityMap.assignBeneficiaries();
                notifySelection();
            }
            lastMouseX = event.getX();
            lastMouseY = event.getY();
            refreshMap();
        }
    }

    private void clampOffsets() {
        double imgW = (mapImage != null) ? mapImage.getWidth() : 1000.0;
        double imgH = (mapImage != null) ? mapImage.getHeight() : 1000.0;
        double mapWidth = imgW * scale;
        double mapHeight = imgH * scale;
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();
        
        if (mapWidth < canvasWidth) {
            offsetX = (canvasWidth - mapWidth) / 2;
        } else {
            if (offsetX > 0) offsetX = 0;
            if (offsetX < canvasWidth - mapWidth) offsetX = canvasWidth - mapWidth;
        }
        
        if (mapHeight < canvasHeight) {
            offsetY = (canvasHeight - mapHeight) / 2;
        } else {
            if (offsetY > 0) offsetY = 0;
            if (offsetY < canvasHeight - mapHeight) offsetY = canvasHeight - mapHeight;
        }
    }
    
    private void adjustScaleAndOffsets() {
        double minScale = getMinScale();
        if (!userHasZoomedOrPanned) {
            scale = minScale;
        } else {
            if (scale < minScale) {
                scale = minScale;
            }
        }
        if (scale > 5.0) {
            scale = 5.0;
        }
        clampOffsets();
    }
    
    private void handleMouseMoved(MouseEvent event) {
        double[] worldPos = screenToWorld(event.getX(), event.getY());
        mouseWorldX = worldPos[0];
        mouseWorldY = worldPos[1];
        Point hoverPoint = new Point(worldPos[0], worldPos[1]);
        
        hoveredElement = null;
        double hoverRadius = 15 / scale;
        
        for (DistributionCenter c : charityMap.getCenters()) {
            if (filterType != null && c.getAidType() != filterType) continue;
            if (hoverPoint.distanceTo(c.getPosition()) <= hoverRadius) {
                hoveredElement = c;
                break;
            }
        }
        
        if (hoveredElement == null) {
            for (Beneficiary b : charityMap.getBeneficiaries()) {
                if (filterType != null && b.getNeed() != filterType) continue;
                if (hoverPoint.distanceTo(b.getPosition()) <= hoverRadius) {
                    hoveredElement = b;
                    break;
                }
            }
        }
        refreshMap();
    }
    
    private void notifySelection() {
        if (onSelectionChanged != null) {
            if (selectedCenter != null) onSelectionChanged.accept(selectedCenter);
            else if (selectedBeneficiary != null) onSelectionChanged.accept(selectedBeneficiary);
            else if (selectedTriangle != null) onSelectionChanged.accept(selectedTriangle);
            else onSelectionChanged.accept(null);
        }
    }
    
    /**
     * Translates coordinates from world map coordinates [0, 1000] to screen pixels.
     *
     * @param wx world x coordinate
     * @param wy world y coordinate
     * @return screen x and y coordinates
     */
    public double[] worldToScreen(double wx, double wy) {
        return new double[] { wx * scale + offsetX, wy * scale + offsetY };
    }
    
    /**
     * Translates coordinates from screen pixel coordinates to world coordinates [0, 1000].
     *
     * @param sx screen x coordinate
     * @param sy screen y coordinate
     * @return world x and y coordinates
     */
    public double[] screenToWorld(double sx, double sy) {
        return new double[] { (sx - offsetX) / scale, (sy - offsetY) / scale };
    }
    
    /**
     * Forcefully redraws everything on the Canvas (image, grid, and geometric overlays).
     */
    public void refreshMap() {
        adjustScaleAndOffsets();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        double imgW = (mapImage != null) ? mapImage.getWidth() : 1000.0;
        double imgH = (mapImage != null) ? mapImage.getHeight() : 1000.0;
        
        if (mapImage != null) {
            gc.drawImage(mapImage, offsetX, offsetY, imgW * scale, imgH * scale);
        } else {
            gc.setFill(Color.web("#f8fafc"));
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            gc.setStroke(Color.web("#e2e8f0"));
            gc.setLineWidth(1);
            double gridSpacing = 100 * scale;
            double startX = offsetX % gridSpacing;
            double startY = offsetY % gridSpacing;
            for (double x = startX; x < canvas.getWidth(); x += gridSpacing) {
                gc.strokeLine(x, 0, x, canvas.getHeight());
            }
            for (double y = startY; y < canvas.getHeight(); y += gridSpacing) {
                gc.strokeLine(0, y, canvas.getWidth(), y);
            }
        }
        
        try {
            gc.save();
            // Clip to map boundaries to prevent lines drawing outside the map image
            gc.beginPath();
            gc.rect(offsetX, offsetY, imgW * scale, imgH * scale);
            gc.clip();
            
            if (showVoronoi) MapPainter.drawVoronoi(gc, this);
            if (showDelaunay) MapPainter.drawDelaunay(gc, this);
            if (showLinks) MapPainter.drawLinks(gc, this);
            if (showCircumcircles) MapPainter.drawCircumcircles(gc, this);
            MapPainter.drawBeneficiaries(gc, this);
            MapPainter.drawCenters(gc, this);
            MapPainter.drawHighlights(gc, this);
            
            gc.restore();
            
            // GPS coordinates HUD in the bottom-right corner
            String coordsStr = GeoProjection.formatGps(mouseWorldX, mouseWorldY);
            gc.setFill(Color.web("rgba(15, 23, 42, 0.75)"));
            double hudWidth = 190;
            double hudHeight = 28;
            double hudX = canvas.getWidth() - hudWidth - 20;
            double hudY = canvas.getHeight() - hudHeight - 20;
            gc.fillRoundRect(hudX, hudY, hudWidth, hudHeight, 8, 8);
            
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Segoe UI", 11));
            gc.fillText(coordsStr, hudX + 12, hudY + 18);
        } catch (Exception e) {
            // Pas de crash
        }
    }
    
    /**
     * Sets whether to show Voronoi cells on the canvas.
     *
     * @param show true to show, false otherwise
     */
    public void setShowVoronoi(boolean show) { this.showVoronoi = show; refreshMap(); }

    /**
     * Sets whether to show Delaunay triangles on the canvas.
     *
     * @param show true to show, false otherwise
     */
    public void setShowDelaunay(boolean show) { this.showDelaunay = show; refreshMap(); }

    /**
     * Sets whether to show assignment links on the canvas.
     *
     * @param show true to show, false otherwise
     */
    public void setShowLinks(boolean show) { this.showLinks = show; refreshMap(); }

    /**
     * Sets whether to show circumscribed circles on the canvas.
     *
     * @param show true to show, false otherwise
     */
    public void setShowCircumcircles(boolean show) { this.showCircumcircles = show; refreshMap(); }

    /**
     * Sets the filter by aid type.
     *
     * @param type the aid type to filter by, or null for no filter
     */
    public void setFilterType(AidType type) { this.filterType = type; refreshMap(); }
    
    /**
     * Sets the selected center on the canvas.
     *
     * @param c the center to select
     */
    public void setSelectedCenter(DistributionCenter c) { this.selectedCenter = c; this.selectedBeneficiary = null; this.selectedTriangle = null; refreshMap(); }

    /**
     * Sets the selected beneficiary on the canvas.
     *
     * @param b the beneficiary to select
     */
    public void setSelectedBeneficiary(Beneficiary b) { this.selectedBeneficiary = b; this.selectedCenter = null; this.selectedTriangle = null; refreshMap(); }

    /**
     * Sets the selected triangle on the canvas.
     *
     * @param t the triangle to select
     */
    public void setSelectedTriangle(Triangle t) { this.selectedTriangle = t; this.selectedCenter = null; this.selectedBeneficiary = null; refreshMap(); }

    /**
     * Sets the consumer to be called when selection changes.
     *
     * @param consumer the selection consumer callback
     */
    public void setOnSelectionChanged(Consumer<Object> consumer) { this.onSelectionChanged = consumer; }

    /**
     * Gets the currently selected center.
     *
     * @return the selected center
     */
    public DistributionCenter getSelectedCenter() { return selectedCenter; }

    /**
     * Gets the currently selected beneficiary.
     *
     * @return the selected beneficiary
     */
    public Beneficiary getSelectedBeneficiary() { return selectedBeneficiary; }

    /**
     * Gets the currently selected triangle.
     *
     * @return the selected triangle
     */
    public Triangle getSelectedTriangle() { return selectedTriangle; }
}