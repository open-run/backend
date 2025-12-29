package io.openur.domain.NFT.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.tx.Contract;
import org.web3j.protocol.Web3j;
import org.web3j.crypto.Credentials;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class NFTContract extends Contract {
    public static final String BINARY = "";

    protected NFTContract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, credentials, gasProvider);
    }

    public static NFTContract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider gasProvider) {
        return new NFTContract(contractAddress, web3j, credentials, gasProvider);
    }

    // NFT 민팅
    public RemoteCall<TransactionReceipt> mintItemForTask(String to, BigInteger taskId, BigInteger amount) {
        final Function function = new Function(
            "mintItemForTask",
            Arrays.asList(new Address(to), new Uint256(taskId), new Uint256(amount)),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    // baseURI 설정 함수
    public RemoteCall<TransactionReceipt> setBaseURI(String uri) {
        final Function function = new Function(
            "setBaseURI",
            Arrays.asList(new Utf8String(uri)),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    //mint를 위해 taskID set
    public RemoteCall<TransactionReceipt> setTaskItem(BigInteger taskId, BigInteger itemId) {
        final Function function = new Function(
            "setTaskItem",
            Arrays.asList(new Uint256(taskId), new Uint256(itemId)),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> balanceOf(String account, BigInteger tokenId) {
        final Function function = new Function(
            "balanceOf",
            Arrays.asList(new Address(account), new Uint256(tokenId)),
            Arrays.asList(new TypeReference<Uint256>() {})
        );
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<String> uri(BigInteger tokenId) {
        final Function function = new Function(
            "uri",
            Arrays.asList(new Uint256(tokenId)),
            Arrays.asList(new TypeReference<Utf8String>() {})
        );
        return executeRemoteCallSingleValueReturn(function, String.class);
    }
}
