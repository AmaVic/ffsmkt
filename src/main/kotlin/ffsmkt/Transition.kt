package be.vamaralds.ffsmkt

/**
 * Represents a transition in a [FsmDef].
 * Transitions are immutable.
 *
 * @param fromState the state from which the transition is made.
 * @param onEventType the event type that triggers the transition.
 * @param toState the state to which the transition leads.
 */
data class Transition(val fromState: State, val onEventType: EventType, val toState: State) {
}