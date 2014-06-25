/*
 * Copyright (c) 2004-2014 Universidade do Porto - Faculdade de Engenharia
 * Laboratório de Sistemas e Tecnologia Subaquática (LSTS)
 * All rights reserved.
 * Rua Dr. Roberto Frias s/n, sala I203, 4200-465 Porto, Portugal
 *
 * This file is part of Neptus, Command and Control Framework.
 *
 * Commercial Licence Usage
 * Licencees holding valid commercial Neptus licences may use this file
 * in accordance with the commercial licence agreement provided with the
 * Software or, alternatively, in accordance with the terms contained in a
 * written agreement between you and Universidade do Porto. For licensing
 * terms, conditions, and further information contact lsts@fe.up.pt.
 *
 * European Union Public Licence - EUPL v.1.1 Usage
 * Alternatively, this file may be used under the terms of the EUPL,
 * Version 1.1 only (the "Licence"), appearing in the file LICENSE.md
 * included in the packaging of this file. You may not use this work
 * except in compliance with the Licence. Unless required by applicable
 * law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF
 * ANY KIND, either express or implied. See the Licence for the specific
 * language governing permissions and limitations at
 * https://www.lsts.pt/neptus/licence.
 *
 * For more information please see <http://lsts.fe.up.pt/neptus>.
 *
 * Author: zp
 * Jun 13, 2014
 */
package pt.lsts.neptus.mra.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

import pt.lsts.imc.EstimatedState;
import pt.lsts.imc.lsf.LsfIndex;
import pt.lsts.imc.lsf.LsfIterator;
import pt.lsts.neptus.types.coord.LocationType;

/**
 * @author zp
 *
 */
public class CorrectedPosition {

    private ArrayList<Position> positions = new ArrayList<>();
    
    public Position getPosition(double timestamp) {
        Position p = new Position();
        p.timestamp = timestamp;
        int pos = Collections.binarySearch(positions, p);
        System.out.println(pos);
        if (pos < 0)
            pos = -pos;
        if (pos >= positions.size())
            return positions.get(positions.size()-1);
        return positions.get(pos);
    }
    
    public CorrectedPosition(LsfIndex index) {
        LsfIterator<EstimatedState> it = index.getIterator(EstimatedState.class, 100l);

        Vector<EstimatedState> nonAdjusted = new Vector<>();
        Vector<LocationType> nonAdjustedLocs = new Vector<>();

        LocationType lastLoc = null;
        double lastTime = 0;

        for (EstimatedState es = it.next(); es != null; es = it.next()) {
            LocationType thisLoc = new LocationType();
            thisLoc.setLatitudeRads(es.getLat());
            thisLoc.setLongitudeRads(es.getLon());
            if (es.getDepth() > 0)
                thisLoc.setDepth(es.getDepth());
            if (es.getAlt() > 0)
                thisLoc.setDepth(-es.getAlt());
            thisLoc.translatePosition(es.getX(), es.getY(), 0);
            double speed = Math.sqrt(es.getU() * es.getU() + es.getV() * es.getV() + es.getW() * es.getW());

            thisLoc.convertToAbsoluteLatLonDepth();

            if (lastLoc != null) {
                double expectedDiff = speed * (es.getTimestamp() - lastTime);

                lastTime = es.getTimestamp();

                double diff = lastLoc.getHorizontalDistanceInMeters(thisLoc);
                
                if (diff < expectedDiff * 3) {
                    nonAdjusted.add(es);
                    nonAdjustedLocs.add(thisLoc);

                }
                else {
                    if (!nonAdjusted.isEmpty()) {
                        double[] adjustment = thisLoc.getOffsetFrom(lastLoc);
                        EstimatedState firstNonAdjusted = nonAdjusted.firstElement();
                        double timeOfAdjustment = es.getTimestamp() - firstNonAdjusted.getTimestamp();
                        double xIncPerSec = adjustment[0] / timeOfAdjustment;
                        double yIncPerSec = adjustment[1] / timeOfAdjustment;

                        for (int i = 0; i < nonAdjusted.size(); i++) {
                            EstimatedState adj = nonAdjusted.get(i);
                            LocationType loc = nonAdjustedLocs.get(i);
                            loc.translatePosition(xIncPerSec * (adj.getTimestamp() - firstNonAdjusted.getTimestamp()),
                                    yIncPerSec * (adj.getTimestamp() - firstNonAdjusted.getTimestamp()), 0);

                            loc.convertToAbsoluteLatLonDepth();
                            Position p = new Position();
                            p.alt = adj.getAlt();
                            p.depth = adj.getDepth();
                            p.lat = loc.getLatitudeDegs();
                            p.lon = loc.getLongitudeDegs();
                            p.timestamp = adj.getTimestamp();
                            positions.add(p);
                        }
                        nonAdjusted.clear();
                        nonAdjustedLocs.clear();
                        nonAdjusted.add(es);
                        nonAdjustedLocs.add(thisLoc);
                    }
                }
            }
            lastLoc = thisLoc;
            lastTime = es.getTimestamp();
        }
    }
    
    
    public static class Position implements Comparable<Position> {
        public double lat, lon, alt, depth, timestamp;
        @Override
        public int compareTo(Position o) {
            return ((Double)timestamp).compareTo(o.timestamp);
        }
    }
    
}