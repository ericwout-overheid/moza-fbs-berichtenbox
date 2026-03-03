package nl.rijksoverheid.moz.berichtenmagazijn.config

import io.mockk.every
import io.mockk.mockk
import io.quarkus.security.identity.SecurityIdentity
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.Principal
import org.junit.jupiter.api.Assertions.assertEquals
import java.util.Optional

class AfzenderResolverTest {

    private val securityIdentity = mockk<SecurityIdentity>()
    private val validOin = "00000001234567890000"

    @Test
    fun `dev-mode returns configured OIN`() {
        val resolver = AfzenderResolver(securityIdentity, devMode = true, devAfzenderOin = Optional.of(validOin))
        assertEquals(validOin, resolver.resolve())
    }

    @Test
    fun `dev-mode throws when OIN not configured`() {
        val resolver = AfzenderResolver(securityIdentity, devMode = true, devAfzenderOin = Optional.empty())
        assertThrows<IllegalStateException> { resolver.resolve() }
    }

    @Test
    fun `dev-mode validates OIN format`() {
        val resolver = AfzenderResolver(securityIdentity, devMode = true, devAfzenderOin = Optional.of("invalid"))
        assertThrows<IllegalArgumentException> { resolver.resolve() }
    }

    @Test
    fun `production resolves OIN from client_id claim`() {
        val principal = mockk<Principal>()
        every { securityIdentity.principal } returns principal
        every { securityIdentity.getAttribute<String>("client_id") } returns validOin
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertEquals(validOin, resolver.resolve())
    }

    @Test
    fun `production falls back to azp when client_id absent`() {
        val principal = mockk<Principal>()
        every { securityIdentity.principal } returns principal
        every { securityIdentity.getAttribute<String>("client_id") } returns null
        every { securityIdentity.getAttribute<String>("azp") } returns validOin
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertEquals(validOin, resolver.resolve())
    }

    @Test
    fun `production falls back to principal name when claims absent`() {
        val principal = mockk<Principal> { every { name } returns validOin }
        every { securityIdentity.principal } returns principal
        every { securityIdentity.getAttribute<String>("client_id") } returns null
        every { securityIdentity.getAttribute<String>("azp") } returns null
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertEquals(validOin, resolver.resolve())
    }

    @Test
    fun `production throws when no principal`() {
        every { securityIdentity.principal } returns null
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertThrows<IllegalStateException> { resolver.resolve() }
    }

    @Test
    fun `validateOin rejects non-20-digit strings`() {
        val principal = mockk<Principal>()
        every { securityIdentity.principal } returns principal
        every { securityIdentity.getAttribute<String>("client_id") } returns "too-short"
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertThrows<IllegalArgumentException> { resolver.resolve() }
    }

    @Test
    fun `validateOin rejects 20 chars with letters`() {
        val principal = mockk<Principal>()
        every { securityIdentity.principal } returns principal
        every { securityIdentity.getAttribute<String>("client_id") } returns "0000000123456789ABCD"
        val resolver = AfzenderResolver(securityIdentity, devMode = false, devAfzenderOin = Optional.empty())
        assertThrows<IllegalArgumentException> { resolver.resolve() }
    }
}
