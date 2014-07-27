package jtss;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcTssVersion;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcITpm;
import iaik.tc.tss.api.tspi.TcTssContextFactory;
import iaik.tc.utils.misc.Utils;

public class TutorialReadTPMSettings {

	/**
	 * This Example shows how to read several TPM settings, flags, etc. from the TPM or context object.
	 */
	public static void readTPMSettings() {
		try {
			TcIContext context = new TcTssContextFactory().newContextObject();
			context.connect();
			TcITpm tpm = context.getTpmObject();

			TcTssVersion tpmVersion = tpm.getCapabilityVersion(
					TcTssConstants.TSS_TPMCAP_VERSION, null);
			System.out.println("TPM " + tpmVersion.toString());

			TcTssVersion tcsVersion = context.getCapabilityVersion(
					TcTssConstants.TSS_TCSCAP_VERSION, null);
			System.out.println("TCS " + tcsVersion.toString());

			TcTssVersion tspVersion = context.getCapabilityVersion(
					TcTssConstants.TSS_TSPCAP_VERSION, null);
			System.out.println("TSP " + tspVersion.toString());

			// get the name of the manufacturer
			TcBlobData subCap = TcBlobData
					.newUINT32((int) TcTssConstants.TSS_TPMCAP_PROP_MANUFACTURER);
			TcBlobData tpmMan = context.getTpmObject().getCapability(
					TcTssConstants.TSS_TPMCAP_PROPERTY, subCap);
			System.out.println("The manufacturer of your TPM is: "
					+ tpmMan.toStringASCII().substring(0,
							tpmMan.toStringASCII().length() - 1));

			
			// get TPM flags
			TcBlobData flags = tpm.getCapability(
					TcTssConstants.TSS_TPMCAP_FLAG, null);
			System.out.println("TPM Flags:" + flags.toHexString() + Utils.getNL());

			context.closeContext();
		} catch (TcTssException e) {
			e.printStackTrace();
		}
	}

}
