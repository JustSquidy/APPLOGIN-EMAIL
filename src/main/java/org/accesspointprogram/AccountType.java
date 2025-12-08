package org.accesspointprogram;

import java.util.Optional;    //  - Represents a value that may or may not be present.  (Similar to Rust's `Option<T>`.)





/** A datatype that represents the type ov an account. */
public enum AccountType {
	/** Manager. (Does something.) */
	Manager,
	/** Worker. (Does something else… subordinately.) */
	Worker;
	
	public static Optional<AccountType> fromString(String s) {
		return switch(s.toLowerCase()) {
			case "manager", "m" -> Optional.of(AccountType.Manager);
			case "worker", "w"  -> Optional.of(AccountType.Worker);
			
			default -> Optional.empty();
		};
	}
	
	@Override public String toString() {
		return switch(this) {
			case Manager -> "Manager";
			case Worker  -> "Worker";
		};
	}
}