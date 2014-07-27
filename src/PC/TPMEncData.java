package PC;

import javax.swing.JOptionPane;

import biz.source_code.base64Coder.Base64Coder;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIEncData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcTssContextFactory;
import iaik.tc.utils.logging.Log;

public class TPMEncData {

	/**
	 *bind data to a hardware-protected TPM key.
	 */
	public static byte[] encryptData(byte[] data,String secret) {
		try {
			//create a context and connect to TPM
			TcIContext context = new TcTssContextFactory().newContextObject();
			context.connect();

			// ..first configure, create and load key..
			// Set up the Storage Root KEY (SRK)
			TcIRsaKey srk = context
					.createRsaKeyObject(TcTssConstants.TSS_KEY_TSP_SRK);
            
			// set SRK policy
			TcIPolicy srkPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_SHA1, TcBlobData
					.newByteArray(TcTssConstants.TSS_WELL_KNOWN_SECRET));
			srkPolicy.assignToObject(srk);

			//create an empty binding Key object and define the security policy
			TcIRsaKey key = context
					.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
							| TcTssConstants.TSS_KEY_TYPE_BIND
							| TcTssConstants.TSS_KEY_NON_VOLATILE
							| TcTssConstants.TSS_KEY_NOT_MIGRATABLE
							| TcTssConstants.TSS_KEY_AUTHORIZATION);

			// Define the object's usage authentication secrets
			//String TPMENCsecret = JOptionPane.showInputDialog("Please create TPM Binding Key Authentication Secret Here!");
			System.out.println("ENC Secret : " + Base64Coder.encodeLines(secret.getBytes()));
			//create usage and migration secret policy object and assigned to binding key object
			TcBlobData keyUsageSecret = TcBlobData
					.newString(secret);
			TcBlobData keyMigrationSecret = TcBlobData
					.newByteArray(TcTssConstants.TSS_WELL_KNOWN_SECRET);
			TcIPolicy keyUsgPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			TcIPolicy keyMigPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
			keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyUsageSecret);
			keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyMigrationSecret);
			keyUsgPolicy.assignToObject(key);
			keyMigPolicy.assignToObject(key);

			// Instruct the TPM to use its tRNG to create the RSA key pair.
			// The TPM will protect the private key with the SRK
			key.createKey(srk, null);

			// Store the created key on the HDD for later use
			//get uuid
			TcTssUuid keyUUID1 = TcUuidFactory.getInstance()
					.getUuidU1SK1();
			/*
			context.unregisterKey(TcTssConstants.TSS_PS_TYPE_SYSTEM,
					TcUuidFactory.getInstance()
					.getUuidU1SK1());
			*/
			//register the binding key with keyUUID1
			context.registerKey(key, TcTssConstants.TSS_PS_TYPE_SYSTEM,
					keyUUID1, TcTssConstants.TSS_PS_TYPE_SYSTEM, TcUuidFactory
							.getInstance().getUuidSRK());
			
			

			Log.info("key1 registered in persistent system storage with "
					+ keyUUID1.toString());

			// Load the key into a key slot of the TPM
			key.loadKey(srk);

			// The data to bind to this TPM
			TcBlobData rawData = TcBlobData.newByteArray(data);
			
			System.out.println("RAW DATA:    " + rawData.toStringASCII());

			// create encryption data object
			TcIEncData encData = context
					.createEncDataObject(TcTssConstants.TSS_ENCDATA_BIND);

			// Perform encryption within the TPM
			encData.bind(key, rawData);

			// get bound data
			TcBlobData boundData = encData.getAttribData(
					TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB,
					TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB);

			context.closeContext();
			
			data = boundData.asByteArray();
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;

	}

}
