package uk.ac.abdn.csd.stereos.util.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import uk.ac.abdn.csd.stereos.Experiment;
import uk.ac.abdn.csd.stereos.agents.Experience;
import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.agents.Profile;

public class GlobalAverageReporter implements Reporter {

    /**
     * String that will be appended to the output filename
     */
    public static final String id = "ga";
    // directory to write to
    private File dir;

    public GlobalAverageReporter(File expsDir) {
        dir = expsDir;
    }

    public void writeReport(Experiment[] e) throws IOException {
        System.out.print("Writing global average results...");
        // write the results to a file
        String profile = e[0].getProfileName();
        PrintWriter out = new PrintWriter(
                new BufferedWriter(new FileWriter(new File(dir, profile + "-" + id + ".csv"))));

        // out.append(getDescriptionString(e[0]));

        // print the header line
        out.append("time,");

        // create out colums for data to go in, one for each experiment
        List<List<Double>> dataColumns = new ArrayList<List<Double>>(e.length);

        // for each experiment, create a column
        // WITHIN THIS LOOP GOES THE CODE TO GENERATE THE RESULTS FOR EACH
        // EXPERIMENT
        // counter to ensure columns match up
        int k = 0;
        for (int j = 0; j < e.length; j++) {
            // create the header and datacolumn
            out.append("condition" + e[j].getCondition() + ",");
            // create the column
            List<Double> thisColumn = new ArrayList<Double>();

            List<Agent> trustors = e[j].getTrustors();
            List<Agent> trustorsRemaining = new ArrayList<Agent>(trustors);

            int icount = 1;
            // while there are some agents with experiences left
            while (!trustorsRemaining.isEmpty()) {
                // total experience value and count for this iteration (for
                // averaging)
                double sum = 0;
                double count = 0;
                // for each *remaining* trustor
                for (Agent t : trustors) {
                    // variables for the individual agent average
                    double tsum = 0;

                    // only tally trustors remamining
                    if (trustorsRemaining.contains(t)) {
                        List<Experience> eba = t.getTrustModel().getExperienceBase();
                        // if the agent has an 'ith' experience
                        if (icount < eba.size()) {
                            for (int i = 0; i < icount; i++) {
                                Experience ex = eba.get(i);

                                tsum += ex.getObservation();

                            }
                            // add the agent's interaction average to the total
                            // sum and increment the count
                            double aavg = tsum / icount;
                            sum += aavg;
                            count++;
                        } else {
                            // else this agent is out of experiences and should
                            // be removed
                            trustorsRemaining.remove(t);
                        }
                    }
                }
                if (count > 0) {
                    // calculate the average for this iteration
                    double avg = sum / count;
                    // add it to the datacolumn
                    thisColumn.add(avg);
                    // increment the interaction count
                    icount++;
                }
                // if no more agents have experiences, then set the complete
                // flag
                // if(agentsRemaining.isEmpty()) complete = true;
            }
            // now this experiment is complete, add the datacolumn to the parent
            // set
            dataColumns.add(k++, thisColumn);

        }

        out.append("\n");
        int i = 0;

        while (dataRemaining(dataColumns)) {
            // write the interaction step
            out.append(i + ",");
            // put the columns together
            for (List<Double> dataColumn : dataColumns) {
                out.append(dataColumn.remove(0) + ",");
                // munch off this item
                // dataColumn.remove(0);
            }
            i++;
            out.append("\n");
        }
        out.close();
        System.out.print("...completed.\n");
    }

    @SuppressWarnings("unused")
    private String getDescriptionString(Experiment e) {
        StringBuffer output = new StringBuffer();
        output.append("PARAMETERS\n");
        output.append("Profile," + e.getProfileName() + "\n");
        output.append("Runs," + e.getTimeSteps() + "\n");
        output.append("Agent count," + e.getAgentCount() + "\n");
        output.append("# teams," + e.getTeamCount() + "\n");
        output.append("# trustor per team," + e.getTrustorCount() + "\n");
        output.append("Team size," + e.getTeamSize() + "\n");
        output.append("Team lifetime," + e.getTeamLifeTime() + "\n");
        output.append("Noise feature count," + e.getNoiseFeatureCount() + "\n");
        output.append("Recency half-life," + e.getHalfLife() + "\n");
        output.append("Temperature," + e.getTemp() + "\n");
        output.append("Maximum rep. queries," + e.getMaxQueries() + "\n\n");

        output.append("AGENT PROFILES\n");
        output.append("name,mean,st.dev,count,features\n");
        List<Profile> profiles = e.getAgentProfiles();
        for (Profile profile : profiles) {
            output.append(profile.getId() + "," + profile.getDefaultMeanPerformance() + ","
                    + profile.getDefaultVariance() + "," + profile.getTrusteeCount() + ",");
            for (Entry<String, Double> feature : profile.getFeatures().entrySet()) {
                output.append(feature.getKey() + ":" + feature.getValue() + " ");
            }
            output.append("\n");
        }
        output.append("\n");
        return output.toString();
    }

    /**
     * Return false if any of the data columns are empty
     *
     * @param data
     * @return
     */
    private boolean dataRemaining(List<List<Double>> data) {
        boolean dataRemaining = true;
        for (List<Double> column : data) {
            if (column.isEmpty()) {
                dataRemaining = false;
            }
        }
        return dataRemaining;
    }
}
