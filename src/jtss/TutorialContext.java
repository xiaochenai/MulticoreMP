package jtss;

import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcTssContextFactory;

public class TutorialContext {

	/**
	 * Simple "Hello World" example to demonstrate the creation of a TPM context.
	 */
	static void shortestjTSSProgram() {
		try {
			
			TcIContext context = new TcTssContextFactory().newContextObject();
			context.connect();

			System.out.println("TPM context connected.");
			
			// work with context here...
			System.out.println("Context is ready to use...");
			
			context.closeContext();
			
			System.out.println("TPM context closed.");
			
		} catch (TcTssException e) {
			e.printStackTrace();
			//handle error
		}
	}
}
