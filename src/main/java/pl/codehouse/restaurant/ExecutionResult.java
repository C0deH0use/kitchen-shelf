package pl.codehouse.restaurant;

import java.util.Optional;

/**
 * Represents the result of a command execution.
 *
 * @param <T> The type of the value contained in the result.
 */
public record ExecutionResult<T>(Optional<T> value, RuntimeException exception) {

    /**
     * Creates a successful execution result with the given value.
     *
     * @param value The value of the successful execution.
     * @param <T> The type of the value.
     * @return An ExecutionResult instance representing a successful execution.
     */
    public static <T> ExecutionResult<T> success(T value) {
        return new ExecutionResult<>(Optional.of(value), null);
    }

    /**
     * Creates a failed execution result with the given exception.
     *
     * @param exception The exception that caused the failure.
     * @param <T> The type of the value (which will be absent in case of failure).
     * @return An ExecutionResult instance representing a failed execution.
     */
    public static <T> ExecutionResult<T> failure(RuntimeException exception) {
        return new ExecutionResult<>(Optional.empty(), exception);
    }

    /**
     * Checks if the execution was successful.
     *
     * @return true if the execution was successful, false otherwise.
     */
    public boolean isSuccess() {
        return value.isPresent();
    }

    /**
     * Checks if the execution failed.
     *
     * @return true if the execution failed, false otherwise.
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    public T handle() {
        return value.orElseThrow(() -> exception);
    }
}
