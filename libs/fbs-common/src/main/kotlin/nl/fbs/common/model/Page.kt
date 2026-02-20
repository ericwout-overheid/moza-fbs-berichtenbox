package nl.fbs.common.model

/**
 * Generieke pagina voor gepagineerde resultaten.
 *
 * @param T type van de resultaten
 * @property resultaten lijst van resultaten op deze pagina
 * @property pagina huidig paginanummer (0-gebaseerd)
 * @property paginaGrootte aantal resultaten per pagina
 * @property totaalPaginas totaal aantal pagina's
 * @property totaalElementen totaal aantal elementen over alle pagina's
 */
data class Page<T>(
    val resultaten: List<T>,
    val pagina: Int,
    val paginaGrootte: Int,
    val totaalPaginas: Int,
    val totaalElementen: Long
) {
    init {
        require(pagina >= 0) { "pagina moet >= 0 zijn" }
        require(paginaGrootte > 0) { "paginaGrootte moet > 0 zijn" }
        require(totaalPaginas >= 0) { "totaalPaginas moet >= 0 zijn" }
        require(totaalElementen >= 0) { "totaalElementen moet >= 0 zijn" }
    }

    companion object {
        /**
         * Berekent het totaal aantal pagina's op basis van het totaal aantal elementen
         * en de paginagrootte.
         */
        fun berekenTotaalPaginas(totaalElementen: Long, paginaGrootte: Int): Int {
            require(paginaGrootte > 0) { "paginaGrootte moet > 0 zijn" }
            if (totaalElementen == 0L) return 0
            return ((totaalElementen + paginaGrootte - 1) / paginaGrootte).toInt()
        }

        /**
         * Maakt een lege pagina aan.
         */
        fun <T> leeg(paginaGrootte: Int = 20): Page<T> = Page(
            resultaten = emptyList(),
            pagina = 0,
            paginaGrootte = paginaGrootte,
            totaalPaginas = 0,
            totaalElementen = 0
        )
    }
}
