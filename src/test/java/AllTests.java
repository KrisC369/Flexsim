import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ SimulatorTests.class, DomainTests.class, ScenarioTests.class, })
public class AllTests {
}
