package eryaz.software.basewms

import com.google.common.truth.Truth.assertThat
import eryaz.software.basewms.util.extensions.isValidPassword
import eryaz.software.basewms.util.extensions.isValidUserId
import org.junit.Test

class LoginTest {
    @Test
    fun `if the userName has one character return false`() {
        assertThat("a".isValidUserId()).isFalse()
    }

    @Test
    fun `if the password has greater or equal than 4 character return true`() {
        assertThat("1234".isValidPassword()).isTrue()
    }
    @Test
    fun `if the password has smaller than 4 character return false`() {
        assertThat("123".isValidPassword()).isFalse()
    }
}