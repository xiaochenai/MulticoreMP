package jtss;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcITpm;
import iaik.tc.tss.api.tspi.TcTssContextFactory;
import iaik.tc.utils.misc.Utils;

import java.util.Vector;

public class TutorialReadPcr {

	/**
	 * Simple example to read the number of available PCRs from the TPM and reading their Values.
	 */
	public static void readPcrs() {
		try {
			TcIContext context = new TcTssContextFactory().newContextObject();
			context.connect();
			TcITpm tpm = context.getTpmObject();

			TcBlobData subCap = TcBlobData
					.newUINT32((int) TcTssConstants.TSS_TPMCAP_PROP_PCR);

			// get the number of available PCRs from the TPM
			long numPcrs = tpm.getCapabilityUINT32(
					TcTssConstants.TSS_TPMCAP_PROPERTY, subCap);
			Vector<String> msgs = new Vector<String>();

			for (int i = 0; i < numPcrs; i++) {
				StringBuffer buffer = new StringBuffer();
				if (i == 0)
					buffer.append(" 0" + i + ": ");
				else if (i < 10)
					buffer.append("0" + i + ": ");
				else
					buffer.append(i + ": ");

				// read the value of the PCR with index i
				TcBlobData pcrValue = tpm.pcrRead(i);
				buffer.append(pcrValue.toHexStringNoWrap());
				buffer.append(Utils.getNL());
				msgs.addElement(buffer.toString());
			}

			context.closeContext();

			System.out.println(Utils.getNL() + "TPM PCR VALUES: ");
			System.out.println(msgs + Utils.getNL());

		} catch (TcTssException e) {
			e.printStackTrace();
		}
	}
}
