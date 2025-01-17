package pl.codehouse.restaurant;

/**
 * Represents the context of a request in the restaurant system.
 *
 * @param <T> The type of the request contained in the context.
 */
public record Context<T>(T request) {
    /**
     * Creates a new Context instance.
     *
     * @param request The request object to be contained in the context.
     */
    public Context {
        // Implicit constructor with validation if needed
    }
}
