package replaymop.output;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import replaymop.output.aspectj.Aspect;
import replaymop.parser.rs.ReplaySpecification;

public class AspectJGenerator {
	ReplaySpecification spec;
	Aspect aspect;

	private AspectJGenerator(ReplaySpecification spec) {
		this.spec = spec;
		aspect = new Aspect(spec.fileName + "Aspect");
	}

	static <T> String printList(List<T> list) {
		return list.toString().replace("[", "{").replace("]", "}");
	}
	
	void generateThreadCreationOrder() {
		aspect.setParameter("THREAD_CREATION_ORDER",
				printList(spec.threadOrder));
	}

	void generateShareVariableAccessPointCut(){
		StringJoiner pointcuts = new StringJoiner(" ||\n\t\t\t");
		for (ReplaySpecification.Variable var : spec.shared){
			pointcuts.add(String.format("set(%s %s)", var.type, var.name));
			pointcuts.add(String.format("get(%s %s)", var.type, var.name));
		}
		aspect.setParameter("SHARED_VAR_ACCESS", pointcuts.toString());
	}
	
	void generateBeforeSyncPointCut() {
		StringJoiner pointcuts = new StringJoiner(" ||\n\t\t\t");
		if (spec.beforeMonitorEnter)
			pointcuts.add("lock()");
		for (String sync : spec.beforeSync){
			pointcuts.add(String.format("call(%s)", sync));
		}
		aspect.setParameter("BEFORE_SYNC_POINTCUTS", pointcuts.toString() + (pointcuts.length() == 0 ? "" : " ||"));
	}
	
	void generateAfterSyncPointCut() {
		StringJoiner pointcuts = new StringJoiner(" ||\n\t\t\t");
		if (spec.afterMonitorExit)
			pointcuts.add("unlock()");
		for (String sync : spec.afterSync){
			pointcuts.add(String.format("call(%s)", sync));
		}
		aspect.setParameter("AFTER_SYNC_POINTCUTS", pointcuts.toString());
	}
	
	void generateThreadSchedule() {
		List<Long> threads = new ArrayList<>();
		List<Integer> counts = new ArrayList<>();
		for (ReplaySpecification.ScheduleUnit unit : spec.schedule){
			threads.add(unit.thread);
			counts.add(unit.count);
		}
		aspect.setParameter("SCHEDULE_THERAD",
				printList(threads));
		aspect.setParameter("SCHEDULE_COUNT",
				printList(counts));
		
	}

	void startGeneration() {
		generateThreadCreationOrder();
		// generateVariableLocks(); //TODO: complete this part
		generateShareVariableAccessPointCut();
		generateBeforeSyncPointCut();
		generateAfterSyncPointCut();
		generateThreadSchedule();

	}

	public static Aspect generate(ReplaySpecification spec) {
		AspectJGenerator generator = new AspectJGenerator(spec);
		generator.startGeneration();
		return generator.aspect;
	}
}
