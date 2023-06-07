package at.alladin.rmbt.qosadmin.controller.list;

import at.alladin.rmbt.qosadmin.model.Settings;
import at.alladin.rmbt.qosadmin.repository.SettingsRepository;
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

/**
 * @author alladin-IT (lb@alladin.at)
 */
@Controller("/list/settings/*")
public class SettingsListController {

    protected static final Logger logger = LoggerFactory.getLogger(SettingsListController.class);

    @Autowired
    SettingsRepository settingsRepository;

    @RequestMapping("/list/settings")
    public String showSettingsList(Locale locale, Model model) {
        return listSettings("", null, locale, model);
    }

    @RequestMapping("/list/settings/{searchParam}/{searchString:[a-zA-Z0-9._]*}")
    public String showSettingsListBy(@PathVariable("searchParam") String searchParam,
                                     @PathVariable("searchString") String searchString, Locale locale, Model model) {
        return listSettings(searchParam, searchString, locale, model);
    }

    /**
     * @param listBy
     * @param parameter
     * @param locale
     * @param model
     * @return
     */
    private String listSettings(String listBy, Object parameter, Locale locale, Model model) {
        List<Settings> list = null;
        String title = "All settings";

        switch (listBy) {
            case "uid":
                Settings object = settingsRepository.findOne(Long.valueOf((String) parameter));
                if (object != null) {
                    list = new ArrayList<Settings>();
                    list.add(object);
                }
                title = "Setting with UID: " + parameter;
                break;
            case "lang":
                list = settingsRepository.getByLang((String) parameter, new Sort("uid"));
                title = "Settings with lang: " + parameter;
                break;
            default:
                list = settingsRepository.findAll(new Sort("uid"));
                title = "All settings";
        }

        if (list != null) {
            model.addAttribute("list", list);
        }
        model.addAttribute("title", title);

        return "list/settings";
    }
}
