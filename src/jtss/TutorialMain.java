package jtss;

import iaik.tc.utils.misc.Utils;

public class TutorialMain {

	/**
	 * The main class to run the jTSS tutorial programs.
	 * Tutorial Programs:
	 *   shortestjTSSProgram( ) using contextFactory
	 *   readPcrs( ) reading PCR values
	 *   readTPMSettigns( ) reading TPM settings and flag values
	 *   keyCreation( ) creating a key under the Storage Root Key (SRK)
	 *   encryptData( ) encrypt and decrypt data
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		System.out.println("Running tutorial examples.." + Utils.getNL());
		
		//short example to show how to use the context
		TutorialContext.shortestjTSSProgram();
		
		//find out number of PCRs and read their values 
		TutorialReadPcr.readPcrs();
		
		//read several TPM settings, flags, etc.
		TutorialReadTPMSettings.readTPMSettings();
		
		//create migratable signign key and sign data
		TutorialKeys.keyCreation();
		
		//kreate non migratable storeing key and encrypt/decrypt data
		TutorialEncryptData.encryptData();
		
		System.out.println(Utils.getNL() + "Tutorial complete.");

	}
}
