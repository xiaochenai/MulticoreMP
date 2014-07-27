package PC;

import javax.swing.JOptionPane;

import biz.source_code.base64Coder.Base64Coder;
import iaik.tc.tss.api.constants.tsp.TcTssConstants;
import iaik.tc.tss.api.structs.common.TcBlobData;
import iaik.tc.tss.api.structs.tsp.TcUuidFactory;
import iaik.tc.tss.api.tspi.TcIContext;
import iaik.tc.tss.api.tspi.TcIEncData;
import iaik.tc.tss.api.tspi.TcIPolicy;
import iaik.tc.tss.api.tspi.TcIRsaKey;
import iaik.tc.tss.api.tspi.TcTssContextFactory;

public class TPMDecData {

	/**
	 * unbind data to a hardware-protected TPM key.
	 */
	public static byte[] decryptData(byte[] data,String secret) {
		try {
			//Create context and connect to TPM.
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
            
			//retrieve the binding key using uuid
			TcIRsaKey keyPKRetrieved = context.getKeyByUuid(
					TcTssConstants.TSS_PS_TYPE_SYSTEM, TcUuidFactory.getInstance()
					.getUuidU1SK1());

			//input the binding key authentication secret here!
			//String TPMDECsecret = JOptionPane.showInputDialog("Please input TPM Binding Key Authentication Secret Here!");
			System.out.println("DEC Secret : " + Base64Coder.encodeLines(secret.getBytes()));
			TcBlobData keyPKLoadedSecret = TcBlobData.newString(secret);
			
			//create secret policy object and assigned to key object
			TcIPolicy keyPKLoadedPolicy = context
					.createPolicyObject(TcTssConstants.TSS_POLICY_USAGE);
			keyPKLoadedPolicy.setSecret(TcTssConstants.TSS_SECRET_MODE_PLAIN,
					keyPKLoadedSecret);
			keyPKLoadedPolicy.assignToObject(keyPKRetrieved);
            
			//load binding key
			keyPKRetrieved.loadKey(srk);
			
			//load binded AES Key
			TcIEncData BoundData = context
					.createEncDataObject(TcTssConstants.TSS_ENCDATA_BIND);
			
			TcBlobData boundData = TcBlobData.newByteArray(data);
			
			BoundData.setAttribData(
					TcTssConstants.TSS_TSPATTRIB_ENCDATA_BLOB,
					TcTssConstants.TSS_TSPATTRIB_ENCDATABLOB_BLOB, boundData);

			// unbind the AES Key for index encrypt
			TcBlobData unboundData = BoundData.unbind(keyPKRetrieved);
			System.out.println("DEC DATA:    " + unboundData.toStringASCII());
            
			//unregister the bond with TPM binding key and uuid U1SK1
			context.unregisterKey(TcTssConstants.TSS_PS_TYPE_SYSTEM,
					TcUuidFactory.getInstance()
					.getUuidU1SK1());
			context.closeContext();

			data = unboundData.asByteArray();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;

	}

}
