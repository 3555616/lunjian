package org.mingy.lunjian;

import java.util.HashMap;
import java.util.Map;

public class TriggerManager {

	private static final Map<String, Class<? extends Trigger>> CLASSES = new HashMap<String, Class<? extends Trigger>>();

	private Map<String, Trigger> triggers = new HashMap<String, Trigger>();

	static {
		CLASSES.put("youxia", YouxiaTrigger.class);
		CLASSES.put("qinglong", QinglongTrigger.class);
		CLASSES.put("zhengxie", ZhengxieTrigger.class);
		CLASSES.put("guanfu", GuanfuTrigger.class);
		CLASSES.put("baozang", BaozangTrigger.class);
	}

	public void add(String name) {
		if (triggers.containsKey(name)) {
			return;
		}
		Class<? extends Trigger> klass = CLASSES.get(name);
		if (klass == null) {
			System.out.println("unknown trigger: " + name);
			return;
		}
		try {
			triggers.put(name, klass.newInstance());
			System.out.println("trigger " + name + " added");
		} catch (Exception e) {
			System.out.println("failed to create trigger");
		}
	}

	public void remove(String name) {
		Trigger trigger = triggers.remove(name);
		if (trigger != null) {
			System.out.println("trigger " + name + " removed");
		}
	}

	public void process(CommandLine cmdline, String message) {
		for (Trigger trigger : triggers.values()) {
			if (trigger.match(cmdline, message)) {
				break;
			}
		}
	}
}
