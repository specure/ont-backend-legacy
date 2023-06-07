package at.alladin.rmbt.qosadmin.controller.list;

import at.alladin.rmbt.qosadmin.model.TestServer;
import at.alladin.rmbt.qosadmin.repository.TestServerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller("/list/testserver/*")
public class TestServerListController {

    protected static final Logger logger = LoggerFactory.getLogger(TestServerListController.class);

    @Autowired
    TestServerRepository testServerRepository;

    @RequestMapping("/list/testserver")
    public String showTestServerList(Locale locale, Model model) {
        return listTestServer("", null, locale, model);
    }

    @RequestMapping("/list/testserver/{searchParam}/{searchString:[a-zA-Z0-9._]*}")
    public String showTestServerListByConcurrecnyGroup(@PathVariable("searchParam") String searchParam,
                                                       @PathVariable("searchString") String searchString, Locale locale, Model model) {
        return listTestServer(searchParam, searchString, locale, model);
    }

    /**
     * @param listBy
     * @param parameter
     * @param locale
     * @param model
     * @return
     */
    private String listTestServer(String listBy, Object parameter, Locale locale, Model model) {
        List<TestServer> list = null;
        String title = "Test servers";

        switch (listBy) {
            case "uid":
                TestServer object = testServerRepository.findOne(Long.valueOf((String) parameter));
                if (object != null) {
                    list = new ArrayList<TestServer>();
                    list.add(object);
                }
                title = "Test server with UID: " + parameter;
                break;
            case "country":
                list = testServerRepository.getByCountry((String) parameter, new Sort("uid"));
                title = "Test servers with country: " + parameter;
                break;
            case "city":
                list = testServerRepository.getByCity((String) parameter, new Sort("uid"));
                title = "Test servers with city: " + parameter;
                break;
            default:
                list = testServerRepository.findAll(new Sort("uid"));
                title = "All test servers";
        }

        if (list != null) {
            model.addAttribute("list", list);
        }
        model.addAttribute("title", title);

        return "list/testserver";
    }
}
