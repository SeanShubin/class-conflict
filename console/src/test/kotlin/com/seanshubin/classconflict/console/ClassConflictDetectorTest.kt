package com.seanshubin.classconflict.console

import com.seanshubin.classconflict.console.ApplicationTester
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClassConflictDetectorTest {
    private lateinit var tester: ApplicationTester

    @Before
    fun setup() {
        tester = ApplicationTester()
    }

    @After
    fun teardown() {
        tester.cleanup()
    }

    @Test
    fun `no conflicts when same class has same bytecode`() {
        val classContent = ApplicationTester.createSimpleClassFile("version1")
        tester.createJarWithClass("app1.jar", "com.example.MyClass", classContent)
        tester.createJarWithClass("app2.jar", "com.example.MyClass", classContent)

        val exitCode = tester.runApplication()

        assertEquals(0, exitCode)
        assertTrue(tester.outputContains("Total Errors: 0 of 0 errors allowed"))
        assertTrue(tester.outputContains("Classes Scanned: 2"))
    }

    @Test
    fun `detects conflict when same class has different bytecode`() {
        val classContent1 = ApplicationTester.createSimpleClassFile("version1")
        val classContent2 = ApplicationTester.createSimpleClassFile("version2")
        tester.createJarWithClass("app1.jar", "com.example.MyClass", classContent1)
        tester.createJarWithClass("app2.jar", "com.example.MyClass", classContent2)

        val exitCode = tester.runApplication()

        assertEquals(1, exitCode)
        assertTrue(tester.outputContains("Conflicting Classes: 1 (counted as errors)"))
        assertTrue(tester.outputContains("Total Errors: 1 of 0 errors allowed"))
    }

    @Test
    fun `no conflicts when different classes`() {
        val classContent1 = ApplicationTester.createSimpleClassFile("class1")
        val classContent2 = ApplicationTester.createSimpleClassFile("class2")
        tester.createJarWithClass("app1.jar", "com.example.ClassA", classContent1)
        tester.createJarWithClass("app2.jar", "com.example.ClassB", classContent2)

        val exitCode = tester.runApplication()

        assertEquals(0, exitCode)
        assertTrue(tester.outputContains("Total Errors: 0 of 0 errors allowed"))
        assertTrue(tester.outputContains("Classes Scanned: 2"))
    }

    @Test
    fun `no artifacts found when input directory is empty`() {
        val exitCode = tester.runApplication()

        assertEquals(1, exitCode)
        assertTrue(tester.outputContains("No artifacts found"))
    }
}
