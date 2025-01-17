package pl.codehouse.restaurant;

import reactor.core.publisher.Mono;

/**
 * Represents a command in the application that can be executed with a given context.
 *
 * @param <T> The type of the input context.
 * @param <R> The type of the result produced by the command.
 */
public interface Command<T, R> {

    boolean isApplicable(T t);

    /**
     * Executes the command with the given context.
     *
     * @param context The context containing the input data for the command.
     * @return A Mono that emits the ExecutionResult containing the result of the command execution.
     */
    Mono<ExecutionResult<R>> execute(T context);

}
