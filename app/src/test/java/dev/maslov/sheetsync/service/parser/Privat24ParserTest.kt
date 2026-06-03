package dev.maslov.sheetsync.service.parser

import android.util.Log
import dev.maslov.sheetsync.model.Currency
import io.mockk.every
import io.mockk.mockkStatic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class Privat24ParserTest {

    private lateinit var parser: Privat24Parser

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0

        parser = Privat24Parser()
    }

    @Test
    fun `parse valid notification from settings example`() {
        val text = """
            ПриватБанк: +1000% Цифрові товари. Youtube premium
            1234 22:10
            Бал. 1111.00 грн
        """.trimIndent()

        val result = parser.parse(text)

        assertTrue(result.isPresent)
        val transaction = result.get()
        assertEquals("fiz", transaction.account)
        assertEquals("Цифрові товари. Youtube premium", transaction.description)
        assertEquals(1000.0, transaction.amount, 0.0)
        assertEquals(Currency.PERCENT, transaction.currency)
    }

    @Test
    fun `parse invalid notification`() {
        val text = "Some random text"
        val result = parser.parse(text)
        assertFalse(result.isPresent)
    }

    @Test
    fun `parse negative integer amount`() {
        val text = "ПриватБанк: -500₴ Купівля товару 12:34"
        val result = parser.parse(text)

        assertTrue(result.isPresent)
        assertEquals(-500.0, result.get().amount, 0.0)
        assertEquals(Currency.UAH, result.get().currency)
    }

    @Test
    fun `parse positive integer amount`() {
        val text = "ПриватБанк: +500₴ Купівля товару 12:34"
        val result = parser.parse(text)

        assertTrue(result.isPresent)
        assertEquals(500.0, result.get().amount, 0.0)
        assertEquals(Currency.UAH, result.get().currency)
    }

    @Test
    fun `parse double amount`() {
        val text = "ПриватБанк: -123.45$ Купівля товару 12:34"
        val result = parser.parse(text)

        assertTrue(result.isPresent)
        assertEquals(-123.45, result.get().amount, 0.0)
        assertEquals(Currency.USD, result.get().currency)
    }

    @Test
    fun `parse amount with space separator`() {
        val text = "ПриватБанк: -1 300.00€ Купівля товару 12:34"
        val result = parser.parse(text)

        assertTrue(result.isPresent)
        assertEquals(-1300.0, result.get().amount, 0.0)
        assertEquals(Currency.EUR, result.get().currency)
    }

    @Test
    fun `parse amount with comma separator`() {
        val text = "ПриватБанк: -100,50₴ Купівля товару 12:34"
        val result = parser.parse(text)

        assertTrue(result.isPresent)
        assertEquals(-100.50, result.get().amount, 0.0)
        assertEquals(Currency.UAH, result.get().currency)
    }
}
