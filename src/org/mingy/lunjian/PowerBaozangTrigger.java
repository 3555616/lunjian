package org.mingy.lunjian;

public class PowerBaozangTrigger extends BaozangTrigger {

	private static final String DIG_MAP1 = "fly 1;dig;inn_op1;dig;n;dig;s;w;dig;e;e;dig;w;s;e;dig;e;dig;s;dig;w;dig;w;dig;s;dig;n;w;dig;e;e;n;n;dig;w;dig;e;e;dig;e;dig;n;dig;s;e;dig;e;dig;n;dig;s;s;dig;n;e;dig;w;w;w;w;w;n;dig;w;dig;e;n;dig;w;dig;e;e;dig;e;dig;w;w;n;dig;w;dig;e;e;dig;w;n;dig";
	
	@Override
	protected void process(CommandLine cmdline) {
		super.process(cmdline);
		if (!cmdline.isFighting()) {
			System.out.println("auto search map 1...");
			cmdline.executeCmd(DIG_MAP1);
		}
	}
}
