package org.overlord.sramp.atom.archive.expand.registry;

import java.util.Map;
import java.util.TreeMap;

/**
 * Hint information which can be used to determine the SRAMP Model type based on 
 * entries in the archive.
 * 
 * @author kstam
 *
 */
public class TypeHintInfo implements  Comparable<TypeHintInfo> {

	/**
	 * The priority is used so that more exotic matches can be given priority.
	 * i.e. META-INF/MANIFEST should be given a low priority so not every jar
	 * archive is matched as a JarArchive.
	 */
	Integer priority;
	/**
	 * A list of path entries, which can be used to determine the type of archive.
     * For example a Drools kiejar contains a META-INF/kmodule.xml entry, which has a
     * S-RAMP Model of KieJarArchive.
	 */
	Map<String,String> pathEntryHintMap;
	
	public TypeHintInfo() {
		super();
	}
		
	public TypeHintInfo(Integer priority, Map<String, String> pathEntryHintMap) {
		super();
		this.pathEntryHintMap = pathEntryHintMap;
		this.priority = priority;
	}
	
	public Map<String, String> getPathEntryHintMap() {
		if (pathEntryHintMap==null) pathEntryHintMap = new TreeMap<String,String>();
		return pathEntryHintMap;
	}
	public void setPathEntryHintMap(Map<String, String> pathEntryHintMap) {
		this.pathEntryHintMap = pathEntryHintMap;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Override
	public int compareTo(TypeHintInfo o) {
		final int BEFORE = -1;
	    final int EQUAL = 0;
	    final int AFTER = 1;

	    //this optimization is usually worthwhile, and can
	    //always be added
	    if (priority == o.getPriority()) return EQUAL;

	    //primitive numbers follow this form
	    if (priority < o.getPriority()) return BEFORE;
	    if (priority > o.getPriority()) return AFTER;
		
		return EQUAL;
	}
}
