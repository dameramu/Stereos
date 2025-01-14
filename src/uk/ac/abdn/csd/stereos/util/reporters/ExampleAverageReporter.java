package uk.ac.abdn.csd.stereos.util.reporters;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;

import uk.ac.abdn.csd.stereos.Experiment;
import uk.ac.abdn.csd.stereos.agents.Experience;
import uk.ac.abdn.csd.stereos.agents.Agent;
import uk.ac.abdn.csd.stereos.agents.Profile;

public class ExampleAverageReporter implements Reporter
{

	/**
	 * String that will be appended to the output filename
	 */
	public static final String id = "ea";

	private Random random;

	// directory to write to
	private File dir;

	public ExampleAverageReporter(File expsDir)
	{
		dir = expsDir;
		random = new Random();
	}

	public void writeReport(Experiment[] e) throws IOException
	{
		System.out.print("Writing example agent average results...");
		// write the results to a file
		String profile = e[0].getProfileName();
		PrintWriter out = new PrintWriter(
				new BufferedWriter(new FileWriter(new File(dir, profile + "-" + id + ".csv"))));

		out.append(getDescriptionString(e[0]));

		// print the header line
		out.append("time,");

		// create out colums for data to go in, one for each experiment
		List<List<Double>> dataColumns = new ArrayList<List<Double>>(e.length);

		// for each experiment, create a column
		for (Experiment exp : e) {
			List<Agent> agents = exp.getTrustors();
			// pick a random agent
			Agent exampleAgent = agents.get(random.nextInt(agents.size()));
			// get and sort its EB
			List<Experience> eb = exampleAgent.getTrustModel().getExperienceBase();
			Collections.sort(eb);

			out.append("condition" + exp.getCondition() + ",");
			// create the column
			List<Double> thisColumn = new ArrayList<Double>();
			double i = 1;
			// double poss = 0;
			// double negs = 0;
			double sum = 0;
			for (Experience experience : eb) {
				// use the subjective evaluation component as metric
				sum += experience.getEvaluation();
				// calculate average
				double avg = sum / i++;
				// double ratio = poss/total;

				// add to the data column
				thisColumn.add(avg);
			}

			// attach the column to the total list
			dataColumns.add(thisColumn);
		}
		out.append("\n");
		int i = 0;
		while (dataRemaining(dataColumns)) {
			// write the interaction step
			out.append(i + ",");
			// put the columns together
			for (List<Double> dataColumn : dataColumns) {
				out.append(dataColumn.get(0) + ",");
				// munch off this item
				dataColumn.remove(0);
			}
			i++;
			out.append("\n");
		}
		out.close();
	}

	private String getDescriptionString(Experiment e)
	{
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
		System.out.print("...completed.\n");
		output.append("\n");
		return output.toString();
	}

	/**
	 * Return false if any of the data columns are empty
	 * 
	 * @param data
	 * @return
	 */
	private boolean dataRemaining(List<List<Double>> data)
	{
		boolean dataRemaining = true;
		for (List<Double> column : data) {
			if (column.isEmpty())
				dataRemaining = false;
		}
		return dataRemaining;
	}

}
