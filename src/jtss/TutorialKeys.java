package jtss;

import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.exceptions.common.TcTssException;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcTssUuid;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIHash;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcTssContextFactory;
import iaik.tc.utils.logging.Log;
import iaik.tc.utils.misc.Utils;

public class TutorialKeys {

	/**
	 * Example that shows how to create a Signing key in the key hierarchy
	 */
	public static void keyCreation() {
		try {
			TcIContext context = new TcTssContextFactory().newContextObject();
			context.connect();
			// Set up the Storage Root KEY (SRK)
			TcIRsaKey srk = context
					.createRsaKeyObject(TcTssConstants.TSS_KEY_TSP_SRK);

			// set SRK policy
			TcIPolicy srkPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			srkPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_SHA1, TcBlobData
					.newByteArray(TcTssConstants.TSS_WELL_KNOWN_SECRET));
			srkPolicy.assignToObject(srk);

			// create an empty Singing Key object and define the security policy
			TcIRsaKey mySigningKey = context
					.createRsaKeyObject(TcTssConstants.TSS_KEY_SIZE_2048
							| TcTssConstants.TSS_KEY_TYPE_SIGNING
							| TcTssConstants.TSS_KEY_NON_VOLATILE
							| TcTssConstants.TSS_KEY_MIGRATABLE
							| TcTssConstants.TSS_KEY_AUTHORIZATION);

			// Define the object's authentication secrets
			TcBlobData keyUsageSecret = TcBlobData
					.newString("Password4Signatures");
			TcBlobData keyMigrationSecret = TcBlobData
					.newString("Password4KeyBackup");
			TcIPolicy keyUsgPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			TcIPolicy keyMigPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_MIGRATION);
			keyUsgPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyUsageSecret);
			keyMigPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyMigrationSecret);
			keyUsgPolicy.assignToObject(mySigningKey);
			keyMigPolicy.assignToObject(mySigningKey);

			// Instruct the TPM to use its tRNG to create the RSA key pair.
			// The TPM will protect the private key with the SRK
			mySigningKey.createKey(srk, null);

			// Store the created key on the HDD for later use
			TcTssUuid keyUUID1 = TcUuidFactory.getInstance()
					.generateRandomUuid();

			context.registerKey(mySigningKey,
					TcTssConstants.TSS_PS_TYPE_SYSTEM, keyUUID1,
					TcTssConstants.TSS_PS_TYPE_SYSTEM, TcUuidFactory
							.getInstance().getUuidSRK());

			Log.info("key1 registered in persistent system storage with "
					+ keyUUID1.toString());

			// Load the key into a key slot of the TPM
			mySigningKey.loadKey(srk);

			// Now my Signing Key can be used for cryptographic operations...
			System.out.println("My public Signing Key: " + Utils.getNL()
					+ mySigningKey.getPubKey().toHexString() + Utils.getNL());

			// create new hash object
			TcIHash hash = context
					.createHashObject(TcTssConstants.TSS_HASH_SHA1);

			// update
			TcBlobData data = TcBlobData.newString("Hello World");
			hash.updateHashValue(data);

			// sign
			TcBlobData signature = hash.sign(mySigningKey);

			// verify
			// If no exception is thrown, the signature could be done
			// successfully
			hash.verifySignature(signature, mySigningKey);

			// close context
			context.closeContext();
		} catch (TcTssException e) {
			e.printStackTrace();
		}

	}

}
