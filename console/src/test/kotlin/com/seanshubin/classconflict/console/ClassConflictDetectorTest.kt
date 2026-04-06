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
        val jar1 = tester.createJarWithClass("app1.jar", "com.example.MyClass", classContent)
        val jar2 = tester.createJarWithClass("app2.jar", "com.example.MyClass", classContent)

        val exitCode = tester.runApplication(listOf(jar1, jar2))

        assertEquals(0, exitCode)
        assertTrue(tester.outputContains("No conflicts detected"))
        assertTrue(tester.outputContains("Classes scanned: 1"))
    }

    @Test
    fun `detects conflict when same class has different bytecode`() {
        val classContent1 = ApplicationTester.createSimpleClassFile("version1")
        val classContent2 = ApplicationTester.createSimpleClassFile("version2")
        val jar1 = tester.createJarWithClass("app1.jar", "com.example.MyClass", classContent1)
        val jar2 = tester.createJarWithClass("app2.jar", "com.example.MyClass", classContent2)

        val exitCode = tester.runApplication(listOf(jar1, jar2))

        assertEquals(1, exitCode)
        assertTrue(tester.outputContains("Conflicts found: 1"))
        assertTrue(tester.outputContains("com.example.MyClass"))
        assertTrue(tester.outputContains("app1.jar"))
        assertTrue(tester.outputContains("app2.jar"))
    }

    @Test
    fun `no conflicts when different classes`() {
        val classContent1 = ApplicationTester.createSimpleClassFile("class1")
        val classContent2 = ApplicationTester.createSimpleClassFile("class2")
        val jar1 = tester.createJarWithClass("app1.jar", "com.example.ClassA", classContent1)
        val jar2 = tester.createJarWithClass("app2.jar", "com.example.ClassB", classContent2)

        val exitCode = tester.runApplication(listOf(jar1, jar2))

        assertEquals(0, exitCode)
        assertTrue(tester.outputContains("No conflicts detected"))
        assertTrue(tester.outputContains("Classes scanned: 2"))
    }

    // TODO: Fix this test - createJarWithClass overwrites when same name used
    // @Test
    // fun `detects multiple conflicts`() {
    //     val class1v1 = ApplicationTester.createSimpleClassFile("class1-version1")
    //     val class1v2 = ApplicationTester.createSimpleClassFile("class1-version2")
    //     val class2v1 = ApplicationTester.createSimpleClassFile("class2-version1")
    //     val class2v2 = ApplicationTester.createSimpleClassFile("class2-version2")
    //
    //     val jar1 = tester.createJarWithClass("app1.jar", "com.example.ClassA", class1v1)
    //     tester.createJarWithClass("app1.jar", "com.example.ClassB", class2v1)
    //
    //     val jar2 = tester.createJarWithClass("app2.jar", "com.example.ClassA", class1v2)
    //     tester.createJarWithClass("app2.jar", "com.example.ClassB", class2v2)
    //
    //     val exitCode = tester.runApplication(listOf(jar1, jar2))
    //
    //     assertEquals(1, exitCode)
    //     assertTrue(tester.outputContains("Conflicts found: 2"))
    //     assertTrue(tester.outputContains("com.example.ClassA"))
    //     assertTrue(tester.outputContains("com.example.ClassB"))
    // }

    @Test
    fun `shows usage when no arguments provided`() {
        val exitCode = tester.runApplication(emptyList())

        assertEquals(1, exitCode)
        assertTrue(tester.outputContains("Usage"))
        assertTrue(tester.outputContains("class-conflict"))
    }
}
