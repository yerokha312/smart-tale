package dev.yerokha.smarttale.exception;

public class UserAlreadyEnabledException extends RuntimeException {
    public UserAlreadyEnabledException(String message) {
        super(message);
    }
}
