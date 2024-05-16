package be.vamaralds.ffsmkt

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure

/**
 * Represents a finite state machine (FSM) definition.
 * FSM definitions are immutable.
 *
 * @param initialState the initial state of the FSM.
 * @param transitions the transitions of the FSM.
 * @property transitionsMatrix a matrix representation of the transitions. Example: transitionsMatrix(state1)(eventType) = state2 means that when in state1 and receiving eventType, the FSM transitions to state2.
 */
class FsmDef(val initialState: State, transitions: Set<Transition> = emptySet()) {
    private val transitionsMatrix: Map<State, Map<EventType, State>> by lazy {
        transitions.groupBy { it.fromState }
            .mapValues {
                it.value
                    .associateBy { it.onEventType }
                    .mapValues { it.value.toState }
            }
    }

    /**
     * Check if a given [EventType] can be handled in a given [State].
     * @param currentState the current state of the FSM.
     * @param eventType the event type to be applied.
     * @return true if the [eventType] can be handled in the [currentState], false otherwise.
     */
    fun canHandleEventType(currentState: State, eventType: EventType): Boolean =
        transitionsMatrix[currentState]?.containsKey(eventType) ?: false

    /**
     * Attempts to retrieve the next after applying the given [eventType] in the [currentState].
     * @param currentState the current state of the FSM.
     * @param eventType the event type to be applied.
     * @return the next state if the transition is valid, an [FsmError.InvalidTransition] otherwise.
     */
    fun nextState(currentState: State, eventType: EventType): Either<FsmError.InvalidTransition, State> = either {
        ensure(canHandleEventType(currentState, eventType)) { FsmError.InvalidTransition(currentState, eventType) }
        transitionsMatrix[currentState]?.get(eventType)!!
    }
}