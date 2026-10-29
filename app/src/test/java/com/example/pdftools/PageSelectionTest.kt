package com.example.pdftools

import org.junit.Assert.*
import org.junit.Test

class PageSelectionTest {
    @Test fun mixedRangesPreserveRequestedOrderAndDeduplicate() {
        assertEquals(listOf(4, 0, 1, 2), parsePages("5, 1-3, 2", 8))
    }
    @Test fun blankSelectsEveryPage() { assertEquals(listOf(0, 1, 2), parsePages("", 3)) }
    @Test fun rejectsPagesOutsideTheDocumentAndMalformedRanges() {
        listOf("0", "1-7", "3-1", "1,", "a", "99999999999999999").forEach { value ->
            assertTrue("Must reject $value", runCatching { parsePages(value, 5) }.isFailure)
        }
    }
    @Test fun filenamesCannotEscapeOutputDirectory() {
        listOf("../private", "a/b", "a\\b", "", "..", "x\u0000").forEach { value ->
            assertTrue(runCatching { safeOutputName(value, "pdf") }.isFailure)
        }
        assertEquals("My document.pdf", safeOutputName("My document", "pdf"))
        assertEquals("My document.PDF", safeOutputName("My document.PDF", "pdf"))
    }
}
