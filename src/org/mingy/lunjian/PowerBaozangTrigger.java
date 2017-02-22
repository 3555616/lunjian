package org.mingy.lunjian;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PowerBaozangTrigger extends BaozangTrigger {

	@Override
	protected void process(CommandLine cmdline) {
		super.process(cmdline);
		if (!Boolean.parseBoolean(cmdline.getProperty("notify.webqq"))
				&& !cmdline.isFighting()) {
			try {
				String data = cmdline.load("maps/1.map");
				BufferedReader reader = new BufferedReader(new StringReader(
						data));
				List<String> steps = new ArrayList<String>();
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.length() > 0) {
						steps.add(line);
					}
				}
				reader.close();
				System.out.println("auto search map 1");
				cmdline.executeCmd("halt;fly 1");
				cmdline.walk(steps.toArray(new String[steps.size()]), "dig go",
						null, 100);
			} catch (Exception e) {
				System.out.println("map not found: 1");
			}
		}
	}
}
