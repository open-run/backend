package io.openur.domain.NFT.contract;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.tx.Contract;
import org.web3j.protocol.Web3j;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class NFTContract extends Contract {
    public static final String BINARY = "";

    protected NFTContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, gasProvider);
    }

    public static NFTContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider gasProvider) {
        return new NFTContract(contractAddress, web3j, transactionManager, gasProvider);
    }

    public RemoteCall<TransactionReceipt> mintToken(String to, BigInteger tokenId, BigInteger amount) {
        final Function function = new Function(
            "mintToken",
            Arrays.asList(new Address(to), new Uint256(tokenId), new Uint256(amount)),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> mintBatchTokens(String to, List<BigInteger> tokenIds, List<BigInteger> amounts) {
        DynamicArray<Uint256> tokenIdsArray = new DynamicArray<>(Uint256.class, 
            tokenIds.stream().map(Uint256::new).toList());
        DynamicArray<Uint256> amountsArray = new DynamicArray<>(Uint256.class,
            amounts.stream().map(Uint256::new).toList());
        
        final Function function = new Function(
            "mintBatchTokens",
            Arrays.asList(new Address(to), tokenIdsArray, amountsArray),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> setBaseURI(String newBaseURI) {
        final Function function = new Function(
            "setBaseURI",
            Arrays.asList(new Utf8String(newBaseURI)),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> safeTransferToken(String from, String to, BigInteger tokenId, BigInteger amount, byte[] data) {
        final Function function = new Function(
            "safeTransferToken",
            Arrays.asList(
                new Address(from),
                new Address(to),
                new Uint256(tokenId),
                new Uint256(amount),
                new DynamicBytes(data)
            ),
            Collections.emptyList()
        );
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> safeTransferBatchTokens(String from, String to, List<BigInteger> tokenIds, List<BigInteger> amounts, byte[] data) {
        DynamicArray<Uint256> tokenIdsArray = new DynamicArray<>(Uint256.class,
            tokenIds.stream().map(Uint256::new).toList());
        DynamicArray<Uint256> amountsArray = new DynamicArray<>(Uint256.class,
            amounts.stream().map(Uint256::new).toList());
        
        final Function function = new Function(
            "safeTransferBatchTokens",
            Arrays.asList(
                new Address(from),
                new Address(to),
                tokenIdsArray,
                amountsArray,
                new DynamicBytes(data)
            ),
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
