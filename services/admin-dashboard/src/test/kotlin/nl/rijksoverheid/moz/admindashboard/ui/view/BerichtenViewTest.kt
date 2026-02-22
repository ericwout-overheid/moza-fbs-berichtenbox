package nl.rijksoverheid.moz.admindashboard.ui.view

import kotlin.test.Test
import kotlin.test.assertEquals

class BerichtenViewTest {

    @Test
    fun `maskeerOntvanger maskeert BSN correct`() {
        assertEquals("*****6789", BerichtenView.maskeerOntvanger("123456789"))
    }

    @Test
    fun `maskeerOntvanger maskeert korte ID als sterren`() {
        assertEquals("****", BerichtenView.maskeerOntvanger("abc"))
    }

    @Test
    fun `maskeerOntvanger maskeert grenswaarde 4 tekens`() {
        assertEquals("****", BerichtenView.maskeerOntvanger("abcd"))
    }

    @Test
    fun `maskeerOntvanger maskeert 5 tekens met 1 ster`() {
        assertEquals("*2345", BerichtenView.maskeerOntvanger("12345"))
    }

    @Test
    fun `maskeerOntvanger maskeert lege string`() {
        assertEquals("****", BerichtenView.maskeerOntvanger(""))
    }
}
