package uk.gov.phe.erdst.sc.awag.dao.template;

import java.util.Collection;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import uk.gov.phe.erdst.sc.awag.dao.AssessmentTemplateDao;
import uk.gov.phe.erdst.sc.awag.datamodel.AssessmentTemplate;
import uk.gov.phe.erdst.sc.awag.exceptions.AWNoSuchEntityException;
import uk.gov.phe.erdst.sc.awag.shared.test.TestConstants;
import uk.gov.phe.erdst.sc.awag.utils.GlassfishTestsHelper;

@Test(groups = {TestConstants.TESTNG_CONTAINER_TESTS_GROUP})
public class AssessmentTemplateDaoImplTest
{

    private AssessmentTemplateDao mAssessmentTemplateDao;

    @BeforeClass
    public static void setUpClass()
    {
        GlassfishTestsHelper.preTestSetup();
    }

    @BeforeMethod
    public void setUp() throws Exception
    {
        mAssessmentTemplateDao = (AssessmentTemplateDao) GlassfishTestsHelper
            .lookupMultiInterface("AssessmentTemplateDaoImpl", AssessmentTemplateDao.class);
    }

    @Test
    private void testGetTemplateById() throws AWNoSuchEntityException
    {
        // CS:OFF: MagicNumber
        Long templateId = 10000L;
        AssessmentTemplate template = mAssessmentTemplateDao.getEntityById(templateId);
        Assert.assertNotNull(template);
        Assert.assertEquals(template.getId(), templateId);
        // CS:ON
    }

    @Test
    private void testGetTemplates()
    {
        // CS:OFF: MagicNumber
        Collection<AssessmentTemplate> templates = mAssessmentTemplateDao.getEntities(0, 10);
        Assert.assertEquals(templates.size(), 3);
        // CS:ON
    }

    @Test
    public void testGetCountAssessmentsByAnimalId()
    {
        // CS:OFF: MagicNumber
        long assessmentTemplateCount = mAssessmentTemplateDao.getCountAssessmentTemplates();
        Assert.assertEquals(assessmentTemplateCount, 3);
        // CS:ON
    }

    @AfterClass
    public static void tearDownClass()
    {
        GlassfishTestsHelper.onTestFinished();
    }
}
