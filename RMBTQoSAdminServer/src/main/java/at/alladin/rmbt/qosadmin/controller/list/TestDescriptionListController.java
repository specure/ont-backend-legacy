package at.alladin.rmbt.qosadmin.controller.list;

import at.alladin.rmbt.qosadmin.model.TestDescription;
import at.alladin.rmbt.qosadmin.repository.TestDescriptionRepository;
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
@Controller("/list/testdesc/*")
public class TestDescriptionListController {

    protected static final Logger logger = LoggerFactory.getLogger(TestDescriptionListController.class);

    @Autowired
    TestDescriptionRepository testDescriptionRepository;

    @RequestMapping("/list/testdesc")
    public String showTestDescriptionList(Locale locale, Model model) {
        return listTestDescription("", null, locale, model);
    }

    @RequestMapping("/list/testdesc/lang/{languageCode}")
    public String showTestDescriptionListByLanguageCode(@PathVariable("languageCode") String languageCode, Locale locale, Model model) {
        return listTestDescription("lang", languageCode, locale, model);
    }

    @RequestMapping("/list/testdesc/key/{key:[a-zA-Z0-9._]*}")
    public String showTestDescriptionListByKey(@PathVariable("key") String key, Locale locale, Model model) {
        return listTestDescription("key", key, locale, model);
    }

    @RequestMapping("/list/testdesc/headkey/{key}")
    public String showTestDescriptionListByHeadKey(@PathVariable("key") String key, Locale locale, Model model) {
        return listTestDescription("headkey", key, locale, model);
    }

    /**
     * @param listBy
     * @param parameter
     * @param locale
     * @param model
     * @return
     */
    private String listTestDescription(String listBy, Object parameter, Locale locale, Model model) {
        List<TestDescription> list = null;
        String title = "All test descriptions";

        switch (listBy) {
            case "lang":
                list = testDescriptionRepository.findByLanguageCode((String) parameter, new Sort("key"));
                title = "Test descriptions with language code: " + parameter;
                break;
            case "key":
                list = testDescriptionRepository.findByKey((String) parameter, new Sort("key"));
                title = "Test descriptions with key: " + parameter;
                break;
            case "headkey":
                list = testDescriptionRepository.findByKeyStartsWith((String) parameter, new Sort("key"));
                title = "Test descriptions containing key starting with: " + parameter;
                break;
            default:
                list = testDescriptionRepository.findAll(new Sort("key"));
        }

        if (list != null) {
            model.addAttribute("list", list);
        }
        model.addAttribute("title", title);

        return "list/testdesc";
    }
}
