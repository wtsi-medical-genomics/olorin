/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 *
 * @author jm20
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TestVCF.class, TestMapFile.class, TestFlowFile.class, TestVariant.class, TestPedFile.class, TestSampleCompare.class, TestVCFMeta.class})
public class OlorinTestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
}
