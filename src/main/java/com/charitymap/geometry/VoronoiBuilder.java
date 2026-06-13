package com.charitymap.geometry;

import com.charitymap.model.DistributionCenter;
import com.charitymap.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds Voronoi coverage zones out of a Delaunay triangulation, using
 * the duality between the two structures.
 * <p>
 * The key fact, also stated in the project slides, is that the Voronoi
 * vertices are exactly the circumcenters of the Delaunay triangles.
 * So this class does almost no geometry of its own: for every triangle
 * it computes the circumcenter, then it gives that circumcenter as a
 * corner to the cells of the three centers that form the triangle.
 *
 * @author CharityMap team
 */
public class VoronoiBuilder {

    /**
     * Default constructor for VoronoiBuilder.
     */
    public VoronoiBuilder() {}

    /**
     * Builds the list of coverage zones for a set of centers, given the
     * Delaunay triangulation of their positions.
     *
     * @param centers       the distribution centers (of one aid type)
     * @param triangulation the Delaunay triangulation of their positions
     * @return one coverage zone per center
     */
    public List<VoronoiCell> build(List<DistributionCenter> centers,
                                   List<Triangle> triangulation) {
        List<VoronoiCell> cells = new ArrayList<>();

        // Create one empty cell per center.
        for (DistributionCenter center : centers) {
            cells.add(new VoronoiCell(center));
        }

        // For every Delaunay triangle, the circumcenter is a Voronoi
        // vertex shared by the cells of the three triangle vertices.
        for (Triangle triangle : triangulation) {
            Point circumcenter = triangle.getCircumcenter();
            if (circumcenter == null) {
                // Degenerate triangle, nothing to add.
                continue;
            }
            for (VoronoiCell cell : cells) {
                Point centerPosition = cell.getCenter().getPosition();
                if (triangle.hasVertex(centerPosition)) {
                    cell.addCorner(circumcenter);
                }
            }
        }

        // Order the corners of every cell so the polygon is clean.
        for (VoronoiCell cell : cells) {
            cell.sortCorners();
        }
        return cells;
    }
}
