/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package test.launcher;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    //TODO (scenario) Running test with TestLauncher API
    // instructions:
    //  - execute the first test method
    //  - verify that it passes and there's a 'Configuration cache entry stored' message in the console log
    //  - execute the second test
    //  - verify that it passes and there's a 'Configuration cache entry reused.' message in the console log

    @Test
    void appHasAGreeting() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }

    @Test
    void appHasAGreeting2nd() {
        App classUnderTest = new App();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
