package ffsmkt

import be.vamaralds.ffsmkt.FsmDef
import be.vamaralds.ffsmkt.Transition
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class FsmDefTest {
    private val switchFsmDef = FsmDef(
        initialState = "off",
        transitions = setOf(
            Transition("off", "switchOn", "on"),
            Transition("on", "switchOff", "off")
        )
    )

    @Test
    fun `Check if Event can be applied in a given State (valid)`() {
        assertTrue(switchFsmDef.canHandleEventType("off", "switchOn"), "Event 'switchOn' can be applied in State 'off', but the function returned the opposite result")
    }

    @Test
    fun `Check if Event can be applied in a given State (invalid)`() {
        assertFalse(switchFsmDef.canHandleEventType("off", "switchOff"), "Event 'switchOff' cannot be applied in State 'off', but the function returned the opposite result")
    }

    @Test
    fun `Check if Event can be applied in unknown State`() {
        assertFalse(switchFsmDef.canHandleEventType("unknown", "switchOn"), "The state 'unknown' is not defined in the FSM, but the transition is considered valid")
    }

    @Test
    fun `Successfully retrieve next State after applying Event`() {
        switchFsmDef.nextState("off", "switchOn").fold(
            { fail("Failed to apply Event `switchOn` in State `off`, but it should have succeeded") },
            { nextState -> assertTrue(nextState == "on", "Expected next State to be 'on', but got '$nextState'") }
        )

        switchFsmDef.nextState("on", "switchOff").fold(
            { fail("Failed to apply Event `switchOff` in State `on`, but it should have succeeded") },
            { nextState -> assertTrue(nextState == "off", "Expected next State to be 'off', but got '$nextState'") }
        )
    }

    @Test
    fun `Fail to apply invalid Event`() {
        switchFsmDef.nextState("off", "switchOff").map {
            fail("Successfully applied invalid Event `switchOff` in State `off`, but it should have failed")
        }
    }
}