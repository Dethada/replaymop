package replaymop.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.runtimeverification.rvpredict.config.Configuration;
import com.runtimeverification.rvpredict.metadata.Metadata;
import com.runtimeverification.rvpredict.log.EventType;
import com.runtimeverification.rvpredict.log.ReadonlyEventInterface;
import com.runtimeverification.rvpredict.trace.OrderedLoggedTraceReader;

public class DumpRVLog {

	Metadata metaData;
	OrderedLoggedTraceReader traceReader;

	boolean importantOnly = false;

	public DumpRVLog(File logFolder, boolean importantOnly) {
		// --predict $HOME/traces/account
		String[] args = new String[2];
		args[0] = "--predict";
		args[1] = logFolder.toString();
		Configuration config = Configuration.instance(args);
		metaData = Metadata.readFrom(config.getMetadataPath());
		traceReader = new OrderedLoggedTraceReader(config);
		this.importantOnly = importantOnly;
	}

	@Override
	public String toString() {
		String ret = "";
		for (int i = 1; metaData.getVariableSig(i) != null; i++){
			ret += (String.format("%d: %s", i, metaData.getVariableSig(i)));
			ret += "\n";
		}
		ret += ("--");
		ret += "\n";

		try {
			long index = 0;
			while (true) {
				ReadonlyEventInterface event = traceReader.readEvent();
				EventType eventType = event.getType();
				long full_addr = event.unsafeGetDataInternalIdentifier();
				int addr_L = (int) ((full_addr >> 32) & 0xFFFFFFFFL);
				int addr_R = (int) (full_addr & 0xFFFFFFFFL);

				if (!important(eventType) && importantOnly)
					continue;

				ret += index + "\t" + event + " " + metaData.getLocationSig(event.getLocationId())
						+ "\t" + addr_L + " " + addr_R;
				ret += "\n";
				// TODO: schedule, thread creation order
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	boolean important(EventType type) {
		switch (type) {
		case READ:
		case WRITE:
		case WRITE_LOCK:
		case WRITE_UNLOCK:
		case READ_LOCK:
		case READ_UNLOCK:
		case WAIT_RELEASE:
			// case WAIT_ACQ:
			// case START:
			// case PRE_JOIN:
		case JOIN_THREAD:
			// case CLINIT_ENTER:
			// case CLINIT_EXIT:
			// case BRANCH:
			return true;
		default:
			return false;
		}
	}



	static class Parameters {
		@Parameter(description = "Trace folder")
		public List<String> inputFolder;

		@Parameter(names = "-output", description = "Output file (standard output if not specified)")
		public String outputFile;

		@Parameter(names = "-important-only", description = "Dump important events only")
		public boolean importantOnly = false;
	}

	static public void main(String[] args) throws IOException {

		Parameters parameters = new Parameters();
		JCommander parameterParser;
		try {
			parameterParser = new JCommander(parameters, args);
		} catch (ParameterException pe) {
			System.err.println(pe.getMessage());
			return;
		}
		if (parameters.inputFolder == null
				|| !(new File(parameters.inputFolder.get(0))).isDirectory()) {
			parameterParser.usage();
			System.exit(1);
		}

		PrintStream out = System.out;
		if (parameters.outputFile != null)
			out = new PrintStream(new File(parameters.outputFile));

		File inputFolder = new File(parameters.inputFolder.get(0));

		DumpRVLog dump = new DumpRVLog(inputFolder, parameters.importantOnly);

		out.println(dump);

	}

}
