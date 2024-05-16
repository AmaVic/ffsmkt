package be.vamaralds.ffsmkt

/**
 * Base class for all error defined in the ffsmkt library. All errors are [Exception]s.
 * @param message the error message.
 */
sealed class FsmError(message: String): Exception(message) {
    /**
     * Error that occurs when there is no [Transition] defined for a given [State] and [EventType] in a [FsmDef].
     * @param fromState the state from which the transition was attempted.
     * @param onEventType the event type that was attempted to be applied.
     */
    class InvalidTransition(fromState: State, onEventType: EventType): FsmError("Event $onEventType cannot be handled in state $fromState")
}