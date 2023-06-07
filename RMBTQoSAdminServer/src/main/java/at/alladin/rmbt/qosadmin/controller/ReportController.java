package at.alladin.rmbt.qosadmin.controller;

import at.alladin.rmbt.qosadmin.repository.TestDescriptionRepository;
import at.alladin.rmbt.qosadmin.repository.TestObjectiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Locale;

/**
 * as yet a pseudo controller for reports/lists (needs a lot more parametrization)
 *
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/report/*")
public class ReportController {

    protected static final Logger logger = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    TestDescriptionRepository testDescriptionRepository;

    @Autowired
    TestObjectiveRepository testObjectiveRepository;

    @RequestMapping("/report/list/objective")
    public String showResport(Locale locale, Model model) {
        List<Object[]> testServers = testObjectiveRepository.getAllTestServersWithCount();
        List<Object[]> testClasses = testObjectiveRepository.getAllTestClassesWithCount();
        List<Object[]> testDescKeys = testObjectiveRepository.getAllActiveDescKeysWithCount();
        List<Object[]> testSummaryKeys = testObjectiveRepository.getAllActiveSummaryKeysWithCount();
        List<Object[]> testGroups = testObjectiveRepository.getAllActiveTestGroupsWithCount();
        List<Object[]> testGroupsInactive = testObjectiveRepository.getAllInActiveTestGroupsWithCount();

        model.addAttribute("testServerList", testServers);
        model.addAttribute("testClassList", testClasses);
        model.addAttribute("testDescList", testDescKeys);
        model.addAttribute("testSummaryList", testSummaryKeys);
        model.addAttribute("testGroupList", testGroups);
        model.addAttribute("testGroupInactiveList", testGroupsInactive);
        model.addAttribute("title", "Detailed report for qos_test_objective");

        return "report";
    }
}
