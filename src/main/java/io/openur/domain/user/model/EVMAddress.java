package io.openur.domain.user.model;

import org.web3j.abi.datatypes.Address;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Custom EVM address data type that provides validation and utility methods.
 * Wraps Web3j's Address class for blockchain operations while adding custom validation.
 */
public class EVMAddress {
    private static final Pattern ETH_ADDRESS_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");
    private static final int ETH_ADDRESS_LENGTH = 42; // 0x + 40 hex characters
    
    private final String address;
    private final Address web3jAddress;
    
    /**
     * Creates a new EVMAddress instance.
     * 
     * @param address The EVM address string
     * @throws IllegalArgumentException if the address is invalid
     */
    public EVMAddress(String address) {
        if (address == null || address.isEmpty()) {
            throw new IllegalArgumentException("EVM address cannot be null or empty");
        }
        
        if (address.length() != ETH_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("EVM address must be exactly 42 characters long");
        }
        
        if (!ETH_ADDRESS_PATTERN.matcher(address).matches()) {
            throw new IllegalArgumentException("Invalid EVM address format. Must start with '0x' and contain valid hex characters.");
        }
        
        this.address = address;
        this.web3jAddress = new Address(address);
    }
    
    /**
     * Creates an EVMAddress from a string, returning null if invalid.
     * 
     * @param address The address string
     * @return EVMAddress instance or null if invalid
     */
    public static EVMAddress ofNullable(String address) {
        try {
            return new EVMAddress(address);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Validates if a string is a valid EVM address.
     * 
     * @param address The address string to validate
     * @return true if valid, false otherwise
     */
    public static boolean isValid(String address) {
        if (address == null || address.isEmpty()) {
            return false;
        }
        
        return address.length() == ETH_ADDRESS_LENGTH && ETH_ADDRESS_PATTERN.matcher(address).matches();
    }
    
    /**
     * Gets the address as a string (preserves original case).
     * 
     * @return The address string
     */
    public String getValue() {
        return address;
    }
    
    /**
     * Gets the address in lowercase format (Web3j standard).
     * 
     * @return The address in lowercase
     */
    public String getValueLowerCase() {
        return address.toLowerCase();
    }
    
    /**
     * Gets the Web3j Address instance for blockchain operations.
     * 
     * @return Web3j Address instance
     */
    public Address getWeb3jAddress() {
        return web3jAddress;
    }
    
    /**
     * Gets the address in the format expected by Web3j.
     * 
     * @return The address in Web3j format
     */
    public String getWeb3jFormat() {
        return web3jAddress.getValue();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EVMAddress that = (EVMAddress) obj;
        return Objects.equals(address.toLowerCase(), that.address.toLowerCase());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(address.toLowerCase());
    }
    
    @Override
    public String toString() {
        return address;
    }
} 