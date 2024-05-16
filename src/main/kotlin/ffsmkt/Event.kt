package be.vamaralds.ffsmkt

/**
 * An EventType represents a type of [Event]s. Each [Event] has a type.
 */
typealias EventType = String

/**
 * An Event is a request to perform a change in the system.
 * Events are immutable.
 *
 * @param E the type of the data carried by the event.
 * @property type the type of the event.
 */
data class Event<E>(val type: EventType, val data: E)
