package com.cergy.charitymap.geometry;

import java.io.Serializable;

/**
 * The coverage zone of one distribution center: its Voronoi cell.
 * <p>
 * Every location inside this cell is closer to this center than to any
 * other center of the same aid type. The cell also carries the social
 * statistics that make this project meaningful for the ethics and
 * design scope: how many beneficiaries are covered, how far they have
 * to travel, and how loaded the center is.
 *
 * @author CharityMap team
 */
public class VoronoiCell implements Serializable {

}
