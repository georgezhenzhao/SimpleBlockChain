package com.dottorsoft.SimpleBlockchain.main;

import com.dottorsoft.SimpleBlockchain.main.core.Block;
import com.dottorsoft.SimpleBlockchain.main.core.Transaction;
import com.dottorsoft.SimpleBlockchain.main.core.TransactionOutput;
import com.dottorsoft.SimpleBlockchain.main.core.Wallet;
import com.dottorsoft.SimpleBlockchain.main.networking.ExecuteCommands;
import com.dottorsoft.SimpleBlockchain.main.util.ChainUtils;
import com.dottorsoft.SimpleBlockchain.main.util.Parameters;
import com.dottorsoft.SimpleBlockchain.main.util.StringUtil;

import java.security.Security;
import java.util.HashMap;

public class Main {


	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;

	public static void main(String[] args) {
		//add our blocks to the blockchain ArrayList:
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

		//Create wallets:
		walletA = new Wallet();
		walletB = new Wallet();
		Wallet wallet = new Wallet();

		//create genesis transaction, which sends 100 NoobCoin to walletA:
		genesisTransaction = new Transaction(wallet.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(wallet.privateKey);	 //manually sign the genesis transaction
		genesisTransaction.setTransactionId("0"); //manually set the transaction id
		genesisTransaction.getOutputs().add(new TransactionOutput(genesisTransaction.getReciepient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId())); //manually add the Transactions Output
		UTXOs.put(genesisTransaction.getOutputs().get(0).id, genesisTransaction.getOutputs().get(0)); //its important to store our first transaction in the UTXOs list.

		System.out.println("Creating and Mining Genesis block... ");
		Block genesis = new Block("0");
		genesis.addTransaction(genesisTransaction);
		addBlock(genesis);

		//testing
		Block block1 = new Block(genesis.getHash());
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		addBlock(block1);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		Block block2 = new Block(block1.getHash());
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		addBlock(block2);
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		Block block3 = new Block(block2.getHash());
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		block3.addTransaction(walletB.sendFunds( walletA.publicKey, 20));
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		System.out.println("WalletB's balance is: " + walletB.getBalance());
		addBlock(block3);


		if(!ChainUtils.isChainValid(Parameters.blockchain, genesisTransaction)){
			System.out.println("Not Valid Chain!!");
			return;
		}

		/*String blockchainJson = StringUtil.getJson(Parameters.blockchain);
		System.out.println("\nThe block chain: ");
		System.out.println(blockchainJson);*/

		ExecuteCommands server = new ExecuteCommands(8888);
		ExecuteCommands client = new ExecuteCommands(8889);
		client.connect("127.0.0.1", 8888);
		String block = client.getLastBlock();
		//System.out.println(block);
		Block block4 = Block.fromJsonToBlock(block);
		System.out.println("***************");
		System.out.println(StringUtil.getJson(block4));
	}



	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(Parameters.difficulty);
		Parameters.blockchain.add(newBlock);
	}
}