/*
 * Copyright 2016 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package report;


import core.DTNHost;
import core.SimClock;
import core.SimError;

/**
 * Node energy level report. Reports the energy level of all 
 * (or only some, see {@link #REPORTED_NODES}) nodes every 
 * configurable-amount-of seconds (see {@link #GRANULARITY}).
 * Works only if all nodes use energy model; see 
 * {@link routing.util.EnergyModel}.
 */
public class EnergyLevelReport extends SnapshotReport {

	@Override
	protected void writeSnapshot(DTNHost h) {
		Double value = (Double)h.getComBus().
				getProperty(routing.util.EnergyModel.ENERGY_VALUE_ID);
			if (value == null) {
				throw new SimError("Host " + h +
						" is not using energy model");
			}
			write(SimClock.getIntTime()+"," +h.toString()+ "," + format(value));
	}

}
