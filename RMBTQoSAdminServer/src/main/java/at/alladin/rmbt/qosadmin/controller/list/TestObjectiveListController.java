package at.alladin.rmbt.qosadmin.controller.list;

import at.alladin.rmbt.qosadmin.model.TestObjective;
import at.alladin.rmbt.qosadmin.model.types.QoSTestType;
import at.alladin.rmbt.qosadmin.repository.TestObjectiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Locale;

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/list/objective/*")
public class TestObjectiveListController {

    protected static final Logger logger = LoggerFactory.getLogger(TestObjectiveListController.class);

    @Autowired
    TestObjectiveRepository testObjectiveRepository;

    @RequestMapping("/list/objective")
    public String showTestObjectiveList(Locale locale, Model model) {
        return listTestObjective("", null, locale, model);
    }

    @RequestMapping("/list/objective/{searchParam}/{searchString:[a-zA-Z0-9._]*}")
    public String showTestObjectiveListByConcurrecnyGroup(@PathVariable("searchParam") String searchParam,
                                                          @PathVariable("searchString") String searchString, Locale locale, Model model) {
        return listTestObjective(searchParam, searchString, locale, model);
    }

    /**
     * @param listBy
     * @param parameter
     * @param locale
     * @param model
     * @return
     */
    private String listTestObjective(String listBy, Object parameter, Locale locale, Model model) {
        List<TestObjective> list = null;
        String title = "Test objectives";

        switch (listBy) {
            case "concurrencygroup":
                list = testObjectiveRepository.getByConcurrencyGroup(Long.valueOf((String) parameter), new Sort("uid"));
                title = "Test objectives with concurrency group: " + parameter;
                break;
            case "testclass":
                list = testObjectiveRepository.getByTestClass(Long.valueOf((String) parameter), new Sort("uid"));
                title = "Test objectives with test class: " + parameter;
                break;
            case "test":
                QoSTestType testType = QoSTestType.valueOf(((String) parameter).toUpperCase(Locale.US));
                list = testObjectiveRepository.getByQosTestType(testType, new Sort("uid"));
                title = "Test objectives with test type: " + testType.getValue();
                break;
            case "testserver":
                list = testObjectiveRepository.findByTestServer(Long.valueOf((String) parameter), new Sort("uid"));
                title = "Test objectives with test server: " + parameter;
                break;
            case "testdesc":
                list = testObjectiveRepository.findByTestDescriptionKey((String) parameter, new Sort("uid"));
                title = "Test objectives with test description key: " + parameter;
                break;
            case "testsummary":
                list = testObjectiveRepository.findByTestSummaryKey((String) parameter, new Sort("uid"));
                title = "Test objectives with test summary key: " + parameter;
                break;
            default:
                list = testObjectiveRepository.getByTestClassNot(new Long(0), new Sort("uid"));
                title = "All active test objectives (test_class != 0)";
        }

        if (list != null) {
            model.addAttribute("list", list);
        }
        model.addAttribute("title", title);

        return "list/objective";
    }
}
