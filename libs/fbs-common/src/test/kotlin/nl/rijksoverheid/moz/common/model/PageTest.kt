package nl.rijksoverheid.moz.common.model

import nl.rijksoverheid.moz.common.FbsConstants
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PageTest {

    @Test
    fun `lege pagina heeft geen resultaten`() {
        val page = Page.leeg<String>()
        assertTrue(page.resultaten.isEmpty())
        assertEquals(1, page.pagina)
        assertEquals(FbsConstants.DEFAULT_PAGE_SIZE, page.paginaGrootte)
        assertEquals(0, page.totaalPaginas)
        assertEquals(0L, page.totaalElementen)
    }

    @Test
    fun `lege pagina met aangepaste paginagrootte`() {
        val page = Page.leeg<String>(paginaGrootte = 50)
        assertEquals(50, page.paginaGrootte)
    }

    @Test
    fun `berekenTotaalPaginas voor 0 elementen`() {
        assertEquals(0, Page.berekenTotaalPaginas(0, 20))
    }

    @Test
    fun `berekenTotaalPaginas exact deelbaar`() {
        assertEquals(5, Page.berekenTotaalPaginas(100, 20))
    }

    @Test
    fun `berekenTotaalPaginas met rest`() {
        assertEquals(6, Page.berekenTotaalPaginas(101, 20))
    }

    @Test
    fun `berekenTotaalPaginas 1 element`() {
        assertEquals(1, Page.berekenTotaalPaginas(1, 20))
    }

    @Test
    fun `berekenTotaalPaginas gooit exception bij paginaGrootte 0`() {
        assertThrows<IllegalArgumentException> {
            Page.berekenTotaalPaginas(100, 0)
        }
    }

    @Test
    fun `pagina met paginanummer 0 gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Page(
                resultaten = emptyList<String>(),
                pagina = 0,
                paginaGrootte = 20,
                totaalPaginas = 0,
                totaalElementen = 0
            )
        }
    }

    @Test
    fun `pagina met negatief paginanummer gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Page(
                resultaten = emptyList<String>(),
                pagina = -1,
                paginaGrootte = 20,
                totaalPaginas = 0,
                totaalElementen = 0
            )
        }
    }

    @Test
    fun `pagina met paginaGrootte 0 gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Page(
                resultaten = emptyList<String>(),
                pagina = 1,
                paginaGrootte = 0,
                totaalPaginas = 0,
                totaalElementen = 0
            )
        }
    }

    @Test
    fun `pagina met negatief totaalPaginas gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Page(
                resultaten = emptyList<String>(),
                pagina = 1,
                paginaGrootte = 20,
                totaalPaginas = -1,
                totaalElementen = 0
            )
        }
    }

    @Test
    fun `pagina met negatief totaalElementen gooit exception`() {
        assertThrows<IllegalArgumentException> {
            Page(
                resultaten = emptyList<String>(),
                pagina = 1,
                paginaGrootte = 20,
                totaalPaginas = 0,
                totaalElementen = -1
            )
        }
    }

    @Test
    fun `pagina met resultaten`() {
        val page = Page(
            resultaten = listOf("a", "b", "c"),
            pagina = 1,
            paginaGrootte = 20,
            totaalPaginas = 1,
            totaalElementen = 3
        )
        assertEquals(3, page.resultaten.size)
        assertEquals("a", page.resultaten[0])
    }
}
